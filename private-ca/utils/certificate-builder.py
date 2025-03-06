#!/usr/bin/env python3

import argparse
import boto3
from botocore.exceptions import ClientError
import time
import base64
from cryptography import x509

parser = argparse.ArgumentParser()
parser.add_argument("-e", "--issueraltname-email", type=str, dest="email", help="Email address to include in the Issuer Alternative Name")
parser.add_argument("-u", "--issueraltname-uri", type=str, dest="uri", help="URI to include in the Issuer Alternative Name")
parser.add_argument("-k", "--kms-arn", type=str, dest="kms", help="arn for asymmetric KMS key to use for Document Signing Certificate")
parser.add_argument("-c", "--ca", type=str, dest="ca", help="arn for the AWS Private Certificate Authority")
parser.add_argument("-n", "--common-name", type=str, dest="name", help="Common Name to embed in the Document Signing Certificate")
parser.add_argument("-i", "--issuer-alt-name", action="store_true", dest="issue", help="Output base64 string for Issuer Alt Name and exit")

args = parser.parse_args()

# Parameter validation
if not (args.email or args.uri):
    parser.error('At least one of --email or --uri must be specified')

if not (args.kms and args.ca and args.name) and not args.issue:
    parser.error('KMS arn, CA arn and Common Name must be specified unless just display the Issuer Alternative Name')

# import CSR Builder utility - requires active AWS credentials
try:
    from csrbuilderforkms.kmscsrbuilder import KMSCSRBuilder, pem_armor_csr
    pcaClient = boto3.client('acm-pca')
except Exception as e:
    print(f"An error occurred connecting to AWS - please make sure you have valid credentials available: {e}")
    exit(1)

# Build Issuer Alternative Name
try:
    general_names = []

    if args.email:
        general_names.append(x509.RFC822Name(args.email))

    if args.uri:
        general_names.append(x509.UniformResourceIdentifier(args.uri))

    issuerAltName = base64.b64encode(x509.IssuerAlternativeName(general_names).public_bytes()).decode("utf-8")
except Exception as e:
    print(f"An error occurred generating the Issuer Alternative Name: {e}")
    exit(1)

# print base64 string and exit if --issuer-alt-name is specified
if args.issue:
    print(issuerAltName)
    exit(0)

# Build CSR
try:
    builder = KMSCSRBuilder(
        {
            'common_name': args.name,
            'country_name': 'UK'
        },
        args.kms)
except Exception as e:
    print(f"An error occurred building the CSR: {e}")
    exit(1)

# Self-sign CSR with KMS key
try:
    csr = builder.build_with_kms(args.kms)
except Exception as e:
    print(f"An error occurred signing the CSR: {e}")
    exit(1)

# Submit request to CA to generate document signing certificate
try:
    issueResponse = pcaClient.issue_certificate(
        ApiPassthrough = {
            'Extensions': {
                'KeyUsage': {
                    'DigitalSignature': True
                },
                'ExtendedKeyUsage': [
                    {
                        'ExtendedKeyUsageObjectIdentifier': '1.0.18013.5.1.2'  # identifier for ISO mDL
                    }
                ],
                'CustomExtensions': [
                    {
                        'ObjectIdentifier': '2.5.29.18',
                        'Value': issuerAltName
                    }
                ]
            }
        },
        CertificateAuthorityArn = args.ca,
        Csr = pem_armor_csr(csr),
        TemplateArn = "arn:aws:acm-pca:::template/BlankEndEntityCertificate_APIPassthrough/V1",
        SigningAlgorithm = "SHA256WITHECDSA",
        Validity = {
            "Value": 1825,
            "Type": "DAYS"
        },
    )
except Exception as e:
    print(f"An error occurred with generating the certificate: {e}")
    exit(1)

# Get resulting document signing certificate from CA
while True:
    try:
        certificateResponse = pcaClient.get_certificate(
            CertificateAuthorityArn = args.ca,
            CertificateArn = issueResponse['CertificateArn']
        )
        break
    except ClientError as e:
        if e.response['Error']['Code'] == "RequestInProgressException":
            time.sleep(2)
            continue
        print(f"An error occurred retrieving the certificate: {e}")
        exit(1)
    except Exception as e:
        print(f"An error occurred retrieving the certificate: {e}")
        exit(1)

print(certificateResponse['Certificate'])

#!/usr/bin/env python3

from cryptography import x509
import base64
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("-e", "--email", type=str)
parser.add_argument("-u", "--uri", type=str)

args = parser.parse_args()

if not (args.email or args.uri):
    parser.error('At least one of --email or --uri must be specified')

general_names = []

if args.email:
    general_names.append(x509.RFC822Name(args.email))

if args.uri:
    general_names.append(x509.UniformResourceIdentifier(args.uri))

extension = x509.IssuerAlternativeName(general_names)

print(base64.b64encode(extension.public_bytes()))

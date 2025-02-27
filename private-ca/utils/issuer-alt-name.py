#!/usr/bin/env python3

from cryptography import x509
import base64

extension = x509.IssuerAlternativeName([x509.RFC822Name("issuer@example.account.gov.uk")])
print(base64.b64encode(extension.public_bytes()))

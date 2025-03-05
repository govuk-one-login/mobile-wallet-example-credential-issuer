#!/usr/bin/env python3

from csrbuilderforkms.kmscsrbuilder import KMSCSRBuilder, pem_armor_csr

kms_arn = 'arn:aws:kms:eu-west-2:671524980203:key/ecd24fc3-6916-4269-9781-4246225d98bc'

builder = KMSCSRBuilder(
    { 'common_name': 'DSC', 'country_name': 'UK'},
    kms_arn)

request = builder.build_with_kms(kms_arn)

print(pem_armor_csr(request).decode('utf-8'))

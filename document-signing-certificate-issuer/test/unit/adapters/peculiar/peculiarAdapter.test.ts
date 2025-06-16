import { createCertificateRequestFromEs256KmsKey } from '../../../../src/adapters/peculiar/peculiarAdapter';
import { getPublicKey, signWithEcdsaSha256 } from '../../../../src/adapters/aws/kmsAdapter';
import { mockEs256Csr, mockPublicKey, mockSignature, commonName, countryName } from '../../data/mockEs256Csr';
import { AsnEcSignatureFormatter } from '@peculiar/x509';

jest.mock('.../../../../src/adapters/aws/kmsAdapter');

describe('createCertificateRequestFromEs256KmsKey', () => {
  beforeEach(() => {
    jest.resetAllMocks();
    jest.mocked(getPublicKey).mockResolvedValueOnce(mockPublicKey);
    jest.mocked(signWithEcdsaSha256).mockResolvedValueOnce(mockSignature);
  });

  it('should create a CSR using an ES256 KMS Key via the KMS Adapter', async () => {
    // ACT
    const response = await createCertificateRequestFromEs256KmsKey(commonName, countryName, 'KEYID');

    // ASSERT
    expect(response).toEqual(mockEs256Csr);
  });

  it('should throw if the signature could not be converted to ASN.1', async () => {
    // ARRANGE
    jest.spyOn(AsnEcSignatureFormatter.prototype, 'toAsnSignature').mockImplementation(() => null);

    // ACT
    const promise = createCertificateRequestFromEs256KmsKey('commonName', 'UK', '0dda229a-4395-400c-947b-bed7ead4f16d');

    // ASSERT
    await expect(promise).rejects.toEqual(Error('Cannot convert WebCrypto signature value to ASN.1 format'));
  });
});

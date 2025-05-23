// This set of values correlate - i.e. a CSR containing commonName, countryName, mockPublicKey
// should also contain the signature mockSignature when signed using the private key corresponding
// to mockPublicKey, and should then be presented as mockCsr which is a PEM-formatted CSR.
//
// The use of these values enables the CSR adapter to be unit tested without needing a real KMS key.

export const commonName = 'commonName';

export const countryName = 'UK';

export const mockPublicKey = new Uint8Array([
  48, 89, 48, 19, 6, 7, 42, 134, 72, 206, 61, 2, 1, 6, 8, 42, 134, 72, 206, 61, 3, 1, 7, 3, 66, 0, 4, 232, 201, 134,
  233, 19, 81, 153, 130, 27, 201, 167, 95, 4, 79, 230, 37, 155, 179, 208, 242, 195, 143, 224, 88, 198, 59, 207, 26, 205,
  160, 129, 91, 45, 168, 103, 138, 129, 228, 35, 16, 253, 98, 31, 248, 246, 80, 232, 147, 126, 16, 144, 205, 43, 128,
  77, 252, 89, 51, 43, 165, 68, 216, 133, 182,
]);

export const mockSignature = new Uint8Array([
  48, 69, 2, 32, 111, 19, 190, 145, 175, 27, 67, 140, 83, 233, 183, 137, 133, 82, 157, 221, 30, 208, 69, 217, 185, 85,
  50, 138, 151, 15, 238, 208, 141, 205, 188, 105, 2, 33, 0, 239, 145, 143, 137, 166, 189, 22, 111, 213, 77, 127, 24,
  255, 244, 191, 178, 193, 129, 80, 61, 169, 38, 125, 216, 96, 17, 241, 246, 130, 53, 56, 19,
]);

export const mockEs256Csr =
  '-----BEGIN CERTIFICATE REQUEST-----\n' +
  'MIHdMIGEAgEAMCIxEzARBgNVBAMTCmNvbW1vbk5hbWUxCzAJBgNVBAYTAlVLMFkw\n' +
  'EwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE6MmG6RNRmYIbyadfBE/mJZuz0PLDj+BY\n' +
  'xjvPGs2ggVstqGeKgeQjEP1iH/j2UOiTfhCQzSuATfxZMyulRNiFtqAAMAoGCCqG\n' +
  'SM49BAMCA0gAMEUCIDBFAiBvE76RrxtDjFPpt4mFUp3dHtBF2blVMoqXD+7QAiEA\n' +
  'jc28aQIhAO+Rj4mmvRZv1U1/GP/0v7LBgVA9qSZ92GA=\n' +
  '-----END CERTIFICATE REQUEST-----';

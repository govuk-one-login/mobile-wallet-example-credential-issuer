import { GetPublicKeyCommand, KMSClient, SignCommand } from '@aws-sdk/client-kms';

const kmsClient = new KMSClient();

export async function getPublicKey(keyId: string) {
  const getPublicKeyCommand = new GetPublicKeyCommand({
    KeyId: keyId,
  });
  const getPublicKeyCommandOutput = await kmsClient.send(getPublicKeyCommand);
  const spki = getPublicKeyCommandOutput.PublicKey;
  if (spki === undefined) {
    throw new Error('Error retrieving public key from KMS');
  }
  return spki;
}

export async function signWithEcdsaSha256(kmsId: string, tbs: ArrayBuffer) {
  const signCommand = new SignCommand({
    KeyId: kmsId,
    Message: Buffer.from(tbs),
    SigningAlgorithm: 'ECDSA_SHA_256',
  });
  const signCommandOutput = await kmsClient.send(signCommand);
  const signature = signCommandOutput.Signature;
  if (signature === undefined) {
    throw new Error('An error occurred when signing the request with KMS');
  }
  return signature;
}

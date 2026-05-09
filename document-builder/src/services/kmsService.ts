import {
  GetPublicKeyCommand,
  KMSClient,
  SignCommand,
  SignCommandInput,
  SigningAlgorithmSpec,
} from "@aws-sdk/client-kms";
import { getKmsConfig } from "../config/aws";
import format from "ecdsa-sig-formatter";

export class KmsService {
  constructor(
    private readonly keyId: string,
    private readonly kmsClient: KMSClient = new KMSClient(getKmsConfig()),
  ) {}

  async sign(
    message: string,
    signingAlgorithm: SigningAlgorithmSpec,
  ): Promise<string> {
    const signCommandInput: SignCommandInput = {
      Message: Buffer.from(message),
      KeyId: this.keyId,
      SigningAlgorithm: signingAlgorithm,
      MessageType: "RAW",
    };

    const command: SignCommand = new SignCommand(signCommandInput);

    const response = await this.kmsClient.send(command);

    if (!response.Signature) {
      throw new Error("No signature returned");
    }

    const base64EncodedSignature = Buffer.from(response.Signature).toString(
      "base64url",
    );

    if (signingAlgorithm.startsWith("RSA")) {
      return base64EncodedSignature;
    } else {
      return format.derToJose(base64EncodedSignature, "ES256");
    }
  }

  public async getPublicKey() {
    const command: GetPublicKeyCommand = new GetPublicKeyCommand({
      KeyId: this.keyId,
    });

    const response = await this.kmsClient.send(command);

    if (!response.PublicKey) {
      throw new Error("No public key returned");
    }

    return this.parsePublicKey(response.PublicKey);
  }

  private parsePublicKey(publicKeyRaw: Uint8Array) {
    return Buffer.from(publicKeyRaw).toString("base64");
  }
}

import { didWebDocumentSchema } from "./didWebDocumentSchema";
import { JWK } from "jose";
import { getAjvInstance } from "../ajv/ajvInstance";

export interface DidDocument {
  "@context": string[];
  id: string;
  verificationMethod: VerificationMethod[];
  assertionMethod: string[];
}

export interface VerificationMethod {
  id: string;
  type: string;
  controller: string;
  publicKeyJwk: JWK;
}

export async function isValidDidWebDocument(
  didWebDocument: DidDocument,
  criDomain: string,
) {
  const ajv = getAjvInstance();
  const rulesValidator = ajv.compile(didWebDocumentSchema);

  if (!rulesValidator(didWebDocument)) {
    const validationErrors = rulesValidator.errors;
    throw new Error(
      `INVALID_DID_DOCUMENT: DID document does not comply with the schema. ${JSON.stringify(validationErrors)}`,
    );
  }

  // When running the CRI and test harness locally, replace domain "host.docker.internal" with "localhost" to match CRI URL
  criDomain = criDomain.replace("host.docker.internal", "localhost");
  const controller = "did:web:" + criDomain;
  if (didWebDocument.id !== controller) {
    throw new Error(
      `INVALID_DID_DOCUMENT: Invalid "id" value in DID document. Should be ${controller} but found ${didWebDocument.id}`,
    );
  }

  for (const item of didWebDocument.verificationMethod) {
    const id = controller + "#" + item.publicKeyJwk.kid;
    if (item.id !== id) {
      throw new Error(
        `INVALID_DID_DOCUMENT: Invalid "id" value in "verificationMethod". Should be ${id} but found ${item.id}`,
      );
    }

    if (item.controller !== controller) {
      throw new Error(
        `INVALID_DID_DOCUMENT: Invalid "controller" value in "verificationMethod". Should be ${controller} but found ${item.controller}`,
      );
    }

    if (!didWebDocument.assertionMethod.includes(id)) {
      throw new Error(
        `INVALID_DID_DOCUMENT: "id" ${id} is missing in "assertionMethod" ${didWebDocument.assertionMethod}`,
      );
    }
  }

  return true;
}

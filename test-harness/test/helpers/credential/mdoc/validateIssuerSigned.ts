import { getAjvInstance } from "../../ajv/ajvInstance";
import { IssuerSigned } from "./types/issuerSigned";
import { isoNamespaceSchema } from "./schemas/isoNamespaceSchema";
import { domesticNamespaceSchema } from "./schemas/domesticNamespaceSchema";
import { issuerSignedSchema } from "./schemas/issuerSignedSchema";
import { MDLValidationError } from "./MDLValidationError";

export function validateIssuerSignedSchema(issuerSigned: IssuerSigned): void {
  const ajv = getAjvInstance();

  if (!ajv.getSchema("isoNamespace")) {
    ajv.addSchema(isoNamespaceSchema, "isoNamespace");
  }
  if (!ajv.getSchema("domesticNamespace")) {
    ajv.addSchema(domesticNamespaceSchema, "domesticNamespace");
  }
  const validator = ajv.compile(issuerSignedSchema);

  if (!validator(issuerSigned)) {
    const errors =
      validator.errors?.map((error) => ({
        path: error.instancePath || "root",
        message: error.message || "Unknown validation error",
        value: error.data,
        keyword: error.keyword,
      })) || [];

    const errorDetails = errors
      .map((err) => `${err.path}: ${err.message}`)
      .join("; ");

    throw new MDLValidationError(
      `IssuerSigned does not comply with schema - ${errorDetails}`,
      "INVALID_SCHEMA",
    );
  }
}

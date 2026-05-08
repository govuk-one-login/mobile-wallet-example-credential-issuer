import { DocumentsConfig } from "../config/documentsConfig";

export function buildTemplateInputForDocuments(
  documentsConfig: DocumentsConfig,
) {
  return Object.entries(documentsConfig).map(([key, { name }]) => ({
    value: key,
    text: name,
  }));
}

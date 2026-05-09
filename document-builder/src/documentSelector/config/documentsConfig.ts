import { CredentialType } from "../../types/CredentialType";

export interface DocumentConfig {
  route: string;
  name: string;
}

export type DocumentsConfig = Record<string, DocumentConfig>;

export const documentsConfig: DocumentsConfig = {
  [CredentialType.SocialSecurityCredential]: {
    route: "/build-nino-document",
    name: "NINO",
  },
  [CredentialType.BasicDisclosureCredential]: {
    route: "/build-dbs-document",
    name: "DBS",
  },
  [CredentialType.DigitalVeteranCard]: {
    route: "/build-veteran-card-document",
    name: "Veteran Card",
  },
  [CredentialType.MobileDrivingLicence]: {
    route: "/build-driving-licence",
    name: "Driving Licence",
  },
  [CredentialType.SimpleDocument]: {
    route: "/build-simple-document",
    name: "Simple Document",
  },
};

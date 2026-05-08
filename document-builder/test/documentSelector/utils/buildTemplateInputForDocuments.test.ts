import { buildTemplateInputForDocuments } from "../../../src/documentSelector/utils/buildTemplateInputForDocuments";

describe("buildTemplateInputForDocuments", () => {
  it("should build array of objects containing 'value' and 'text' from input config", () => {
    const testConfig = {
      SocialSecurityCredential: {
        route: "/build-nino-document",
        name: "NINO",
      },
      BasicDisclosureCredential: {
        route: "/build-dbs-document",
        name: "DBS",
      },
      DigitalVeteranCard: {
        route: "/build-veteran-card-document",
        name: "Veteran Card",
      },
      MobileDrivingLicence: {
        route: "/build-driving-licence",
        name: "Driving Licence",
      },
    };
    const expectedOutput = [
      { value: "SocialSecurityCredential", text: "NINO" },
      { value: "BasicDisclosureCredential", text: "DBS" },
      { value: "DigitalVeteranCard", text: "Veteran Card" },
      { value: "MobileDrivingLicence", text: "Driving Licence" },
    ];

    const result = buildTemplateInputForDocuments(testConfig);

    expect(result).toEqual(expectedOutput);
  });
});

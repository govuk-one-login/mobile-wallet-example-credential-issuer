import { VeteranCardRequestBody } from "../../../src/veteranCardDocumentBuilder/types/VeteranCardRequestBody";
import { validateVeteranCardForm } from "../../../src/veteranCardDocumentBuilder/helpers/VeteranCardFormValidator";

const validBody: VeteranCardRequestBody = {
  givenName: "John",
  familyName: "Smith",
  "dateOfBirth-day": "01",
  "dateOfBirth-month": "01",
  "dateOfBirth-year": "1990",
  "cardExpiryDate-day": "01",
  "cardExpiryDate-month": "01",
  "cardExpiryDate-year": "2030",
  serviceNumber: "123456",
  serviceBranch: "Army",
  portrait: "portrait",
  credentialTtl: "2592000",
  "credentialExpiry-day": "",
  "credentialExpiry-month": "",
  "credentialExpiry-year": "",
  throwError: "",
};

describe("validateVeteranCardForm", () => {
  it("should return valid when credentialTtl is not 'other'", () => {
    const result = validateVeteranCardForm(validBody);

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return valid when credentialTtl is 'other' and expiry date is valid", () => {
    const result = validateVeteranCardForm({
      ...validBody,
      credentialTtl: "other",
      "credentialExpiry-day": "01",
      "credentialExpiry-month": "01",
      "credentialExpiry-year": "2030",
    });

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return an error when credentialTtl is 'other' and expiry date is invalid", () => {
    const result = validateVeteranCardForm({
      ...validBody,
      credentialTtl: "other",
      "credentialExpiry-day": "99",
      "credentialExpiry-month": "01",
      "credentialExpiry-year": "2030",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({
      credential_expiry_date: "Enter a valid credential expiry date",
    });
  });

  it("should return an error when expectedUpdateSeconds is not a number", () => {
    const result = validateVeteranCardForm({
      ...validBody,
      expectedUpdateSeconds: "abc",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({
      expected_update: "Enter a number",
    });
  });

  it("should return valid when expectedUpdateSeconds is a valid number", () => {
    const result = validateVeteranCardForm({
      ...validBody,
      expectedUpdateSeconds: "10",
    });

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return valid when expectedUpdateSeconds is empty", () => {
    const result = validateVeteranCardForm({
      ...validBody,
      expectedUpdateSeconds: "",
    });

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return valid when expectedUpdateSeconds is undefined", () => {
    const result = validateVeteranCardForm(validBody);

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });
});

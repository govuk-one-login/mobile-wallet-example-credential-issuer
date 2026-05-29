import { VeteranCardRequestBody } from "../../../src/veteranCardDocumentBuilder/types/VeteranCardRequestBody";
import { VeteranCardFormValidator } from "../../../src/veteranCardDocumentBuilder/helpers/VeteranCardFormValidator";

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

describe("VeteranCardFormValidator", () => {
  const validator = new VeteranCardFormValidator();

  it("should return valid when credentialTtl is not 'other'", () => {
    const result = validator.validate(validBody);

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return valid when credentialTtl is 'other' and expiry date is valid", () => {
    const result = validator.validate({
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
    const result = validator.validate({
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

  it("should return an error when expectedUpdateDays is not a number", () => {
    const result = validator.validate({
      ...validBody,
      expectedUpdateDays: "abc",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({
      expected_update: "Enter a number",
    });
  });

  it("should return valid when expectedUpdateDays is a valid number", () => {
    const result = validator.validate({
      ...validBody,
      expectedUpdateDays: "10",
    });

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return valid when expectedUpdateDays is empty", () => {
    const result = validator.validate({
      ...validBody,
      expectedUpdateDays: "",
    });

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return valid when expectedUpdateDays is undefined", () => {
    const result = validator.validate(validBody);

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });
});

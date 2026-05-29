import { DrivingLicenceRequestBody } from "../../../src/drivingLicenceBuilder/types/DrivingLicenceRequestBody";
import { validateDrivingLicenceForm } from "../../../src/drivingLicenceBuilder/helpers/DrivingLicenceFormValidator";

const validBody: DrivingLicenceRequestBody = {
  family_name: "Smith",
  given_name: "John",
  title: "Mr",
  welsh_licence: "false",
  portrait: "portrait",
  "birth-day": "01",
  "birth-month": "01",
  "birth-year": "1990",
  birth_place: "London",
  "issue-day": "01",
  "issue-month": "01",
  "issue-year": "2020",
  "expiry-day": "01",
  "expiry-month": "01",
  "expiry-year": "2030",
  issuing_authority: "DVLA",
  issuing_country: "GB",
  document_number: "SMITH123456",
  resident_address: ["1 Test Street"],
  resident_postal_code: "SW1A 1AA",
  resident_city: "London",
  fullVehicleCategoryCode: "B",
  fullRestrictionCodes: "",
  "fullPrivilegeIssue-day": "01",
  "fullPrivilegeIssue-month": "01",
  "fullPrivilegeIssue-year": "2020",
  "fullPrivilegeExpiry-day": "01",
  "fullPrivilegeExpiry-month": "01",
  "fullPrivilegeExpiry-year": "2030",
  credentialTtl: "2592000",
  "credentialExpiry-day": "",
  "credentialExpiry-month": "",
  "credentialExpiry-year": "",
  throwError: "",
};

describe("validateDrivingLicenceForm", () => {
  it("should return valid when all dates are valid", () => {
    const result = validateDrivingLicenceForm(validBody);

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return an error for an invalid birth date", () => {
    const result = validateDrivingLicenceForm({
      ...validBody,
      "birth-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({ birth_date: "Enter a valid birth date" });
  });

  it("should return an error for an invalid issue date", () => {
    const result = validateDrivingLicenceForm({
      ...validBody,
      "issue-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({ issue_date: "Enter a valid issue date" });
  });

  it("should return an error for an invalid expiry date", () => {
    const result = validateDrivingLicenceForm({
      ...validBody,
      "expiry-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({ expiry_date: "Enter a valid expiry date" });
  });

  it("should return valid when credentialTtl is 'other' and expiry date is valid", () => {
    const result = validateDrivingLicenceForm({
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
    const result = validateDrivingLicenceForm({
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

  it("should return multiple errors for multiple invalid dates", () => {
    const result = validateDrivingLicenceForm({
      ...validBody,
      "birth-day": "99",
      "issue-day": "99",
      "expiry-day": "99",
      credentialTtl: "other",
      "credentialExpiry-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({
      birth_date: "Enter a valid birth date",
      credential_expiry_date: "Enter a valid credential expiry date",
      issue_date: "Enter a valid issue date",
      expiry_date: "Enter a valid expiry date",
    });
  });
});

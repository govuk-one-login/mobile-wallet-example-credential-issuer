import { validateSimpleDocumentForm } from "../../../src/simpleDocumentBuilder/helpers/SimpleDocumentFormValidator";
import { SimpleDocumentRequestBody } from "../../../src/simpleDocumentBuilder/types/SimpleDocumentRequestBody";

const validBody: SimpleDocumentRequestBody = {
  family_name: "Smith",
  given_name: "John",
  portrait: "portrait",
  "birth-day": "01",
  "birth-month": "01",
  "birth-year": "1990",
  "issue-day": "01",
  "issue-month": "01",
  "issue-year": "2020",
  "expiry-day": "01",
  "expiry-month": "01",
  "expiry-year": "2030",
  issuing_country: "GB",
  document_number: "FLN123456",
  type_of_fish: "Coarse fish",
  number_of_fishing_rods: "2",
  credentialTtl: "2592000",
  throwError: "",
};

describe("validateSimpleDocumentForm", () => {
  it("should return valid when all fields are valid", () => {
    const result = validateSimpleDocumentForm(validBody);

    expect(result.isValid).toBe(true);
    expect(result.errors).toEqual({});
  });

  it("should return an error for an invalid birth date", () => {
    const result = validateSimpleDocumentForm({
      ...validBody,
      "birth-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({ birth_date: "Enter a valid birth date" });
  });

  it("should return an error for an invalid issue date", () => {
    const result = validateSimpleDocumentForm({
      ...validBody,
      "issue-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({ issue_date: "Enter a valid issue date" });
  });

  it("should return an error for an invalid expiry date", () => {
    const result = validateSimpleDocumentForm({
      ...validBody,
      "expiry-day": "99",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({ expiry_date: "Enter a valid expiry date" });
  });

  it("should return an error for an invalid fish type", () => {
    const result = validateSimpleDocumentForm({
      ...validBody,
      type_of_fish: "Invalid fish",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({
      type_of_fish: "Select a valid type of fish",
    });
  });

  it("should return multiple errors for multiple invalid fields", () => {
    const result = validateSimpleDocumentForm({
      ...validBody,
      "birth-day": "99",
      type_of_fish: "Invalid fish",
    });

    expect(result.isValid).toBe(false);
    expect(result.errors).toEqual({
      birth_date: "Enter a valid birth date",
      type_of_fish: "Select a valid type of fish",
    });
  });
});

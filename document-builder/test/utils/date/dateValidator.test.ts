import {
  isValidDate,
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
  validateCredentialExpiryDate,
} from "../../../src/utils/date";

describe("validateBirthDate", () => {
  it("should return no errors for a valid date", () => {
    expect(validateBirthDate("01", "01", "2025")).toEqual({});
  });

  it("should return an error for an invalid date", () => {
    expect(validateBirthDate("32", "01", "2025")).toEqual({
      birth_date: "Enter a valid birth date",
    });
  });
});

describe("validateIssueDate", () => {
  it("should return no errors for a valid date", () => {
    expect(validateIssueDate("14", "05", "2025")).toEqual({});
  });

  it("should return an error for an invalid date", () => {
    expect(validateIssueDate("99", "05", "2025")).toEqual({
      issue_date: "Enter a valid issue date",
    });
  });
});

describe("validateExpiryDate", () => {
  it("should return no errors for a valid date", () => {
    expect(validateExpiryDate("16", "06", "2025")).toEqual({});
  });

  it("should return an error for an invalid date", () => {
    expect(validateExpiryDate("00", "06", "2025")).toEqual({
      expiry_date: "Enter a valid expiry date",
    });
  });
});

describe("validateCredentialExpiryDate", () => {
  it("should return no errors for a valid date", () => {
    expect(validateCredentialExpiryDate("01", "01", "2025")).toEqual({});
  });

  it("should return an error for an invalid date", () => {
    expect(validateCredentialExpiryDate("aa", "01", "2025")).toEqual({
      credential_expiry_date: "Enter a valid credential expiry date",
    });
  });
});

describe("isValidDate", () => {
  it("should return true for a valid date", () => {
    expect(isValidDate("19", "05", "2025")).toBe(true);
    expect(isValidDate("01", "01", "2000")).toBe(true);
    expect(isValidDate("2", "2", "2000")).toBe(true);
    expect(isValidDate("29", "02", "2024")).toBe(true); // Leap year
  });

  it("should return false for invalid day", () => {
    expect(isValidDate("", "01", "2025")).toBe(false);
    expect(isValidDate("aa", "01", "2025")).toBe(false);
    expect(isValidDate("32", "01", "2025")).toBe(false);
    expect(isValidDate("00", "10", "2025")).toBe(false);
  });

  it("should return false for invalid month", () => {
    expect(isValidDate("10", "", "2025")).toBe(false);
    expect(isValidDate("10", "aa", "2025")).toBe(false);
    expect(isValidDate("10", "13", "2025")).toBe(false);
    expect(isValidDate("15", "00", "2025")).toBe(false);
  });

  it("should return false for invalid year", () => {
    expect(isValidDate("10", "10", "")).toBe(false);
    expect(isValidDate("10", "10", "abcd")).toBe(false);
    expect(isValidDate("10", "10", "24")).toBe(false);
  });

  it("should return false for February 29 in a non-leap year", () => {
    expect(isValidDate("29", "02", "2023")).toBe(false);
    expect(isValidDate("29", "02", "1900")).toBe(false); // 1900 is not a leap year
  });

  it("should return true for February 29 in a leap year", () => {
    expect(isValidDate("29", "02", "2024")).toBe(true);
    expect(isValidDate("29", "02", "2000")).toBe(true); // 2000 is a leap year
  });
});

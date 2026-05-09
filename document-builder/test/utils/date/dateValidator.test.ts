import {
  isValidDate,
  validateDateFields,
} from "../../../src/utils/date/dateValidator";
import { DrivingLicenceRequestBody } from "../../../src/drivingLicenceBuilder/types/DrivingLicenceRequestBody";

describe("validateDateFields", () => {
  it("should return no errors if all dates are valid", () => {
    const body = {
      "birth-day": "01",
      "birth-month": "01",
      "birth-year": "2025",
      "issue-day": "14",
      "issue-month": "05",
      "issue-year": "2025",
      "expiry-day": "16",
      "expiry-month": "06",
      "expiry-year": "2025",
    } as DrivingLicenceRequestBody;

    expect(validateDateFields(body)).toEqual({});
  });

  it("should return an error for an invalid birth date", () => {
    const body = {
      "birth-day": "32", // 32 is not a valid day
      "birth-month": "01",
      "birth-year": "2025",
      "issue-day": "14",
      "issue-month": "05",
      "issue-year": "2025",
      "expiry-day": "16",
      "expiry-month": "06",
      "expiry-year": "2025",
    } as DrivingLicenceRequestBody;

    expect(validateDateFields(body)).toEqual({
      birth_date: "Enter a valid birth date",
    });
  });

  it("should return an error for an invalid issue date", () => {
    const body = {
      "birth-day": "01",
      "birth-month": "01",
      "birth-year": "2025",
      "issue-day": "99", // 99 is not a valid day
      "issue-month": "05",
      "issue-year": "2025",
      "expiry-day": "16",
      "expiry-month": "06",
      "expiry-year": "2025",
    } as DrivingLicenceRequestBody;

    expect(validateDateFields(body)).toEqual({
      issue_date: "Enter a valid issue date",
    });
  });

  it("should return an error for an invalid expiry date", () => {
    const body = {
      "birth-day": "01",
      "birth-month": "01",
      "birth-year": "2025",
      "issue-day": "15",
      "issue-month": "05",
      "issue-year": "2025",
      "expiry-day": "00", // 00 is not a valid day
      "expiry-month": "06",
      "expiry-year": "2025",
    } as DrivingLicenceRequestBody;

    expect(validateDateFields(body)).toEqual({
      expiry_date: "Enter a valid expiry date",
    });
  });

  it("should return errors for multiple invalid dates", () => {
    const body = {
      "birth-day": "32", // 32 is not a valid day
      "birth-month": "01",
      "birth-year": "2025",
      "issue-day": "aa", // aa is not a valid day
      "issue-month": "05",
      "issue-year": "2025",
      "expiry-day": "$$", // $$ is not a valid day
      "expiry-month": "06",
      "expiry-year": "2025",
    } as DrivingLicenceRequestBody;

    expect(validateDateFields(body)).toEqual({
      birth_date: "Enter a valid birth date",
      issue_date: "Enter a valid issue date",
      expiry_date: "Enter a valid expiry date",
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

  it("should return false for non-leap year when date is February 29", () => {
    expect(isValidDate("29", "02", "2023")).toBe(false);
    expect(isValidDate("29", "02", "1900")).toBe(false); // 1900 is not a leap year
  });

  it("should return true for leap year when date is February 29", () => {
    expect(isValidDate("29", "02", "2024")).toBe(true);
    expect(isValidDate("29", "02", "2000")).toBe(true); // 2000 is a leap year
  });
});

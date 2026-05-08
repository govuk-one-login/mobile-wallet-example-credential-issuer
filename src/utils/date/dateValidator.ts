import { DrivingLicenceRequestBody } from "../../drivingLicenceBuilder/types/DrivingLicenceRequestBody";
import { SimpleDocumentRequestBody } from "../../simpleDocumentBuilder/types/SimpleDocumentRequestBody";

/**
 * Validates the date fields in a request body and returns error messages for any invalid dates.
 *
 * This function checks three date fields: birth date, issue date, and expiry date.
 * For each date, it uses the `isValidDate` function to determine if the provided day, month, and year
 * values form a valid calendar date. If a date is invalid, an error message is added to the returned object
 * under a corresponding key.
 *
 * @param {MdlRequestBody | SimpleDocumentRequestBody} body The request body object containing date fields as strings.
 * @returns {Record<string, string>} An object mapping field names to error messages for any invalid dates.
 * If all dates are valid, the returned object will be empty.
 *
 * @example
 * const body = {
 *   "birth-day": "31", "birth-month": "2", "birth-year": "2023",
 *   "issue-day": "15", "issue-month": "4", "issue-year": "2022",
 *   "expiry-day": "30", "expiry-month": "6", "expiry-year": "2030"
 * };
 * validateDateFields(body); // { birth_date: "Enter a valid birth date" }
 */
export function validateDateFields(
  body: DrivingLicenceRequestBody | SimpleDocumentRequestBody,
): Record<string, string> {
  const errors: Record<string, string> = {};

  if (
    !isValidDate(body["birth-day"], body["birth-month"], body["birth-year"])
  ) {
    errors["birth_date"] = "Enter a valid birth date";
  }

  if (
    !isValidDate(body["issue-day"], body["issue-month"], body["issue-year"])
  ) {
    errors["issue_date"] = "Enter a valid issue date";
  }

  if (
    !isValidDate(body["expiry-day"], body["expiry-month"], body["expiry-year"])
  ) {
    errors["expiry_date"] = "Enter a valid expiry date";
  }

  return errors;
}

/**
 * Checks if the provided day, month, and year strings represent a valid calendar date.
 *
 * This function parses the input strings as integers and attempts to construct a JavaScript `Date` object.
 * It then verifies that the resulting date matches the input values, accounting for JavaScript's
 * zero-based month indexing and handling invalid dates (e.g., February 30th, April 31st).
 *
 * @param {string} dayStr The day of the month (e.g., "15").
 * @param {string} monthStr The month (1-based, e.g., "4" for April).
 * @param {string} yearStr The full year (e.g., "2024").
 * @returns {boolean} Returns `true` if the inputs represent a valid date; otherwise, returns `false`.
 *
 * @example
 * isValidDate("29", "2", "2024"); // true (Leap year)
 * isValidDate("31", "4", "2023"); // false (April has 30 days)
 * isValidDate("15", "13", "2023"); // false (Invalid month)
 * isValidDate("abc", "2", "2023"); // false (Invalid day)
 */
export function isValidDate(
  dayStr: string,
  monthStr: string,
  yearStr: string,
): boolean {
  const dayNum = Number.parseInt(dayStr);
  const monthNum = Number.parseInt(monthStr);
  const yearNum = Number.parseInt(yearStr);

  const date = new Date(yearNum, monthNum - 1, dayNum); // Month is 0-indexed in JavaScript

  if (Number.isNaN(date.getTime())) return false;

  return (
    date.getFullYear() === yearNum &&
    date.getMonth() === monthNum - 1 &&
    date.getDate() === dayNum
  );
}

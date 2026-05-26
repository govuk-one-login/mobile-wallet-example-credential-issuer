export function validateBirthDate(
  day: string,
  month: string,
  year: string,
): Record<string, string> {
  if (!isValidDate(day, month, year)) {
    return { birth_date: "Enter a valid birth date" };
  }
  return {};
}

export function validateIssueDate(
  day: string,
  month: string,
  year: string,
): Record<string, string> {
  if (!isValidDate(day, month, year)) {
    return { issue_date: "Enter a valid issue date" };
  }
  return {};
}

export function validateExpiryDate(
  day: string,
  month: string,
  year: string,
): Record<string, string> {
  if (!isValidDate(day, month, year)) {
    return { expiry_date: "Enter a valid expiry date" };
  }
  return {};
}

export function validateCredentialExpiryDate(
  day: string,
  month: string,
  year: string,
): Record<string, string> {
  if (!isValidDate(day, month, year)) {
    return { credential_expiry_date: "Enter a valid credential expiry date" };
  }
  return {};
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

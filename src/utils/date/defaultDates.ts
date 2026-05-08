interface DateParts {
  day: string;
  month: string;
  year: string;
}

/**
 * Generates default issue and expiry dates.
 *
 * The default issue date is set to the current date.
 * The default expiry date is set to 10 years from the issue date, minus one day.
 * Both dates are returned as objects containing zero-padded day and month strings and a four-digit year.
 *
 * @returns {{ defaultIssueDate: DateParts, defaultExpiryDate: DateParts }}
 *
 * @example
 * const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
 * // { defaultIssueDate: { day: "16", month: "05", year: "2025" }, defaultExpiryDate: { day: "15", month: "05", year: "2035" } }
 */
export function getDefaultDates(): {
  defaultIssueDate: DateParts;
  defaultExpiryDate: DateParts;
} {
  const issueDate = new Date();
  const expiryDate = new Date(issueDate);
  expiryDate.setFullYear(expiryDate.getFullYear() + 10);
  expiryDate.setDate(expiryDate.getDate() - 1);

  return {
    defaultIssueDate: getDateParts(issueDate),
    defaultExpiryDate: getDateParts(expiryDate),
  };
}

/**
 * Converts a JavaScript Date object into a DateParts object with zero-padded day and month.
 *
 * @param {Date} date The JavaScript Date object to convert.
 * @returns {DateParts} An object with string representations of the day, month, and year.
 *
 * @example
 * getDateParts(new Date("2025-05-16")); // { day: "16", month: "05", year: "2025" }
 */
export function getDateParts(date: Date): DateParts {
  return {
    day: String(date.getDate()).padStart(2, "0"),
    month: String(date.getMonth() + 1).padStart(2, "0"),
    year: date.getFullYear().toString(),
  };
}

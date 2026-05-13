/**
 * Formats day, month, and year strings into a "DD-MM-YYYY" date format.
 *
 * This function pads the day and month values with a leading zero if necessary to ensure
 * two-digit representation, then concatenates them with the year using hyphens as separators.
 *
 * @param {string} day The day of the month (e.g., "7" or "15").
 * @param {string} month The month (e.g., "4" or "11").
 * @param {string} year The year (e.g., "2024").
 * @returns {string} The formatted date string in "DD-MM-YYYY" format.
 *
 * @example
 * formatDate("3", "4", "2025"); // "03-04-2025"
 * formatDate("15", "11", "1999"); // "15-11-1999"
 */
export function formatDate(day: string, month: string, year: string): string {
  const paddedDay = day.padStart(2, "0");
  const paddedMonth = month.padStart(2, "0");
  return `${paddedDay}-${paddedMonth}-${year}`;
}

import {
  getDateParts,
  getDefaultDates,
} from "../../../src/utils/date/defaultDates";

describe("getDateParts", () => {
  it("should return zero-padded day and month, and four-digit year", () => {
    const date = new Date(2025, 4, 6); // May 6, 2025

    const parts = getDateParts(date);

    expect(parts).toEqual({ day: "06", month: "05", year: "2025" });
  });

  it("should handle single-digit months and days", () => {
    const date = new Date(2025, 0, 1); // Jan 1, 2025

    const parts = getDateParts(date);

    expect(parts).toEqual({ day: "01", month: "01", year: "2025" });
  });

  it("should handle double-digit months and days", () => {
    const date = new Date(2025, 11, 24); // Dec 24, 2025

    const parts = getDateParts(date);

    expect(parts).toEqual({ day: "24", month: "12", year: "2025" });
  });
});

describe("getDefaultDates", () => {
  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(new Date("2025-05-16T12:00:00Z"));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("should return correct default issue and expiry dates", () => {
    const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();

    expect(defaultIssueDate).toEqual({ day: "16", month: "05", year: "2025" });
    expect(defaultExpiryDate).toEqual({ day: "15", month: "05", year: "2035" });
  });

  it("should handle leap years correctly", () => {
    jest.setSystemTime(new Date("2024-02-29T00:00:00Z")); // Leap year

    const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();

    expect(defaultIssueDate).toEqual({ day: "29", month: "02", year: "2024" });
    expect(defaultExpiryDate).toEqual({ day: "28", month: "02", year: "2034" });
  });

  it("should handle end-of-year rollover", () => {
    jest.setSystemTime(new Date("2020-01-01T00:00:00Z"));

    const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();

    expect(defaultIssueDate).toEqual({ day: "01", month: "01", year: "2020" });
    expect(defaultExpiryDate).toEqual({ day: "31", month: "12", year: "2029" });
  });
});

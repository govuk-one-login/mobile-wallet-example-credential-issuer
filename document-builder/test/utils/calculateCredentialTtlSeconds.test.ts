import { calculateCredentialTtlSeconds } from "../../src/utils/calculateCredentialTtlSeconds";

describe("calculateCredentialTtlSeconds", () => {
  afterEach(() => {
    jest.useRealTimers();
  });

  it("should return the seconds between now and the expiry date", () => {
    jest.useFakeTimers().setSystemTime(new Date("2025-05-02T00:00:00Z"));
    expect(calculateCredentialTtlSeconds("02", "05", "2026")).toBe(31536000);
  });

  it("should return whole days regardless of time of day", () => {
    jest.useFakeTimers().setSystemTime(new Date("2025-05-02T13:45:00Z"));
    expect(calculateCredentialTtlSeconds("02", "05", "2026")).toBe(31536000);
  });
});

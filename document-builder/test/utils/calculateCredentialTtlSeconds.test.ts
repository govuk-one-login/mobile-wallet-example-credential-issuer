import { calculateCredentialTtlSeconds } from "../../src/utils/calculateCredentialTtlSeconds";

describe("calculateCredentialTtlSeconds", () => {
  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(new Date("2025-05-02T00:00:00Z"));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("should return the seconds between now and the expiry date", () => {
    expect(calculateCredentialTtlSeconds("02", "05", "2026")).toBe(31536000);
  });
});

import { getRandomIntInclusive } from "../../src/utils/getRandomIntInclusive";

describe("getRandomIntInclusive", () => {
  it("should return an integer within the range [100000, 999999]", () => {
    const result = getRandomIntInclusive();

    expect(Number.isInteger(result)).toBe(true);
    expect(result).toBeGreaterThanOrEqual(100000);
    expect(result).toBeLessThanOrEqual(999999);
  });
});

import { ISSUING_AUTHORITY } from "../../src/config/issuingAuthority";

describe("issuingAuthority", () => {
  describe("ISSUING_AUTHORITY", () => {
    it("should be set to GDS", () => {
      expect(ISSUING_AUTHORITY).toBe("GDS");
    });
  });
});

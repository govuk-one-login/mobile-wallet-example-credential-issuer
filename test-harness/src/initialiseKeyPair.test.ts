import { initialiseKeyPair } from "./initialiseKeyPair";

jest.mock("node:fs/promises", () => ({
  writeFile: jest.fn().mockResolvedValue(undefined),
}));
jest.mock("./config", () => ({
  getKeyId: jest.fn().mockReturnValue("test-kid"),
}));

import { writeFile } from "node:fs/promises";

describe("initialiseKeyPair", () => {
  beforeEach(() => jest.clearAllMocks());

  it("returns a public key with kid set", async () => {
    const publicKey = await initialiseKeyPair();

    expect(publicKey.kty).toBe("EC");
    expect(publicKey.crv).toBe("P-256");
    expect(publicKey.kid).toBe("test-kid");
  });

  it("writes private and public key files", async () => {
    await initialiseKeyPair();

    expect(writeFile).toHaveBeenCalledTimes(2);
    expect(writeFile).toHaveBeenCalledWith(
      "test/helpers/credential/privateKey",
      expect.any(String),
    );
    expect(writeFile).toHaveBeenCalledWith(
      "test/helpers/credential/publicKey",
      expect.any(String),
    );
  });

  it("rejects when file write fails", async () => {
    (writeFile as jest.Mock).mockRejectedValueOnce(new Error("disk full"));

    await expect(initialiseKeyPair()).rejects.toThrow("disk full");
  });
});

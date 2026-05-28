import { createApp } from "./app";
import { initialiseKeyPair } from "./initialiseKeyPair";
import { buildJwksResponse } from "./buildJwksResponse";

jest.mock("./initialiseKeyPair");
jest.mock("./buildJwksResponse");

const mockPublicKey = {
  kty: "EC",
  crv: "P-256",
  x: "test-x",
  y: "test-y",
  kid: "test-kid",
};

beforeEach(() => {
  (initialiseKeyPair as jest.Mock).mockResolvedValue(mockPublicKey);
  (buildJwksResponse as jest.Mock).mockReturnValue(jest.fn());
});

describe("createApp", () => {
  it("initialises on startup", async () => {
    await createApp();

    expect(initialiseKeyPair).toHaveBeenCalledTimes(1);
    expect(buildJwksResponse).toHaveBeenCalledTimes(1);
    expect(buildJwksResponse).toHaveBeenCalledWith(mockPublicKey);
  });

  it("disables x-powered-by", async () => {
    const app = await createApp();
    expect(app.get("x-powered-by")).toBe(false);
  });
});

import { buildJwksResponse } from "./buildJwksResponse";
import { JWK } from "jose";
import { Request, Response } from "express";

const mockPublicKey: JWK = {
  kty: "EC",
  crv: "P-256",
  x: "test-x",
  y: "test-y",
  kid: "test-kid",
};

const mockRes = () => {
  const res = {} as Response;
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
};

describe("buildJwksResponse", () => {
  it("returns 200 with the public key in a keys array", async () => {
    const res = mockRes();

    await buildJwksResponse(mockPublicKey)({} as Request, res);

    const body = (res.json as jest.Mock).mock.calls[0][0];

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({ keys: [mockPublicKey] });
    expect(body.keys).toHaveLength(1);
  });

  it("returns 500 when public key is not present", async () => {
    const res = mockRes();

    await buildJwksResponse(null as unknown as JWK)({} as Request, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      error: "Public key not initialised",
    });
  });
});

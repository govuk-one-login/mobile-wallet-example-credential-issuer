import { NextFunction } from "express";
import * as proofJwt from "../../src/proofJwt/proofJwt";
import * as appConfig from "../../src/config/appConfig";
import { proofJwtController } from "../../src/proofJwt/controller";
import { getMockReq, getMockRes } from "@jest-mock/express";

describe("controller.ts", () => {
  let nextFunction: NextFunction;

  beforeEach(() => {
    jest.clearAllMocks();
    nextFunction = jest.fn();
  });

  it("should return 200 and proof JWT when request is successful", async () => {
    jest
      .spyOn(appConfig, "getMockProofSigningKeyId")
      .mockReturnValue("mock_signing_key_id");
    const mockSignedJwt = "signed jwt token";
    jest.spyOn(proofJwt, "getProofJwt").mockResolvedValue(mockSignedJwt);

    const req = getMockReq({
      query: {
        nonce: "test-nonce",
        audience: "test-audience",
      },
    });
    const { res } = getMockRes();

    await proofJwtController(req, res, nextFunction);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({ proofJwt: mockSignedJwt });
    expect(nextFunction).not.toHaveBeenCalled();
  });

  it("should call next with error when signing fails", async () => {
    jest
      .spyOn(appConfig, "getMockProofSigningKeyId")
      .mockReturnValue("mock_signing_key_id");
    jest
      .spyOn(proofJwt, "getProofJwt")
      .mockRejectedValue(new Error("Signing failed"));

    const req = getMockReq({
      query: {
        nonce: "test-nonce",
        audience: "test-audience",
      },
    });
    const { res } = getMockRes();

    await proofJwtController(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith(
      expect.objectContaining({
        message: "An error happened processing proof JWT request",
      }),
    );
    expect(res.status).not.toHaveBeenCalled();
  });
});

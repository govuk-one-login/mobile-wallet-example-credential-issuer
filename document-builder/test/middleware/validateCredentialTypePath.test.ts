import { NextFunction } from "express";
import { validateCredentialTypePath } from "../../src/middleware/validateCredentialTypePath";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { logger } from "../../src/middleware/logger";

describe("validateCredentialTypePath", () => {
  const loggerErrorSpy = jest
    .spyOn(logger, "error")
    .mockImplementation(() => undefined);

  it.each([
    ["SocialSecurityCredential"],
    ["BasicDisclosureCredential"],
    ["DigitalVeteranCard"],
    ["org.iso.18013.5.1.mDL"],
  ])("should call next when path=%s", (path) => {
    const req = getMockReq({
      params: { credentialType: path },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateCredentialTypePath(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalled();
    expect(loggerErrorSpy).not.toHaveBeenCalled();
  });

  it("should call next with error when path is invalid", () => {
    const req = getMockReq({
      params: { credentialType: "NotAValidPath" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateCredentialTypePath(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith(
      new Error("Invalid credential type path parameter provided"),
    );
    expect(loggerErrorSpy).toHaveBeenCalledWith(
      { credentialType: "NotAValidPath" },
      "Invalid credential type path parameter provided",
    );
  });
});

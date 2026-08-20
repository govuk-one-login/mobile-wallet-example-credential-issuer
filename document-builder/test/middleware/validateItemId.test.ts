import { NextFunction } from "express";
import { validateItemId } from "../../src/middleware/validateItemId";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { logger } from "../../src/middleware/logger";

describe("validateItemId", () => {
  const loggerErrorSpy = jest
    .spyOn(logger, "error")
    .mockImplementation(() => undefined);

  it("should call next when itemId is a valid UUID", () => {
    const req = getMockReq({
      params: { itemId: "550e8400-e29b-41d4-a716-446655440000" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateItemId(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalled();
    expect(nextFunction).toHaveBeenCalledWith();
    expect(loggerErrorSpy).not.toHaveBeenCalled();
  });

  it("should call next when itemId is a valid uppercase UUID", () => {
    const req = getMockReq({
      params: { itemId: "550E8400-E29B-41D4-A716-446655440000" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateItemId(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith();
  });

  it("should call next with error when itemId is not a valid UUID", () => {
    const req = getMockReq({
      params: { itemId: "not-a-uuid" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateItemId(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith(
      new Error("Invalid itemId path parameter provided"),
    );
    expect(loggerErrorSpy).toHaveBeenCalledWith(
      { itemId: "not-a-uuid" },
      "Invalid itemId path parameter provided",
    );
  });

  it("should call next with error when itemId contains special characters", () => {
    const req = getMockReq({
      params: { itemId: "<script>alert('xss')</script>" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateItemId(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith(
      new Error("Invalid itemId path parameter provided"),
    );
  });

  it("should call next with error when itemId is an empty string", () => {
    const req = getMockReq({
      params: { itemId: "" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateItemId(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith(
      new Error("Invalid itemId path parameter provided"),
    );
  });

  it("should call next with error when itemId has extra characters appended to a valid UUID", () => {
    const req = getMockReq({
      params: { itemId: "550e8400-e29b-41d4-a716-446655440000-extra" },
    });
    const { res } = getMockRes();
    const nextFunction: NextFunction = jest.fn();

    validateItemId(req, res, nextFunction);

    expect(nextFunction).toHaveBeenCalledWith(
      new Error("Invalid itemId path parameter provided"),
    );
  });
});

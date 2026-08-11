import { getMockReq, getMockRes } from "@jest-mock/express";
import type { NextFunction } from "express";
import { generateCspNonce } from "../../src/middleware/cspNonce";

describe("generateCspNonce", () => {
  it("should set res.locals.cspNonce to a base64 string", () => {
    const req = getMockReq();
    const { res } = getMockRes();
    const next: NextFunction = jest.fn();

    generateCspNonce(req, res, next);

    expect(res.locals.cspNonce).toBeDefined();
    expect(typeof res.locals.cspNonce).toBe("string");
    expect(res.locals.cspNonce).toMatch(/^[A-Za-z0-9+/=]+$/);
  });

  it("should generate a 16-byte nonce (24 base64 characters)", () => {
    const req = getMockReq();
    const { res } = getMockRes();
    const next: NextFunction = jest.fn();

    generateCspNonce(req, res, next);

    // 16 bytes = 24 base64 characters (with padding)
    expect(res.locals.cspNonce).toHaveLength(24);
  });

  it("should generate a unique nonce per request", () => {
    const req1 = getMockReq();
    const { res: res1 } = getMockRes();
    const next1: NextFunction = jest.fn();

    const req2 = getMockReq();
    const { res: res2 } = getMockRes();
    const next2: NextFunction = jest.fn();

    generateCspNonce(req1, res1, next1);
    generateCspNonce(req2, res2, next2);

    expect(res1.locals.cspNonce).not.toBe(res2.locals.cspNonce);
  });

  it("should call next()", () => {
    const req = getMockReq();
    const { res } = getMockRes();
    const next: NextFunction = jest.fn();

    generateCspNonce(req, res, next);

    expect(next).toHaveBeenCalledTimes(1);
  });
});

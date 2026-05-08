import { Request, Response } from "express";
import { deleteCookies } from "../../../src/logout/utils/deleteCookies";

describe("deleteCookies", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  const req = {
    cookies: {
      id_token: "id_token",
      access_token: "access_token",
      app: "app",
    },
  } as unknown as Request;
  const res = {
    clearCookie: jest.fn(),
  } as unknown as Response;

  it("should clear cookie when only one cookie matches", () => {
    const cookiesToDelete = ["id_token"];
    deleteCookies(req, res, cookiesToDelete);
    expect(res.clearCookie).toHaveBeenCalledTimes(1);
    expect(res.clearCookie).toHaveBeenCalledWith("id_token");
  });

  it("should clear cookies when many cookies match", () => {
    const cookiesToDelete = ["id_token", "access_token", "app"];
    deleteCookies(req, res, cookiesToDelete);
    expect(res.clearCookie).toHaveBeenCalledTimes(3);
    expect(res.clearCookie).toHaveBeenNthCalledWith(1, "id_token");
    expect(res.clearCookie).toHaveBeenNthCalledWith(2, "access_token");
    expect(res.clearCookie).toHaveBeenNthCalledWith(3, "app");
  });

  it("should not clear cookies when no cookies match", () => {
    const cookiesToDelete = ["state"];
    deleteCookies(req, res, cookiesToDelete);
    expect(res.clearCookie).toHaveBeenCalledTimes(0);
  });
});

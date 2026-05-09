import { getMockReq, getMockRes } from "@jest-mock/express";
import { pageNotFound } from "../../src/middleware/pageNotFound";

describe("pageNotFound", () => {
  it("should call next if headers were already sent", () => {
    const req = getMockReq();
    const { res, next } = getMockRes({
      headersSent: true,
    });

    pageNotFound(req, res, next);

    expect(next).toHaveBeenCalledWith();
  });

  it("should return 404 and render the 404 page", () => {
    const req = getMockReq();
    const { res, next } = getMockRes();

    pageNotFound(req, res, next);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.render).toHaveBeenCalledWith("404.njk");
    expect(next).not.toHaveBeenCalled();
  });
});

import { getMockReq, getMockRes } from "@jest-mock/express";
import { robotsTxtController } from "../../src/robotsTxt/controller";

describe("robotsTxtController", () => {
  it("should return robots.txt disallowing all crawlers", () => {
    const req = getMockReq();
    const { res } = getMockRes();

    robotsTxtController(req, res);

    expect(res.type).toHaveBeenCalledWith("text/plain");
    expect(res.send).toHaveBeenCalledWith("User-agent: *\nDisallow: /");
  });
});

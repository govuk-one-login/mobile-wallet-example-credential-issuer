import { getMockReq, getMockRes } from "@jest-mock/express";
import { healthcheckGetController } from "../../src/healthcheck/controller";

describe("healthcheck", () => {
  it("should return 200 status", () => {
    const req = getMockReq();
    const { res } = getMockRes();

    healthcheckGetController(req, res);

    expect(res.status).toHaveBeenCalledWith(200);
  });
});

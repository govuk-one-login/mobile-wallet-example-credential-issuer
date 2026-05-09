import { dvsStartGetController } from "../../src/dvsStart/controller";
import { Request, Response } from "express";

describe("dvsStartGetController", () => {
  it("should render the start page", () => {
    const req = {} as Request;
    const res = { render: jest.fn() } as unknown as Response;

    dvsStartGetController(req, res);

    expect(res.render).toHaveBeenCalledWith("start-page.njk");
  });
});

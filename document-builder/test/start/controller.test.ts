import { startGetController } from "../../src/start/controller";
import { Request, Response } from "express";

describe("startGetController", () => {
  it("should render the start page", () => {
    const req = {} as Request;
    const res = { render: jest.fn() } as unknown as Response;

    startGetController(req, res);

    expect(res.render).toHaveBeenCalledWith("start-now.njk");
  });
});

import { errorHandler } from "../../src/middleware/errorHandler";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { logger } from "../../src/middleware/logger";

jest.mock("../../src/middleware/logger", () => ({
  logger: { error: jest.fn() },
}));

describe("errorHandler", () => {
  it("should log the error message", () => {
    const req = getMockReq();
    const { res, next } = getMockRes();

    errorHandler(new Error("Something went wrong."), req, res, next);

    expect(logger.error).toHaveBeenCalledWith("Something went wrong.");
  });

  it("should render the 500 page with status 500", () => {
    const req = getMockReq();
    const { res, next } = getMockRes();

    errorHandler(new Error("Something went wrong."), req, res, next);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.render).toHaveBeenCalledWith("500.njk");
  });
});

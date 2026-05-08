import { getMockReq, getMockRes } from "@jest-mock/express";
import { loggedOutGetController } from "../../src/loggedOut/controller";

describe("controller.ts", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should render the 'You have signed out' page", async () => {
    const req = getMockReq();
    const { res } = getMockRes();

    await loggedOutGetController(req, res);

    expect(res.render).toHaveBeenCalledWith("logged-out.njk");
  });
});

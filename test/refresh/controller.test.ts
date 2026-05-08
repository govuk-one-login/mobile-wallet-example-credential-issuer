import { getMockReq, getMockRes } from "@jest-mock/express";
import {
  refreshGetController,
  refreshNoUpdateGetController,
  refreshPostController,
} from "../../src/refresh/controller";

describe("refreshGetController", () => {
  it("should render refresh form with credentialType from params", () => {
    const req = getMockReq({
      params: { credentialType: "SocialSecurityCredential" },
    });
    const { res } = getMockRes();

    refreshGetController(req, res);

    expect(res.render).toHaveBeenCalledWith("refresh-form.njk", {
      credentialType: "SocialSecurityCredential",
      authenticated: expect.anything(),
    });
  });
});

describe("refreshPostController", () => {
  it("should redirect to /no-update when refreshCredential=No", () => {
    const req = getMockReq({
      params: { credentialType: "SocialSecurityCredential" },
      body: { refreshCredential: "false" },
    });
    const { res } = getMockRes();

    refreshPostController(req, res);

    expect(res.redirect).toHaveBeenCalledWith(
      "/refresh/SocialSecurityCredential/no-update",
    );
  });

  it("should redirect to /select-app with credentialType when refreshCredential=true", () => {
    const req = getMockReq({
      params: { credentialType: "SocialSecurityCredential" },
      body: { refreshCredential: "true" },
    });
    const { res } = getMockRes();

    refreshPostController(req, res);

    expect(res.redirect).toHaveBeenCalledWith(
      "/select-app?credentialType=SocialSecurityCredential",
    );
  });

  it("should re-render refresh form with validation error when refreshCredential is missing", () => {
    const req = getMockReq({
      params: { credentialType: "SocialSecurityCredential" },
      body: {},
    });
    const { res } = getMockRes();

    refreshPostController(req, res);

    expect(res.render).toHaveBeenCalledWith("refresh-form.njk", {
      error: true,
      credentialType: "SocialSecurityCredential",
      authenticated: expect.anything(),
    });
    expect(res.redirect).not.toHaveBeenCalled();
  });

  it("should re-render refresh form with validation error when refreshCredential is invalid", () => {
    const req = getMockReq({
      params: { credentialType: "SocialSecurityCredential" },
      body: { refreshCredential: "Maybe" },
    });
    const { res } = getMockRes();

    refreshPostController(req, res);

    expect(res.render).toHaveBeenCalledWith("refresh-form.njk", {
      error: true,
      credentialType: "SocialSecurityCredential",
      authenticated: expect.anything(),
    });
    expect(res.redirect).not.toHaveBeenCalled();
  });
});

describe("refreshNoUpdateGetController", () => {
  it("should render no-update page", () => {
    const req = getMockReq({
      params: { credentialType: "SocialSecurityCredential" },
    });
    const { res } = getMockRes();

    refreshNoUpdateGetController(req, res);

    expect(res.render).toHaveBeenCalledWith("no-update.njk", {
      authenticated: expect.anything(),
    });
  });
});

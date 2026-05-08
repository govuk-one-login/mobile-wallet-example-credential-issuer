import { Request, Response, NextFunction } from "express";
import { getOIDCClient } from "../../src/config/oidc";
import { auth } from "../../src/middleware/auth";
import { AuthMiddlewareConfiguration } from "../../src/types/AuthMiddlewareConfiguration"; // Assuming Express.js
import { Client } from "openid-client";

jest.mock("../../src/config/oidc");
const mockGetOIDCClient = getOIDCClient as jest.MockedFunction<
  typeof getOIDCClient
>;

describe("auth.ts", () => {
  let mockReq: Partial<Request>;
  let mockRes: Partial<Response>;
  let mockNext: jest.MockedFunction<NextFunction>;

  beforeEach(() => {
    mockReq = {};
    mockRes = {
      render: jest.fn(),
    };
    mockNext = jest.fn();
  });

  it("should successfully authenticate and call next middleware", async () => {
    const mockConfiguration = {
      clientId: "mock-client-id",
      discoveryEndpoint:
        "https://mock-server.com/.well-known/openid-configuration",
      redirectUri: "https://mock-self.com/return-from-auth",
    } as AuthMiddlewareConfiguration;
    const mockClient = {} as Client;
    mockGetOIDCClient.mockResolvedValue(mockClient);

    const middleware = auth(mockConfiguration);
    await middleware(mockReq as Request, mockRes as Response, mockNext);

    expect(mockGetOIDCClient).toHaveBeenCalledWith(mockConfiguration);
    expect(mockReq.oidc).toBe(mockClient);
    expect(mockNext).toHaveBeenCalled();
    expect(mockRes.render).not.toHaveBeenCalled();
  });

  it("should handle errors during OIDC client creation and render 500 error page", async () => {
    const mockConfiguration = {
      clientId: "mock-client-id",
      discoveryEndpoint:
        "https://mock-server.com/.well-known/openid-configuration",
      redirectUri: "https://mock-self.com/return-from-auth",
    } as AuthMiddlewareConfiguration;
    const mockError = new Error("Failed to create OIDC client");
    mockGetOIDCClient.mockRejectedValue(mockError);

    const middleware = auth(mockConfiguration);
    await middleware(mockReq as Request, mockRes as Response, mockNext);

    expect(mockGetOIDCClient).toHaveBeenCalledWith(mockConfiguration);
    expect(mockReq.oidc).toBeUndefined();
    expect(mockNext).not.toHaveBeenCalled();
    expect(mockRes.render).toHaveBeenCalledWith("500.njk", {
      errorMessage:
        "Failed to connect to authentication provider. Please try again later.",
    });
  });
});

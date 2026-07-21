import QRCode from "qrcode";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { credentialOfferViewerController } from "../../src/credentialOfferViewer/controller";
import { getCredentialOfferUrl } from "../../src/credentialOfferViewer/services/credentialOfferService";
import { customiseCredentialOfferUrl } from "../../src/credentialOfferViewer/helpers/customCredentialOfferUrl";
import type { Request, Response, NextFunction } from "express";

jest.mock("../../src/credentialOfferViewer/services/credentialOfferService");
jest.mock("../../src/credentialOfferViewer/helpers/customCredentialOfferUrl");
jest.mock("qrcode");

const wallletSubjectId =
  "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";

const walletAppsConfig = {
  "test-app-1": { url: "https://test-one.com", name: "Test App (1)" },
  "test-app-2": { url: "https://test-two.com", name: "Test App (2)" },
};

const config = {
  walletAppsConfig,
  walletApps: ["test-app-1", "test-app-2"],
  environment: "test",
};

describe("credentialOfferViewerController", () => {
  let req: Request;
  let res: Response;
  let next: NextFunction;

  beforeEach(() => {
    jest.clearAllMocks();

    req = getMockReq({
      params: { itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46" },
      cookies: { app: "test-app-1", wallet_subject_id: wallletSubjectId },
      query: { type: "BasicCheckCredential", error: "" },
    });
    const mockRes = getMockRes();
    res = mockRes.res;
    next = mockRes.next;

    (QRCode.toDataURL as jest.Mock).mockResolvedValue("mocked-qrcode");
    (getCredentialOfferUrl as jest.Mock).mockResolvedValue("mocked-offer-url");
    (customiseCredentialOfferUrl as jest.Mock).mockReturnValue(
      "customised-url",
    );
  });

  it("should render the credential offer page", async () => {
    await credentialOfferViewerController(config)(req, res, next);

    expect(getCredentialOfferUrl).toHaveBeenCalledWith(
      wallletSubjectId,
      "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
      "BasicCheckCredential",
    );
    expect(customiseCredentialOfferUrl).toHaveBeenCalledWith(
      "mocked-offer-url",
      "test-app-1",
      walletAppsConfig,
      ["test-app-1", "test-app-2"],
      "",
    );
    expect(QRCode.toDataURL).toHaveBeenCalledWith("customised-url");
    expect(res.render).toHaveBeenCalledWith("credential-offer.njk", {
      authenticated: expect.anything(),
      universalLink: "customised-url",
      qrCode: "mocked-qrcode",
      environment: "test",
    });
  });

  it("should call next with an error when an exception is thrown", async () => {
    (getCredentialOfferUrl as jest.Mock).mockRejectedValueOnce(
      new Error("Network error"),
    );
    await credentialOfferViewerController(config)(req, res, next);
    expect(next).toHaveBeenCalledWith(
      expect.objectContaining({
        message: "An error happened processing credential offer request",
      }),
    );
  });
});

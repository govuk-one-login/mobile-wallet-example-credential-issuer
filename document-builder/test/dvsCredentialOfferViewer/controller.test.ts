import QRCode from "qrcode";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { dvsCredentialOfferViewerController } from "../../src/dvsCredentialOfferViewer/controller";
import { getCredentialOfferUrl } from "../../src/credentialOfferViewer/services/credentialOfferService";
import { customiseCredentialOfferUrl } from "../../src/credentialOfferViewer/helpers/customCredentialOfferUrl";
import type { Request, Response } from "express";
import { ENVIRONMENTS } from "../../src/config/environments";
import { WalletAppsConfig } from "../../src/config/walletAppsConfig";
import { getHardcodedWalletSubjectId } from "../../src/config/appConfig";
import { CredentialType } from "../../src/types/CredentialType";

jest.mock("../../src/credentialOfferViewer/services/credentialOfferService");
jest.mock("../../src/credentialOfferViewer/helpers/customCredentialOfferUrl");
jest.mock("qrcode");

const walletAppsConfig: WalletAppsConfig = {
  "wallet-test-build": { url: "https://test-build.com", name: "Test Build" },
  "wallet-test-verifier-integration": {
    url: "https://test-verifier.com",
    name: "Test Verifier",
  },
};

const config = {
  walletAppsConfig,
  walletApps: ["wallet-test-build", "wallet-test-verifier-integration"],
  environment: ENVIRONMENTS.DEV,
};
const mockItemId = "mock-item-id";
const mockQrCode = "mocked-qrcode";
const mockOfferUrl = "mocked-offer-url";
const mockCustomisedUrl = "customised-url";

describe("dvsCredentialOfferViewerController", () => {
  let req: Request;
  let res: Response;

  beforeEach(() => {
    jest.clearAllMocks();

    req = getMockReq({
      params: { itemId: mockItemId },
    });
    res = getMockRes().res;

    (QRCode.toDataURL as jest.Mock).mockResolvedValue(mockQrCode);
    (getCredentialOfferUrl as jest.Mock).mockResolvedValue(mockOfferUrl);
    (customiseCredentialOfferUrl as jest.Mock).mockReturnValue(
      mockCustomisedUrl,
    );
  });

  it("should render the credential offer page with wallet-test-build for non-prod environments", async () => {
    await dvsCredentialOfferViewerController(config)(req, res);

    expect(getCredentialOfferUrl).toHaveBeenCalledWith(
      getHardcodedWalletSubjectId(),
      mockItemId,
      CredentialType.MobileDrivingLicence,
    );
    expect(customiseCredentialOfferUrl).toHaveBeenCalledWith(
      mockOfferUrl,
      "wallet-test-build",
      walletAppsConfig,
      ["wallet-test-build", "wallet-test-verifier-integration"],
      "",
    );
    expect(QRCode.toDataURL).toHaveBeenCalledWith(mockCustomisedUrl);
    expect(res.render).toHaveBeenCalledWith("dvs-credential-offer.njk", {
      authenticated: expect.anything(),
      universalLink: "customised-url",
      qrCode: "mocked-qrcode",
      environment: "dev",
    });
  });

  it("should render the credential offer page with wallet-test-verifier-integration for prod environments", async () => {
    const prodConfig = { ...config, environment: ENVIRONMENTS.INT };
    await dvsCredentialOfferViewerController(prodConfig)(req, res);

    expect(getCredentialOfferUrl).toHaveBeenCalledWith(
      getHardcodedWalletSubjectId(),
      mockItemId,
      CredentialType.MobileDrivingLicence,
    );
    expect(customiseCredentialOfferUrl).toHaveBeenCalledWith(
      mockOfferUrl,
      "wallet-test-verifier-integration",
      walletAppsConfig,
      ["wallet-test-build", "wallet-test-verifier-integration"],
      "",
    );
    expect(QRCode.toDataURL).toHaveBeenCalledWith(mockCustomisedUrl);
    expect(res.render).toHaveBeenCalledWith("dvs-credential-offer.njk", {
      authenticated: expect.anything(),
      universalLink: "customised-url",
      qrCode: "mocked-qrcode",
      environment: ENVIRONMENTS.INT,
    });
  });

  it("should redirect to / for unknown environments", async () => {
    const prodConfig = { ...config, environment: "PRODUCTION" };
    await dvsCredentialOfferViewerController(prodConfig)(req, res);

    expect(getCredentialOfferUrl).not.toHaveBeenCalled();
    expect(customiseCredentialOfferUrl).not.toHaveBeenCalled();
    expect(QRCode.toDataURL).not.toHaveBeenCalled();
    expect(res.render).not.toHaveBeenCalled();
    expect(res.redirect).toHaveBeenCalledWith("/start");
  });

  it("should render error page when an exception is thrown", async () => {
    (getCredentialOfferUrl as jest.Mock).mockRejectedValueOnce(
      new Error("Network error"),
    );
    await dvsCredentialOfferViewerController(config)(req, res);
    expect(res.render).toHaveBeenCalledWith("500.njk");
  });
});

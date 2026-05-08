import {
  AppSelectorConfig,
  appSelectorGetController,
  appSelectorPostController,
} from "../../src/appSelector/controller";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { buildTemplateInputForApps } from "../../src/appSelector/utils/buildTemplateInputForApps";
import { WalletAppsConfig } from "../../src/config/walletAppsConfig";

jest.mock("../../src/appSelector/utils/buildTemplateInputForApps");
const mockBuildTemplateInputForApps =
  buildTemplateInputForApps as jest.MockedFunction<
    typeof buildTemplateInputForApps
  >;

const walletAppsConfig: WalletAppsConfig = {
  "test-app-1": {
    url: "https://test-one.com/wallet/",
    name: "Test App (1)",
  },
  "test-app-2": {
    url: "https://test-two.com/wallet/",
    name: "Test App (2)",
  },
  "test-app-3": {
    url: "https://test-three.com/wallet/",
    name: "Test App (3)",
  },
};

const config: AppSelectorConfig = {
  walletAppsConfig: walletAppsConfig,
  walletApps: ["test-app-1", "test-app-3"],
  cookieExpiry: 100000,
};

const mockApps = [
  {
    text: "Test App (1)",
    value: "test-app-1",
  },
  {
    text: "Test App (3)",
    value: "test-app-3",
  },
];

describe("appSelectorGetController", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockBuildTemplateInputForApps.mockReturnValue(mockApps);
  });

  it("should render select-app form with 'test-app-1' and 'test-app-3' options when walletApps=['test-app-1','test-app-3']", () => {
    const req = getMockReq();
    const { res } = getMockRes();

    appSelectorGetController(config)(req, res);

    expect(mockBuildTemplateInputForApps).toHaveBeenCalledWith(
      ["test-app-1", "test-app-3"],
      walletAppsConfig,
    );
    expect(res.render).toHaveBeenCalledWith("select-app-form.njk", {
      apps: mockApps,
      authenticated: false,
      credentialType: undefined,
    });
  });
});

describe("appSelectorPostController", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockBuildTemplateInputForApps.mockReturnValue(mockApps);
  });

  it("should re-render select-app form with a validation error when no app is selected", () => {
    const req = getMockReq();
    const { res } = getMockRes();

    appSelectorPostController(config)(req, res);

    expect(mockBuildTemplateInputForApps).toHaveBeenCalledWith(
      ["test-app-1", "test-app-3"],
      walletAppsConfig,
    );
    expect(res.render).toHaveBeenCalledWith("select-app-form.njk", {
      errors: {
        app: {
          text: "Select the app you want to create a document in",
          href: "#app",
        },
      },
      errorList: [
        {
          text: "Select the app you want to create a document in",
          href: "#app",
        },
      ],
      apps: mockApps,
      authenticated: false,
      credentialType: undefined,
    });
    expect(res.redirect).not.toHaveBeenCalled();
    expect(res.cookie).not.toHaveBeenCalled();
  });

  it("should re-render select-app form with a validation error when app selected is invalid", () => {
    const req = getMockReq({
      body: {
        app: "not-a-valid-app-option",
      },
    });
    const { res } = getMockRes();

    appSelectorPostController(config)(req, res);

    expect(mockBuildTemplateInputForApps).toHaveBeenCalledWith(
      ["test-app-1", "test-app-3"],
      walletAppsConfig,
    );
    expect(res.render).toHaveBeenCalledWith("select-app-form.njk", {
      errors: {
        app: {
          text: "Select the app you want to create a document in",
          href: "#app",
        },
      },
      errorList: [
        {
          text: "Select the app you want to create a document in",
          href: "#app",
        },
      ],
      app: "not-a-valid-app-option",
      apps: mockApps,
      authenticated: false,
      credentialType: undefined,
    });
    expect(res.redirect).not.toHaveBeenCalled();
    expect(res.cookie).not.toHaveBeenCalled();
  });

  it("should set the app cookie to the selected value with the configured expiry", () => {
    const req = getMockReq({
      body: {
        app: "test-app-1",
      },
    });
    const { res } = getMockRes();

    appSelectorPostController(config)(req, res);

    expect(res.cookie).toHaveBeenCalledWith("app", "test-app-1", {
      httpOnly: true,
      maxAge: 100000,
    });
  });

  it("should redirect to /select-document with credentialType when provided", () => {
    const req = getMockReq({
      body: {
        app: "test-app-1",
        credentialType: "SocialSecurityCredential",
      },
    });
    const { res } = getMockRes();

    appSelectorPostController(config)(req, res);

    expect(res.redirect).toHaveBeenCalledWith(
      "/select-document?credentialType=SocialSecurityCredential",
    );
  });

  it("should redirect to /select-document without credentialType when not provided", () => {
    const req = getMockReq({
      body: {
        app: "test-app-1",
      },
    });
    const { res } = getMockRes();

    appSelectorPostController(config)(req, res);

    expect(res.redirect).toHaveBeenCalledWith("/select-document");
  });
});

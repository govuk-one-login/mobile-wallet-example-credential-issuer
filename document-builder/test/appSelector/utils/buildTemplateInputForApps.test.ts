import { buildTemplateInputForApps } from "../../../src/appSelector/utils/buildTemplateInputForApps";

describe("buildTemplateInputForApps", () => {
  it("should map wallet apps to array of objects containing 'value' and 'text'", () => {
    const testWalletApps = ["test-app-1", "test-app-2"];
    const testConfig = {
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
    const expectedOutput = [
      { value: "test-app-1", text: "Test App (1)" },
      { value: "test-app-2", text: "Test App (2)" },
    ];

    const result = buildTemplateInputForApps(testWalletApps, testConfig);

    expect(result).toEqual(expectedOutput);
  });

  it("should throw error if wallet app not in config", () => {
    const testWalletApps = ["test-app-unknown", "test-app-1"];
    const testWalletAppsConfig = {
      "test-app-1": {
        url: "https://test-one.com/wallet/",
        name: "Test App (1)",
      },
      "test-app-2": {
        url: "https://test-two.com/wallet/",
        name: "Test App (2)",
      },
    };

    expect(() =>
      buildTemplateInputForApps(testWalletApps, testWalletAppsConfig),
    ).toThrow();
  });
});

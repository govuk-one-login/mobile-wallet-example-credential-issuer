export interface WalletAppConfig {
  url: string;
  name: string;
}

export type WalletAppsConfig = Record<string, WalletAppConfig>;

export const WALLET_APPS = {
  GOVUK_BUILD: "govuk-build",
  GOVUK_STAGING: "govuk-staging",
  WALLET_TEST_DEV: "wallet-test-dev",
  WALLET_TEST_BUILD: "wallet-test-build",
  WALLET_TEST_STAGING: "wallet-test-staging",
  WALLET_TEST_VERIFIER_INTEGRATION: "wallet-test-verifier-integration",
} as const;

export const walletAppsConfig: WalletAppsConfig = {
  [WALLET_APPS.GOVUK_BUILD]: {
    url: "https://mobile.build.account.gov.uk/wallet/",
    name: "GOV.UK One Login App (Build)",
  },
  [WALLET_APPS.GOVUK_STAGING]: {
    url: "https://mobile.staging.account.gov.uk/wallet/",
    name: "GOV.UK One Login App (Staging)",
  },
  [WALLET_APPS.WALLET_TEST_DEV]: {
    url: "https://mobile.dev.account.gov.uk/wallet-test/",
    name: "Wallet Test App (Dev)",
  },
  [WALLET_APPS.WALLET_TEST_BUILD]: {
    url: "https://mobile.build.account.gov.uk/wallet-test/",
    name: "Wallet Test App (Build)",
  },
  [WALLET_APPS.WALLET_TEST_STAGING]: {
    url: "https://mobile.staging.account.gov.uk/wallet-test/",
    name: "Wallet Test App (Staging)",
  },
  [WALLET_APPS.WALLET_TEST_VERIFIER_INTEGRATION]: {
    url: "https://mobile.integration.account.gov.uk/wallet-test/",
    name: "Wallet Test App (Integration)",
  },
};

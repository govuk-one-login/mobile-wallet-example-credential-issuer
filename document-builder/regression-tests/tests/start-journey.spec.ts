import { test, expect } from "@playwright/test";

/**
 * Covers the happy path from the start screen through to the credential offer page.
 *
 * This test runs against the document-builder service alone (port 8001).
 * It does not require the example-credential-issuer (port 8080) or the STS mock
 * (port 9090) to be running.
 *
 * To run the full stack locally see:
 * https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer/tree/main/example-credential-issuer#local-setup
 */
test("navigates from the start screen to the credential offer page", async ({
  page,
}) => {
  // 1. Land on the start page
  await page.goto("/start");
  await expect(page).toHaveTitle(/Document Builder/i);

  // 2. Click "Start now"
  await page.click("a.govuk-button--start");
  await expect(page).toHaveURL(/\/select-app/);

  // 3. Select the first app option and continue
  await page.check('input[name="app"][value="govuk-build"]');
  await page.click('button[type="Submit"]');
  await expect(page).toHaveURL(/\/select-document/);

  // 4. Select Driving Licence and continue
  await page.check('input[name="document"][value="org.iso.18013.5.1.mDL"]');
  await page.click('button[type="Submit"]');
  await expect(page).toHaveURL(/\/build-driving-licence/);

  // 5. Submit the driving licence form using its pre-filled default values
  await page.click('button[type="Submit"]');

  // 6. Assert we have landed on the credential offer page
  await expect(page).toHaveURL(/\/view-credential-offer\/.+/);
});

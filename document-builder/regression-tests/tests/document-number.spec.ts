import { test, expect } from "@playwright/test";
import * as fs from "fs";
import * as path from "path";

/**
 * Navigates to the View Credential page and extracts the document number
 * from the credential explorer component.
 *
 * The document number is found in the jsontree component at:
 *   data-jsontree-js-path = "nameSpaces\\org.iso.18013.5.1\\12\\elementValue"
 *
 * Index 12 in the org.iso.18013.5.1 namespace corresponds to the
 * document_number element of a Driving Licence (mDL) credential.
 *
 * REQUIRES the full local stack to be running:
 *   - document-builder on port 8001       (npm run dev)
 *   - example-credential-issuer on 8080   (./gradlew run)
 *   - STS mock on port 9090               (see test-harness README)
 *
 * Without these services this test will be skipped.
 *
 * The extracted document number is saved to document-number.txt so it
 * can be referenced across multiple test runs.
 */

const DOCUMENT_NUMBER_FILE = path.join(__dirname, "../document-number.txt");
const DOCUMENT_NUMBER_SELECTOR =
  '[data-jsontree-js-path="nameSpaces\\\\org.iso.18013.5.1\\\\12\\\\elementValue"]';

async function isFullStackRunning(): Promise<boolean> {
  try {
    const res = await fetch("http://localhost:8080");
    return res.status < 500;
  } catch {
    return false;
  }
}

test("extracts the document number from the credential explorer on the view credential page", async ({
  page,
}) => {
  const fullStackAvailable = await isFullStackRunning();

  if (!fullStackAvailable) {
    test.skip(
      true,
      "Full stack not running — start example-credential-issuer on port 8080 to run this test",
    );
    return;
  }

  // 1. Complete the journey to the credential offer page
  await page.goto("/start");
  await page.click("a.govuk-button--start");
  await page.check('input[name="app"][value="govuk-build"]');
  await page.click('button[type="Submit"]');
  await page.check('input[name="document"][value="org.iso.18013.5.1.mDL"]');
  await page.click('button[type="Submit"]');
  await page.click('button[type="Submit"]');
  await expect(page).toHaveURL(/\/view-credential-offer\/.+/);

  // 2. Navigate to the view credential page
  await page.click('a[href*="/view-credential/"]');
  await expect(page).toHaveURL(/\/view-credential/);

  // 3. Wait for the jsontree component to finish rendering
  await page.waitForSelector(DOCUMENT_NUMBER_SELECTOR, { timeout: 10_000 });

  // 4. Extract the document number
  const documentNumber = await page
    .locator(DOCUMENT_NUMBER_SELECTOR)
    .textContent();

  expect(documentNumber).toBeTruthy();

  const trimmed = documentNumber!.trim();
  console.log("Document number:", trimmed);

  // 5. Save it for reuse across test runs
  fs.writeFileSync(DOCUMENT_NUMBER_FILE, trimmed);
  console.log("Saved to:", DOCUMENT_NUMBER_FILE);
});

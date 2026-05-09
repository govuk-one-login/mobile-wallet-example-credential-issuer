import {
  dbsDocumentBuilderGetController,
  dbsDocumentBuilderPostController,
} from "../../src/dbsDocumentBuilder/controller";
import * as databaseService from "../../src/services/databaseService";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { ERROR_CHOICES } from "../../src/utils/errorChoices";

jest.mock("node:crypto", () => ({
  randomUUID: jest.fn().mockReturnValue("2e0fac05-4b38-480f-9cbd-b046eabe1e46"),
}));
jest.mock("../../src/services/databaseService", () => ({
  saveDocument: jest.fn(),
}));

const config = { environment: "staging" };

describe("controller.ts", () => {
  const nowMilliSec = 1757582135042;
  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(nowMilliSec);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe("get", () => {
    it("should render the form for inputting DBS document details", async () => {
      const req = getMockReq({ cookies: { id_token: "id_token" } });
      const { res } = getMockRes();

      await dbsDocumentBuilderGetController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith("dbs-document-details-form.njk", {
        authenticated: true,
        errorChoices: ERROR_CHOICES,
        showThrowError: false,
      });
    });

    test.each([
      ["staging", false],
      ["test", true],
    ])(
      "should set showThrowError correctly when environment is %s",
      async (environment, expectedShowThrowError) => {
        const req = getMockReq({ cookies: {} });
        const { res } = getMockRes();

        await dbsDocumentBuilderGetController({ environment })(req, res);

        expect(res.render).toHaveBeenCalledWith(
          "dbs-document-details-form.njk",
          {
            authenticated: false,
            errorChoices: ERROR_CHOICES,
            showThrowError: expectedShowThrowError,
          },
        );
      },
    );
  });

  describe("post", () => {
    const requestBody = {
      "issuance-day": "16",
      "issuance-month": "1",
      "issuance-year": "2025",
      "expiration-day": "16",
      "expiration-month": "1",
      "expiration-year": " 2026",
      firstName: "Sarah Elizabeth",
      lastName: "Edwards",
      "birth-day": "6",
      "birth-month": "3",
      "birth-year": "1980",
      subBuildingName: "Flat 11",
      buildingName: "",
      streetName: "Adelaide Road",
      addressLocality: "London",
      addressCountry: "GB",
      postalCode: "NW3 3RX",
      certificateNumber: "009878863",
      applicationNumber: "E0023455534",
      credentialTtl: "43200",
      throwError: "",
    };

    const saveDocument = databaseService.saveDocument as jest.Mock;

    describe("given an error happens trying to process the request", () => {
      it("should render the error page", async () => {
        saveDocument.mockRejectedValueOnce(new Error("SOME_DATABASE_ERROR"));
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await dbsDocumentBuilderPostController(req, res);

        expect(res.render).toHaveBeenCalledWith("500.njk");
      });
    });

    describe("given the document has been created", () => {
      it(`should call the function to save the document twice and with the correct arguments`, async () => {
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await dbsDocumentBuilderPostController(req, res);

        expect(saveDocument).toHaveBeenCalledWith("testTable", {
          itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
          documentId: "009878863",
          data: {
            "issuance-day": "16",
            "issuance-month": "1",
            "issuance-year": "2025",
            "expiration-day": "16",
            "expiration-month": "1",
            "expiration-year": " 2026",
            "birth-day": "6",
            "birth-month": "3",
            "birth-year": "1980",
            firstName: "Sarah Elizabeth",
            lastName: "Edwards",
            subBuildingName: "Flat 11",
            buildingName: "",
            streetName: "Adelaide Road",
            addressLocality: "London",
            addressCountry: "GB",
            postalCode: "NW3 3RX",
            certificateNumber: "009878863",
            applicationNumber: "E0023455534",
            certificateType: "basic",
            outcome: "Result clear",
            policeRecordsCheck: "Clear",
          },
          vcType: "BasicDisclosureCredential",
          timeToLive: 1760174135,
          credentialTtlSeconds: 43200,
        });
      });
    });

    describe("given the document has been saved successfully", () => {
      describe("when an unknown error code has been received in the request body", () => {
        it("should redirect to the credential offer page with only 'BasicDisclosureCredential' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await dbsDocumentBuilderPostController(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=BasicDisclosureCredential",
          );
        });
      });

      describe("when an error scenario has not been selected", () => {
        it("should redirect to the credential offer page with only 'BasicDisclosureCredential' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await dbsDocumentBuilderPostController(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=BasicDisclosureCredential",
          );
        });
      });

      describe("when an error scenario has been selected", () => {
        it.each(["ERROR:401", "ERROR:500", "ERROR:CLIENT", "ERROR:GRANT"])(
          "should redirect with the correct error parameter when selectedError is '%s'",
          async (selectedError) => {
            const req = getMockReq({
              body: { ...requestBody, ...{ throwError: selectedError } },
            });
            const { res } = getMockRes();

            await dbsDocumentBuilderPostController(req, res);

            expect(res.redirect).toHaveBeenCalledWith(
              `/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=BasicDisclosureCredential&error=${selectedError}`,
            );
          },
        );
      });
    });
  });
});

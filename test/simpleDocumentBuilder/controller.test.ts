import {
  simpleDocumentBuilderGetController,
  simpleDocumentBuilderPostController,
} from "../../src/simpleDocumentBuilder/controller";
import * as databaseService from "../../src/services/databaseService";
import * as s3Service from "../../src/services/s3Service";
import * as photoUtils from "../../src/utils/photoUtils";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { SimpleDocumentRequestBody } from "../../src/simpleDocumentBuilder/types/SimpleDocumentRequestBody";
import { ERROR_CHOICES } from "../../src/utils/errorChoices";

jest.mock("node:crypto", () => ({
  randomUUID: jest.fn().mockReturnValue("2e0fac05-4b38-480f-9cbd-b046eabe1e46"),
}));
jest.mock("../../src/services/databaseService", () => ({
  saveDocument: jest.fn(),
}));
jest.mock("../../src/services/s3Service", () => ({
  uploadPhoto: jest.fn(),
}));
jest.mock("../../src/utils/photoUtils", () => ({
  getPhoto: jest.fn(),
}));
jest.mock("../../src/utils/getRandomIntInclusive", () => ({
  getRandomIntInclusive: jest.fn().mockReturnValue(550000),
}));

const config = {
  environment: "test",
};

describe("controller.ts", () => {
  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2025-05-02T00:00:00Z"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  describe("get", () => {
    it("should render the form for inputting the simple document details", async () => {
      const req = getMockReq({ cookies: { id_token: "id_token" } });
      const { res } = getMockRes();

      await simpleDocumentBuilderGetController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith(
        "simple-document-details-form.njk",
        {
          defaultIssueDate: {
            day: "02",
            month: "05",
            year: "2025",
          },
          defaultExpiryDate: {
            day: "01",
            month: "05",
            year: "2035",
          },
          documentNumber: "FLN550000",
          fishTypeOptions: [
            {
              selected: true,
              text: "Coarse fish",
              value: "Coarse fish",
            },
            {
              selected: false,
              text: "Salmon and trout",
              value: "Salmon and trout",
            },
            {
              selected: false,
              text: "Sea fishing",
              value: "Sea fishing",
            },
            {
              selected: false,
              text: "All freshwater fish",
              value: "All freshwater fish",
            },
          ],
          errorChoices: ERROR_CHOICES,
          authenticated: true,
          showThrowError: true,
        },
      );
    });

    test.each([
      [false, "staging"],
      [true, "not-staging"],
    ])(
      "should set showThrowError to %s when environment is %s",
      async (expectedValue, environment) => {
        const req = getMockReq({ cookies: {} });
        const { res } = getMockRes();

        await simpleDocumentBuilderGetController({ environment })(req, res);

        expect(res.render).toHaveBeenCalledWith(
          "simple-document-details-form.njk",
          {
            defaultIssueDate: {
              day: "02",
              month: "05",
              year: "2025",
            },
            defaultExpiryDate: {
              day: "01",
              month: "05",
              year: "2035",
            },
            documentNumber: "FLN550000",
            fishTypeOptions: [
              {
                selected: true,
                text: "Coarse fish",
                value: "Coarse fish",
              },
              {
                selected: false,
                text: "Salmon and trout",
                value: "Salmon and trout",
              },
              {
                selected: false,
                text: "Sea fishing",
                value: "Sea fishing",
              },
              {
                selected: false,
                text: "All freshwater fish",
                value: "All freshwater fish",
              },
            ],
            errorChoices: ERROR_CHOICES,
            authenticated: false,
            showThrowError: expectedValue,
          },
        );
      },
    );

    it("should render the 500 error page if an error is thrown", async () => {
      const req = getMockReq({ cookies: { id_token: "id_token" } });
      const { res } = getMockRes();

      (res.render as jest.Mock).mockImplementationOnce(() => {
        throw new Error("Rendering error");
      });

      await simpleDocumentBuilderGetController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith("500.njk");
    });
  });

  describe("post", () => {
    const requestBody = buildSimpleDocumentRequestBody();

    const photoBuffer = Buffer.from("mock photo data");
    const mockGetPhoto = photoUtils.getPhoto as jest.Mock;
    mockGetPhoto.mockReturnValue({ photoBuffer, mimeType: "image/jpeg" });

    const saveDocument = databaseService.saveDocument as jest.Mock;
    const uploadPhoto = s3Service.uploadPhoto as jest.Mock;

    describe("should re-render the form with an error", () => {
      it("when a date has empty fields", async () => {
        const body = buildSimpleDocumentRequestBody({
          "birth-day": "",
          "birth-month": "08",
          "birth-year": "",
        });
        const req = getMockReq({
          body,
          cookies: { id_token: "id_token" },
        });
        const { res } = getMockRes();
        await simpleDocumentBuilderPostController(config)(req, res);
        expect(res.render).toHaveBeenCalledWith(
          "simple-document-details-form.njk",
          {
            errors: expect.objectContaining({
              birth_date: "Enter a valid birth date",
            }),
            defaultIssueDate: {
              day: "02",
              month: "05",
              year: "2025",
            },
            defaultExpiryDate: {
              day: "01",
              month: "05",
              year: "2035",
            },
            errorChoices: ERROR_CHOICES,
            documentNumber: "FLN550000",
            fishTypeOptions: [
              {
                selected: true,
                text: "Coarse fish",
                value: "Coarse fish",
              },
              {
                selected: false,
                text: "Salmon and trout",
                value: "Salmon and trout",
              },
              {
                selected: false,
                text: "Sea fishing",
                value: "Sea fishing",
              },
              {
                selected: false,
                text: "All freshwater fish",
                value: "All freshwater fish",
              },
            ],
            authenticated: true,
            showThrowError: true,
          },
        );
        expect(res.redirect).not.toHaveBeenCalled();
      });

      it("when the type of fish selected is unknown", async () => {
        const body = buildSimpleDocumentRequestBody({
          type_of_fish: "Unknwon fish type",
        });
        const req = getMockReq({
          body,
          cookies: { id_token: "id_token" },
        });
        const { res } = getMockRes();
        await simpleDocumentBuilderPostController(config)(req, res);
        expect(res.render).toHaveBeenCalledWith(
          "simple-document-details-form.njk",
          {
            errors: expect.objectContaining({
              type_of_fish: "Select a valid type of fish",
            }),
            defaultIssueDate: {
              day: "02",
              month: "05",
              year: "2025",
            },
            defaultExpiryDate: {
              day: "01",
              month: "05",
              year: "2035",
            },
            errorChoices: ERROR_CHOICES,
            documentNumber: "FLN550000",
            fishTypeOptions: [
              {
                selected: true,
                text: "Coarse fish",
                value: "Coarse fish",
              },
              {
                selected: false,
                text: "Salmon and trout",
                value: "Salmon and trout",
              },
              {
                selected: false,
                text: "Sea fishing",
                value: "Sea fishing",
              },
              {
                selected: false,
                text: "All freshwater fish",
                value: "All freshwater fish",
              },
            ],
            authenticated: true,
            showThrowError: true,
          },
        );
        expect(res.redirect).not.toHaveBeenCalled();
      });
    });

    describe("given an error happens trying to process the request", () => {
      it("should render the error page", async () => {
        saveDocument.mockRejectedValueOnce(new Error("SOME_DATABASE_ERROR"));
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await simpleDocumentBuilderPostController(config)(req, res);

        expect(res.render).toHaveBeenCalledWith("500.njk");
      });
    });

    describe.each([
      ["JPEG", "420x525.jpg", "image/jpeg"],
      ["PNG", "100x125.png", "image/png"],
      ["JFIF", "photo.jfif", "image/jpeg"],
    ])(
      "given a file of type %s is to be uploaded",
      (fileType, fileName, mimeType) => {
        it(`should call the function to upload the photo with the correct arguments`, async () => {
          const req = getMockReq({
            body: {
              ...requestBody,
              ...{ portrait: fileName },
            },
          });
          const { res } = getMockRes();

          mockGetPhoto.mockReturnValue({ photoBuffer, mimeType });

          await simpleDocumentBuilderPostController(config)(req, res);

          expect(mockGetPhoto).toHaveBeenCalledWith(fileName);
          expect(uploadPhoto).toHaveBeenCalledWith(
            photoBuffer,
            "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
            "testBucket",
            mimeType,
          );
        });
      },
    );

    describe("given the photo has been stored successfully", () => {
      it("should call the function to save the document with the correct arguments", async () => {
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await simpleDocumentBuilderPostController(config)(req, res);

        expect(saveDocument).toHaveBeenCalledWith("testTable", {
          itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
          documentId: "FLN550000",
          vcType:
            "uk.gov.account.mobile.example-credential-issuer.simplemdoc.1",
          timeToLive: 1748736000,
          credentialTtlSeconds: 43200,
          data: {
            family_name: "Smith",
            given_name: "John",
            portrait: "s3://testBucket/2e0fac05-4b38-480f-9cbd-b046eabe1e46",
            birth_date: "15-06-1985",
            issue_date: "01-04-2024",
            expiry_date: "01-04-2029",
            issuing_country: "GB",
            document_number: "FLN550000",
            type_of_fish: "Sea fishing",
            number_of_fishing_rods: "2",
          },
        });
      });
    });

    describe("given the document and photo have been stored successfully", () => {
      describe("when an unknown error code has been received in the request body", () => {
        it("should redirect to the credential offer page with only the simple document credential type in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await simpleDocumentBuilderPostController(config)(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=uk.gov.account.mobile.example-credential-issuer.simplemdoc.1",
          );
        });
      });

      describe("when an error scenario has not been selected", () => {
        it("should redirect to the credential offer page with only the simple document credential type in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await simpleDocumentBuilderPostController(config)(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=uk.gov.account.mobile.example-credential-issuer.simplemdoc.1",
          );
        });
      });

      describe("when an error scenario has been selected", () => {
        it.each(["ERROR:GRANT", "ERROR:500", "ERROR:401", "ERROR:CLIENT"])(
          "should redirect with the correct error parameter when selectedError is '%s'",
          async (selectedError) => {
            const req = getMockReq({
              body: { ...requestBody, ...{ throwError: selectedError } },
            });
            const { res } = getMockRes();
            await simpleDocumentBuilderPostController(config)(req, res);

            expect(res.redirect).toHaveBeenCalledWith(
              `/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=uk.gov.account.mobile.example-credential-issuer.simplemdoc.1&error=${selectedError}`,
            );
          },
        );
      });
    });
  });
});

export function buildSimpleDocumentRequestBody(
  overrides: Partial<SimpleDocumentRequestBody> = {},
): SimpleDocumentRequestBody {
  const defaults: SimpleDocumentRequestBody = {
    family_name: "Smith",
    given_name: "John",
    portrait: "420x525.jpg",
    "birth-day": "15",
    "birth-month": "06",
    "birth-year": "1985",
    "issue-day": "01",
    "issue-month": "04",
    "issue-year": "2024",
    "expiry-day": "01",
    "expiry-month": "04",
    "expiry-year": "2029",
    issuing_country: "GB",
    document_number: "FLN550000",
    type_of_fish: "Sea fishing",
    number_of_fishing_rods: "2",
    credentialTtl: "43200",
    throwError: "",
  };
  return { ...defaults, ...overrides };
}

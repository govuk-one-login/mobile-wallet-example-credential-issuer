import {
  veteranCardDocumentBuilderGetController,
  veteranCardDocumentBuilderPostController,
} from "../../src/veteranCardDocumentBuilder/controller";
import * as databaseService from "../../src/services/databaseService";
import * as s3Service from "../../src/services/s3Service";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { ERROR_CHOICES } from "../../src/utils/errorChoices";
import * as veteranCardFormValidator from "../../src/veteranCardDocumentBuilder/helpers/VeteranCardFormValidator";
import * as calculateCredentialTtlSeconds from "../../src/utils/calculateCredentialTtlSeconds";
import * as photoUtils from "../../src/utils/photoUtils";

jest.mock("node:crypto", () => ({
  randomUUID: jest.fn().mockReturnValue("2e0fac05-4b38-480f-9cbd-b046eabe1e46"),
}));
jest.mock(
  "../../src/veteranCardDocumentBuilder/helpers/VeteranCardFormValidator",
  () => ({
    validateVeteranCardForm: jest.fn(),
  }),
);
jest.mock("../../src/utils/photoUtils", () => ({
  getPhoto: jest.fn(),
}));
jest.mock("../../src/services/s3Service", () => ({
  uploadPhoto: jest.fn(),
}));
jest.mock("../../src/utils/calculateCredentialTtlSeconds");
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
    it("should render the form for inputting the Veteran Card document details", async () => {
      const req = getMockReq({ cookies: {} });
      const { res } = getMockRes();

      await veteranCardDocumentBuilderGetController(config)(req, res);
      expect(res.render).toHaveBeenCalledWith(
        "veteran-card-document-details-form.njk",
        {
          authenticated: false,
          errorChoices: ERROR_CHOICES,
          showThrowError: false,
        },
      );
    });

    test.each([
      ["staging", false],
      ["test", true],
    ])(
      "should set showThrowError correctly when environment is %s",
      async (environment, expectedShowThrowError) => {
        const req = getMockReq({ cookies: {} });
        const { res } = getMockRes();

        await veteranCardDocumentBuilderGetController({ environment })(
          req,
          res,
        );

        expect(res.render).toHaveBeenCalledWith(
          "veteran-card-document-details-form.njk",
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
    const photoBuffer = Buffer.from("mock photo data");
    const mockGetPhoto = photoUtils.getPhoto as jest.Mock;
    mockGetPhoto.mockReturnValue({ photoBuffer, mimeType: "image/jpeg" });
    const saveDocument = databaseService.saveDocument as jest.Mock;
    const uploadPhoto = s3Service.uploadPhoto as jest.Mock;
    const mockValidate =
      veteranCardFormValidator.validateVeteranCardForm as jest.Mock;

    beforeEach(() => {
      mockValidate.mockReturnValue({ isValid: true, errors: {} });
    });

    const requestBody = {
      givenName: "Sarah Elizabeth",
      familyName: "Edwards-Smith",
      "dateOfBirth-day": "06",
      "dateOfBirth-month": "03",
      "dateOfBirth-year": "1975",
      "cardExpiryDate-day": "08",
      "cardExpiryDate-month": "04",
      "cardExpiryDate-year": "2029",
      serviceNumber: "25057386",
      serviceBranch: "HM Naval Service",
      portrait: "420x525.jpg",
      credentialTtl: "43200",
      throwError: "",
    };

    describe("given validation fails", () => {
      it("should re-render the form with errors", async () => {
        const validationErrors = { some_field: "some error" };
        mockValidate.mockReturnValueOnce({
          isValid: false,
          errors: validationErrors,
        });
        const req = getMockReq({ body: requestBody });
        const { res } = getMockRes();

        await veteranCardDocumentBuilderPostController(config)(req, res);

        expect(res.render).toHaveBeenCalledWith(
          "veteran-card-document-details-form.njk",
          {
            authenticated: false,
            credentialTtl: "43200",
            showThrowError: false,
            errorChoices: ERROR_CHOICES,
            errors: validationErrors,
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

        await veteranCardDocumentBuilderPostController(config)(req, res);

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
        it(`should call the upload function with the correct arguments`, async () => {
          const req = getMockReq({
            body: {
              ...requestBody,
              ...{ portrait: fileName },
            },
          });
          const { res } = getMockRes();

          mockGetPhoto.mockReturnValue({ photoBuffer, mimeType });

          await veteranCardDocumentBuilderPostController(config)(req, res);

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

    describe("given credentialTtl is 'other'", () => {
      it("should call calculateCredentialTtlSeconds with the expiry date fields", async () => {
        const mockCalculate =
          calculateCredentialTtlSeconds.calculateCredentialTtlSeconds as jest.Mock;
        mockCalculate.mockReturnValue(12345);

        const req = getMockReq({
          body: {
            ...requestBody,
            credentialTtl: "other",
            "credentialExpiry-day": "02",
            "credentialExpiry-month": "05",
            "credentialExpiry-year": "2026",
          },
        });
        const { res } = getMockRes();

        await veteranCardDocumentBuilderPostController(config)(req, res);

        expect(mockCalculate).toHaveBeenCalledWith("02", "05", "2026");
        expect(saveDocument).toHaveBeenCalledWith(
          "testTable",
          expect.objectContaining({ credentialTtlSeconds: 12345 }),
        );
      });
    });

    describe("given the photo has been stored successfully", () => {
      it(`should call the function to save the document with the correct arguments`, async () => {
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await veteranCardDocumentBuilderPostController(config)(req, res);

        expect(saveDocument).toHaveBeenCalledWith("testTable", {
          itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
          documentId: "25057386",
          credentialTtlSeconds: 43200,
          data: {
            givenName: "Sarah Elizabeth",
            familyName: "Edwards-Smith",
            "dateOfBirth-day": "06",
            "dateOfBirth-month": "03",
            "dateOfBirth-year": "1975",
            "cardExpiryDate-day": "08",
            "cardExpiryDate-month": "04",
            "cardExpiryDate-year": "2029",
            serviceNumber: "25057386",
            serviceBranch: "HM Naval Service",
            portrait: "s3://testBucket/2e0fac05-4b38-480f-9cbd-b046eabe1e46",
          },
          vcType: "DigitalVeteranCard",
          timeToLive: 1760174135,
        });
      });
    });

    describe("given the document and photo have been stored successfully", () => {
      describe("when an unknown error code has been received in the request body", () => {
        it("should redirect to the credential offer page with only 'VeteranCardCredential' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await veteranCardDocumentBuilderPostController(config)(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=DigitalVeteranCard",
          );
        });
      });

      describe("when an error scenario has not been selected", () => {
        it("should redirect to the credential offer page with only 'VeteranCardCredential' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await veteranCardDocumentBuilderPostController(config)(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=DigitalVeteranCard",
          );
        });
      });

      describe("when an error scenario has been selected", () => {
        it.each(["ERROR:CLIENT", "ERROR:500", "ERROR:401", "ERROR:GRANT"])(
          "should redirect with the correct error parameter when selectedError is '%s'",
          async (selectedError) => {
            const req = getMockReq({
              body: { ...requestBody, ...{ throwError: selectedError } },
            });
            const { res } = getMockRes();
            await veteranCardDocumentBuilderPostController(config)(req, res);

            expect(res.redirect).toHaveBeenCalledWith(
              `/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=DigitalVeteranCard&error=${selectedError}`,
            );
          },
        );
      });
    });
  });
});

import { readFileSync } from "fs";
import {
  veteranCardDocumentBuilderGetController,
  veteranCardDocumentBuilderPostController,
} from "../../src/veteranCardDocumentBuilder/controller";
import * as databaseService from "../../src/services/databaseService";
import * as s3Service from "../../src/services/s3Service";
import { getMockReq, getMockRes } from "@jest-mock/express";
import * as path from "path";
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
jest.mock("fs");

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

    const photoBuffer = Buffer.from("mock photo data");
    const mockReadFileSync = readFileSync as jest.Mock;
    mockReadFileSync.mockReturnValue(Buffer.from("mock photo data"));

    const saveDocument = databaseService.saveDocument as jest.Mock;
    const uploadPhoto = s3Service.uploadPhoto as jest.Mock;

    describe("given an error happens trying to process the request", () => {
      it("should render the error page", async () => {
        saveDocument.mockRejectedValueOnce(new Error("SOME_DATABASE_ERROR"));
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await veteranCardDocumentBuilderPostController(req, res);

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

          await veteranCardDocumentBuilderPostController(req, res);

          const expectedPath = path.resolve(
            process.cwd(),
            "dist/resources",
            fileName,
          );
          expect(mockReadFileSync).toHaveBeenCalledWith(expectedPath);
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
      it(`should call the function to save the document with the correct arguments`, async () => {
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await veteranCardDocumentBuilderPostController(req, res);

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

          await veteranCardDocumentBuilderPostController(req, res);

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

          await veteranCardDocumentBuilderPostController(req, res);

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
            await veteranCardDocumentBuilderPostController(req, res);

            expect(res.redirect).toHaveBeenCalledWith(
              `/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=DigitalVeteranCard&error=${selectedError}`,
            );
          },
        );
      });
    });
  });
});

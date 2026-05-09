import * as databaseService from "../../src/services/databaseService";
import { getMockReq, getMockRes } from "@jest-mock/express";
import {
  ninoDocumentBuilderGetController,
  ninoDocumentBuilderPostController,
} from "../../src/ninoDocumentBuilder/controller";
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
    it("should render the form for inputting NINO document details", async () => {
      const req = getMockReq({ cookies: { id_token: "id_token" } });
      const { res } = getMockRes();

      await ninoDocumentBuilderGetController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith(
        "nino-document-details-form.njk",
        {
          authenticated: true,
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

        await ninoDocumentBuilderGetController({ environment })(req, res);

        expect(res.render).toHaveBeenCalledWith(
          "nino-document-details-form.njk",
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
      title: "Ms",
      givenName: "Irene",
      familyName: "Adler",
      nino: "QQ123456A",
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

        await ninoDocumentBuilderPostController(req, res);

        expect(res.render).toHaveBeenCalledWith("500.njk");
      });
    });

    describe("given the document has been created", () => {
      it(`should call the function to save the document twice and with the correct arguments`, async () => {
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await ninoDocumentBuilderPostController(req, res);

        expect(saveDocument).toHaveBeenCalledWith("testTable", {
          itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
          documentId: "QQ123456A",
          credentialTtlSeconds: 43200,
          data: {
            title: "Ms",
            givenName: "Irene",
            familyName: "Adler",
            nino: "QQ123456A",
          },
          vcType: "SocialSecurityCredential",
          timeToLive: 1760174135,
        });
      });
    });

    describe("given the document has been saved successfully", () => {
      describe("when an unknown error code has been received in the request body", () => {
        it("should redirect to the credential offer page with only 'SocialSecurityCredential' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await ninoDocumentBuilderPostController(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=SocialSecurityCredential",
          );
        });
      });

      describe("when an error scenario has not been selected", () => {
        it("should redirect to the credential offer page with only 'SocialSecurityCredential' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await ninoDocumentBuilderPostController(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=SocialSecurityCredential",
          );
        });
      });

      describe("when an error scenario has been selected", () => {
        it.each(["ERROR:500", "ERROR:401", "ERROR:CLIENT", "ERROR:GRANT"])(
          "should redirect with the correct error parameter when selectedError is '%s'",
          async (selectedError) => {
            const req = getMockReq({
              body: { ...requestBody, ...{ throwError: selectedError } },
            });
            const { res } = getMockRes();
            await ninoDocumentBuilderPostController(req, res);

            expect(res.redirect).toHaveBeenCalledWith(
              `/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=SocialSecurityCredential&error=${selectedError}`,
            );
          },
        );
      });
    });
  });
});

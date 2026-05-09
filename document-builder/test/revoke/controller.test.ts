import {
  RevokeConfig,
  revokeGetController,
  revokePostController,
} from "../../src/revoke/controller";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { revoke } from "../../src/revoke/services/revokeService";

jest.mock("../../src/revoke/services/revokeService", () => ({
  revoke: jest.fn(),
}));

const CRI_URL = "https://test-cri.example.com";
const DOCUMENT_ID = "ABC123def567";

describe("revoke", () => {
  let config: RevokeConfig;

  beforeEach(async () => {
    config = {
      criUrl: CRI_URL,
    };
  });

  describe("revokeGetController", () => {
    const req = getMockReq();
    const { res } = getMockRes();

    it("should render the revoke form", () => {
      revokeGetController()(req, res);

      expect(res.render).toHaveBeenCalledWith("revoke-form.njk");
    });
  });

  describe("revokePostController", () => {
    beforeEach(async () => {
      jest.clearAllMocks();
    });

    const req = getMockReq({
      body: {
        documentId: DOCUMENT_ID,
      },
    });
    const { res } = getMockRes();

    it.each([
      "shrt", // 4 characters but the minimum is 5
      "documentIdIsTooLong1234567", // 26 characters but the maximum is 25
      "spaces are not ok",
      "invalidChars@!",
    ])(
      "should render an error when the document ID is invalid ('%s')",
      async (invalidId) => {
        const req = getMockReq({
          body: {
            documentId: invalidId,
          },
        });

        await revokePostController(config)(req, res);

        expect(revoke).not.toHaveBeenCalled();
        expect(res.render).toHaveBeenCalledWith("revoke-form.njk", {
          documentId: invalidId,
          errorList: [
            {
              href: "#documentId",
              text: "ID must be 5 to 25 characters long and contain only uppercase or lowercase letters and digits",
            },
          ],
          errors: {
            documentId: {
              href: "#documentId",
              text: "ID must be 5 to 25 characters long and contain only uppercase or lowercase letters and digits",
            },
          },
        });
      },
    );

    it("should render 500 error page if an unexpected error occurs", async () => {
      (revoke as jest.Mock).mockRejectedValue(new Error("Unexpected error"));

      await revokePostController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith("500.njk");
    });

    it("should render success message when CRI returns 202 (revocation succeeded)", async () => {
      const mockResult = 202;
      (revoke as jest.Mock).mockResolvedValue(mockResult);

      await revokePostController(config)(req, res);

      expect(revoke).toHaveBeenCalledWith(CRI_URL, DOCUMENT_ID);
      expect(res.render).toHaveBeenCalledWith("revoke-form.njk", {
        message: "Digital driving licence successfully revoked",
        messageType: "success",
      });
    });

    it("should render an error when CRI returns 404 (no credential found for the provided driving licence)", async () => {
      const mockResult = 404;
      (revoke as jest.Mock).mockResolvedValue(mockResult);

      await revokePostController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith("revoke-form.njk", {
        documentId: DOCUMENT_ID,
        errorList: [
          {
            href: "#documentId",
            text: "No digital driving licence found with this licence number",
          },
        ],
        errors: {
          documentId: {
            href: "#documentId",
            text: "No digital driving licence found with this licence number",
          },
        },
      });
    });

    it("should render error message when CRI returns any other status code", async () => {
      const mockResult = 500;
      (revoke as jest.Mock).mockResolvedValue(mockResult);

      await revokePostController(config)(req, res);

      expect(revoke).toHaveBeenCalledWith(CRI_URL, DOCUMENT_ID);
      expect(res.render).toHaveBeenCalledWith("revoke-form.njk", {
        message:
          "Something went wrong and the credential(s) may not have been revoked",
        messageType: "error",
      });
    });
  });
});

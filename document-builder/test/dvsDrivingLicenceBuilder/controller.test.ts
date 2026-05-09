import { getMockReq, getMockRes } from "@jest-mock/express";
import { dvsDrivingLicenceBuilderGetController } from "../../src/dvsDrivingLicenceBuilder/controller";
import * as photoUtils from "../../src/utils/photoUtils";
import * as s3Service from "../../src/services/s3Service";
import * as databaseService from "../../src/services/databaseService";
import * as buildDefault from "../../src/dvsDrivingLicenceBuilder/helpers/buildDefaultDrivingLicenceData";

jest.mock("node:crypto", () => ({
  randomUUID: jest.fn().mockReturnValue("2e0fac05-4b38-480f-9cbd-b046eabe1e46"),
}));
jest.mock("../../src/utils/photoUtils", () => ({
  getPhoto: jest.fn(),
}));
jest.mock("../../src/services/s3Service", () => ({
  uploadPhoto: jest.fn(),
}));
jest.mock(
  "../../src/dvsDrivingLicenceBuilder/helpers/buildDefaultDrivingLicenceData",
  () => ({
    buildDefaultDrivingLicenceData: jest.fn(),
  }),
);
jest.mock("../../src/services/databaseService", () => ({
  saveDocument: jest.fn(),
}));

describe("controller.ts", () => {
  const photoBuffer = Buffer.from("mock photo data");
  const mockGetPhoto = photoUtils.getPhoto as jest.Mock;
  const uploadPhoto = s3Service.uploadPhoto as jest.Mock;
  const mockBuildDefaultDrivingLicenceData =
    buildDefault.buildDefaultDrivingLicenceData as jest.Mock;
  const saveDocument = databaseService.saveDocument as jest.Mock;

  beforeEach(() => {
    mockGetPhoto.mockReturnValue({ photoBuffer, mimeType: "image/jpeg" });
    mockBuildDefaultDrivingLicenceData.mockReturnValue({
      document_number: "TEST1769688000000",
      family_name: "Test FirstName",
      given_name: "Test-Surname",
    });
  });

  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2025-05-02T00:00:00Z"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  describe("get", () => {
    it("should render the error page when an error occurs", async () => {
      saveDocument.mockRejectedValueOnce(new Error("SOME_DATABASE_ERROR"));
      const req = getMockReq();
      const { res } = getMockRes();

      await dvsDrivingLicenceBuilderGetController(req, res);

      expect(res.render).toHaveBeenCalledWith("500.njk");
    });

    it("should call getPhoto with the correct filename", async () => {
      const req = getMockReq();
      const { res } = getMockRes();

      await dvsDrivingLicenceBuilderGetController(req, res);

      expect(mockGetPhoto).toHaveBeenCalledWith("dvs.jpeg");
    });

    it("should call uploadPhoto with the correct arguments", async () => {
      const req = getMockReq();
      const { res } = getMockRes();

      await dvsDrivingLicenceBuilderGetController(req, res);

      expect(uploadPhoto).toHaveBeenCalledWith(
        photoBuffer,
        "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
        "testBucket",
        "image/jpeg",
      );
    });

    it("should call buildDefaultDrivingLicenceData with the correct S3 URI", async () => {
      const req = getMockReq();
      const { res } = getMockRes();

      await dvsDrivingLicenceBuilderGetController(req, res);

      expect(mockBuildDefaultDrivingLicenceData).toHaveBeenCalledWith(
        "s3://testBucket/2e0fac05-4b38-480f-9cbd-b046eabe1e46",
      );
    });

    it("should call saveDocument with the correct arguments", async () => {
      const req = getMockReq();
      const { res } = getMockRes();

      await dvsDrivingLicenceBuilderGetController(req, res);

      expect(saveDocument).toHaveBeenCalledWith("testTable", {
        documentId: "TEST1769688000000",
        itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
        timeToLive: 1748736000,
        vcType: "org.iso.18013.5.1.mDL",
        credentialTtlSeconds: 172800,
        data: {
          document_number: "TEST1769688000000",
          family_name: "Test FirstName",
          given_name: "Test-Surname",
        },
      });
    });

    it("should redirect to the credential offer page with the correct query params", async () => {
      const req = getMockReq();
      const { res } = getMockRes();

      await dvsDrivingLicenceBuilderGetController(req, res);

      expect(res.redirect).toHaveBeenCalledWith(
        "/dvs/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=org.iso.18013.5.1.mDL",
      );
    });
  });
});

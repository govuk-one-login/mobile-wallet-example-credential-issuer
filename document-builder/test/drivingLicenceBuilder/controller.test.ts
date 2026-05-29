import {
  drivingLicenceBuilderGetController,
  drivingLicenceBuilderPostController,
} from "../../src/drivingLicenceBuilder/controller";
import * as databaseService from "../../src/services/databaseService";
import * as s3Service from "../../src/services/s3Service";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { DrivingLicenceRequestBody } from "../../src/drivingLicenceBuilder/types/DrivingLicenceRequestBody";
import { ERROR_CHOICES } from "../../src/utils/errorChoices";
import * as drivingLicenceFormValidator from "../../src/drivingLicenceBuilder/helpers/DrivingLicenceFormValidator";
import * as calculateCredentialTtlSeconds from "../../src/utils/calculateCredentialTtlSeconds";
import * as photoUtils from "../../src/utils/photoUtils";

jest.mock("node:crypto", () => ({
  randomUUID: jest.fn().mockReturnValue("2e0fac05-4b38-480f-9cbd-b046eabe1e46"),
}));
jest.mock("../../src/utils/getRandomIntInclusive", () => ({
  getRandomIntInclusive: jest.fn().mockReturnValue(550000),
}));
jest.mock(
  "../../src/drivingLicenceBuilder/helpers/DrivingLicenceFormValidator",
  () => ({
    validateDrivingLicenceForm: jest.fn(),
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
  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2025-05-02T00:00:00Z"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  describe("get", () => {
    it("should render the form for inputting the driving licence details", async () => {
      const req = getMockReq({ cookies: { id_token: "id_token" } });
      const { res } = getMockRes();

      await drivingLicenceBuilderGetController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith("driving-licence-form.njk", {
        authenticated: true,
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
        drivingLicenceNumber: "EDWAR550000SE5RO",
        showThrowError: false,
      });
    });

    test.each([
      ["staging", false],
      ["test", true],
    ])(
      "should set showThrowError correctly when environment is %s",
      async (environment, expectedShowThrowError) => {
        const req = getMockReq({ cookies: { id_token: "id_token" } });
        const { res } = getMockRes();

        await drivingLicenceBuilderGetController({ environment })(req, res);

        expect(res.render).toHaveBeenCalledWith("driving-licence-form.njk", {
          authenticated: true,
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
          drivingLicenceNumber: "EDWAR550000SE5RO",
          showThrowError: expectedShowThrowError,
        });
      },
    );

    it("should render the 500 error page if an error is thrown", async () => {
      const req = getMockReq({ cookies: { id_token: "id_token" } });
      const { res } = getMockRes();

      (res.render as jest.Mock).mockImplementationOnce(() => {
        throw new Error("Rendering error");
      });

      await drivingLicenceBuilderGetController(config)(req, res);

      expect(res.render).toHaveBeenCalledWith("500.njk");
    });
  });

  describe("post", () => {
    const photoBuffer = Buffer.from("mock photo data");
    const mockGetPhoto = photoUtils.getPhoto as jest.Mock;
    mockGetPhoto.mockReturnValue({ photoBuffer, mimeType: "image/jpeg" });
    const saveDocument = databaseService.saveDocument as jest.Mock;
    const uploadPhoto = s3Service.uploadPhoto as jest.Mock;
    const mockValidate =
      drivingLicenceFormValidator.validateDrivingLicenceForm as jest.Mock;

    beforeEach(() => {
      mockValidate.mockReturnValue({ isValid: true, errors: {} });
    });

    const requestBody = buildDrivingLicenceRequestBody();

    describe("given validation fails", () => {
      it("should re-render the form with errors", async () => {
        const validationErrors = { some_field: "some error" };
        mockValidate.mockReturnValueOnce({
          isValid: false,
          errors: validationErrors,
        });
        const req = getMockReq({
          body: requestBody,
          cookies: { id_token: "id_token" },
        });
        const { res } = getMockRes();

        await drivingLicenceBuilderPostController(config)(req, res);

        expect(res.render).toHaveBeenCalledWith("driving-licence-form.njk", {
          errors: validationErrors,
          authenticated: true,
          credentialTtl: "43200",
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
          drivingLicenceNumber: "EDWAR550000SE5RO",
          showThrowError: false,
        });
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

        await drivingLicenceBuilderPostController(config)(req, res);

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

          await drivingLicenceBuilderPostController(config)(req, res);

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
          body: buildDrivingLicenceRequestBody({
            credentialTtl: "other",
            "credentialExpiry-day": "02",
            "credentialExpiry-month": "05",
            "credentialExpiry-year": "2026",
          }),
        });
        const { res } = getMockRes();

        await drivingLicenceBuilderPostController(config)(req, res);

        expect(mockCalculate).toHaveBeenCalledWith("02", "05", "2026");
        expect(saveDocument).toHaveBeenCalledWith(
          "testTable",
          expect.objectContaining({ credentialTtlSeconds: 12345 }),
        );
      });
    });

    describe("given the photo has been stored successfully", () => {
      describe("when there are no provisional driving privileges", () => {
        it("should call the function to save the document with the correct arguments (without provisional_driving_privileges)", async () => {
          const req = getMockReq({
            body: buildDrivingLicenceRequestBody({
              provisionalVehicleCategoryCode: "",
            }),
          });
          const { res } = getMockRes();

          await drivingLicenceBuilderPostController(config)(req, res);

          expect(saveDocument).toHaveBeenCalledWith("testTable", {
            itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
            documentId: "EDWAR550000SE5RO",
            vcType: "org.iso.18013.5.1.mDL",
            timeToLive: 1748736000,
            credentialTtlSeconds: 43200,
            data: {
              family_name: "Edwards-Smith",
              given_name: "Sarah Elizabeth",
              title: "Miss",
              welsh_licence: false,
              portrait: "s3://testBucket/2e0fac05-4b38-480f-9cbd-b046eabe1e46",
              birth_date: "06-03-1975",
              birth_place: "London",
              issue_date: "08-04-2019",
              expiry_date: "08-04-2029",
              issuing_authority: "DVLA",
              issuing_country: "GB",
              document_number: "EDWAR550000SE5RO",
              driving_privileges: [
                {
                  vehicle_category_code: "A",
                  issue_date: "01-05-2025",
                  expiry_date: null,
                  codes: [{ code: "01" }],
                },
                {
                  vehicle_category_code: "B",
                  issue_date: "01-05-2025",
                  expiry_date: "10-08-2030",
                  codes: [{ code: "44(7)" }],
                },
              ],
              resident_address: ["Flat 11, Blashford, Adelaide Road"],
              resident_postal_code: "NW3 3RX",
              resident_city: "London",
              un_distinguishing_sign: "UK",
            },
          });
        });
      });

      describe("when there are provisional driving privileges", () => {
        it(`should call the function to save the document with the correct arguments (all attributes)`, async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await drivingLicenceBuilderPostController(config)(req, res);

          expect(saveDocument).toHaveBeenCalledWith("testTable", {
            itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
            documentId: "EDWAR550000SE5RO",
            vcType: "org.iso.18013.5.1.mDL",
            timeToLive: 1748736000,
            credentialTtlSeconds: 43200,
            data: {
              family_name: "Edwards-Smith",
              given_name: "Sarah Elizabeth",
              title: "Miss",
              welsh_licence: false,
              portrait: "s3://testBucket/2e0fac05-4b38-480f-9cbd-b046eabe1e46",
              birth_date: "06-03-1975",
              birth_place: "London",
              issue_date: "08-04-2019",
              expiry_date: "08-04-2029",
              issuing_authority: "DVLA",
              issuing_country: "GB",
              document_number: "EDWAR550000SE5RO",
              driving_privileges: [
                {
                  vehicle_category_code: "A",
                  issue_date: "01-05-2025",
                  expiry_date: null,
                  codes: [{ code: "01" }],
                },
                {
                  vehicle_category_code: "B",
                  issue_date: "01-05-2025",
                  expiry_date: "10-08-2030",
                  codes: [{ code: "44(7)" }],
                },
              ],
              provisional_driving_privileges: [
                {
                  expiry_date: "03-03-2033",
                  issue_date: "04-03-2023",
                  vehicle_category_code: "C",
                  codes: null,
                },
              ],
              resident_address: ["Flat 11, Blashford, Adelaide Road"],
              resident_postal_code: "NW3 3RX",
              resident_city: "London",
              un_distinguishing_sign: "UK",
            },
          });
        });
      });
    });

    describe("given the document and photo have been stored successfully", () => {
      describe("when an unknown error code has been received in the request body", () => {
        it("should redirect to the credential offer page with only 'org.iso.18013.5.1.mDL' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await drivingLicenceBuilderPostController(config)(req, res);

          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=org.iso.18013.5.1.mDL",
          );
        });
      });

      describe("when an error scenario has not been selected", () => {
        it("should redirect to the credential offer page with only 'org.iso.18013.5.1.mDL' in the query params", async () => {
          const req = getMockReq({
            body: requestBody,
          });
          const { res } = getMockRes();

          await drivingLicenceBuilderPostController(config)(req, res);
          expect(res.redirect).toHaveBeenCalledWith(
            "/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=org.iso.18013.5.1.mDL",
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

            await drivingLicenceBuilderPostController(config)(req, res);

            expect(res.redirect).toHaveBeenCalledWith(
              `/view-credential-offer/2e0fac05-4b38-480f-9cbd-b046eabe1e46?type=org.iso.18013.5.1.mDL&error=${selectedError}`,
            );
          },
        );
      });
    });

    describe("expectedUpdate calculation", () => {
      it("should include expectedUpdate in saved document data when expectedUpdateDays has a value", async () => {
        const req = getMockReq({
          body: buildDrivingLicenceRequestBody({ expectedUpdateDays: "5" }),
        });
        const { res } = getMockRes();

        await drivingLicenceBuilderPostController(config)(req, res);

        expect(saveDocument).toHaveBeenCalledWith(
          "testTable",
          expect.objectContaining({
            data: expect.objectContaining({
              expectedUpdate: 43200 - 5 * 86400,
            }),
          }),
        );
      });

      it("should calculate expectedUpdate using calculateCredentialTtlSeconds when credentialTtl is 'other'", async () => {
        const mockCalculate =
          calculateCredentialTtlSeconds.calculateCredentialTtlSeconds as jest.Mock;
        mockCalculate.mockReturnValue(2592000);

        const req = getMockReq({
          body: buildDrivingLicenceRequestBody({
            credentialTtl: "other",
            "credentialExpiry-day": "02",
            "credentialExpiry-month": "05",
            "credentialExpiry-year": "2026",
            expectedUpdateDays: "10",
          }),
        });
        const { res } = getMockRes();

        await drivingLicenceBuilderPostController(config)(req, res);

        expect(saveDocument).toHaveBeenCalledWith(
          "testTable",
          expect.objectContaining({
            data: expect.objectContaining({
              expectedUpdate: 2592000 - 10 * 86400,
            }),
          }),
        );
      });

      it("should not include expectedUpdate in saved document data when expectedUpdateDays is empty", async () => {
        const req = getMockReq({
          body: buildDrivingLicenceRequestBody({ expectedUpdateDays: "" }),
        });
        const { res } = getMockRes();

        await drivingLicenceBuilderPostController(config)(req, res);

        expect(saveDocument).toHaveBeenCalledWith(
          "testTable",
          expect.objectContaining({
            data: expect.not.objectContaining({
              expectedUpdate: expect.anything(),
            }),
          }),
        );
      });

      it("should not include expectedUpdate in saved document data when expectedUpdateDays is not provided", async () => {
        const req = getMockReq({
          body: requestBody,
        });
        const { res } = getMockRes();

        await drivingLicenceBuilderPostController(config)(req, res);

        expect(saveDocument).toHaveBeenCalledWith(
          "testTable",
          expect.objectContaining({
            data: expect.not.objectContaining({
              expectedUpdate: expect.anything(),
            }),
          }),
        );
      });
    });
  });
});

export function buildDrivingLicenceRequestBody(
  overrides: Partial<DrivingLicenceRequestBody> = {},
): DrivingLicenceRequestBody {
  const defaults: DrivingLicenceRequestBody = {
    family_name: "Edwards-Smith",
    given_name: "Sarah Elizabeth",
    title: "Miss",
    welsh_licence: "false",
    portrait: "420x525.jpg",
    "birth-day": "06",
    "birth-month": "03",
    "birth-year": "1975",
    birth_place: "London",
    "issue-day": "08",
    "issue-month": "04",
    "issue-year": "2019",
    "expiry-day": "08",
    "expiry-month": "04",
    "expiry-year": "2029",
    issuing_authority: "DVLA",
    issuing_country: "GB",
    document_number: "EDWAR550000SE5RO",
    resident_address: ["Flat 11, Blashford, Adelaide Road"],
    resident_postal_code: "NW3 3RX",
    resident_city: "London",
    throwError: "",
    fullVehicleCategoryCode: ["A", "B"],
    fullRestrictionCodes: ["01", "44(7)"],
    "fullPrivilegeIssue-day": ["01", "01"],
    "fullPrivilegeIssue-month": ["05", "05"],
    "fullPrivilegeIssue-year": ["2025", "2025"],
    "fullPrivilegeExpiry-day": ["", "10"],
    "fullPrivilegeExpiry-month": ["", "08"],
    "fullPrivilegeExpiry-year": ["", "2030"],
    provisionalVehicleCategoryCode: "C",
    "provisionalPrivilegeIssue-day": "04",
    "provisionalPrivilegeIssue-month": "03",
    "provisionalPrivilegeIssue-year": "2023",
    "provisionalPrivilegeExpiry-day": "03",
    "provisionalPrivilegeExpiry-month": "03",
    "provisionalPrivilegeExpiry-year": "2033",
    credentialTtl: "43200",
    "credentialExpiry-day": "",
    "credentialExpiry-month": "",
    "credentialExpiry-year": "",
  };
  return { ...defaults, ...overrides };
}

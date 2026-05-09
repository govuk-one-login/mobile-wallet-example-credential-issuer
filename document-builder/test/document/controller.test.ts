import { documentController } from "../../src/document/controller";
import * as documentStore from "../../src/services/databaseService";
import * as s3Service from "../../src/services/s3Service";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { CredentialType } from "../../src/types/CredentialType";

jest.mock("../../src/services/databaseService", () => ({
  getDocument: jest.fn(),
}));
jest.mock("../../src/services/s3Service", () => ({
  getPhoto: jest.fn(),
}));
const getDocument = documentStore.getDocument as jest.Mock;
const getPhoto = s3Service.getPhoto as jest.Mock;

const itemId = "2e0fac05-4b38-480f-9cbd-b046eabe1e46";
const bucketName = "photosBucket";

const ninoData = {
  title: "Ms",
  givenName: "Irene",
  familyName: "Adler",
  nino: "QQ123456A",
};

const veteranCardData = {
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
  portrait: "s3://photosBucket/" + itemId,
};

const drivingLicenceData = {
  family_name: "Edwards-Smith",
  given_name: "Sarah Elizabeth",
  portrait: "s3://photosBucket/" + itemId,
  birth_date: "15-06-1985",
  birth_place: "London",
  issue_date: "01-04-2024",
  expiry_date: "01-04-2029",
  issuing_authority: "DVLA",
  issuing_country: "GB",
  document_number: "25057386",
  resident_address: "Flat 11, Blashford, Adelaide Road",
  resident_postal_code: "NW3 3RX",
  resident_city: "London",
};

const simpleDocumentData = {
  family_name: "Smith",
  given_name: "John",
  portrait: "s3://photosBucket/" + itemId,
  birth_date: "15-06-1985",
  issue_date: "01-04-2024",
  expiry_date: "01-04-2029",
  issuing_country: "GB",
  document_number: "FLN550000",
  type_of_fish: "Sea fishing",
  number_of_fishing_rods: "2",
};

describe("controller.ts", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should return 500 if an error happens when trying to process the request", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    const getDocument = documentStore.getDocument as jest.Mock;
    getDocument.mockRejectedValueOnce(new Error("SOME_ERROR"));

    await documentController(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
  });

  it("should return 404 if the NINO document was not found", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce(undefined);

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).not.toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("should return 404 if the Veteran Card photo was not found", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: veteranCardData,
      vcType: CredentialType.DigitalVeteranCard,
    });
    getPhoto.mockReturnValueOnce(undefined);

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).toHaveBeenCalledWith(itemId, bucketName);
    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("should return 404 if the Driving Licence photo was not found", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: drivingLicenceData,
      vcType: CredentialType.MobileDrivingLicence,
    });
    getPhoto.mockReturnValueOnce(undefined);

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).toHaveBeenCalledWith(itemId, bucketName);
    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("should return 404 if the simple document photo was not found", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: simpleDocumentData,
      vcType: CredentialType.SimpleDocument,
    });
    getPhoto.mockReturnValueOnce(undefined);

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).toHaveBeenCalledWith(itemId, bucketName);
    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("should return 200 and the NINO document as JSON", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: ninoData,
      vcType: CredentialType.SocialSecurityCredential,
    });

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).not.toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(200);
  });

  it("should return 200 and the Veteran Card document as JSON", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: veteranCardData,
      vcType: CredentialType.DigitalVeteranCard,
    });
    const mockedPhoto = "mockBase64EncodedPhoto";
    getPhoto.mockReturnValueOnce(mockedPhoto);

    await documentController(req, res);

    const veteranCardDocumentWithPhoto = { ...veteranCardData };
    veteranCardDocumentWithPhoto.portrait = mockedPhoto;

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).toHaveBeenCalledWith(itemId, bucketName);
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      itemId,
      data: { ...veteranCardData, portrait: mockedPhoto },
      vcType: CredentialType.DigitalVeteranCard,
    });
  });

  it("should return 200 and the driving licence record as JSON", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: drivingLicenceData,
      vcType: CredentialType.MobileDrivingLicence,
    });
    const mockedPhoto = "mockBase64EncodedPhoto";
    getPhoto.mockReturnValueOnce(mockedPhoto);

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).toHaveBeenCalledWith(itemId, bucketName);
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      itemId,
      data: { ...drivingLicenceData, portrait: mockedPhoto },
      vcType: CredentialType.MobileDrivingLicence,
    });
  });

  it("should return 200 and the simple document record as JSON", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      params: { itemId },
    });
    getDocument.mockReturnValueOnce({
      itemId,
      data: simpleDocumentData,
      vcType: CredentialType.SimpleDocument,
    });
    const mockedPhoto = "mockBase64EncodedPhoto";
    getPhoto.mockReturnValueOnce(mockedPhoto);

    await documentController(req, res);

    expect(getDocument).toHaveBeenCalledWith("testTable", itemId);
    expect(getPhoto).toHaveBeenCalledWith(itemId, bucketName);
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      itemId,
      data: { ...simpleDocumentData, portrait: mockedPhoto },
      vcType: CredentialType.SimpleDocument,
    });
  });
});

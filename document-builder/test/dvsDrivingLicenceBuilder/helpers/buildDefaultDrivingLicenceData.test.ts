import { buildDefaultDrivingLicenceData } from "../../../src/dvsDrivingLicenceBuilder/helpers/buildDefaultDrivingLicenceData";

describe("buildDefaultDrivingLicenceData", () => {
  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2026-01-29T12:00:00Z"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  it("should build default driving licence data", () => {
    const s3Uri = "s3://bucket/item-uuid";

    const drivingLicenceData = buildDefaultDrivingLicenceData(s3Uri);

    expect(drivingLicenceData).toEqual({
      family_name: "Test-Surname",
      given_name: "Test FirstName",
      title: "Dr",
      welsh_licence: false,
      portrait: "s3://bucket/item-uuid",
      birth_date: "21-06-2000",
      birth_place: "Birth city",
      issue_date: "29-01-2026",
      expiry_date: "28-02-2026",
      issuing_authority: "GDS",
      issuing_country: "GB",
      document_number: "TST1769688000000",
      resident_address: ["Flat test, Building X, Street test"],
      resident_postal_code: "XX1 3XX",
      resident_city: "City test",
      driving_privileges: [
        {
          vehicle_category_code: "AM",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "01",
            },
            {
              code: "02",
            },
          ],
        },
        {
          vehicle_category_code: "P",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "15",
            },
            {
              code: "20",
            },
            {
              code: "25",
            },
            {
              code: "30",
            },
          ],
        },
        {
          vehicle_category_code: "Q",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "A1",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "A2",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "44",
            },
            {
              code: "44(1)",
            },
            {
              code: "44(2)",
            },
          ],
        },
        {
          vehicle_category_code: "A",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "44(12)",
            },
            {
              code: "45",
            },
          ],
        },
        {
          vehicle_category_code: "B",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "45",
            },
          ],
        },
        {
          vehicle_category_code: "B auto",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "BE",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "71",
            },
          ],
        },
        {
          vehicle_category_code: "B1",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "96",
            },
            {
              code: "97",
            },
            {
              code: "101",
            },
            {
              code: "102",
            },
            {
              code: "103",
            },
          ],
        },
        {
          vehicle_category_code: "C1",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "C1E",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "C",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "79(3)",
            },
            {
              code: "96",
            },
            {
              code: "97",
            },
            {
              code: "101",
            },
            {
              code: "102",
            },
            {
              code: "103",
            },
            {
              code: "105",
            },
            {
              code: "106",
            },
            {
              code: "107",
            },
            {
              code: "108",
            },
            {
              code: "110",
            },
            {
              code: "111",
            },
          ],
        },
        {
          vehicle_category_code: "CE",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "01",
            },
            {
              code: "02",
            },
            {
              code: "10",
            },
            {
              code: "15",
            },
          ],
        },
        {
          vehicle_category_code: "D1",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "D1E",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "79",
            },
            {
              code: "79(2)",
            },
            {
              code: "79(3)",
            },
            {
              code: "96",
            },
            {
              code: "97",
            },
            {
              code: "101",
            },
            {
              code: "102",
            },
            {
              code: "103",
            },
            {
              code: "105",
            },
            {
              code: "106",
            },
            {
              code: "107",
            },
            {
              code: "108",
            },
            {
              code: "110",
            },
            {
              code: "111",
            },
            {
              code: "113",
            },
          ],
        },
        {
          vehicle_category_code: "D",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "01",
            },
          ],
        },
        {
          vehicle_category_code: "DE",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "F",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "01",
            },
            {
              code: "02",
            },
          ],
        },
        {
          vehicle_category_code: "G",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "H",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "K",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "L",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "M",
          issue_date: "29-01-2026",
        },
        {
          vehicle_category_code: "N",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "01",
            },
            {
              code: "02",
            },
          ],
        },
      ],
      provisional_driving_privileges: [
        {
          vehicle_category_code: "AM",
          issue_date: "29-01-2026",
          codes: [
            {
              code: "01",
            },
            {
              code: "02",
            },
          ],
        },
      ],
      un_distinguishing_sign: "UK",
    });
  });
});

import {
  buildDrivingPrivileges,
  getFullDrivingPrivileges,
  getProvisionalDrivingPrivileges,
  stringToArray,
} from "../../../src/drivingLicenceBuilder/helpers/drivingPrivilegeBuilder";
import { DrivingLicenceRequestBody } from "../../../src/drivingLicenceBuilder/types/DrivingLicenceRequestBody";

describe("drivingPrivilegeBuilder", () => {
  describe("stringToArray", () => {
    it("should wrap a string in an array", () => {
      expect(stringToArray("abc")).toEqual(["abc"]);
    });

    it("should return the array as-is if input is already an array", () => {
      expect(stringToArray(["a", "b"])).toEqual(["a", "b"]);
    });
  });

  describe("buildDrivingPrivileges", () => {
    it("should set issue date value to null when a date part is missing", () => {
      const result = buildDrivingPrivileges({
        vehicleCategoryCodes: ["C"],
        issueDays: ["2"],
        issueMonths: ["3"],
        issueYears: [""],
        expiryDays: ["1"],
        expiryMonths: ["3"],
        expiryYears: ["2026"],
        restrictionCodes: ["01"],
      });

      expect(result).toEqual([
        {
          vehicle_category_code: "C",
          issue_date: null,
          expiry_date: "01-03-2026",
          codes: [{ code: "01" }],
        },
      ]);
    });

    it("should set expiry date value to null when a date part is missing", () => {
      const result = buildDrivingPrivileges({
        vehicleCategoryCodes: ["C"],
        issueDays: ["2"],
        issueMonths: ["3"],
        issueYears: ["2020"],
        expiryDays: ["1"],
        expiryMonths: ["3"],
        expiryYears: [""],
        restrictionCodes: ["01"],
      });

      expect(result).toEqual([
        {
          vehicle_category_code: "C",
          issue_date: "02-03-2020",
          expiry_date: null,
          codes: [{ code: "01" }],
        },
      ]);
    });

    it("should set codes value to null when restriction codes is undefined", () => {
      const result = buildDrivingPrivileges({
        vehicleCategoryCodes: ["C"],
        issueDays: ["2"],
        issueMonths: ["3"],
        issueYears: ["2020"],
        expiryDays: ["1"],
        expiryMonths: ["3"],
        expiryYears: ["2025"],
        restrictionCodes: undefined,
      });

      expect(result).toEqual([
        {
          vehicle_category_code: "C",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2025",
          codes: null,
        },
      ]);
    });

    it("should set codes value to null when there are no restriction codes", () => {
      const result = buildDrivingPrivileges({
        vehicleCategoryCodes: ["C"],
        issueDays: ["2"],
        issueMonths: ["3"],
        issueYears: ["2020"],
        expiryDays: ["1"],
        expiryMonths: ["3"],
        expiryYears: ["2025"],
        restrictionCodes: [""],
      });

      expect(result).toEqual([
        {
          vehicle_category_code: "C",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2025",
          codes: null,
        },
      ]);
    });

    it("should build array with one driving privilege when there is one vehicle category code", () => {
      const result = buildDrivingPrivileges({
        vehicleCategoryCodes: ["C"],
        issueDays: ["2"],
        issueMonths: ["3"],
        issueYears: ["2020"],
        expiryDays: ["1"],
        expiryMonths: ["3"],
        expiryYears: ["2030"],
        restrictionCodes: ["01"],
      });

      expect(result).toEqual([
        {
          vehicle_category_code: "C",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2030",
          codes: [{ code: "01" }],
        },
      ]);
    });

    it("should build array with two driving privileges when there are two vehicle category codes", () => {
      const result = buildDrivingPrivileges({
        vehicleCategoryCodes: ["B", "D"],
        issueDays: ["2", "11"],
        issueMonths: ["3", "12"],
        issueYears: ["2020", "2021"],
        expiryDays: ["1", "10"],
        expiryMonths: ["3", "12"],
        expiryYears: ["2030", "2031"],
        restrictionCodes: ["01,03", "22,44(7),44(8)"],
      });

      expect(result).toEqual([
        {
          vehicle_category_code: "B",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2030",
          codes: [{ code: "01" }, { code: "03" }],
        },
        {
          vehicle_category_code: "D",
          issue_date: "11-12-2021",
          expiry_date: "10-12-2031",
          codes: [{ code: "22" }, { code: "44(7)" }, { code: "44(8)" }],
        },
      ]);
    });
  });

  describe("getFullDrivingPrivileges", () => {
    it("should build full driving privileges with one privilege", () => {
      const body = {
        "fullPrivilegeIssue-day": "2",
        "fullPrivilegeIssue-month": "3",
        "fullPrivilegeIssue-year": "2020",
        "fullPrivilegeExpiry-day": "1",
        "fullPrivilegeExpiry-month": "3",
        "fullPrivilegeExpiry-year": "2030",
        fullVehicleCategoryCode: "B",
        fullRestrictionCodes: "02",
      } as DrivingLicenceRequestBody;

      const result = getFullDrivingPrivileges(body);

      expect(result).toEqual([
        {
          vehicle_category_code: "B",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2030",
          codes: [{ code: "02" }],
        },
      ]);
    });

    it("should build full driving privileges with two privileges", () => {
      const body = {
        "fullPrivilegeIssue-day": ["2", "1"],
        "fullPrivilegeIssue-month": ["3", "10"],
        "fullPrivilegeIssue-year": ["2020", "2021"],
        "fullPrivilegeExpiry-day": ["1", "2"],
        "fullPrivilegeExpiry-month": ["3", "4"],
        "fullPrivilegeExpiry-year": ["2030", "2035"],
        fullVehicleCategoryCode: ["B", "A"],
        fullRestrictionCodes: ["02", ""],
      } as DrivingLicenceRequestBody;

      const result = getFullDrivingPrivileges(body);

      expect(result).toEqual([
        {
          vehicle_category_code: "B",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2030",
          codes: [{ code: "02" }],
        },
        {
          vehicle_category_code: "A",
          issue_date: "01-10-2021",
          expiry_date: "02-04-2035",
          codes: null,
        },
      ]);
    });
  });

  describe("getProvisionalDrivingPrivileges", () => {
    it("should return an empty array when there are is no vehicle category code", () => {
      const body = {} as DrivingLicenceRequestBody;

      expect(getProvisionalDrivingPrivileges(body)).toEqual([]);
    });

    it("should build provisional driving privileges with one privilege", () => {
      const body = {
        provisionalVehicleCategoryCode: "P",
        "provisionalPrivilegeIssue-day": "5",
        "provisionalPrivilegeIssue-month": "6",
        "provisionalPrivilegeIssue-year": "2022",
        "provisionalPrivilegeExpiry-day": "4",
        "provisionalPrivilegeExpiry-month": "6",
        "provisionalPrivilegeExpiry-year": "2032",
      } as DrivingLicenceRequestBody;

      const result = getProvisionalDrivingPrivileges(body);

      expect(result).toEqual([
        {
          vehicle_category_code: "P",
          issue_date: "05-06-2022",
          expiry_date: "04-06-2032",
          codes: null,
        },
      ]);
    });

    it("should build provisional driving privileges with two privileges", () => {
      const body = {
        provisionalVehicleCategoryCode: ["B", "A"],
        "provisionalPrivilegeIssue-day": ["2", "1"],
        "provisionalPrivilegeIssue-month": ["3", "10"],
        "provisionalPrivilegeIssue-year": ["2020", "2021"],
        "provisionalPrivilegeExpiry-day": ["1", "2"],
        "provisionalPrivilegeExpiry-month": ["3", "4"],
        "provisionalPrivilegeExpiry-year": ["2030", "2035"],
      } as DrivingLicenceRequestBody;

      const result = getProvisionalDrivingPrivileges(body);

      expect(result).toEqual([
        {
          vehicle_category_code: "B",
          issue_date: "02-03-2020",
          expiry_date: "01-03-2030",
          codes: null,
        },
        {
          vehicle_category_code: "A",
          issue_date: "01-10-2021",
          expiry_date: "02-04-2035",
          codes: null,
        },
      ]);
    });
  });
});

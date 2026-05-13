import { formatDate } from "../../utils/date";
import { DrivingLicenceData } from "../../types/DrivingLicenceData";
import { DrivingPrivilege } from "../../types/DrivingPrivilege";
import drivingPrivilegesData from "./drivingPrivileges.json";
import { RawDrivingPrivilege } from "../types/RawDrivingPrivilege";

/**
 * Builds the default data for the driving licence document for the DVS journey.
 *
 * @param s3Uri - The S3 URI where the portrait image is stored.
 * @returns The default driving licence data.
 */
export function buildDefaultDrivingLicenceData(
  s3Uri: string,
): DrivingLicenceData {
  const now = new Date();
  const nowPlus30Days = new Date();
  nowPlus30Days.setDate(nowPlus30Days.getDate() + 30);
  const dateToday = formatDate(
    now.getDate().toString(),
    (now.getMonth() + 1).toString(),
    now.getFullYear().toString(),
  );
  const dateIn30Days = formatDate(
    nowPlus30Days.getDate().toString(),
    (nowPlus30Days.getMonth() + 1).toString(),
    nowPlus30Days.getFullYear().toString(),
  );

  return {
    family_name: "Test-Surname",
    given_name: "Test FirstName",
    title: "Dr",
    welsh_licence: false,
    portrait: s3Uri,
    birth_date: "21-06-2000",
    birth_place: "Birth city",
    issue_date: dateToday,
    expiry_date: dateIn30Days,
    issuing_authority: "GDS",
    issuing_country: "GB",
    document_number: "TST" + Date.now(),
    resident_address: ["Flat test, Building X, Street test"],
    resident_postal_code: "XX1 3XX",
    resident_city: "City test",
    driving_privileges: buildDefaultDrivingPrivileges(
      drivingPrivilegesData.drivingPrivileges,
      dateToday,
    ),
    provisional_driving_privileges: buildDefaultDrivingPrivileges(
      drivingPrivilegesData.provisionalDrivingPrivileges,
      dateToday,
    ),
    un_distinguishing_sign: "UK",
  };
}

export const buildDefaultDrivingPrivileges = (
  privileges: RawDrivingPrivilege[],
  issueDate: string,
): DrivingPrivilege[] =>
  privileges.map((privilege: RawDrivingPrivilege) => ({
    ...privilege,
    issue_date: issueDate,
    codes: privilege.codes?.map((code: string) => ({ code })),
  }));

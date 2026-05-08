import { DrivingLicenceRequestBody } from "../types/DrivingLicenceRequestBody";
import { formatDate } from "../../utils/date";
import { DrivingPrivilege } from "../../types/DrivingPrivilege";

interface BuildDrivingPrivilegesParams {
  vehicleCategoryCodes: string[];
  issueDays: string[];
  issueMonths: string[];
  issueYears: string[];
  expiryDays: string[];
  expiryMonths: string[];
  expiryYears: string[];
  restrictionCodes?: string[];
}

export function getFullDrivingPrivileges(body: DrivingLicenceRequestBody) {
  const fullIssueDays = stringToArray(body["fullPrivilegeIssue-day"]);
  const fullIssueMonths = stringToArray(body["fullPrivilegeIssue-month"]);
  const fullIssueYears = stringToArray(body["fullPrivilegeIssue-year"]);
  const fullExpiryDays = stringToArray(body["fullPrivilegeExpiry-day"]);
  const fullExpiryMonths = stringToArray(body["fullPrivilegeExpiry-month"]);
  const fullExpiryYears = stringToArray(body["fullPrivilegeExpiry-year"]);
  const fullVehicleCategoryCodes = stringToArray(body.fullVehicleCategoryCode);
  const fullRestrictionCodes = stringToArray(body.fullRestrictionCodes);

  return buildDrivingPrivileges({
    vehicleCategoryCodes: fullVehicleCategoryCodes,
    issueDays: fullIssueDays,
    issueMonths: fullIssueMonths,
    issueYears: fullIssueYears,
    expiryDays: fullExpiryDays,
    expiryMonths: fullExpiryMonths,
    expiryYears: fullExpiryYears,
    restrictionCodes: fullRestrictionCodes,
  });
}

export function getProvisionalDrivingPrivileges(
  body: DrivingLicenceRequestBody,
) {
  if (!body.provisionalVehicleCategoryCode) {
    return [];
  }

  const provisionalVehicleCategoryCodes = stringToArray(
    body.provisionalVehicleCategoryCode,
  );
  const provisionalIssueDays = stringToArray(
    body["provisionalPrivilegeIssue-day"]!,
  );
  const provisionalIssueMonths = stringToArray(
    body["provisionalPrivilegeIssue-month"]!,
  );
  const provisionalIssueYears = stringToArray(
    body["provisionalPrivilegeIssue-year"]!,
  );
  const provisionalExpiryDays = stringToArray(
    body["provisionalPrivilegeExpiry-day"]!,
  );
  const provisionalExpiryMonths = stringToArray(
    body["provisionalPrivilegeExpiry-month"]!,
  );
  const provisionalExpiryYears = stringToArray(
    body["provisionalPrivilegeExpiry-year"]!,
  );

  return buildDrivingPrivileges({
    vehicleCategoryCodes: provisionalVehicleCategoryCodes,
    issueDays: provisionalIssueDays,
    issueMonths: provisionalIssueMonths,
    issueYears: provisionalIssueYears,
    expiryDays: provisionalExpiryDays,
    expiryMonths: provisionalExpiryMonths,
    expiryYears: provisionalExpiryYears,
  });
}

export const stringToArray = (input: string | string[]): string[] =>
  Array.isArray(input) ? input : [input];

export function buildDrivingPrivileges({
  vehicleCategoryCodes,
  issueDays,
  issueMonths,
  issueYears,
  expiryDays,
  expiryMonths,
  expiryYears,
  restrictionCodes = undefined,
}: BuildDrivingPrivilegesParams): DrivingPrivilege[] {
  return vehicleCategoryCodes.map((categoryCode, i) => ({
    vehicle_category_code: categoryCode,
    issue_date: createDateFromParts(
      issueDays[i],
      issueMonths[i],
      issueYears[i],
    ),
    expiry_date: createDateFromParts(
      expiryDays[i],
      expiryMonths[i],
      expiryYears[i],
    ),
    codes: restrictionCodes?.[i]
      ? parseRestrictionCodes(restrictionCodes[i])
      : null,
  }));
}

function createDateFromParts(
  day: string,
  month: string,
  year: string,
): string | null {
  return day === "" || month === "" || year === ""
    ? null
    : formatDate(day, month, year);
}

function parseRestrictionCodes(
  restrictionCodes: string,
): { code: string }[] | null {
  const codes = restrictionCodes
    .split(",")
    .map((code) => code.trim())
    .filter((code) => code !== "")
    .map((code) => ({ code }));

  return codes.length > 0 ? codes : null;
}

import { DrivingPrivilege } from "./DrivingPrivilege";

export interface DrivingLicenceData {
  family_name: string;
  given_name: string;
  title: string;
  welsh_licence: boolean;
  portrait: string;
  birth_date: string;
  birth_place: string;
  issue_date: string;
  expiry_date: string;
  issuing_authority: string;
  issuing_country: string;
  document_number: string;
  resident_address: string[];
  resident_postal_code: string;
  resident_city: string;
  driving_privileges: DrivingPrivilege[];
  un_distinguishing_sign: string;
  provisional_driving_privileges?: DrivingPrivilege[];
}

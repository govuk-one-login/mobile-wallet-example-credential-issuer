export interface DrivingPrivilege {
  vehicle_category_code: string;
  issue_date?: string | null;
  expiry_date?: string | null;
  codes?: Code[] | null;
}

export interface Code {
  code: string;
}

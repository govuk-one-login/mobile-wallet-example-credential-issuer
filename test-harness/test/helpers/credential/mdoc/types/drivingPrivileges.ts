import { Tag } from "cbor2";

interface Code {
  code: string;
  sign?: string;
  value?: string;
}

export interface DrivingPrivileges {
  vehicle_category_code: string;
  issue_date?: string;
  expiry_date?: string;
  codes?: Code[];
}

export interface TaggedDrivingPrivileges extends Omit<
  DrivingPrivileges,
  "issue_date" | "expiry_date"
> {
  issue_date?: Tag;
  expiry_date?: Tag;
  codes?: Code[];
}

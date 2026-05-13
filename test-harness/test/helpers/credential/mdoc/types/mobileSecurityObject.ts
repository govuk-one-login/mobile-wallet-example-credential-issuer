import { Tag } from "cbor2";

export interface DeviceKeyInfo {
  deviceKey: Map<unknown, unknown>;
  keyAuthorizations: {
    nameSpaces: ("org.iso.18013.5.1.GB" | "org.iso.18013.5.1")[];
  };
}

export interface ValueDigests {
  "org.iso.18013.5.1.GB": Map<unknown, Uint8Array>;
  "org.iso.18013.5.1": Map<unknown, Uint8Array>;
}

export interface ValidityInfo {
  signed: string;
  validFrom: string;
  validUntil: string;
}

export interface MobileSecurityObject {
  version: "1.0";
  digestAlgorithm: "SHA-256";
  deviceKeyInfo: DeviceKeyInfo;
  valueDigests: ValueDigests;
  docType: "org.iso.18013.5.1.mDL";
  validityInfo: ValidityInfo;
  status: {
    status_list: {
      idx: number;
      uri: string;
    };
  };
}

export interface TaggedMobileSecurityObject extends Omit<
  MobileSecurityObject,
  "validityInfo"
> {
  validityInfo: {
    signed: Tag;
    validFrom: Tag;
    validUntil: Tag;
  };
}

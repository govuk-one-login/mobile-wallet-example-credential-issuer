import { decode, Tag } from "cbor2";
import { TAGS } from "./constants/tags";
import {
  TaggedIssuerSigned,
  TaggedIssuerSignedItem,
} from "./types/issuerSigned";
import { errorMessage, MDLValidationError } from "./MDLValidationError";
import { TaggedDrivingPrivileges } from "./types/drivingPrivileges";
import { TaggedMobileSecurityObject } from "./types/mobileSecurityObject";

const FULL_DATE_ELEMENTS = new Set(["birth_date", "issue_date", "expiry_date"]);

const DRIVING_PRIVILEGES_ELEMENTS = new Set([
  "driving_privileges",
  "provisional_driving_privileges",
]);

export function validateTags(taggedIssuerSigned: TaggedIssuerSigned): void {
  try {
    for (const [namespaceName, elements] of Object.entries(
      taggedIssuerSigned.nameSpaces,
    )) {
      for (const element of elements) {
        validateNamespacesTags(element, namespaceName);
      }
    }

    validateMobileSecurityObjectTags(taggedIssuerSigned.issuerAuth[2]);
  } catch (error) {
    throw new MDLValidationError(
      `Failed to validate tags - ${errorMessage(error)}`,
      "INVALID_TAGS",
    );
  }
}

function validateNamespacesTags(element: Tag, namespaceName: string): void {
  if (element.tag !== TAGS.ENCODED_CBOR_DATA) {
    throw new Error(
      `IssuerSignedItem in namespace '${namespaceName}' missing tag '${TAGS.ENCODED_CBOR_DATA}'`,
    );
  }

  const decodedItem: TaggedIssuerSignedItem = decode(
    element.contents as Uint8Array,
  );

  if (FULL_DATE_ELEMENTS.has(decodedItem.elementIdentifier)) {
    if (
      !(decodedItem.elementValue instanceof Tag) ||
      decodedItem.elementValue.tag !== TAGS.FULL_DATE
    ) {
      throw new Error(
        `'${decodedItem.elementIdentifier}' missing tag '${TAGS.FULL_DATE}'`,
      );
    }
  }

  if (DRIVING_PRIVILEGES_ELEMENTS.has(decodedItem.elementIdentifier)) {
    const privileges = decodedItem.elementValue as TaggedDrivingPrivileges[];

    for (const privilege of privileges) {
      if (privilege.issue_date && privilege.issue_date.tag !== TAGS.FULL_DATE) {
        throw new Error(
          `'issue_date' in '${decodedItem.elementIdentifier}' missing tag '${TAGS.FULL_DATE}'`,
        );
      }

      if (
        privilege.expiry_date &&
        privilege.expiry_date.tag !== TAGS.FULL_DATE
      ) {
        throw new Error(
          `'expiry_date' in '${decodedItem.elementIdentifier}' missing tag '${TAGS.FULL_DATE}'`,
        );
      }
    }
  }
}

function validateMobileSecurityObjectTags(payload: Uint8Array) {
  const taggedMsoBytes: Tag = decode(payload);
  if (taggedMsoBytes.tag !== TAGS.ENCODED_CBOR_DATA) {
    throw new Error(
      `MobileSecurityObjectBytes missing tag '${TAGS.ENCODED_CBOR_DATA}'`,
    );
  }
  const mso = decode(
    taggedMsoBytes.contents as Uint8Array,
  ) as TaggedMobileSecurityObject;

  const taggedValidityInfo = mso.validityInfo;

  if (
    taggedValidityInfo.signed &&
    taggedValidityInfo.signed.tag !== TAGS.DATE_TIME
  ) {
    throw new Error(`'signed' in 'ValidityInfo' missing tag ${TAGS.DATE_TIME}`);
  }

  if (
    taggedValidityInfo.validFrom &&
    taggedValidityInfo.validFrom.tag !== TAGS.DATE_TIME
  ) {
    throw new Error(
      `'validFrom' in 'ValidityInfo' missing tag ${TAGS.DATE_TIME}`,
    );
  }

  if (
    taggedValidityInfo.validUntil &&
    taggedValidityInfo.validUntil.tag !== TAGS.DATE_TIME
  ) {
    throw new Error(
      `'validUntil' in 'ValidityInfo' missing tag ${TAGS.DATE_TIME}`,
    );
  }
}

import {
  createHash,
  createPrivateKey,
  createSign,
  X509Certificate,
} from "node:crypto";
import { encode, Tag } from "cbor2";
import { base64url } from "jose";
import { TAGS } from "./constants/tags";
import {
  COSE_ALGORITHMS,
  COSE_ELLIPTIC_CURVES,
  COSE_HEADER_PARAMETERS,
  COSE_KEY_PARAMETERS,
  COSE_KEY_TYPES,
} from "./constants/cose";
import { NAMESPACES } from "./constants/namespaces";
import { IssuerSignedItem } from "./types/issuerSigned";
import { NameSpace } from "./types/namespaces";
import { DrivingPrivileges } from "./types/drivingPrivileges";

export class TestMDLBuilder {
  private readonly namespaces: Map<NameSpace, IssuerSignedItem[]>;
  private readonly validityInfo: {
    signed: Tag | string;
    validFrom: Tag | string;
    validUntil: Tag | string;
  };
  private readonly deviceKey: Map<number, any>;
  private readonly protectedHeader: any;
  private readonly unprotectedHeader: Map<number, Uint8Array>;

  private readonly elementsWithoutTag24: Set<string>;
  private readonly elementsWithMismatchedDigests: Map<string, Uint8Array>;
  private readonly elementsWithoutDigests: Set<string>;
  private untaggedMsoBytes = false;

  constructor() {
    this.namespaces = new Map();
    for (const [namespace, items] of DEFAULT_NAMESPACES) {
      this.namespaces.set(
        namespace,
        items.map((item) => ({ ...item })),
      );
    }

    this.validityInfo = {
      signed: new Tag(TAGS.DATE_TIME, "2025-09-10T15:20:00Z"),
      validFrom: new Tag(TAGS.DATE_TIME, "2025-09-10T15:20:00Z"),
      validUntil: new Tag(TAGS.DATE_TIME, "2026-09-10T15:20:00Z"),
    };

    this.deviceKey = new Map<number, number | Uint8Array>(DEFAULT_DEVICE_KEY);

    this.protectedHeader = new Map().set(
      COSE_HEADER_PARAMETERS.ALG,
      COSE_ALGORITHMS.ES256,
    );

    const documentSigningCertificate = new X509Certificate(
      DEFAULT_DOCUMENT_SIGNING_CERTIFICATE,
    );
    this.unprotectedHeader = new Map().set(
      COSE_HEADER_PARAMETERS.X5_CHAIN,
      new Uint8Array(documentSigningCertificate.raw),
    );

    this.elementsWithoutTag24 = new Set<string>();
    this.elementsWithoutDigests = new Set<string>();
    this.elementsWithMismatchedDigests = new Map<string, Uint8Array>();
  }

  build() {
    const valueDigests: Record<string, Map<number, Uint8Array>> = {};
    const nameSpacesEncoded: Record<string, (Tag | Uint8Array)[]> = {};

    for (const [namespace, items] of this.namespaces) {
      valueDigests[namespace] = new Map();
      nameSpacesEncoded[namespace] = [];

      for (const item of items) {
        const itemEncoded = encode(item);
        const shouldTag = !this.elementsWithoutTag24.has(
          item.elementIdentifier,
        );

        const taggedItem = shouldTag
          ? new Tag(TAGS.ENCODED_CBOR_DATA, itemEncoded)
          : itemEncoded;

        nameSpacesEncoded[namespace].push(taggedItem);

        if (this.elementsWithoutDigests.has(item.elementIdentifier)) {
          continue;
        }

        const digestOverride = this.elementsWithMismatchedDigests.get(
          item.elementIdentifier,
        );
        if (digestOverride) {
          valueDigests[namespace].set(item.digestID, digestOverride);
        } else {
          const digest = createHash("sha256")
            .update(encode(taggedItem))
            .digest();
          valueDigests[namespace].set(item.digestID, new Uint8Array(digest));
        }
      }
    }

    const mso = {
      version: "1.0",
      digestAlgorithm: "SHA-256",
      valueDigests,
      deviceKeyInfo: {
        deviceKey: this.deviceKey,
        keyAuthorizations: {
          nameSpaces: Array.from(this.namespaces.keys()),
        },
      },
      docType: "org.iso.18013.5.1.mDL",
      status: {
        status_list: { idx: 1, uri: "https://example-status-list.com" },
      },
      validityInfo: {
        signed: this.validityInfo.signed,
        validFrom: this.validityInfo.validFrom,
        validUntil: this.validityInfo.validUntil,
      },
    };

    const msoBytes = encode(mso);
    const tagged = this.untaggedMsoBytes
      ? msoBytes
      : new Tag(TAGS.ENCODED_CBOR_DATA, msoBytes);
    const payload = encode(tagged);

    const protectedHeader = encode(this.protectedHeader);
    const toBeSigned = encode([
      "Signature1",
      protectedHeader,
      new Uint8Array(),
      payload,
    ]);

    const signer = createSign("sha256");
    signer.update(toBeSigned);

    const signingKey = createPrivateKey({
      key: DEFAULT_SIGNING_KEY,
      type: "pkcs8",
      format: "pem",
    });

    const signature = signer.sign({
      key: signingKey,
      dsaEncoding: "ieee-p1363",
    });

    const issuerAuth = [
      protectedHeader,
      this.unprotectedHeader,
      payload,
      new Uint8Array(signature),
    ];

    const result = {
      nameSpaces: nameSpacesEncoded,
      issuerAuth,
    };

    return base64url.encode(encode(result));
  }

  withElementValue(
    elementIdentifier: string,
    elementValue: string | Uint8Array | DrivingPrivileges[],
  ) {
    for (const items of this.namespaces.values()) {
      const item = items.find((i) => i.elementIdentifier === elementIdentifier);
      if (item) {
        item.elementValue = elementValue;
        return this;
      }
    }
    return this;
  }

  withDigestId(elementIdentifier: string, digestId: number) {
    for (const items of this.namespaces.values()) {
      const item = items.find((i) => i.elementIdentifier === elementIdentifier);
      if (item) {
        item.digestID = digestId;
        return this;
      }
    }
    return this;
  }

  withUntaggedIssuerSignedItemBytes(elementIdentifier: string) {
    this.elementsWithoutTag24.add(elementIdentifier);
    return this;
  }

  withUntaggedMsoBytes(): this {
    this.untaggedMsoBytes = true;
    return this;
  }

  withoutDigest(elementIdentifier: string) {
    this.elementsWithoutDigests.add(elementIdentifier);
    return this;
  }

  withMismatchedDigest(
    elementIdentifier: string,
    mismatchedDigest: Uint8Array,
  ) {
    this.elementsWithMismatchedDigests.set(elementIdentifier, mismatchedDigest);
    return this;
  }

  withValidityInfo(
    validityInfo: Partial<{
      signed: Tag | string;
      validFrom: Tag | string;
      validUntil: Tag | string;
    }>,
  ) {
    Object.assign(this.validityInfo, validityInfo);
    return this;
  }

  withDeviceKeyParameter(key: number, value: any) {
    this.deviceKey.set(key, value);
    return this;
  }

  withProtectedHeader(protectedHeader: any) {
    this.protectedHeader.clear();
    for (const [key, value] of protectedHeader) {
      this.protectedHeader.set(key, value);
    }
    return this;
  }

  withUnprotectedHeader(unprotectedHeader: Map<number, Uint8Array>) {
    this.unprotectedHeader.clear();
    for (const [key, value] of unprotectedHeader) {
      this.unprotectedHeader.set(key, value);
    }
    return this;
  }
}

const DEFAULT_NAMESPACES = new Map([
  [
    NAMESPACES.GB,
    [
      {
        digestID: 20,
        random: new Uint8Array([
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        ]),
        elementIdentifier: "welsh_licence",
        elementValue: true,
      },
      {
        digestID: 30,
        random: new Uint8Array([
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        ]),
        elementIdentifier: "provisional_driving_privileges",
        elementValue: [
          {
            vehicle_category_code: "C1",
            issue_date: new Tag(TAGS.FULL_DATE, "2029-05-10"),
          },
        ],
      },
      {
        digestID: 40,
        random: new Uint8Array([
          3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        ]),
        elementIdentifier: "title",
        elementValue: "Mr",
      },
    ],
  ],
  [
    NAMESPACES.ISO,
    [
      {
        digestID: 10,
        random: new Uint8Array([
          16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1,
        ]),
        elementIdentifier: "family_name",
        elementValue: "Doe",
      },
      {
        digestID: 20,
        random: new Uint8Array([
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        ]),
        elementIdentifier: "given_name",
        elementValue: "Jane",
      },
      {
        digestID: 30,
        random: new Uint8Array([
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        ]),
        elementIdentifier: "birth_date",
        elementValue: new Tag(TAGS.FULL_DATE, "2000-12-12"),
      },
      {
        digestID: 40,
        random: new Uint8Array([
          3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        ]),
        elementIdentifier: "issue_date",
        elementValue: new Tag(TAGS.FULL_DATE, "2020-07-01"),
      },
      {
        digestID: 50,
        random: new Uint8Array([
          4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        ]),
        elementIdentifier: "expiry_date",
        elementValue: new Tag(TAGS.FULL_DATE, "2030-06-30"),
      },
      {
        digestID: 60,
        random: new Uint8Array([
          6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        ]),
        elementIdentifier: "issuing_country",
        elementValue: "GB",
      },
      {
        digestID: 70,
        random: new Uint8Array([
          7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        ]),
        elementIdentifier: "issuing_authority",
        elementValue: "DVLA",
      },
      {
        digestID: 80,
        random: new Uint8Array([
          8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        ]),
        elementIdentifier: "document_number",
        elementValue: "TEST123",
      },
      {
        digestID: 90,
        random: new Uint8Array([
          9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        ]),
        elementIdentifier: "portrait",
        elementValue: new Uint8Array([255, 216, 255, 224, 255, 217]),
      },
      {
        digestID: 100,
        random: new Uint8Array([
          10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
        ]),
        elementIdentifier: "birth_place",
        elementValue: "London",
      },
      {
        digestID: 110,
        random: new Uint8Array([
          11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
        ]),
        elementIdentifier: "driving_privileges",
        elementValue: [
          {
            vehicle_category_code: "C1",
            issue_date: new Tag(TAGS.FULL_DATE, "2029-05-10"),
          },
        ],
      },
      {
        digestID: 120,
        random: new Uint8Array([
          12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
        ]),
        elementIdentifier: "un_distinguishing_sign",
        elementValue: "UK",
      },
      {
        digestID: 130,
        random: new Uint8Array([
          13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
        ]),
        elementIdentifier: "resident_address",
        elementValue: "Adelaide Road",
      },
      {
        digestID: 140,
        random: new Uint8Array([
          14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
        ]),
        elementIdentifier: "resident_postal_code",
        elementValue: "NW3 3RX",
      },
      {
        digestID: 150,
        random: new Uint8Array([
          15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
        ]),
        elementIdentifier: "resident_city",
        elementValue: "London",
      },
    ],
  ],
]);

const DEFAULT_SIGNING_KEY = `-----BEGIN EC PRIVATE KEY-----
MHcCAQEEIKexbdPE2TDYzOuasfwN4QWNqHF1wNsV30ERMPPaRYnWoAoGCCqGSM49
AwEHoUQDQgAE+NKi4QpYV/avqTFFoldRIYEZaRgKF/qv+xJsek63Eh2cKn922zlJ
Hj2KglzSlLm439BfFYGDYVet6W7pkvIYfg==
-----END EC PRIVATE KEY-----`;

const DEFAULT_DOCUMENT_SIGNING_CERTIFICATE = `-----BEGIN CERTIFICATE-----
MIIBtzCCAV2gAwIBAgIUZpfeB6WGkUsUk13SiJX8i6vG1IAwCgYIKoZIzj0EAwIw
QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
IExPQ0FMIGVudmlyb25tZW50MB4XDTI1MDkwMjEwMzMzMloXDTI2MDkwMjEwMzMz
MlowMjELMAkGA1UEBhMCR0IxIzAhBgNVBAMMGkV4YW1wbGUgSXNzdWVyIERTQyAo
TE9DQUwpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+NKi4QpYV/avqTFFoldR
IYEZaRgKF/qv+xJsek63Eh2cKn922zlJHj2KglzSlLm439BfFYGDYVet6W7pkvIY
fqNCMEAwHQYDVR0OBBYEFFBBWigj2hXjuJNidBxTFPvGxzOLMB8GA1UdIwQYMBaA
FPY4eri7CuGrxh14YMTQe1qnBVjoMAoGCCqGSM49BAMCA0gAMEUCIQCm99llHZfq
nPUS1X4/UZfbJ4HlbU33EaTqS/Y4vrOPVQIgLcG3k0jJQIxapcCUF7r/4rVUju0z
FmibH8pIONDZjSI=
-----END CERTIFICATE-----`;

const DEFAULT_DEVICE_KEY = new Map<number, number | Uint8Array>([
  [COSE_KEY_PARAMETERS.KTY, COSE_KEY_TYPES.EC2],
  [COSE_KEY_PARAMETERS.EC2_CRV, COSE_ELLIPTIC_CURVES.P_256],
  [
    COSE_KEY_PARAMETERS.EC2_X,
    new Uint8Array(
      Buffer.from(
        "6DCF397495962365F7E8FA912AB95D9990E9002E31CC151840FD7754AFA6BD53",
        "hex",
      ),
    ),
  ],
  [
    COSE_KEY_PARAMETERS.EC2_Y,
    new Uint8Array(
      Buffer.from(
        "39D569F97C102510B56506AE414A72D6EEAA084ED454C751DCF90FF9602C2953",
        "hex",
      ),
    ),
  ],
]);

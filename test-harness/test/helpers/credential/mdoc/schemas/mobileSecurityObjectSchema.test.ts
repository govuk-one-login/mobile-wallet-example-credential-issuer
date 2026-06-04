import { getAjvInstance } from "../../../ajv/ajvInstance";
import { mobileSecurityObjectSchema } from "./mobileSecurityObjectSchema";
import { testMobileSecurityObjectBuilder } from "./testMobileSecurityObjectBuilder";
import { MobileSecurityObject } from "../types/mobileSecurityObject";

describe("mobileSecurityObjectSchema", () => {
  const ajv = getAjvInstance();
  const validate = ajv.compile(mobileSecurityObjectSchema);

  it("should return false when it contains additional properties", () => {
    const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
      extra: "not allowed",
    });

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        params: { additionalProperty: "extra" },
        message: "must NOT have additional properties",
      }),
    );
  });

  it.each([
    "version",
    "digestAlgorithm",
    "deviceKeyInfo",
    "valueDigests",
    "docType",
    "validityInfo",
    "status",
  ])("should return false when %s is missing", (field) => {
    const data: Record<string, unknown> = { ...defaultData };
    // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
    delete data[field];

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        message: `must have required property '${field}'`,
      }),
    );
  });

  describe("version", () => {
    it("should return false when it is not '1.0'", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        version: "2.0",
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/version",
          params: { allowedValues: ["1.0"] },
          message: "must be equal to one of the allowed values",
        }),
      );
    });
  });

  describe("digestAlgorithm", () => {
    it("should return false when it is not 'SHA-256'", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        digestAlgorithm: "SHA-512",
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/digestAlgorithm",
          params: { allowedValues: ["SHA-256"] },
          message: "must be equal to one of the allowed values",
        }),
      );
    });
  });

  describe("docType", () => {
    it("should return false when it is not 'org.iso.18013.5.1.mDL'", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        docType: "invalid.doc.type",
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/docType",
          params: { allowedValues: ["org.iso.18013.5.1.mDL"] },
          message: "must be equal to one of the allowed values",
        }),
      );
    });
  });

  describe("deviceKeyInfo", () => {
    it("should return false when deviceKey is missing", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        deviceKeyInfo: {
          keyAuthorizations: defaultData.deviceKeyInfo.keyAuthorizations,
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/deviceKeyInfo",
          message: "must have required property 'deviceKey'",
        }),
      );
    });

    it("should return false when keyAuthorizations is missing", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        deviceKeyInfo: {
          deviceKey: new Map(),
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/deviceKeyInfo",
          message: "must have required property 'keyAuthorizations'",
        }),
      );
    });

    it("should return false when it contains additional properties", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        deviceKeyInfo: {
          ...defaultData.deviceKeyInfo,
          extra: "not allowed",
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/deviceKeyInfo",
          message: "must NOT have additional properties",
        }),
      );
    });

    describe("deviceKey", () => {
      it("should return false when it is not a Map", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            deviceKeyInfo: {
              deviceKey: {},
              keyAuthorizations: defaultData.deviceKeyInfo.keyAuthorizations,
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
      });
    });

    describe("keyAuthorizations", () => {
      it("should return false when nameSpaces is missing", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            deviceKeyInfo: {
              deviceKey: new Map(),
              keyAuthorizations: {},
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/deviceKeyInfo/keyAuthorizations",
            message: "must have required property 'nameSpaces'",
          }),
        );
      });

      describe("nameSpaces", () => {
        it("should return false when it contains an invalid namespace", () => {
          const data = testMobileSecurityObjectBuilder(
            defaultData,
          ).withOverrides({
            deviceKeyInfo: {
              deviceKey: new Map(),
              keyAuthorizations: {
                nameSpaces: ["org.iso.18013.5.1", "invalid.namespace"],
              },
            },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/deviceKeyInfo/keyAuthorizations/nameSpaces/1",
              message: "must be equal to one of the allowed values",
            }),
          );
        });

        it("should return false when it has fewer than 2 items", () => {
          const data = testMobileSecurityObjectBuilder(
            defaultData,
          ).withOverrides({
            deviceKeyInfo: {
              deviceKey: new Map(),
              keyAuthorizations: {
                nameSpaces: ["org.iso.18013.5.1"],
              },
            },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/deviceKeyInfo/keyAuthorizations/nameSpaces",
              message: "must NOT have fewer than 2 items",
            }),
          );
        });

        it("should return false when it has more than 2 items", () => {
          const data = testMobileSecurityObjectBuilder(
            defaultData,
          ).withOverrides({
            deviceKeyInfo: {
              deviceKey: new Map(),
              keyAuthorizations: {
                nameSpaces: [
                  "org.iso.18013.5.1",
                  "org.iso.18013.5.1.GB",
                  "org.iso.18013.5.1",
                ],
              },
            },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/deviceKeyInfo/keyAuthorizations/nameSpaces",
              message: "must NOT have more than 2 items",
            }),
          );
        });

        it("should return false when items are not unique", () => {
          const data = testMobileSecurityObjectBuilder(
            defaultData,
          ).withOverrides({
            deviceKeyInfo: {
              deviceKey: new Map(),
              keyAuthorizations: {
                nameSpaces: ["org.iso.18013.5.1", "org.iso.18013.5.1"],
              },
            },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/deviceKeyInfo/keyAuthorizations/nameSpaces",
              keyword: "uniqueItems",
            }),
          );
        });
      });
    });
  });

  describe("valueDigests", () => {
    it("should return false when org.iso.18013.5.1 is missing", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        valueDigests: {
          "org.iso.18013.5.1.GB": new Map(),
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/valueDigests",
          message: "must have required property 'org.iso.18013.5.1'",
        }),
      );
    });

    it("should return false when org.iso.18013.5.1.GB is missing", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        valueDigests: {
          "org.iso.18013.5.1": new Map(),
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/valueDigests",
          message: "must have required property 'org.iso.18013.5.1.GB'",
        }),
      );
    });

    it("should return false when it contains additional properties", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        valueDigests: {
          ...defaultData.valueDigests,
          "org.unknown.namespace": new Map(),
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/valueDigests",
          message: "must NOT have additional properties",
        }),
      );
    });

    describe("org.iso.18013.5.1", () => {
      it("should return false when it is not a Map", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            valueDigests: {
              "org.iso.18013.5.1": {},
              "org.iso.18013.5.1.GB": new Map(),
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
      });
    });

    describe("org.iso.18013.5.1.GB", () => {
      it("should return false when it is not a Map", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            valueDigests: {
              "org.iso.18013.5.1": new Map(),
              "org.iso.18013.5.1.GB": {},
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
      });
    });
  });

  describe("validityInfo", () => {
    it("should return false when signed is missing", () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { signed, ...rest } = defaultData.validityInfo;
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        validityInfo: rest,
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/validityInfo",
          message: "must have required property 'signed'",
        }),
      );
    });

    it("should return false when validFrom is missing", () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { validFrom, ...rest } = defaultData.validityInfo;
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        validityInfo: rest,
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/validityInfo",
          message: "must have required property 'validFrom'",
        }),
      );
    });

    it("should return false when validUntil is missing", () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { validUntil, ...rest } = defaultData.validityInfo;
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        validityInfo: rest,
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/validityInfo",
          message: "must have required property 'validUntil'",
        }),
      );
    });

    it("should return false when it contains additional properties", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        validityInfo: {
          ...defaultData.validityInfo,
          extra: "not allowed",
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/validityInfo",
          message: "must NOT have additional properties",
        }),
      );
    });

    describe("signed", () => {
      it("should return false when it is not a valid date-time", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            validityInfo: {
              ...defaultData.validityInfo,
              signed: "not-a-date",
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/validityInfo/signed",
            keyword: "format",
            params: { format: "date-time" },
          }),
        );
      });
    });

    describe("validFrom", () => {
      it("should return false when it is not a valid date-time", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            validityInfo: {
              ...defaultData.validityInfo,
              validFrom: "not-a-date",
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/validityInfo/validFrom",
            keyword: "format",
            params: { format: "date-time" },
          }),
        );
      });
    });

    describe("validUntil", () => {
      it("should return false when it is not a valid date-time", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            validityInfo: {
              ...defaultData.validityInfo,
              validUntil: "not-a-date",
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/validityInfo/validUntil",
            keyword: "format",
            params: { format: "date-time" },
          }),
        );
      });
    });

    describe("expectedUpdate", () => {
      it("should return false when it is not a valid date-time", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            validityInfo: {
              ...defaultData.validityInfo,
              expectedUpdate: "not-a-date",
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/validityInfo/expectedUpdate",
            keyword: "format",
            params: { format: "date-time" },
          }),
        );
      });

      it("should return true when it is absent", () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { expectedUpdate, ...rest } = defaultData.validityInfo;
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            validityInfo: rest,
          },
        );

        expect(validate(data)).toBe(true);
      });
    });
  });

  describe("status", () => {
    it("should return false when status_list is missing", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        status: {},
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/status",
          message: "must have required property 'status_list'",
        }),
      );
    });

    it("should return false when it contains additional properties", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        status: {
          status_list: {
            idx: 1,
            uri: "https://example.com/status",
          },
          extra: "not allowed",
        },
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/status",
          message: "must NOT have additional properties",
        }),
      );
    });

    describe("status_list", () => {
      it("should return false when idx is missing", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            status: {
              status_list: {
                uri: "https://example.com/status",
              },
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/status/status_list",
            message: "must have required property 'idx'",
          }),
        );
      });

      it("should return false when uri is missing", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            status: {
              status_list: {
                idx: 1,
              },
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/status/status_list",
            message: "must have required property 'uri'",
          }),
        );
      });

      it("should return false when it contains additional properties", () => {
        const data = testMobileSecurityObjectBuilder(defaultData).withOverrides(
          {
            status: {
              status_list: {
                idx: 1,
                uri: "https://example.com/status",
                extra: "not allowed",
              },
            },
          },
        );

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/status/status_list",
            message: "must NOT have additional properties",
          }),
        );
      });

      describe("idx", () => {
        it("should return false when it is not a number", () => {
          const data = testMobileSecurityObjectBuilder(
            defaultData,
          ).withOverrides({
            status: {
              status_list: {
                idx: "not-a-number",
                uri: "https://example.com/status",
              },
            },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/status/status_list/idx",
              keyword: "type",
              params: { type: "number" },
            }),
          );
        });
      });

      describe("uri", () => {
        it("should return false when it is not a valid URI", () => {
          const data = testMobileSecurityObjectBuilder(
            defaultData,
          ).withOverrides({
            status: {
              status_list: {
                idx: 1,
                uri: "not-a-uri",
              },
            },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/status/status_list/uri",
              keyword: "format",
              params: { format: "uri" },
            }),
          );
        });
      });
    });
  });

  it("should return true when data is valid", () => {
    const isValid = validate(defaultData);

    expect(isValid).toBe(true);
  });
});

const defaultData: MobileSecurityObject = {
  version: "1.0",
  digestAlgorithm: "SHA-256",
  deviceKeyInfo: {
    deviceKey: new Map(),
    keyAuthorizations: {
      nameSpaces: ["org.iso.18013.5.1", "org.iso.18013.5.1.GB"],
    },
  },
  valueDigests: {
    "org.iso.18013.5.1": new Map(),
    "org.iso.18013.5.1.GB": new Map(),
  },
  docType: "org.iso.18013.5.1.mDL",
  validityInfo: {
    signed: "2023-10-10T10:10:10Z",
    validFrom: "2023-10-10T10:10:10Z",
    validUntil: "2024-10-10T10:10:10Z",
    expectedUpdate: "2024-06-01T00:00:00Z",
  },
  status: {
    status_list: {
      idx: 1,
      uri: "https://example.com/status",
    },
  },
};

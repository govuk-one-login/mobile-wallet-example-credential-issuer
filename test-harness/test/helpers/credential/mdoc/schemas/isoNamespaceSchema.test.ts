import { getAjvInstance } from "../../../ajv/ajvInstance";
import { isoNamespaceSchema } from "./isoNamespaceSchema";
import { testIssuerSignedItemsBuilder } from "./testIssuerSignedItemsBuilder";

describe("isoNamespaceSchema", () => {
  const ajv = getAjvInstance();
  const validate = ajv.compile(isoNamespaceSchema);

  it("should return false if it is not array", () => {
    const data = {};

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        message: "must be array",
      }),
    );
  });

  it("should return false if array does not contain objects only", () => {
    const data = [
      "string",
      ...testIssuerSignedItemsBuilder(defaultData).withDefaults(),
    ];

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "/0",
        message: "must be object",
      }),
    );
  });

  it.each([
    "family_name",
    "given_name",
    "birth_date",
    "issue_date",
    "expiry_date",
    "issuing_country",
    "issuing_authority",
    "document_number",
    "portrait",
    "birth_place",
    "driving_privileges",
    "un_distinguishing_sign",
    "resident_address",
    "resident_postal_code",
    "resident_city",
  ])(
    "should return false if required object with elementIdentifier '%s' is missing",
    (elementIdentifier) => {
      const data =
        testIssuerSignedItemsBuilder(defaultData).withMissingRequiredElement(
          elementIdentifier,
        );

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          message: "must be equal to constant",
          params: { allowedValue: elementIdentifier },
        }),
      );
    },
  );

  describe("IssuerSignedItem", () => {
    describe("digestID", () => {
      it("should return false if it is missing", () => {
        const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
          digestID: undefined,
          elementIdentifier: "family_name",
        });

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0",
            message: "must have required property 'digestID'",
          }),
        );
      });

      it("should return false if it is smaller than 0", () => {
        const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
          digestID: -1,
          elementIdentifier: "family_name",
        });

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0/digestID",
            message: "must be >= 0",
          }),
        );
      });

      it("should return false if it is larger than 2147483648", () => {
        const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
          digestID: 2147483649,
          elementIdentifier: "family_name",
        });

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0/digestID",
            message: "must be <= 2147483648",
          }),
        );
      });
    });

    describe("random", () => {
      it("should return false if it is missing", () => {
        const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
          random: undefined,
          elementIdentifier: "family_name",
        });

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0",
            message: "must have required property 'random'",
          }),
        );
      });

      it("should return false if it is not a Uint8Array", () => {
        const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
          random: "not Uint8Array",
          elementIdentifier: "family_name",
        });

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0/random",
            message: "must be instance of Uint8Array",
          }),
        );
      });
    });

    describe("elementIdentifier", () => {
      it("should return false if it is missing", () => {
        const data = [
          {
            digestID: 999,
            random: new Uint8Array([1]),
            elementIdentifier: undefined,
            elementValue: "some value",
          },
          ...testIssuerSignedItemsBuilder(defaultData).withDefaults(),
        ];

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0",
            message: "must have required property 'elementIdentifier'",
          }),
        );
      });

      it("should return false if it is not one of the allowed values", () => {
        const data = [
          {
            digestID: 999,
            random: new Uint8Array([1]),
            elementIdentifier: "unknownElementIdentifier",
            elementValue: "some value",
          },
          ...testIssuerSignedItemsBuilder(defaultData).withDefaults(),
        ];

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/0/elementIdentifier",
            message: "must be equal to one of the allowed values",
            params: {
              allowedValues: [
                "family_name",
                "given_name",
                "portrait",
                "birth_date",
                "age_over_18",
                "age_over_21",
                "age_over_25",
                "birth_place",
                "issue_date",
                "expiry_date",
                "issuing_authority",
                "issuing_country",
                "document_number",
                "resident_address",
                "resident_postal_code",
                "resident_city",
                "driving_privileges",
                "un_distinguishing_sign",
              ],
            },
          }),
        );
      });
    });

    describe("elementValue", () => {
      describe("family_name", () => {
        it("should return false if it is not string", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "family_name",
            elementValue: 123,
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/0/elementValue",
              message: "must be string",
            }),
          );
        });
      });

      describe("age_over_18", () => {
        it("should return false if it is not boolean", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "age_over_18",
            elementValue: 123,
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/15/elementValue",
              message: "must be boolean",
            }),
          );
        });
      });

      describe("birth_date", () => {
        it("should return false if it is not in the format YYYY-MM-DD", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "birth_date",
            elementValue: "01-01-1990",
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue",
              message: 'must match pattern "^\\d{4}-\\d{2}-\\d{2}$"',
            }),
          );
        });
      });

      describe("driving_privileges", () => {
        it("should return false if it is not array", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "driving_privileges",
            elementValue: { vehicle_category_code: "B" },
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/10/elementValue",
              message: "must be array",
            }),
          );
        });

        it("should return false if array does not contain objects only", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "driving_privileges",
            elementValue: ["string", { vehicle_category_code: "B" }],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/10/elementValue/0",
              message: "must be object",
            }),
          );
        });

        it("should return false if driving privilege object is missing 'vehicle_category_code'", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "driving_privileges",
            elementValue: [{ issue_date: "2024-09-12" }],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/10/elementValue/0",
              message: "must have required property 'vehicle_category_code'",
            }),
          );
        });

        it("should return false if 'vehicle_category_code' is not string", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "driving_privileges",
            elementValue: [
              { vehicle_category_code: 1, issue_date: "2024-09-12" },
            ],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/10/elementValue/0/vehicle_category_code",
              message: "must be string",
            }),
          );
        });
      });

      describe("portrait", () => {
        it("should return false if it is not a Uint8Array", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "portrait",
            elementValue: "not Uint8Array",
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/8/elementValue",
              message: "must be instance of Uint8Array",
            }),
          );
        });
      });
    });
  });

  it("should return true when data is valid", () => {
    const data = testIssuerSignedItemsBuilder(defaultData).withDefaults();

    const isValid = validate(data);

    expect(isValid).toBe(true);
  });
});

const defaultData = [
  {
    digestID: 0,
    elementIdentifier: "family_name",
    random: new Uint8Array(0),
    elementValue: "Doe",
  },
  {
    digestID: 1,
    elementIdentifier: "given_name",
    random: new Uint8Array(1),
    elementValue: "John",
  },
  {
    digestID: 2,
    elementIdentifier: "birth_date",
    random: new Uint8Array(2),
    elementValue: "1990-01-01",
  },
  {
    digestID: 3,
    elementIdentifier: "issue_date",
    random: new Uint8Array(3),
    elementValue: "2020-01-01",
  },
  {
    digestID: 4,
    elementIdentifier: "expiry_date",
    random: new Uint8Array(4),
    elementValue: "2030-01-01",
  },
  {
    digestID: 5,
    elementIdentifier: "issuing_country",
    random: new Uint8Array(5),
    elementValue: "GB",
  },
  {
    digestID: 6,
    elementIdentifier: "issuing_authority",
    random: new Uint8Array(6),
    elementValue: "DVLA",
  },
  {
    digestID: 7,
    elementIdentifier: "document_number",
    random: new Uint8Array(7),
    elementValue: "12345678",
  },
  {
    digestID: 8,
    elementIdentifier: "portrait",
    random: new Uint8Array(8),
    elementValue: new Uint8Array(1),
  },
  {
    digestID: 9,
    elementIdentifier: "birth_place",
    random: new Uint8Array(9),
    elementValue: "London",
  },
  {
    digestID: 10,
    elementIdentifier: "driving_privileges",
    random: new Uint8Array(10),
    elementValue: [{ vehicle_category_code: "B" }],
  },
  {
    digestID: 11,
    elementIdentifier: "un_distinguishing_sign",
    random: new Uint8Array(11),
    elementValue: "GB",
  },
  {
    digestID: 12,
    elementIdentifier: "resident_address",
    random: new Uint8Array(12),
    elementValue: "123 Street",
  },
  {
    digestID: 13,
    elementIdentifier: "resident_postal_code",
    random: new Uint8Array(13),
    elementValue: "AB1 2CD",
  },
  {
    digestID: 14,
    elementIdentifier: "resident_city",
    random: new Uint8Array(14),
    elementValue: "London",
  },
  {
    digestID: 15,
    elementIdentifier: "age_over_18",
    random: new Uint8Array(15),
    elementValue: true,
  },
  {
    digestID: 16,
    elementIdentifier: "age_over_21",
    random: new Uint8Array(16),
    elementValue: true,
  },
  {
    digestID: 17,
    elementIdentifier: "age_over_25",
    random: new Uint8Array(17),
    elementValue: true,
  },
];

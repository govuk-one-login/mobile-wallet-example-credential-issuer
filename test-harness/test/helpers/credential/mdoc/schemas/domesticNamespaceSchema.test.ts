import { getAjvInstance } from "../../../ajv/ajvInstance";
import { domesticNamespaceSchema } from "./domesticNamespaceSchema";
import { testIssuerSignedItemsBuilder } from "./testIssuerSignedItemsBuilder";

describe("domesticNamespaceSchema", () => {
  const ajv = getAjvInstance();
  const validate = ajv.compile(domesticNamespaceSchema);

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

  it("should return false if required object with elementIdentifier 'welsh_licence' is missing", () => {
    const data =
      testIssuerSignedItemsBuilder(defaultData).withMissingRequiredElement(
        "welsh_licence",
      );

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "/0/elementIdentifier",
        message: "must be equal to constant",
        params: { allowedValue: "welsh_licence" },
      }),
    );
  });

  describe("IssuerSignedItem", () => {
    describe("digestID", () => {
      it("should return false if it is missing", () => {
        const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
          digestID: undefined,
          elementIdentifier: "welsh_licence",
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
          elementIdentifier: "welsh_licence",
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
          elementIdentifier: "welsh_licence",
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
          elementIdentifier: "welsh_licence",
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
          elementIdentifier: "welsh_licence",
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
                "title",
                "welsh_licence",
                "provisional_driving_privileges",
              ],
            },
          }),
        );
      });
    });

    describe("elementValue", () => {
      describe("title", () => {
        it("should return false if it is not string", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "title",
            elementValue: 123,
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/1/elementValue",
              message: "must be string",
            }),
          );
        });
      });

      describe("welsh_licence", () => {
        it("should return false if it is not boolean", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "welsh_licence",
            elementValue: "not boolean",
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/0/elementValue",
              message: "must be boolean",
            }),
          );
        });
      });

      describe("provisional_driving_privileges", () => {
        it("should return false if it is not array", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "provisional_driving_privileges",
            elementValue: "not array",
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue",
              message: "must be array",
            }),
          );
        });

        it("should return false if array does not contain objects only", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "provisional_driving_privileges",
            elementValue: ["string", { vehicle_category_code: "A" }],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue/0",
              message: "must be object",
            }),
          );
        });

        it("should return false if driving privilege object is missing 'vehicle_category_code'", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "provisional_driving_privileges",
            elementValue: [{ vehicle_category_code: undefined }],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue/0",
              message: "must have required property 'vehicle_category_code'",
            }),
          );
        });

        it("should return false if 'vehicle_category_code' is not string", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "provisional_driving_privileges",
            elementValue: [{ vehicle_category_code: 123 }],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue/0/vehicle_category_code",
              message: "must be string",
            }),
          );
        });

        it("should return false if 'issue_date' is not in the format YYYY-MM-DD", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "provisional_driving_privileges",
            elementValue: [
              { vehicle_category_code: "B", issue_date: "09-12-2020" },
            ],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue/0/issue_date",
              message: 'must match pattern "^\\d{4}-\\d{2}-\\d{2}$"',
            }),
          );
        });

        it("should return false if 'expiry_date' is not in the format YYYY-MM-DD", () => {
          const data = testIssuerSignedItemsBuilder(defaultData).withOverrides({
            elementIdentifier: "provisional_driving_privileges",
            elementValue: [
              { vehicle_category_code: "B", expiry_date: "09-12-2024" },
            ],
          });

          const isValid = validate(data);

          expect(isValid).toBe(false);
          expect(validate.errors).toContainEqual(
            expect.objectContaining({
              instancePath: "/2/elementValue/0/expiry_date",
              message: 'must match pattern "^\\d{4}-\\d{2}-\\d{2}$"',
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
    digestID: 1,
    elementIdentifier: "welsh_licence",
    random: new Uint8Array(1),
    elementValue: true,
  },
  {
    digestID: 2,
    elementIdentifier: "title",
    random: new Uint8Array(2),
    elementValue: "Mr",
  },
  {
    digestID: 3,
    elementIdentifier: "provisional_driving_privileges",
    random: new Uint8Array(3),
    elementValue: [
      {
        vehicle_category_code: "B",
        issue_date: "2020-01-01",
        expiry_date: "2030-01-01",
      },
    ],
  },
];

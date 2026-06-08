import { getAjvInstance } from "../../../ajv/ajvInstance";
import { issuerSignedSchema } from "./issuerSignedSchema";

describe("issuerSignedSchema", () => {
  const ajv = getAjvInstance();
  if (!ajv.getSchema("iso-namespace")) {
    ajv.addSchema({ $id: "iso-namespace", type: "array" });
  }
  if (!ajv.getSchema("domestic-namespace")) {
    ajv.addSchema({ $id: "domestic-namespace", type: "array" });
  }
  const validate = ajv.compile(issuerSignedSchema);

  const validData = {
    nameSpaces: {
      "org.iso.18013.5.1": [],
      "org.iso.18013.5.1.GB": [],
    },
    issuerAuth: [
      new Uint8Array(),
      new Map(),
      new Uint8Array(),
      new Uint8Array(),
    ],
  };

  it("should return false when it contains additional properties", () => {
    const data = {
      ...validData,
      extra: "not allowed",
    };

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        message: "must NOT have additional properties",
      }),
    );
  });

  it("should return false when nameSpaces is missing", () => {
    const data: Record<string, unknown> = { ...validData };
    delete data.nameSpaces;

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        message: "must have required property 'nameSpaces'",
      }),
    );
  });

  it("should return false when issuerAuth is missing", () => {
    const data: Record<string, unknown> = { ...validData };
    delete data.issuerAuth;

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        message: "must have required property 'issuerAuth'",
      }),
    );
  });

  describe("nameSpaces", () => {
    it("should return false when org.iso.18013.5.1 is missing", () => {
      const data = {
        ...validData,
        nameSpaces: {
          "org.iso.18013.5.1.GB": [],
        },
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/nameSpaces",
          message: "must have required property 'org.iso.18013.5.1'",
        }),
      );
    });

    it("should return false when org.iso.18013.5.1.GB is missing", () => {
      const data = {
        ...validData,
        nameSpaces: {
          "org.iso.18013.5.1": [],
        },
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/nameSpaces",
          message: "must have required property 'org.iso.18013.5.1.GB'",
        }),
      );
    });

    it("should return false when it contains additional properties", () => {
      const data = {
        ...validData,
        nameSpaces: {
          ...validData.nameSpaces,
          "org.unknown.namespace": [],
        },
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/nameSpaces",
          message: "must NOT have additional properties",
        }),
      );
    });
  });

  describe("issuerAuth", () => {
    it("should return false when it has fewer than 4 items", () => {
      const data = {
        ...validData,
        issuerAuth: [new Uint8Array(), new Map(), new Uint8Array()],
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/issuerAuth",
          message: "must NOT have fewer than 4 items",
        }),
      );
    });

    it("should return false when it has more than 4 items", () => {
      const data = {
        ...validData,
        issuerAuth: [
          new Uint8Array(),
          new Map(),
          new Uint8Array(),
          new Uint8Array(),
          new Uint8Array(),
        ],
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/issuerAuth",
          message: "must NOT have more than 4 items",
        }),
      );
    });

    describe("Protected header", () => {
      it("should return false when it is not a Uint8Array", () => {
        const data = {
          ...validData,
          issuerAuth: [
            new Map(), // protected header
            new Map(),
            new Uint8Array(),
            new Uint8Array(),
          ],
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/issuerAuth/0",
            message: "must be instance of Uint8Array",
          }),
        );
      });
    });

    describe("Unprotected header", () => {
      it("should return false when it is not a Map", () => {
        const data = {
          ...validData,
          issuerAuth: [
            new Uint8Array(),
            new Uint8Array(), // unprotected header
            new Uint8Array(),
            new Uint8Array(),
          ],
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
      });
    });

    describe("Payload", () => {
      it("should return false when it is not a Uint8Array", () => {
        const data = {
          ...validData,
          issuerAuth: [
            new Uint8Array(),
            new Map(),
            new Map(), // payload
            new Uint8Array(),
          ],
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/issuerAuth/2",
            message: "must be instance of Uint8Array",
          }),
        );
      });
    });

    describe("signature", () => {
      it("should return false when it is not a Uint8Array", () => {
        const data = {
          ...validData,
          issuerAuth: [
            new Uint8Array(),
            new Map(),
            new Uint8Array(),
            new Map(), // signature
          ],
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/issuerAuth/3",
            message: "must be instance of Uint8Array",
          }),
        );
      });
    });
  });

  it("should return true when data is valid", () => {
    const isValid = validate(validData);

    expect(isValid).toBe(true);
  });
});

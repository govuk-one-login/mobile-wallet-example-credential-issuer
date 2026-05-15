import { getAjvInstance } from "./ajvInstance";

describe("getAjvInstance", () => {
  it("should return an AJV instance", () => {
    const ajv = getAjvInstance();

    expect(ajv).toBeDefined();
    expect(typeof ajv.compile).toBe("function");
    expect(typeof ajv.validate).toBe("function");
  });

  it("should return the same instance on multiple calls (singleton)", () => {
    const ajv1 = getAjvInstance();
    const ajv2 = getAjvInstance();

    expect(ajv1).toBe(ajv2);
  });

  it("should have allErrors enabled", () => {
    const ajv = getAjvInstance();
    const schema = {
      type: "object",
      properties: {
        name: { type: "string", minLength: 1 },
        age: { type: "number", minimum: 0 },
      },
      required: ["name", "age"],
    };
    const validate = ajv.compile(schema);

    const result = validate({ name: "", age: -1 });

    expect(result).toBe(false);
    expect(validate.errors).toBeDefined();
    expect(validate.errors!.length).toBeGreaterThan(1); // Should report all errors
  });

  it("should support URI format validation", () => {
    const ajv = getAjvInstance();
    const schema = {
      type: "object",
      properties: {
        url: { type: "string", format: "uri" },
      },
    };
    const validate = ajv.compile(schema);

    // Valid URI
    expect(validate({ url: "https://example.com" })).toBe(true);
    expect(validate({ url: "ftp://files.example.com/file.txt" })).toBe(true);
    // Invalid URI
    expect(validate({ url: "not-a-uri" })).toBe(false);
    expect(validate({ url: "" })).toBe(false);
  });

  it("should validate data as Uint8Array when using custom instanceofUint8Array keyword", () => {
    const ajv = getAjvInstance();
    const schema = {
      type: "object",
      properties: {
        data: {
          type: "object",
          instanceofUint8Array: true,
        },
      },
      required: ["data"],
    };

    const validate = ajv.compile(schema);
    // Buffer/Uint8Array
    expect(validate({ data: Buffer.from("test data") })).toBe(true);
    // Non-Buffer/Uint8Array
    expect(validate({ data: "string" })).toBe(false);
    expect(validate({ data: [1, 2, 3] })).toBe(false);
    expect(validate({ data: { length: 4 } })).toBe(false);
    expect(validate({ data: null })).toBe(false);
  });

  it("should validate data as Map when using custom instanceofMap keyword", () => {
    const ajv = getAjvInstance();
    const schema = {
      type: "object",
      properties: {
        data: {
          type: "object",
          instanceofMap: true,
        },
      },
      required: ["data"],
    };

    const validate = ajv.compile(schema);
    // Map
    expect(validate({ data: new Map() })).toBe(true);
    // Non-Map
    expect(validate({ data: { object: "test" } })).toBe(false);
    expect(validate({ data: ["array"] })).toBe(false);
    expect(validate({ data: null })).toBe(false);
    expect(validate({ data: "string" })).toBe(false);
    expect(validate({ data: 1 })).toBe(false);
    expect(validate({ data: false })).toBe(false);
  });
});

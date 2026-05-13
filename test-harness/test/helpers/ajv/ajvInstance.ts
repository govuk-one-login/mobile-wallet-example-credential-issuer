import Ajv from "ajv";
import addFormats from "ajv-formats";

let ajvInstance: Ajv | null = null;

/* eslint-disable @typescript-eslint/no-explicit-any */
function createAjvInstance(): Ajv {
  const ajv = new Ajv({ allErrors: true, verbose: false });
  addFormats(ajv, { formats: ["uri", "date-time", "date"] });

  /*
   * The use of custom AJV keywords like 'instanceofUint8Array' and 'instanceofMap' is required to validate
   * data types in JavaScript that are not supported by standard JSON Schema, such as Uint8Array and Map objects.
   * These keywords allow AJV to enforce that data matches specific built-in types, rather than generic 'object'.
   * This is necessary because mDoc credential contain Uint8Array and Map data types.
   */
  ajv
    .addKeyword({
      keyword: "instanceofUint8Array",
      validate: function (schema: any, data: any) {
        if (!schema) return true;
        return data instanceof Uint8Array;
      },
      errors: true,
      error: {
        message: "must be instance of Uint8Array",
      },
    })
    .addKeyword({
      keyword: "instanceofMap",
      validate: function (schema: any, data: any) {
        if (!schema) return true;
        return data instanceof Map;
      },
      errors: false,
      type: "object",
    });
  return ajv;
}

export function getAjvInstance(): Ajv {
  ajvInstance ??= createAjvInstance();
  return ajvInstance;
}

// Function required to reset AJV instances between unit tests
export function resetAjvInstance(): void {
  ajvInstance = null;
}

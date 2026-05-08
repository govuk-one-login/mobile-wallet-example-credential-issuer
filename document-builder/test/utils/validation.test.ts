import {
  formatValidationError,
  generateErrorList,
} from "../../src/utils/validation";

describe("formatValidationError", () => {
  it("should return a formatted error object with the correct key and message", () => {
    const key = "testField";
    const message = "This is a validation error";
    const result = formatValidationError(key, message);

    expect(result).toEqual({
      [key]: {
        text: message,
        href: `#${key}`,
      },
    });
  });
});

describe("generateErrorList", () => {
  it("should return a list of unique error values", () => {
    const errors = {
      field1: { text: "Error message 1", href: "#field1" },
      field2: { text: "Error message 2", href: "#field2" },
      field3: { text: "Error message 1", href: "#field3" }, // Duplicate message
    };

    const result = generateErrorList(errors);

    expect(result).toHaveLength(2);
    expect(result).toContainEqual(errors.field2);
    expect(result).toContainEqual(errors.field3);
    expect(result).not.toContainEqual(errors.field1);
  });

  it("should return all errors if all messages are unique", () => {
    const errors = {
      field1: { text: "Error message 1", href: "#field1" },
      field2: { text: "Error message 2", href: "#field2" },
    };

    const result = generateErrorList(errors);

    expect(result).toHaveLength(2);
    expect(result).toContainEqual(errors.field1);
    expect(result).toContainEqual(errors.field2);
  });
});

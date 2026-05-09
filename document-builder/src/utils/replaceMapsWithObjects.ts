// replacer function for use with JSON.stringify to replace Maps with Objects
export function replaceMapsWithObjects(_key: string, value: object) {
  if (value instanceof Map) {
    return {
      type: "Map",
      value: [...value],
    };
  } else {
    return value;
  }
}

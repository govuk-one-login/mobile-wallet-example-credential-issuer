import { IssuerSignedItem } from "./types/issuerSigned";
import { MDLValidationError } from "./MDLValidationError";

export function validatePortrait(issuerSignedItems: IssuerSignedItem[]): void {
  const issuerSignedItem = issuerSignedItems.find(
    (item) => item.elementIdentifier === "portrait",
  )!;
  const portrait = issuerSignedItem.elementValue as Uint8Array;

  // Check for SOI (Start of Image) marker: 0xFF 0xD8 0xE0 (or 0xEE or 0xDB)
  const byte1 = portrait[0];
  const byte2 = portrait[1];
  const byte3 = portrait[2];
  const byte4 = portrait[3];

  if (
    byte1 !== 0xff ||
    byte2 !== 0xd8 ||
    byte3 !== 0xff ||
    ![0xe0, 0xee, 0xdb].includes(byte4)
  ) {
    throw new MDLValidationError(
      `Invalid SOI - JPEG should start with ffd8ffe0 or ffd8ffee or ffd8ffdb but found ${Buffer.from([byte1, byte2, byte3, byte4]).toString("hex")}`,
      "INVALID_PORTRAIT",
    );
  }

  // Look for EOI (End of Image) marker: FF D9
  const penultimateByte = portrait.at(-2) ?? 0;
  const lastByte = portrait.at(-1) ?? 0;
  if (!(penultimateByte === 0xff && lastByte === 0xd9)) {
    throw new MDLValidationError(
      `Invalid EOI - JPEG should end with ffd9 but found ${Buffer.from([penultimateByte, lastByte]).toString("hex")}`,
      "INVALID_PORTRAIT",
    );
  }
}

import path from "node:path";
import { readFileSync } from "node:fs";
import { MIME_TYPES } from "./mimeTypes";

export interface Photo {
  photoBuffer: Buffer<ArrayBufferLike>;
  mimeType: string;
}

export function getPhoto(selectedPhoto: string): Photo {
  const ext = path.extname(selectedPhoto);
  if (!MIME_TYPES[ext] || path.basename(selectedPhoto) !== selectedPhoto) {
    throw new Error("Invalid photo");
  }

  const filePath = path.resolve(process.cwd(), "dist/resources", selectedPhoto);
  const photoBuffer = readFileSync(filePath);
  const mimeType = MIME_TYPES[ext];

  return { photoBuffer, mimeType };
}

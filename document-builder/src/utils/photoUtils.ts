import path from "node:path";
import { readFileSync } from "node:fs";
import { MIME_TYPES } from "./mimeTypes";

export interface Photo {
  photoBuffer: Buffer<ArrayBufferLike>;
  mimeType: string;
}

export function getPhoto(selectedPhoto: string): Photo {
  const filePath = path.resolve(process.cwd(), "dist/resources", selectedPhoto);
  const photoBuffer = readFileSync(filePath);
  const ext = path.extname(selectedPhoto);
  const mimeType = MIME_TYPES[ext];
  return { photoBuffer, mimeType };
}

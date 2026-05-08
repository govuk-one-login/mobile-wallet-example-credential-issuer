import { readFileSync } from "node:fs";
import { getPhoto } from "../../src/utils/photoUtils";

jest.mock("node:fs", () => ({
  readFileSync: jest.fn(),
}));

describe("getPhoto", () => {
  const mockBuffer = Buffer.from([0x01, 0x02, 0x03]);

  beforeEach(() => {
    (readFileSync as jest.Mock).mockReturnValue(mockBuffer);
  });

  test.each([
    [".jpg", "100x125.jpg", "image/jpeg"],
    [".png", "100x125.png", "image/png"],
    [".jfif", "JFIF.jfif", "image/jpeg"],
    [".jp2", "140x175.jp2", "image/jp2"],
  ])(
    "should return buffer and correct mimeType for %s file",
    async (_fileType, fileName, expectedMimeType) => {
      const { photoBuffer, mimeType } = getPhoto(fileName);

      expect(photoBuffer).toBe(mockBuffer);
      expect(mimeType).toBe(expectedMimeType);
      expect(readFileSync).toHaveBeenCalledTimes(1);
    },
  );

  it("should propagate fs errors (e.g., file missing)", () => {
    (readFileSync as jest.Mock).mockImplementation(() => {
      throw new Error("ENOENT");
    });
    expect(() => getPhoto("missing.jpg")).toThrow();
  });
});

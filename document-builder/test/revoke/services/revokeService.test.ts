import axios, { AxiosResponse } from "axios";
import { revoke } from "../../../src/revoke/services/revokeService";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

const CRI_URL = "https://test-cri.example.com";
const DOCUMENT_ID = "ABC123DEF567";

describe("revokeService.ts", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should send POST request to the CRI to revoke credentials", async () => {
    const expectedRevokeUrl =
      "https://test-cri.example.com/revoke/ABC123DEF567";
    const mockCriResponse = {
      status: 202,
    } as AxiosResponse;
    mockedAxios.post.mockResolvedValue(mockCriResponse);

    await revoke(CRI_URL, DOCUMENT_ID);

    expect(mockedAxios.post).toHaveBeenCalledWith(
      expectedRevokeUrl,
      null,
      expect.objectContaining({
        validateStatus: expect.any(Function),
      }),
    );
  });

  it("should return return the status code returned by the CRI", async () => {
    const mockCriResponse = {
      status: 202,
    } as AxiosResponse;
    mockedAxios.post.mockResolvedValue(mockCriResponse);

    const response = await revoke(CRI_URL, DOCUMENT_ID);

    expect(response).toEqual(202);
  });

  it("should propagate error thrown by axios", async () => {
    mockedAxios.post.mockRejectedValue(new Error("Network error"));

    await expect(revoke(CRI_URL, DOCUMENT_ID)).rejects.toThrow("Network error");
  });
});

import { Request, Response } from "express";
import { createAccessToken } from "./token/createAccessToken";
import {
  validateGrantType,
  getPreAuthorizedCodePayload,
} from "./token/validateTokenRequest";
import {
  ACCESS_TOKEN_TTL_IN_SECS,
  getHardcodedWalletSubjectId,
  getStsSigningKeyId,
} from "../config/appConfig";
import { logger } from "../middleware/logger";
import { PREAUTHORIZED_CODE_ERRORS } from "./types/PreAuthorizedCodeErrors";

export async function stsStubAccessTokenController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    const grantType = req.body["grant_type"];
    const preAuthorizedCode = req.body["pre-authorized_code"];

    // check if pre-authorized code should trigger an error
    const preAuthorizedCodeErrors = Object.entries(PREAUTHORIZED_CODE_ERRORS);
    for (const [key, value] of preAuthorizedCodeErrors) {
      if (preAuthorizedCode === key) {
        logger.error(`Error pre-authorized code: ${preAuthorizedCode}`);
        res.status(value.statusCode).json(value.message);
        return;
      }
    }

    const isGrantTypeValid = validateGrantType(grantType);
    const payload = getPreAuthorizedCodePayload(preAuthorizedCode);

    if (!isGrantTypeValid || !payload) {
      res.status(400).json({ error: "invalid_grant" });
      return;
    }

    logger.info(`Valid pre-authorized code received: ${preAuthorizedCode}`);

    const accessToken = await createAccessToken(
      getHardcodedWalletSubjectId(),
      payload,
      getStsSigningKeyId(),
      ACCESS_TOKEN_TTL_IN_SECS,
    );

    res.status(200).json({
      access_token: accessToken,
      token_type: "bearer",
      expires_in: ACCESS_TOKEN_TTL_IN_SECS,
    });
    return;
  } catch (error) {
    logger.error(error, "An error happened creating the access token");
    res.status(500).json({ error: "server_error" });
    return;
  }
}

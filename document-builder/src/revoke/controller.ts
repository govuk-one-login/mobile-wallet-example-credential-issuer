import { Request, Response } from "express";
import { logger } from "../middleware/logger";
import { revoke } from "./services/revokeService";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { getCriEndpoint } from "../config/appConfig";
import { formatValidationError, generateErrorList } from "../utils/validation";

const REVOKE_TEMPLATE = "revoke-form.njk";

export interface RevokeConfig {
  criUrl?: string;
}

export function revokeGetController(): ExpressRouteFunction {
  return function (req: Request, res: Response): void {
    res.render(REVOKE_TEMPLATE);
  };
}

export function revokePostController({
  criUrl = getCriEndpoint(),
}: RevokeConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const documentId = req.body["documentId"];
      if (!validateDocumentId(documentId)) {
        const errors = formatValidationError(
          "documentId",
          "ID must be 5 to 25 characters long and contain only uppercase or lowercase letters and digits",
        );
        res.status(400);
        return res.render(REVOKE_TEMPLATE, {
          errors,
          errorList: generateErrorList(errors),
          documentId,
        });
      }

      const result = await revoke(criUrl, documentId);

      if (result === 404) {
        const errors = formatValidationError(
          "documentId",
          "No digital driving licence found with this licence number",
        );
        res.status(400);
        return res.render(REVOKE_TEMPLATE, {
          errors,
          errorList: generateErrorList(errors),
          documentId,
        });
      }

      if (result === 202) {
        return res.render(REVOKE_TEMPLATE, {
          message: "Digital driving licence successfully revoked",
          messageType: "success",
        });
      }

      res.render(REVOKE_TEMPLATE, {
        message:
          "Something went wrong and the credential(s) may not have been revoked",
        messageType: "error",
      });
    } catch (error) {
      logger.error(
        error,
        "An error happened trying to revoke the credential(s)",
      );
      res.render("500.njk");
    }
  };
}

function validateDocumentId(documentId: string): boolean {
  const pattern = /^[a-zA-Z0-9]{5,25}$/;
  return pattern.test(documentId);
}

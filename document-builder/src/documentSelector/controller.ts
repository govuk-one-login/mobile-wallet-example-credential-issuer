import { NextFunction, Request, Response } from "express";
import { isAuthenticated } from "../utils/isAuthenticated";
import { buildTemplateInputForDocuments } from "./utils/buildTemplateInputForDocuments";
import {
  DocumentsConfig,
  documentsConfig as config,
} from "./config/documentsConfig";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { formatValidationError, generateErrorList } from "../utils/validation";

const SELECT_DOCUMENT_TEMPLATE = "select-document-form.njk";

export interface DocumentSelectorConfig {
  documentsConfig?: DocumentsConfig;
}

export function documentSelectorGetController({
  documentsConfig = config,
}: DocumentSelectorConfig = {}): ExpressRouteFunction {
  return function (req: Request, res: Response, next: NextFunction): void {
    try {
      const credentialType = req.query["credentialType"] as string;
      const { route } = documentsConfig[credentialType] ?? {};
      if (route) {
        return res.redirect(route);
      }

      return res.render(SELECT_DOCUMENT_TEMPLATE, {
        documents: buildTemplateInputForDocuments(documentsConfig),
        authenticated: isAuthenticated(req),
      });
    } catch (error) {
      next(
        new Error("An error happened rendering document selection page", {
          cause: error,
        }),
      );
    }
  };
}

export function documentSelectorPostController({
  documentsConfig = config,
}: DocumentSelectorConfig = {}): ExpressRouteFunction {
  return function (req: Request, res: Response, next: NextFunction): void {
    try {
      const { document } = req.body;
      if (!document || !documentsConfig[document]) {
        const errors = formatValidationError(
          "document",
          "Select the document you want to create",
        );
        res.status(400);
        return res.render(SELECT_DOCUMENT_TEMPLATE, {
          errors,
          errorList: generateErrorList(errors),
          document,
          documents: buildTemplateInputForDocuments(documentsConfig),
          authenticated: isAuthenticated(req),
        });
      }

      const { route } = documentsConfig[document];
      return res.redirect(route);
    } catch (error) {
      next(
        new Error("An error happened processing request to select document", {
          cause: error,
        }),
      );
    }
  };
}

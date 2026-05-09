import { Request, Response } from "express";

export function deleteCookies(
  req: Request,
  res: Response,
  cookieNames: string[],
): void {
  if (req.cookies) {
    if (cookieNames) {
      for (const cookieName of Object.keys(req.cookies)) {
        if (cookieNames.includes(cookieName)) {
          res.clearCookie(cookieName);
        }
      }
    }
  }
}

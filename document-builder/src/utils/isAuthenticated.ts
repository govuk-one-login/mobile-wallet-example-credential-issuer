import e from "express";

export function isAuthenticated(req: e.Request): boolean {
  return !!req.cookies?.id_token;
}

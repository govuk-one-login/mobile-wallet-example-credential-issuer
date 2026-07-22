import { Request, Response } from "express";

export const robotsTxtController = (req: Request, res: Response) => {
  res.type("text/plain").send("User-agent: *\nDisallow: /");
};

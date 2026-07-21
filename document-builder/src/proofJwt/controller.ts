import { Request, Response, NextFunction } from "express";
import { getProofJwt } from "./proofJwt";
import { getMockProofSigningKeyId } from "../config/appConfig";

export async function proofJwtController(
  req: Request,
  res: Response,
  next: NextFunction,
): Promise<void> {
  try {
    const { nonce, audience } = req.query;

    const proofJwt = await getProofJwt(
      nonce as string,
      audience as string,
      getMockProofSigningKeyId(),
    );

    res.status(200).json({ proofJwt });
    return;
  } catch (error) {
    next(new Error("An error happened processing proof JWT request", { cause: error }));
  }
}

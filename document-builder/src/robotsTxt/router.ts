import express from "express";
import { robotsTxtController } from "./controller";

export const robotsTxtRouter = express.Router();

robotsTxtRouter.get("/robots.txt", robotsTxtController);

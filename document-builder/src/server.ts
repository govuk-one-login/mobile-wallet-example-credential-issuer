import "dotenv/config";
import { createApp } from "./app";
import { getPortNumber } from "./config/appConfig";
import { logger } from "./middleware/logger";

(async () => {
  const port = getPortNumber();
  const server = await createApp();

  server
    .listen(port, () => {
      logger.info(`Server is running on port ${port}`);
    })
    .on("error", (error: Error) => {
      logger.error(error, "Unable to start server");
    });
})();

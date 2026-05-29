import { getPortNumber } from "./config.js";
import { createApp } from "./app.js";

const port = getPortNumber();
const app = await createApp();

export const server = app
  .listen(port, async () => {
    console.log(`Server is running on port ${port}`);
  })
  .on("error", (error: Error) => {
    console.log(error, "Unable to start server");
  });

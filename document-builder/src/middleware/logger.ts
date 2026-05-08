import pino from "pino";
import PinoHttp from "pino-http";

const logger = pino({
  name: "mobile-wallet-document-builder",
  level: "debug",
  timestamp: pino.stdTimeFunctions.isoTime,
  formatters: {
    bindings: () => ({}),
    level(label) {
      return { level: label.toUpperCase() };
    },
  },
  serializers: {
    req: (req) => {
      return {
        method: req.method,
        url: req.url,
      };
    },
    res: (res) => {
      return {
        statusCode: res.statusCode,
      };
    },
  },
});

const ignorePaths = new Set<string>(["/healthcheck"]);

const loggerMiddleware = PinoHttp({
  logger,
  wrapSerializers: false,
  autoLogging: { ignore: (req) => ignorePaths.has(req.url!) },
  customErrorMessage: function (error, res) {
    return "Request errored with status code: " + res.statusCode;
  },
  customSuccessMessage: function (req, res) {
    if (res.statusCode === 404) {
      return "Resource not found";
    }
    return `${req.method} completed with status code ${res.statusCode}`;
  },
  customAttributeKeys: {
    responseTime: "timeTaken",
  },
});

export { logger, loggerMiddleware };

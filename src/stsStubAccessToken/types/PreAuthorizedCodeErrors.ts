interface PreAuthorizedCodeErrors {
  "ERROR:401": Error;
  "ERROR:CLIENT": Error;
  "ERROR:GRANT": Error;
  "ERROR:500": Error;
}

interface Error {
  statusCode: number;
  message: Message | undefined;
}

interface Message {
  error: string;
}

export const PREAUTHORIZED_CODE_ERRORS: PreAuthorizedCodeErrors = {
  "ERROR:401": {
    statusCode: 401,
    message: undefined,
  },
  "ERROR:CLIENT": {
    statusCode: 400,
    message: { error: "invalid_client" },
  },
  "ERROR:GRANT": {
    statusCode: 400,
    message: { error: "invalid_grant" },
  },
  "ERROR:500": {
    statusCode: 500,
    message: { error: "server_error" },
  },
};

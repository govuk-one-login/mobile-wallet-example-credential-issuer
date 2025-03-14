import { LogAttributes } from '@aws-lambda-powertools/logger/types';

export class LogMessage implements LogAttributes {
  static readonly STATIC_FILE_UPLOADER_STARTED = new LogMessage(
    'MOBILE_BACKEND_STATIC_FILE_UPLOADER_STARTED',
    'Lambda handler processing has started.',
    'N/A',
  );
  static readonly STATIC_FILE_UPLOADER_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_STATIC_FILE_UPLOADER_COMPLETED',
    'Lambda handler processing has completed successfully.',
    'N/A',
  );
  static readonly UPLOAD_STATIC_FILES_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_STATIC_FILES_ATTEMPT',
    'Attempting to upload the static files.',
    'N/A',
  );
  static readonly UPLOAD_STATIC_FILES_READ_FAILURE = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_STATIC_FILES_READ_FAILURE',
    'Failed read static files to be uploaded.',
    'N/A',
  );
  static readonly UPLOAD_STATIC_FILES_FAILURE = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_STATIC_FILES_FAILURE',
    'Failed to upload the static files.',
    'N/A',
  );
  static readonly UPLOAD_STATIC_FILES_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_STATIC_FILES_SUCCESS',
    'Successfully uploaded the static files.',
    'N/A',
  );
  static readonly CLOUDFORMATION_EVENT_WRITER_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_CLOUDFORMATION_EVENT_WRITER_ATTEMPT',
    'Attempting to write an event to CloudFormation.',
    'N/A',
  );
  static readonly CLOUDFORMATION_EVENT_WRITER_FAILURE = new LogMessage(
    'MOBILE_BACKEND_CLOUDFORMATION_EVENT_WRITER_FAILURE',
    'There was an unexpected error writing the event to CloudFormation.',
    'N/A',
  );
  static readonly CLOUDFORMATION_EVENT_WRITER_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_CLOUDFORMATION_EVENT_WRITER_SUCCESS',
    'Successfully wrote an event to CloudFormation.',
    'N/A',
  );
  static readonly JWKS_UPLOADER_STARTED = new LogMessage(
    'MOBILE_BACKEND_JWKS_UPLOADER_STARTED',
    'Lambda handler processing has started.',
    'N/A',
  );
  static readonly JWKS_UPLOADER_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_JWKS_UPLOADER_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'N/A',
  );
  static readonly JWKS_UPLOADER_UPLOAD_JWKS_TO_S3_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_JWKS_UPLOADER_UPLOAD_JWKS_TO_S3_ATTEMPT',
    'Lambda handler has recieved a Create, Update or Manual Invocation and will now proceed to upload JWKS to S3.',
    'N/A',
  );
  static readonly JWKS_UPLOADER_STOPPED = new LogMessage(
    'MOBILE_BACKEND_JWKS_UPLOADER_STOPPED',
    'Lambda handler has recieved a RequestType that is not Create, Update or Manual Invocation and has stopped executing.',
    'N/A',
  );
  static readonly JWKS_UPLOADER_UNEXPECTED_ERROR = new LogMessage(
    'MOBILE_BACKEND_JWKS_UPLOADER_UNEXPECTED_ERROR',
    'Failed to upload the public keys to the JWKS S3 bucket due to an unexpected error.',
    'N/A',
  );
  static readonly JWKS_UPLOADER_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_JWKS_UPLOADER_COMPLETED',
    'Lambda handler processing has completed successfully.',
    'N/A',
  );

  static readonly GET_PUBLIC_KEY_AS_JWK_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_GET_PUBLIC_KEY_AS_JWK_ATTEMPT',
    'Attempting to get public key in JWK format',
    'N/A',
  );
  static readonly FAILED_TO_RETRIEVE_PUBLIC_KEY_FROM_KMS = new LogMessage(
    'MOBILE_BACKEND_FAILED_TO_RETRIEVE_PUBLIC_KEY_FROM_KMS',
    'An error occurred while calling KMS to retrieve a public key',
    'The user cannot continue with their journey, and a 500 internal server error will be returned',
  );
  static readonly FAILED_TO_CONVERT_PUBLIC_KEY_TO_JWK = new LogMessage(
    'MOBILE_BACKEND_FAILED_TO_CONVERT_PUBLIC_KEY_TO_JWK',
    'An error occurred while trying to convert the public key retrieved from KMS to JWK format',
    'The user cannot continue with their journey, and a 500 internal server error will be returned',
  );
  static readonly GET_PUBLIC_KEY_AS_JWK_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_GET_PUBLIC_KEY_AS_JWK_SUCCESS',
    'Successfully retrieved public key in JWK format',
    'N/A',
  );
  static readonly UPLOAD_OBJECT_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_OBJECT_ATTEMPT',
    'Attempting to upload an object as JSON to an S3 bucket',
    'N/A',
  );
  static readonly UPLOAD_OBJECT_FAILURE = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_OBJECT_FAILURE',
    'Failed to upload an object as JSON to an S3 bucket',
    'N/A',
  );
  static readonly UPLOAD_OBJECT_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_UPLOAD_OBJECT_SUCCESS',
    'Successfully uploaded an object as JSON to an S3 bucket',
    'N/A',
  );
  static readonly APP_CHECK_AUTHORIZER_STARTED = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_AUTHORIZER_STARTED',
    'Mobile Backend App Check Authorizer started.',
    'N/A',
  );
  static readonly APP_CHECK_AUTHORIZER_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_AUTHORIZER_SUCCESS',
    'Mobile Backend App Check Authorizer has successfully validated the Firebase App Check token.',
    'N/A',
  );
  static readonly APP_CHECK_AUTHORIZER_INVALID_REQUEST = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_AUTHORIZER_INVALID_REQUEST',
    'Mobile Backend App Check Authorizer received an invalid request.',
    'The user is not authorized to access the requested resource and a 401 Unauthorized will be returned.',
  );
  static readonly APP_CHECK_AUTHORIZER_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_AUTHORIZER_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'N/A',
  );

  static readonly CLIENT_ATTESTATION_STARTED = new LogMessage(
    'MOBILE_BACKEND_CLIENT_ATTESTATION_STARTED',
    'Lambda handler processing has started.',
    'N/A',
  );
  static readonly CLIENT_ATTESTATION_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_CLIENT_ATTESTATION_COMPLETED',
    'Lambda handler processing has completed successfully.',
    'N/A',
  );
  static readonly CLIENT_ATTESTATION_INVALID_REQUEST = new LogMessage(
    'MOBILE_BACKEND_CLIENT_ATTESTATION_INVALID_REQUEST',
    'The request body received is invalid.',
    'The user cannot obtain a client attestation and will be unable to request an access token from STS.',
  );
  static readonly CLIENT_ATTESTATION_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_CLIENT_ATTESTATION_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'The user cannot continue with their journey, and a 500 internal server error will be returned.',
  );

  static readonly GET_JWKS_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_GET_JWKS_ATTEMPT',
    'Attempting to retrieve JWKS',
    'N/A',
  );
  static readonly GET_JWKS_FAILURE = new LogMessage(
    'MOBILE_BACKEND_GET_JWKS_FAILURE',
    'An error has occurred while calling JWKS URI',
    'The user cannot continue with their journey. The client will receive an Internal Server Error.',
  );
  static readonly MALFORMED_JWKS_RESPONSE = new LogMessage(
    'MOBILE_BACKEND_MALFORMED_JWKS_RESPONSE',
    'The request to the JWKS URI was successful, but the response is not as expected',
    'The user cannot continue with their journey. The client will receive an Internal Server Error.',
  );
  static readonly GET_JWKS_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_GET_JWKS_SUCCESS',
    'Successfully retrieved JWKS',
    'N/A',
  );
  static readonly DYNAMO_DB_SAVE_FIREBASE_JWT_ID_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_DYNAMO_DB_SAVE_FIREBASE_JWT_ID_ATTEMPT',
    'Attempting to save used Firebase JWT ID to Dynamo DB',
    'N/A',
  );
  static readonly DYNAMO_DB_SAVE_FIREBASE_JWT_ID_FAILURE = new LogMessage(
    'MOBILE_BACKEND_DYNAMO_DB_SAVE_FIREBASE_JWT_ID_FAILURE',
    'Failed to save used Firebase JWT ID to Dynamo DB',
    'The user cannot continue with their journey, and will receive an Invalid App Check Token error if the token has already been used, and a Server Error otherwise.',
  );
  static readonly DYNAMO_DB_SAVE_FIREBASE_JWT_ID_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_DYNAMO_DB_SAVE_FIREBASE_JWT_ID_SUCCESS',
    'Successfully saved used Firebase JWT ID to Dynamo DB',
    'N/A',
  );
  static readonly SIGN_JWT_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_SIGN_JWT_ATTEMPT',
    'Attempting to sign JWT using KMS',
    'N/A',
  );
  static readonly SIGN_JWT_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_SIGN_JWT_SUCCESS',
    'Successfully signed JWT using KMS',
    'N/A',
  );
  static readonly SIGN_JWT_FAILURE = new LogMessage(
    'MOBILE_BACKEND_SIGN_JWT_FAILURE',
    'An error occurred while calling KMS to sign a JWT.',
    'The user cannot continue with their journey, a 500 Internal Server Error will be returned to the client.',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_STARTED = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_STARTED',
    'App Check Incident Checker Lambda started',
    'N/A',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_COMPLETED',
    'App Check Incident Checker Lambda completed',
    'N/A',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_NO_INCIDENT_DETECTED = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_NO_INCIDENT_DETECTED',
    'App Check Incident Checker found no in progress incidents',
    'N/A',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_INCIDENT_DETECTED = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_INCIDENT_DETECTED',
    'App Check Incident Checker found in progress incidents',
    'N/A',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_FAILED_TO_DETERMINE_APP_CHECK_STATUS = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_FAILED_TO_DETERMINE_APP_CHECK_STATUS',
    'App Check Incident Checker failed to determine if there are any ongoing incidents',
    'N/A',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_INCIDENTS_ENDPOINT_CALL_FAILED = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_INCIDENTS_ENDPOINT_CALL_FAILED',
    'An error has occurred while calling the Firebase Incidents API',
    'N/A',
  );
  static readonly APP_CHECK_INCIDENT_CHECKER_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_APP_CHECK_INCIDENT_CHECKER_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'N/A',
  );
  static readonly DISABLE_APP_CHECK_FEATURE_FLAG_STARTED = new LogMessage(
    'MOBILE_BACKEND_DISABLE_APP_CHECK_FEATURE_FLAG_STARTED',
    'Lambda handler processing has started.',
    'N/A',
  );
  static readonly DISABLE_APP_CHECK_FEATURE_FLAG_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_DISABLE_APP_CHECK_FEATURE_FLAG_COMPLETED',
    'Lambda handler processing has completed.',
    'N/A',
  );
  static readonly DISABLE_APP_CHECK_FEATURE_FLAG_FAILED = new LogMessage(
    'MOBILE_BACKEND_DISABLE_APP_CHECK_FEATURE_FLAG_FAILED',
    'Lambda handler failed to disable App Check feature flag in App Info.',
    'N/A',
  );
  static readonly DISABLE_APP_CHECK_FEATURE_FLAG_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_DISABLE_APP_CHECK_FEATURE_FLAG_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'N/A',
  );
  static readonly ENABLE_APP_CHECK_FEATURE_FLAG_STARTED = new LogMessage(
    'MOBILE_BACKEND_ENABLE_APP_CHECK_FEATURE_FLAG_STARTED',
    'Lambda handler processing has started.',
    'N/A',
  );
  static readonly ENABLE_APP_CHECK_FEATURE_FLAG_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_ENABLE_APP_CHECK_FEATURE_FLAG_COMPLETED',
    'Lambda handler processing has completed.',
    'N/A',
  );
  static readonly ENABLE_APP_CHECK_FEATURE_FLAG_FAILED = new LogMessage(
    'MOBILE_BACKEND_ENABLE_APP_CHECK_FEATURE_FLAG_FAILED',
    'Lambda handler failed to ENABLE App Check feature flag in App Info.',
    'N/A',
  );
  static readonly ENABLE_APP_CHECK_FEATURE_FLAG_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_ENABLE_APP_CHECK_FEATURE_FLAG_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'N/A',
  );
  static readonly APP_INFO_RETRIEVAL_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_APP_INFO_RETRIEVAL_ATTEMPT',
    'Attempting to retrieve App Info file from S3.',
    'N/A',
  );
  static readonly APP_INFO_RETRIEVAL_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_APP_INFO_RETRIEVAL_SUCCESS',
    'Successfully retrieved App Info file from S3.',
    'N/A',
  );
  static readonly APP_INFO_S3_FAILURE = new LogMessage(
    'MOBILE_BACKEND_APP_INFO_S3_FAILURE',
    'Failed to get App info file from S3.',
    'N/A',
  );
  static readonly APP_INFO_PARSE_FAILURE = new LogMessage(
    'MOBILE_BACKEND_APP_INFO_PARSE_FAILURE',
    'App Info file from S3 could not be parsed as a JSON object adhering to App Info schema.',
    'N/A',
  );
  static readonly TXMA_EVENT_STARTED = new LogMessage(
    'MOBILE_BACKEND_TXMA_EVENT_STARTED',
    'Lambda handler processing has started.',
    'N/A',
  );
  static readonly TXMA_EVENT_COMPLETED = new LogMessage(
    'MOBILE_BACKEND_TXMA_EVENT_COMPLETED',
    'Lambda handler processing has completed successfully.',
    'N/A',
  );
  static readonly TXMA_EVENT_INVALID_CONFIG = new LogMessage(
    'MOBILE_BACKEND_TXMA_EVENT_INVALID_CONFIG',
    'A required environment variable is missing or invalid.',
    'Audit information for the provided event will not be sent to the outbound TxMA SQS queue. The client will receive an Internal Server Error.',
  );
  static readonly TXMA_WRITE_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_TXMA_WRITE_ATTEMPT',
    'Attempting to write event to outbound TxMA SQS queue.',
    'N/A',
  );
  static readonly TXMA_WRITE_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_TXMA_WRITE_SUCCESS',
    'Successfully wrote event to outbound TxMA SQS queue.',
    'N/A',
  );
  static readonly TXMA_WRITE_FAILURE = new LogMessage(
    'MOBILE_BACKEND_TXMA_WRITE_FAILURE',
    'Failed to write event to outbound TxMA SQS queue.',
    'Audit information for the provided event will not be available. The client will receive an Internal Server Error.',
  );
  static readonly IP_ADDRESS_FROM_CLOUDFRONT_IS_MALFORMED = new LogMessage(
    'MOBILE_BACKEND_IP_ADDRESS_FROM_CLOUDFRONT_IS_MALFORMED',
    'IP Address could not be retrieved from the cloudfront-viewer-address header and the fallback value of event.requestContext.identity.sourceIp will be used for TxMA events.',
    'N/A',
  );
  static readonly TXMA_EVENT_INVALID_REQUEST = new LogMessage(
    'MOBILE_BACKEND_TXMA_EVENT_INVALID_REQUEST',
    'The request parameters are missing or invalid.',
    'Audit information for the provided event will not be sent to the outbound TxMA SQS queue. The caller will receive a 400 Bad Request error.',
  );
  static readonly KMS_ASYMMETRIC_DECRYPTION_ATTEMPT = new LogMessage(
    'MOBILE_BACKEND_KMS_ASYMMETRIC_DECRYPTION_ATTEMPT',
    'Attempting to decrypt data using asymmetric KMS key.',
    'N/A',
  );
  static readonly KMS_ASYMMETRIC_DECRYPTION_SUCCESS = new LogMessage(
    'MOBILE_BACKEND_KMS_ASYMMETRIC_DECRYPTION_SUCCESS',
    'Successfully decrypted data using asymmetric KMS key.',
    'N/A',
  );
  static readonly KMS_ASYMMETRIC_DECRYPTION_FAILURE = new LogMessage(
    'MOBILE_BACKEND_KMS_ASYMMETRIC_DECRYPTION_FAILURE',
    'The data provided cannot be decrypted with the specified KMS key.',
    'Audit information for the provided event will not be sent to the outbound TxMA SQS queue. The client will receive a 401 Unauthorized',
  );
  static readonly KMS_ASYMMETRIC_DECRYPTION_SERVER_ERROR = new LogMessage(
    'MOBILE_BACKEND_KMS_ASYMMETRIC_DECRYPTION_SERVER_ERROR',
    'A server error occurred while calling KMS to decrypt data using asymmetric key.',
    'Audit information for the provided event will not be sent to the outbound TxMA SQS queue. The client will receive a 500 Internal Server Error.',
  );
  static readonly PROTECTED_RESOURCE_AUTHORIZATION_FAILURE = new LogMessage(
    'MOBILE_BACKEND_PROTECTED_RESOURCE_AUTHORIZATION_FAILURE',
    'Request to access protected resource could not be authorized.',
    'Audit information for the provided event will not be sent to the outbound TxMA SQS queue. The client will receive a 500 Internal Server Error if this was a server error, or a 401 Unauthorized otherwise.',
  );

  private constructor(
    public readonly messageCode: string,
    public readonly message: string,
    public readonly userImpact: string,
  ) {}

  [key: string]: string; // Index signature needed to implement LogAttributesWithMessage
}

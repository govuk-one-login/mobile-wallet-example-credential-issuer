export interface LocalStackAwsConfig {
  endpoint: string;
  credentials: Credentials;
  region: string;
}

export interface Credentials {
  accessKeyId: string;
  secretAccessKey: string;
}

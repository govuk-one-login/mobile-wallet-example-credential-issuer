export interface NinoRequestBody {
  title: string;
  givenName: string;
  familyName: string;
  nino: string;
  credentialTtl: string;
  throwError: string;
}

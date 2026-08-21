export const ENVIRONMENTS = {
  LOCAL: "local",
  DEV: "dev",
  BUILD: "build",
  STAGE: "staging",
  INT: "integration",
};

export const dvsRoutesNonProdEnvs = [
  ENVIRONMENTS.LOCAL,
  ENVIRONMENTS.DEV,
  ENVIRONMENTS.BUILD,
];
export const dvsRoutesProdEnvs = [ENVIRONMENTS.INT];
export const allDvsRoutesEnvs = [...dvsRoutesNonProdEnvs, ...dvsRoutesProdEnvs];

export const gdsRoutesEnvs = [
  ENVIRONMENTS.LOCAL,
  ENVIRONMENTS.DEV,
  ENVIRONMENTS.BUILD,
  ENVIRONMENTS.STAGE,
];

export const AUTH_DISABLED_ENVS = [ENVIRONMENTS.LOCAL, ENVIRONMENTS.INT];

export function isAuthDisabled(): boolean {
  return AUTH_DISABLED_ENVS.includes(process.env.ENVIRONMENT ?? "");
}

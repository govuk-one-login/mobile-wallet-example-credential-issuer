import {
  ENVIRONMENTS,
  dvsRoutesNonProdEnvs,
  dvsRoutesProdEnvs,
  allDvsRoutesEnvs,
  gdsRoutesEnvs,
  AUTH_DISABLED_ENVS,
  isAuthDisabled,
} from "../../src/config/environments";

describe("environments", () => {
  describe("ENVIRONMENTS", () => {
    it("should define all environment values", () => {
      expect(ENVIRONMENTS.LOCAL).toBe("local");
      expect(ENVIRONMENTS.DEV).toBe("dev");
      expect(ENVIRONMENTS.BUILD).toBe("build");
      expect(ENVIRONMENTS.STAGE).toBe("staging");
      expect(ENVIRONMENTS.INT).toBe("integration");
    });
  });

  describe("dvsRoutesNonProdEnvs", () => {
    it("should contain non-prod DVS environments", () => {
      expect(dvsRoutesNonProdEnvs).toEqual([
        ENVIRONMENTS.LOCAL,
        ENVIRONMENTS.DEV,
        ENVIRONMENTS.BUILD,
      ]);
    });
  });

  describe("dvsRoutesProdEnvs", () => {
    it("should contain prod DVS environments", () => {
      expect(dvsRoutesProdEnvs).toEqual([ENVIRONMENTS.INT]);
    });
  });

  describe("allDvsRoutesEnvs", () => {
    it("should contain all DVS environments", () => {
      expect(allDvsRoutesEnvs).toEqual([
        ...dvsRoutesNonProdEnvs,
        ...dvsRoutesProdEnvs,
      ]);
    });
  });

  describe("gdsRoutesEnvs", () => {
    it("should contain GDS environments", () => {
      expect(gdsRoutesEnvs).toEqual([
        ENVIRONMENTS.LOCAL,
        ENVIRONMENTS.DEV,
        ENVIRONMENTS.BUILD,
        ENVIRONMENTS.STAGE,
      ]);
    });
  });

  describe("AUTH_DISABLED_ENVS", () => {
    it("should contain local and integration environments", () => {
      expect(AUTH_DISABLED_ENVS).toEqual([
        ENVIRONMENTS.LOCAL,
        ENVIRONMENTS.INT,
      ]);
    });
  });

  describe("isAuthDisabled", () => {
    const originalEnv = process.env.ENVIRONMENT;

    afterEach(() => {
      process.env.ENVIRONMENT = originalEnv;
    });

    it.each([ENVIRONMENTS.LOCAL, ENVIRONMENTS.INT])(
      "should return true for %s",
      (testEnvName) => {
        process.env.ENVIRONMENT = testEnvName;
        expect(isAuthDisabled()).toBe(true);
      },
    );

    it.each([ENVIRONMENTS.DEV, ENVIRONMENTS.BUILD, ENVIRONMENTS.STAGE])(
      "should return false for %s",
      (testEnvName) => {
        process.env.ENVIRONMENT = testEnvName;
        expect(isAuthDisabled()).toBe(false);
      },
    );
  });

  describe("environment coverage", () => {
    it("should cover all environments in combination of allDvsRoutesEnvs and gdsRoutesEnvs", () => {
      const allEnvironmentValues = Object.values(ENVIRONMENTS);
      const combinedEnvs = [...allDvsRoutesEnvs, ...gdsRoutesEnvs];

      allEnvironmentValues.forEach((env) => {
        expect(combinedEnvs).toContain(env);
      });
    });

    it("should have no repeated values in allDvsRoutesEnvs", () => {
      const uniqueEnvs = new Set(allDvsRoutesEnvs);
      expect(allDvsRoutesEnvs.length).toBe(uniqueEnvs.size);
    });

    it("should have no repeated values in gdsRoutesEnvs", () => {
      const uniqueEnvs = new Set(gdsRoutesEnvs);
      expect(gdsRoutesEnvs.length).toBe(uniqueEnvs.size);
    });
  });
});

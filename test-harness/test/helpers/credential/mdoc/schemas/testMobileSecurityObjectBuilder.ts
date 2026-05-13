import { MobileSecurityObject } from "../types/mobileSecurityObject";

export function testMobileSecurityObjectBuilder(data: MobileSecurityObject) {
  return {
    withDefaults() {
      return { ...data };
    },
    withOverrides(overrides: Record<string, unknown>) {
      return {
        ...data,
        ...overrides,
      };
    },
  };
}

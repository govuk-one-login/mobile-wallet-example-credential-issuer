/*
 * For a detailed explanation regarding each configuration property and type check, visit:
 * https://jestjs.io/docs/configuration
 */

export default {
  setupFiles: ['reflect-metadata'],
  transform: {
    '^.+\\.[tj]s$': ['ts-jest', { tsconfig: { allowJs: true, module: 'commonjs', esModuleInterop: true } }],
  },
  // @peculiar packages (and asn1js) are ESM-only, so they must be transformed to CommonJS for Jest
  transformIgnorePatterns: ['/node_modules/(?!(@peculiar|asn1js)/)'],
  clearMocks: true,
  collectCoverage: true,
  coverageDirectory: 'coverage',
  coverageProvider: 'v8',
  testMatch: ['**/*.test.ts'],
  collectCoverageFrom: ['**/*.ts', '!**/node_modules/**', '!**/*.test.ts', '!jest.config.ts', '!**/tests/**'],
};

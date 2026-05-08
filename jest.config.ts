const commonTsConfig = {
  module: "CommonJS",
  moduleResolution: "node",
  esModuleInterop: true,
  allowSyntheticDefaultImports: true,
};

export default {
  transform: {
    "^.+\\.tsx?$": ["ts-jest", { tsconfig: commonTsConfig }],
    "^.+\\.m?js$": ["ts-jest", { tsconfig: commonTsConfig }],
  },
  transformIgnorePatterns: ["node_modules/(?!(@cto.af|cbor2)/)"],
  setupFilesAfterEnv: ["<rootDir>/jest.setup.js"],
  reporters: [
    "default",
    ["jest-junit", { outputDirectory: "results", outputName: "report.xml" }],
  ],
  collectCoverage: true,
  collectCoverageFrom: ["src/**"],
  coveragePathIgnorePatterns: [
    "/types/",
    "<rootDir>/src/server.ts",
    "src/credentialViewer/types.ts",
  ],
  coverageDirectory: "coverage",
  coverageProvider: "v8",
  testMatch: ["**/*.test.ts"],
  testEnvironment: "node",
  clearMocks: true,
};

export default {
  setupFiles: ["reflect-metadata"],
  transform: {
    "^.+\\.(js)$": ["ts-jest"],
  },
  reporters: [
    "default",
    ["jest-junit", { outputDirectory: "results", outputName: "report.xml" }],
  ],
  testMatch: ["**/*.test.ts"],
  testEnvironment: "node",
  collectCoverage: true,
  collectCoverageFrom: ["test/**", "src/**"],
  coverageDirectory: "coverage",
  coverageProvider: "v8",
  /*
  By default, Jest skips transforming files in node_modules, assuming they're CommonJS-compatible.
  Some modern packages (cbor2, @cto.af/wtf8, jose v6) ship only ESM syntax, which Jest can't execute.
  The line below instructs Jest to transform these specific modules to CommonJS so ts-jest can process them.
  */
  transformIgnorePatterns: ["/node_modules/(?!cbor2|@cto.af/wtf8|jose)"],
  preset: "ts-jest",
};

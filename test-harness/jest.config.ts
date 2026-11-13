export default {
  setupFiles: ["reflect-metadata"],
  moduleNameMapper: {
    "^(\\.{1,2}/.*)\\.js$": "$1",
  },
  transform: {
    "^.+\\.tsx?$": ["ts-jest", {}],
    "^.+\\.js$": ["ts-jest", { diagnostics: false }],
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
  The @peculiar scope is matched as a whole because @peculiar/x509 pulls in several @peculiar/asn1-* ESM
  packages transitively, so matching individual ones risks missing a transitive import.
  */
  transformIgnorePatterns: [
    "/node_modules/(?!cbor2|@cto.af/wtf8|jose|@peculiar|asn1js|pvtsutils|pvutils)",
  ],
  preset: "ts-jest",
};

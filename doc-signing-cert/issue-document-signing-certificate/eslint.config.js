// import globals from 'globals';
// import pluginJs from '@eslint/js';
//
// /** @type {import('eslint').Linter.Config[]} */
// export default [
//   {
//     languageOptions: { globals: globals.node },
//   },
//   {
//     ignores: ['node_modules/', '.aws-sam/*'],
//   },
//   pluginJs.configs.recommended,
// ];

// eslint.config.js
import { defineConfig } from 'eslint/config';

export default defineConfig([
  {
    rules: {
      semi: 'error',
      'prefer-const': 'error',
    },
    ignores: ['node_modules/', '.aws-sam/*'],
  },
]);

// eslint.config.js
// import { defineConfig } from "eslint/config";
//
// export default defineConfig([
//   {
//     rules: {
//       semi: "error",
//       "prefer-const": "error"
//     }
//   }
// ]);

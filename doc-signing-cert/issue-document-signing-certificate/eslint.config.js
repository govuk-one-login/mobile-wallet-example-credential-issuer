// import { defineConfig } from 'eslint/config';
import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  eslint.configs.recommended,
  tseslint.configs.recommended,
  // );
  // // export default defineConfig([
  {
    rules: {
      semi: 'error',
      'prefer-const': 'error',
    },
    ignores: ['node_modules/**/*', '.aws-sam/**/*', 'coverage/**/*', '.prettierrc.cjs'],
  },
);
// ]);

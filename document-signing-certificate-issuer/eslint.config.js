import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';

export default [
  eslint.configs.recommended,
  ...tseslint.configs.strict,
  { ignores: ['**/*.js', 'node_modules/**/*', '.aws-sam/**/*', 'coverage/**/*', '.prettierrc.cjs'] },
  {
    rules: {
      semi: 'error',
      'prefer-const': 'error',
      '@typescript-eslint/no-non-null-assertion': 'off',
    },
  },
];

export const COSE_HEADER_PARAMETERS = {
  ALG: 1,
  X5_CHAIN: 33,
} as const;

export const COSE_ALGORITHMS = {
  ES256: -7,
} as const;

export const COSE_KEY_PARAMETERS = {
  KTY: 1,
  EC2_CRV: -1,
  EC2_X: -2,
  EC2_Y: -3,
} as const;

export const COSE_ELLIPTIC_CURVES = {
  P_256: 1,
} as const;

export const COSE_KEY_TYPES = {
  EC2: 2,
} as const;

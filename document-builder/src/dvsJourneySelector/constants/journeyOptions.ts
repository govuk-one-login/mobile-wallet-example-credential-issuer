import { JOURNEY_VALUES } from "./journeyValues";

export const JOURNEY_OPTIONS = [
  {
    value: JOURNEY_VALUES.ISSUE,
    text: "Issue a new test digital driving licence",
  },
  {
    value: JOURNEY_VALUES.REVOKE,
    text: "Revoke an issued test digital driving licence",
  },
] as const;

/**
 * Returns a random integer between 100000 and 999999 (both inclusive).
 * Used to for generate test document numbers.
 */
export function getRandomIntInclusive(): number {
  const minCeiled = Math.ceil(100000);
  const maxFloored = Math.floor(999999);
  return Math.floor(Math.random() * (maxFloored - minCeiled + 1) + minCeiled);
}

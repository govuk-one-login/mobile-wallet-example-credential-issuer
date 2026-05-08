export function getTimeToLiveEpoch(ttlMinutes: number): number {
  return Math.floor((Date.now() + ttlMinutes * 60 * 1000) / 1000);
}

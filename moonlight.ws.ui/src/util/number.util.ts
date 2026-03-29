export function isValidFiniteNumber(toCheck: any): boolean {
  return getValidFiniteNumber(toCheck) !== undefined;
}

export function getValidFiniteNumber(from: any, defaultValue?: number): number | undefined {
  let nr: number|undefined;
  if (typeof from === 'number') {
    nr = from;
  } else if (typeof from === 'string') {
    nr = from ? +from : undefined; // Check for an empty string, as converting an empty string to a number with the + operator returns 0.
  }
  return !isNaN(<number>nr) && isFinite(<number>nr) ? <number>nr : defaultValue;
}

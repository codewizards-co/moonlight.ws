export function isStringEqual(a: string | undefined, b: string | undefined): boolean {
    return (a??'') === (b??'');
}

export function trimString(s: string | undefined): string | undefined {
    return s ? s.trim() : undefined;
}
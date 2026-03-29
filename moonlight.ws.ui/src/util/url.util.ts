/**
 * Gets the base-URL of this web-app.
 */
export function getAppUrl(withHash = true): string {
    let href = location.href;
    const hashIndex = href.indexOf("#");
    if (hashIndex === -1) {
        console.warn("location.href does not contain a '#': " + location.href);
        href = appendSlash(href);
        return withHash ? concatUrlSegments(href, "#/") : href;
    }
    href = href.substring(0, hashIndex);
    href = appendSlash(href);
    return withHash ? concatUrlSegments(href, "#/") : href;
}

export function concatUrlSegments(...path: any[]): string {
    let result = "";
    for (const p of path) {
        if (!p) {
            // ignore empty strings, null, undefined
        } else if (result.endsWith("/") || (typeof(p) === "string" && (p.startsWith("/") || p.startsWith("?") || p.startsWith("&")))) {
            result += p;
        } else {
            // determine, whether it's a path-segment or a query-param
            if (result.includes("?")) {
                // we are in the query-part
                if (result.endsWith("?")) {
                    result += p;
                } else {
                    result += "&" + p;
                }
            } else { // path-segment
                if (result) {
                    result += "/" + p;
                } else {
                    result += p;
                }
            }
        }
    }
    return result;
}

export function appendSlash(href: string) {
    if (href.endsWith("/")) {
        return href;
    } else {
        return href + "/";
    }
}
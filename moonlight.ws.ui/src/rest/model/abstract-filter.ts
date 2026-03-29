import { ReadOptionSet } from './read-option-set';

export interface AbstractFilter extends ReadOptionSet {
    /**
     * 1-based page-number.
     * <p>
     * If a client specifies an illegal value (e.g. 0 or -5), the first page-number 1 is used silently.
     * <p>
     * If a client specifies a value greater than the last page-number available in the query-result, the page returned
     * is empty, but no error/exception is thrown.
     */
    pageNumber?: number;

    /**
     * Size of a single page, means the maximum number of items on one page. A value of 0 can be specified to query the
     * number of the items, only, without actually querying a single item. If a client specifies a negative value, 0 is
     * used silently. If a client specifies a {@code pageSize} greater than the maximum, the maximum page-size is used
     * silently.
     */
    pageSize?: number;

    /**
     * Comma-separated list of properties to be sorted, optionally suffixed by
     * {@code ":asc"} or {@code ":desc"}.
     * <p>
     * For example: {@code "sku:asc,created:desc,id"} is equivalent to
     * {@code "sku:asc,created:desc,id:asc"} and means to order by the 3 properties
     * "sku", "created" and "id", where the 2nd property is sorted in descending
     * order (newest first) and the other 2 properties are sorted in ascending
     * order.
     */
    sort?: string;

    /**
     * Comma-separated list of relation-properties to be fetched in the same
     * HTTP-request.
     * <p>
     * For example, the {@link Supplier.party}
     */
    fetch?: string;
}
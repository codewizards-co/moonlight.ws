
export interface AbstractPage<T> {
    items: T[];

    /**
     * 1-based page-number.
     */
    pageNumber: number;

    /**
     * Size of a single page, means the maximum number of items on one page.
     */
    pageSize: number;

    /**
     * Last page-number, calculated from the {@link #getTotalSize() totalSize} and
     * the {@link #getPageSize() pageSize}.
     * <p>
     * The {@link #getPageNumber() pageNumber} can be greater than this
     * {@code lastPageNumber}. This is a valid state and simply causes an empty page
     * to be returned.
     * <p>
     * If the {@code pageSize} is 0, there is no last page-number (i.e. this is
     * {@code null}).
     */
    lastPageNumber?: number;

    /**
     * Total size of the query-result (number of items found).
     */
    totalSize: number;
}
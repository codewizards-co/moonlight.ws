package moonlight.ws.api;

import static moonlight.ws.api.RestConst.*;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Filter {

	/**
	 * First and default page-number.
	 */
	public static final int FIRST_PAGE_NUMBER = 1;

	/**
	 * Minimum page-size. Specifying a {@code pageSize} of 0 can be used to query
	 * the number of items, only, without actually querying any item.
	 */
	public static final int MIN_PAGE_SIZE = 0;

	/**
	 * Maximum page-size.
	 */
	public static final int MAX_PAGE_SIZE = 500;

	public static final int DEFAULT_PAGE_SIZE = 50;

	public static final boolean DEFAULT_INCLUDE_DELETED = false;

	/**
	 * 1-based page-number.
	 * <p>
	 * If a client specifies an illegal value (e.g. 0 or -5), the
	 * {@linkplain #FIRST_PAGE_NUMBER first page-number} {@value #FIRST_PAGE_NUMBER}
	 * is used silently.
	 * <p>
	 * If a client specifies a value greater than the last page-number available in
	 * the query-result, the page returned is empty, but no error/exception is
	 * thrown.
	 */
	@QueryParam(QUERY_PAGE_NUMBER)
	private Integer pageNumber;

	/**
	 * Size of a single page, means the maximum number of items on one page. A value
	 * of {@value #MIN_PAGE_SIZE} can be specified to query the number of the items,
	 * only, without actually querying a single item. If a client specifies a
	 * negative value, {@value #MIN_PAGE_SIZE} is used silently. If a client
	 * specifies a {@code pageSize} greater than the maximum
	 * {@value #MAX_PAGE_SIZE}, the maximum page-size is used silently.
	 */
	@QueryParam(QUERY_PAGE_SIZE)
	private Integer pageSize;

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
	@QueryParam(QUERY_SORT)
	private String sort;

	@QueryParam(QUERY_INCLUDE_DELETED)
	private Boolean includeDeleted;

//	/**
//	 * Comma-separated list of relation-properties to be fetched in the same
//	 * HTTP-request.
//	 * <p>
//	 * For example, the {@link SupplierDto#getParty() Supplier.party}
//	 */
//	@QueryParam("fetch")
//	private String fetch;

	public int getPageNumberOrDefault() {
		return Math.max(FIRST_PAGE_NUMBER, pageNumber == null ? FIRST_PAGE_NUMBER : pageNumber);
	}

	public int getPageSizeOrDefault() {
		return Math.min(MAX_PAGE_SIZE, Math.max(MIN_PAGE_SIZE, pageSize == null ? DEFAULT_PAGE_SIZE : pageSize));
	}

	public boolean getIncludeDeletedOrDefault() {
		return includeDeleted == null ? DEFAULT_INCLUDE_DELETED : includeDeleted.booleanValue();
	}
}

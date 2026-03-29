package moonlight.ws.api;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public abstract class AbstractDtoPage<T> {

	private List<T> items;

	/**
	 * 1-based page-number.
	 */
	private long pageNumber;

	/**
	 * Size of a single page, means the maximum number of items on one page.
	 */
	private long pageSize;

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
	private Long lastPageNumber;

	/**
	 * Total size of the query-result (number of items found).
	 */
	private long totalSize;

	public List<T> getItems() {
		if (items == null) {
			items = new ArrayList<>();
		}
		return items;
	}

	public void copyFromFilter(Filter filter) {
		requireNonNull(filter, "filter");
		setPageNumber(filter.getPageNumberOrDefault());
		setPageSize(filter.getPageSizeOrDefault());
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
		calculateLastPageNumber();
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
		calculateLastPageNumber();
	}

	public void setLastPageNumber(long lastPageNumber) {
		// ignore silently
	}

	protected void calculateLastPageNumber() {
		if (pageSize < 1) {
			this.lastPageNumber = null;
			return;
		}
		long lpn = totalSize / pageSize;
		if (lpn * pageSize < totalSize) {
			++lpn;
		}
		this.lastPageNumber = Math.max(Filter.FIRST_PAGE_NUMBER, lpn);
	}
}

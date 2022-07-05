/*
 * Prism (Refracted)
 *
 * Copyright (c) 2022 M Botsko (viveleroi)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package network.darkhelmet.prism.api;

import java.util.List;

public final class PaginatedResults<T> {
    /**
     * The current page's results.
     */
    private final List<T> results;

    /**
     * Count of results per page.
     */
    private final int perPage;

    /**
     * Count of total results.
     */
    private final int totalResults;

    /**
     * The current page number.
     */
    private final int currentPage;

    /**
     * Constructor.
     *
     * @param results The results
     * @param perPage The per page count
     * @param totalResults The total results count
     * @param currentPage The current page number
     */
    public PaginatedResults(List<T> results, int perPage, int totalResults, int currentPage) {
        this.results = results;
        this.perPage = perPage;
        this.totalResults = totalResults;
        this.currentPage = currentPage;
    }

    /**
     * Check if these results have a next page.
     *
     * @return True if there's a next page
     */
    public boolean hasNextPage() {
        return currentPage < totalPages();
    }

    /**
     * Check if these results have a previous page.
     *
     * @return True if there's a previous page
     */
    public boolean hasPrevPage() {
        return currentPage > 1;
    }

    /**
     * Check if the results are empty.
     *
     * @return True if no results
     */
    public boolean isEmpty() {
        return results.isEmpty();
    }

    /**
     * Get the results.
     *
     * @return The results
     */
    public List<T> results() {
        return results;
    }

    /**
     * Get the total number of pages.
     *
     * @return The total pages
     */
    public int totalPages() {
        return (int) Math.ceil(totalResults / (double) perPage);
    }

    /**
     * Get the count of results per page.
     *
     * @return Count of results per page
     */
    public int perPage() {
        return perPage;
    }

    /**
     * Get the count of total results.
     *
     * @return Count of total results
     */
    public int totalResults() {
        return totalResults;
    }

    /**
     * Get the current page number.
     *
     * @return The current page number
     */
    public int currentPage() {
        return currentPage;
    }
}

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

package network.darkhelmet.prism.services.expectations;

import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import network.darkhelmet.prism.api.services.expectations.ExpectationType;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

public class ExpectationService {
    /**
     * Cache of expectation types and their caches.
     */
    Map<ExpectationType, ExpectationsCache> expectationsCaches = new HashMap<>();

    /**
     * The logging service.
     */
    private LoggingService loggingService;

    /**
     * Constructor.
     *
     * @param loggingService The logging service
     */
    @Inject
    public ExpectationService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Get or create an expectations cache.
     *
     * @param type The expectation type
     * @return The expectations cache
     */
    public ExpectationsCache cacheFor(ExpectationType type) {
        if (expectationsCaches.containsKey(type)) {
            return expectationsCaches.get(type);
        }

        ExpectationsCache cache = new ExpectationsCache(loggingService);
        expectationsCaches.put(type, cache);

        return cache;
    }
}

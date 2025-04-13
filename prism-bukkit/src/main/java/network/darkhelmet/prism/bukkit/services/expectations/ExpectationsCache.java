/*
 * prism
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

package network.darkhelmet.prism.bukkit.services.expectations;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import network.darkhelmet.prism.loader.services.logging.LoggingService;

public class ExpectationsCache {
    /**
     * The logging service.
     */
    private LoggingService loggingService;

    /**
     * Cache expectations and wipe them if our expectations aren't met.
     */
    Cache<Object, Object> expectations = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .evictionListener((key, value, cause) -> {
            String msg = "Removing from expectations cache: Key: %s  Value: %s  Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .build();

    /**
     * Constructor.
     *
     * @param loggingService The logging service
     */
    public ExpectationsCache(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Add a target object and a cause we expect an event will "claim".
     *
     * @param target The target object
     * @param cause The cause
     */
    public void expect(Object target, Object cause) {
        expectations.put(target, cause);
    }

    /**
     * Get the expectation.
     *
     * @param target The target
     * @return The cause, if target present
     */
    public Optional<Object> expectation(Object target) {
        return Optional.ofNullable(expectations.getIfPresent(target));
    }

    /**
     * Mark a target as a met expectation. This immediately
     * removes it from the expectations cache and avoids
     * auto-expiring.
     *
     * @param target The target
     */
    public void metExpectation(Object target) {
        expectations.invalidate(target);
    }
}


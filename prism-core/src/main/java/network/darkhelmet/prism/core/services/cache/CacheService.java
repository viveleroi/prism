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

package network.darkhelmet.prism.core.services.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import network.darkhelmet.prism.loader.services.logging.LoggingService;

@Getter
public class CacheService {
    /**
     * The logging service.
     */
    private LoggingService loggingService;

    /**
     * A cache of action keys to primary keys.
     */
    private final Cache<String, Byte> actionKeyPkMap = Caffeine.newBuilder().maximumSize(100).build();

    /**
     * A cache of entity types to primary keys.
     */
    private final Cache<String, Integer> entityTypePkMap = Caffeine.newBuilder()
        .maximumSize(200)
        .evictionListener((key, value, cause) -> {
            String msg = "Evicting entity type from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .removalListener((key, value, cause) -> {
            String msg = "Removing entity type from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .build();

    /**
     * A cache of base materials to primary keys.
     */
    private final Cache<String, Integer> materialDataPkMap = Caffeine.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .maximumSize(5000)
        .evictionListener((key, value, cause) -> {
            String msg = "Evicting material data from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .removalListener((key, value, cause) -> {
            String msg = "Removing material data from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .build();

    /**
     * A cache of named causes to primary keys.
     */
    private final Cache<String, Long> namedCausePkMap = Caffeine.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .maximumSize(100)
        .evictionListener((key, value, cause) -> {
            String msg = "Evicting named cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .removalListener((key, value, cause) -> {
            String msg = "Removing named cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .build();

    /**
     * A cache of player ids to cause primary keys.
     */
    private final Cache<Long, Long> playerCausePkMap = Caffeine.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .maximumSize(200)
        .evictionListener((key, value, cause) -> {
            String msg = "Evicting player cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .removalListener((key, value, cause) -> {
            String msg = "Removing player cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .build();

    /**
     * A cache of player uuids to primary keys.
     */
    private final Cache<UUID, Long> playerUuidPkMap = Caffeine.newBuilder()
        .maximumSize(200)
        .evictionListener((key, value, cause) -> {
            String msg = "Evicting player from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .removalListener((key, value, cause) -> {
            String msg = "Removing player from PK cache: Key: %s, Value: %s, Removal Cause: %s";
            loggingService.debug(String.format(msg, key, value, cause));
        })
        .build();

    /**
     * A cache of world uuids to primary keys.
     */
    private final Cache<UUID, Byte> worldUuidPkMap = Caffeine.newBuilder().maximumSize(10).build();

    /**
     * Constructor.
     *
     * @param loggingService The logging service
     */
    @Inject
    public CacheService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }
}

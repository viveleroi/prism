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

package network.darkhelmet.prism.core.services.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.UUID;

import lombok.Getter;

import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.configuration.cache.CacheConfiguration;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

@Getter
@Singleton
public class CacheService {
    /**
     * A cache of action keys to primary keys.
     */
    private final Cache<String, Byte> actionKeyPkMap;

    /**
     * A cache of entity types to primary keys.
     */
    private final Cache<String, Integer> entityTypePkMap;

    /**
     * A cache of base materials to primary keys.
     */
    private final Cache<String, Integer> materialDataPkMap;

    /**
     * A cache of named causes to primary keys.
     */
    private final Cache<String, Long> namedCausePkMap;

    /**
     * A cache of player ids to cause primary keys.
     */
    private final Cache<Long, Long> playerCausePkMap;

    /**
     * A cache of player uuids to primary keys.
     */
    private final Cache<UUID, Long> playerUuidPkMap;

    /**
     * A cache of world uuids to primary keys.
     */
    private final Cache<UUID, Byte> worldUuidPkMap;

    /**
     * Constructor.
     *
     * @param configurationService The configuration service
     * @param loggingService The logging service
     */
    @Inject
    public CacheService(ConfigurationService configurationService, LoggingService loggingService) {
        final CacheConfiguration cacheConfiguration = configurationService.prismConfig().cache();

        // Build the action key cache
        Caffeine<String, Byte> actionBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCacheActionKey().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting action key from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing action key from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCacheActionKey().expiresAfterAccess() != null
                && cacheConfiguration.pkCacheActionKey().expiresAfterAccess().duration() != null) {
            actionBuilder.expireAfterAccess(cacheConfiguration.pkCacheActionKey().expiresAfterAccess().duration(),
                cacheConfiguration.pkCacheActionKey().expiresAfterAccess().timeUnit());
        }

        actionKeyPkMap = actionBuilder.build();

        // Build the entity type cache
        Caffeine<String, Integer> entityBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCacheEntityType().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting entity type from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing entity type from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCacheEntityType().expiresAfterAccess() != null
                && cacheConfiguration.pkCacheEntityType().expiresAfterAccess().duration() != null) {
            entityBuilder.expireAfterAccess(cacheConfiguration.pkCacheEntityType().expiresAfterAccess().duration(),
                cacheConfiguration.pkCacheEntityType().expiresAfterAccess().timeUnit());
        }

        entityTypePkMap = entityBuilder.build();

        // Build the material data cache
        Caffeine<String, Integer> materialBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCacheMaterialData().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting material data from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing material data from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCacheMaterialData().expiresAfterAccess() != null
                && cacheConfiguration.pkCacheMaterialData().expiresAfterAccess().duration() != null) {
            materialBuilder.expireAfterAccess(cacheConfiguration.pkCacheMaterialData().expiresAfterAccess().duration(),
                cacheConfiguration.pkCacheMaterialData().expiresAfterAccess().timeUnit());
        }

        materialDataPkMap = materialBuilder.build();

        // Build the named cause cache
        Caffeine<String, Long> namedCauseBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCacheNamedCause().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting named cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing named cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCacheNamedCause().expiresAfterAccess() != null
                && cacheConfiguration.pkCacheNamedCause().expiresAfterAccess().duration() != null) {
            namedCauseBuilder.expireAfterAccess(cacheConfiguration.pkCacheNamedCause().expiresAfterAccess().duration(),
                cacheConfiguration.pkCacheNamedCause().expiresAfterAccess().timeUnit());
        }

        namedCausePkMap = namedCauseBuilder.build();

        // Build the player cause cache
        Caffeine<Long, Long> playerCauseBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCachePlayer().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting player cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing player cause from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCachePlayer().expiresAfterAccess() != null
                && cacheConfiguration.pkCachePlayer().expiresAfterAccess().duration() != null) {
            playerCauseBuilder.expireAfterAccess(cacheConfiguration.pkCachePlayer().expiresAfterAccess().duration(),
                cacheConfiguration.pkCachePlayer().expiresAfterAccess().timeUnit());
        }

        playerCausePkMap = playerCauseBuilder.build();

        // Build the player cache
        Caffeine<UUID, Long> playerBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCachePlayer().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting player from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing player from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCachePlayer().expiresAfterAccess() != null
                && cacheConfiguration.pkCachePlayer().expiresAfterAccess().duration() != null) {
            playerBuilder.expireAfterAccess(cacheConfiguration.pkCachePlayer().expiresAfterAccess().duration(),
                cacheConfiguration.pkCachePlayer().expiresAfterAccess().timeUnit());
        }

        playerUuidPkMap = playerBuilder.build();

        // Create the world cache
        Caffeine<UUID, Byte> worldBuilder = Caffeine.newBuilder()
            .maximumSize(cacheConfiguration.pkCacheWorld().maxSize())
            .evictionListener((key, value, cause) -> {
                String msg = "Evicting player from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            })
            .removalListener((key, value, cause) -> {
                String msg = "Removing player from PK cache: Key: %s, Value: %s, Removal Cause: %s";
                loggingService.debug(String.format(msg, key, value, cause));
            });

        if (cacheConfiguration.pkCacheWorld().expiresAfterAccess() != null
                && cacheConfiguration.pkCacheWorld().expiresAfterAccess().duration() != null) {
            worldBuilder.expireAfterAccess(cacheConfiguration.pkCacheWorld().expiresAfterAccess().duration(),
                cacheConfiguration.pkCacheWorld().expiresAfterAccess().timeUnit());
        }

        worldUuidPkMap = worldBuilder.build();
    }
}

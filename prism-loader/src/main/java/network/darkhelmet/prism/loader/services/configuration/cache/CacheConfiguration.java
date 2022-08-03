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

package network.darkhelmet.prism.loader.services.configuration.cache;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class CacheConfiguration {
    @Comment("Activity queries (lookups) are cached so that they can be re-used or paginated.")
    private CacheBuilderConfiguration lookupExpiration = new CacheBuilderConfiguration(
        3, new DurationConfiguration(5, TimeUnit.MINUTES));

    @Comment("Cache settings for action key/primary keys.")
    private CacheBuilderConfiguration pkCacheActionKey = new CacheBuilderConfiguration(100);

    @Comment("Cache settings for entity types/primary keys.")
    private CacheBuilderConfiguration pkCacheEntityType = new CacheBuilderConfiguration(200);

    @Comment("Cache settings for material data/primary keys.")
    private CacheBuilderConfiguration pkCacheMaterialData = new CacheBuilderConfiguration(
        5000, new DurationConfiguration(15, TimeUnit.MINUTES));

    @Comment("Cache settings for named causes/primary keys.")
    private CacheBuilderConfiguration pkCacheNamedCause = new CacheBuilderConfiguration(
        200, new DurationConfiguration(15, TimeUnit.MINUTES));

    @Comment("Cache settings for players/primary keys.")
    private CacheBuilderConfiguration pkCachePlayer = new CacheBuilderConfiguration(
        200, new DurationConfiguration(15, TimeUnit.MINUTES));

    @Comment("Cache settings for world/primary keys.")
    private CacheBuilderConfiguration pkCacheWorld = new CacheBuilderConfiguration(20);
}

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

package network.darkhelmet.prism.loader.services.configuration;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class CacheConfiguration {
    @Comment("""
            Lookup queries are cached so that they can be re-used or paginated.
            This value (duration in ticks, default = 5 minutes) determines how
            long they're held in memory before being discarded.
            """)
    private long lookupExpiration = 5 * 60 * 20;

    @Comment("Cache settings for action key/primary keys.")
    private PkCacheConfiguration pkCacheActionKey = new PkCacheConfiguration(100);

    @Comment("Cache settings for entity types/primary keys.")
    private PkCacheConfiguration pkCacheEntityType = new PkCacheConfiguration(200);

    @Comment("Cache settings for material data/primary keys.")
    private PkCacheConfiguration pkCacheMaterialData = new PkCacheConfiguration(
        5000, new DurationConfiguration(15, TimeUnit.MINUTES));

    @Comment("Cache settings for named causes/primary keys.")
    private PkCacheConfiguration pkCacheNamedCause = new PkCacheConfiguration(
        200, new DurationConfiguration(15, TimeUnit.MINUTES));

    @Comment("Cache settings for players/primary keys.")
    private PkCacheConfiguration pkCachePlayer = new PkCacheConfiguration(
        200, new DurationConfiguration(15, TimeUnit.MINUTES));

    @Comment("Cache settings for world/primary keys.")
    private PkCacheConfiguration pkCacheWorld = new PkCacheConfiguration(20);
}

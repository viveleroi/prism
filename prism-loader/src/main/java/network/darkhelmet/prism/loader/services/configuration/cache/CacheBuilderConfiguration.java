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

package network.darkhelmet.prism.loader.services.configuration.cache;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class CacheBuilderConfiguration {
    @Comment("Set a length of time (since last access) until entries are evicted.")
    private DurationConfiguration expiresAfterAccess;

    @Comment("""
            The max size of this cache.
            Data will be evicted if the cache size reaches this limit.""")
    private long maxSize;

    /**
     * Constructor.
     */
    public CacheBuilderConfiguration() {}

    /**
     * Constructor.
     *
     * @param maxSize The max size
     */
    public CacheBuilderConfiguration(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Constructor.
     *
     * @param maxSize The max size
     * @param expiresAfterAccess The duration
     */
    public CacheBuilderConfiguration(long maxSize, DurationConfiguration expiresAfterAccess) {
        this.maxSize = maxSize;
        this.expiresAfterAccess = expiresAfterAccess;
    }
}

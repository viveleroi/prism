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

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.UUID;

public class CacheService {
    /**
     * A cache of action keys to primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Object2ByteOpenHashMap<String> actionKeyPkMap = new Object2ByteOpenHashMap<>(100);

    /**
     * A cache of entity types to primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Object2IntOpenHashMap<String> entityTypePkMap = new Object2IntOpenHashMap<>(100);

    /**
     * A cache of base materials to primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Object2IntOpenHashMap<String> materialPkMap = new Object2IntOpenHashMap<>();

    /**
     * A cache of named causes to primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Object2LongOpenHashMap<String> namedCausePkMap = new Object2LongOpenHashMap<>(100);

    /**
     * A cache of player ids to cause primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Long2LongOpenHashMap playerCausePkMap = new Long2LongOpenHashMap(100);

    /**
     * A cache of player uuids to primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Object2LongOpenHashMap<UUID> playerUuidPkMap = new Object2LongOpenHashMap<>(100);

    /**
     * A cache of world uuids to primary keys.
     *
     * <p>Uses fastutil because these maps may be hit with every activity.</p>
     */
    private final Object2ByteOpenHashMap<UUID> worldUuidPkMap = new Object2ByteOpenHashMap<>(10);

    /**
     * Get the action key/primary key cache.
     *
     * @return The action key/primary key cache
     */
    public Object2ByteOpenHashMap<String> actionKeyPkMap() {
        return actionKeyPkMap;
    }

    /**
     * Get the entity type/primary key cache.
     *
     * @return The entity type/primary key cache
     */
    public Object2IntOpenHashMap<String> entityTypePkMap() {
        return entityTypePkMap;
    }

    /**
     * Get the material/primary key cache.
     *
     * @return The material/primary key cache
     */
    public Object2IntOpenHashMap<String> materialPkMap() {
        return materialPkMap;
    }

    /**
     * Get the named cause/primary key cache.
     *
     * @return The named cause/primary key cache
     */
    public Object2LongOpenHashMap<String> namedCausePkMap() {
        return namedCausePkMap;
    }

    /**
     * Get the player pk cause/primary key cache.
     *
     * @return The player pk cause/primary key cache
     */
    public Long2LongOpenHashMap playerCausePkMap() {
        return playerCausePkMap;
    }

    /**
     * Get the player uuid/primary key cache.
     *
     * @return The player uuid/primary key cache
     */
    public Object2LongOpenHashMap<UUID> playerUuidPkMap() {
        return playerUuidPkMap;
    }

    /**
     * Get the world uuid/primary key cache.
     *
     * @return The world uuid/primary key cache
     */
    public Object2ByteOpenHashMap<UUID> worldUuidPkMap() {
        return worldUuidPkMap;
    }
}

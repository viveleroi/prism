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

package org.prism_mc.prism.core.storage.dbo;

import static org.prism_mc.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    public static final Index PRISM_ACTIVITIES_ACTIONID = Internal.createIndex(
        DSL.name("idx_prism_actionId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.ACTION_ID },
        false
    );
    public static final Index PRISM_ACTIVITIES_CAUSEID = Internal.createIndex(
        DSL.name("idx_prism_causeId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.CAUSE_ID },
        false
    );
    public static final Index PRISM_ACTIVITIES_COORDINATE = Internal.createIndex(
        DSL.name("idx_prism_coordinates"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.X, PRISM_ACTIVITIES.Z, PRISM_ACTIVITIES.Y, PRISM_ACTIVITIES.TIMESTAMP },
        false
    );
    public static final Index PRISM_ACTIVITIES_ENTITYTYPEID = Internal.createIndex(
        DSL.name("idx_prism_entityTypeId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.ENTITY_TYPE_ID },
        false
    );
    public static final Index PRISM_ACTIVITIES_ITEMID = Internal.createIndex(
        DSL.name("idx_prism_itemId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.ITEM_ID },
        false
    );
    public static final Index PRISM_ACTIVITIES_BLOCKID = Internal.createIndex(
        DSL.name("idx_prism_blockId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.BLOCK_ID },
        false
    );
    public static final Index PRISM_ACTIVITIES_REPLACEDBLOCKID = Internal.createIndex(
        DSL.name("idx_prism_replacedBlockId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.BLOCK_ID },
        false
    );
    public static final Index PRISM_ACTIVITIES_WORLDID = Internal.createIndex(
        DSL.name("idx_prism_worldId"),
        PRISM_ACTIVITIES,
        new OrderField[] { PRISM_ACTIVITIES.WORLD_ID },
        false
    );
}

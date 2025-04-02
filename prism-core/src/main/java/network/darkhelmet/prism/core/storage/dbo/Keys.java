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

package network.darkhelmet.prism.core.storage.dbo;

import network.darkhelmet.prism.core.storage.dbo.records.PrismActionsRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismActivitiesCustomDataRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismActivitiesRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismCausesRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismEntityTypesRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismMaterialsRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismMetaRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismPlayersRecord;
import network.darkhelmet.prism.core.storage.dbo.records.PrismWorldsRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIONS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ACTIVITIES_CUSTOM_DATA;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_CAUSES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_ENTITY_TYPES;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_MATERIALS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_META;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_PLAYERS;
import static network.darkhelmet.prism.core.storage.adapters.sql.AbstractSqlStorageAdapter.PRISM_WORLDS;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {
    public static final UniqueKey<PrismActionsRecord> KEY_PRISM_ACTIONS_ACTION = Internal.createUniqueKey(
        PRISM_ACTIONS,
        DSL.name("KEY_prism_actions_action"),
        new TableField[] { PRISM_ACTIONS.ACTION },
        true);
    public static final UniqueKey<PrismActionsRecord> KEY_PRISM_ACTIONS_PRIMARY = Internal.createUniqueKey(
        PRISM_ACTIONS,
        DSL.name("KEY_prism_actions_PRIMARY"),
        new TableField[] { PRISM_ACTIONS.ACTION_ID },
        true);
    public static final UniqueKey<PrismActivitiesRecord> KEY_PRISM_ACTIVITIES_PRIMARY = Internal.createUniqueKey(
        PRISM_ACTIVITIES,
        DSL.name("KEY_prism_activities_PRIMARY"),
        new TableField[] { PRISM_ACTIVITIES.ACTIVITY_ID },
        true);
    public static final UniqueKey<PrismActivitiesCustomDataRecord> KEY_PRISM_ACTIVITIES_CUSTOM_DATA_PRIMARY = Internal
        .createUniqueKey(
            PRISM_ACTIVITIES_CUSTOM_DATA,
            DSL.name("KEY_prism_activities_custom_data_PRIMARY"),
            new TableField[] { PRISM_ACTIVITIES_CUSTOM_DATA.EXTRA_ID },
            true);
    public static final UniqueKey<PrismCausesRecord> KEY_PRISM_CAUSES_CAUSE = Internal.createUniqueKey(
        PRISM_CAUSES,
        DSL.name("KEY_prism_causes_cause"),
        new TableField[] { PRISM_CAUSES.CAUSE },
        true);
    public static final UniqueKey<PrismCausesRecord> KEY_PRISM_CAUSES_PRIMARY = Internal.createUniqueKey(
        PRISM_CAUSES,
        DSL.name("KEY_prism_causes_PRIMARY"),
        new TableField[] { PRISM_CAUSES.CAUSE_ID },
        true);
    public static final UniqueKey<PrismEntityTypesRecord> KEY_PRISM_ENTITY_TYPES_ENTITYTYPE = Internal.createUniqueKey(
        PRISM_ENTITY_TYPES,
        DSL.name("KEY_prism_entity_types_entityType"),
        new TableField[] { PRISM_ENTITY_TYPES.ENTITY_TYPE },
        true);
    public static final UniqueKey<PrismEntityTypesRecord> KEY_PRISM_ENTITY_TYPES_PRIMARY = Internal.createUniqueKey(
        PRISM_ENTITY_TYPES,
        DSL.name("KEY_prism_entity_types_PRIMARY"),
        new TableField[] { PRISM_ENTITY_TYPES.ENTITY_TYPE_ID },
        true);
    public static final UniqueKey<PrismMaterialsRecord> KEY_PRISM_MATERIALS_MATERIALDATA = Internal.createUniqueKey(
        PRISM_MATERIALS,
        DSL.name("KEY_prism_materials_materialData"),
        new TableField[] { PRISM_MATERIALS.MATERIAL, PRISM_MATERIALS.DATA },
            true);
    public static final UniqueKey<PrismMaterialsRecord> KEY_PRISM_MATERIALS_PRIMARY = Internal.createUniqueKey(
        PRISM_MATERIALS,
            DSL.name("KEY_prism_materials_PRIMARY"),
        new TableField[] { PRISM_MATERIALS.MATERIAL_ID },
        true);
    public static final UniqueKey<PrismMetaRecord> KEY_PRISM_META_K = Internal.createUniqueKey(
        PRISM_META,
        DSL.name("KEY_prism_meta_k"),
        new TableField[] { PRISM_META.K },
        true);
    public static final UniqueKey<PrismMetaRecord> KEY_PRISM_META_PRIMARY = Internal.createUniqueKey(
        PRISM_META,
        DSL.name("KEY_prism_meta_PRIMARY"),
        new TableField[] { PRISM_META.META_ID },
        true);
    public static final UniqueKey<PrismPlayersRecord> KEY_PRISM_PLAYERS_PLAYER_UUID = Internal.createUniqueKey(
        PRISM_PLAYERS,
        DSL.name("KEY_prism_players_player_uuid"),
        new TableField[] { PRISM_PLAYERS.PLAYER_UUID },
        true);
    public static final UniqueKey<PrismPlayersRecord> KEY_PRISM_PLAYERS_PRIMARY = Internal.createUniqueKey(
        PRISM_PLAYERS,
        DSL.name("KEY_prism_players_PRIMARY"),
        new TableField[] { PRISM_PLAYERS.PLAYER_ID },
        true);
    public static final UniqueKey<PrismWorldsRecord> KEY_PRISM_WORLDS_PRIMARY = Internal.createUniqueKey(
        PRISM_WORLDS,
        DSL.name("KEY_prism_worlds_PRIMARY"),
        new TableField[] { PRISM_WORLDS.WORLD_ID },
        true);
    public static final UniqueKey<PrismWorldsRecord> KEY_PRISM_WORLDS_WORLD_UUID = Internal.createUniqueKey(
        PRISM_WORLDS,
        DSL.name("KEY_prism_worlds_world_uuid"),
        new TableField[] { PRISM_WORLDS.WORLD_UUID },
        true);

    public static final ForeignKey<PrismActivitiesRecord, PrismActionsRecord> ACTIONID = Internal.createForeignKey(
        PRISM_ACTIVITIES,
        DSL.name("actionId"),
        new TableField[] { PRISM_ACTIVITIES.ACTION_ID },
        Keys.KEY_PRISM_ACTIONS_PRIMARY,
        new TableField[] { PRISM_ACTIONS.ACTION_ID },
        true);
    public static final ForeignKey<PrismActivitiesRecord, PrismCausesRecord> CAUSEID = Internal.createForeignKey(
        PRISM_ACTIVITIES, DSL.name("causeId"),
        new TableField[] { PRISM_ACTIVITIES.CAUSE_ID },
        Keys.KEY_PRISM_CAUSES_PRIMARY,
        new TableField[] { PRISM_CAUSES.CAUSE_ID },
        true);
    public static final ForeignKey<PrismActivitiesRecord, PrismEntityTypesRecord> ENTITYTYPEID = Internal
        .createForeignKey(
            PRISM_ACTIVITIES,
            DSL.name("entityTypeId"),
            new TableField[] { PRISM_ACTIVITIES.ENTITY_TYPE_ID },
            Keys.KEY_PRISM_ENTITY_TYPES_PRIMARY,
            new TableField[] { PRISM_ENTITY_TYPES.ENTITY_TYPE_ID },
            true);
    public static final ForeignKey<PrismActivitiesRecord, PrismMaterialsRecord> MATERIALID = Internal.createForeignKey(
        PRISM_ACTIVITIES,
        DSL.name("materialId"),
        new TableField[] { PRISM_ACTIVITIES.MATERIAL_ID },
        Keys.KEY_PRISM_MATERIALS_PRIMARY,
        new TableField[] { PRISM_MATERIALS.MATERIAL_ID },
        true);
    public static final ForeignKey<PrismActivitiesRecord, PrismMaterialsRecord> OLDMATERIALID = Internal
        .createForeignKey(
            PRISM_ACTIVITIES,
            DSL.name("oldMaterialId"),
            new TableField[] { PRISM_ACTIVITIES.OLD_MATERIAL_ID },
            Keys.KEY_PRISM_MATERIALS_PRIMARY,
            new TableField[] { PRISM_MATERIALS.MATERIAL_ID },
            true);
    public static final ForeignKey<PrismActivitiesRecord, PrismWorldsRecord> WORLDID = Internal.createForeignKey(
        PRISM_ACTIVITIES,
        DSL.name("worldId"),
        new TableField[] { PRISM_ACTIVITIES.WORLD_ID },
        Keys.KEY_PRISM_WORLDS_PRIMARY,
        new TableField[] { PRISM_WORLDS.WORLD_ID },
        true);
    public static final ForeignKey<PrismActivitiesCustomDataRecord, PrismActivitiesRecord> ACTIVITYID = Internal
        .createForeignKey(
            PRISM_ACTIVITIES_CUSTOM_DATA,
            DSL.name("activityId"),
            new TableField[] { PRISM_ACTIVITIES_CUSTOM_DATA.ACTIVITY_ID },
            Keys.KEY_PRISM_ACTIVITIES_PRIMARY,
            new TableField[] { PRISM_ACTIVITIES.ACTIVITY_ID },
            true);
    public static final ForeignKey<PrismCausesRecord, PrismPlayersRecord> PLAYERID = Internal.createForeignKey(
        PRISM_CAUSES, DSL.name("playerId"),
        new TableField[] { PRISM_CAUSES.PLAYER_ID },
        Keys.KEY_PRISM_PLAYERS_PRIMARY,
        new TableField[] { PRISM_PLAYERS.PLAYER_ID },
        true);
}

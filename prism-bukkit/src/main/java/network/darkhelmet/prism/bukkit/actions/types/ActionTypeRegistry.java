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

package network.darkhelmet.prism.bukkit.actions.types;

import com.google.inject.Singleton;

import network.darkhelmet.prism.api.actions.metadata.ReasonMetadata;
import network.darkhelmet.prism.api.actions.metadata.TeleportMetadata;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.core.actions.types.AbstractActionTypeRegistry;

@Singleton
public class ActionTypeRegistry extends AbstractActionTypeRegistry {
    /**
     * Static cache of action types.
     */
    public static final ActionType BED_ENTER =
        new BlockActionType("bed-enter", ActionResultType.NONE, false);
    public static final ActionType BLOCK_BREAK =
        new BlockActionType("block-break", ActionResultType.REMOVES, true);
    public static final ActionType BLOCK_FADE =
        new BlockActionType("block-fade", ActionResultType.REMOVES, true);
    public static final ActionType BLOCK_FORM =
        new BlockActionType("block-form", ActionResultType.CREATES, true);
    public static final ActionType BLOCK_HARVEST =
        new BlockActionType("block-harvest", ActionResultType.CREATES, true);
    public static final ActionType BLOCK_IGNITE =
        new BlockActionType("block-ignite", ActionResultType.NONE, false);
    public static final ActionType BLOCK_PLACE =
        new BlockActionType("block-place", ActionResultType.CREATES, true);
    public static final ActionType BLOCK_SHIFT =
        new BlockActionType("block-shift", ActionResultType.NONE, false);
    public static final ActionType BLOCK_SPREAD =
        new BlockActionType("block-spread", ActionResultType.CREATES, true);
    public static final ActionType BLOCK_USE =
        new BlockActionType("block-use", ActionResultType.NONE, false);
    public static final ActionType BONEMEAL_USE =
        new BlockActionType("bonemeal-use", ActionResultType.NONE, false);
    public static final ActionType BUCKET_EMPTY =
        new ItemActionType("bucket-empty", ActionResultType.NONE, false);
    public static final ActionType BUCKET_FILL =
        new ItemActionType("bucket-fill", ActionResultType.NONE, false);
    public static final ActionType ENTITY_DYE =
        new EntityActionType("entity-dye", ActionResultType.REPLACES, true);
    public static final ActionType ENTITY_EAT =
        new BlockActionType("entity-eat", ActionResultType.REMOVES, true);
    public static final ActionType ENTITY_KILL =
        new EntityActionType("entity-kill", ActionResultType.REMOVES, true);
    public static final ActionType ENTITY_LEASH =
        new EntityActionType("entity-leash", ActionResultType.NONE, false);
    public static final ActionType ENTITY_PLACE =
        new EntityActionType("entity-place", ActionResultType.CREATES, true);
    public static final ActionType ENTITY_SHEAR =
        new EntityActionType("entity-shear", ActionResultType.CREATES, true);
    public static final ActionType ENTITY_TRANSFORM =
        new EntityActionType("entity-transform", ActionResultType.CREATES, false, ReasonMetadata.class);
    public static final ActionType ENTITY_UNLEASH =
        new EntityActionType("entity-unleash", ActionResultType.NONE, false);
    public static final ActionType FLUID_FLOW =
        new BlockActionType("fluid-flow", ActionResultType.CREATES, true);
    public static final ActionType HANGING_BREAK =
        new EntityActionType("hanging-break", ActionResultType.REMOVES, true);
    public static final ActionType HANGING_PLACE =
        new EntityActionType("hanging-place", ActionResultType.CREATES, true);
    public static final ActionType INVENTORY_OPEN =
        new BlockActionType("inventory-open", ActionResultType.NONE, false);
    public static final ActionType ITEM_DISPENSE =
        new ItemActionType("item-dispense", ActionResultType.NONE, false);
    public static final ActionType ITEM_DROP =
        new ItemActionType("item-drop", ActionResultType.REMOVES, true);
    public static final ActionType ITEM_INSERT =
        new ItemActionType("item-insert", ActionResultType.CREATES, true);
    public static final ActionType ITEM_PICKUP =
        new ItemActionType("item-pickup", ActionResultType.NONE, false);
    public static final ActionType ITEM_REMOVE =
        new ItemActionType("item-remove", ActionResultType.REMOVES, true);
    public static final ActionType ITEM_THROW =
        new GenericActionType("item-throw", ActionResultType.NONE, false);
    public static final ActionType PLAYER_JOIN =
        new GenericActionType("player-join", ActionResultType.NONE, false);
    public static final ActionType PLAYER_QUIT =
        new GenericActionType("player-quit", ActionResultType.NONE, false);
    public static final ActionType PLAYER_TELEPORT =
        new GenericActionType("player-teleport", ActionResultType.NONE, false, TeleportMetadata.class);
    public static final ActionType SIGN_EDIT =
        new BlockActionType("sign-edit", ActionResultType.CREATES, false);
    public static final ActionType VEHICLE_BREAK =
        new EntityActionType("vehicle-break", ActionResultType.REMOVES, true);
    public static final ActionType VEHICLE_ENTER =
        new EntityActionType("vehicle-enter", ActionResultType.NONE, false);
    public static final ActionType VEHICLE_EXIT =
        new EntityActionType("vehicle-exit", ActionResultType.NONE, false);
    public static final ActionType VEHICLE_PLACE =
        new EntityActionType("vehicle-place", ActionResultType.CREATES, true);
    public static final ActionType XP_PICKUP =
        new GenericActionType("xp-pickup", ActionResultType.NONE, false);

    /**
     * Construct the action registry.
     */
    public ActionTypeRegistry() {
        // Register Prism actions
        registerAction(BED_ENTER);
        registerAction(BLOCK_BREAK);
        registerAction(BLOCK_FADE);
        registerAction(BLOCK_FORM);
        registerAction(BLOCK_HARVEST);
        registerAction(BLOCK_IGNITE);
        registerAction(BLOCK_PLACE);
        registerAction(BLOCK_SHIFT);
        registerAction(BLOCK_SPREAD);
        registerAction(BLOCK_USE);
        registerAction(BONEMEAL_USE);
        registerAction(BUCKET_EMPTY);
        registerAction(BUCKET_FILL);
        registerAction(ENTITY_DYE);
        registerAction(ENTITY_EAT);
        registerAction(ENTITY_KILL);
        registerAction(ENTITY_LEASH);
        registerAction(ENTITY_PLACE);
        registerAction(ENTITY_SHEAR);
        registerAction(ENTITY_TRANSFORM);
        registerAction(ENTITY_UNLEASH);
        registerAction(FLUID_FLOW);
        registerAction(HANGING_BREAK);
        registerAction(HANGING_PLACE);
        registerAction(INVENTORY_OPEN);
        registerAction(ITEM_DISPENSE);
        registerAction(ITEM_DROP);
        registerAction(ITEM_INSERT);
        registerAction(ITEM_PICKUP);
        registerAction(ITEM_REMOVE);
        registerAction(ITEM_THROW);
        registerAction(PLAYER_JOIN);
        registerAction(PLAYER_QUIT);
        registerAction(PLAYER_TELEPORT);
        registerAction(SIGN_EDIT);
        registerAction(VEHICLE_BREAK);
        registerAction(VEHICLE_ENTER);
        registerAction(VEHICLE_EXIT);
        registerAction(VEHICLE_PLACE);
        registerAction(XP_PICKUP);
    }
}

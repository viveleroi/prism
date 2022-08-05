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

package network.darkhelmet.prism.actions.types;

import com.google.inject.Singleton;

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
    public static final ActionType BLOCK_IGNITE =
        new BlockActionType("block-ignite", ActionResultType.NONE, false);
    public static final ActionType BLOCK_PLACE =
        new BlockActionType("block-place", ActionResultType.CREATES, true);
    public static final ActionType BLOCK_SPREAD =
        new BlockActionType("block-spread", ActionResultType.CREATES, true);
    public static final ActionType ENTITY_KILL =
        new EntityActionType("entity-kill", ActionResultType.REMOVES, true);
    public static final ActionType HANGING_BREAK =
        new EntityActionType("hanging-break", ActionResultType.REMOVES, true);
    public static final ActionType ITEM_DISPENSE =
        new ItemActionType("item-dispense", ActionResultType.NONE, false);
    public static final ActionType ITEM_DROP =
        new ItemActionType("item-drop", ActionResultType.REMOVES, true);
    public static final ActionType ITEM_PICKUP =
        new ItemActionType("item-pickup", ActionResultType.REMOVES, false);
    public static final ActionType PLAYER_JOIN =
        new GenericActionType("player-join", ActionResultType.NONE, false);
    public static final ActionType PLAYER_QUIT =
        new GenericActionType("player-quit", ActionResultType.NONE, false);
    public static final ActionType VEHICLE_ENTER =
        new EntityActionType("vehicle-enter", ActionResultType.NONE, false);
    public static final ActionType VEHICLE_EXIT =
        new EntityActionType("vehicle-exit", ActionResultType.NONE, false);
    public static final ActionType VEHICLE_PLACE =
        new EntityActionType("vehicle-place", ActionResultType.CREATES, false);

    /**
     * Construct the action registry.
     */
    public ActionTypeRegistry() {
        // Register Prism actions
        registerAction(BED_ENTER);
        registerAction(BLOCK_BREAK);
        registerAction(BLOCK_FADE);
        registerAction(BLOCK_FORM);
        registerAction(BLOCK_IGNITE);
        registerAction(BLOCK_PLACE);
        registerAction(BLOCK_SPREAD);
        registerAction(ENTITY_KILL);
        registerAction(HANGING_BREAK);
        registerAction(ITEM_DISPENSE);
        registerAction(ITEM_DROP);
        registerAction(ITEM_PICKUP);
        registerAction(PLAYER_JOIN);
        registerAction(PLAYER_QUIT);
        registerAction(VEHICLE_ENTER);
        registerAction(VEHICLE_EXIT);
        registerAction(VEHICLE_PLACE);
    }
}

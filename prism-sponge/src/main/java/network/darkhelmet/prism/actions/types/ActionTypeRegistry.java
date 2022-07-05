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

import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.core.actions.types.AbstractActionTypeRegistry;

public class ActionTypeRegistry extends AbstractActionTypeRegistry {
    /**
     * Static cache of action types.
     */
    public static final ActionType BLOCK_BREAK =
        new BlockActionType("block-break", ActionResultType.REMOVES, true);

    /**
     * Construct the action registry.
     */
    public ActionTypeRegistry() {
        // Register Prism actions
        registerAction(BLOCK_BREAK);
    }
}

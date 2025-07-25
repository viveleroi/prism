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

package org.prism_mc.prism.api.services.modifications;

public enum ModificationSkipReason {
    /**
     * The block already matches what the modification would do.
     */
    ALREADY_SET,

    /**
     * The action is completely blacklisted.
     */
    BLACKLISTED,

    /**
     * An error occurred.
     */
    ERRORED,

    /**
     * The action was skipped because the inventory is full.
     */
    FULL_INVENTORY,

    /**
     * The rollback is not applicable due to specifics.
     */
    NOT_APPLICABLE,

    /**
     * The action has no implementation for the modification.
     */
    NOT_IMPLEMENTED,
}

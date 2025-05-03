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

import lombok.Getter;

@Getter
public abstract class StateChange<B> {

    /**
     * The old state.
     */
    private final B oldState;

    /**
     * The new state.
     */
    private final B newState;

    /**
     * Construct a new state change.
     *
     * @param oldState The old state
     * @param newState The new state
     */
    public StateChange(B oldState, B newState) {
        this.oldState = oldState;
        this.newState = newState;
    }
}

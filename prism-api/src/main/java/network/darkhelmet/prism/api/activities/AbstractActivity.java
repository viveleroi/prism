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

package network.darkhelmet.prism.api.activities;

import lombok.Getter;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

public abstract class AbstractActivity implements IActivity {
    /**
     * The action.
     */
    @Getter
    protected final IAction action;

    /**
     * The cause.
     */
    @Getter
    protected final String cause;

    /**
     * The world coordinate, if any.
     */
    @Getter
    protected final WorldCoordinate location;

    /**
     * The causing player.
     */
    @Getter
    protected final NamedIdentity player;

    /**
     * The timestamp.
     */
    @Getter
    protected final long timestamp;

    /**
     * Construct an activity.
     *
     * @param action The action
     * @param location The world coordinate
     * @param cause The cause
     * @param player The player
     * @param timestamp The timestamp (or average)
     */
    public AbstractActivity(
            IAction action, WorldCoordinate location, String cause, NamedIdentity player, long timestamp) {
        this.action = action;
        this.cause = cause;
        this.location = location;
        this.player = player;
        this.timestamp = timestamp;
    }
}

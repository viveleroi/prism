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

package network.darkhelmet.prism.api.activities;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

public abstract class AbstractActivity implements IActivity {
    /**
     * The action.
     */
    private final IAction action;

    /**
     * The cause.
     */
    private final String cause;

    /**
     * The causing player.
     */
    private final NamedIdentity player;

    /**
     * The timestamp.
     */
    private final long timestamp;

    /**
     * The world coordinate, if any.
     */
    private final WorldCoordinate worldCoordinate;

    /**
     * Construct an activity.
     *
     * @param action The action
     * @param worldCoordinate The world coordinate
     * @param cause The cause
     * @param player The player
     * @param timestamp The timestamp (or average)
     */
    public AbstractActivity(
            IAction action, WorldCoordinate worldCoordinate, String cause, NamedIdentity player, long timestamp) {
        this.action = action;
        this.cause = cause;
        this.worldCoordinate = worldCoordinate;
        this.player = player;
        this.timestamp = timestamp;
    }

    @Override
    public IAction action() {
        return action;
    }

    @Override
    public String cause() {
        return cause;
    }

    @Override
    public WorldCoordinate location() {
        return worldCoordinate;
    }

    @Override
    public NamedIdentity player() {
        return player;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }
}

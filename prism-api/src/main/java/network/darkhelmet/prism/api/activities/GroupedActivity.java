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

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

public final class GroupedActivity extends AbstractActivity implements IGroupedActivity {
    /**
     * The count.
     */
    private final int count;

    /**
     * Constructor.
     *
     * @param action The action
     * @param worldCoordinate The world coordinate (or average)
     * @param cause The cause
     * @param player The player
     * @param timestamp The timestamp
     * @param count The count
     */
    public GroupedActivity(
            IAction action,
            WorldCoordinate worldCoordinate,
            String cause,
            NamedIdentity player,
            long timestamp,
            int count) {
        super(action, worldCoordinate, cause, player, timestamp);
        this.count = count;
    }

    @Override
    public int count() {
        return count;
    }
}

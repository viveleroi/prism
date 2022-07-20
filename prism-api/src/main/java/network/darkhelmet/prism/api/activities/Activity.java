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

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Tolerate;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

public final class Activity extends AbstractActivity implements ISingleActivity {
    /**
     * The storage engine primary key.
     */
    @Getter
    private Object primaryKey;

    /**
     * Construct a new activity.
     *
     * @param action The action
     * @param location The world coordinate
     * @param cause The cause
     * @param player The player
     */
    @Builder()
    public Activity(
            IAction action,
            WorldCoordinate location,
            String cause,
            NamedIdentity player) {
        super(action, location, cause, player, System.currentTimeMillis());
    }

    /**
     * Construct a new activity.
     *
     * @param primaryKey The storage engine primary key
     * @param action The action
     * @param location The world coordinate
     * @param cause The cause
     * @param player The player
     * @param timestamp The timestamp
     */
    public Activity(
            Object primaryKey,
            IAction action,
            WorldCoordinate location,
            String cause,
            NamedIdentity player,
            long timestamp) {
        super(action, location, cause, player, timestamp);

        this.primaryKey = primaryKey;
    }

    public static class ActivityBuilder {
        /**
         * Set the location.
         *
         * @param worldUuid The world uuid
         * @param worldName The world name
         * @param x The x coordinate
         * @param y The y coordinate
         * @param z The z coordinate
         * @return The builder
         */
        @Tolerate
        public ActivityBuilder location(UUID worldUuid, String worldName, double x, double y, double z) {
            this.location = new WorldCoordinate(new NamedIdentity(worldUuid, worldName), x, y, z);
            return this;
        }

        /**
         * Set the player.
         *
         * @param playerUuid The player uuid
         * @param playerName The player name
         * @return The builder
         */
        @Tolerate
        public ActivityBuilder player(UUID playerUuid, String playerName) {
            this.player = new NamedIdentity(playerUuid, playerName);
            return this;
        }
    }
}

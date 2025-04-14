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

import java.util.UUID;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.experimental.Tolerate;

import network.darkhelmet.prism.api.actions.Action;
import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.api.util.Pair;

/**
 * An activity represents a single activity that's either being
 * recorded to store or read back without grouping (e.g. modifications).
 */
@Getter
@SuperBuilder()
public class Activity extends AbstractActivity {
    /**
     * The storage engine primary key.
     */
    private Object primaryKey;

    /**
     * Construct a new activity.
     *
     * @param primaryKey The storage engine primary key
     * @param action The action
     * @param world The world
     * @param coordinate The coordinate
     * @param cause The cause
     * @param player The player
     * @param timestamp The timestamp
     */
    public Activity(
            Object primaryKey,
            Action action,
            Pair<UUID, String> world,
            Coordinate coordinate,
            String cause,
            Pair<UUID, String> player,
            long timestamp) {
        super(action, world, coordinate, cause, player, timestamp);

        this.primaryKey = primaryKey;
    }

    @Override
    public String toString() {
        return String.format("Activity{action=%s,cause=%s,player=%s,world=%s,coordinate=%s,timestamp=%s}",
            action, cause, player, world, coordinate, timestamp);
    }

    public abstract static class ActivityBuilder<C extends Activity, B extends ActivityBuilder<C, B>>
            extends AbstractActivityBuilder<C, B> {
        @Tolerate
        public B world(UUID worldUuid, String worldName) {
            this.world(new Pair<>(worldUuid, worldName));
            return self();
        }

        /**
         * Set the location.
         *
         * @param x The x coordinate
         * @param y The y coordinate
         * @param z The z coordinate
         * @return The builder
         */
        @Tolerate
        public B coordinate(double x, double y, double z) {
            this.coordinate(new Coordinate(x, y, z));
            return self();
        }

        /**
         * Set the player.
         *
         * @param playerUuid The player uuid
         * @param playerName The player name
         * @return The builder
         */
        @Tolerate
        public B player(UUID playerUuid, String playerName) {
            this.player(new Pair<>(playerUuid, playerName));
            return self();
        }
    }
}

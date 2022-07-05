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

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;

import org.jetbrains.annotations.NotNull;

public final class SingleActivity extends AbstractActivity implements ISingleActivity {
    /**
     * The timestamp.
     */
    private final long timestamp;

    /**
     * Construct a new activity.
     *
     * @param action The action
     * @param worldCoordinate The world coordinate
     * @param cause The cause
     * @param timestamp The timestamp
     */
    public SingleActivity(
            IAction action,
            WorldCoordinate worldCoordinate,
            String cause,
            NamedIdentity player,
            long timestamp) {
        super(action, worldCoordinate, cause, player);

        this.timestamp = timestamp;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    /**
     * Get a new builder.
     *
     * @return The activity builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        /**
         * The action.
         */
        private IAction action;

        /**
         * The cause name, if any.
         */
        private String cause;

        /**
         * The player.
         */
        private NamedIdentity player;

        /**
         * The timestamp.
         */
        private long timestamp = System.currentTimeMillis();

        /**
         * The world coordinate.
         */
        private WorldCoordinate worldCoordinate;

        /**
         * Set an action.
         *
         * @param action The action
         * @return The builder
         */
        public Builder action(IAction action) {
            this.action = action;
            return this;
        }

        /**
         * Set a cause.
         *
         * @param cause The cause
         * @return The builder
         */
        public Builder cause(String cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Set the location.
         *
         * @param worldCoordinate The world coordinate
         * @return The builder
         */
        public Builder location(WorldCoordinate worldCoordinate) {
            this.worldCoordinate = worldCoordinate;
            return this;
        }

        /**
         * Set the player.
         *
         * @param playerUuid The player uuid
         * @param playerName The player name
         * @return The builder
         */
        public Builder player(UUID playerUuid, String playerName) {
            this.player = new NamedIdentity(playerUuid, playerName);

            return this;
        }

        /**
         * Set a timestamp.
         *
         * @param timestamp The timestamp
         * @return The builder
         */
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Build the final activity.
         *
         * @return The activity
         */
        public ISingleActivity build() {
            return new SingleActivity(action, worldCoordinate, cause, player, timestamp);
        }
    }
}

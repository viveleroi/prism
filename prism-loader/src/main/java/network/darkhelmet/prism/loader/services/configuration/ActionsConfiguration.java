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

package network.darkhelmet.prism.loader.services.configuration;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class ActionsConfiguration {
    @Comment("block-break is when a player or entity destroys a block (except from burn/explode).")
    private boolean blockBreak = true;

    @Comment("block-place is when a player or entity places a block.")
    private boolean blockPlace = true;

    @Comment("entity-kill is when an entity (or player) kills another.")
    private boolean entityKill = true;

    @Comment("hanging-break is when an item frame or painting is broken/detached.\n"
            + "This event will operate if block-break is false, even for detachments.")
    private boolean hangingBreak = true;

    @Comment("item-drop is when a player or block drops an item on the ground.")
    private boolean itemDrop = true;

    @Comment("player-join is when a player connects to the server.")
    private boolean playerJoin = false;

    @Comment("player-quit is when a player disconnects from the server.")
    private boolean playerQuit = false;

    @Comment("vehicle-enter is when an entity enters a boat or minecart.")
    private boolean vehicleEnter = true;

    @Comment("vehicle-exit is when an entity exits a boat or minecart.")
    private boolean vehicleExit = true;

    @Comment("vehicle-place is when a player places a boat or minecart.")
    private boolean vehiclePlace = true;
}

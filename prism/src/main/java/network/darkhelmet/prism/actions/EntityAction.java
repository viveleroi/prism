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

package network.darkhelmet.prism.actions;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTEntity;

import java.util.Locale;

import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResultStatus;
import network.darkhelmet.prism.utils.EntityUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class EntityAction extends Action implements IEntityAction {
    /**
     * The nbt container.
     */
    private NBTContainer nbtContainer;

    /**
     * The entity type.
     */
    private EntityType entityType;

    /**
     * Construct a new entity action.
     *
     * @param type The action type
     * @param entity The entity
     */
    public EntityAction(IActionType type, Entity entity) {
        super(type);

        this.entityType = entity.getType();
        this.nbtContainer = new NBTContainer(new NBTEntity(entity).getCompound().toString());

        // Strip some data we don't want to track/rollback.
        String[] rejects = {
            "DeathTime",
            "Fire",
            "Health",
            "HurtByTimestamp",
            "HurtTime",
            "OnGround",
            "Pos",
            "UUID",
            "WorldUUIDLeast",
            "WorldUUIDMost"
        };
        for (String reject : rejects) {
            nbtContainer.removeKey(reject);
        }

        if (entity instanceof Boat boat) {
            this.descriptor += EntityUtils.treeSpeciesToDescriptor(boat.getWoodType()) + " ";
        }

        this.descriptor += entityType.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
    }

    /**
     * Construct a new entity action with the type and nbt container.
     *
     * @param type The action type
     * @param entityType The entity type
     * @param container The nbt container
     * @poram descriptor The descriptor
     */
    public EntityAction(IActionType type, EntityType entityType, NBTContainer container, String descriptor) {
        super(type, descriptor);

        this.entityType = entityType;
        this.nbtContainer = container;
    }

    @Override
    public String serializeEntityType() {
        return entityType.toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public boolean hasCustomData() {
        return this.nbtContainer != null;
    }

    @Override
    public @Nullable String serializeCustomData() {
        return nbtContainer.toString();
    }

    @Override
    public ModificationResult applyRollback(CommandSender owner, IActivity activityContext, boolean isPreview) {
        Location loc = activityContext.location();
        if (loc.getWorld() != null && entityType.getEntityClass() != null) {
            loc.getWorld().spawn(loc, entityType.getEntityClass(), entity ->
                new NBTEntity(entity).mergeCompound(nbtContainer));

            return new ModificationResult(ModificationResultStatus.APPLIED, null);
        }

        return new ModificationResult(ModificationResultStatus.SKIPPED, null);
    }

    @Override
    public ModificationResult applyRestore(CommandSender owner, IActivity activityContext, boolean isPreview) {
        return new ModificationResult(ModificationResultStatus.SKIPPED, null);
    }
}

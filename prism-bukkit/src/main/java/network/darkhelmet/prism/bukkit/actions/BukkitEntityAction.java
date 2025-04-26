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

package network.darkhelmet.prism.bukkit.actions;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;

import java.util.Locale;
import java.util.UUID;

import net.kyori.adventure.text.Component;

import network.darkhelmet.prism.api.actions.EntityAction;
import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.util.Coordinate;
import network.darkhelmet.prism.bukkit.PrismBukkit;
import network.darkhelmet.prism.bukkit.services.nbt.NbtService;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class BukkitEntityAction extends BukkitAction implements EntityAction {
    /**
     * The read/write nbt.
     */
    private final ReadWriteNBT readWriteNbt;

    /**
     * The entity type.
     */
    private final EntityType entityType;

    /**
     * Construct a new entity action.
     *
     * @param type The action type
     * @param entity The entity
     * @param metadata The metadata
     */
    public BukkitEntityAction(ActionType type, Entity entity, Record metadata) {
        this(type, entity);

        this.metadata = metadata;
    }

    /**
     * Construct a new entity action.
     *
     * @param type The action type
     * @param entity The entity
     */
    public BukkitEntityAction(ActionType type, Entity entity) {
        super(type);

        this.entityType = entity.getType();

        readWriteNbt = NBT.createNBTObject();

        var nbtService = PrismBukkit.instance().injectorProvider().injector().getInstance(NbtService.class);
        nbtService.processEntityNbt(entity, readWriteNbt::mergeCompound);

        this.descriptor = entityType.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
    }

    /**
     * Construct a new entity action with the type and nbt container.
     *
     * @param type The action type
     * @param entityType The entity type
     * @param readWriteNbt The read/write nbt
     * @param descriptor The descriptor
     * @param metadata The metadata
     */
    public BukkitEntityAction(
            ActionType type, EntityType entityType, ReadWriteNBT readWriteNbt, String descriptor, Record metadata) {
        super(type, descriptor, metadata);

        this.entityType = entityType;
        this.readWriteNbt = readWriteNbt;
    }

    @Override
    public Component descriptorComponent() {
        return Component.translatable(entityType.getTranslationKey());
    }

    /**
     * Get the entity type.
     *
     * @return Entity type
     */
    public EntityType entityType() {
        return entityType;
    }

    @Override
    public String serializeEntityType() {
        return entityType.toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public boolean hasCustomData() {
        return this.readWriteNbt != null;
    }

    @Override
    public @Nullable String serializeCustomData() {
        return readWriteNbt.toString();
    }

    @Override
    public ModificationResult applyRollback(
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        return modifyEntity(true, modificationRuleset, owner, activityContext, mode);
    }

    @Override
    public ModificationResult applyRestore(
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        return modifyEntity(false, modificationRuleset, owner, activityContext, mode);
    }

    protected ModificationResult modifyEntity(
            boolean isRollback,
            ModificationRuleset modificationRuleset,
            Object owner,
            Activity activityContext,
            ModificationQueueMode mode) {
        // Skip if entity is in the blacklist
        if (modificationRuleset.entityBlacklistContainsAny(entityType.toString())) {
            return ModificationResult.builder().activity(activityContext).build();
        }

        Coordinate coordinate = activityContext.coordinate();
        World world = Bukkit.getServer().getWorld(activityContext.worldUuid());
        if (world == null) {
            return ModificationResult.builder().activity(activityContext).build();
        }

        boolean shouldSpawn = (isRollback && type().resultType().equals(ActionResultType.REMOVES))
            || (!isRollback && type().resultType().equals(ActionResultType.CREATES));

        if (shouldSpawn) {
            if (entityType.getEntityClass() != null) {
                Location loc = new Location(world, coordinate.x(), coordinate.y(), coordinate.z());

                world.spawn(loc, entityType.getEntityClass(), entity -> {
                    NBT.modify(entity, nbt -> {
                        nbt.mergeCompound(readWriteNbt);
                    });
                });

                return ModificationResult.builder().activity(activityContext).applied().build();
            }
        } else {
            UUID uuid = readWriteNbt.getUUID("UUID");
            if (uuid != null) {
                for (var entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(uuid)) {
                        if (type().resultType().equals(ActionResultType.CREATES)) {
                            entity.remove();

                            return ModificationResult.builder().activity(activityContext).applied().build();
                        } else if (type().resultType().equals(ActionResultType.REPLACES)) {
                            NBT.modify(entity, nbt -> {
                                nbt.mergeCompound(readWriteNbt);
                            });

                            return ModificationResult.builder().activity(activityContext).applied().build();
                        }
                    }
                }
            }
        }

        return ModificationResult.builder().activity(activityContext).build();
    }

    @Override
    public String toString() {
        return String.format("EntityAction{type=%s}", type);
    }
}

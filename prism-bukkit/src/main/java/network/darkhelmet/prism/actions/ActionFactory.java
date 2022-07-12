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

import com.google.inject.Inject;

import java.util.Optional;

import network.darkhelmet.prism.actions.types.BlockActionType;
import network.darkhelmet.prism.actions.types.EntityActionType;
import network.darkhelmet.prism.actions.types.ItemActionType;
import network.darkhelmet.prism.api.actions.IActionFactory;
import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.IItemAction;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.actions.types.IActionTypeRegistry;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ActionFactory implements IActionFactory<BlockState, Entity, ItemStack> {
    /**
     * The action type registry.
     */
    private final IActionTypeRegistry actionTypeRegistry;

    /**
     * Constructor.
     *
     * @param actionTypeRegistry The action type registry
     */
    @Inject
    public ActionFactory(IActionTypeRegistry actionTypeRegistry) {
        this.actionTypeRegistry = actionTypeRegistry;
    }

    @Override
    public IBlockAction createBlockAction(IActionType type, BlockState blockState) {
        return createBlockAction(type, blockState, null);
    }

    @Override
    public IBlockAction createBlockAction(IActionType type, BlockState blockState, BlockState replaced) {
        if (!(type instanceof BlockActionType)) {
            throw new IllegalArgumentException(
                    "Block change actions cannot be made from non-block change action types.");
        }

        return new BlockStateAction(type, blockState, replaced);
    }

    @Override
    public IBlockAction createBlockAction(String key, BlockState blockState) {
        return createBlockAction(key, blockState, null);
    }

    @Override
    public IBlockAction createBlockAction(String key, BlockState blockState, BlockState replaced) {
        Optional<IActionType> actionTypeOptional = actionTypeRegistry.getActionType(key);
        if (actionTypeOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid action type key");
        }

        return createBlockAction(actionTypeOptional.get(), blockState, replaced);
    }

    @Override
    public IEntityAction createEntityAction(IActionType type, Entity entity) {
        if (!(type instanceof EntityActionType)) {
            throw new IllegalArgumentException("Entity actions cannot be made from non-entity action types.");
        }

        return new EntityAction(type, entity);
    }

    @Override
    public IEntityAction createEntityAction(String key, Entity entity) {
        Optional<IActionType> actionTypeOptional = actionTypeRegistry.getActionType(key);
        if (actionTypeOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid action type key");
        }

        return new EntityAction(actionTypeOptional.get(), entity);
    }

    @Override
    public IItemAction createItemStackAction(IActionType type, ItemStack itemStack) {
        if (!(type instanceof ItemActionType)) {
            throw new IllegalArgumentException("Item actions cannot be made from non-item action types.");
        }

        return new ItemStackAction(type, itemStack);
    }

    @Override
    public IItemAction createItemStackAction(String key, ItemStack itemStack) {
        Optional<IActionType> actionTypeOptional = actionTypeRegistry.getActionType(key);
        if (actionTypeOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid action type key");
        }

        return new ItemStackAction(actionTypeOptional.get(), itemStack);
    }
}

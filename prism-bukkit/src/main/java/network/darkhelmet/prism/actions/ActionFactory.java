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

package network.darkhelmet.prism.actions;

import com.google.inject.Singleton;

import network.darkhelmet.prism.actions.types.BlockActionType;
import network.darkhelmet.prism.actions.types.EntityActionType;
import network.darkhelmet.prism.actions.types.GenericActionType;
import network.darkhelmet.prism.actions.types.ItemActionType;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.IActionFactory;
import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.IEntityAction;
import network.darkhelmet.prism.api.actions.IItemAction;
import network.darkhelmet.prism.api.actions.types.IActionType;

import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

@Singleton
public class ActionFactory implements IActionFactory<BlockState, BlockData, Entity, ItemStack> {
    @Override
    public IAction createAction(IActionType type) {
        return createAction(type, null);
    }

    @Override
    public IAction createAction(IActionType type, String descriptor) {
        return createAction(type, descriptor, null);
    }

    @Override
    public IAction createAction(IActionType type, String descriptor, Record metadata) {
        if (!(type instanceof GenericActionType)) {
            throw new IllegalArgumentException("Generic actions cannot be made from non-generic action types.");
        }

        return new GenericAction(type, descriptor, metadata);
    }

    @Override
    public IBlockAction createBlockDataAction(IActionType type, BlockData blockData, BlockData replacedBlockData) {
        if (!(type instanceof BlockActionType)) {
            throw new IllegalArgumentException("Block actions cannot be made from non-block action types.");
        }

        return new BlockAction(type, blockData, replacedBlockData);
    }

    @Override
    public IBlockAction createBlockStateAction(IActionType type, BlockState blockState) {
        return createBlockStateAction(type, blockState, null);
    }

    @Override
    public IBlockAction createBlockStateAction(IActionType type, BlockState blockState, BlockState replaced) {
        if (!(type instanceof BlockActionType)) {
            throw new IllegalArgumentException("Block actions cannot be made from non-block action types.");
        }

        return new BlockAction(type, blockState, replaced);
    }

    @Override
    public IEntityAction createEntityAction(IActionType type, Entity entity) {
        if (!(type instanceof EntityActionType)) {
            throw new IllegalArgumentException("Entity actions cannot be made from non-entity action types.");
        }

        return new EntityAction(type, entity);
    }

    @Override
    public IItemAction createItemStackAction(IActionType type, ItemStack itemStack) {
        if (!(type instanceof ItemActionType)) {
            throw new IllegalArgumentException("Item actions cannot be made from non-item action types.");
        }

        return new ItemStackAction(type, itemStack);
    }
}

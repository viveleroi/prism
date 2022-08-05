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

package network.darkhelmet.prism.api.actions;

import network.darkhelmet.prism.api.actions.types.IActionType;

public interface IActionFactory<B, D, E, I> {
    /**
     * Create a new generic action.
     *
     * @param type The action type
     * @return The action
     */
    IAction createAction(IActionType type);

    /**
     * Create a new generic action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     * @return The action
     */
    IAction createAction(IActionType type, String descriptor);

    /**
     * Create a new generic action.
     *
     * @param key The action type key
     * @return The action
     */
    IAction createAction(String key);

    /**
     * Create a new generic action.
     *
     * @param key The action type key
     * @param descriptor The descriptor
     * @return The action
     */
    IAction createAction(String key, String descriptor);

    /**
     * Create a new action for the given type/block.
     *
     * @param type The action type
     * @param blockState The new block state
     * @return The block action
     */
    IBlockAction createBlockStateAction(IActionType type, B blockState);

    /**
     * Create a new action for the given type/block, including a replaced block state.
     *
     * @param type The action type
     * @param blockState The new block state
     * @param replacedBlockState The replaced block state
     * @return The block action
     */
    IBlockAction createBlockStateAction(IActionType type, B blockState, B replacedBlockState);

    /**
     * Create a new action for the given type/block.
     *
     * @param key The action type key
     * @param blockState The new block state
     * @return The block action
     */
    IBlockAction createBlockStateAction(String key, B blockState);

    /**
     * Create a new action for the given type/block, including a replaced block state.
     *
     * @param key The action type key
     * @param blockState The new block state
     * @param replaced The replaced block state
     * @return The block action
     */
    IBlockAction createBlockStateAction(String key, B blockState, B replaced);

    /**
     * Create a new action for the given type/block, including a replaced block data.
     *
     * @param type The action type
     * @param blockData The block data
     * @param replacedBlockData The replaced block data
     * @return The block action
     */
    IBlockAction createBlockDataAction(IActionType type, D blockData, D replacedBlockData);

    /**
     * Create a new action for the given entity.
     *
     * @param type The action type
     * @param entity The entity
     * @return The entity action
     */
    IEntityAction createEntityAction(IActionType type, E entity);

    /**
     * Create a new action for the given entity.
     *
     * @param key The action type key
     * @param entity The entity
     * @return The entity action
     */
    IEntityAction createEntityAction(String key, E entity);

    /**
     * Create a new action for the given type/item stack.
     *
     * @param type The action type
     * @param itemStack The item stack
     * @return The item stack action
     */
    IItemAction createItemStackAction(IActionType type, I itemStack);

    /**
     * Create a new action for the given type/item stack.
     *
     * @param key The action type key
     * @param itemStack The item stack
     * @return The item stack action
     */
    IItemAction createItemStackAction(String key, I itemStack);
}

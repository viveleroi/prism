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

package org.prism_mc.prism.paper.integrations.worldedit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class PrismLoggingExtent extends AbstractDelegateExtent {

    /**
     * The recorder.
     */
    private final WorldEditChangeRecorder recorder;

    /**
     * Constructor.
     *
     * @param extent The extent to wrap
     * @param recorder The change recorder
     */
    public PrismLoggingExtent(Extent extent, WorldEditChangeRecorder recorder) {
        super(extent);
        this.recorder = recorder;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
        // Get the old block state BEFORE making changes
        BlockState oldWEState = getBlock(position);

        // Perform the actual block change first
        boolean result = super.setBlock(position, block);

        // Only log if the change actually happened
        if (!result) {
            return false;
        }

        recorder.record(position.x(), position.y(), position.z(), oldWEState, block.toImmutableState());

        return true;
    }
}

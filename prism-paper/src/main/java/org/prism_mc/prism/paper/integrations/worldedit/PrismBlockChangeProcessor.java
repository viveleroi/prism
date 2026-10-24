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

import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;

public class PrismBlockChangeProcessor implements IBatchProcessor {

    /**
     * The recorder for this edit session.
     */
    private final WorldEditChangeRecorder recorder;

    /**
     * Constructor.
     *
     * @param recorder The change recorder
     */
    public PrismBlockChangeProcessor(WorldEditChangeRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
        if (!recorder.enabled()) {
            return set;
        }

        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;

        for (int layer = set.getMinSectionPosition(); layer <= set.getMaxSectionPosition(); layer++) {
            if (!set.hasSection(layer)) {
                continue;
            }

            char[] setBlocks = set.loadIfPresent(layer);
            if (setBlocks == null) {
                continue;
            }

            char[] getBlocks = get.load(layer);
            int baseY = layer << 4;

            for (int i = 0; i < setBlocks.length; i++) {
                char ordinalSet = setBlocks[i];
                if (ordinalSet == BlockTypesCache.ReservedIDs.__RESERVED__) {
                    // No change at this position
                    continue;
                }

                char ordinalGet = getBlocks[i];
                if (ordinalGet == BlockTypesCache.ReservedIDs.__RESERVED__) {
                    ordinalGet = BlockTypesCache.ReservedIDs.AIR;
                }

                if (ordinalGet == ordinalSet) {
                    continue;
                }

                BlockState oldState = BlockTypesCache.states[ordinalGet];
                BlockState newState = BlockTypesCache.states[ordinalSet];

                // Section arrays are indexed y << 8 | z << 4 | x
                recorder.record(chunkX + (i & 15), baseY + (i >> 8), chunkZ + ((i >> 4) & 15), oldState, newState);
            }
        }

        return set;
    }

    @Override
    public Extent construct(Extent child) {
        // Used when FAWE isn't placing chunks through its queue and falls back to per-block extents
        return new PrismLoggingExtent(child, recorder);
    }

    @Override
    public ProcessorScope getScope() {
        // Run after every processor that may add, change, or remove blocks so we log what's actually set
        return ProcessorScope.READING_BLOCKS;
    }
}

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

package org.prism_mc.prism.api.services.modifications;

import java.util.Arrays;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ModificationRuleset {
    /**
     * The block black list.
     */
    private List<String> blockBlacklist;

    /**
     * Whether to drain lava.
     */
    private boolean drainLava;

    /**
     * The entity black list.
     */
    private List<String> entityBlacklist;

    /**
     * Mask modifications per task.
     */
    private int maxPerTask;

    /**
     * Move entities.
     */
    private boolean moveEntities;

    /**
     * A list of blocks to remove.
     */
    private List<String> removeBlocks;

    /**
     * Whether to remove drops.
     */
    private boolean removeDrops;

    /**
     * Whether to force set blocks even if they seem to be already set.
     */
    private boolean overwrite;

    /**
     * The delay between tasks.
     */
    private long taskDelay;

    /**
     * Check strings against the block blacklist.
     *
     * @param values The block strings
     * @return True if a match found
     */
    public boolean blockBlacklistContainsAny(String... values) {
        return blockBlacklist.stream().anyMatch(str ->
            Arrays.stream(values).anyMatch(v -> v.equalsIgnoreCase(str)));
    }

    /**
     * Check strings against the entity blacklist.
     *
     * @param values The block strings
     * @return True if a match found
     */
    public boolean entityBlacklistContainsAny(String... values) {
        return entityBlacklist.stream().anyMatch(str ->
            Arrays.stream(values).anyMatch(v -> v.equalsIgnoreCase(str)));
    }
}

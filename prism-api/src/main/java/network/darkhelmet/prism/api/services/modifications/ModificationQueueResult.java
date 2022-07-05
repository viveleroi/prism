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

package network.darkhelmet.prism.api.services.modifications;

public final class ModificationQueueResult {
    /**
     * The modification queue state.
     */
    private final ModificationQueueState phase;

    /**
     * The count of skipped modifications.
     */
    private final int countSkipped;

    /**
     * The count of planned modifications.
     */
    private final int countPlanned;

    /**
     * The count of applied modifications.
     */
    private final int countApplied;

    /**
     * Constructor.
     *
     * @param phase The modification queue state
     * @param countSkipped The count of skipped modifications
     * @param countPlanned The count of planned modifications
     * @param countApplied The count of applied modifications
     */
    public ModificationQueueResult(
            ModificationQueueState phase,
            int countSkipped,
            int countPlanned,
            int countApplied) {
        this.phase = phase;
        this.countSkipped = countSkipped;
        this.countPlanned = countPlanned;
        this.countApplied = countApplied;
    }

    /**
     * Get the queue state.
     *
     * @return The queue state
     */
    public ModificationQueueState phase() {
        return phase;
    }

    /**
     * Get the count of skipped modifications.
     *
     * @return The count of skipped modifications
     */
    public int countSkipped() {
        return countSkipped;
    }

    /**
     * Get the count of planned modifications.
     *
     * @return The count of planned modifications
     */
    public int countPlanned() {
        return countPlanned;
    }

    /**
     * Get the count of applied modifications.
     *
     * @return The count of applied modifications
     */
    public int countApplied() {
        return countApplied;
    }
}

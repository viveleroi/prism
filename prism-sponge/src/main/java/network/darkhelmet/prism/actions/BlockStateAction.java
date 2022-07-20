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

import network.darkhelmet.prism.api.actions.IBlockAction;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;

import org.jetbrains.annotations.Nullable;

public class BlockStateAction implements IBlockAction {
    public BlockStateAction(IActionType type) {}

    @Override
    public @Nullable String serializeBlockData() {
        return null;
    }

    @Override
    public boolean hasCustomData() {
        return false;
    }

    @Override
    public @Nullable String serializeCustomData() {
        return null;
    }

    @Override
    public @Nullable String serializeReplacedMaterial() {
        return null;
    }

    @Override
    public @Nullable String serializeReplacedBlockData() {
        return null;
    }

    @Override
    public ModificationResult applyRollback(Object owner, IActivity activityContext, ModificationQueueMode mode) {
        return ModificationResult.builder().activity(activityContext).build();
    }

    @Override
    public ModificationResult applyRestore(Object owner, IActivity activityContext, ModificationQueueMode mode) {
        return ModificationResult.builder().activity(activityContext).build();
    }

    @Override
    public String descriptor() {
        return null;
    }

    @Override
    public IActionType type() {
        return null;
    }

    @Override
    public String serializeMaterial() {
        return null;
    }
}

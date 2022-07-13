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

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationResultStatus;

public class GenericAction extends Action implements IAction {
    /**
     * Construct a new generic action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     */
    public GenericAction(IActionType type, String descriptor) {
        super(type);

        this.descriptor = descriptor;
    }

    @Override
    public ModificationResult applyRollback(Object owner, IActivity activityContext, boolean isPreview) {
        return new ModificationResult(ModificationResultStatus.SKIPPED, null);
    }

    @Override
    public ModificationResult applyRestore(Object owner, IActivity activityContext, boolean isPreview) {
        return new ModificationResult(ModificationResultStatus.SKIPPED, null);
    }
}

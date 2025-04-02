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

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;

public class GenericAction extends Action implements IAction {
    /**
     * Construct a new generic action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     */
    public GenericAction(IActionType type, String descriptor) {
        super(type, descriptor);
    }

    /**
     * Construct a new generic action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     * @param metadata The metadata
     */
    public GenericAction(IActionType type, String descriptor, Record metadata) {
        super(type, descriptor, metadata);
    }

    /**
     * Construct a new generic action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     * @param metadata The metadata
     */
    public GenericAction(IActionType type, String descriptor, String metadata) throws Exception {
        super(type, descriptor);

        this.metadata = objectMapper.readValue(metadata, type.metadataClass());
    }

    @Override
    public ModificationResult applyRollback(
            ModificationRuleset modificationRuleset,
            Object owner,
            IActivity activityContext,
            ModificationQueueMode mode) {
        return ModificationResult.builder().activity(activityContext).build();
    }

    @Override
    public ModificationResult applyRestore(
            ModificationRuleset modificationRuleset,
            Object owner,
            IActivity activityContext,
            ModificationQueueMode mode) {
        return ModificationResult.builder().activity(activityContext).build();
    }
}

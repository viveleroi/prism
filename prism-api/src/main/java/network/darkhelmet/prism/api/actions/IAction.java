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

package network.darkhelmet.prism.api.actions;

import net.kyori.adventure.text.Component;

import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueMode;
import network.darkhelmet.prism.api.services.modifications.ModificationResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.api.services.translation.ITranslationService;

import org.jetbrains.annotations.Nullable;

public interface IAction {
    /**
     * Apply the rollback. If the action type is not reversible, this does nothing.
     *
     * @param modificationRuleset The modification ruleset
     * @param owner The owner of this modification
     * @param activityContext The activity as a context
     * @param mode Modification mode
     */
    ModificationResult applyRollback(
        ModificationRuleset modificationRuleset,
        Object owner,
        IActivity activityContext,
        ModificationQueueMode mode);

    /**
     * Apply the restore. If the action type is not reversible, this does nothing.
     *
     * @param modificationRuleset The modification ruleset
     * @param owner The owner of this modification
     * @param activityContext The activity as a context
     * @param mode Modification mode
     */
    ModificationResult applyRestore(
        ModificationRuleset modificationRuleset,
        Object owner,
        IActivity activityContext,
        ModificationQueueMode mode);

    /**
     * Get the descriptor.
     *
     * <p>A descriptor is a mix between an identifier and formatted/specific content.</p>
     *
     * <p>For example we log the entity type of "BOAT" and custom nbt serialized data
     * but custom data is too unique to group, yet pieces are important to users.
     * A descriptor parses the key data for a *descriptive*, *group-able* name.</p>
     *
     * <p>Like "mangrove boat". The descriptor is stored in the DB so it a) works
     * in grouping queries and b) is available to external sources like a web ui.</p>
     *
     * @return The descriptor
     */
    String descriptor();

    /**
     * Get the metadata record attached to this action.
     *
     * @return The metadata
     */
    @Nullable Record metadata();

    /**
     * Get the metadata as a component.
     *
     * <p>Metadata components are used as hover effects for descriptors in chat results.</p>
     *
     * @param receiver The receiver
     * @param translationService The translation service
     * @return The metadata component.
     */
    Component metadataComponent(Object receiver, ITranslationService translationService);

    /**
     * Serialize the metdata.
     *
     * @return The serialized metdata
     * @throws Exception Serialization exception
     */
    String serializeMetadata() throws Exception;

    /**
     * Get the action type.
     *
     * @return The action type
     */
    IActionType type();
}

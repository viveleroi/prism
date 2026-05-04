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

package org.prism_mc.prism.paper.services.query.parsers.parameters;

import java.util.Collection;
import org.prism_mc.prism.api.actions.types.ActionType;
import org.prism_mc.prism.loader.services.configuration.DefaultsConfiguration;
import org.prism_mc.prism.paper.actions.types.PaperActionTypeRegistry;
import org.prism_mc.prism.paper.api.activities.PaperActivityQuery;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.query.annotations.ConflictsWith;

@ConflictsWith(value = { ActionParameterParser.class })
public class ExcludeActionParameterParser extends ActionParameterParser {

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param defaultsConfiguration The defaults configuration
     * @param actionRegistry The action registry
     */
    public ExcludeActionParameterParser(
        MessageService messageService,
        DefaultsConfiguration defaultsConfiguration,
        PaperActionTypeRegistry actionRegistry
    ) {
        super(messageService, defaultsConfiguration, "a!", actionRegistry);
    }

    @Override
    protected void applyKey(PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder, String actionKey) {
        builder.actionTypeKeyExcluded(actionKey);
    }

    @Override
    protected void applyFamily(
        PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder,
        Collection<? extends ActionType> familyTypes
    ) {
        for (var actionType : familyTypes) {
            builder.actionTypeKeyExcluded(actionType.key());
        }
    }
}

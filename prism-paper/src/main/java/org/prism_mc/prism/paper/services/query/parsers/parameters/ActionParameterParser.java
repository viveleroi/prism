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

import dev.triumphteam.cmd.core.argument.keyed.Arguments;
import java.util.Collection;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.prism_mc.prism.api.actions.types.ActionType;
import org.prism_mc.prism.loader.services.configuration.DefaultsConfiguration;
import org.prism_mc.prism.paper.actions.types.PaperActionTypeRegistry;
import org.prism_mc.prism.paper.api.activities.PaperActivityQuery;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.query.ParameterContext;
import org.prism_mc.prism.paper.services.query.annotations.ConflictsWith;
import org.prism_mc.prism.paper.services.query.parsers.multiple.StringSetQueryArgumentParser;

@ConflictsWith(value = { ExcludeActionParameterParser.class })
public class ActionParameterParser extends StringSetQueryArgumentParser {

    /**
     * The action registry.
     */
    protected final PaperActionTypeRegistry actionRegistry;

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param defaultsConfiguration The defaults configuration
     * @param actionRegistry The action registry
     */
    public ActionParameterParser(
        MessageService messageService,
        DefaultsConfiguration defaultsConfiguration,
        PaperActionTypeRegistry actionRegistry
    ) {
        this(messageService, defaultsConfiguration, "a", actionRegistry);
    }

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param defaultsConfiguration The defaults configuration
     * @param alias The parameter alias
     * @param actionRegistry The action registry
     */
    protected ActionParameterParser(
        MessageService messageService,
        DefaultsConfiguration defaultsConfiguration,
        String alias,
        PaperActionTypeRegistry actionRegistry
    ) {
        super(messageService, defaultsConfiguration, alias);
        this.actionRegistry = actionRegistry;
    }

    @Override
    public boolean parse(
        CommandSender sender,
        ParameterContext parameterContext,
        Arguments arguments,
        PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder
    ) {
        var values = parseMultipleParameters(arguments, builder);

        if (!values.isEmpty()) {
            if (alertConflicts(sender, arguments)) {
                return false;
            }

            for (String actionKey : values) {
                if (actionKey.contains("-")) {
                    var optionalIActionType = actionRegistry.actionType(actionKey.toLowerCase(Locale.ENGLISH));
                    if (optionalIActionType.isPresent()) {
                        applyKey(builder, actionKey);
                    } else {
                        messageService.errorParamInvalidAction(sender, actionKey);

                        return false;
                    }
                } else {
                    var familyTypes = actionRegistry.actionTypesInFamily(actionKey.toLowerCase(Locale.ENGLISH));
                    if (familyTypes.isEmpty()) {
                        messageService.errorParamInvalidAction(sender, actionKey);

                        return false;
                    }

                    applyFamily(builder, familyTypes);
                }
            }
        }

        return true;
    }

    protected void applyKey(PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder, String actionKey) {
        builder.actionTypeKey(actionKey);
    }

    protected void applyFamily(
        PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder,
        Collection<? extends ActionType> familyTypes
    ) {
        builder.actionTypes(familyTypes);
    }
}

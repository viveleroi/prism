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
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.prism_mc.prism.loader.services.configuration.DefaultsConfiguration;
import org.prism_mc.prism.paper.api.activities.PaperActivityQuery;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.query.ParameterContext;
import org.prism_mc.prism.paper.services.query.parsers.single.StringQueryArgumentParser;
import org.prism_mc.prism.paper.utils.DateUtils;

public class SinceParameterParser extends StringQueryArgumentParser {

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param defaultsConfiguration The defaults configuration
     */
    public SinceParameterParser(MessageService messageService, DefaultsConfiguration defaultsConfiguration) {
        super(messageService, defaultsConfiguration, "since");
    }

    @Override
    public boolean parse(
        CommandSender sender,
        ParameterContext parameterContext,
        Arguments arguments,
        PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder
    ) {
        var optionalParameter = parseSingleParameter(arguments, builder);

        if (optionalParameter.isPresent()) {
            Long parsedTimestamp = DateUtils.parseTimestamp(optionalParameter.get());
            if (parsedTimestamp != null) {
                builder.after(parsedTimestamp);
            } else {
                messageService.errorParamInvalidTime(sender);

                return false;
            }
        }

        return true;
    }

    /**
     * Resolve the lower time bound (the {@code after} timestamp) this query
     * would use — the explicit {@code since:} value, or the configured default
     * it falls back to — without mutating the builder. Empty means the query
     * has no lower bound and would reach arbitrarily far into the past. Used by
     * the look-back limit to floor that bound.
     *
     * @param arguments The arguments
     * @return The effective lower-bound timestamp, or empty if unbounded
     */
    public Optional<Long> effectiveLowerBound(Arguments arguments) {
        return parseSingleParameter(arguments, null).map(DateUtils::parseTimestamp);
    }
}

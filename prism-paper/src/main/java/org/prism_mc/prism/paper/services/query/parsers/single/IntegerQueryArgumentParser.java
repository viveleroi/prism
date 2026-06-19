/*
 * prism
 *
 * Copyright (c) 2022 M Botsko (viveleroi)
 * Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY and FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.prism_mc.prism.paper.services.query.parsers.single;

import dev.triumphteam.cmd.core.argument.keyed.Arguments;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.prism_mc.prism.loader.services.configuration.DefaultsConfiguration;
import org.prism_mc.prism.paper.api.activities.PaperActivityQuery;
import org.prism_mc.prism.paper.services.limits.EffectiveLimits;
import org.prism_mc.prism.paper.services.messages.MessageService;
import org.prism_mc.prism.paper.services.query.parsers.QueryArgumentSingleValueParser;

public abstract class IntegerQueryArgumentParser extends QueryArgumentSingleValueParser<Integer> {

    /**
     * Constructor.
     *
     * @param messageService The message service
     * @param defaultsConfiguration The defaults configuration
     * @param parameter The parameter
     */
    public IntegerQueryArgumentParser(
        MessageService messageService,
        DefaultsConfiguration defaultsConfiguration,
        String parameter
    ) {
        super(messageService, defaultsConfiguration, parameter, Integer.class);
    }

    @Override
    protected Optional<Integer> parseSingleParameter(
        Arguments arguments,
        PaperActivityQuery.PaperActivityQueryBuilder<?, ?> builder
    ) {
        return parseSingleParameter(arguments, builder, (defaultValue, paramName) -> Integer.parseInt(defaultValue));
    }

    @Override
    public boolean checkLimit(CommandSender sender, Arguments arguments, EffectiveLimits limits) {
        // Honor any allowed-value whitelist configured for this numeric parameter.
        if (!super.checkLimit(sender, arguments, limits)) {
            return false;
        }

        // Validate the effective value — the explicit argument, or the configured
        // default the query would otherwise fall back to — so a default larger
        // than the cap cannot slip through when the parameter is omitted.
        var value = parseSingleParameter(arguments, null);
        if (value.isEmpty()) {
            return true;
        }

        int actual = value.get();

        var max = limits.max(parameter);
        if (max.isPresent() && actual > max.get()) {
            messageService.errorLimitMax(sender, parameter, max.get());

            return false;
        }

        var min = limits.min(parameter);
        if (min.isPresent() && actual < min.get()) {
            messageService.errorLimitMin(sender, parameter, min.get());

            return false;
        }

        return true;
    }
}

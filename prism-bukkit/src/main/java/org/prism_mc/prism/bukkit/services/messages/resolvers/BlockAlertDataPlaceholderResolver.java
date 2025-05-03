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

package org.prism_mc.prism.bukkit.services.messages.resolvers;

import com.google.inject.Singleton;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.moonshine.placeholder.ConclusionValue;
import net.kyori.moonshine.placeholder.ContinuanceValue;
import net.kyori.moonshine.placeholder.IPlaceholderResolver;
import net.kyori.moonshine.util.Either;

import org.prism_mc.prism.bukkit.services.alerts.BlockAlertData;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class BlockAlertDataPlaceholderResolver implements
        IPlaceholderResolver<CommandSender, BlockAlertData, Component> {
    @Override
    public @NonNull Map<String, Either<ConclusionValue<? extends Component>, ContinuanceValue<?>>> resolve(
            final String placeholderName,
            final BlockAlertData value,
            final CommandSender receiver,
            final Type owner,
            final Method method,
            final @Nullable Object[] parameters
    ) {
        Component color = Component.text().color(value.color()).build();
        Component blockName = block(value.blockTranslationKey(), value.itemKey());
        Component playerName = Component.text(value.playerName());

        return Map.of(placeholderName + "_color", Either.left(ConclusionValue.conclusionValue(color)),
            placeholderName + "_player", Either.left(ConclusionValue.conclusionValue(playerName)),
            placeholderName + "_block", Either.left(ConclusionValue.conclusionValue(blockName)));
    }

    /**
     * Build the block component.
     *
     * @param blockTranslationKey The block translation key
     * @param itemKey The item key
     * @return The component
     */
    protected Component block(String blockTranslationKey, Key itemKey) {
        return Component.text()
            .append(Component.translatable(blockTranslationKey))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_ITEM,
                HoverEvent.ShowItem.showItem(itemKey, 1)))
            .build();
    }
}
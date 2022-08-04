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

package network.darkhelmet.prism.services.messages.resolvers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.moonshine.placeholder.ConclusionValue;
import net.kyori.moonshine.placeholder.ContinuanceValue;
import net.kyori.moonshine.placeholder.IPlaceholderResolver;
import net.kyori.moonshine.util.Either;

import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.activities.IGroupedActivity;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.services.translation.TranslationService;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class ActivityPlaceholderResolver implements IPlaceholderResolver<CommandSender, IActivity, Component> {
    /**
     * The translation service.
     */
    private final TranslationService translationService;

    /**
     * Construct an activity placeholder resolver.
     *
     * @param translationService The translation service
     */
    @Inject
    public ActivityPlaceholderResolver(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public @NonNull Map<String, Either<ConclusionValue<? extends Component>, ContinuanceValue<?>>> resolve(
        final String placeholderName,
        final IActivity value,
        final CommandSender receiver,
        final Type owner,
        final Method method,
        final @Nullable Object[] parameters
    ) {
        String pastTenseTranslationKey = value.action().type().pastTenseTranslationKey();
        String pastTense = translationService.messageOf(receiver, pastTenseTranslationKey);

        Component actionPastTense = Component.text(pastTense);
        Component actionFamily = actionFamily(value.action().type().key());
        Component cause = cause(receiver, value.cause(), value.player());
        Component since = since(receiver, value.timestamp());

        Component content = Component.empty();
        if (value.action().descriptor() != null) {
            content = Component.text(value.action().descriptor());
        }

        Component count = Component.text("1");
        if (value instanceof IGroupedActivity grouped) {
            count = Component.text(grouped.count());
        }

        Component sign;
        if (value.action().type().resultType().equals(ActionResultType.REMOVES)) {
            sign = MiniMessage.miniMessage().deserialize(translationService.messageOf(receiver, "sign-minus"));
        } else {
            sign = MiniMessage.miniMessage().deserialize(translationService.messageOf(receiver, "sign-plus"));
        }

        return Map.of(placeholderName + "_action_past_tense",
                Either.left(ConclusionValue.conclusionValue(actionPastTense)),
                placeholderName + "_action_family", Either.left(ConclusionValue.conclusionValue(actionFamily)),
                placeholderName + "_cause", Either.left(ConclusionValue.conclusionValue(cause)),
                placeholderName + "_count", Either.left(ConclusionValue.conclusionValue(count)),
                placeholderName + "_sign", Either.left(ConclusionValue.conclusionValue(sign)),
                placeholderName + "_since", Either.left(ConclusionValue.conclusionValue(since)),
                placeholderName + "_content", Either.left(ConclusionValue.conclusionValue(content)));
    }

    /**
     * Get the action family. "break" for "block-break"
     *
     * @param typeKey The action type key
     * @return The action family
     */
    protected Component actionFamily(String typeKey) {
        String[] segments = typeKey.split("-");

        return Component.text(segments[segments.length - 1]);
    }

    /**
     * Convert the cause into a text string.
     *
     * @param cause The cause
     * @return The cause name/string
     */
    protected Component cause(CommandSender receiver, String cause, NamedIdentity player) {
        if (player != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.uuid());

            Component hover = Component.text()
                .append(Component.text("Player\n", NamedTextColor.DARK_PURPLE))
                .append(Component.text("UUID: ", NamedTextColor.GRAY))
                .append(Component.text(player.uuid().toString(), NamedTextColor.WHITE))
                .append(Component.text("\n"))
                .append(Component.text("Online? ", NamedTextColor.GRAY))
                .append(Component.text(offlinePlayer.isOnline() ? "Yes" : "No", NamedTextColor.WHITE))
                .append(Component.text("\n"))
                .append(Component.text("Banned? ", NamedTextColor.GRAY))
                .append(Component.text(offlinePlayer.isBanned() ? "Yes" : "No", NamedTextColor.WHITE))

                .build();

            return Component.text()
                .append(Component.text(player.name()))
                .hoverEvent(HoverEvent.showText(hover))
                .build();
        }

        if (cause != null) {
            Component hover = Component.text()
                .append(Component.text("Non-player", NamedTextColor.GRAY)).build();

            return Component.text().append(Component.text(cause)).hoverEvent(HoverEvent.showText(hover)).build();
        } else {
            return Component.text(translationService.messageOf(receiver, "unknown-cause"));
        }
    }

    /**
     * Get the shorthand syntax for time since.
     *
     * @param timestamp The timestamp
     * @return The time since
     */
    protected Component since(CommandSender receiver, long timestamp) {
        long diffInSeconds = System.currentTimeMillis() / 1000 - timestamp;

        if (diffInSeconds < 60) {
            return Component.text(translationService.messageOf(receiver, "just-now"));
        }

        long period = 24 * 60 * 60;

        final long[] diff = {
            diffInSeconds / period,
            (diffInSeconds / (period /= 24)) % 24,
            (diffInSeconds / (period / 60)) % 60
        };

        StringBuilder timeAgo = new StringBuilder();

        if (diff[0] > 0) {
            timeAgo.append(diff[0]).append('d');
        }

        if (diff[1] > 0) {
            timeAgo.append(diff[1]).append('h');
        }

        if (diff[2] > 0) {
            timeAgo.append(diff[2]).append('m');
        }

        // 'time_ago' will have something at this point
        String ago = translationService.messageOf(receiver, "ago");
        return Component.text(timeAgo.append(" ").append(ago).toString());
    }
}
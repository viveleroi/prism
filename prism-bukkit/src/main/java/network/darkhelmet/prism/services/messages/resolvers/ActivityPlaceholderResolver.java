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

package network.darkhelmet.prism.services.messages.resolvers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.moonshine.placeholder.ConclusionValue;
import net.kyori.moonshine.placeholder.ContinuanceValue;
import net.kyori.moonshine.placeholder.IPlaceholderResolver;
import net.kyori.moonshine.util.Either;

import network.darkhelmet.prism.api.actions.types.ActionResultType;
import network.darkhelmet.prism.api.actions.types.IActionType;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.activities.IGroupedActivity;
import network.darkhelmet.prism.api.util.NamedIdentity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.services.translation.TranslationService;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        Component actionPastTense = actionPastTense(receiver, value.action().type());
        Component cause = cause(receiver, value.cause(), value.player());
        Component since = since(receiver, value.timestamp());
        Component descriptor = descriptor(receiver, value);
        Component location = location(receiver, value.location());

        Component count = Component.text("1");
        if (value instanceof IGroupedActivity grouped) {
            count = Component.text(grouped.count());
        }

        Component sign;
        if (value.action().type().resultType().equals(ActionResultType.REMOVES)) {
            sign = MiniMessage.miniMessage().deserialize(
                translationService.messageOf(receiver, "text.sign-minus"));
        } else {
            sign = MiniMessage.miniMessage().deserialize(
                translationService.messageOf(receiver, "text.sign-plus"));
        }

        return Map.of(placeholderName + "_action_past_tense",
            Either.left(ConclusionValue.conclusionValue(actionPastTense)),
            placeholderName + "_cause", Either.left(ConclusionValue.conclusionValue(cause)),
            placeholderName + "_count", Either.left(ConclusionValue.conclusionValue(count)),
            placeholderName + "_sign", Either.left(ConclusionValue.conclusionValue(sign)),
            placeholderName + "_location", Either.left(ConclusionValue.conclusionValue(location)),
            placeholderName + "_since", Either.left(ConclusionValue.conclusionValue(since)),
            placeholderName + "_descriptor", Either.left(ConclusionValue.conclusionValue(descriptor)));
    }

    /**
     * Build the action past tense component.
     *
     * @param receiver The receiver
     * @param actionType The action type
     * @return The component
     */
    protected Component actionPastTense(CommandSender receiver, IActionType actionType) {
        String pastTenseTranslationKey = actionType.pastTenseTranslationKey();
        String pastTense = translationService.messageOf(receiver, pastTenseTranslationKey);

        Component actionHover = Component.text()
            .append(Component.text("a:", NamedTextColor.GRAY))
            .append(Component.text(actionType.key(), TextColor.fromCSSHexString("#ffd782")))
            .append(Component.text(" or ", NamedTextColor.WHITE))
            .append(Component.text("a:", NamedTextColor.GRAY))
            .append(Component.text(actionType.familyKey(), TextColor.fromCSSHexString("#ffd782")))
            .build();

        return Component.text()
            .append(Component.text(pastTense))
            .hoverEvent(HoverEvent.showText(actionHover))
            .build();
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

            Component playerHeading = MiniMessage.miniMessage().deserialize(
                translationService.messageOf(receiver, "rich.player-hover-header"));

            Component uuid = MiniMessage.miniMessage().deserialize(
                translationService.messageOf(receiver, "rich.player-hover-uuid"));

            Component online = MiniMessage.miniMessage().deserialize(
                translationService.messageOf(receiver, "rich.player-hover-online"));

            Component banned = MiniMessage.miniMessage().deserialize(
                translationService.messageOf(receiver, "rich.player-hover-banned"));

            String yes = translationService.messageOf(receiver, "text.player-hover-yes");
            String no = translationService.messageOf(receiver, "text.player-hover-no");

            Component hover = Component.text()
                .append(playerHeading)
                .append(Component.text("\n"))
                .append(uuid)
                .append(Component.text(" "))
                .append(Component.text(player.uuid().toString(), NamedTextColor.WHITE))
                .append(Component.text("\n"))
                .append(online)
                .append(Component.text(" "))
                .append(Component.text(offlinePlayer.isOnline() ? yes : no, NamedTextColor.WHITE))
                .append(Component.text("\n"))
                .append(banned)
                .append(Component.text(" "))
                .append(Component.text(offlinePlayer.isBanned() ? yes : no, NamedTextColor.WHITE))
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
            return Component.text(translationService.messageOf(receiver, "text.unknown-cause"));
        }
    }

    /**
     * Get the descriptor.
     *
     * @param receiver The receiver
     * @param value The activity
     * @return The descriptor component
     */
    protected Component descriptor(CommandSender receiver, IActivity value) {
        Component descriptor = Component.empty();
        if (value.action().descriptor() != null) {
            TextComponent.Builder builder = Component.text().append(Component.text(value.action().descriptor()));

            if (value.action().metadata() != null) {
                builder.hoverEvent(HoverEvent.showText(value.action().metadataComponent(receiver, translationService)));
            }

            descriptor = builder.build();
        }

        return descriptor;
    }

    /**
     * Get the location.
     *
     * @param receiver The receiver
     * @param worldCoordinate The world coordinate
     * @return The location
     */
    protected Component location(CommandSender receiver, WorldCoordinate worldCoordinate) {
        Component hover = Component.text(translationService.messageOf(receiver, "text.click-to-teleport"));

        return Component.text()
            .append(Component.text((int) worldCoordinate.x()))
            .append(Component.text(" "))
            .append(Component.text((int) worldCoordinate.y()))
            .append(Component.text(" "))
            .append(Component.text((int) worldCoordinate.z()))
            .hoverEvent(HoverEvent.showText(hover))
            .clickEvent(ClickEvent.callback((audience) -> {
                if (receiver instanceof Player player) {
                    World world = Bukkit.getServer().getWorld(worldCoordinate.world().uuid());
                    Location location = new Location(world,
                        worldCoordinate.x(), worldCoordinate.y(), worldCoordinate.z());
                    player.teleport(location);
                }
            }))
            .build();
    }

    /**
     * Get the shorthand syntax for time since.
     *
     * @param receiver The receiver
     * @param timestamp The timestamp
     * @return The time since
     */
    protected Component since(CommandSender receiver, long timestamp) {
        long diffInSeconds = System.currentTimeMillis() / 1000 - timestamp;

        if (diffInSeconds < 60) {
            return Component.text(translationService.messageOf(receiver, "text.just-now"));
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
        String ago = translationService.messageOf(receiver, "text.ago");
        return Component.text(timeAgo.append(" ").append(ago).toString());
    }
}
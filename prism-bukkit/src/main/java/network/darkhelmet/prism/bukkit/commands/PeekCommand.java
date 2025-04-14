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

package network.darkhelmet.prism.bukkit.commands;

import com.google.inject.Inject;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;

import net.kyori.adventure.text.minimessage.MiniMessage;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.bukkit.PrismBukkit;
import network.darkhelmet.prism.bukkit.actions.BukkitItemStackAction;
import network.darkhelmet.prism.bukkit.services.lookup.LookupService;
import network.darkhelmet.prism.bukkit.services.messages.MessageService;
import network.darkhelmet.prism.bukkit.services.translation.BukkitTranslationService;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Command(value = "prism", alias = {"pr"})
public class PeekCommand {
    /**
     * The lookup service.
     */
    private final LookupService lookupService;

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The translation service.
     */
    private final BukkitTranslationService translationService;

    /**
     * Construct the near command.
     *
     * @param lookupService The lookup service
     * @param messageService The message service
     * @param translationService The translation service
     */
    @Inject
    public PeekCommand(
            LookupService lookupService,
            MessageService messageService,
            BukkitTranslationService translationService) {
        this.messageService = messageService;
        this.lookupService = lookupService;
        this.translationService = translationService;
    }

    /**
     * Peek at inventory-related data of an activity.
     *
     * @param player The player
     * @param activityId The activity id
     */
    @Command("peek")
    @Permission("prism.lookup")
    public void onPeek(final Player player, Integer activityId) {
        final ActivityQuery query = ActivityQuery.builder()
            .activityId(activityId).lookup(false).grouped(false).limit(1).build();
        lookupService.lookup(player, query, results -> {
            if (results.isEmpty()) {
                messageService.noResults(player);
            } else {
                var activity = results.getFirst();

                var title = MiniMessage.miniMessage().deserialize(
                    translationService.messageOf(player, "rich.peek-gui-title"));

                if (activity.action() instanceof BukkitItemStackAction itemStackAction) {
                    Bukkit.getServer().getScheduler().runTask(PrismBukkit.instance().loaderPlugin(), () -> {
                        Gui gui = Gui.gui().title(title).disableAllInteractions().create();
                        gui.setItem(4, ItemBuilder.from(itemStackAction.itemStack()).asGuiItem());
                        gui.open(player);
                    });
                } else {
                    messageService.errorPeekNoItem(player);
                }
            }
        });
    }
}
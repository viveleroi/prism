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

package network.darkhelmet.prism.services.messages;

import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;

import network.darkhelmet.prism.api.PaginatedResults;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.services.translation.TranslationKey;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface MessageService {
    @Message("about")
    void about(CommandSender receiver, @Placeholder String version);

    @Message("error")
    void error(CommandSender receiver, @Placeholder TranslationKey message);

    @Message("activity-row")
    void listActivityRow(CommandSender receiver, @Placeholder IActivity activity);

    @Message("modifications-applied")
    void modificationsApplied(CommandSender receiver, @Placeholder Integer count);

    @Message("modifications-planned")
    void modificationsPlanned(CommandSender receiver, @Placeholder Integer count);

    @Message("modifications-removed-blocks")
    void modificationsRemovedBlocks(CommandSender receiver, @Placeholder Integer count);

    @Message("modifications-removed-drops")
    void modificationsRemovedDrops(CommandSender receiver, @Placeholder Integer count);

    @Message("modifications-skipped")
    void modificationsSkipped(CommandSender receiver, @Placeholder Integer count);

    @Message("modifications-success")
    void modificationsSuccess(CommandSender receiver);

    @Message("no-results")
    void noResults(CommandSender receiver);

    @Message("pagination-header")
    void paginationHeader(CommandSender receiver, @Placeholder PaginatedResults<?> pagination);

    @Message("preview-applied")
    void previewApplied(CommandSender receiver);

    @Message("preview-cancelled")
    void previewCancelled(CommandSender receiver);

    @Message("reloaded-config")
    void reloadedConfig(CommandSender receiver);

    @Message("reloaded-locales")
    void reloadedLocales(CommandSender receiver);

    @Message("removed-blocks")
    void removedBlocks(CommandSender receiver, @Placeholder Integer count);

    @Message("removed-drops")
    void removedDrops(CommandSender receiver, @Placeholder Integer count);

    @Message("wand-activated")
    void wandActivated(Player player, @Placeholder WandMode wandmode);

    @Message("wand-switched")
    void wandSwitched(Player player, @Placeholder WandMode wandmode);

    @Message("wand-deactivated")
    void wandDeactivated(Player player, @Placeholder WandMode wandmode);
}

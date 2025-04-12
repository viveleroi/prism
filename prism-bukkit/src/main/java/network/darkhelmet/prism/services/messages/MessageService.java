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

package network.darkhelmet.prism.services.messages;

import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;

import network.darkhelmet.prism.api.PaginatedResults;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.purges.PurgeCycleResult;
import network.darkhelmet.prism.api.services.wands.WandMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface MessageService {
    @Message("rich.about")
    void about(CommandSender receiver, @Placeholder String version);

    @Message("rich.defaults-used")
    void defaultsUsed(CommandSender receiver, @Placeholder String defaults);

    @Message("rich.error.console-only")
    void errorConsoleOnly(CommandSender receiver);

    @Message("rich.error.insufficient-permission")
    void errorInsufficientPermission(CommandSender receiver);

    @Message("rich.error.invalid-page")
    void errorInvalidPage(CommandSender receiver);

    @Message("rich.error.invalid-parameter")
    void errorInvalidParameter(CommandSender receiver);

    @Message("rich.error.no-blocks-removed")
    void errorNoBlocksRemoved(CommandSender receiver);

    @Message("rich.error.no-last-query")
    void errorNoLastQuery(CommandSender receiver);

    @Message("rich.error.not-previewable")
    void errorNotPreviewable(CommandSender receiver);

    @Message("rich.error.param-at-invalid-loc")
    void errorParamAtInvalidLocation(CommandSender receiver);

    @Message("rich.error.param-at-no-world")
    void errorParamAtNoWorld(CommandSender receiver);

    @Message("rich.error.param-bounds-invalid-format")
    void errorParamBoundsInvalid(CommandSender receiver);

    @Message("rich.error.param-console-bounds")
    void errorParamConsoleBounds(CommandSender receiver);

    @Message("rich.error.param-console-in")
    void errorParamConsoleIn(CommandSender receiver);

    @Message("rich.error.param-console-radius")
    void errorParamConsoleRadius(CommandSender receiver);

    @Message("rich.error.param-invalid-block-tag")
    void errorParamInvalidBlockTag(CommandSender receiver);

    @Message("rich.error.param-invalid-entity-type-tag")
    void errorParamInvalidEntityTypeTag(CommandSender receiver);

    @Message("rich.error.param-invalid-item-tag")
    void errorParamInvalidItemTag(CommandSender receiver);

    @Message("rich.error.param-invalid-namespace")
    void errorParamInvalidNamespace(CommandSender receiver);

    @Message("rich.error.param-invalid-world")
    void errorParamInvalidWorld(CommandSender receiver);

    @Message("rich.error.param-r-and-in-chunk")
    void errorParamRadiusAndChunk(CommandSender receiver);

    @Message("rich.error.player-only")
    void errorPlayerOnly(CommandSender receiver);

    @Message("rich.error.purge-queue-not-free")
    void errorPurgeQueryNotFree(CommandSender receiver);

    @Message("rich.error.queue-missing")
    void errorQueueMissing(CommandSender receiver);

    @Message("rich.error.queue-not-free")
    void errorQueueNotFree(CommandSender receiver);

    @Message("rich.error.query-exec")
    void errorQueryExec(CommandSender receiver);

    @Message("rich.error.reload-locale")
    void errorReloadLocale(CommandSender receiver);

    @Message("rich.error.unknown-command")
    void errorUnknownCommand(CommandSender receiver);

    @Message("rich.activity-row-grouped")
    void listActivityRowGrouped(CommandSender receiver, @Placeholder IActivity activity);

    @Message("rich.activity-row-grouped-no-descriptor")
    void listActivityRowGroupedNoDescriptor(CommandSender receiver, @Placeholder IActivity activity);

    @Message("rich.activity-row-single")
    void listActivityRowSingle(CommandSender receiver, @Placeholder IActivity activity);

    @Message("rich.activity-row-single-no-descriptor")
    void listActivityRowSingleNoDescriptor(CommandSender receiver, @Placeholder IActivity activity);

    @Message("rich.modifications-applied")
    void modificationsApplied(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.modifications-removed-blocks")
    void modificationsRemovedBlocks(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.modifications-removed-drops")
    void modificationsRemovedDrops(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.modifications-skipped")
    void modificationsSkipped(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.modifications-applied-success")
    void modificationsAppliedSuccess(CommandSender receiver);

    @Message("rich.modifications-planned-success")
    void modificationsAppliedSuccess(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.no-results")
    void noResults(CommandSender receiver);

    @Message("rich.pagination-header")
    void paginationHeader(CommandSender receiver, @Placeholder PaginatedResults<?> pagination);

    @Message("rich.preview-applied")
    void previewApplied(CommandSender receiver);

    @Message("rich.preview-cancelled")
    void previewCancelled(CommandSender receiver);

    @Message("rich.purge-complete")
    void purgeComplete(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.purge-cycle")
    void purgeCycle(CommandSender receiver, @Placeholder PurgeCycleResult result);

    @Message("rich.purge-starting")
    void purgeStarting(CommandSender receiver);

    @Message("rich.reloaded-config")
    void reloadedConfig(CommandSender receiver);

    @Message("rich.reloaded-locales")
    void reloadedLocales(CommandSender receiver);

    @Message("rich.removed-blocks")
    void removedBlocks(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.removed-drops")
    void removedDrops(CommandSender receiver, @Placeholder Integer count);

    @Message("rich.teleporting-to-activity")
    void teleportingToActivity(CommandSender receiver, @Placeholder IActivity activity);

    @Message("rich.teleporting-to")
    void teleportingTo(CommandSender receiver,
        @Placeholder String worldname, @Placeholder Integer x, @Placeholder Integer y, @Placeholder Integer z);

    @Message("rich.wand-activated")
    void wandActivated(Player player, @Placeholder WandMode wandmode);

    @Message("rich.wand-deactivated")
    void wandDeactivated(Player player, @Placeholder WandMode wandmode);

    @Message("rich.wand-switched")
    void wandSwitched(Player player, @Placeholder WandMode wandmode);
}

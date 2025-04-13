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

package network.darkhelmet.prism.bukkit.services.wands;

import com.google.inject.Inject;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.services.modifications.IModificationQueueService;
import network.darkhelmet.prism.api.services.wands.IWand;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.api.storage.IStorageAdapter;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.bukkit.providers.TaskChainProvider;
import network.darkhelmet.prism.bukkit.services.messages.MessageService;
import network.darkhelmet.prism.bukkit.services.modifications.Rollback;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

public class RollbackWand extends AbstractModificationWand implements IWand {
    /**
     * Construct a new inspection wand.
     *
     * @param configurationService The configuration service
     * @param storageAdapter The storage adapter
     * @param messageService The message service
     * @param modificationQueueService The modification queue service
     * @param taskChainProvider The task chain provider
     * @param loggingService The logging service
     */
    @Inject
    public RollbackWand(
            ConfigurationService configurationService,
            IStorageAdapter storageAdapter,
            MessageService messageService,
            IModificationQueueService modificationQueueService,
            TaskChainProvider taskChainProvider,
            LoggingService loggingService) {
        super(
            configurationService,
            storageAdapter,
            messageService,
            modificationQueueService,
            taskChainProvider,
            loggingService);
    }

    @Override
    public WandMode mode() {
        return WandMode.ROLLBACK;
    }

    @Override
    public void setOwner(Object owner) {
        this.owner = owner;
    }

    @Override
    public void use(WorldCoordinate at) {
        final ActivityQuery query = ActivityQuery.builder()
            .worldUuid(at.world().uuid()).coordinate(at).limit(1).rollback().build();

        super.use(query, Rollback.class);
    }
}

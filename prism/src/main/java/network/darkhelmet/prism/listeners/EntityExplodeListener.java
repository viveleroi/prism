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

package network.darkhelmet.prism.listeners;

import com.google.inject.Inject;

import java.util.List;

import network.darkhelmet.prism.api.actions.IActionRegistry;
import network.darkhelmet.prism.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.filters.FilterService;

import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionRegistry The action registry
     * @param filterService The filter service
     */
    @Inject
    public EntityExplodeListener(
            ConfigurationService configurationService,
            IActionRegistry actionRegistry,
            FilterService filterService) {
        super(configurationService, actionRegistry, filterService);
    }

    /**
     * Listens for entity explode events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().explosion()) {
            return;
        }

        String cause = "explosion";
        if (event.getEntity() instanceof TNTPrimed) {
            cause = "tnt";
        } else if (event.getEntity() instanceof EnderDragon) {
            cause = "ender dragon";
        } else {
            cause = event.getEntity().getType().name().toLowerCase().replace("_", " ");
        }

        List<Block> affected = event.blockList();
        for (Block block : affected) {
            recordBlockBreakAction(block, cause);
        }
    }
}

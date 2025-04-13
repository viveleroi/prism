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

package network.darkhelmet.prism.bukkit.listeners.entity;

import com.google.inject.Inject;

import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.Activity;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.bukkit.actions.ActionFactory;
import network.darkhelmet.prism.bukkit.actions.BlockAction;
import network.darkhelmet.prism.bukkit.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.bukkit.listeners.AbstractListener;
import network.darkhelmet.prism.bukkit.services.expectations.ExpectationService;
import network.darkhelmet.prism.bukkit.services.recording.RecordingService;
import network.darkhelmet.prism.bukkit.utils.LocationUtils;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeBlockListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public EntityChangeBlockListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            RecordingService recordingService) {
        super(configurationService, actionFactory, expectationService, recordingService);
    }

    /**
     * Listens for entity change block events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        // The event seems to fire for certain blocks changing state, which is
        // useless when we're only given the material it's changing to.
        // Ex: DEEPSLATE_REDSTONE_ORE changes to DEEPSLATE_REDSTONE_ORE because of SKELETON
        if (event.getBlock().getType().equals(event.getTo())) {
            return;
        }

        WorldCoordinate at = LocationUtils.locToWorldCoordinate(event.getBlock().getLocation());
        BlockState oldState = event.getBlock().getState();
        BlockState newState = event.getBlock().getState();
        newState.setType(event.getTo());

        // If the entity is a sheep, it's eating
        if (event.getEntityType().equals(EntityType.SHEEP)) {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().entityEat()) {
                return;
            }

            // The event.getTo method return AIR for some insane reason. Since we know sheep can only
            // turn grass into dirt, we'll just fake the "new" material.
            var action = new BlockAction(ActionTypeRegistry.ENTITY_EAT, event.getBlock().getType(), Material.DIRT);

            // Build the block activity
            final ISingleActivity activity = Activity.builder()
                .action(action).location(at).cause(nameFromCause(event.getEntity())).build();

            recordingService.addToQueue(activity);

            return;
        }

        // This will handle:
        // - gravity breaking/placing blocks that fall
        // - enderman griefing
        // - etc
        IAction action;
        if (event.getTo().equals(Material.AIR)) {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().blockBreak()) {
                return;
            }

            action = actionFactory.createBlockStateAction(
                ActionTypeRegistry.BLOCK_BREAK, oldState);
        } else {
            // Ignore if this event is disabled
            if (!configurationService.prismConfig().actions().blockPlace()) {
                return;
            }

            action = actionFactory.createBlockStateAction(
                ActionTypeRegistry.BLOCK_PLACE, newState);
        }

        // Build the block activity
        final ISingleActivity activity = Activity.builder()
            .action(action).location(at).cause(nameFromCause(event.getEntity())).build();

        recordingService.addToQueue(activity);
    }
}

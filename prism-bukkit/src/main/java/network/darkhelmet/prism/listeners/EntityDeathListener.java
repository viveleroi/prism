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

import network.darkhelmet.prism.actions.ActionFactory;
import network.darkhelmet.prism.actions.types.ActionTypeRegistry;
import network.darkhelmet.prism.api.actions.IAction;
import network.darkhelmet.prism.api.activities.ISingleActivity;
import network.darkhelmet.prism.api.activities.SingleActivity;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.core.services.configuration.ConfigurationService;
import network.darkhelmet.prism.services.expectations.ExpectationService;
import network.darkhelmet.prism.services.filters.FilterService;
import network.darkhelmet.prism.services.recording.RecordingQueue;
import network.darkhelmet.prism.utils.LocationUtils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.BlockProjectileSource;

public class EntityDeathListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param actionFactory The action factory
     * @param expectationService The expectation service
     * @param filterService The filter service
     */
    @Inject
    public EntityDeathListener(
            ConfigurationService configurationService,
            ActionFactory actionFactory,
            ExpectationService expectationService,
            FilterService filterService) {
        super(configurationService, actionFactory, expectationService, filterService);
    }

    /**
     * Listen to entity death events.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().entityKill()) {
            return;
        }

        final LivingEntity entity = event.getEntity();

        // Resolve cause using last damage
        Object cause = null;
        EntityDamageEvent damageEvent = entity.getLastDamageCause();
        if (damageEvent != null && !damageEvent.isCancelled()) {
            if (damageEvent instanceof EntityDamageByEntityEvent) {
                cause = ((EntityDamageByEntityEvent) damageEvent).getDamager();

                if (cause instanceof Projectile) {
                    cause = ((Projectile) cause).getShooter();

                    if (cause instanceof BlockProjectileSource) {
                        cause = ((BlockProjectileSource) cause).getBlock();
                    }
                }
            } else if (damageEvent instanceof EntityDamageByBlockEvent) {
                cause = ((EntityDamageByBlockEvent) damageEvent).getDamager();
            }
        }

        final IAction action = actionFactory.createEntityAction(ActionTypeRegistry.ENTITY_KILL, entity);

        WorldCoordinate at = LocationUtils.locToWorldCoordinate(entity.getLocation());

        // Build the block break by player activity
        final SingleActivity.Builder builder = SingleActivity.builder()
            .action(action).location(at);

        if (cause != null) {
            if (cause instanceof Player player) {
                builder.player(player.getUniqueId(), player.getName());
            } else {
                builder.cause(causeDescriptor(cause));
            }
        }

        ISingleActivity activity = builder.build();

        if (filterService.allows(activity)) {
            RecordingQueue.addToQueue(activity);
        }
    }
}

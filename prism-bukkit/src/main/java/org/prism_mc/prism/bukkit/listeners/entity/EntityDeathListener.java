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

package org.prism_mc.prism.bukkit.listeners.entity;

import com.google.inject.Inject;

import org.prism_mc.prism.bukkit.actions.BukkitEntityAction;
import org.prism_mc.prism.bukkit.actions.types.BukkitActionTypeRegistry;
import org.prism_mc.prism.bukkit.api.activities.BukkitActivity;
import org.prism_mc.prism.bukkit.listeners.AbstractListener;
import org.prism_mc.prism.bukkit.services.expectations.ExpectationService;
import org.prism_mc.prism.bukkit.services.recording.BukkitRecordingService;
import org.prism_mc.prism.loader.services.configuration.ConfigurationService;

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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.projectiles.BlockProjectileSource;

public class EntityDeathListener extends AbstractListener implements Listener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     * @param expectationService The expectation service
     * @param recordingService The recording service
     */
    @Inject
    public EntityDeathListener(
            ConfigurationService configurationService,
            ExpectationService expectationService,
            BukkitRecordingService recordingService) {
        super(configurationService, expectationService, recordingService);
    }

    /**
     * Listen to entity death events.
     *
     * <p>Note: Player deaths are handled via the PlayerDeathListener.</p>
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) {
        // Ignore if this event is disabled
        if (!configurationService.prismConfig().actions().entityKill() || event.getEntity() instanceof Player) {
            return;
        }

        final LivingEntity entity = event.getEntity();

        // Resolve cause using last damage
        Object cause = null;
        EntityDamageEvent damageEvent = entity.getLastDamageCause();

        if (damageEvent != null && !damageEvent.isCancelled()) {
            if (damageEvent instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
                cause = entityDamageByEntityEvent.getDamager();

                if (cause instanceof Projectile projectile) {
                    cause = projectile.getShooter();

                    if (cause instanceof BlockProjectileSource blockProjectileSource) {
                        cause = blockProjectileSource.getBlock();
                    }
                }
            } else if (damageEvent instanceof EntityDamageByBlockEvent) {
                cause = ((EntityDamageByBlockEvent) damageEvent).getDamager();
            } else {
                cause = nameFromCause(damageEvent.getCause());
            }
        }

        var action = new BukkitEntityAction(BukkitActionTypeRegistry.ENTITY_KILL, entity);

        var builder = BukkitActivity.builder().action(action).location(entity.getLocation());
        if (cause instanceof Player player) {
            builder.player(player);
        } else if (cause != null) {
            builder.cause(nameFromCause(cause));
        }

        recordingService.addToQueue(builder.build());

        // Log inventory drops
        // While event.getDrops() would include these items, it would also list loot which we do not track
        if (entity instanceof InventoryHolder inventoryHolder) {
            recordItemDropFromInventory(
                inventoryHolder.getInventory(), event.getEntity().getLocation(), event.getEntity());
        }
    }
}

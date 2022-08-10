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

import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class ChangeBlockListener extends AbstractListener {
    /**
     * Construct the listener.
     *
     * @param configurationService The configuration service
     */
    @Inject
    public ChangeBlockListener(ConfigurationService configurationService) {
        super(configurationService);
    }

    /**
     * Listens to the base change block event.
     *
     * @param event ChangeBlockEvent
     */
    @Listener(order = Order.POST)
    public void onChangeBlock(ChangeBlockEvent event) {}
}

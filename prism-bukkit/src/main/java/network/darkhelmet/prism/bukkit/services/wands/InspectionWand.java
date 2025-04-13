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
import network.darkhelmet.prism.api.services.wands.IWand;
import network.darkhelmet.prism.api.services.wands.WandMode;
import network.darkhelmet.prism.api.util.WorldCoordinate;
import network.darkhelmet.prism.bukkit.services.lookup.LookupService;
import network.darkhelmet.prism.loader.services.configuration.ConfigurationService;

import org.bukkit.command.CommandSender;

public class InspectionWand implements IWand {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The lookup service.
     */
    private final LookupService lookupService;

    /**
     * The owner.
     */
    private Object owner;

    /**
     * Construct a new inspection wand.
     *
     * @param configurationService The configuration service
     * @param lookupService The lookup server
     */
    @Inject
    public InspectionWand(
            ConfigurationService configurationService,
            LookupService lookupService) {
        this.configurationService = configurationService;
        this.lookupService = lookupService;
    }

    @Override
    public WandMode mode() {
        return WandMode.INSPECT;
    }

    @Override
    public void setOwner(Object owner) {
        this.owner = owner;
    }

    @Override
    public void use(WorldCoordinate at) {
        final ActivityQuery query = ActivityQuery.builder().worldUuid(at.world().uuid())
            .coordinate(at).limit(configurationService.prismConfig().defaults().perPage()).build();

        lookupService.lookup((CommandSender) owner, query);
    }
}

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

package network.darkhelmet.prism.core.storage.adapters.sql;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import network.darkhelmet.prism.api.providers.IWorldIdentityProvider;
import network.darkhelmet.prism.loader.services.logging.LoggingService;

@Singleton
public class SqlSchemaUpdater {
    /**
     * The logger.
     */
    private final LoggingService loggingService;

    /**
     * The world identity provider.
     */
    private final IWorldIdentityProvider worldIdentityProvider;

    /**
     * Construct the updater.
     *
     * @param loggingService The logging service
     */
    @Inject
    public SqlSchemaUpdater(LoggingService loggingService, IWorldIdentityProvider worldIdentityProvider) {
        this.loggingService = loggingService;
        this.worldIdentityProvider = worldIdentityProvider;
    }
}

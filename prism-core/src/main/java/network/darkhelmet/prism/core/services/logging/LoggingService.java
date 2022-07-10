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

package network.darkhelmet.prism.core.services.logging;

import com.google.inject.Inject;

import network.darkhelmet.prism.core.services.configuration.ConfigurationService;

import org.slf4j.Logger;

public class LoggingService {
    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The logger.
     */
    private final Logger logger;

    /**
     * Constructor.
     *
     * @param configurationService The configuration service
     * @param logger The logger
     */
    @Inject
    public LoggingService(ConfigurationService configurationService, Logger logger) {
        this.configurationService = configurationService;
        this.logger = logger;
    }

    /**
     * Log a debug message.
     *
     * <p>It's not feasible for users to set log4j log levels so we emulate debug here.</p>
     *
     * @param msg The message
     */
    public void debug(String msg) {
        if (configurationService.prismConfig().debug()) {
            logger.info(msg);
        }
    }

    /**
     * Log the debug message and format args.
     *
     * <p>It's not feasible for users to set log4j log levels so we emulate debug here.</p>
     *
     * @param msg The string message
     * @param args The args
     */
    public void debug(String msg, Object... args) {
        if (configurationService.prismConfig().debug()) {
            logger.info(String.format(msg, args));
        }
    }

    /**
     * Handle exceptions.
     *
     * @param ex The exception
     */
    public void handleException(Exception ex) {
        logger.error(ex.getMessage(), ex);
    }

    /**
     * Get the logger.
     *
     * @return Logger
     */
    public Logger logger() {
        return logger;
    }
}

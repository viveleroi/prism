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

package network.darkhelmet.prism.core.storage.p6spy;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.FormattedLogger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * log4j logger adapter for p6spy.
 */
public class Log4JLogger extends FormattedLogger {
    /**
     * The logger.
     */
    private final Logger logger;

    /**
     * Constructor.
     */
    public Log4JLogger() {
        logger = LogManager.getLogger("prism-spy");
    }

    @Override
    public void logException(Exception e) {
        logger.error(e.getMessage(), e);
    }

    @Override
    public void logSQL(int connectionId, String now, long elapsed,
           Category category, String prepared, String sql, String url) {
        final String msg = strategy.formatMessage(connectionId, now, elapsed,
            category.toString(), prepared, sql, url);

        if (Category.ERROR.equals(category)) {
            logger.error(msg);
        } else if (Category.WARN.equals(category)) {
            logger.warn(msg);
        } else if (Category.DEBUG.equals(category)) {
            logger.debug(msg);
        } else {
            logger.info(msg);
        }
    }

    @Override
    public void logText(String text) {
        logger.info(text);
    }

    @Override
    public boolean isCategoryEnabled(Category category) {
        if (Category.ERROR.equals(category)) {
            return logger.isErrorEnabled();
        } else if (Category.WARN.equals(category)) {
            return logger.isWarnEnabled();
        } else if (Category.DEBUG.equals(category)) {
            return logger.isDebugEnabled();
        } else {
            return logger.isInfoEnabled();
        }
    }
}

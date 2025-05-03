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

package org.prism_mc.prism.core.storage;

import com.zaxxer.hikari.HikariConfig;
import java.io.File;
import lombok.experimental.UtilityClass;
import org.prism_mc.prism.loader.services.configuration.storage.SqlDataSourceConfiguration;
import org.prism_mc.prism.loader.services.configuration.storage.StorageConfiguration;
import org.prism_mc.prism.loader.storage.StorageType;

@UtilityClass
public class HikariConfigFactory {

    /**
     * Create a hikari configuration for H2 databases.
     *
     * @param storageConfiguration The storage configuration
     * @return The hikari configuration
     */
    public static HikariConfig h2(StorageConfiguration storageConfiguration, File h2File) {
        HikariConfig hikariConfig = createSharedConfig(storageConfiguration);

        String jdbcUrl =
            "jdbc:" +
            (storageConfiguration.spy() ? "p6spy:" : "") +
            "h2:file:" +
            h2File.getAbsolutePath() +
            ";MODE=mysql;" +
            "DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;";

        if (storageConfiguration.spy()) {
            hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setJdbcUrl(jdbcUrl);
        } else {
            tryDataSourceClassNames(hikariConfig, "org.h2.jdbcx.JdbcDataSource");
            tryDriverClassNames("org.h2.Driver");
        }

        hikariConfig.addDataSourceProperty("url", jdbcUrl);

        return hikariConfig;
    }

    /**
     * Load drivers for a given storage type.
     *
     * @param storageType The storage type
     */
    public static void loadDriver(StorageType storageType) {
        switch (storageType) {
            case H2 -> tryDriverClassNames("org.h2.Driver");
            case MARIADB -> tryDriverClassNames("org.mariadb.jdbc.Driver");
            case MYSQL -> tryDriverClassNames("org.mysql.jdbc.Driver");
            case POSTGRES -> tryDriverClassNames("org.postgresql.Driver");
            default -> {
                // ignored
            }
        }
    }

    /**
     * Create a hikari configuration for MariaDB databases.
     *
     * @param storageConfiguration The storage configuration
     * @return The hikari configuration
     */
    public static HikariConfig mariadb(StorageConfiguration storageConfiguration) {
        HikariConfig hikariConfig = createSharedConfig(storageConfiguration);

        String host = storageConfiguration.mariadb().host();
        String port = storageConfiguration.mariadb().port();
        String database = storageConfiguration.mariadb().database();
        boolean useSpy = storageConfiguration.spy();

        loadDriver(StorageType.MARIADB);

        String jdbcUrl = "jdbc:" + (useSpy ? "p6spy:" : "") + String.format("mariadb://%s:%s/%s", host, port, database);

        if (storageConfiguration.spy()) {
            hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setJdbcUrl(jdbcUrl);
        } else {
            tryDataSourceClassNames(hikariConfig, "org.mariadb.jdbc.MariaDbDataSource");
        }

        hikariConfig.addDataSourceProperty("url", jdbcUrl);

        hikariConfig.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

        return hikariConfig;
    }

    /**
     * Create a hikari configuration for MySQL databases.
     *
     * @param storageConfiguration The storage configuration
     * @return The hikari configuration
     */
    public static HikariConfig mysql(StorageConfiguration storageConfiguration) {
        HikariConfig hikariConfig = createSharedConfig(storageConfiguration);

        String host = storageConfiguration.mysql().host();
        String port = storageConfiguration.mysql().port();
        String database = storageConfiguration.mysql().database();
        boolean useSpy = storageConfiguration.spy();

        String jdbcUrl = "jdbc:" + (useSpy ? "p6spy:" : "") + String.format("mysql://%s:%s/%s", host, port, database);

        if (storageConfiguration.spy()) {
            hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setJdbcUrl(jdbcUrl);
        } else {
            tryDataSourceClassNames(hikariConfig, "com.mysql.cj.jdbc.MysqlDataSource");
        }

        hikariConfig.addDataSourceProperty("url", jdbcUrl);

        if (storageConfiguration.mysql().useHikariOptimizations()) {
            hikariConfig.addDataSourceProperty("cachePrepStmts", true);
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
            hikariConfig.addDataSourceProperty("cacheCallableStmts", true);
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", true);
            hikariConfig.addDataSourceProperty("useLocalSessionState", true);
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", true);
            hikariConfig.addDataSourceProperty("alwaysSendSetIsolation", false);
        }

        hikariConfig.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

        return hikariConfig;
    }

    /**
     * Create a hikari configuration for Postgres databases.
     *
     * @param storageConfiguration The storage configuration
     * @return The hikari configuration
     */
    public static HikariConfig postgres(StorageConfiguration storageConfiguration) {
        HikariConfig hikariConfig = createSharedConfig(storageConfiguration);

        String host = storageConfiguration.postgres().host();
        String port = storageConfiguration.postgres().port();
        String database = storageConfiguration.postgres().database();
        boolean useSpy = storageConfiguration.spy();

        String jdbcUrl =
            "jdbc:" + (useSpy ? "p6spy:" : "") + String.format("postgresql://%s:%s/%s", host, port, database);

        if (storageConfiguration.spy()) {
            hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setJdbcUrl(jdbcUrl);
        } else {
            tryDataSourceClassNames(hikariConfig, "org.postgresql.ds.PGSimpleDataSource");
        }

        hikariConfig.addDataSourceProperty("url", jdbcUrl);

        return hikariConfig;
    }

    /**
     * Create a hikari configuration for sqlite databases.
     *
     * @param storageConfiguration The storage configuration
     * @return The hikari configuration
     */
    public static HikariConfig sqlite(StorageConfiguration storageConfiguration, File sqliteFile) {
        HikariConfig hikariConfig = createSharedConfig(storageConfiguration);

        String jdbcUrl =
            "jdbc:" + (storageConfiguration.spy() ? "p6spy:" : "") + "sqlite:file:" + sqliteFile.getAbsolutePath();

        if (storageConfiguration.spy()) {
            hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setJdbcUrl(jdbcUrl);
        } else {
            tryDataSourceClassNames(hikariConfig, "org.sqlite.SQLiteDataSource");
            tryDriverClassNames("org.sqlite.JDBC");
        }

        hikariConfig.addDataSourceProperty("url", jdbcUrl);

        return hikariConfig;
    }

    /**
     * Create a hikari config with common settings.
     *
     * @param storageConfiguration The storage configuration
     * @return The hikari config
     */
    private static HikariConfig createSharedConfig(StorageConfiguration storageConfiguration) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("prism");

        if (storageConfiguration.primaryDataSource() instanceof SqlDataSourceConfiguration sqlConfig) {
            if (sqlConfig.username() != null) {
                hikariConfig.addDataSourceProperty("user", sqlConfig.username());
            }

            if (sqlConfig.password() != null) {
                hikariConfig.addDataSourceProperty("password", sqlConfig.password());
            }
        }

        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setMinimumIdle(3);
        hikariConfig.setMaximumPoolSize(5);

        return hikariConfig;
    }

    /**
     * Try to find an available data source class name.
     *
     * @param hikariConfig The hikari config
     * @param dataSourceClassNames The class names to try
     */
    private static void tryDataSourceClassNames(HikariConfig hikariConfig, String... dataSourceClassNames) {
        for (String dataSourceClassName : dataSourceClassNames) {
            try {
                Class.forName(dataSourceClassName);

                hikariConfig.setDataSourceClassName(dataSourceClassName);

                break;
            } catch (ClassNotFoundException e) {
                // ignored
            }
        }
    }

    /**
     * Try to find an available driver class name.
     *
     * @param hikariConfig The hikari config
     * @param driverClassNames The class names to try
     */
    private static void tryDriverClassNames(HikariConfig hikariConfig, String... driverClassNames) {
        String driverClassName = tryDriverClassNames(driverClassNames);
        if (driverClassName != null) {
            hikariConfig.setDriverClassName(driverClassName);
        }
    }

    /**
     * Try to find an available driver class name.
     *
     * @param driverClassNames The class names to try
     * @return A found classname
     */
    private static String tryDriverClassNames(String... driverClassNames) {
        for (String driverClassName : driverClassNames) {
            try {
                Class.forName(driverClassName).getDeclaredConstructor().newInstance();

                return driverClassName;
            } catch (Exception e) {
                // ignore
            }
        }

        return null;
    }
}

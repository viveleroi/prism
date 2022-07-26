/*
 * This file is a part of prism-idb.
 *
 * MIT License
 *
 * Copyright (c) 2014-2018 Daniel Ennis
 * Copyright 2022 viveleroi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.darkhelmet.prism.idb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class HikariPooledDatabase extends BaseDatabase {
    /**
     * Constructor.
     *
     * @param poolOptions The pool options
     */
    public HikariPooledDatabase(PooledDatabaseOptions poolOptions) {
        super(poolOptions.options);
        DatabaseOptions options = poolOptions.options;

        HikariConfig config = new HikariConfig();
        if (poolOptions.options().useSpy()) {
            config.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            config.setJdbcUrl("jdbc:" + options.dsn);
        } else {
            if (options.dataSourceClassName != null) {
                config.setDataSourceClassName(options.dataSourceClassName);
            }
        }
        config.setPoolName(options.poolName);
        config.addDataSourceProperty("url", "jdbc:" + options.dsn);

        if (options.user != null) {
            config.addDataSourceProperty("user", options.user);
        }
        if (options.pass != null) {
            config.addDataSourceProperty("password", options.pass);
        }

        if (options.useOptimizations && options.dsn.startsWith("mysql")) {
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 250);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("cacheCallableStmts", true);
            config.addDataSourceProperty("cacheResultSetMetadata", true);
            config.addDataSourceProperty("cacheServerConfiguration", true);
            config.addDataSourceProperty("useLocalSessionState", true);
            config.addDataSourceProperty("elideSetAutoCommits", true);
            config.addDataSourceProperty("alwaysSendSetIsolation", false);
        }
        if (poolOptions.dataSourceProperties != null) {
            for (Map.Entry<String, Object> entry : poolOptions.dataSourceProperties.entrySet()) {
                config.addDataSourceProperty(entry.getKey(), entry.getValue());
            }
        }

        config.setConnectionTestQuery("SELECT 1");
        config.setMinimumIdle(poolOptions.minIdleConnections);
        config.setMaximumPoolSize(poolOptions.maxConnections);
        config.setTransactionIsolation(options.defaultIsolationLevel);

        dataSource = new HikariDataSource(config);
    }

    /**
     * Constructor.
     *
     * @param poolOptions The pool options
     * @param hikariConfig The hikari config
     */
    public HikariPooledDatabase(PooledDatabaseOptions poolOptions, HikariConfig hikariConfig) {
        super(poolOptions.options);
        dataSource = new HikariDataSource(hikariConfig);
    }
}

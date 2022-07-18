package network.darkhelmet.prism.idb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class HikariPooledDatabase extends BaseDatabase {
    public HikariPooledDatabase(PooledDatabaseOptions poolOptions) {
        super(poolOptions.options);
        DatabaseOptions options = poolOptions.options;

        HikariConfig config = new HikariConfig();
        if (poolOptions.options().useSpy()) {
            config.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        } else {
            if (options.dataSourceClassName != null) {
                config.setDataSourceClassName(options.dataSourceClassName);
            }
        }
        config.setPoolName(options.poolName);
        config.addDataSourceProperty("url", "jdbc:" + options.dsn);
        config.setJdbcUrl("jdbc:" + options.dsn);

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

    public HikariPooledDatabase(PooledDatabaseOptions poolOptions, HikariConfig hikariConfig) {
        super(poolOptions.options);
        dataSource = new HikariDataSource(hikariConfig);
    }
}

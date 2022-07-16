package network.darkhelmet.prism.idb;

import com.zaxxer.hikari.HikariConfig;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@SuppressWarnings("UnusedAssignment")
@Builder(toBuilder = true) @Data
public class PooledDatabaseOptions {
    @Builder.Default int minIdleConnections = 3;
    @Builder.Default int maxConnections = 5;
    Map<String, Object> dataSourceProperties;
    DatabaseOptions options;
    HikariConfig hikariConfig;

    public static class PooledDatabaseOptionsBuilder  {
        public HikariPooledDatabase createHikariDatabase() {
            if (hikariConfig != null) {
                String url = hikariConfig.getDataSourceProperties().getProperty("url");
                String dsn = url.replace("jdbc:", "");

                DatabaseOptions.DatabaseOptionsBuilder builder = DatabaseOptions.builder().dsn(dsn);
                options = builder.build();

                return new HikariPooledDatabase(this.build(), hikariConfig);
            }

            return new HikariPooledDatabase(this.build());
        }
    }
}

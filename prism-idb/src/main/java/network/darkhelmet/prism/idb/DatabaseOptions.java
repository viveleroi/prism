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

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import org.apache.logging.log4j.Logger;

@Builder(toBuilder = true)
@Data
public class DatabaseOptions {
    private static final DatabaseTiming NULL_TIMING = new NullDatabaseTiming();
    /**
     * JDBC DSN to connect to.
     */
    @NonNull String dsn;

    /**
     * JDBC Classname of the Driver name to use.
     */
    String driverClassName;

    /**
     * Class name of DataSource to use.
     */
    String dataSourceClassName;
    String defaultIsolationLevel;

    @Builder.Default boolean favorDataSourceOverDriver = true;

    @Builder.Default String poolName = "DB";
    @Builder.Default boolean useOptimizations = true;
    @Builder.Default boolean useSpy = false;

    /**
     * For Async queries, minimum threads in the pool to use.
     */
    @Builder.Default int minAsyncThreads = Math.min(Runtime.getRuntime().availableProcessors(), 2);

    /**
     * For Async queries, maximum threads in the pool to use.
     */
    @Builder.Default int maxAsyncThreads = Runtime.getRuntime().availableProcessors();
    @Builder.Default int asyncThreadTimeout = 60;
    @Builder.Default TimingsProvider timingsProvider = (name, parent) -> NULL_TIMING;
    @Builder.Default Consumer<Exception> onFatalError = DB::logException;
    @Builder.Default Consumer<Exception> onDatabaseConnectionFailure = DB::logException;

    String user;
    String pass;
    Logger logger;
    ExecutorService executor;

    public static class DatabaseOptionsBuilder {
        /**
         * Create options for a mysql db.
         *
         * @param user The user
         * @param pass The pass
         * @param db The database
         * @param hostAndPort The host and port
         * @param useSpy If using spy
         * @return The builder
         */
        public DatabaseOptionsBuilder mysql(
                @NonNull String user,
                @NonNull String pass,
                @NonNull String db,
                @NonNull String hostAndPort,
                boolean useSpy) {
            if (hostAndPort == null) {
                hostAndPort = "localhost:3306";
            }
            this.user = user;
            this.pass = pass;
            this.useSpy(useSpy);

            if (defaultIsolationLevel == null) {
                defaultIsolationLevel = "TRANSACTION_READ_COMMITTED";
            }

            if (dataSourceClassName == null) {
                tryDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
            }
            if (dataSourceClassName == null) {
                tryDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            }
            if (dataSourceClassName == null) {
                tryDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
            }

            if (driverClassName == null) {
                tryDriverClassName("org.mariadb.jdbc.Driver");
            }
            if (driverClassName == null) {
                tryDriverClassName("com.mysql.cj.jdbc.Driver");
            }
            if (driverClassName == null) {
                tryDriverClassName("com.mysql.jdbc.Driver");
            }

            this.dsn = (useSpy ? "p6spy:" : "") + "mysql://" + hostAndPort + "/" + db;
            return this;
        }

        /**
         * Create an sqlite builder.
         *
         * @param fileName The filename
         * @return The builder
         */
        public DatabaseOptionsBuilder sqlite(@NonNull String fileName) {
            if (defaultIsolationLevel == null) {
                defaultIsolationLevel = "TRANSACTION_SERIALIZABLE";
            }

            if (dataSourceClassName == null) {
                tryDataSourceClassName("org.sqlite.SQLiteDataSource");
            }

            if (driverClassName == null) {
                tryDriverClassName("org.sqlite.JDBC");
            }

            this.dsn = "sqlite:" + fileName;

            return this;
        }

        /**
         * Tries the specified JDBC driverClassName, and uses it if it is valid.
         */
        public DatabaseOptionsBuilder tryDriverClassName(@NonNull String className) {
            try {
                driverClassName(className);
            } catch (Exception ignored) {
                // ignored
            }

            return this;
        }

        /**
         * Tries the specified JDBC DataSource, and uses it if it is valid.
         */
        public DatabaseOptionsBuilder tryDataSourceClassName(@NonNull String className) {
            try {
                dataSourceClassName(className);
            } catch (Exception ignored) {
                // ignored
            }

            return this;
        }

        /**
         * Set the driver class name.
         *
         * @param className The classname
         * @return The builder
         */
        public DatabaseOptionsBuilder driverClassName(@NonNull String className) {
            try {
                Class.forName(className).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            this.driverClassName = className;
            return this;
        }

        /**
         * Set the data source class name.
         *
         * @param className The class name
         * @return The builder
         */
        public DatabaseOptionsBuilder dataSourceClassName(@NonNull String className) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            this.dataSourceClassName = className;
            return this;
        }
    }
}

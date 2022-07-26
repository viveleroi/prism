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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseDatabase implements Database {
    private TimingsProvider timingsProvider;
    private DatabaseTiming sqlTiming;
    private Logger logger;
    protected DatabaseOptions options;
    private ExecutorService threadPool;
    DataSource dataSource;

    /**
     * Constructor.
     *
     * @param options The options
     */
    public BaseDatabase(DatabaseOptions options) {
        this.options = options;
        if (!options.favorDataSourceOverDriver) {
            options.dataSourceClassName = null;
        }
        this.timingsProvider = options.timingsProvider;
        this.threadPool = options.executor;
        if (this.threadPool == null) {
            this.threadPool = new ThreadPoolExecutor(
                options.minAsyncThreads,
                options.maxAsyncThreads,
                options.asyncThreadTimeout,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
            );
            ((ThreadPoolExecutor) threadPool).allowCoreThreadTimeOut(true);
        }
        this.sqlTiming = timingsProvider.of("Database");
        this.logger = options.logger;
        if (this.logger == null) {
            this.logger = LogManager.getLogger(options.poolName);
        }
        this.logger.info("Connecting to Database: " + options.dsn);
    }

    /**
     * Close the connection.
     *
     * @param timeout The timeout
     * @param unit The timeout time unit
     */
    public void close(long timeout, TimeUnit unit) {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            logException(e);
        }
        if (dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                logException(e);
            } finally {
                dataSource = null;
            }
        }
    }

    @Override
    public synchronized <T> CompletableFuture<T> dispatchAsync(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Runnable run = () -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };
        if (threadPool == null) {
            run.run();
        } else {
            threadPool.submit(run);
        }
        return future;
    }

    @Override
    public DatabaseTiming timings(String name) {
        return timingsProvider.of(options.poolName + " - " + name, sqlTiming);
    }

    @Override
    public DatabaseOptions getOptions() {
        return this.options;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Connection getConnection() throws SQLException {
        return dataSource != null ? dataSource.getConnection() : null;
    }
}

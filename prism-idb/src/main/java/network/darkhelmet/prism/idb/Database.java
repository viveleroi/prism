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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Language;

public interface Database {
    /**
     * Called in onDisable, destroys the Data source and nulls out references.
     */
    default void close() {
        close(120, TimeUnit.SECONDS);
    }

    /**
     * Called in onDisable, destroys the Data source and nulls out references.
     */
    void close(long timeout, TimeUnit unit);

    <T> CompletableFuture<T> dispatchAsync(Callable<T> task);

    /**
     * Get a JDBC Connection.
     */
    Connection getConnection() throws SQLException;

    /**
     * Create a Timings object.
     */
    DatabaseTiming timings(String name);

    /**
     * Get the Logger.
     */
    Logger getLogger();

    /**
     * Get the options object.
     */
    DatabaseOptions getOptions();

    default void fatalError(Exception e) {
        getOptions().onFatalError.accept(e);
    }

    default void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    /**
     * Initiates a new DbStatement.
     *
     * <p/>YOU MUST MANUALLY CLOSE THIS STATEMENT IN A finally {} BLOCK!</p>
     */
    default DbStatement createStatement() throws SQLException {
        return (new DbStatement(this));
    }

    /**
     * Initiates a new DbStatement and prepares the first query.
     *
     * <p>YOU MUST MANUALLY CLOSE THIS STATEMENT IN A finally {} BLOCK!</p>
     */
    default DbStatement query(@Language("SQL") String query) throws SQLException {
        DbStatement stm = new DbStatement(this);
        try {
            stm.query(query);
            return stm;
        } catch (Exception e) {
            stm.close();
            throw e;
        }
    }

    /**
     * Initiates a new DbStatement and prepares the first query.
     *
     * <p>YOU MUST MANUALLY CLOSE THIS STATEMENT IN A finally {} BLOCK!</p>
     */
    default CompletableFuture<DbStatement> queryAsync(@Language("SQL") String query) {
        return dispatchAsync(() -> new DbStatement(this).query(query));
    }

    /**
     * Utility method to execute a query and retrieve the first row, then close statement.
     * You should ensure result will only return 1 row for maximum performance.
     *
     * @param query  The query to run
     * @param params The parameters to execute the statement with
     * @return DbRow of your results (HashMap with template return type)
     */
    default DbRow getFirstRow(@Language("SQL") String query, Object... params) throws SQLException {
        try (DbStatement statement = query(query)) {
            statement.execute(params);
            return statement.getNextRow();
        }
    }

    /**
     * Utility method to execute a query and retrieve the first row, then close statement.
     * You should ensure result will only return 1 row for maximum performance.
     *
     * @param query  The query to run
     * @param params The parameters to execute the statement with
     * @return DbRow of your results (HashMap with template return type)
     */
    default CompletableFuture<DbRow> getFirstRowAsync(@Language("SQL") String query, Object... params) {
        return dispatchAsync(() -> getFirstRow(query, params));
    }

    /**
     * Utility method to execute a query and retrieve the first column of the first row, then close statement.
     * You should ensure result will only return 1 row for maximum performance.
     *
     * @param query  The query to run
     * @param params The parameters to execute the statement with
     * @return DbRow of your results (HashMap with template return type)
     */
    default <T> T getFirstColumn(@Language("SQL") String query, Object... params) throws SQLException {
        try (DbStatement statement = query(query)) {
            statement.execute(params);
            return statement.getFirstColumn();
        }
    }

    /**
     * Utility method to execute a query and retrieve the first column of the first row, then close statement.
     * You should ensure result will only return 1 row for maximum performance.
     *
     * @param query  The query to run
     * @param params The parameters to execute the statement with
     * @return DbRow of your results (HashMap with template return type)
     */
    default <T> CompletableFuture<T> getFirstColumnAsync(@Language("SQL") String query, Object... params) {
        return dispatchAsync(() -> getFirstColumn(query, params));
    }

    /**
     * Utility method to execute a query and retrieve first column of all results, then close statement.
     *
     * <p>Meant for single queries that will not use the statement multiple times.</p>
     */
    default <T> List<T> getFirstColumnResults(@Language("SQL") String query, Object... params) throws SQLException {
        List<T> dbRows = new ArrayList<>();
        T result;
        try (DbStatement statement = query(query)) {
            statement.execute(params);
            while ((result = statement.getFirstColumn()) != null) {
                dbRows.add(result);
            }
        }
        return dbRows;
    }

    /**
     * Utility method to execute a query and retrieve first column of all results, then close statement.
     *
     * <p>Meant for single queries that will not use the statement multiple times.</p>
     */
    default <T> CompletableFuture<List<T>> getFirstColumnResultsAsync(@Language("SQL") String query, Object... params) {
        return dispatchAsync(() -> getFirstColumnResults(query, params));
    }

    /**
     * Utility method to execute a query and retrieve all results, then close statement.
     *
     * <p>Meant for single queries that will not use the statement multiple times.</p>
     *
     * @param query  The query to run
     * @param params The parameters to execute the statement with
     * @return List of DbRow of your results (HashMap with template return type)
     */
    default List<DbRow> getResults(@Language("SQL") String query, Object... params) throws SQLException {
        try (DbStatement statement = query(query)) {
            statement.execute(params);
            return statement.getResults();
        }
    }

    /**
     * Utility method to execute a query and retrieve all results, then close statement.
     *
     * <p>Meant for single queries that will not use the statement multiple times.</p>
     *
     * @param query  The query to run
     * @param params The parameters to execute the statement with
     * @return List of DbRow of your results (HashMap with template return type)
     */
    default CompletableFuture<List<DbRow>> getResultsAsync(@Language("SQL") String query, Object... params) {
        return dispatchAsync(() -> getResults(query, params));
    }

    /**
     * Utility method for executing an update synchronously that does an insert,
     * closes the statement, and returns the last insert ID.
     *
     * @param query  Query to run
     * @param params Params to execute the statement with.
     * @return Inserted Row Id.
     */
    default Long executeInsert(@Language("SQL") String query, Object... params) throws SQLException {
        try (DbStatement statement = query(query)) {
            int i = statement.executeUpdate(params);
            if (i > 0) {
                return statement.getLastInsertId();
            }
        }
        return null;
    }

    /**
     * Utility method for executing an update synchronously, and then close the statement.
     *
     * @param query  Query to run
     * @param params Params to execute the statement with.
     * @return Number of rows modified.
     */
    default int executeUpdate(@Language("SQL") String query, Object... params) throws SQLException {
        try (DbStatement statement = query(query)) {
            return statement.executeUpdate(params);
        }
    }

    /**
     * Utility method to execute an update statement asynchronously and close the connection.
     *
     * @param query  Query to run
     * @param params Params to execute the update with
     */
    default CompletableFuture<Integer> executeUpdateAsync(@Language("SQL") String query, final Object... params) {
        return dispatchAsync(() -> executeUpdate(query, params));
    }

    default void createTransactionAsync(TransactionCallback run) {
        createTransactionAsync(run, null, null);
    }

    /**
     * Create a transaction async.
     *
     * @param run The runner
     * @param onSuccess The success callback
     * @param onFail The failure callback
     */
    default void createTransactionAsync(TransactionCallback run, Runnable onSuccess, Runnable onFail) {
        dispatchAsync(() -> {
            if (!createTransaction(run)) {
                if (onFail != null) {
                    onFail.run();
                }
            } else if (onSuccess != null) {
                onSuccess.run();
            }
            return null;
        });
    }

    /**
     * Create a transaction.
     *
     * @param run The runner
     * @return True if commit
     */
    default boolean createTransaction(TransactionCallback run) {
        try (DbStatement stm = new DbStatement(this)) {
            try {
                stm.startTransaction();
                if (!run.apply(stm)) {
                    stm.rollback();
                    return false;
                } else {
                    stm.commit();
                    return true;
                }
            } catch (Exception e) {
                stm.rollback();
                DB.logException(e);
            }
        } catch (SQLException e) {
            DB.logException(e);
        }
        return false;
    }

    default void logException(String message, Exception e) {
        DB.logException(getLogger(), Level.SEVERE, message, e);
    }

    default void logException(Exception e) {
        DB.logException(getLogger(), Level.SEVERE, e.getMessage(), e);
    }
}

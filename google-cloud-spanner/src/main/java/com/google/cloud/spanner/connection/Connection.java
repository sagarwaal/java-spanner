/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spanner.connection;

import com.google.api.core.ApiFuture;
import com.google.api.core.InternalApi;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.AbortedDueToConcurrentModificationException;
import com.google.cloud.spanner.AbortedException;
import com.google.cloud.spanner.AsyncResultSet;
import com.google.cloud.spanner.CommitResponse;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Dialect;
import com.google.cloud.spanner.ErrorCode;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Options.QueryOption;
import com.google.cloud.spanner.Options.RpcPriority;
import com.google.cloud.spanner.Options.UpdateOption;
import com.google.cloud.spanner.PartitionOptions;
import com.google.cloud.spanner.ReadContext.QueryAnalyzeMode;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerBatchUpdateException;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TimestampBound;
import com.google.cloud.spanner.connection.StatementResult.ResultType;
import com.google.spanner.v1.DirectedReadOptions;
import com.google.spanner.v1.ExecuteBatchDmlRequest;
import com.google.spanner.v1.ResultSetStats;
import com.google.spanner.v1.TransactionOptions.IsolationLevel;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * Internal connection API for Google Cloud Spanner. This interface may introduce breaking changes
 * without prior notice.
 *
 * <p>A connection to a Cloud Spanner database. Connections are not designed to be thread-safe. The
 * only exception is the {@link Connection#cancel()} method that may be called by any other thread
 * to stop the execution of the current statement on the connection.
 *
 * <p>All -Async methods on {@link Connection} are guaranteed to be executed in the order that they
 * are issued on the {@link Connection}. Mixing synchronous and asynchronous method calls is also
 * supported, and these are also guaranteed to be executed in the order that they are issued.
 *
 * <p>Connections accept a number of additional SQL statements for setting or changing the state of
 * a {@link Connection}. These statements can only be executed using the {@link
 * Connection#execute(Statement)} method:
 *
 * <ul>
 *   <li><code>SHOW AUTOCOMMIT</code>: Returns the current value of <code>AUTOCOMMIT</code> of this
 *       connection as a {@link ResultSet}
 *   <li><code>SET AUTOCOMMIT=TRUE|FALSE</code>: Sets the value of <code>AUTOCOMMIT</code> for this
 *       connection
 *   <li><code>SHOW READONLY</code>: Returns the current value of <code>READONLY</code> of this
 *       connection as a {@link ResultSet}
 *   <li><code>SET READONLY=TRUE|FALSE</code>: Sets the value of <code>READONLY</code> for this
 *       connection
 *   <li><code>SHOW RETRY_ABORTS_INTERNALLY</code>: Returns the current value of <code>
 *       RETRY_ABORTS_INTERNALLY</code> of this connection as a {@link ResultSet}
 *   <li><code>SET RETRY_ABORTS_INTERNALLY=TRUE|FALSE</code>: Sets the value of <code>
 *       RETRY_ABORTS_INTERNALLY</code> for this connection
 *   <li><code>SHOW AUTOCOMMIT_DML_MODE</code>: Returns the current value of <code>
 *       AUTOCOMMIT_DML_MODE</code> of this connection as a {@link ResultSet}
 *   <li><code>SET AUTOCOMMIT_DML_MODE='TRANSACTIONAL' | 'PARTITIONED_NON_ATOMIC'</code>: Sets the
 *       value of <code>AUTOCOMMIT_DML_MODE</code> for this connection
 *   <li><code>SHOW STATEMENT_TIMEOUT</code>: Returns the current value of <code>STATEMENT_TIMEOUT
 *       </code> of this connection as a {@link ResultSet}
 *   <li><code>SET STATEMENT_TIMEOUT='&lt;int64&gt;s|ms|us|ns' | NULL</code>: Sets the value of
 *       <code>STATEMENT_TIMEOUT</code> for this connection. The supported {@link TimeUnit}s are:
 *       <ul>
 *         <li>s - Seconds
 *         <li>ms - Milliseconds
 *         <li>us - Microseconds
 *         <li>ns - Nanoseconds
 *       </ul>
 *       Setting the STATEMENT_TIMEOUT to NULL will clear the value for the STATEMENT_TIMEOUT on the
 *       connection.
 *   <li><code>SHOW READ_TIMESTAMP</code>: Returns the last <code>READ_TIMESTAMP</code> of this
 *       connection as a {@link ResultSet}
 *   <li><code>SHOW COMMIT_TIMESTAMP</code>: Returns the last <code>COMMIT_TIMESTAMP</code> of this
 *       connection as a {@link ResultSet}
 *   <li><code>SHOW READ_ONLY_STALENESS</code>: Returns the current value of <code>
 *       READ_ONLY_STALENESS</code> of this connection as a {@link ResultSet}
 *   <li><code>
 *       SET READ_ONLY_STALENESS='STRONG' | 'MIN_READ_TIMESTAMP &lt;timestamp&gt;' | 'READ_TIMESTAMP &lt;timestamp&gt;' | 'MAX_STALENESS &lt;int64&gt;s|ms|mus|ns' | 'EXACT_STALENESS (&lt;int64&gt;s|ms|mus|ns)'
 *       </code>: Sets the value of <code>READ_ONLY_STALENESS</code> for this connection.
 *   <li><code>SHOW OPTIMIZER_VERSION</code>: Returns the current value of <code>
 *       OPTIMIZER_VERSION</code> of this connection as a {@link ResultSet}
 *   <li><code>
 *       SET OPTIMIZER_VERSION='&lt;version&gt;' | 'LATEST'
 *       </code>: Sets the value of <code>OPTIMIZER_VERSION</code> for this connection.
 *   <li><code>SHOW OPTIMIZER_STATISTICS_PACKAGE</code>: Returns the current value of <code>
 *       OPTIMIZER_STATISTICS_PACKAGE</code> of this connection as a {@link ResultSet}
 *   <li><code>
 *       SET OPTIMIZER_STATISTICS_PACKAGE='&lt;package&gt;' | ''
 *       </code>: Sets the value of <code>OPTIMIZER_STATISTICS_PACKAGE</code> for this connection.
 *   <li><code>BEGIN [TRANSACTION]</code>: Begins a new transaction. This statement is optional when
 *       the connection is not in autocommit mode, as a new transaction will automatically be
 *       started when a query or update statement is issued. In autocommit mode, this statement will
 *       temporarily put the connection in transactional mode, and return the connection to
 *       autocommit mode when <code>COMMIT [TRANSACTION]</code> or <code>ROLLBACK [TRANSACTION]
 *       </code> is executed
 *   <li><code>COMMIT [TRANSACTION]</code>: Commits the current transaction
 *   <li><code>ROLLBACK [TRANSACTION]</code>: Rollbacks the current transaction
 *   <li><code>SET TRANSACTION READ ONLY|READ WRITE</code>: Sets the type for the current
 *       transaction. May only be executed before a transaction is actually running (i.e. before any
 *       statements have been executed in the transaction)
 *   <li><code>START BATCH DDL</code>: Starts a batch of DDL statements. May only be executed when
 *       no transaction has been started and the connection is in read/write mode. The connection
 *       will only accept DDL statements while a DDL batch is active.
 *   <li><code>START BATCH DML</code>: Starts a batch of DML statements. May only be executed when
 *       the connection is in read/write mode. The connection will only accept DML statements while
 *       a DML batch is active.
 *   <li><code>RUN BATCH</code>: Ends the current batch, sends the batched DML or DDL statements to
 *       Spanner and blocks until all statements have been executed or an error occurs. May only be
 *       executed when a (possibly empty) batch is active. The statement will return the update
 *       counts of the batched statements as {@link ResultSet} with an ARRAY&lt;INT64&gt; column. In
 *       case of a DDL batch, this array will always be empty.
 *   <li><code>ABORT BATCH</code>: Ends the current batch and removes any DML or DDL statements from
 *       the buffer without sending any statements to Spanner. May only be executed when a (possibly
 *       empty) batch is active.
 * </ul>
 *
 * Note that Cloud Spanner could abort read/write transactions in the background, and that
 * <strong>any</strong> database call during a read/write transaction could fail with an {@link
 * AbortedException}. This also includes calls to {@link ResultSet#next()}.
 *
 * <p>If {@link Connection#isRetryAbortsInternally()} is <code>true</code>, then the connection will
 * silently handle any {@link AbortedException}s by internally re-acquiring all transactional locks
 * and verifying (via the use of cryptographic checksums) that no underlying data has changed. If a
 * change to the underlying data is detected, then an {@link
 * AbortedDueToConcurrentModificationException} error will be thrown. If your application already
 * uses retry loops to handle these Aborted errors, then it will be most efficient to set {@link
 * Connection#isRetryAbortsInternally()} to <code>false</code>.
 *
 * <p>Use {@link ConnectionOptions} to create a {@link Connection}.
 */
@InternalApi
public interface Connection extends AutoCloseable {

  /** Closes this connection. This is a no-op if the {@link Connection} has already been closed. */
  @Override
  void close();

  /**
   * Closes this connection without blocking. This is a no-op if the {@link Connection} has already
   * been closed. The {@link Connection} is no longer usable directly after calling this method. The
   * returned {@link ApiFuture} is done when the running statement(s) (if any) on the connection
   * have finished.
   */
  ApiFuture<Void> closeAsync();

  /**
   * @return <code>true</code> if this connection has been closed.
   */
  boolean isClosed();

  /**
   * Resets the state of this connection to the default state that it had when it was first created.
   * Calling this method after a transaction has started (that is; after a statement has been
   * executed in the transaction), does not change the active transaction. If for example a
   * transaction has been started with a transaction tag, the transaction tag for the active
   * transaction is not reset.
   *
   * <p>You can use this method to reset the state of the connection before returning a connection
   * to a connection pool, and/or before using a connection that was retrieved from a connection
   * pool.
   */
  void reset();

  /**
   * Sets autocommit on/off for this {@link Connection}. Connections in autocommit mode will apply
   * any changes to the database directly without waiting for an explicit commit. DDL- and DML
   * statements as well as {@link Mutation}s are sent directly to Spanner, and committed
   * automatically unless the statement caused an error. The statement is retried in case of an
   * {@link AbortedException}. All other errors will cause the underlying transaction to be rolled
   * back.
   *
   * <p>A {@link Connection} that is in autocommit and read/write mode will allow all types of
   * statements: Queries, DML, DDL, and Mutations (writes). If the connection is in read-only mode,
   * only queries will be allowed.
   *
   * <p>{@link Connection}s in autocommit mode may also accept partitioned DML statements. See
   * {@link Connection#setAutocommitDmlMode(AutocommitDmlMode)} for more information.
   *
   * @param autocommit true/false to turn autocommit on/off
   */
  void setAutocommit(boolean autocommit);

  /**
   * @return <code>true</code> if this connection is in autocommit mode
   */
  boolean isAutocommit();

  /**
   * Sets this connection to read-only or read-write. This method may only be called when no
   * transaction is active. A connection that is in read-only mode, will never allow any kind of
   * changes to the database to be submitted.
   *
   * @param readOnly true/false to turn read-only mode on/off
   */
  void setReadOnly(boolean readOnly);

  /**
   * @return <code>true</code> if this connection is in read-only mode
   */
  boolean isReadOnly();

  /** Sets the default isolation level for read/write transactions for this connection. */
  void setDefaultIsolationLevel(IsolationLevel isolationLevel);

  /** Returns the default isolation level for read/write transactions for this connection. */
  IsolationLevel getDefaultIsolationLevel();

  /**
   * Sets the duration the connection should wait before automatically aborting the execution of a
   * statement. The default is no timeout. Statement timeouts are applied all types of statements,
   * both in autocommit and transactional mode. They also apply to {@link Connection#commit()} and
   * {@link Connection#rollback()} statements.
   *
   * <p>A DML statement in autocommit mode may or may not have actually been applied to the
   * database, depending on when the timeout occurred.
   *
   * <p>A DML statement in a transaction that times out may still have been applied to the
   * transaction. If you still decide to commit the transaction after such a timeout, the DML
   * statement may or may not have been part of the transaction, depending on whether the timeout
   * occurred before or after the statement was (successfully) sent to Spanner. You should therefore
   * either always rollback a transaction that had a DML statement that timed out, or you should
   * accept that the timed out statement still might have been applied to the database.
   *
   * <p>DDL statements and DML statements in {@link AutocommitDmlMode#PARTITIONED_NON_ATOMIC} mode
   * cannot be rolled back. If such a statement times out, it may or may not have been applied to
   * the database. The same applies to commit and rollback statements.
   *
   * <p>Statements that time out will throw a {@link SpannerException} with error code {@link
   * ErrorCode#DEADLINE_EXCEEDED}.
   *
   * @param timeout The number of {@link TimeUnit}s before a statement is automatically aborted by
   *     the connection. Zero or negative values are not allowed. The maximum allowed value is
   *     315,576,000,000 seconds. Use {@link Connection#clearStatementTimeout()} to remove a timeout
   *     value that has been set.
   * @param unit The {@link TimeUnit} to specify the timeout value in. Must be one of {@link
   *     TimeUnit#NANOSECONDS}, {@link TimeUnit#MICROSECONDS}, {@link TimeUnit#MILLISECONDS}, {@link
   *     TimeUnit#SECONDS}.
   */
  void setStatementTimeout(long timeout, TimeUnit unit);

  /**
   * Clears the statement timeout value for this connection. This is a no-op if there is currently
   * no statement timeout set on this connection.
   */
  void clearStatementTimeout();

  /**
   * @param unit The {@link TimeUnit} to get the timeout value in. Must be one of {@link
   *     TimeUnit#NANOSECONDS}, {@link TimeUnit#MICROSECONDS}, {@link TimeUnit#MILLISECONDS}, {@link
   *     TimeUnit#SECONDS}
   * @return the current statement timeout value or 0 if no timeout value has been set.
   */
  long getStatementTimeout(TimeUnit unit);

  /**
   * @return <code>true</code> if this {@link Connection} has a statement timeout value.
   */
  boolean hasStatementTimeout();

  /**
   * Cancels the currently running statement on this {@link Connection} (if any). If canceling the
   * statement execution succeeds, the statement will be terminated and a {@link SpannerException}
   * with code {@link ErrorCode#CANCELLED} will be thrown. The result of the statement will be the
   * same as when a statement times out (see {@link Connection#setStatementTimeout(long, TimeUnit)}
   * for more information).
   *
   * <p>Canceling a DDL statement in autocommit mode or a RUN BATCH statement of a DDL batch will
   * cause the connection to try to cancel the execution of the DDL statement(s). This is not
   * guaranteed to cancel the execution of the statement(s) on Cloud Spanner. See
   * https://cloud.google.com/spanner/docs/reference/rpc/google.longrunning#google.longrunning.Operations.CancelOperation
   * for more information.
   *
   * <p>Canceling a DML statement that is running in {@link
   * AutocommitDmlMode#PARTITIONED_NON_ATOMIC} mode will not cancel a statement on Cloud Spanner
   * that is already being executed, and its effects will still be applied to the database.
   */
  void cancel();

  /**
   * Begins a new transaction for this connection. The transaction will use the default isolation
   * level of this connection.
   *
   * <ul>
   *   <li>Calling this method on a connection that has no transaction and that is
   *       <strong>not</strong> in autocommit mode, will register a new transaction that has not yet
   *       started on this connection
   *   <li>Calling this method on a connection that has no transaction and that <strong>is</strong>
   *       in autocommit mode, will register a new transaction that has not yet started on this
   *       connection, and temporarily turn off autocommit mode until the next commit/rollback
   *   <li>Calling this method on a connection that already has a transaction that has not yet
   *       started, will cause a {@link SpannerException}
   *   <li>Calling this method on a connection that already has a transaction that has started, will
   *       cause a {@link SpannerException} (no nested transactions)
   * </ul>
   */
  void beginTransaction();

  /**
   * Same as {@link #beginTransaction()}, but this transaction will use the given isolation level,
   * instead of the default isolation level of this connection.
   */
  void beginTransaction(IsolationLevel isolationLevel);

  /**
   * Begins a new transaction for this connection. This method is guaranteed to be non-blocking. The
   * returned {@link ApiFuture} will be done when the transaction has been initialized. The
   * transaction will use the default isolation level of this connection.
   *
   * <ul>
   *   <li>Calling this method on a connection that has no transaction and that is
   *       <strong>not</strong> in autocommit mode, will register a new transaction that has not yet
   *       started on this connection
   *   <li>Calling this method on a connection that has no transaction and that <strong>is</strong>
   *       in autocommit mode, will register a new transaction that has not yet started on this
   *       connection, and temporarily turn off autocommit mode until the next commit/rollback
   *   <li>Calling this method on a connection that already has a transaction that has not yet
   *       started, will cause a {@link SpannerException}
   *   <li>Calling this method on a connection that already has a transaction that has started, will
   *       cause a {@link SpannerException} (no nested transactions)
   * </ul>
   */
  ApiFuture<Void> beginTransactionAsync();

  /**
   * Same as {@link #beginTransactionAsync()}, but this transaction will use the given isolation
   * level, instead of the default isolation level of this connection.
   */
  ApiFuture<Void> beginTransactionAsync(IsolationLevel isolationLevel);

  /**
   * Sets the transaction mode to use for current transaction. This method may only be called when
   * in a transaction, and before the transaction is actually started, i.e. before any statements
   * have been executed in the transaction.
   *
   * @param transactionMode The transaction mode to use for the current transaction.
   *     <ul>
   *       <li>{@link TransactionMode#READ_ONLY_TRANSACTION} will create a read-only transaction and
   *           prevent any changes to written to the database through this transaction. The read
   *           timestamp to be used will be determined based on the current readOnlyStaleness
   *           setting of this connection. It is recommended to use {@link
   *           TransactionMode#READ_ONLY_TRANSACTION} instead of {@link
   *           TransactionMode#READ_WRITE_TRANSACTION} when possible, as read-only transactions do
   *           not acquire locks on Cloud Spanner, and read-only transactions never abort.
   *       <li>{@link TransactionMode#READ_WRITE_TRANSACTION} this value is only allowed when the
   *           connection is not in read-only mode and will create a read-write transaction. If
   *           {@link Connection#isRetryAbortsInternally()} is <code>true</code>, each read/write
   *           transaction will keep track of a running SHA256 checksum for each {@link ResultSet}
   *           that is returned in order to be able to retry the transaction in case the transaction
   *           is aborted by Spanner.
   *     </ul>
   */
  void setTransactionMode(TransactionMode transactionMode);

  /**
   * @return the transaction mode of the current transaction. This method may only be called when
   *     the connection is in a transaction.
   */
  TransactionMode getTransactionMode();

  /**
   * Sets the transaction tag to use for the current transaction. This method may only be called
   * when in a transaction and before any statements have been executed in the transaction.
   *
   * <p>The tag will be set as the transaction tag of all statements during the transaction, and as
   * the transaction tag of the commit.
   *
   * <p>The transaction tag will automatically be cleared after the transaction has ended.
   *
   * @param tag The tag to use.
   */
  default void setTransactionTag(String tag) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The transaction tag of the current transaction.
   */
  default String getTransactionTag() {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets the statement tag to use for the next statement that is executed. The tag is automatically
   * cleared after the statement is executed. Statement tags can be used both with autocommit=true
   * and autocommit=false, and can be used for partitioned DML.
   *
   * <p>Statement tags are not allowed before COMMIT and ROLLBACK statements.
   *
   * <p>Statement tags are allowed before START BATCH DML statements and will be included in the
   * {@link ExecuteBatchDmlRequest} that is sent to Spanner. Statement tags are not allowed inside a
   * batch.
   *
   * @param tag The statement tag to use with the next statement that will be executed on this
   *     connection.
   */
  default void setStatementTag(String tag) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The statement tag that will be used with the next statement that is executed on this
   *     connection.
   */
  default String getStatementTag() {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets whether the next transaction should be excluded from all change streams with the DDL
   * option `allow_txn_exclusion=true`
   */
  default void setExcludeTxnFromChangeStreams(boolean excludeTxnFromChangeStreams) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns true if the next transaction should be excluded from all change streams with the DDL
   * option `allow_txn_exclusion=true`
   */
  default boolean isExcludeTxnFromChangeStreams() {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets the proto descriptors to use for the next DDL statement (single or batch) that will be
   * executed. The proto descriptor is automatically cleared after the statement is executed.
   *
   * @param protoDescriptors The proto descriptors to use with the next DDL statement (single or
   *     batch) that will be executed on this connection.
   */
  default void setProtoDescriptors(@Nonnull byte[] protoDescriptors) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The proto descriptor that will be used with the next DDL statement (single or batch)
   *     that is executed on this connection.
   */
  default byte[] getProtoDescriptors() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return <code>true</code> if this connection will automatically retry read/write transactions
   *     that abort. This method may only be called when the connection is in read/write
   *     transactional mode and no transaction has been started yet.
   */
  boolean isRetryAbortsInternally();

  /**
   * Sets whether this connection will internally retry read/write transactions that abort. The
   * default is <code>true</code>. When internal retry is enabled, the {@link Connection} will keep
   * track of a running SHA256 checksum of all {@link ResultSet}s that have been returned from Cloud
   * Spanner. If the checksum that is calculated during an internal retry differs from the original
   * checksum, the transaction will abort with an {@link
   * AbortedDueToConcurrentModificationException}.
   *
   * <p>Note that retries of a read/write transaction that calls a non-deterministic function on
   * Cloud Spanner, such as CURRENT_TIMESTAMP(), will never be successful, as the data returned
   * during the retry will always be different from the original transaction.
   *
   * <p>It is also highly recommended that all queries in a read/write transaction have an ORDER BY
   * clause that guarantees that the data is returned in the same order as in the original
   * transaction if the transaction is internally retried. The most efficient way to achieve this is
   * to always include the primary key columns at the end of the ORDER BY clause.
   *
   * <p>This method may only be called when the connection is in read/write transactional mode and
   * no transaction has been started yet.
   *
   * @param retryAbortsInternally Set to <code>true</code> to internally retry transactions that are
   *     aborted by Spanner. When set to <code>false</code>, any database call on a transaction that
   *     has been aborted by Cloud Spanner will throw an {@link AbortedException} instead of being
   *     retried. Set this to false if your application already uses retry loops to handle {@link
   *     AbortedException}s.
   */
  void setRetryAbortsInternally(boolean retryAbortsInternally);

  /**
   * Add a {@link TransactionRetryListener} to this {@link Connection} for testing and logging
   * purposes. The method {@link TransactionRetryListener#retryStarting(Timestamp, long, int)} will
   * be called before an automatic retry is started for a read/write transaction on this connection.
   * The method {@link TransactionRetryListener#retryFinished(Timestamp, long, int,
   * TransactionRetryListener.RetryResult)} will be called after the retry has finished.
   *
   * @param listener The listener to add to this connection.
   */
  void addTransactionRetryListener(TransactionRetryListener listener);

  /**
   * Removes one existing {@link TransactionRetryListener} from this {@link Connection}, if it is
   * present (optional operation).
   *
   * @param listener The listener to remove from the connection.
   * @return <code>true</code> if a listener was removed from the connection.
   */
  boolean removeTransactionRetryListener(TransactionRetryListener listener);

  /**
   * @return an unmodifiable iterator of the {@link TransactionRetryListener}s registered for this
   *     connection.
   */
  Iterator<TransactionRetryListener> getTransactionRetryListeners();

  /**
   * Sets the mode for executing DML statements in autocommit mode for this connection. This setting
   * is only used when the connection is in autocommit mode, and may only be set while the
   * transaction is in autocommit mode and not in a temporary transaction. The autocommit
   * transaction mode is reset to its default value of {@link AutocommitDmlMode#TRANSACTIONAL} when
   * autocommit mode is changed on the connection.
   *
   * @param mode The DML autocommit mode to use
   *     <ul>
   *       <li>{@link AutocommitDmlMode#TRANSACTIONAL} DML statements are executed as single
   *           read-write transaction. After successful execution, the DML statement is guaranteed
   *           to have been applied exactly once to the database
   *       <li>{@link AutocommitDmlMode#PARTITIONED_NON_ATOMIC} DML statements are executed as
   *           partitioned DML transactions. If an error occurs during the execution of the DML
   *           statement, it is possible that the statement has been applied to some but not all of
   *           the rows specified in the statement.
   *     </ul>
   */
  void setAutocommitDmlMode(AutocommitDmlMode mode);

  /**
   * @return the current {@link AutocommitDmlMode} setting for this connection. This method may only
   *     be called on a connection that is in autocommit mode and not while in a temporary
   *     transaction.
   */
  AutocommitDmlMode getAutocommitDmlMode();

  /**
   * Sets the staleness to use for the current read-only transaction. This method may only be called
   * when the transaction mode of the current transaction is {@link
   * TransactionMode#READ_ONLY_TRANSACTION} and there is no transaction that has started, or when
   * the connection is in read-only and autocommit mode.
   *
   * @param staleness The staleness to use for the current but not yet started read-only transaction
   */
  void setReadOnlyStaleness(TimestampBound staleness);

  /**
   * @return the read-only staleness setting for the current read-only transaction. This method may
   *     only be called when the current transaction is a read-only transaction, or when the
   *     connection is in read-only and autocommit mode.
   */
  TimestampBound getReadOnlyStaleness();

  /**
   * Sets the {@link DirectedReadOptions} to use for both single-use and multi-use read-only
   * transactions on this connection.
   */
  default void setDirectedRead(DirectedReadOptions directedReadOptions) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Returns the {@link DirectedReadOptions} that are used for both single-use and multi-use
   * read-only transactions on this connection.
   */
  default DirectedReadOptions getDirectedRead() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Sets the query optimizer version to use for this connection.
   *
   * @param optimizerVersion The query optimizer version to use. Must be a valid optimizer version
   *     number, the string <code>LATEST</code> or an empty string. The empty string will instruct
   *     the connection to use the optimizer version that is defined in the environment variable
   *     <code>SPANNER_OPTIMIZER_VERSION</code>. If no value is specified in the environment
   *     variable, the default query optimizer of Cloud Spanner is used.
   */
  void setOptimizerVersion(String optimizerVersion);

  /**
   * Gets the current query optimizer version of this connection.
   *
   * @return The query optimizer version that is currently used by this connection.
   */
  String getOptimizerVersion();

  /**
   * Sets the query optimizer statistics package
   *
   * @param optimizerStatisticsPackage The query optimizer statistics package to use. Must be a
   *     string composed of letters, numbers, dashes and underscores or an empty string. The empty
   *     string will instruct the connection to use the optimizer statistics package that is defined
   *     the environment variable <code>SPANNER_OPTIMIZER_STATISTICS_PACKAGE</code>. If no value is
   *     specified in the environment variable, the client level query optimizer is used. If none is
   *     set, the default query optimizer of Cloud Spanner is used.
   */
  default void setOptimizerStatisticsPackage(String optimizerStatisticsPackage) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Gets the current query optimizer statistics package of this connection.
   *
   * @return The query optimizer statistics package that is currently used by this connection.
   */
  default String getOptimizerStatisticsPackage() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Sets whether this connection should request commit statistics from Cloud Spanner for read/write
   * transactions and DML statements in autocommit mode.
   */
  void setReturnCommitStats(boolean returnCommitStats);

  /**
   * @return true if this connection requests commit statistics from Cloud Spanner
   */
  boolean isReturnCommitStats();

  /** Sets the max_commit_delay that will be applied to commit requests from this connection. */
  default void setMaxCommitDelay(Duration maxCommitDelay) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /** Returns the max_commit_delay that will be applied to commit requests from this connection. */
  default Duration getMaxCommitDelay() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Sets the priority to use for RPCs executed by this connection..
   *
   * @param rpcPriority The RPC priority to use.
   *     <ul>
   *       <li>{@link RpcPriority#HIGH} This specifies that the RPC's invocation will be of high
   *           priority.
   *       <li>{@link RpcPriority#MEDIUM} This specifies that the RPC's invocation will be of medium
   *           priority.
   *       <li>{@link RpcPriority#LOW} This specifies that the RPC's invocation will be of low
   *           priority.
   *     </ul>
   */
  default void setRPCPriority(RpcPriority rpcPriority) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Gets the current RPC priority of this connection.
   *
   * @return The RPC priority that is currently used by this connection.
   */
  default RpcPriority getRPCPriority() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Sets whether this connection should delay the actual start of a read/write transaction until
   * the first write operation is observed on that transaction. All read operations that are
   * executed before the first write operation in the transaction will be executed as if the
   * connection was in auto-commit mode. This can reduce locking, especially for transactions that
   * execute a large number of reads before any writes, at the expense of a lower transaction
   * isolation.
   *
   * <p>NOTE: This will make read/write transactions non-serializable.
   */
  default void setDelayTransactionStartUntilFirstWrite(
      boolean delayTransactionStartUntilFirstWrite) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * @return true if this connection delays the actual start of a read/write transaction until the
   *     first write operation on that transaction.
   */
  default boolean isDelayTransactionStartUntilFirstWrite() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Sets whether this connection should keep read/write transactions alive by executing a SELECT 1
   * once every 10 seconds during inactive read/write transactions.
   *
   * <p>NOTE: This will keep read/write transactions alive and hold on to locks until it is
   * explicitly committed or rolled back.
   */
  default void setKeepTransactionAlive(boolean keepTransactionAlive) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * @return true if this connection keeps read/write transactions alive by executing a SELECT 1
   *     once every 10 seconds during inactive read/write transactions.
   */
  default boolean isKeepTransactionAlive() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  /**
   * Commits the current transaction of this connection. All mutations that have been buffered
   * during the current transaction will be written to the database.
   *
   * <p>If the connection is in autocommit mode, and there is a temporary transaction active on this
   * connection, calling this method will cause the connection to go back to autocommit mode after
   * calling this method.
   *
   * <p>This method will throw a {@link SpannerException} with code {@link
   * ErrorCode#DEADLINE_EXCEEDED} if a statement timeout has been set on this connection, and the
   * commit operation takes longer than this timeout.
   *
   * <ul>
   *   <li>Calling this method on a connection in autocommit mode and with no temporary transaction,
   *       will cause an exception
   *   <li>Calling this method while a DDL batch is active will cause an exception
   *   <li>Calling this method on a connection with a transaction that has not yet started, will end
   *       that transaction and any properties that might have been set on that transaction, and
   *       return the connection to its previous state. This means that if a transaction is created
   *       and set to read-only, and then committed before any statements have been executed, the
   *       read-only transaction is ended and any subsequent statements will be executed in a new
   *       transaction. If the connection is in read-write mode, the default for new transactions
   *       will be {@link TransactionMode#READ_WRITE_TRANSACTION}. Committing an empty transaction
   *       also does not generate a read timestamp or a commit timestamp, and calling one of the
   *       methods {@link Connection#getReadTimestamp()} or {@link Connection#getCommitTimestamp()}
   *       will cause an exception.
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_ONLY_TRANSACTION}
   *       transaction will end that transaction. If the connection is in read-write mode, any
   *       subsequent transaction will by default be a {@link
   *       TransactionMode#READ_WRITE_TRANSACTION} transaction, unless any following transaction is
   *       explicitly set to {@link TransactionMode#READ_ONLY_TRANSACTION}
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_WRITE_TRANSACTION}
   *       transaction will send all buffered mutations to the database, commit any DML statements
   *       that have been executed during this transaction and end the transaction.
   * </ul>
   */
  void commit();

  /**
   * Commits the current transaction of this connection. All mutations that have been buffered
   * during the current transaction will be written to the database.
   *
   * <p>This method is guaranteed to be non-blocking. The returned {@link ApiFuture} will be done
   * when the transaction has committed or the commit has failed.
   *
   * <p>Calling this method will always end the current transaction and start a new transaction when
   * the next statement is executed, regardless whether this commit call succeeded or failed. If the
   * next statement(s) rely on the results of the transaction that is being committed, it is
   * recommended to check the status of this commit by inspecting the value of the returned {@link
   * ApiFuture} before executing the next statement, to ensure that the commit actually succeeded.
   *
   * <p>If the connection is in autocommit mode, and there is a temporary transaction active on this
   * connection, calling this method will cause the connection to go back to autocommit mode after
   * calling this method.
   *
   * <p>This method will throw a {@link SpannerException} with code {@link
   * ErrorCode#DEADLINE_EXCEEDED} if a statement timeout has been set on this connection, and the
   * commit operation takes longer than this timeout.
   *
   * <ul>
   *   <li>Calling this method on a connection in autocommit mode and with no temporary transaction,
   *       will cause an exception
   *   <li>Calling this method while a DDL batch is active will cause an exception
   *   <li>Calling this method on a connection with a transaction that has not yet started, will end
   *       that transaction and any properties that might have been set on that transaction, and
   *       return the connection to its previous state. This means that if a transaction is created
   *       and set to read-only, and then committed before any statements have been executed, the
   *       read-only transaction is ended and any subsequent statements will be executed in a new
   *       transaction. If the connection is in read-write mode, the default for new transactions
   *       will be {@link TransactionMode#READ_WRITE_TRANSACTION}. Committing an empty transaction
   *       also does not generate a read timestamp or a commit timestamp, and calling one of the
   *       methods {@link Connection#getReadTimestamp()} or {@link Connection#getCommitTimestamp()}
   *       will cause an exception.
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_ONLY_TRANSACTION}
   *       transaction will end that transaction. If the connection is in read-write mode, any
   *       subsequent transaction will by default be a {@link
   *       TransactionMode#READ_WRITE_TRANSACTION} transaction, unless any following transaction is
   *       explicitly set to {@link TransactionMode#READ_ONLY_TRANSACTION}
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_WRITE_TRANSACTION}
   *       transaction will send all buffered mutations to the database, commit any DML statements
   *       that have been executed during this transaction and end the transaction.
   * </ul>
   */
  ApiFuture<Void> commitAsync();

  /**
   * Rollbacks the current transaction of this connection. All mutations or DDL statements that have
   * been buffered during the current transaction will be removed from the buffer.
   *
   * <p>If the connection is in autocommit mode, and there is a temporary transaction active on this
   * connection, calling this method will cause the connection to go back to autocommit mode after
   * calling this method.
   *
   * <ul>
   *   <li>Calling this method on a connection in autocommit mode and with no temporary transaction
   *       will cause an exception
   *   <li>Calling this method while a DDL batch is active will cause an exception
   *   <li>Calling this method on a connection with a transaction that has not yet started, will end
   *       that transaction and any properties that might have been set on that transaction, and
   *       return the connection to its previous state. This means that if a transaction is created
   *       and set to read-only, and then rolled back before any statements have been executed, the
   *       read-only transaction is ended and any subsequent statements will be executed in a new
   *       transaction. If the connection is in read-write mode, the default for new transactions
   *       will be {@link TransactionMode#READ_WRITE_TRANSACTION}.
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_ONLY_TRANSACTION}
   *       transaction will end that transaction. If the connection is in read-write mode, any
   *       subsequent transaction will by default be a {@link
   *       TransactionMode#READ_WRITE_TRANSACTION} transaction, unless any following transaction is
   *       explicitly set to {@link TransactionMode#READ_ONLY_TRANSACTION}
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_WRITE_TRANSACTION}
   *       transaction will clear all buffered mutations, rollback any DML statements that have been
   *       executed during this transaction and end the transaction.
   * </ul>
   */
  void rollback();

  /**
   * Rollbacks the current transaction of this connection. All mutations or DDL statements that have
   * been buffered during the current transaction will be removed from the buffer.
   *
   * <p>This method is guaranteed to be non-blocking. The returned {@link ApiFuture} will be done
   * when the transaction has been rolled back.
   *
   * <p>If the connection is in autocommit mode, and there is a temporary transaction active on this
   * connection, calling this method will cause the connection to go back to autocommit mode after
   * calling this method.
   *
   * <ul>
   *   <li>Calling this method on a connection in autocommit mode and with no temporary transaction
   *       will cause an exception
   *   <li>Calling this method while a DDL batch is active will cause an exception
   *   <li>Calling this method on a connection with a transaction that has not yet started, will end
   *       that transaction and any properties that might have been set on that transaction, and
   *       return the connection to its previous state. This means that if a transaction is created
   *       and set to read-only, and then rolled back before any statements have been executed, the
   *       read-only transaction is ended and any subsequent statements will be executed in a new
   *       transaction. If the connection is in read-write mode, the default for new transactions
   *       will be {@link TransactionMode#READ_WRITE_TRANSACTION}.
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_ONLY_TRANSACTION}
   *       transaction will end that transaction. If the connection is in read-write mode, any
   *       subsequent transaction will by default be a {@link
   *       TransactionMode#READ_WRITE_TRANSACTION} transaction, unless any following transaction is
   *       explicitly set to {@link TransactionMode#READ_ONLY_TRANSACTION}
   *   <li>Calling this method on a connection with a {@link TransactionMode#READ_WRITE_TRANSACTION}
   *       transaction will clear all buffered mutations, rollback any DML statements that have been
   *       executed during this transaction and end the transaction.
   * </ul>
   */
  ApiFuture<Void> rollbackAsync();

  /** Functional interface for the {@link #runTransaction(TransactionCallable)} method. */
  interface TransactionCallable<T> {
    /** This method is invoked with a fresh transaction on the connection. */
    T run(Connection transaction);
  }

  /**
   * Runs the given callable in a transaction. The transaction type is determined by the current
   * state of the connection. That is; if the connection is in read/write mode, the transaction type
   * will be a read/write transaction. If the connection is in read-only mode, it will be a
   * read-only transaction. The transaction will automatically be retried if it is aborted by
   * Spanner.
   */
  <T> T runTransaction(TransactionCallable<T> callable);

  /** Returns the current savepoint support for this connection. */
  SavepointSupport getSavepointSupport();

  /** Sets how savepoints should be supported on this connection. */
  void setSavepointSupport(SavepointSupport savepointSupport);

  /** Returns the current {@link DdlInTransactionMode} for this connection. */
  DdlInTransactionMode getDdlInTransactionMode();

  /** Sets how the connection should behave if a DDL statement is executed during a transaction. */
  void setDdlInTransactionMode(DdlInTransactionMode ddlInTransactionMode);

  /**
   * Returns the default sequence kind that will be set for this database if a DDL statement is
   * executed that uses auto_increment or serial.
   */
  String getDefaultSequenceKind();

  /**
   * Sets the default sequence kind that will be set for this database if a DDL statement is
   * executed that uses auto_increment or serial.
   */
  void setDefaultSequenceKind(String defaultSequenceKind);

  /**
   * Creates a savepoint with the given name.
   *
   * <p>The uniqueness constraints on a savepoint name depends on the database dialect that is used:
   *
   * <ul>
   *   <li>{@link Dialect#GOOGLE_STANDARD_SQL} requires that savepoint names are unique within a
   *       transaction. The name of a savepoint that has been released or destroyed because the
   *       transaction has rolled back to a savepoint that was defined before that savepoint can be
   *       re-used within the transaction.
   *   <li>{@link Dialect#POSTGRESQL} follows the rules for savepoint names in PostgreSQL. This
   *       means that multiple savepoints in one transaction can have the same name, but only the
   *       last savepoint with a given name is visible. See <a
   *       href="https://www.postgresql.org/docs/current/sql-savepoint.html">PostgreSQL savepoint
   *       documentation</a> for more information.
   * </ul>
   *
   * @param name the name of the savepoint to create
   * @throws SpannerException if a savepoint with the same name already exists and the dialect that
   *     is used is {@link Dialect#GOOGLE_STANDARD_SQL}
   * @throws SpannerException if there is no transaction on this connection
   * @throws SpannerException if internal retries have been disabled for this connection
   */
  void savepoint(String name);

  /**
   * Releases the savepoint with the given name. The savepoint and all later savepoints will be
   * removed from the current transaction and can no longer be used.
   *
   * @param name the name of the savepoint to release
   * @throws SpannerException if no savepoint with the given name exists
   */
  void releaseSavepoint(String name);

  /**
   * Rolls back to the given savepoint. Rolling back to a savepoint undoes all changes and releases
   * all internal locks that have been taken by the transaction after the savepoint. Rolling back to
   * a savepoint does not remove the savepoint from the transaction, and it is possible to roll back
   * to the same savepoint multiple times. All savepoints that have been defined after the given
   * savepoint are removed from the transaction.
   *
   * @param name the name of the savepoint to roll back to.
   * @throws SpannerException if no savepoint with the given name exists.
   * @throws AbortedDueToConcurrentModificationException if rolling back to the savepoint failed
   *     because another transaction has modified the data that has been read or modified by this
   *     transaction
   */
  void rollbackToSavepoint(String name);

  /**
   * @return <code>true</code> if this connection has a transaction (that has not necessarily
   *     started). This method will only return false when the {@link Connection} is in autocommit
   *     mode and no explicit transaction has been started by calling {@link
   *     Connection#beginTransaction()}. If the {@link Connection} is not in autocommit mode, there
   *     will always be a transaction.
   */
  boolean isInTransaction();

  /**
   * @return <code>true</code> if this connection has a transaction that has started. A transaction
   *     is automatically started by the first statement that is executed in the transaction.
   */
  boolean isTransactionStarted();

  /**
   * Returns the read timestamp of the current/last {@link TransactionMode#READ_ONLY_TRANSACTION}
   * transaction, or the read timestamp of the last query in autocommit mode.
   *
   * <ul>
   *   <li>When in autocommit mode: The method will return the read timestamp of the last statement
   *       if the last statement was a query.
   *   <li>When in a {@link TransactionMode#READ_ONLY_TRANSACTION} transaction that has started (a
   *       query has been executed), or that has just committed: The read timestamp of the
   *       transaction. If the read-only transaction was committed without ever executing a query,
   *       calling this method after the commit will also throw a {@link SpannerException}
   *   <li>In all other cases the method will throw a {@link SpannerException}.
   * </ul>
   *
   * @return the read timestamp of the current/last read-only transaction.
   */
  Timestamp getReadTimestamp();

  /**
   * @return the commit timestamp of the last {@link TransactionMode#READ_WRITE_TRANSACTION}
   *     transaction. This method throws a {@link SpannerException} if there is no last {@link
   *     TransactionMode#READ_WRITE_TRANSACTION} transaction. That is, if the last transaction was a
   *     {@link TransactionMode#READ_ONLY_TRANSACTION}), or if the last {@link
   *     TransactionMode#READ_WRITE_TRANSACTION} transaction rolled back. It also throws a {@link
   *     SpannerException} if the last {@link TransactionMode#READ_WRITE_TRANSACTION} transaction
   *     was empty when committed.
   */
  Timestamp getCommitTimestamp();

  /**
   * @return the {@link CommitResponse} of the last {@link TransactionMode#READ_WRITE_TRANSACTION}
   *     transaction. This method throws a {@link SpannerException} if there is no last {@link
   *     TransactionMode#READ_WRITE_TRANSACTION} transaction. That is, if the last transaction was a
   *     {@link TransactionMode#READ_ONLY_TRANSACTION}), or if the last {@link
   *     TransactionMode#READ_WRITE_TRANSACTION} transaction rolled back. It also throws a {@link
   *     SpannerException} if the last {@link TransactionMode#READ_WRITE_TRANSACTION} transaction
   *     was empty when committed.
   */
  CommitResponse getCommitResponse();

  /**
   * Starts a new DDL batch on this connection. A DDL batch allows several DDL statements to be
   * grouped into a batch that can be executed as a group. DDL statements that are issued during the
   * batch are buffered locally and will return immediately with an OK. It is not guaranteed that a
   * DDL statement that has been issued during a batch will eventually succeed when running the
   * batch. Aborting a DDL batch will clear the DDL buffer and will have made no changes to the
   * database. Running a DDL batch will send all buffered DDL statements to Spanner, and Spanner
   * will try to execute these. The result will be OK if all the statements executed successfully.
   * If a statement cannot be executed, Spanner will stop execution at that point and return an
   * error message for the statement that could not be executed. Preceding statements of the batch
   * may have been executed.
   *
   * <p>This method may only be called when the connection is in read/write mode, autocommit mode is
   * enabled or no read/write transaction has been started, and there is not already another batch
   * active. The connection will only accept DDL statements while a DDL batch is active.
   */
  void startBatchDdl();

  /**
   * Starts a new DML batch on this connection. A DML batch allows several DML statements to be
   * grouped into a batch that can be executed as a group. DML statements that are issued during the
   * batch are buffered locally and will return immediately with an OK. It is not guaranteed that a
   * DML statement that has been issued during a batch will eventually succeed when running the
   * batch. Aborting a DML batch will clear the DML buffer and will have made no changes to the
   * database. Running a DML batch will send all buffered DML statements to Spanner, and Spanner
   * will try to execute these. The result will be OK if all the statements executed successfully.
   * If a statement cannot be executed, Spanner will stop execution at that point and return {@link
   * SpannerBatchUpdateException} for the statement that could not be executed. Preceding statements
   * of the batch will have been executed, and the update counts of those statements can be
   * retrieved through {@link SpannerBatchUpdateException#getUpdateCounts()}.
   *
   * <p>This method may only be called when the connection is in read/write mode, autocommit mode is
   * enabled or no read/write transaction has been started, and there is not already another batch
   * active. The connection will only accept DML statements while a DML batch is active.
   */
  void startBatchDml();

  /**
   * Sends all buffered DML or DDL statements of the current batch to the database, waits for these
   * to be executed and ends the current batch. The method will throw an exception for the first
   * statement that cannot be executed, or return successfully if all statements could be executed.
   * If an exception is thrown for a statement in the batch, the preceding statements in the same
   * batch may still have been applied to the database.
   *
   * <p>This method may only be called when a (possibly empty) batch is active.
   *
   * @return the update counts in case of a DML batch. Returns an array containing 1 for each
   *     successful statement and 0 for each failed statement or statement that was not executed in
   *     case of a DDL batch.
   */
  long[] runBatch();

  /**
   * Sends all buffered DML or DDL statements of the current batch to the database, waits for these
   * to be executed and ends the current batch. The method will throw an exception for the first
   * statement that cannot be executed, or return successfully if all statements could be executed.
   * If an exception is thrown for a statement in the batch, the preceding statements in the same
   * batch may still have been applied to the database.
   *
   * <p>This method is guaranteed to be non-blocking. The returned {@link ApiFuture} will be done
   * when the batch has been successfully applied, or when one or more of the statements in the
   * batch has failed and the further execution of the batch has been halted.
   *
   * <p>This method may only be called when a (possibly empty) batch is active.
   *
   * @return an {@link ApiFuture} containing the update counts in case of a DML batch. The {@link
   *     ApiFuture} contains an array containing 1 for each successful statement and 0 for each
   *     failed statement or statement that was not executed in case of a DDL batch.
   */
  ApiFuture<long[]> runBatchAsync();

  /**
   * Clears all buffered statements in the current batch and ends the batch.
   *
   * <p>This method may only be called when a (possibly empty) batch is active.
   */
  void abortBatch();

  /**
   * @return <code>true</code> if a DDL batch is active on this connection.
   */
  boolean isDdlBatchActive();

  /**
   * @return <code>true</code> if a DML batch is active on this connection.
   */
  boolean isDmlBatchActive();

  /**
   * Executes the given statement if allowed in the current {@link TransactionMode} and connection
   * state. The returned value depends on the type of statement:
   *
   * <ul>
   *   <li>Queries and DML statements with returning clause will return a {@link ResultSet}.
   *   <li>Simple DML statements will return an update count
   *   <li>DDL statements will return a {@link ResultType#NO_RESULT}
   *   <li>Connection and transaction statements (SET AUTOCOMMIT=TRUE|FALSE, SHOW AUTOCOMMIT, SET
   *       TRANSACTION READ ONLY, etc) will return either a {@link ResultSet} or {@link
   *       ResultType#NO_RESULT}, depending on the type of statement (SHOW or SET)
   * </ul>
   *
   * @param statement The statement to execute
   * @return the result of the statement
   */
  StatementResult execute(Statement statement);

  /**
   * Executes the given statement if allowed in the current {@link TransactionMode} and connection
   * state, and if the result that would be returned is in the set of allowed result types. The
   * statement will not be sent to Cloud Spanner if the result type would not be allowed. This
   * method can be used by drivers that must limit the type of statements that are allowed for a
   * given method, e.g. for the {@link java.sql.Statement#executeQuery(String)} and {@link
   * java.sql.Statement#executeUpdate(String)} methods.
   *
   * <p>The returned value depends on the type of statement:
   *
   * <ul>
   *   <li>Queries and DML statements with returning clause will return a {@link ResultSet}.
   *   <li>Simple DML statements will return an update count
   *   <li>DDL statements will return a {@link ResultType#NO_RESULT}
   *   <li>Connection and transaction statements (SET AUTOCOMMIT=TRUE|FALSE, SHOW AUTOCOMMIT, SET
   *       TRANSACTION READ ONLY, etc) will return either a {@link ResultSet} or {@link
   *       ResultType#NO_RESULT}, depending on the type of statement (SHOW or SET)
   * </ul>
   *
   * @param statement The statement to execute
   * @param allowedResultTypes The result types that this method may return. The statement will not
   *     be sent to Cloud Spanner if the statement would return a result that is not one of the
   *     types in this set.
   * @return the result of the statement
   */
  default StatementResult execute(Statement statement, Set<ResultType> allowedResultTypes) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Executes the given statement if allowed in the current {@link TransactionMode} and connection
   * state asynchronously. The returned value depends on the type of statement:
   *
   * <ul>
   *   <li>Queries and DML statements with returning clause will return an {@link AsyncResultSet}.
   *   <li>Simple DML statements will return an {@link ApiFuture} with an update count that is done
   *       when the DML statement has been applied successfully, or that throws an {@link
   *       ExecutionException} if the DML statement failed.
   *   <li>DDL statements will return an {@link ApiFuture} containing a {@link Void} that is done
   *       when the DDL statement has been applied successfully, or that throws an {@link
   *       ExecutionException} if the DDL statement failed.
   *   <li>Connection and transaction statements (SET AUTOCOMMIT=TRUE|FALSE, SHOW AUTOCOMMIT, SET
   *       TRANSACTION READ ONLY, etc) will return either a {@link ResultSet} or {@link
   *       ResultType#NO_RESULT}, depending on the type of statement (SHOW or SET)
   * </ul>
   *
   * This method is guaranteed to be non-blocking.
   *
   * @param statement The statement to execute
   * @return the result of the statement
   */
  AsyncStatementResult executeAsync(Statement statement);

  /**
   * Executes the given statement (a query or a DML statement with returning clause) and returns the
   * result as a {@link ResultSet}. This method blocks and waits for a response from Spanner. If the
   * statement does not contain a valid query or a DML statement with returning clause, the method
   * will throw a {@link SpannerException}.
   *
   * @param query The query statement or DML statement with returning clause to execute
   * @param options the options to configure the query
   * @return a {@link ResultSet} with the results of the statement
   */
  ResultSet executeQuery(Statement query, QueryOption... options);

  /**
   * Executes the given statement (a query or a DML statement with returning clause) asynchronously
   * and returns the result as an {@link AsyncResultSet}. This method is guaranteed to be
   * non-blocking. If the statement does not contain a valid query or a DML statement with returning
   * clause, the method will throw a {@link SpannerException}.
   *
   * <p>See {@link AsyncResultSet#setCallback(java.util.concurrent.Executor,
   * com.google.cloud.spanner.AsyncResultSet.ReadyCallback)} for more information on how to consume
   * the results of the statement asynchronously.
   *
   * <p>It is also possible to consume the returned {@link AsyncResultSet} in the same way as a
   * normal {@link ResultSet}, i.e. in a while-loop calling {@link AsyncResultSet#next()}.
   *
   * @param query The query statement or DML statement with returning clause to execute
   * @param options the options to configure the query
   * @return an {@link AsyncResultSet} with the results of the statement
   */
  AsyncResultSet executeQueryAsync(Statement query, QueryOption... options);

  /**
   * Analyzes a query or a DML statement and returns query plan and/or query execution statistics
   * information.
   *
   * <p>The query plan and query statistics information is contained in {@link
   * com.google.spanner.v1.ResultSetStats} that can be accessed by calling {@link
   * ResultSet#getStats()} on the returned {@code ResultSet}.
   *
   * <pre>
   * <!--SNIPPET connection_analyze_query-->
   * {@code
   * ResultSet resultSet =
   *     connection.analyzeQuery(
   *         Statement.of("SELECT SingerId, AlbumId, MarketingBudget FROM Albums"),
   *         ReadContext.QueryAnalyzeMode.PROFILE);
   * while (resultSet.next()) {
   *   // Discard the results. We're only processing because getStats() below requires it.
   * }
   * ResultSetStats stats = resultSet.getStats();
   * }
   * <!--SNIPPET connection_analyze_query-->
   * </pre>
   *
   * @param query the query statement to execute
   * @param queryMode the mode in which to execute the query
   */
  ResultSet analyzeQuery(Statement query, QueryAnalyzeMode queryMode);

  /**
   * Enables or disables automatic batching of DML statements. When enabled, DML statements that are
   * executed on this connection will be buffered in memory instead of actually being executed. The
   * buffered DML statements are flushed to Spanner when a statement that cannot be part of a DML
   * batch is executed on the connection. This can be a query, a DDL statement with a THEN RETURN
   * clause, or a Commit call. The update count that is returned for DML statements that are
   * buffered is determined by the value that has been set with {@link
   * #setAutoBatchDmlUpdateCount(long)}. The default is 1. The connection verifies that the update
   * counts that were returned while buffering DML statements match the actual update counts that
   * are returned by Spanner when the batch is executed. This verification can be disabled by
   * calling {@link #setAutoBatchDmlUpdateCountVerification(boolean)}.
   */
  void setAutoBatchDml(boolean autoBatchDml);

  /** Returns whether automatic DML batching is enabled on this connection. */
  boolean isAutoBatchDml();

  /**
   * Sets the update count that is returned for DML statements that are buffered during an automatic
   * DML batch. This value is only used if {@link #isAutoBatchDml()} is enabled.
   */
  void setAutoBatchDmlUpdateCount(long updateCount);

  /**
   * Returns the update count that is returned for DML statements that are buffered during an
   * automatic DML batch.
   */
  long getAutoBatchDmlUpdateCount();

  /**
   * Sets whether the update count that is returned by Spanner after executing an automatic DML
   * batch should be verified against the update counts that were returned during the buffering of
   * those statements.
   */
  void setAutoBatchDmlUpdateCountVerification(boolean verification);

  /** Indicates whether the update counts of automatic DML batches should be verified. */
  boolean isAutoBatchDmlUpdateCountVerification();

  /**
   * Enable data boost for partitioned queries. See also {@link #partitionQuery(Statement,
   * PartitionOptions, QueryOption...)}
   */
  void setDataBoostEnabled(boolean dataBoostEnabled);

  /**
   * Returns whether data boost is enabled for partitioned queries. See also {@link
   * #partitionQuery(Statement, PartitionOptions, QueryOption...)}
   */
  boolean isDataBoostEnabled();

  /**
   * Sets whether this connection should always use partitioned queries when a query is executed on
   * this connection. Setting this flag to <code>true</code> and then executing a query that cannot
   * be partitioned, or executing a query in a read/write transaction, will cause an error. Use this
   * flag in combination with {@link #setDataBoostEnabled(boolean)} to force all queries on this
   * connection to use data boost.
   */
  void setAutoPartitionMode(boolean autoPartitionMode);

  /** Returns whether this connection will execute all queries as partitioned queries. */
  boolean isAutoPartitionMode();

  /**
   * Sets the maximum number of partitions that should be included as a hint to Cloud Spanner when
   * partitioning a query on this connection. Note that this is only a hint and Cloud Spanner might
   * choose to ignore the hint.
   */
  void setMaxPartitions(int maxPartitions);

  /**
   * Gets the maximum number of partitions that should be included as a hint to Cloud Spanner when
   * partitioning a query on this connection. Note that this is only a hint and Cloud Spanner might
   * choose to ignore the hint.
   */
  int getMaxPartitions();

  /**
   * Partitions the given query, so it can be executed in parallel. This method returns a {@link
   * ResultSet} with a string-representation of the partitions that were created. These strings can
   * be used to execute a partition either on this connection or an any other connection (on this
   * host or an any other host) by calling the method {@link #runPartition(String)}. This method
   * will automatically enable data boost for the query if {@link #isDataBoostEnabled()} returns
   * true.
   */
  ResultSet partitionQuery(
      Statement query, PartitionOptions partitionOptions, QueryOption... options);

  /**
   * Executes the given partition of a query. The encodedPartitionId should be a string that was
   * returned by {@link #partitionQuery(Statement, PartitionOptions, QueryOption...)}.
   */
  ResultSet runPartition(String encodedPartitionId);

  /**
   * Sets the maximum degree of parallelism that is used when executing a partitioned query using
   * {@link #runPartitionedQuery(Statement, PartitionOptions, QueryOption...)}. The method will use
   * up to <code>maxThreads</code> to execute and retrieve the results from Cloud Spanner. Set this
   * value to <code>0</code>> to use the number of available processors as returned by {@link
   * Runtime#availableProcessors()}.
   */
  void setMaxPartitionedParallelism(int maxThreads);

  /**
   * Returns the maximum degree of parallelism that is used for {@link
   * #runPartitionedQuery(Statement, PartitionOptions, QueryOption...)}
   */
  int getMaxPartitionedParallelism();

  /**
   * Executes the given query as a partitioned query. The query will first be partitioned using the
   * {@link #partitionQuery(Statement, PartitionOptions, QueryOption...)} method. Each of the
   * partitions will then be executed in the background, and the results will be merged into a
   * single result set.
   *
   * <p>This method will use <code>maxPartitionedParallelism</code> threads to execute the
   * partitioned query. Set this variable to a higher/lower value to increase/decrease the degree of
   * parallelism used for execution.
   */
  PartitionedQueryResultSet runPartitionedQuery(
      Statement query, PartitionOptions partitionOptions, QueryOption... options);

  /**
   * Executes the given statement as a simple DML statement. If the statement does not contain a
   * valid DML statement, the method will throw a {@link SpannerException}.
   *
   * @param update The update statement to execute
   * @return the number of records that were inserted/updated/deleted by this statement
   */
  long executeUpdate(Statement update);

  /**
   * Analyzes a DML statement and returns query plan and/or execution statistics information.
   *
   * <p>{@link com.google.cloud.spanner.ReadContext.QueryAnalyzeMode#PLAN} only returns the plan for
   * the statement. {@link com.google.cloud.spanner.ReadContext.QueryAnalyzeMode#PROFILE} executes
   * the DML statement, returns the modified row count and execution statistics, and the effects of
   * the DML statement will be visible to subsequent operations in the transaction.
   *
   * @deprecated Use {@link #analyzeUpdateStatement(Statement, QueryAnalyzeMode, UpdateOption...)}
   *     instead
   */
  @Deprecated
  default ResultSetStats analyzeUpdate(Statement update, QueryAnalyzeMode analyzeMode) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Analyzes a DML statement and returns execution plan, undeclared parameters and optionally
   * execution statistics information.
   *
   * <p>{@link com.google.cloud.spanner.ReadContext.QueryAnalyzeMode#PLAN} only returns the plan and
   * undeclared parameters for the statement. {@link
   * com.google.cloud.spanner.ReadContext.QueryAnalyzeMode#PROFILE} also executes the DML statement,
   * returns the modified row count and execution statistics, and the effects of the DML statement
   * will be visible to subsequent operations in the transaction.
   */
  default ResultSet analyzeUpdateStatement(
      Statement statement, QueryAnalyzeMode analyzeMode, UpdateOption... options) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Executes the given statement asynchronously as a simple DML statement. If the statement does
   * not contain a simple DML statement, the method will throw a {@link SpannerException}. A DML
   * statement with returning clause will throw a {@link SpannerException}.
   *
   * <p>This method is guaranteed to be non-blocking.
   *
   * @param update The update statement to execute
   * @return an {@link ApiFuture} containing the number of records that were
   *     inserted/updated/deleted by this statement
   */
  ApiFuture<Long> executeUpdateAsync(Statement update);

  /**
   * Executes a list of DML statements (can be simple DML statements or DML statements with
   * returning clause) in a single request. The statements will be executed in order and the
   * semantics is the same as if each statement is executed by {@link
   * Connection#executeUpdate(Statement)} in a loop. This method returns an array of long integers,
   * each representing the number of rows modified by each statement.
   *
   * <p>If an individual statement fails, execution stops and a {@code SpannerBatchUpdateException}
   * is returned, which includes the error and the number of rows affected by the statements that
   * are run prior to the error.
   *
   * <p>For example, if statements contains 3 statements, and the 2nd one is not a valid DML. This
   * method throws a {@code SpannerBatchUpdateException} that contains the error message from the
   * 2nd statement, and an array of length 1 that contains the number of rows modified by the 1st
   * statement. The 3rd statement will not run. Executes the given statements as DML statements in
   * one batch. If one of the statements does not contain a valid DML statement, the method will
   * throw a {@link SpannerException}.
   *
   * @param updates The update statements that will be executed as one batch.
   * @return an array containing the update counts per statement.
   */
  long[] executeBatchUpdate(Iterable<Statement> updates);

  /**
   * Executes a list of DML statements (can be simple DML statements or DML statements with
   * returning clause) in a single request. The statements will be executed in order and the
   * semantics is the same as if each statement is executed by {@link
   * Connection#executeUpdate(Statement)} in a loop. This method returns an {@link ApiFuture} that
   * contains an array of long integers, each representing the number of rows modified by each
   * statement.
   *
   * <p>This method is guaranteed to be non-blocking.
   *
   * <p>If an individual statement fails, execution stops and a {@code SpannerBatchUpdateException}
   * is returned, which includes the error and the number of rows affected by the statements that
   * are run prior to the error.
   *
   * <p>For example, if statements contains 3 statements, and the 2nd one is not a valid DML. This
   * method throws a {@code SpannerBatchUpdateException} that contains the error message from the
   * 2nd statement, and an array of length 1 that contains the number of rows modified by the 1st
   * statement. The 3rd statement will not run. Executes the given statements as DML statements in
   * one batch. If one of the statements does not contain a valid DML statement, the method will
   * throw a {@link SpannerException}.
   *
   * @param updates The update statements that will be executed as one batch.
   * @return an {@link ApiFuture} containing an array with the update counts per statement.
   */
  ApiFuture<long[]> executeBatchUpdateAsync(Iterable<Statement> updates);

  /**
   * Writes the specified mutation directly to the database and commits the change. The value is
   * readable after the successful completion of this method. Writing multiple mutations to a
   * database by calling this method multiple times mode is inefficient, as each call will need a
   * round trip to the database. Instead, you should consider writing the mutations together by
   * calling {@link Connection#write(Iterable)}.
   *
   * <p>Calling this method is only allowed in autocommit mode. See {@link
   * Connection#bufferedWrite(Iterable)} for writing mutations in transactions.
   *
   * @param mutation The {@link Mutation} to write to the database
   * @throws SpannerException if the {@link Connection} is not in autocommit mode
   */
  void write(Mutation mutation);

  /**
   * Writes the specified mutation directly to the database and commits the change. The value is
   * readable after the successful completion of the returned {@link ApiFuture}. Writing multiple
   * mutations to a database by calling this method multiple times mode is inefficient, as each call
   * will need a round trip to the database. Instead, you should consider writing the mutations
   * together by calling {@link Connection#writeAsync(Iterable)}.
   *
   * <p>This method is guaranteed to be non-blocking.
   *
   * <p>Calling this method is only allowed in autocommit mode. See {@link
   * Connection#bufferedWrite(Iterable)} for writing mutations in transactions.
   *
   * @param mutation The {@link Mutation} to write to the database
   * @throws SpannerException if the {@link Connection} is not in autocommit mode
   */
  ApiFuture<Void> writeAsync(Mutation mutation);

  /**
   * Writes the specified mutations directly to the database and commits the changes. The values are
   * readable after the successful completion of this method.
   *
   * <p>Calling this method is only allowed in autocommit mode. See {@link
   * Connection#bufferedWrite(Iterable)} for writing mutations in transactions.
   *
   * @param mutations The {@link Mutation}s to write to the database
   * @throws SpannerException if the {@link Connection} is not in autocommit mode
   */
  void write(Iterable<Mutation> mutations);

  /**
   * Writes the specified mutations directly to the database and commits the changes. The values are
   * readable after the successful completion of the returned {@link ApiFuture}.
   *
   * <p>This method is guaranteed to be non-blocking.
   *
   * <p>Calling this method is only allowed in autocommit mode. See {@link
   * Connection#bufferedWrite(Iterable)} for writing mutations in transactions.
   *
   * @param mutations The {@link Mutation}s to write to the database
   * @throws SpannerException if the {@link Connection} is not in autocommit mode
   */
  ApiFuture<Void> writeAsync(Iterable<Mutation> mutations);

  /**
   * Buffers the given mutation locally on the current transaction of this {@link Connection}. The
   * mutation will be written to the database at the next call to {@link Connection#commit()}. The
   * value will not be readable on this {@link Connection} before the transaction is committed.
   *
   * <p>Calling this method is only allowed when not in autocommit mode. See {@link
   * Connection#write(Mutation)} for writing mutations in autocommit mode.
   *
   * @param mutation the {@link Mutation} to buffer for writing to the database on the next commit
   * @throws SpannerException if the {@link Connection} is in autocommit mode
   */
  void bufferedWrite(Mutation mutation);

  /**
   * Buffers the given mutations locally on the current transaction of this {@link Connection}. The
   * mutations will be written to the database at the next call to {@link Connection#commit()}. The
   * values will not be readable on this {@link Connection} before the transaction is committed.
   *
   * <p>Calling this method is only allowed when not in autocommit mode. See {@link
   * Connection#write(Iterable)} for writing mutations in autocommit mode.
   *
   * @param mutations the {@link Mutation}s to buffer for writing to the database on the next commit
   * @throws SpannerException if the {@link Connection} is in autocommit mode
   */
  void bufferedWrite(Iterable<Mutation> mutations);

  /** The {@link Dialect} that is used by this {@link Connection}. */
  default Dialect getDialect() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /** The {@link DatabaseClient} that is used by this {@link Connection}. */
  @InternalApi
  default DatabaseClient getDatabaseClient() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /** The {@link Spanner} instance that is used by this {@link Connection}. */
  @InternalApi
  default Spanner getSpanner() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * This query option is used internally to indicate that a query is executed by the library itself
   * to fetch metadata. These queries are specifically allowed to be executed even when a DDL batch
   * is active.
   *
   * <p>NOT INTENDED FOR EXTERNAL USE!
   */
  @InternalApi
  final class InternalMetadataQuery implements QueryOption {
    @InternalApi public static final InternalMetadataQuery INSTANCE = new InternalMetadataQuery();

    private InternalMetadataQuery() {}
  }
}

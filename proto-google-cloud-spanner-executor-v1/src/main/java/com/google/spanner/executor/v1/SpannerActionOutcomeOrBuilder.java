/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/spanner/executor/v1/cloud_executor.proto

// Protobuf Java Version: 3.25.5
package com.google.spanner.executor.v1;

public interface SpannerActionOutcomeOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.spanner.executor.v1.SpannerActionOutcome)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * If an outcome is split into multiple parts, status will be set only in the
   * last part.
   * </pre>
   *
   * <code>optional .google.rpc.Status status = 1;</code>
   *
   * @return Whether the status field is set.
   */
  boolean hasStatus();
  /**
   *
   *
   * <pre>
   * If an outcome is split into multiple parts, status will be set only in the
   * last part.
   * </pre>
   *
   * <code>optional .google.rpc.Status status = 1;</code>
   *
   * @return The status.
   */
  com.google.rpc.Status getStatus();
  /**
   *
   *
   * <pre>
   * If an outcome is split into multiple parts, status will be set only in the
   * last part.
   * </pre>
   *
   * <code>optional .google.rpc.Status status = 1;</code>
   */
  com.google.rpc.StatusOrBuilder getStatusOrBuilder();

  /**
   *
   *
   * <pre>
   * Transaction timestamp. It must be set for successful committed actions.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp commit_time = 2;</code>
   *
   * @return Whether the commitTime field is set.
   */
  boolean hasCommitTime();
  /**
   *
   *
   * <pre>
   * Transaction timestamp. It must be set for successful committed actions.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp commit_time = 2;</code>
   *
   * @return The commitTime.
   */
  com.google.protobuf.Timestamp getCommitTime();
  /**
   *
   *
   * <pre>
   * Transaction timestamp. It must be set for successful committed actions.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp commit_time = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCommitTimeOrBuilder();

  /**
   *
   *
   * <pre>
   * Result of a ReadAction. This field must be set for ReadActions even if
   * no rows were read.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.ReadResult read_result = 3;</code>
   *
   * @return Whether the readResult field is set.
   */
  boolean hasReadResult();
  /**
   *
   *
   * <pre>
   * Result of a ReadAction. This field must be set for ReadActions even if
   * no rows were read.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.ReadResult read_result = 3;</code>
   *
   * @return The readResult.
   */
  com.google.spanner.executor.v1.ReadResult getReadResult();
  /**
   *
   *
   * <pre>
   * Result of a ReadAction. This field must be set for ReadActions even if
   * no rows were read.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.ReadResult read_result = 3;</code>
   */
  com.google.spanner.executor.v1.ReadResultOrBuilder getReadResultOrBuilder();

  /**
   *
   *
   * <pre>
   * Result of a Query. This field must be set for Queries even if no rows were
   * read.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.QueryResult query_result = 4;</code>
   *
   * @return Whether the queryResult field is set.
   */
  boolean hasQueryResult();
  /**
   *
   *
   * <pre>
   * Result of a Query. This field must be set for Queries even if no rows were
   * read.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.QueryResult query_result = 4;</code>
   *
   * @return The queryResult.
   */
  com.google.spanner.executor.v1.QueryResult getQueryResult();
  /**
   *
   *
   * <pre>
   * Result of a Query. This field must be set for Queries even if no rows were
   * read.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.QueryResult query_result = 4;</code>
   */
  com.google.spanner.executor.v1.QueryResultOrBuilder getQueryResultOrBuilder();

  /**
   *
   *
   * <pre>
   * This bit indicates that Spanner has restarted the current transaction. It
   * means that the client should replay all the reads and writes.
   * Setting it to true is only valid in the context of a read-write
   * transaction, as an outcome of a committing FinishTransactionAction.
   * </pre>
   *
   * <code>optional bool transaction_restarted = 5;</code>
   *
   * @return Whether the transactionRestarted field is set.
   */
  boolean hasTransactionRestarted();
  /**
   *
   *
   * <pre>
   * This bit indicates that Spanner has restarted the current transaction. It
   * means that the client should replay all the reads and writes.
   * Setting it to true is only valid in the context of a read-write
   * transaction, as an outcome of a committing FinishTransactionAction.
   * </pre>
   *
   * <code>optional bool transaction_restarted = 5;</code>
   *
   * @return The transactionRestarted.
   */
  boolean getTransactionRestarted();

  /**
   *
   *
   * <pre>
   * In successful StartBatchTransactionAction outcomes, this contains the ID of
   * the transaction.
   * </pre>
   *
   * <code>optional bytes batch_txn_id = 6;</code>
   *
   * @return Whether the batchTxnId field is set.
   */
  boolean hasBatchTxnId();
  /**
   *
   *
   * <pre>
   * In successful StartBatchTransactionAction outcomes, this contains the ID of
   * the transaction.
   * </pre>
   *
   * <code>optional bytes batch_txn_id = 6;</code>
   *
   * @return The batchTxnId.
   */
  com.google.protobuf.ByteString getBatchTxnId();

  /**
   *
   *
   * <pre>
   * Generated database partitions (result of a
   * GenetageDbPartitionsForReadAction/GenerateDbPartitionsForQueryAction).
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.BatchPartition db_partition = 7;</code>
   */
  java.util.List<com.google.spanner.executor.v1.BatchPartition> getDbPartitionList();
  /**
   *
   *
   * <pre>
   * Generated database partitions (result of a
   * GenetageDbPartitionsForReadAction/GenerateDbPartitionsForQueryAction).
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.BatchPartition db_partition = 7;</code>
   */
  com.google.spanner.executor.v1.BatchPartition getDbPartition(int index);
  /**
   *
   *
   * <pre>
   * Generated database partitions (result of a
   * GenetageDbPartitionsForReadAction/GenerateDbPartitionsForQueryAction).
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.BatchPartition db_partition = 7;</code>
   */
  int getDbPartitionCount();
  /**
   *
   *
   * <pre>
   * Generated database partitions (result of a
   * GenetageDbPartitionsForReadAction/GenerateDbPartitionsForQueryAction).
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.BatchPartition db_partition = 7;</code>
   */
  java.util.List<? extends com.google.spanner.executor.v1.BatchPartitionOrBuilder>
      getDbPartitionOrBuilderList();
  /**
   *
   *
   * <pre>
   * Generated database partitions (result of a
   * GenetageDbPartitionsForReadAction/GenerateDbPartitionsForQueryAction).
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.BatchPartition db_partition = 7;</code>
   */
  com.google.spanner.executor.v1.BatchPartitionOrBuilder getDbPartitionOrBuilder(int index);

  /**
   *
   *
   * <pre>
   * Result of admin related actions.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.AdminResult admin_result = 8;</code>
   *
   * @return Whether the adminResult field is set.
   */
  boolean hasAdminResult();
  /**
   *
   *
   * <pre>
   * Result of admin related actions.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.AdminResult admin_result = 8;</code>
   *
   * @return The adminResult.
   */
  com.google.spanner.executor.v1.AdminResult getAdminResult();
  /**
   *
   *
   * <pre>
   * Result of admin related actions.
   * </pre>
   *
   * <code>optional .google.spanner.executor.v1.AdminResult admin_result = 8;</code>
   */
  com.google.spanner.executor.v1.AdminResultOrBuilder getAdminResultOrBuilder();

  /**
   *
   *
   * <pre>
   * Stores rows modified by query in single DML or batch DML action.
   * In case of batch DML action, stores 0 as row count of errored DML query.
   * </pre>
   *
   * <code>repeated int64 dml_rows_modified = 9;</code>
   *
   * @return A list containing the dmlRowsModified.
   */
  java.util.List<java.lang.Long> getDmlRowsModifiedList();
  /**
   *
   *
   * <pre>
   * Stores rows modified by query in single DML or batch DML action.
   * In case of batch DML action, stores 0 as row count of errored DML query.
   * </pre>
   *
   * <code>repeated int64 dml_rows_modified = 9;</code>
   *
   * @return The count of dmlRowsModified.
   */
  int getDmlRowsModifiedCount();
  /**
   *
   *
   * <pre>
   * Stores rows modified by query in single DML or batch DML action.
   * In case of batch DML action, stores 0 as row count of errored DML query.
   * </pre>
   *
   * <code>repeated int64 dml_rows_modified = 9;</code>
   *
   * @param index The index of the element to return.
   * @return The dmlRowsModified at the given index.
   */
  long getDmlRowsModified(int index);

  /**
   *
   *
   * <pre>
   * Change stream records returned by a change stream query.
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.ChangeStreamRecord change_stream_records = 10;
   * </code>
   */
  java.util.List<com.google.spanner.executor.v1.ChangeStreamRecord> getChangeStreamRecordsList();
  /**
   *
   *
   * <pre>
   * Change stream records returned by a change stream query.
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.ChangeStreamRecord change_stream_records = 10;
   * </code>
   */
  com.google.spanner.executor.v1.ChangeStreamRecord getChangeStreamRecords(int index);
  /**
   *
   *
   * <pre>
   * Change stream records returned by a change stream query.
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.ChangeStreamRecord change_stream_records = 10;
   * </code>
   */
  int getChangeStreamRecordsCount();
  /**
   *
   *
   * <pre>
   * Change stream records returned by a change stream query.
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.ChangeStreamRecord change_stream_records = 10;
   * </code>
   */
  java.util.List<? extends com.google.spanner.executor.v1.ChangeStreamRecordOrBuilder>
      getChangeStreamRecordsOrBuilderList();
  /**
   *
   *
   * <pre>
   * Change stream records returned by a change stream query.
   * </pre>
   *
   * <code>repeated .google.spanner.executor.v1.ChangeStreamRecord change_stream_records = 10;
   * </code>
   */
  com.google.spanner.executor.v1.ChangeStreamRecordOrBuilder getChangeStreamRecordsOrBuilder(
      int index);
}

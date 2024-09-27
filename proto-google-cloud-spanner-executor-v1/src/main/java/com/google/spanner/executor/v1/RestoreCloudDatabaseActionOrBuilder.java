/*
 * Copyright 2024 Google LLC
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

public interface RestoreCloudDatabaseActionOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.spanner.executor.v1.RestoreCloudDatabaseAction)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * Cloud project ID, e.g. "spanner-cloud-systest".
   * </pre>
   *
   * <code>string project_id = 1;</code>
   *
   * @return The projectId.
   */
  java.lang.String getProjectId();
  /**
   *
   *
   * <pre>
   * Cloud project ID, e.g. "spanner-cloud-systest".
   * </pre>
   *
   * <code>string project_id = 1;</code>
   *
   * @return The bytes for projectId.
   */
  com.google.protobuf.ByteString getProjectIdBytes();

  /**
   *
   *
   * <pre>
   * Cloud instance ID (not path) containing the backup, e.g. "backup-instance".
   * </pre>
   *
   * <code>string backup_instance_id = 2;</code>
   *
   * @return The backupInstanceId.
   */
  java.lang.String getBackupInstanceId();
  /**
   *
   *
   * <pre>
   * Cloud instance ID (not path) containing the backup, e.g. "backup-instance".
   * </pre>
   *
   * <code>string backup_instance_id = 2;</code>
   *
   * @return The bytes for backupInstanceId.
   */
  com.google.protobuf.ByteString getBackupInstanceIdBytes();

  /**
   *
   *
   * <pre>
   * The id of the backup from which to restore, e.g. "test-backup".
   * </pre>
   *
   * <code>string backup_id = 3;</code>
   *
   * @return The backupId.
   */
  java.lang.String getBackupId();
  /**
   *
   *
   * <pre>
   * The id of the backup from which to restore, e.g. "test-backup".
   * </pre>
   *
   * <code>string backup_id = 3;</code>
   *
   * @return The bytes for backupId.
   */
  com.google.protobuf.ByteString getBackupIdBytes();

  /**
   *
   *
   * <pre>
   * Cloud instance ID (not path) containing the database, e.g.
   * "database-instance".
   * </pre>
   *
   * <code>string database_instance_id = 4;</code>
   *
   * @return The databaseInstanceId.
   */
  java.lang.String getDatabaseInstanceId();
  /**
   *
   *
   * <pre>
   * Cloud instance ID (not path) containing the database, e.g.
   * "database-instance".
   * </pre>
   *
   * <code>string database_instance_id = 4;</code>
   *
   * @return The bytes for databaseInstanceId.
   */
  com.google.protobuf.ByteString getDatabaseInstanceIdBytes();

  /**
   *
   *
   * <pre>
   * The id of the database to create and restore to, e.g. "db0". Note that this
   * database must not already exist.
   * </pre>
   *
   * <code>string database_id = 5;</code>
   *
   * @return The databaseId.
   */
  java.lang.String getDatabaseId();
  /**
   *
   *
   * <pre>
   * The id of the database to create and restore to, e.g. "db0". Note that this
   * database must not already exist.
   * </pre>
   *
   * <code>string database_id = 5;</code>
   *
   * @return The bytes for databaseId.
   */
  com.google.protobuf.ByteString getDatabaseIdBytes();

  /**
   *
   *
   * <pre>
   * The KMS key(s) used to encrypt the restored database to be created if the
   * restored database should be CMEK protected.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.EncryptionConfig encryption_config = 7;</code>
   *
   * @return Whether the encryptionConfig field is set.
   */
  boolean hasEncryptionConfig();
  /**
   *
   *
   * <pre>
   * The KMS key(s) used to encrypt the restored database to be created if the
   * restored database should be CMEK protected.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.EncryptionConfig encryption_config = 7;</code>
   *
   * @return The encryptionConfig.
   */
  com.google.spanner.admin.database.v1.EncryptionConfig getEncryptionConfig();
  /**
   *
   *
   * <pre>
   * The KMS key(s) used to encrypt the restored database to be created if the
   * restored database should be CMEK protected.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.EncryptionConfig encryption_config = 7;</code>
   */
  com.google.spanner.admin.database.v1.EncryptionConfigOrBuilder getEncryptionConfigOrBuilder();
}

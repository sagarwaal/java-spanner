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
// source: google/spanner/admin/database/v1/backup_schedule.proto

// Protobuf Java Version: 3.25.5
package com.google.spanner.admin.database.v1;

public interface BackupScheduleOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.spanner.admin.database.v1.BackupSchedule)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * Identifier. Output only for the
   * [CreateBackupSchedule][DatabaseAdmin.CreateBackupSchededule] operation.
   * Required for the
   * [UpdateBackupSchedule][google.spanner.admin.database.v1.DatabaseAdmin.UpdateBackupSchedule]
   * operation. A globally unique identifier for the backup schedule which
   * cannot be changed. Values are of the form
   * `projects/&lt;project&gt;/instances/&lt;instance&gt;/databases/&lt;database&gt;/backupSchedules/[a-z][a-z0-9_&#92;-]*[a-z0-9]`
   * The final segment of the name must be between 2 and 60 characters in
   * length.
   * </pre>
   *
   * <code>string name = 1 [(.google.api.field_behavior) = IDENTIFIER];</code>
   *
   * @return The name.
   */
  java.lang.String getName();
  /**
   *
   *
   * <pre>
   * Identifier. Output only for the
   * [CreateBackupSchedule][DatabaseAdmin.CreateBackupSchededule] operation.
   * Required for the
   * [UpdateBackupSchedule][google.spanner.admin.database.v1.DatabaseAdmin.UpdateBackupSchedule]
   * operation. A globally unique identifier for the backup schedule which
   * cannot be changed. Values are of the form
   * `projects/&lt;project&gt;/instances/&lt;instance&gt;/databases/&lt;database&gt;/backupSchedules/[a-z][a-z0-9_&#92;-]*[a-z0-9]`
   * The final segment of the name must be between 2 and 60 characters in
   * length.
   * </pre>
   *
   * <code>string name = 1 [(.google.api.field_behavior) = IDENTIFIER];</code>
   *
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString getNameBytes();

  /**
   *
   *
   * <pre>
   * Optional. The schedule specification based on which the backup creations
   * are triggered.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.database.v1.BackupScheduleSpec spec = 6 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   *
   * @return Whether the spec field is set.
   */
  boolean hasSpec();
  /**
   *
   *
   * <pre>
   * Optional. The schedule specification based on which the backup creations
   * are triggered.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.database.v1.BackupScheduleSpec spec = 6 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   *
   * @return The spec.
   */
  com.google.spanner.admin.database.v1.BackupScheduleSpec getSpec();
  /**
   *
   *
   * <pre>
   * Optional. The schedule specification based on which the backup creations
   * are triggered.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.database.v1.BackupScheduleSpec spec = 6 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   */
  com.google.spanner.admin.database.v1.BackupScheduleSpecOrBuilder getSpecOrBuilder();

  /**
   *
   *
   * <pre>
   * Optional. The retention duration of a backup that must be at least 6 hours
   * and at most 366 days. The backup is eligible to be automatically deleted
   * once the retention period has elapsed.
   * </pre>
   *
   * <code>
   * .google.protobuf.Duration retention_duration = 3 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   *
   * @return Whether the retentionDuration field is set.
   */
  boolean hasRetentionDuration();
  /**
   *
   *
   * <pre>
   * Optional. The retention duration of a backup that must be at least 6 hours
   * and at most 366 days. The backup is eligible to be automatically deleted
   * once the retention period has elapsed.
   * </pre>
   *
   * <code>
   * .google.protobuf.Duration retention_duration = 3 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   *
   * @return The retentionDuration.
   */
  com.google.protobuf.Duration getRetentionDuration();
  /**
   *
   *
   * <pre>
   * Optional. The retention duration of a backup that must be at least 6 hours
   * and at most 366 days. The backup is eligible to be automatically deleted
   * once the retention period has elapsed.
   * </pre>
   *
   * <code>
   * .google.protobuf.Duration retention_duration = 3 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   */
  com.google.protobuf.DurationOrBuilder getRetentionDurationOrBuilder();

  /**
   *
   *
   * <pre>
   * Optional. The encryption configuration that will be used to encrypt the
   * backup. If this field is not specified, the backup will use the same
   * encryption configuration as the database.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.database.v1.CreateBackupEncryptionConfig encryption_config = 4 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   *
   * @return Whether the encryptionConfig field is set.
   */
  boolean hasEncryptionConfig();
  /**
   *
   *
   * <pre>
   * Optional. The encryption configuration that will be used to encrypt the
   * backup. If this field is not specified, the backup will use the same
   * encryption configuration as the database.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.database.v1.CreateBackupEncryptionConfig encryption_config = 4 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   *
   * @return The encryptionConfig.
   */
  com.google.spanner.admin.database.v1.CreateBackupEncryptionConfig getEncryptionConfig();
  /**
   *
   *
   * <pre>
   * Optional. The encryption configuration that will be used to encrypt the
   * backup. If this field is not specified, the backup will use the same
   * encryption configuration as the database.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.database.v1.CreateBackupEncryptionConfig encryption_config = 4 [(.google.api.field_behavior) = OPTIONAL];
   * </code>
   */
  com.google.spanner.admin.database.v1.CreateBackupEncryptionConfigOrBuilder
      getEncryptionConfigOrBuilder();

  /**
   *
   *
   * <pre>
   * The schedule creates only full backups.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.FullBackupSpec full_backup_spec = 7;</code>
   *
   * @return Whether the fullBackupSpec field is set.
   */
  boolean hasFullBackupSpec();
  /**
   *
   *
   * <pre>
   * The schedule creates only full backups.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.FullBackupSpec full_backup_spec = 7;</code>
   *
   * @return The fullBackupSpec.
   */
  com.google.spanner.admin.database.v1.FullBackupSpec getFullBackupSpec();
  /**
   *
   *
   * <pre>
   * The schedule creates only full backups.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.FullBackupSpec full_backup_spec = 7;</code>
   */
  com.google.spanner.admin.database.v1.FullBackupSpecOrBuilder getFullBackupSpecOrBuilder();

  /**
   *
   *
   * <pre>
   * The schedule creates incremental backup chains.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.IncrementalBackupSpec incremental_backup_spec = 8;
   * </code>
   *
   * @return Whether the incrementalBackupSpec field is set.
   */
  boolean hasIncrementalBackupSpec();
  /**
   *
   *
   * <pre>
   * The schedule creates incremental backup chains.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.IncrementalBackupSpec incremental_backup_spec = 8;
   * </code>
   *
   * @return The incrementalBackupSpec.
   */
  com.google.spanner.admin.database.v1.IncrementalBackupSpec getIncrementalBackupSpec();
  /**
   *
   *
   * <pre>
   * The schedule creates incremental backup chains.
   * </pre>
   *
   * <code>.google.spanner.admin.database.v1.IncrementalBackupSpec incremental_backup_spec = 8;
   * </code>
   */
  com.google.spanner.admin.database.v1.IncrementalBackupSpecOrBuilder
      getIncrementalBackupSpecOrBuilder();

  /**
   *
   *
   * <pre>
   * Output only. The timestamp at which the schedule was last updated.
   * If the schedule has never been updated, this field contains the timestamp
   * when the schedule was first created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp update_time = 9 [(.google.api.field_behavior) = OUTPUT_ONLY];
   * </code>
   *
   * @return Whether the updateTime field is set.
   */
  boolean hasUpdateTime();
  /**
   *
   *
   * <pre>
   * Output only. The timestamp at which the schedule was last updated.
   * If the schedule has never been updated, this field contains the timestamp
   * when the schedule was first created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp update_time = 9 [(.google.api.field_behavior) = OUTPUT_ONLY];
   * </code>
   *
   * @return The updateTime.
   */
  com.google.protobuf.Timestamp getUpdateTime();
  /**
   *
   *
   * <pre>
   * Output only. The timestamp at which the schedule was last updated.
   * If the schedule has never been updated, this field contains the timestamp
   * when the schedule was first created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp update_time = 9 [(.google.api.field_behavior) = OUTPUT_ONLY];
   * </code>
   */
  com.google.protobuf.TimestampOrBuilder getUpdateTimeOrBuilder();

  com.google.spanner.admin.database.v1.BackupSchedule.BackupTypeSpecCase getBackupTypeSpecCase();
}

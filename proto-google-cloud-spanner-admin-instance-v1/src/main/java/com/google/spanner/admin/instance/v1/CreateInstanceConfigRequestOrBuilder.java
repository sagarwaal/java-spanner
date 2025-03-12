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
// source: google/spanner/admin/instance/v1/spanner_instance_admin.proto

// Protobuf Java Version: 3.25.5
package com.google.spanner.admin.instance.v1;

public interface CreateInstanceConfigRequestOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.spanner.admin.instance.v1.CreateInstanceConfigRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * Required. The name of the project in which to create the instance
   * configuration. Values are of the form `projects/&lt;project&gt;`.
   * </pre>
   *
   * <code>
   * string parent = 1 [(.google.api.field_behavior) = REQUIRED, (.google.api.resource_reference) = { ... }
   * </code>
   *
   * @return The parent.
   */
  java.lang.String getParent();
  /**
   *
   *
   * <pre>
   * Required. The name of the project in which to create the instance
   * configuration. Values are of the form `projects/&lt;project&gt;`.
   * </pre>
   *
   * <code>
   * string parent = 1 [(.google.api.field_behavior) = REQUIRED, (.google.api.resource_reference) = { ... }
   * </code>
   *
   * @return The bytes for parent.
   */
  com.google.protobuf.ByteString getParentBytes();

  /**
   *
   *
   * <pre>
   * Required. The ID of the instance configuration to create. Valid identifiers
   * are of the form `custom-[-a-z0-9]*[a-z0-9]` and must be between 2 and 64
   * characters in length. The `custom-` prefix is required to avoid name
   * conflicts with Google-managed configurations.
   * </pre>
   *
   * <code>string instance_config_id = 2 [(.google.api.field_behavior) = REQUIRED];</code>
   *
   * @return The instanceConfigId.
   */
  java.lang.String getInstanceConfigId();
  /**
   *
   *
   * <pre>
   * Required. The ID of the instance configuration to create. Valid identifiers
   * are of the form `custom-[-a-z0-9]*[a-z0-9]` and must be between 2 and 64
   * characters in length. The `custom-` prefix is required to avoid name
   * conflicts with Google-managed configurations.
   * </pre>
   *
   * <code>string instance_config_id = 2 [(.google.api.field_behavior) = REQUIRED];</code>
   *
   * @return The bytes for instanceConfigId.
   */
  com.google.protobuf.ByteString getInstanceConfigIdBytes();

  /**
   *
   *
   * <pre>
   * Required. The `InstanceConfig` proto of the configuration to create.
   * `instance_config.name` must be
   * `&lt;parent&gt;/instanceConfigs/&lt;instance_config_id&gt;`.
   * `instance_config.base_config` must be a Google-managed configuration name,
   * e.g. &lt;parent&gt;/instanceConfigs/us-east1, &lt;parent&gt;/instanceConfigs/nam3.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.instance.v1.InstanceConfig instance_config = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   *
   * @return Whether the instanceConfig field is set.
   */
  boolean hasInstanceConfig();
  /**
   *
   *
   * <pre>
   * Required. The `InstanceConfig` proto of the configuration to create.
   * `instance_config.name` must be
   * `&lt;parent&gt;/instanceConfigs/&lt;instance_config_id&gt;`.
   * `instance_config.base_config` must be a Google-managed configuration name,
   * e.g. &lt;parent&gt;/instanceConfigs/us-east1, &lt;parent&gt;/instanceConfigs/nam3.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.instance.v1.InstanceConfig instance_config = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   *
   * @return The instanceConfig.
   */
  com.google.spanner.admin.instance.v1.InstanceConfig getInstanceConfig();
  /**
   *
   *
   * <pre>
   * Required. The `InstanceConfig` proto of the configuration to create.
   * `instance_config.name` must be
   * `&lt;parent&gt;/instanceConfigs/&lt;instance_config_id&gt;`.
   * `instance_config.base_config` must be a Google-managed configuration name,
   * e.g. &lt;parent&gt;/instanceConfigs/us-east1, &lt;parent&gt;/instanceConfigs/nam3.
   * </pre>
   *
   * <code>
   * .google.spanner.admin.instance.v1.InstanceConfig instance_config = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   */
  com.google.spanner.admin.instance.v1.InstanceConfigOrBuilder getInstanceConfigOrBuilder();

  /**
   *
   *
   * <pre>
   * An option to validate, but not actually execute, a request,
   * and provide the same response.
   * </pre>
   *
   * <code>bool validate_only = 4;</code>
   *
   * @return The validateOnly.
   */
  boolean getValidateOnly();
}

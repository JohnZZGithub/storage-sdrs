/*
 * Copyright 2019 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 * Any software provided by Google hereunder is distributed “AS IS”,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, and is not intended for production use.
 *
 */
package com.google.gcs.sdrs.service.mq.pojo;

import com.google.gcs.sdrs.service.mq.events.SuccessDeleteNotificationEvent;
import com.google.gcs.sdrs.service.mq.events.context.EventContext;
import com.google.gcs.sdrs.util.RetentionUtil;
import java.time.Instant;
import java.util.UUID;
import org.joda.time.DateTime;

/** POJO for successful delete notification message */
public class DeleteNotificationMessage {
  public static final String DELETE_NOTIFICAITON_EVENT_NAME = "SuccessDeleteNotificationEvent";
  public static final String AVRO_MESSAGE_VERSION = "1.0";

  private String projectId;
  private Instant deletedAt;
  private String deletedDirectoryUri;
  private String trigger;
  private String correlationId;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String getDeletedDirectoryUri() {
    return deletedDirectoryUri;
  }

  public void setDeletedDirectoryUri(String deletedDirectoryUri) {
    this.deletedDirectoryUri = deletedDirectoryUri;
  }

  public String getTrigger() {
    return trigger;
  }

  public void setTrigger(String trigger) {
    this.trigger = trigger;
  }

  public SuccessDeleteNotificationEvent convertToAvro() {

    EventContext ctx =
        EventContext.newBuilder()
            .setName(DELETE_NOTIFICAITON_EVENT_NAME)
            .setUuid(UUID.randomUUID().toString())
            .setVersion(AVRO_MESSAGE_VERSION)
            .setCorrelationID(this.getCorrelationId())
            .setTimestamp(new DateTime())
            .build();

    SuccessDeleteNotificationEvent event =
        SuccessDeleteNotificationEvent.newBuilder()
            .setContext(ctx)
            .setBucket(RetentionUtil.getBucketName(this.getDeletedDirectoryUri()))
            .setDeletedAt(this.getDeletedAt().toString())
            .setDirectory(this.getDeletedDirectoryUri())
            .setTrigger(this.getTrigger())
            .setProjectId(this.getProjectId())
            .setVersion(AVRO_MESSAGE_VERSION)
            .build();

    return event;
  }
}

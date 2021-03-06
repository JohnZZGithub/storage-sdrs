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
 */

package com.google.gcs.sdrs.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gcs.sdrs.SdrsApplication;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to provide authentication, protocol, and hostname details for a web request back to the
 * SDRS app.
 */
public class SdrsRequestClientUtil {

  private static final Logger logger = LoggerFactory.getLogger(SdrsRequestClientUtil.class);
  static String serviceUrl;
  static String protocol;
  private static String apiKey;
  static String port;

  static CredentialsUtil credentialsUtil = CredentialsUtil.getInstance();

  private SdrsRequestClientUtil() {}

  /**
   * Makes an external request back to the SDRS application.
   *
   * @param client the client to use to make the request
   * @param path the path segment for the endpoint
   * @return a builder that can be used to invoke the request
   */
  public static Invocation.Builder request(Client client, String path) {
    String jwt = generateJwt(String.format("https://%s", getServiceUrl()));

    Invocation.Builder result =
        client
            .target(String.format("%s://%s:%s", getProtocol(), getServiceUrl(), getPort()))
            .queryParam("key", getApiKey())
            .path(path)
            .request()
            .header("Authorization", String.format("Bearer %s", jwt));

    return result;
  }

  private static String generateJwt(String audience) {
    try {
      GoogleCredential cred = credentialsUtil.getCredentials();

      Date now = new Date();
      Date expTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3600));

      JWTCreator.Builder token =
          JWT.create()
              .withIssuedAt(now)
              .withExpiresAt(expTime)
              .withIssuer(cred.getServiceAccountId())
              .withSubject(cred.getServiceAccountId())
              .withAudience(audience)
              .withClaim("email", cred.getServiceAccountId());

      // Sign the JWT with a service account
      RSAPrivateKey key = (RSAPrivateKey) cred.getServiceAccountPrivateKey();
      Algorithm algorithm = Algorithm.RSA256(null, key);
      return token.sign(algorithm);
    } catch (IOException exception) {
      logger.error(String.format("Unable to generate JWT: %s", exception.getMessage()));
      return "";
    }
  }

  private static String getServiceUrl() {
    if (serviceUrl == null) {
      serviceUrl =
          SdrsApplication.getAppConfigProperty("scheduler.task.endpointHost.", "localhost");
    }
    return serviceUrl;
  }

  private static String getProtocol() {
    if (protocol == null) {
      protocol =
          Boolean.valueOf(
                  SdrsApplication.getAppConfigProperty(
                      "scheduler.task.endpointHttpsEnabled", "false"))
              ? "https"
              : "http";
    }
    return protocol;
  }

  private static String getApiKey() {
    if (apiKey == null) {
      apiKey = SdrsApplication.getAppConfigProperty("scheduler.task.endpointApiKey", "");
    }
    return apiKey;
  }

  private static String getPort() {
    if (port == null) {
      port = SdrsApplication.getAppConfigProperty("scheduler.task.endpointPort", "80");
    }
    return port;
  }
}

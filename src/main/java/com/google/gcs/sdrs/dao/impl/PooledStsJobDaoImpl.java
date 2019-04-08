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

package com.google.gcs.sdrs.dao.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gcs.sdrs.dao.PooledStsJobDao;
import com.google.gcs.sdrs.dao.model.PooledStsJob;

public class PooledStsJobDaoImpl extends GenericDao<PooledStsJob, Integer>
    implements PooledStsJobDao {

  private static final Logger logger = LoggerFactory.getLogger(PooledStsJobDaoImpl.class);

  public PooledStsJobDaoImpl() {
    super(PooledStsJob.class);
  }

  /**
   * Gets all project ids associated with dataset rules
   *
   * @return a {@link List} of {@link String} project ids
   */
  @Override
  public List<PooledStsJob> getAllPooledStsJobsByBucketName(
      String sourceBucket, String sourceProject) {
    Session session = openSession();
    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<PooledStsJob> criteria = builder.createQuery(PooledStsJob.class);
    Root<PooledStsJob> root = criteria.from(PooledStsJob.class);

    criteria
        .select(root)
        .where(
            builder.equal(root.get("sourceBucket"), sourceBucket),
            builder.equal(root.get("sourceProject"), sourceProject));

    Query<PooledStsJob> query = session.createQuery(criteria);
    List<PooledStsJob> result = query.getResultList();
    closeSession(session);
    return result;
  }
}

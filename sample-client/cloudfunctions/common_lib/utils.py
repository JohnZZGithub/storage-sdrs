# Copyright 2019 Google LLC. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#
# Any software provided by Google hereunder is distributed "AS IS", WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, and is not intended for production use.

import json
import os
import time

from google.auth import jwt
import googleapiclient.discovery

_ENDPOINT = os.getenv('endpoint')
_SERVICE_ACCOUNT_EMAIL = os.getenv('FUNCTION_IDENTITY')

PROJECT_ID = os.getenv('projectId')
RETENTION_RULES_ENDPOINT = '{}:80/retentionrules'.format(_ENDPOINT)
EVENTS_ENDPOINT = '{}:80/events'.format(_ENDPOINT)
EVENTS_EXECUTION_ENDPOINT = '{}/execution'.format(EVENTS_ENDPOINT)
EVENTS_VALIDATION_ENDPOINT = '{}/validation'.format(EVENTS_ENDPOINT)
EVENTS_NOTIFICATION_ENDPOINT = '{}/notification'.format(EVENTS_ENDPOINT)

JWT = None


def parse_rpo_request(re_match, event_attributes, object_id):
  """Takes the pubsub notification and parses out the required information.

  An example of a RPO pattern: .rpo/<datetime>_<retention-days>
  """
  retention_period_index = object_id.rfind('_')
  retention_period = object_id[retention_period_index + 1:]

  object_prefix = object_id[:re_match.start()]
  bucket_id = event_attributes['bucketId']
  data_storage_name = 'gs://{}/{}'.format(bucket_id, object_prefix)

  return SdrsRequest(data_storage_name, PROJECT_ID, retention_period)


def parse_delete_request(re_match, event_attributes, object_id):
  """Takes the pubsub notification and parses out the required information.

  An example of a delete pattern: .delete_this_folder
  """
  object_prefix = object_id[:re_match.start()]
  bucket_id = event_attributes['bucketId']
  data_storage_name = 'gs://{}/{}'.format(bucket_id, object_prefix)

  return SdrsRequest(data_storage_name, PROJECT_ID, None)


def get_auth_header():
  """Returns an authorization header that can be attached to a request."""
  return {'Authorization': 'Bearer {}'.format(_get_jwt())}


def _get_jwt():
  """Checks to see if the global JWT is still valid and either returns it or
  generates a new one."""
  global JWT
  if JWT is None:
    JWT = _generate_jwt()
  else:
    try:
      # This will throw a ValueError if the JWT is expired by over 5 min
      decoded = jwt.decode(JWT, verify=False)

      # Err on the side of caution and just create a new JWT if we're at expiry
      if time.time() >= decoded['exp']:
        JWT = _generate_jwt()
    except ValueError:
      JWT = _generate_jwt()
  return JWT


def _generate_jwt():
  """Generates a signed JWT using the currently running service account."""
  service = googleapiclient.discovery.build(serviceName='iam', version='v1',
                                            cache_discovery=False)
  now = int(time.time())
  payload_json = json.dumps({
    'iat': now,
    # expires after one hour
    'exp': now + 3600,
    # iss is the service account email
    'iss': _SERVICE_ACCOUNT_EMAIL,
    # sub is required for cloud endpoints and must match iss
    'sub': _SERVICE_ACCOUNT_EMAIL,
    'email': _SERVICE_ACCOUNT_EMAIL,
    # aud is the URL of the target service
    'aud': _ENDPOINT
  })

  slist = service.projects().serviceAccounts().signJwt(
      name='projects/-/serviceAccounts/{}'.format(_SERVICE_ACCOUNT_EMAIL),
      body={'payload': payload_json})
  resp = slist.execute()
  return resp['signedJwt']


class SdrsRequest:

  def __init__(self, data_storage_name, project_id, retention_period):
    self.data_storage_name = data_storage_name
    self.project_id = project_id
    self.retention_period = retention_period

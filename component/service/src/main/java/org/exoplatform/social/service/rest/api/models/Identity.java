/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.service.rest.api.models;

import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.Util;

/**
 * The comment model for Social Rest APIs.
 * 
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since Jun 17, 2011
 */
public class Identity {
  private String id;
  private String remoteId;
  private String providerId;

  public Identity() {
  }
  
  public Identity(org.exoplatform.social.core.identity.model.Identity identity) {
    id = identity.getId();
    remoteId = identity.getRemoteId();
    providerId = identity.getProviderId();
  }
  
  public Identity(String identityId) {
    IdentityManager identityManager =  Util.getIdentityManager();
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getIdentity(identityId, false);
    id = identity.getId();
    remoteId = identity.getRemoteId();
    providerId = identity.getProviderId();
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getRemoteId() {
    return remoteId;
  }
  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }
  public String getProviderId() {
    return providerId;
  }
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }
}

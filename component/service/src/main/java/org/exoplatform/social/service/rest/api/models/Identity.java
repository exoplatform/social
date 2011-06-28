package org.exoplatform.social.service.rest.api.models;

import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.Util;

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

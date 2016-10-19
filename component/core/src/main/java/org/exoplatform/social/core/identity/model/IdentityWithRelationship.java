/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.social.core.identity.model;

import org.exoplatform.social.core.profile.ProfileLoader;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * DTO of identity of a person or a space with relationShip with current viewer user's identity.
 */
public class IdentityWithRelationship extends Identity {

  private Relationship relationship;

  private Identity     delegate;

  public IdentityWithRelationship(String id) {
    super(id);
  }

  public IdentityWithRelationship(Identity identity) {
    super(identity.getId());
    this.delegate = identity;
  }

  public Relationship getRelationship() {
    return relationship;
  }

  public void setRelationship(Relationship relationship) {
    this.relationship = relationship;
  }

  @Override
  public GlobalId getGlobalId() {
    if (delegate != null) {
      return delegate.getGlobalId();
    }
    return super.getGlobalId();
  }

  @Override
  public String getId() {
    if (delegate != null) {
      return delegate.getId();
    }
    return super.getId();
  }

  @Override
  public Profile getProfile() {
    if (delegate != null) {
      return delegate.getProfile();
    }
    return super.getProfile();
  }

  @Override
  public String getRemoteId() {
    if (delegate != null) {
      return delegate.getRemoteId();
    }
    return super.getRemoteId();
  }

  @Override
  public String getProviderId() {
    if (delegate != null) {
      return delegate.getProviderId();
    }
    return super.getProviderId();
  }

  @Override
  public boolean isDeleted() {
    if (delegate != null) {
      return delegate.isDeleted();
    }
    return super.isDeleted();
  }

  @Override
  public boolean isEnable() {
    if (delegate != null) {
      return delegate.isEnable();
    }
    return super.isEnable();
  }

  @Override
  public void setDeleted(boolean isDeleted) {
    if (delegate != null) {
      delegate.setDeleted(isDeleted);
    }
    super.setDeleted(isDeleted);
  }

  @Override
  public void setEnable(boolean isEnable) {
    if (delegate != null) {
      delegate.setEnable(isEnable);
    }
    super.setEnable(isEnable);
  }

  @Override
  public void setId(String id) {
    if (delegate != null) {
      delegate.setId(id);
    }
    super.setId(id);
  }

  @Override
  public void setProfile(Profile profile) {
    if (delegate != null) {
      delegate.setProfile(profile);
    }
    super.setProfile(profile);
  }

  @Override
  public void setProfileLoader(ProfileLoader profileLoader) {
    if (delegate != null) {
      delegate.setProfileLoader(profileLoader);
    }
    super.setProfileLoader(profileLoader);
  }

  @Override
  public void setProviderId(String providerId) {
    if (delegate != null) {
      delegate.setProviderId(providerId);
    }
    super.setProviderId(providerId);
  }

  @Override
  public void setRemoteId(String remoteId) {
    if (delegate != null) {
      delegate.setRemoteId(remoteId);
    }
    super.setRemoteId(remoteId);
  }

  @Override
  public String toString() {
    if (delegate != null) {
      return delegate.getId();
    }
    return super.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (delegate != null) {
      return delegate.equals(obj);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    if (delegate != null) {
      return delegate.hashCode();
    }
    return super.hashCode();
  }
}

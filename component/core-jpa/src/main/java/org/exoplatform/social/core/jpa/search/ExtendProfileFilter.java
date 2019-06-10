/*
 * Copyright (C) 2015 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.search;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.search.Sorting;

import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ExtendProfileFilter extends ProfileFilter {

  private ProfileFilter delegate = null;

  private Identity connection = null;
  private Relationship.Type connectionStatus = null;

  private boolean excludeDeleted = true;
  private boolean excludeDisabled = true;
  private List<Long> identityIds = null;
  private String providerId = null;
  private boolean forceLoadProfile = false;

  public ExtendProfileFilter() {
    this.delegate = new ProfileFilter();
  }

  public ExtendProfileFilter(ProfileFilter delegate) {
    if (delegate == null) {
      delegate = new ProfileFilter();
    }

    this.delegate = delegate;
  }

  public Identity getConnection() {
    return connection;
  }

  public void setConnection(Identity connection) {
    this.connection = connection;
  }

  public Relationship.Type getConnectionStatus() {
    return connectionStatus;
  }

  public void setConnectionStatus(Relationship.Type connectionStatus) {
    this.connectionStatus = connectionStatus;
  }

  public List<Long> getIdentityIds() {
    return identityIds;
  }

  public void setIdentityIds(List<Long> identityIds) {
    this.identityIds = identityIds;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public boolean isExcludeDeleted() {
    return excludeDeleted;
  }

  public void setExcludeDeleted(boolean excludeDeleted) {
    this.excludeDeleted = excludeDeleted;
  }

  public boolean isExcludeDisabled() {
    return excludeDisabled;
  }

  public void setExcludeDisabled(boolean excludeDisabled) {
    this.excludeDisabled = excludeDisabled;
  }

  public ProfileFilter getDelegate() {
    return delegate;
  }

  public void setDelegate(ProfileFilter delegate) {
    this.delegate = delegate;
  }

  public boolean isForceLoadProfile() {
    return forceLoadProfile;
  }

  public void setForceLoadProfile(boolean forceLoadProfile) {
    this.forceLoadProfile = forceLoadProfile;
  }

  @Override
  public String getPosition() {
    return delegate.getPosition();
  }

  @Override
  public void setPosition(String position) {
    delegate.setPosition(position);
  }

  @Override
  public String getCompany() {
    return delegate.getCompany();
  }

  @Override
  public void setCompany(String company) {
    delegate.setCompany(company);
  }

  @Override
  public String getSkills() {
    return delegate.getSkills();
  }

  @Override
  public void setSkills(String skills) {
    delegate.setSkills(skills);
  }

  @Override
  public void setName(String name) {
    delegate.setName(name);
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public void setExcludedIdentityList(List<Identity> excludedIdentityList) {
    delegate.setExcludedIdentityList(excludedIdentityList);
  }

  @Override
  public List<Identity> getExcludedIdentityList() {
    return delegate.getExcludedIdentityList();
  }

  @Override
  public void setOnlineRemoteIds(List<String> onlineRemoteIds) {
    delegate.setOnlineRemoteIds(onlineRemoteIds);
  }

  @Override
  public List<String> getOnlineRemoteIds() {
    return delegate.getOnlineRemoteIds();
  }

  @Override
  public char getFirstCharacterOfName() {
    return delegate.getFirstCharacterOfName();
  }

  @Override
  public void setFirstCharacterOfName(char firstCharacterOfName) {
    delegate.setFirstCharacterOfName(firstCharacterOfName);
  }

  @Override
  public String getFirstCharFieldName() {
    return delegate.getFirstCharFieldName();
  }

  @Override
  public void setFirstCharFieldName(String firstCharField) {
    delegate.setFirstCharFieldName(firstCharField);
  }

  @Override
  public String getAll() {
    return delegate.getAll();
  }

  @Override
  public void setAll(String all) {
    delegate.setAll(all);
  }

  @Override
  public Sorting getSorting() {
    return delegate.getSorting();
  }

  @Override
  public void setSorting(Sorting sorting) {
    delegate.setSorting(sorting);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }
}

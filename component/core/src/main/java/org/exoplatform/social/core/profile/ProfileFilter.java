/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.profile;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.SpaceUtils;

/**
 * This class using for filter profile of identity
 */
public class ProfileFilter implements Cloneable {
  /* filer by user profile name*/
  /** The name. */
  private String name;
  /* filer by user profile position*/
  /** The position. */
  private String position;
  /* filer by user profile company*/
  /** The company. */
  private String company;
  /* filer by user profile professional*/
  /** The skills. */
  private String skills;

  /**
   * Whether search on email field or not
   */
  private boolean searchEmail;

  /** Used for unified search */
  private String all;

  /** the list of identity to be excluded from profile filter **/
  private List<Identity> excludedIdentityList;
  
  /** the list of remoteId who online on system**/
  private List<String> onlineRemoteIds;

  /** Current viewer identity */
  private Identity viewerIdentity;

  /** Filter by first character of name. */
  private char firstCharacterOfName;

  private List<String> remoteIds = null;

  private Sorting sorting;

  private String firstCharFieldName = null;

  public ProfileFilter() {
    this.name = "";
    this.position = "";
    this.company = "";
    this.skills = "";
    this.firstCharacterOfName = '\u0000';
    this.excludedIdentityList = new ArrayList<Identity>();
    this.onlineRemoteIds = new ArrayList<String>();
    this.all = "";
  }

  /**
   * Enable email searching
   * 
   * @param searchEmail
   */
  public void setSearchEmail(boolean searchEmail) {
    this.searchEmail = searchEmail;
  }

  /**
   * Whether enable email in search or not
   * @return
   */
  public boolean isSearchEmail() {
    return searchEmail;
  }

  /**
   * Gets the position.
   *
   * @return the position
   */
  public String getPosition() { return position; }

  /**
   * Sets the position.
   *
   * @param position the new position
   */
  public void setPosition(String position) { 
    this.position = position; 
  }

  /**
   * Gets the company.
   *
   * @return the company
   */
  public String getCompany() { return company; }

  /**
   * Sets the company.
   *
   * @param company the new company
   */
  public void setCompany(String company) {
    this.company = company;
  }

  /**
   * Gets the skills.
   *
   * @return the skills
   */
  public String getSkills() { return skills;}

  /**
   * Sets the skills.
   *
   * @param skills the new skills
   */
  public void setSkills(String skills) {
    this.skills = skills;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() { return name; }

  /**
   * Sets the excludedIdentityList
   *
   * @param excludedIdentityList
   * @since  1.2.0-GA
   */
  public void setExcludedIdentityList(List<Identity> excludedIdentityList) {
    this.excludedIdentityList = excludedIdentityList;
  }

  /**
   * Gets the excludedIdentityList
   * @return the excludedIdentityList
   * @since  1.2.0-GA
   */
  public List<Identity> getExcludedIdentityList() {
    return this.excludedIdentityList;
  }
  
  /**
   * Sets the onlineRemoteIds
   *
   * @param onlineRemoteIds
   * @since  4.0.2-GA and 4.1.0-GA
   */
  public void setOnlineRemoteIds(List<String> onlineRemoteIds) {
    this.onlineRemoteIds = onlineRemoteIds;
  }

  /**
   * Gets the onlineRemoteIds
   * @return the onlineRemoteIds
   * @since  4.0.2-GA and 4.1.0-GA
   */
  public List<String> getOnlineRemoteIds() {
    return this.onlineRemoteIds;
  }

  /**
   * Gets the first character of name.
   *
   * @return the first character of name
   * @since 1.2.0-GA
   */
  public char getFirstCharacterOfName() { return firstCharacterOfName; }

  /**
   * Sets the first character of name.
   *
   * @param firstCharacterOfName the first character of name
   * @since 1.2.0-GA
   */
  public void setFirstCharacterOfName(char firstCharacterOfName) {
    this.firstCharacterOfName = firstCharacterOfName;
  }

  public String getAll() {
    return all;
  }

  public void setAll(String all) {
    this.all = SpaceUtils.processUnifiedSearchCondition(all);
  }

  public boolean isSortingEmpty() {
    return sorting == null;
  }

  public Sorting getSorting() {
    if (sorting == null) {
      return new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    }
    return sorting;
  }

  public void setSorting(Sorting sorting) {
    this.sorting = sorting;
  }
  
  public Identity getViewerIdentity() {
    return viewerIdentity;
  }

  public void setViewerIdentity(Identity currentIdentity) {
    if (currentIdentity == null && this.viewerIdentity != null && this.excludedIdentityList != null) {
      this.excludedIdentityList.remove(this.viewerIdentity);
    }
    this.viewerIdentity = currentIdentity;
    if (currentIdentity == null) {
      return;
    }
    if(this.excludedIdentityList == null) {
      this.excludedIdentityList = new ArrayList<Identity>();
    }
    if(!this.excludedIdentityList.contains(currentIdentity)) {
      this.excludedIdentityList.add(this.viewerIdentity);
    }
  }

  public void setRemoteIds(List<String> remoteIds) {
    this.remoteIds = remoteIds;
  }

  public List<String> getRemoteIds() {
    return remoteIds;
  }

  public String getFirstCharFieldName() {
    return firstCharFieldName;
  }

  public void setFirstCharFieldName(String firstCharField) {
    this.firstCharFieldName = firstCharField;
  }

  public boolean isEmpty() {
    return StringUtils.isBlank(this.all)
        && StringUtils.isBlank(this.name)
        && StringUtils.isBlank(this.company)
        && StringUtils.isBlank(this.position)
        && StringUtils.isBlank(this.skills)
        && (this.remoteIds == null || this.remoteIds.isEmpty())
        && (this.excludedIdentityList == null || this.excludedIdentityList.isEmpty() || (this.excludedIdentityList.size() == 1 && this.viewerIdentity != null && this.excludedIdentityList.contains(this.viewerIdentity)));
  }

  @Override
  public ProfileFilter clone() throws CloneNotSupportedException {
    return (ProfileFilter) super.clone();
  }
}

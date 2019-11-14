/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import java.util.List;

public class ProfileEntity extends BaseEntity {
  private static final long serialVersionUID = -3241490307391015454L;
  private List<DataEntity> phones;
  public ProfileEntity() {
  }

  public ProfileEntity(String id) {
    super(id);
  }
  public ProfileEntity setIdentity(String identity) {
    setProperty("identity", identity);
    return this;
  }

  public String getIdentity() {
    return getString("identity");
  }

  public ProfileEntity setUsername(String username) {
    setProperty("username", username);
    return this;
  }

  public String getUsername() {
    return getString("username");
  }

  public ProfileEntity setFirstname(String firstname) {
    setProperty("firstname", firstname);
    return this;
  }

  public String getFirstname() {
    return getString("firstname");
  }

  public ProfileEntity setLastname(String lastname) {
    setProperty("lastname", lastname);
    return this;
  }

  public String getLastname() {
    return getString("lastname");
  }

  public ProfileEntity setFullname(String fullname) {
    setProperty("fullname", fullname);
    return this;
  }

  public String getFullname() {
    return getString("fullname");
  }

  public ProfileEntity setGender(String gender) {
    setProperty("gender", gender);
    return this;
  }

  public String getGender() {
    return getString("gender");
  }

  public ProfileEntity setEmail(String email) {
    setProperty("email", email);
    return this;
  }

  public String getEmail() {
    return getString("email");
  }

  public ProfileEntity setPosition(String position) {
    setProperty("position", position);
    return this;
  }

  public String getPosition() {
    return getString("position");
  }

  public ProfileEntity setAvatar(String avatar) {
    setProperty("avatar", avatar);
    return this;
  }

  public String getAvatar() {
    return getString("avatar");
  }

  public void setPhones(List<DataEntity> phones) {
    this.phones = phones;
  }

  public List<DataEntity> getPhones() {
    return phones;
  }
  
  public ProfileEntity setExperiences(List<DataEntity> experiences) {
    setProperty("experiences", experiences);
    return this;
  }
  
  public List<DataEntity> getExperiences() {
    return (List<DataEntity>)getProperty("experiences");
  }
  
  public ProfileEntity setIms(List<DataEntity> ims) {
    setProperty("ims", ims);
    return this;
  }

  public List<DataEntity> getIMs() {
    return (List<DataEntity>)getProperty("ims");
  }
  
  public ProfileEntity setUrls(List<DataEntity> urls) {
    setProperty("url", urls);
    return this;
  }

  public List<DataEntity> getUrls() {
    return (List<DataEntity>)getProperty("url");
  }
  
  public ProfileEntity setDeleted(Boolean deleted) {
    setProperty("deleted", deleted);
    return this;
  }

  public String getDeleted() {
    return getString("deleted");
  }

  public boolean isNotValid() {
    return isEmpty(getUsername()) || isEmpty(getEmail()) 
         || isEmpty(getFirstname()) || isEmpty(getLastname()); 
  }
  
  private boolean isEmpty(String input) {
    return input == null || input.length() == 0;
  }

  public ProfileEntity setEnabled(boolean enable) {
    setProperty("enabled", enable);
    return this;
  }

  public String isEnabled() {
    return getString("enabled");
  }
}

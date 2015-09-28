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

public class UserEntity extends BaseEntity {
  private static final long serialVersionUID = -3241490307391015454L;

  public UserEntity() {
  }

  public UserEntity(String id) {
    super(id);
  }
  public UserEntity setIdentity(String identity) {
    setProperty("identity", identity);
    return this;
  }

  public String getIdentity() {
    return getString("identity");
  }

  public UserEntity setUsername(String username) {
    setProperty("username", username);
    return this;
  }

  public String getUsername() {
    return getString("username");
  }

  public UserEntity setPassword(String password) {
    setProperty("password", password);
    return this;
  }
  
  public String getPassword() {
    return getString("password");
  }

  public UserEntity setFirstname(String firstname) {
    setProperty("firstname", firstname);
    return this;
  }

  public String getFirstname() {
    return getString("firstname");
  }

  public UserEntity setLastname(String lastname) {
    setProperty("lastname", lastname);
    return this;
  }

  public String getLastname() {
    return getString("lastname");
  }

  public UserEntity setFullname(String fullname) {
    setProperty("fullname", fullname);
    return this;
  }

  public String getFullname() {
    return getString("fullname");
  }

  public UserEntity setGender(String gender) {
    setProperty("gender", gender);
    return this;
  }

  public String getGender() {
    return getString("gender");
  }

  public UserEntity setEmail(String email) {
    setProperty("email", email);
    return this;
  }

  public String getEmail() {
    return getString("email");
  }

  public UserEntity setPosition(String position) {
    setProperty("position", position);
    return this;
  }

  public String getPosition() {
    return getString("position");
  }

  public UserEntity setAvatar(String avatar) {
    setProperty("avatar", avatar);
    return this;
  }

  public String getAvatar() {
    return getString("avatar");
  }

  public UserEntity setPhones(List<DataEntity> phones) {
    setProperty("phones", phones);
    return this;
  }

  public UserEntity setExperiences(List<DataEntity> experiences) {
    setProperty("experiences", experiences);
    return this;
  }
  
  public UserEntity setIms(List<DataEntity> ims) {
    setProperty("ims", ims);
    return this;
  }

  public UserEntity setUrls(List<DataEntity> urls) {
    setProperty("urls", urls);
    return this;
  }

  public UserEntity setDeleted(Boolean deleted) {
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
}

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
package org.exoplatform.social.core.identity;


/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 15, 2009
 */

/**
 * This class using for filter profile of identity
 */
public class ProfileFiler {
  /* filer by user profile name*/
  private String userName;
  /* filer by user profile position*/
  private String position;
  /* filer by user profile company*/
  private String company;
  /* filer by user profile gender*/
  private String gender;
  
  public String getPosition() { return position; }
  public void setPosition(String position) { this.position = position; }
  public String getCompany() { return company; }
  public void setCompany(String company) { this.company = company; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public void setUserName(String userName) { this.userName = userName; }
  public String getUserName() { return userName; }
}
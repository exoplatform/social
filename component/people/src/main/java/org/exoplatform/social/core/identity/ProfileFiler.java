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

// TODO: Auto-generated Javadoc
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
  /* filer by user profile gender*/
  /** The gender. */
  private String gender;

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
  public void setPosition(String position) { this.position = position; }

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
  public void setCompany(String company) { this.company = company; }

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
  public void setSkills(String skills) { this.skills = skills;}

  /**
   * Gets the gender.
   *
   * @return the gender
   */
  public String getGender() { return gender; }

  /**
   * Sets the gender.
   *
   * @param gender the new gender
   */
  public void setGender(String gender) { this.gender = gender; }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) { this.name = name; }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() { return name; }
}

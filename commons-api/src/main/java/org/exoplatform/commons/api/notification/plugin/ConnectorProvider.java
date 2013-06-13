/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.plugin;

import java.util.ArrayList;
import java.util.List;


public class ConnectorProvider {
  private String              id;

  private String              name;

  private String              isActive       = "false";

  private String              template;

  private List<MailParameter> mailParameters = new ArrayList<MailParameter>();

  public ConnectorProvider() {
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the isActive
   */
  public String getIsActive() {
    return isActive;
  }

  /**
   * @return the boolean of isActive
   */
  public boolean isActive() {
    return Boolean.valueOf(getIsActive());
  }

  /**
   * @param isActive the isActive to set
   */
  public void setIsActive(String isActive) {
    this.isActive = isActive;
  }

  /**
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * @param template the template to set
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * @return the mailParameters
   */
  public List<MailParameter> getMailParameters() {
    return mailParameters;
  }

  /**
   * @param mailParameters the mailParameters to set
   */
  public void setMailParameters(List<MailParameter> mailParameters) {
    this.mailParameters = mailParameters;
  }

}

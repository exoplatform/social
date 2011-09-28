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

import java.util.Map;

/**
 * The Activity Input model for Social Rest APIs.
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.3
 */
public class ActivityRestIn {
  /**
   * The title.
   */
  private String title;
  
  /**
   * The activity type.
   */
  private String type;
  
  /**
   * The priority from 0 to 1. 1 is the higher priority.
   */
  private Float priority;
  
  /**
   * The title id.
   */
  private String titleId;
  /**
   * The template parameters.
   */
  private Map<String, String> templateParams;

  /**
   * Gets the activity title, required value.
   *
   * @return the activity title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the activity title, required value.
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the activity type, optional value.
   *
   * @return the activity type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the activity type, optional value.
   *
   * @param type the activity type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the activity priority, optional value.
   *
   * @return the activity priority.
   */
  public Float getPriority() {
    return priority;
  }

  /**
   * Sets the activity priority, optional value.
   *
   * @param priority the activity priority
   */
  public void setPriority(Float priority) {
    this.priority = priority;
  }

  /**
   * Gets activity's title id, optional value.
   *
   * @return the activity title's id
   */
  public String getTitleId() {
    return titleId;
  }

  /**
   * Sets activity's title id, optional value.
   *
   * @param titleId the activity title's id
   */
  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }

  /**
   * Gets the activity's template params, optional value.
   *
   * @return the activity's template params
   */
  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  /**
   * Sets the activity's template params, optional value.
   *
   * @param templateParams the template params
   */
  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  /**
   * Utility to check if this object contains valid inputs.
   *
   * @return true or false
   */
  public boolean isValid() {
    if (title == null || title.isEmpty()) {
      return false;
    }
    return true;
  }
}

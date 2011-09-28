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
  
  
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Float getPriority() {
    return priority;
  }
  public void setPriority(Float priority) {
    this.priority = priority;
  }
  public String getTitleId() {
    return titleId;
  }
  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }
  public Map<String, String> getTemplateParams() {
    return templateParams;
  }
  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }
}

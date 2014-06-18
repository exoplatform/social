/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.extras.injection;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 18, 2014  
 */
public class PatternInjectorConfig {
  
  /** . */
  private final static String SPACE_SUFFIX_NAME = "space-suffix-length";
  
  /** . */
  private final static String USER_SUFFIX_NAME = "user-suffix-length";
  
  /** . */
  private final static String USER_PASSWORD_NAME = "user-password-default";
  
  /** . */
  private int spaceSuffixValue;
  
  /** . */
  private int userSuffixValue;
  
  /** . */
  private String userPasswordValue;
  
  public PatternInjectorConfig(InitParams params) {
    ValueParam suffix = params.getValueParam(SPACE_SUFFIX_NAME);
    if (suffix != null) {
      try {
        spaceSuffixValue = Integer.valueOf(suffix.getValue());
      } catch (Exception e) {
        spaceSuffixValue = -1;
      }
    } else {
      spaceSuffixValue = -1;
    }

    suffix = params.getValueParam(USER_SUFFIX_NAME);
    if (suffix != null) {
      try {
        userSuffixValue = Integer.valueOf(suffix.getValue());
      } catch (Exception e) {
        userSuffixValue = -1;
      }
    } else {
      userSuffixValue = -1;
    }
    
    ValueParam param = params.getValueParam(USER_PASSWORD_NAME);
    if (param != null) {
      try {
        userPasswordValue = param.getValue();
      } catch (Exception e) {
        userPasswordValue = "exo";
      }
    } else {
      userPasswordValue = "exo";
    }
  }

  public int getSpaceSuffixValue() {
    return spaceSuffixValue;
  }

  public int getUserSuffixValue() {
    return userSuffixValue;
  }

  public String getUserPasswordValue() {
    return userPasswordValue;
  }
  
}

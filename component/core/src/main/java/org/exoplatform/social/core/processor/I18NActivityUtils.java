/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.core.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class I18NActivityUtils {

  /** */
  private final static String RESOURCE_BUNDLE_VALUES_PARAM = "RESOURCE_BUNDLE_VALUES_PARAM";

  private final static String RESOURCE_BUNDLE_KEY_TO_PROCESS = "RESOURCE_BUNDLE_KEY_TO_PROCESS";
  
  /** */
  private final static String RESOURCE_BUNDLE_VALUES_CHARACTER = "#";
  
  /** */
  private final static String RESOURCE_BUNDLE_ESCAPE_CHARACTER = "${_}";
  
  /** */
  private final static String RESOURCE_BUNDLE_ESCAPE_KEY_CHARACTER = "${-}";
  
  /** */
  private final static String RESOURCE_BUNDLE_KEYS_CHARACTER = ",";
  
  /**
   * Checks activity needs to process multi keys.
   * @param activity
   * @return
   */
  public static boolean isProcessMultiKeys(ExoSocialActivity activity) {
    String[] got = getResourceKeys(activity);
    return got.length > 1;
  }
  
  /**
   * Gets list of values in template params
   * @param valueParam
   * @return
   */
  public static String[] getParamValues(String valueParam) {
    if (valueParam == null) {
      return null;
    }
    String[] got = valueParam.split(RESOURCE_BUNDLE_VALUES_CHARACTER);
    for(int i = 0; i<got.length; i++) {
      got[i] = postProcess(got[i].trim());
    }
    
    return got;
  }
  
  /**
   * Gets list of resource bundle keys
   * @param activity
   * @return
   */
  public static String[] getResourceKeys(ExoSocialActivity activity) {
    String resourceKeys = activity.getTitleId();
    String[] got = resourceKeys.split(RESOURCE_BUNDLE_KEYS_CHARACTER);
    return got;
  }
  
  /**
   * Gets list of resource bundle keys
   * @param activity
   * @return
   */
  public static String[] getResourceValues(ExoSocialActivity activity) {
    
    Map<String, String> params = activity.getTemplateParams();
    if (params == null) {
      return null;
    }
    
    //
    String v = params.get(RESOURCE_BUNDLE_VALUES_PARAM);
    String[] got = v.split(RESOURCE_BUNDLE_KEYS_CHARACTER);
    
    for(int i = 0; i<got.length; i++) {
      got[i] = postKeyProcess(got[i].trim());
    }
    
    //
    return got;
  }
  
  /**
   * Adds ResouceBundle key into existing ExoSocialActivity.
   * 
   * @param activity
   * @param key
   * @param values
   */
  public static void addResourceKey(ExoSocialActivity activity, String key, String...values) {
    if (activity == null) {
      return;
    }
    
    //
    Map<String, String> params = activity.getTemplateParams();
    if (params == null) {
      params = new LinkedHashMap<String, String>();
    }
    
    //
    String titleId = activity.getTitleId();
    if (titleId != null) {
      activity.setTitleId(String.format("%s,%s", titleId, key));
    } else {
      activity.setTitleId(key);
    }
    
    //
    String newValue = transformValuesToString(values);
    String oldValue = params.get(RESOURCE_BUNDLE_VALUES_PARAM);
    if (oldValue != null || titleId != null) {
      String s = String.format("%s,%s",oldValue, newValue);
      params.put(RESOURCE_BUNDLE_VALUES_PARAM, s);
    } else {
      params.put(RESOURCE_BUNDLE_VALUES_PARAM, newValue);
    }
    
    activity.setTemplateParams(params);
  }

  /**
   * Keep which resource key should be process to remove invalid html tag
   * @param activity the activity
   * @param key the key
   */
  public static void addResourceKeyToProcess(ExoSocialActivity activity, String key) {
    if (activity == null) {
      return;
    }

    //
    Map<String, String> params = activity.getTemplateParams();
    if (params == null) {
      params = new LinkedHashMap<String, String>();
    }

    String value = key;
    String oldValue = params.get(RESOURCE_BUNDLE_KEY_TO_PROCESS);
    if (oldValue != null) {
      String s = String.format("%s,%s", oldValue, key);
      params.put(RESOURCE_BUNDLE_KEY_TO_PROCESS, s);
    } else {
      params.put(RESOURCE_BUNDLE_KEY_TO_PROCESS, key);
    }

    activity.setTemplateParams(params);
  }

  /**
   * Get list of resource key which need to process
   * @param activity
   * @return list of key
   */
  public static List<String> getResourceKeysToProcess(ExoSocialActivity activity) {
    if (activity == null) {
      return Collections.emptyList();
    }

    //
    Map<String, String> params = activity.getTemplateParams();
    if (params == null || !params.containsKey(RESOURCE_BUNDLE_KEY_TO_PROCESS)) {
      return Collections.emptyList();
    }

    return Arrays.asList(params.get(RESOURCE_BUNDLE_KEY_TO_PROCESS).split(","));
  }

  /**
   * Escapse # character into values
   * @param value
   * @return
   */
  private static String postProcess(String value) {
    return value.replace(RESOURCE_BUNDLE_ESCAPE_CHARACTER, RESOURCE_BUNDLE_VALUES_CHARACTER);
  }
  
  /**
   * Escape , character into values by ${-}
   * @param value
   * @return
   */
  private static String preKeyProcess(String value) {
    return value.replace(RESOURCE_BUNDLE_KEYS_CHARACTER, RESOURCE_BUNDLE_ESCAPE_KEY_CHARACTER);
  }
  
  /**
   * Escape , character into values
   * @param value
   * @return
   */
  private static String postKeyProcess(String value) {
    return value.replace(RESOURCE_BUNDLE_ESCAPE_KEY_CHARACTER, RESOURCE_BUNDLE_KEYS_CHARACTER);
  }
  
  /**
   * Escapse # character into values by ${_}
   * @param value
   * @return
   */
  private static String preProcess(String value) {
    return value.replace(RESOURCE_BUNDLE_VALUES_CHARACTER, RESOURCE_BUNDLE_ESCAPE_CHARACTER);
  }
  
  /**
   * 
   * @param values
   * @return
   */
  private static String transformValuesToString(String...values) {
    //
    if (values == null) {
      return " ";
    }
    
    //
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for(String s : values) {
      if (s == null || s.length() == 0) {
        sb.append(" ");
      } else {
        s = preProcess(s);
        s = preKeyProcess(s);
        sb.append(s);
      }
      
      
      if (++count < values.length) {
        sb.append(RESOURCE_BUNDLE_VALUES_CHARACTER);
      }
    }
    
    return sb.toString();
  }
  
}

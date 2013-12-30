/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.social.common.ResourceBundleUtil;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * Processes any i18n on activity to a dynamic new activity with the i18n title.
 * <p/>
 * How to I18N-ize an activity:
 * <ul>
 *   <li>
 *     Set <em>titleId</em> to indicate it is an I18N activity. <em>titleId</em> is used to map with a specific message
 *     bundle key via configuration.
 *   </li>
 *   <li>
 *     If that resource bundle message is a compound resource bundle message, provide templateParams. The argument number will
 *     be counted as it appears on the map.
 *     For example: templateParams = {"key1": "value1", "key2": "value2"} => message bundle arguments = ["value1", "value2"].
 *     Note: To reserve the order of elements, LinkedHashMap must be used to create templateParams.
 *   </li>
 *   <li>
 *     Create a resource bundle file and this file name is called "resourceBundleKeyFile" for configuration later.
 *   </li>
 *   <li>
 *     Register the resource bundle file with {@link BaseResourceBundlePlugin} to
 *     {@link ResourceBundleService}.
 *   </li>
 *   <li>
 *     Register {@link ActivityResourceBundlePlugin} with this service.
 *   </li>
 * </ul>
 *
 * @since 1.2.8
 * 
 * @see org.exoplatform.social.core.processor.ActivityResourceBundlePlugin
 */
public final class I18NActivityProcessor {

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(I18NActivityProcessor.class);

  /**
   * The map of registered resource bundle plugins. The key is activityType.
   */
  private Map<String, ActivityResourceBundlePlugin> resourceBundlePluginMap;

  /**
   * The resource bundle service for getting associated {@link ResourceBundle}.
   */
  private ResourceBundleService resourceBundleService;

  /**
   * Constructor.
   */
  public I18NActivityProcessor() {

  }

  /**
   * Registers an activity resource bundle plugin.
   *
   * @param activityResourceBundlePlugin The activity resource bundle plugin.
   * @LevelAPI Platform
   */
  public void addActivityResourceBundlePlugin(ActivityResourceBundlePlugin activityResourceBundlePlugin) {
    //this could be a bug from exojcr as component plugin is not set name on it's constructor.
    activityResourceBundlePlugin.setActivityType(activityResourceBundlePlugin.getName());

    if (!activityResourceBundlePlugin.isValid()) {
      LOG.warn("Failed to register the plugin: not valid");
      return;
    }
    if (resourceBundlePluginMap == null) {
      resourceBundlePluginMap = new HashMap<String, ActivityResourceBundlePlugin>();
    }
    resourceBundlePluginMap.put(activityResourceBundlePlugin.getActivityType(),
            activityResourceBundlePlugin);
  }

  /**
   * Unregisters an existing registered resource bundle plugin.
   *
   * @param activityResourceBundlePlugin The existing activity resource bundle plugin.
   * @LevelAPI Platform
   */
  public void removeActivityResourceBundlePlugin(ActivityResourceBundlePlugin activityResourceBundlePlugin) {
    if (!activityResourceBundlePlugin.isValid()) {
      LOG.warn("Failed to remove the plugin: not valid");
    }
    if (resourceBundlePluginMap == null) {
      LOG.info("resourceBundlePluginMap is null.");
      return;
    }
    resourceBundlePluginMap.remove(activityResourceBundlePlugin.getActivityType());
  }

  /**
   * Processes the I18N activity which means that activity.getTitleId() != null.
   *
   * @param i18nActivity The target activity to be processed.
   *        
   * @param selectedLocale The target locale that activity will be localized.
   *
   * @return The activity which content has been localized.
   * 
   * @LevelAPI Platform
   */
  public ExoSocialActivity process(ExoSocialActivity i18nActivity, Locale selectedLocale) {
    //only processes I18N activity type
    if (i18nActivity.getTitleId() != null) {
      if (activityTypeRegistered(i18nActivity)) {
        ResourceBundle resourceBundle = getResourceBundle(i18nActivity, selectedLocale);
        if (resourceBundle == null) {
          LOG.warn("no resource bundle key found registered for: " + getResourceBundleKeyFile(i18nActivity));
          return i18nActivity;
        }
        if (getMessageBundleKey(i18nActivity) == null) {
          return i18nActivity;
        }
        String newTitle = appRes(resourceBundle, getMessageBundleKey(i18nActivity), i18nActivity.getTemplateParams());
        if (newTitle != null) {
          i18nActivity.setTitle(newTitle);
        }
      }
    }
    return i18nActivity;
  }
  
  /**
   * Processes the I18N activity which means that activity.getTitleId() != null.
   *
   * @param i18nActivity The I18N activity.
   * @param selectedLocale The selected locale.
   *
   * @return The new activity with I18N title.
   * @LevelAPI Platform
   */
  public ExoSocialActivity processKeys(ExoSocialActivity i18nActivity, Locale selectedLocale) {
    //only processes I18N activity type
    if (i18nActivity.getTitleId() != null) {
      if (activityTypeRegistered(i18nActivity)) {
        ResourceBundle resourceBundle = getResourceBundle(i18nActivity, selectedLocale);
        if (resourceBundle == null) {
          LOG.warn("no resource bundle key found registered for: " + getResourceBundleKeyFile(i18nActivity));
          return i18nActivity;
        }
        if (getMessageBundleKeys(i18nActivity) == false) {
          return i18nActivity;
        }
        
        transformKeys(i18nActivity, resourceBundle);
      }
    }
    return i18nActivity;
  }
  
  /**
   * Sets the external resource bundle service.
   *
   * @param resourceBundleService The resource bundle service.
   * @LevelAPI Platform
   */
  public void setResourceBundleService(ResourceBundleService resourceBundleService) {
    this.resourceBundleService = resourceBundleService;
  }

  private ExoSocialActivity transformKeys(ExoSocialActivity i18nActivity, ResourceBundle resourceBundle) {

    String[] resourceKeys = I18NActivityUtils.getResourceKeys(i18nActivity);
    String[] resourceParamValues = I18NActivityUtils.getResourceValues(i18nActivity);
    String type = i18nActivity.getType();

    int count = 0;
    StringBuilder sb = new StringBuilder();
    String[] valuesOfParam = null;

    //
    String key = null;
    for (int i = 0; i < resourceKeys.length; i++) {
      //
      key = resourceKeys[i];

      //
      valuesOfParam = I18NActivityUtils.getParamValues(resourceParamValues[i]);

      String title = appRes(resourceBundle, getMessageBundleKey(type, key), valuesOfParam);
      sb.append(title);

      if (++count < resourceKeys.length) {
        sb.append("<br/>");
      }
    }

    //
    String newTitle = sb.toString();
    if (newTitle.length() > 0) {
      i18nActivity.setTitle(newTitle);
    }

    return i18nActivity;
  }
  
  /**
   * Checks if the i18n activity has registered the activity resource bundle plugin.
   *
   * @param i18nActivity The i18n activity.
   * @return A boolean value.
   */
  private boolean activityTypeRegistered(ExoSocialActivity i18nActivity) {
    if (resourceBundlePluginMap == null) {
      return false;
    }
    return resourceBundlePluginMap.containsKey(i18nActivity.getType());
  }

  /**
   * Gets an associated registered message bundle key for the i18n activity's titleId.
   *
   * @param i18nActivity The i18n activity.
   * @return The registered message bundle key.
   */
  private String getMessageBundleKey(ExoSocialActivity i18nActivity) {
    ActivityResourceBundlePlugin resourceBundlePlugin = resourceBundlePluginMap.get(i18nActivity.getType());
    return resourceBundlePlugin.getMessageBundleKey(i18nActivity.getTitleId());
  }
  
  /**
   * Gets associated registered message bundle keys for the i18n activity's titleId.
   *
   * @param i18nActivity The i18n activity.
   * @return The registered message bundle key.
   */
  private boolean getMessageBundleKeys(ExoSocialActivity i18nActivity) {
    ActivityResourceBundlePlugin resourceBundlePlugin = resourceBundlePluginMap.get(i18nActivity.getType());
    String[] keys = I18NActivityUtils.getResourceKeys(i18nActivity);
    
    for(String key : keys) {
      if (resourceBundlePlugin.getMessageBundleKey(key) != null) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Gets an associated registered message bundle key for the i18n activity's titleId.
   *
   * @param i18nActivity The i18n activity.
   * @return The registered message bundle key.
   */
  private String getMessageBundleKey(String activityType, String resourceBundleKey) {
    ActivityResourceBundlePlugin resourceBundlePlugin = resourceBundlePluginMap.get(activityType);
    return resourceBundlePlugin.getMessageBundleKey(resourceBundleKey);
  }

  /**
   * Gets an associated resource bundle key file registered with this type of activity.
   *
   * @param i18nActivity The i18n activity.
   * @return The associated resource bundle key file.
   */
  private String getResourceBundleKeyFile(ExoSocialActivity i18nActivity) {
    return resourceBundlePluginMap.get(i18nActivity.getType()).getResourceBundleKeyFile();
  }

  /**
   * Gets an associated message bundle value from a specific message bundle key. If templateParams is not null, try to resolve
   * the detected compound message bundle.
   *
   * @param resourceBundle The registered resource bundle from the resource bundle key file.
   * @param msgKey The message key.
   * @param templateParams The possible template params for resolving compound message bundle.
   * @return The message bundle value or null if not found.
   */
  private String appRes(ResourceBundle resourceBundle, String msgKey, Map<String, String> templateParams) {

    String value = appRes(resourceBundle, msgKey);
    List<String> arguments = null;
    if (templateParams != null) {
      arguments = new ArrayList(templateParams.values());
    }
    if (arguments != null && arguments.size() > 0) {
      value = ResourceBundleUtil.replaceArguments(value, arguments);
    }
    return value;
  }
  
  /**
   * Gets an associated message bundle value from a specific message bundle key. If templateParams is not null, try to resolve
   * the detected compound message bundle.
   *
   * @param resourceBundle The registered resource bundle from the resource bundle key file.
   * @param msgKey The message key.
   * @param templateParams The possible template params for resolving the compound message bundle.
   * @return The message bundle value or null if not found.
   */
  private String appRes(ResourceBundle resourceBundle, String msgKey, String[] values) {

    String value = appRes(resourceBundle, msgKey);
    List<String> arguments = null;
    if (values != null) {
      arguments = Arrays.asList(values);
    }
    if (arguments != null && arguments.size() > 0) {
      value = ResourceBundleUtil.replaceArguments(value, arguments);
    }
    return value;
  }

  /**
   * Finds a message bundle value from a message bundle key.
   *
   * @param res The resource bundle.
   * @param msgKey The message bundle key.
   * @return The message bundle value or null if not found.
   */
  private String appRes(ResourceBundle res, String msgKey) {
    String value = null;
    try {
      value = res.getString(msgKey);
    } catch (MissingResourceException ex) {
      LOG.warn("Failed find message bundle value for key : " + msgKey);
    }
    return value;
  }


  /**
   * Gets an associated registered resource bundle from an i18n activity and the selected locale.
   *
   * @param i18nActivity The i18n activity.
   * @param selectedLocale The selected locale.
   * @return The associated registered resource bundle.
   */
  private ResourceBundle getResourceBundle(ExoSocialActivity i18nActivity, Locale selectedLocale) {
    if (resourceBundleService == null) {
      resourceBundleService = (ResourceBundleService) PortalContainer.getInstance().
              getComponentInstanceOfType(ResourceBundleService.class);
    }
    if (resourceBundleService == null) {
      LOG.error("Failed to get resourceBundleService for I18N activity type.");
      return null;
    }
    if (resourceBundlePluginMap == null || resourceBundlePluginMap.size() == 0) {
      LOG.warn("No registered activity resource bundle");
      return null;
    }
    ActivityResourceBundlePlugin resourceBundlePlugin = resourceBundlePluginMap.get(i18nActivity.getType());
    return resourceBundleService.getResourceBundle(resourceBundlePlugin.getResourceBundleKeyFile(), selectedLocale);
  }

}

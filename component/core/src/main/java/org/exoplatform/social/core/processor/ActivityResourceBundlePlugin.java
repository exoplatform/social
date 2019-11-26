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

import java.util.Iterator;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The activity resource bundle plugin for registering external resource bundle
 * for I18N activity type.
 *
 * Must set titleId for ExoSocialActivity model to indicate that it's i18n activity and that titleId is used to map with message
 * bundle key via configuration.
 * Sample code:
 * <pre>
 * &lt;external-component-plugins&gt;
 *    &lt;target-component&gt;org.exoplatform.social.core.processor.I18NActivityProcessor&lt;/target-component&gt;
 *    &lt;component-plugin&gt;
 *      &lt;name&gt;exosocial:spaces&lt;/name&gt; &lt;!-- activity type --&gt;
 *      &lt;set-method&gt;addActivityResourceBundlePlugin&lt;/set-method&gt;
 *      &lt;type&gt;org.exoplatform.social.core.processor.ActivityResourceBundlePlugin&lt;/type&gt;
 *      &lt;init-params&gt;
 *        &lt;object-param&gt;
 *          &lt;name&gt;locale.social.Core&lt;/name&gt; &lt;!-- resource bundle key file --&gt;
 *          &lt;description&gt;activity key type resource bundle mapping for exosocial:spaces&lt;/description&gt;
 *          &lt;object type=&quot;org.exoplatform.social.core.processor.ActivityResourceBundlePlugin&quot;&gt;
 *            &lt;field name=&quot;activityKeyTypeMapping&quot;&gt;
 *              &lt;map type=&quot;java.util.HashMap&quot;&gt;
 *                &lt;entry&gt;
 *                  &lt;key&gt;&lt;string&gt;space_created&lt;/string&gt;&lt;/key&gt;
 *                  &lt;value&gt;&lt;string&gt;SpaceActivityPublisher.space_created&lt;/string&gt;&lt;/value&gt;
 *                &lt;/entry&gt;
 *              &lt;/map&gt;
 *            &lt;/field&gt;
 *          &lt;/object&gt;
 *        &lt;/object-param&gt;
 *      &lt;/init-params&gt;
 *    &lt;/component-plugin&gt;
 *  &lt;/external-component-plugins&gt;
 * </pre>
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.8
 * @since Feb 6, 2012.
 */
public class ActivityResourceBundlePlugin extends BaseComponentPlugin {

  /**
   * The logger
   */
  private static final Log LOG = ExoLogger.getLogger(ActivityResourceBundlePlugin.class);

  /**
   * The associated activity type.
   */
  private String activityType;

  /**
   * The associated titleId and message bundle key mapping for this activity type.
   */
  private Map<String, String> activityKeyTypeMapping;

  /**
   * The associated resource bundle key file
   */
  private String resourceBundleKeyFile;

  /**
   * Default constructor
   */
  public ActivityResourceBundlePlugin() {

  }

  /**
   * Constructor for getting activityType, activityKeyTypeMapping, and resourceBundleKeyFile from init params.
   *
   * @param initParams the init params.
   */
  public ActivityResourceBundlePlugin(InitParams initParams) {
    if (initParams == null) {
      LOG.warn("Failed to register this plugin: initParams is null");
      return;
    }
    Iterator<ObjectParameter> itr = initParams.getObjectParamIterator();
    if (!itr.hasNext()) {
      LOG.warn("Failed to register this plugin: no <object-param>");
      return;
    }
    ObjectParameter objectParameter = itr.next();
    if (objectParameter.getName() == null || objectParameter.getName().isEmpty()) {
      LOG.warn("Failed to register this plugin: must set name with <object-param> as resource bundle key file");
      return;
    }
    resourceBundleKeyFile = objectParameter.getName();
    ActivityResourceBundlePlugin pluginConfig = (ActivityResourceBundlePlugin) objectParameter.getObject();

    if (pluginConfig.getActivityKeyTypeMapping() == null ||
        pluginConfig.getActivityKeyTypeMapping().size() == 0) {
      LOG.warn("Failed to register this plugin: no <entry> found for <object-param> config");
      return;
    }
    activityKeyTypeMapping = pluginConfig.getActivityKeyTypeMapping();
  }

  /**
   * Gets the associated activity type from this activity resource bundle plugin.
   *
   * @return the associated activity type.
   */
  public String getActivityType() {
    return activityType;
  }

  /**
   * Sets the associated activity type from this activity resource bundle plugin.
   *
   * @param activityType the associated activity type.
   */
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  /**
   * Gets the associated activity key type mapping from this activity resource bundle plugin.
   * @return
   */
  public Map<String, String> getActivityKeyTypeMapping() {
    return activityKeyTypeMapping;
  }

  /**
   * Sets the associated activity key type mapping from this activity resource bundle plugin.
   *
   * @param mapping the hash map of key as titleId and value as message bundle key
   */
  public void setActivityKeyTypeMapping(Map<String, String> mapping) {
    if (mapping == null || mapping.size() == 0) {
      LOG.warn("mapping is null or size = 0");
      return;
    }
    activityKeyTypeMapping = mapping;
  }

  /**
   * Gets the associated resource bundle key file for getting resource bundle via ResourceBundleService.
   *
   * @return the associated resource bundle key file.
   */
  public String getResourceBundleKeyFile() {
    return resourceBundleKeyFile;
  }

  /**
   * Checks if this activity resource bundle plugin contains a specific activity titleId.
   *
   * @param activityTitleId the activity's titleId.
   * @return a boolean value
   */
  public boolean hasMessageBundleKey(String activityTitleId) {
    if (activityKeyTypeMapping == null || activityKeyTypeMapping.size() == 0) {
      return false;
    }
    return activityKeyTypeMapping.containsKey(activityTitleId);
  }

  /**
   * Gets the registered message bundle key for a specific activity's titleId.
   *
   * @param activityTitleId the activity's titleId.
   * @return the found registered message bundle key or null if not found.
   */
  public String getMessageBundleKey(String activityTitleId) {
    if (hasMessageBundleKey(activityTitleId)) {
      return activityKeyTypeMapping.get(activityTitleId);
    }
    return null;
  }

  /**
   * Checks if this is a valid plugin which means that activityType, resourceBundleKeyFile must
   * not be null and activityKeyTypeMapping must contain equal or more than 1 entry.
   *
   * @return a boolean value.
   */
  public boolean isValid() {
    return (activityType != null && (activityKeyTypeMapping != null && activityKeyTypeMapping.size() > 0) &&
            resourceBundleKeyFile != null);
  }

}

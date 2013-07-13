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
package org.exoplatform.social.notification.mock;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.plugin.MappingKey;
import org.exoplatform.commons.api.notification.plugin.TemplateConfigurationPlugin;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.TemplateGeneratorImpl;

public class MockTemplateGeneratorImpl implements TemplateGenerator {

  TemplateGeneratorImpl generatorImpl;
  public MockTemplateGeneratorImpl() {
    generatorImpl = TemplateGeneratorImpl.getInstance();
  }

  @Override
  public String processTemplateIntoString(String providerId, Map<String, String> valueables, String language) {
    MappingKey mappingKey = generatorImpl.getMappingKey(providerId);
    String templateKey = mappingKey.getKeyValue("template", "Notification.template." + providerId);
    String template = NotificationUtils.getResourceBundle(templateKey, null, mappingKey.getLocaleResouceBundle());
    if (valueables != null) {
      for (String findKey : valueables.keySet()) {
        template = StringUtils.replace(template, findKey, valueables.get(findKey));
      }
    }
    return template;
  }

  @Override
  public void registerTemplateConfigurationPlugin(TemplateConfigurationPlugin configurationPlugin) {
    generatorImpl.registerTemplateConfigurationPlugin(configurationPlugin);
  }

  @Override
  public String processSubjectIntoString(String providerId, Map<String, String> valueables, String language) {
    return generatorImpl.processSubjectIntoString(providerId, valueables, language);
  }

  @Override
  public String processDigestIntoString(String providerId, Map<String, String> valueables, String language, int size) {
    return generatorImpl.processDigestIntoString(providerId, valueables, language, size);
  }

 
}

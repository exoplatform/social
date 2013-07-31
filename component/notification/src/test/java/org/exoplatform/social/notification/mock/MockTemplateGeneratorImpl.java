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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.service.template.TemplateGenerator;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.service.template.TemplateGeneratorImpl;

public class MockTemplateGeneratorImpl implements TemplateGenerator {

  TemplateGeneratorImpl generatorImpl;
  public MockTemplateGeneratorImpl() {
    generatorImpl = TemplateGeneratorImpl.getInstance();
  }

  @Override
  public String processTemplate(TemplateContext ctx) {
    TemplateConfig mappingKey = generatorImpl.getTemplateConfig(ctx.getProviderId());
    String templateKey = mappingKey.getKeyValue("template", "Notification.template." + ctx.getProviderId());
    String template = NotificationUtils.getResourceBundle(templateKey, null, mappingKey.getBundlePath());
    if (ctx != null) {
      for (String findKey : ctx.keySet()) {
        template = StringUtils.replace(template, "$" + findKey, (String)ctx.get(findKey));
      }
    }
    return template;
  }

  @Override
  public String processSubject(TemplateContext ctx) {
    return generatorImpl.processSubject(ctx);
  }

  @Override
  public String processDigest(TemplateContext ctx) {
    return generatorImpl.processDigest(ctx);
  }

  @Override
  public String processTemplateInContainer(TemplateContext ctx) {
    return null;
  }

 
}

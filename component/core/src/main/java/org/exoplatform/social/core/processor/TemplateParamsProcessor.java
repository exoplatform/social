/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.processor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;


public class TemplateParamsProcessor extends BaseActivityProcessorPlugin {
  private static final Log LOG = ExoLogger.getLogger(TemplateParamsProcessor.class);
  private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{([^}]*)}");

  public TemplateParamsProcessor(InitParams params) {
    super(params);
  }
  
  public void processActivity(ExoSocialActivity activity) {
    try {
      Map<String,String> params = activity.getTemplateParams();
      activity.setTitle(processTemplate(activity.getTitle(), params));
      activity.setBody(processTemplate(activity.getBody(), params));
    } catch (Exception e) {
      LOG.error("TemplateParamsProcessor error : ", e);
    }
  }

  private String processTemplate(String template, Map<String, String> params) throws Exception {
    if (template == null) {
      return template;
    }
    
    Matcher matcher = PARAMETER_PATTERN.matcher(template);
    int index = 0;
    while (matcher.find(index)) {
      index = matcher.end();
      String templateKey = matcher.group();
      String key = matcher.group(1);
      String value = params.get(key);
      if (value == null) {
        continue;
      }
      template = template.replace(templateKey, value);
    }

    return template;
  }
}
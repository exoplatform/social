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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;


public class TemplateParamsProcessor extends BaseActivityProcessorPlugin {
  private static final Log LOG = ExoLogger.getLogger(TemplateParamsProcessor.class);
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
    
    int start = 0;
    int open;
    int close;
    while ((open = template.indexOf("${", start)) != -1){
      start = close = template.indexOf("}");
      if (close == -1) {
        throw new Exception("Template is invalid. [Do not contain '}']");
      }

      String key = template.substring(open + 2, close);
      String value = params.get(key);
      String templateKey = "${"+key+"}";
      template = template.replace(templateKey, value);
    }

    return template;
  }
}
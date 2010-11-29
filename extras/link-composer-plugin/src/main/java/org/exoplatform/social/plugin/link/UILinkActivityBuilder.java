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
package org.exoplatform.social.plugin.link;

import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 23, 2010  
 */
public class UILinkActivityBuilder extends BaseUIActivityBuilder {
  private static final Log log = ExoLogger.getLogger(UILinkActivityBuilder.class);
  
  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    UILinkActivity uiLinkActivity = (UILinkActivity) uiActivity;
    try {
      Map<String, String> templateParams = activity.getTemplateParams();
      if (templateParams != null) {
          uiLinkActivity.setLinkSource(templateParams.get(UILinkActivityComposer.LINK_PARAM));

          uiLinkActivity.setLinkTitle(templateParams.get(UILinkActivityComposer.TITLE_PARAM));

          uiLinkActivity.setLinkImage(templateParams.get(UILinkActivityComposer.IMAGE_PARAM));

          uiLinkActivity.setLinkDescription(templateParams.get(UILinkActivityComposer.DESCRIPTION_PARAM));

          uiLinkActivity.setLinkComment(templateParams.get(UILinkActivityComposer.COMMENT_PARAM));
      } 
    } catch (Exception e) {
      log.warn("Has error when building link activity at Activity Builder");
    }
  }
}

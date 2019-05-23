/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.social.webui.space;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

@ComponentConfig(
    template = "war:/groovy/social/webui/space/UISpaceTemplateDescription.gtmpl"
)

/**
 * Displays the space template description.
 *
 */
public class UISpaceTemplateDescription extends UIComponent {

  private String templateName;
  private SpaceTemplateService spaceTemplateService;

  /**
   * Constructor
   */
  public UISpaceTemplateDescription() {
    spaceTemplateService = CommonsUtils.getService(SpaceTemplateService.class);
    templateName = spaceTemplateService.getDefaultSpaceTemplate();
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String name) {
    this.templateName = name;
  }
}

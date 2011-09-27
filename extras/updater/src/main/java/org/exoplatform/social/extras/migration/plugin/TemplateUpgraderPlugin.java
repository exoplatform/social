/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.extras.migration.plugin;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.extras.migration.TemplateTool;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class TemplateUpgraderPlugin extends UpgradeProductPlugin {

  public TemplateUpgraderPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(final String oldVersion, final String newVersion) {
    new TemplateTool().run();
  }

  @Override
  public boolean shouldProceedToUpgrade(final String previousVersion, final String newVersion) {
    return VersionUpgrade.from11xTo12x(previousVersion, newVersion);
  }

}
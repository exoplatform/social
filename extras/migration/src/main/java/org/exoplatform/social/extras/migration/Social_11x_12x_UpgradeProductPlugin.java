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

package org.exoplatform.social.extras.migration;


import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.extras.migration.io.WriterContext;
import org.exoplatform.social.extras.migration.rw.NodeReader;
import org.exoplatform.social.extras.migration.rw.NodeWriter;

import javax.jcr.Session;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Social_11x_12x_UpgradeProductPlugin extends UpgradeProductPlugin {

  private static final Log LOG = ExoLogger.getLogger(Social_11x_12x_UpgradeProductPlugin.class);

  public Social_11x_12x_UpgradeProductPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(final String s, final String s1) {

    final String PREVIOUS = "11x";
    final String NEW      = "12x";

    //
    PortalContainer portalContainer = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) portalContainer.getComponentInstanceOfType(ChromatticManager.class);
    SocialChromatticLifeCycle lifeCycle =
        (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);

    //
    Session session = lifeCycle.getSession().getJCRSession();

    try {

      //
      MigrationTool tool = new MigrationTool();
      WriterContext ctx = new WriterContext(session, PREVIOUS, NEW);
      NodeReader reader = tool.createReader(PREVIOUS, NEW, session);
      NodeWriter writer = tool.createWriter(PREVIOUS, NEW, session);

      //
      tool.runAll(reader, writer, ctx);

      //
      tool.commit(reader, writer, ctx);

    }
    catch (Exception e) {
      LOG.error(e);
    }
    finally {
      session.logout();
    }

  }

  @Override
  public boolean shouldProceedToUpgrade(final String previousVersion, final String newVersion) {
    return newVersion.equals("3.5.0");
  }

}

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


import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IndexTool {

  private final Session session;

  private static final Log LOG = ExoLogger.getLogger(IndexTool.class);

  public IndexTool(final Session session) {
    this.session = session;
  }

  public void run() {

    try {

      //
      RequestLifeCycle.begin(PortalContainer.getInstance());

      Node organizationNode = session.getRootNode().getNode("production/soc:providers/soc:organization/");
      NodeIterator userIterator = organizationNode.getNodes();
      while(userIterator.hasNext()) {
        Node user = userIterator.nextNode();
        Node profileNode = user.getNode("soc:profile");
        NodeIterator subProfileNodeIterator = profileNode.getNodes();

        List<String> skills = new ArrayList<String>();
        while (subProfileNodeIterator.hasNext()) {
          Node subProfileNode = subProfileNodeIterator.nextNode();

          if (subProfileNode.getPrimaryNodeType().getName().equals("soc:profilexp")) {
            try {
              String skillsValue = subProfileNode.getProperty("soc:skills").getString();
              skills.add(skillsValue);
            }
            catch(PathNotFoundException e) {
              continue;
            }
          }
        }
        LOG.info("Index skills for " + user.getName());
        profileNode.setProperty("index-skills", skills.toArray(new String[]{}));
      }

    }
    catch (RepositoryException e) {
      LOG.info("Error during indexing profiles : " + e.getMessage(), e);
    }
    finally {
      RequestLifeCycle.end();
    }

  }

}

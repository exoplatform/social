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
package org.exoplatform.social.common;

import javax.jcr.Session;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.social.common.jcr.JCRSessionManager;

/**
 * Abstract Common Test.
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 6, 2010
 * @copyright eXo SAS
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration" +
          ".xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test" +
          ".configuration.xml")
})
public abstract class AbstractCommonTest extends AbstractKernelTest {

  protected PortalContainer portalContainer;
  protected RepositoryService repositoryService;
  protected JCRSessionManager sessionManager;
  protected ExtendedNodeTypeManager nodeTypeManager;
  protected Session session;
  protected final String WORKSPACE = "portal-test";

  @Override
  protected void setUp() throws Exception {
    portalContainer = PortalContainer.getInstance();
    repositoryService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
    sessionManager = new JCRSessionManager(WORKSPACE, repositoryService);

    Session session = sessionManager.getOrOpenSession();
    try {
      nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
    } finally {
      sessionManager.closeSession();
    }
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    end();
  }
}

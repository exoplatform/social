/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.identity;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.core.ManageableRepository;

import javax.jcr.*;

import junit.framework.TestCase;


public class TestIdentityManager extends TestCase {
  protected Node rootNode_;
  protected Node mailHomeNode_;
  protected Node systemNode_;
  protected SimpleCredentials credentials_;
  protected PortalContainer manager_;
  protected Session session_;


  protected Session session;

  protected ManageableRepository repository;

  protected RepositoryService repositoryService;

  protected StandaloneContainer container;

  public void setUp() throws Exception {
    /*StandaloneContainer.addConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");

    container = StandaloneContainer.getInstance();
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/test/java/conf/standalone/login.conf");
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = repositoryService.getDefaultRepository();

    SessionProviderService spService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = spService.getSystemSessionProvider(null);
    session = sessionProvider.getSession("social", repository);*/
  }

  @Override
  protected void tearDown() throws Exception {
    /*if (session != null) {
      Node node = null;
      try {
        session.refresh(false);

        Node rootNode = session.getRootNode();

        if (rootNode.getNode("exo:applications").hasNode("Social_Relationship")) {
          NodeIterator children = rootNode.getNode("exo:applications").getNode("Social_Relationship").getNodes();
          while (children.hasNext()) {
            node = children.nextNode();
            //System.out.println("DELETing Social_Relationship ------------- " + node.getPath());
            node.remove();
          }
        }

        session.save();
        session.refresh(false);

        NodeIterator children = rootNode.getNode("exo:applications").getNode("Social_Identity").getNodes();
        while (children.hasNext()) {
          node = children.nextNode();
          //System.out.println("DELETing ------------- "+node.getPath());
          node.remove();
        }

        session.save();
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
        session.logout();
      }
    }
    super.tearDown();*/
  }

  public void testGetIdentityByRemoteId() throws Exception {
    /*IdentityManager iManager = (IdentityManager) StandaloneContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull(iManager);
    //iManager.addIdentityProvider(new OrganizationIdentityProvider());

    Identity identity = iManager.getIdentityByRemoteId("organization", "john");
    assertNotNull(identity);
    //assertEquals("john", identity.getProfile().getNickname());
    assertEquals("john", identity.getDisplayName());

    iManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    //check if we load it a second time if we get the ID
    identity = iManager.getIdentityByRemoteId("organization", "john");
    assertNotNull(identity);
    assertNotNull("This object should have an id since it has been saved", identity.getId());

    String id = identity.getId();
    iManager.saveIdentity(identity);
    assertEquals("The id should not change after having been saved", id, identity.getId());*/
  }

  public void testGetIdentityById() throws Exception {
    /*IdentityManager iManager = (IdentityManager) StandaloneContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    //iManager.addIdentityProvider(new OrganizationIdentityProvider());

    Identity identity = iManager.getIdentityByRemoteId("organization", "james");
    assertNotNull(identity);

    assertNull(identity.getId());
    iManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    String oldId = identity.getId();
    identity = iManager.getIdentityById(identity.getId());
    assertNotNull(identity);
    assertEquals("this id should still be the same", oldId, identity.getId());*/
  }

  public void testGetWrongId() throws Exception {
    /*IdentityManager iManager = (IdentityManager) StandaloneContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    //iManager.addIdentityProvider(new OrganizationIdentityProvider());

    Identity identity = iManager.getIdentityByRemoteId("organization", "jack");
    assertNull(identity);

    identity = iManager.getIdentityById("wrongID");
    assertNull(identity);*/
  }

}
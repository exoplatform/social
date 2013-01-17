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

import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class MentionsProcessorTest extends AbstractCoreTest {

  private IdentityManager identityManager;
  private Identity rootIdentity, johnIdentity;


  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) PortalContainer.getComponent(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    assertNotNull("rootIdentity.getId() must not be null", rootIdentity.getId());
    assertNotNull("johnIdentity.getId() must not be null", johnIdentity.getId());
  }

  public void tearDown() throws Exception {
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    super.tearDown();
  }

  public void testSubstituteUsernames() throws Exception {
    assertTrue(true);
    MentionsProcessor processor = (MentionsProcessor) getContainer().getComponentInstanceOfType(MentionsProcessor.class);
    assertNotNull("prococessor must not be null", processor);
    ExoSocialActivity activity = null;
    processor.processActivity(activity);
    assertNull("returned activity must be null", activity);

    activity = new ExoSocialActivityImpl();
    processor.processActivity(activity);
    assertNull(activity.getTitle());
    assertNull(activity.getBody());

    String root = "root", john = "john";

    String rootLink = LinkProvider.getProfileLink(root, LinkProvider.DEFAULT_PORTAL_OWNER);
    String johnLink = LinkProvider.getProfileLink(john, LinkProvider.DEFAULT_PORTAL_OWNER);

    activity.setTitle("single @root substitution");
    processor.processActivity(activity);
    assertEquals("Single substitution : ",activity.getTitle(), "single " + rootLink + " substitution");
    assertNull(activity.getBody());

    activity.setTitle("@root and @john title");
    activity.setBody("body with @root and @john");
    processor.processActivity(activity);
    assertEquals("Multiple substitution : ",activity.getTitle(), rootLink + " and " + johnLink + " title");
    assertEquals(activity.getBody(), "body with " + rootLink + " and " + johnLink);
    
    { // test case wrong user name is mentioned 
      activity.setTitle("@root and @wrong_username title");
      activity.setBody("body with @root and @wrong_username");
      processor.processActivity(activity);
      assertEquals(activity.getTitle(), rootLink + " and @wrong_username title");
      assertEquals(activity.getBody(), "body with " + rootLink + " and @wrong_username");
    }
  }
  
  public void testProcessActivityWithTemplateParam() throws Exception {
    MentionsProcessor processor = (MentionsProcessor) getContainer().getComponentInstanceOfType(MentionsProcessor.class);
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String sample = "this is a <strong> tag to keep</strong>";
    activity.setTitle(sample);
    activity.setBody(sample);
    String keysToProcess = "a|b|c";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("a", "@root and @john");
    templateParams.put("b", "@john");
    templateParams.put("d", "@mary");
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, keysToProcess);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    
    templateParams = activity.getTemplateParams();
    assertEquals(LinkProvider.getProfileLink("root", LinkProvider.DEFAULT_PORTAL_OWNER) + " and " + 
                 LinkProvider.getProfileLink("john", LinkProvider.DEFAULT_PORTAL_OWNER),
                 templateParams.get("a"));
    assertEquals(LinkProvider.getProfileLink("john", LinkProvider.DEFAULT_PORTAL_OWNER),
                 templateParams.get("b"));    
    assertEquals("@mary", templateParams.get("d"));  
  }
}

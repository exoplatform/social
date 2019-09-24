/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.*;

/**
 * Unit Test for {@link SpaceUtilsTest}
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jan 27, 2011
 * @since 1.2.0-GA
 */
public class SpaceUtilsTest extends AbstractCoreTest {

  private static final Log LOG = ExoLogger.getLogger(SpaceUtilsTest.class);

  private List<Space> tearDown = new ArrayList<Space>();

  private org.exoplatform.services.security.Identity identity;
  private Identity rootIdentity;
  private IdentityManager identityManager;
  private PageService pageService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Collection<String> roles = Collections.emptySet();
    Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
    identity = new org.exoplatform.services.security.Identity("root", memberships, roles);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    String spaceDisplayName = "Space1";
    Space space1 = new Space();
    space1.setApp("Members:true,Contact:true");
    space1.setDisplayName(spaceDisplayName);
    space1.setPrettyName(space1.getDisplayName());
    String shortName = SpaceUtils.cleanString(spaceDisplayName);
    space1.setGroupId("/spaces/" + shortName);
    space1.setUrl(shortName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing");
    space1.setType("classic");
    space1.setVisibility("public");
    space1.setPriority("2");
    String[] manager = new String []{"root"};
    String[] members = new String []{"demo", "john", "mary"};
    space1.setManagers(manager);
    space1.setMembers(members);

    spaceService.createSpace(space1, "root");
    tearDown.add(space1);
  }

  @Override
  protected  void tearDown() throws Exception {
    for(Space space : tearDown) {
      spaceService.deleteSpace(space);
    }
    identityManager.deleteIdentity(rootIdentity);
    super.tearDown();
  }

  public void testGetDisplayAppName() throws Exception {

    final String inputPortlet = "ABCPortlet";
    final String outputPortlet = "ABC";

    final String inputPortlet2 = "hello world portlet";
    final String outputPortlet2 = "hello world";

    assertEquals(outputPortlet, SpaceUtils.getDisplayAppName(inputPortlet));
    assertEquals(outputPortlet2, SpaceUtils.getDisplayAppName(inputPortlet2));

  }
  /**
   * Test {@link SpaceUtils#removeSpecialCharacterInSpaceFilter(String)}
   * @throws Exception
   * @since 1.2.2
   */
  public void testRemoveSpecialCharacter() {
    assertEquals("The filter should only filter special characters only","script alert 'Hello' script 100", SpaceUtils.removeSpecialCharacterInSpaceFilter("<script>alert('Hello');</script> 100"));
    assertEquals("The filter should keep wildcard *,? and %","% * '?", SpaceUtils.removeSpecialCharacterInSpaceFilter("( ) %{ } * [ ] \'? \""));
    /* I comment this part because unicode String may have problem with Jenkins build.
    assertEquals("The filter should only filter special characters only","script alert a á à ả ã ạ ă ắ ằ ẳ ẵ ặ â" +
    		          " ấ ầ ẩ ẫ ậ o ó ò ỏ õ ọ ơ ớ ờ ở ỡ ợ u ú ù ủ ũ ụ ư ứ ừ ử ữ ự đ script 100",
    		          SpaceUtils.removeSpecialCharacterInSpaceFilter("<script>alert(' a á à ả ã ạ ă ắ ằ ẳ ẵ ặ â" +
                  " ấ ầ ẩ ẫ ậ o ó ò ỏ õ ọ ơ ớ ờ ở ỡ ợ u ú ù ủ ũ ụ ư ứ ừ ử ữ ự đ');</script> 100"));
    assertEquals("The filter should keep the unicode characters","ˆáàâäåÁÃÄÅÀÂæÆçÇêéëèÊËÉÈïíîìÍÌÎÏñÑœŒöòõóøÓÔÕØÖÒšŠúüûùÙÚÜÛÿŸýÝžŽÞþƒßµÐºÇüéâäàåçêëèïîìÄÅÉæÆôöòû" +
    		         "ùÿÖÜáíóúñÑºĈĉĜĝĤĥĴĵŜŝŬŭàâçéèêëîïœôùûÀÂÇÈÉÊËÎÏŒáéíñóúüÁÉÍÑÓÚÜÔÙÛàèìòùÀÈÌÒÙãÃçÇòÒóÓõÕäåæðëöøßþü" +
    		         "ÿÄÅÆÐËÖØÞÜ",
    		         SpaceUtils.removeSpecialCharacterInSpaceFilter(
		             "ˆáàâäåÁÃÄÅÀÂæÆçÇêéëèÊËÉÈïíîìÍÌÎÏñÑœŒöòõóøÓÔÕØÖÒšŠúüûùÙÚÜÛÿŸýÝžŽÞþƒßµÐºÇüéâäàåçêëèïîìÄÅÉæÆôöòû" +
                 "ùÿÖÜáíóúñÑºĈĉĜĝĤĥĴĵŜŝŬŭàâçéèêëîïœôùûÀÂÇÈÉÊËÎÏŒáéíñóúüÁÉÍÑÓÚÜÔÙÛàèìòùÀÈÌÒÙãÃçÇòÒóÓõÕäåæðëöøßþü" +
                 "ÿÄÅÆÐËÖØÞÜ"
                 ));
    */
  }
  
  public void testIsInstalledApp() {
    Space space = new Space();
    String apps = "ForumPortlet:Forums:true:active,WikiPortlet:Wiki:true:active,FileExplorerPortlet:Documents:true:active,"
                + "CalendarPortlet:Agenda:true:active,SpaceSettingPortlet:Space Settings:false:active,"
                + "AnswersPortlet:Answer:true:active,FAQPortlet:FAQ:true:active,MembersPortlet:Members:true:active";
    space.setApp(apps);
    boolean isInstalledApp = SpaceUtils.isInstalledApp(space, "Agenda");
    assertTrue(isInstalledApp);
    isInstalledApp = SpaceUtils.isInstalledApp(space, "CalendarPortlet");
    assertTrue(isInstalledApp);
  }
 
  public void testProcessUnifiedSearchCondition() {
    String input = "spa~ce~0.5";
    assertEquals("spa~ce", SpaceUtils.processUnifiedSearchCondition(input));
    input = "space~0.5";
    assertEquals("space", SpaceUtils.processUnifiedSearchCondition(input));
    input = "space~0.5 test~0.5";
    assertEquals("space test", SpaceUtils.processUnifiedSearchCondition(input));
  }

  public void testchangeAppPageTitle() throws Exception {
    Space space1 = tearDown.get(0);

    ConversationState.setCurrent(new ConversationState(identity));
    List<UserNode> childNodes = SpaceUtils.getSpaceUserNodeChildren(space1);

    PageService pageService = (PageService) getContainer().getComponentInstanceOfType(PageService.class);
    PageContext pc = null;
    String AppName ="";

    for (UserNode childNode : childNodes)
    {
      pc = pageService.loadPage(childNode.getPageRef());
      AppName = pc.getState().getDisplayName().split("-")[0].trim();

      assertEquals("Space1", AppName);
      SpaceUtils.changeAppPageTitle(childNode,"newspacetitle");
      pc = pageService.loadPage(childNode.getPageRef());
      AppName = pc.getState().getDisplayName().split("-")[0].trim();

      assertEquals("newspacetitle", AppName);
    }

  }
}

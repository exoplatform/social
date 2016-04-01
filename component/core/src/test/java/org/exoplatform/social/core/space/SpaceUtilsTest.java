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

import junit.framework.TestCase;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;

/**
 * Unit Test for {@link SpaceUtilsTest}
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jan 27, 2011
 * @since 1.2.0-GA
 */
public class SpaceUtilsTest extends TestCase {

  private static final Log LOG = ExoLogger.getLogger(SpaceUtilsTest.class);

  public void testGetDisplayAppName() throws Exception {

    final String inputPortlet = "ABCPortlet";
    final String outputPortlet = "ABC";

    final String inputPortlet2 = "hello world portlet";
    final String outputPortlet2 = "hello world";

    final String inputGadget = "abc def gadget";
    final String outputGadget = "abc def";

    assertEquals(outputPortlet, SpaceUtils.getDisplayAppName(inputPortlet));
    assertEquals(outputPortlet2, SpaceUtils.getDisplayAppName(inputPortlet2));
    assertEquals(outputGadget, SpaceUtils.getDisplayAppName(inputGadget));

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
}

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

import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class MentionsProcessorTest extends AbstractCoreTest {

  public void testSubstituteUsernames() throws Exception {
    MentionsProcessor processor = (MentionsProcessor) getContainer().getComponentInstanceOfType(MentionsProcessor.class);
    LinkProvider linkProvider = (LinkProvider) getContainer().getComponentInstanceOfType(LinkProvider.class);
    assertNotNull("prococessor must not be null", processor);
    Activity activity = null;
    processor.processActivity(activity);
    assertNull("returned activity must be null", activity);

    activity = new Activity();
    processor.processActivity(activity);
    assertNull(activity.getTitle());
    assertNull(activity.getBody());
    String root = "root", john = "john";
    String rootLink = linkProvider.getProfileLink(root);
    String johnLink = linkProvider.getProfileLink(john);

    activity.setTitle("single @root substitution");
    processor.processActivity(activity);
    assertEquals(activity.getTitle(), "single " + rootLink + " substitution");
    assertNull(activity.getBody());

    activity.setTitle("@root and @john title");
    activity.setBody("body with @root and @john");
    processor.processActivity(activity);
    assertEquals(activity.getTitle(), rootLink + " and " + johnLink + " title");
    assertEquals(activity.getBody(), "body with " + rootLink + " and " + johnLink);

  }
}

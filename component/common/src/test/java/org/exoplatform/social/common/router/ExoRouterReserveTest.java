/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.router;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.AbstractCommonTest;
import org.exoplatform.social.common.router.ExoRouter.ActionBuilder;

public class ExoRouterReserveTest extends AbstractCommonTest {
  
  private static final Log LOG = ExoLogger.getLogger(ExoRouterReserveTest.class);
  
  private ExoRouter exoRouter;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    exoRouter = (ExoRouter) getContainer().getComponentInstanceOfType(ExoRouter.class);
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testExoRouterConfig() throws Exception {
    assertNotNull(this.exoRouter);
  }
  
  public void testReserveForActivityShow() throws Exception {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("activityID", "12345");
    ActionBuilder builder = ExoRouter.reverse("activity.show", args);
    assertNotNull(builder);
    assertEquals("/activity/12345", builder.toString());
  }
  
  public void testReserveForForumHome() throws Exception {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    ActionBuilder builder = ExoRouter.reverse("forum.home", args);
    assertNotNull(builder);
    assertEquals("/12345/ForumService", builder.toString());
  }
  
  public void testReserveForSearch() throws Exception {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    ActionBuilder builder = ExoRouter.reverse("forum.search", args);
    assertNotNull(builder);
    assertEquals("/12345/SearchForum", builder.toString());
  }
  
  public void testReserveForTag() throws Exception {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    ActionBuilder builder = ExoRouter.reverse("forum.tag", args);
    assertNotNull(builder);
    assertEquals("/12345/Tag", builder.toString());
  }
  
  public void testReserveForShowTopic() throws Exception {
    
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    args.put("topicID", "topic987654321");
    ActionBuilder builder = ExoRouter.reverse("forum.topic.show", args);
    assertNotNull(builder);
    assertEquals("/12345/topic/topic987654321", builder.toString());
  }
  
  public void testReserveForShowTopicTrue() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{reply}", "forum.tag.show.true");
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    args.put("topicID", "topic987654321");
    ActionBuilder builder = ExoRouter.reverse("forum.topic.reply", args);
    assertNotNull(builder);
    assertEquals("/12345/topic/topic987654321/reply", builder.toString());
    
    
  }
  
  public void testReserveForShowTopicFalse() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{reply}", "forum.tag.show.true");
    
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    args.put("topicID", "topic987654321");
    ActionBuilder builder = ExoRouter.reverse("forum.topic.quote", args);
    assertNotNull(builder);
    assertEquals("/12345/topic/topic987654321/quote", builder.toString());
  }
  
  
  public void testReserveForShowTopicPage() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{[0-9]}", "forum.topic.page");
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("pageID", "12345");
    args.put("topicID", "topic987654321");
    args.put("pageNo", "3");
    ActionBuilder builder = ExoRouter.reverse("forum.topic.page", args);
    assertNotNull(builder);
    assertEquals("/12345/topic/topic987654321/page/3", builder.toString());
  }
}
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
import org.exoplatform.social.common.router.ExoRouter.Route;

public class ExoRouterTest extends AbstractCommonTest {
  
  private static final Log LOG = ExoLogger.getLogger(ExoRouterTest.class);
  
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
  
  public void testRouterForActivityShow() throws Exception {
    Route route = ExoRouter.route("/activity/4437hg2121");
    
    assertRouter(route, "activity.show", new HashMap<String,String>(){{
      put("activityID","4437hg2121");
    }});
    
  }
  
  public void testRouterForActivityOwnerShow() throws Exception {
    Route route = ExoRouter.route("/activities/mary");
    
    assertRouter(route, "activity.stream.owner.show", new HashMap<String,String>(){{
      put("streamOwnerId","mary");
    }});
    
  }
  
  public void testRouterForProfileShow() throws Exception {
    Route route = ExoRouter.route("/profile/mary");
    
    assertRouter(route, "profile.owner.show", new HashMap<String,String>(){{
      put("streamOwnerId","mary");
    }});
    
  }
  
  public void testRouterForConnectionsShow() throws Exception {
    Route route = ExoRouter.route("/connections/network/mary");
    
    assertRouter(route, "connections.network.show", new HashMap<String,String>(){{
      put("streamOwnerId","mary");
    }});
    
    //
    route = ExoRouter.route("/connections/all-people/demo");
    
    assertRouter(route, "connections.network.show", new HashMap<String,String>(){{
      put("streamOwnerId","demo");
    }});
    
    //
    route = ExoRouter.route("/connections/receivedInvitations/root");
    
    assertRouter(route, "connections.network.show", new HashMap<String,String>(){{
      put("streamOwnerId","root");
    }});
    
    //
    route = ExoRouter.route("/connections/pendingRequests/john.vu");
    
    assertRouter(route, "connections.network.show", new HashMap<String,String>(){{
      put("streamOwnerId","john.vu");
    }});
    
  }
  
  public void testRouterForSpaceAccess() throws Exception {
    Route route = ExoRouter.route("mary-space");
    
    assertRouter(route, "space.access", new HashMap<String,String>(){{
      put("spacePrettyName","mary-space");
    }});
    
  }
  
  public void testRouterForSpaceAppAccess() throws Exception {
    Route route = ExoRouter.route("mary-space/wiki");
    
    assertRouter(route, "space.app.access", new HashMap<String,String>(){{
      put("spacePrettyName","mary-space");
      put("appName","wiki");
    }});
    
  }
  
  public void testRouterForSpaceAppWikiAccess() throws Exception {
    Route route = ExoRouter.route("mary-space/wiki/mypage/my_sub_page");
    
    assertRouter(route, "space.app.page.access", new HashMap<String,String>(){{
      put("spacePrettyName","mary-space");
      put("appName","wiki");
      put("page","mypage/my_sub_page");
    }});
    
  }
  
  public void testRouterForSpaceWikiAccess() throws Exception {
    Route route = ExoRouter.route("mary-space/wiki/technical_space_access");
    
    assertRouter(route, "space.app.page.access", new HashMap<String,String>(){{
      put("spacePrettyName","mary-space");
      put("appName","wiki");
      put("page","technical_space_access");
    }});
    
  }
  
  public void testRouterForForumHome() throws Exception {
    Route route = ExoRouter.route("/12345/ForumService");
    
    assertRouter(route, "forum.home", new HashMap<String,String>(){{
      put("pageID","12345");
    }});
  }
  
  public void testRouterForSearch() throws Exception {
    Route route = ExoRouter.route("/12345/SearchForum");
    
    //
    assertRouter(route, "forum.search", new HashMap<String,String>(){{
      put("pageID","12345");
    }});
    
  }
  
  public void testRouterForTag() throws Exception {
    Route route = ExoRouter.route("/12345/Tag");
    
    //
    assertRouter(route, "forum.tag", new HashMap<String,String>(){{
      put("pageID","12345");
    }});
  }
  
  public void testRouterForShowTopic() throws Exception {
    Route route = ExoRouter.route("/12345/topic/topic987654321");
    
    //
    assertRouter(route, "forum.topic.show", new HashMap<String,String>(){{
      put("pageID","12345");
      put("topicID","topic987654321");
    }});
  }
  
  public void testRouterForShowTopicTrue() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{reply}", "forum.tag.show.true");
    Route route = ExoRouter.route("/12345/topic/topic987654321/reply");
    
    //
    assertRouter(route, "forum.topic.reply", new HashMap<String,String>(){{
      put("pageID","12345");
      put("topicID","topic987654321");
    }});
    
  }
  
  public void testRouterForShowTopicFalse() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{reply}", "forum.tag.show.true");
    Route route = ExoRouter.route("/12345/topic/topic987654321/quote");
    
    //
    assertRouter(route, "forum.topic.quote", new HashMap<String,String>(){

    {
      put("pageID","12345");
      put("topicID","topic987654321");
    }});
  }
  
  public void testRouterForShowTopicWrong() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{reply}", "forum.tag.show.true");
    Route route = ExoRouter.route("/12345/fail/topic987654321");
    assertNull(route);
  }
  
  public void testRouterForShowTopicPage() throws Exception {
    //ExoRouter.addRoute("/{pageID}/topic/{topicID}/{[0-9]}", "forum.topic.page");
    Route route = ExoRouter.route("/12345/topic/topic987654321/page/3");
    
    //
    assertRouter(route, "forum.topic.page", new HashMap<String,String>(){{
      put("pageID","12345");
      put("topicID","topic987654321");
      put("pageNo","3");
    }});
  }
  
  private void assertRouter(Route route, String actionName, Map<String, String> expectedArgs) {
    assertNotNull(route);
    assertEquals(actionName, route.action);
    for(Map.Entry<String , String> entry : expectedArgs.entrySet()) {
      assertEquals(entry.getValue(), route.localArgs.get(entry.getKey()));
    }
  }
}

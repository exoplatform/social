package org.exoplatform.social.core.search;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SpaceSearchConnectorTestCase extends AbstractCoreTest {

  private IdentityManager identityManager;
  private SpaceSearchConnector spaceSearchConnector;

  private List<Space> tearDown = new ArrayList<Space>();
  private final Log LOG = ExoLogger.getLogger(SpaceSearchConnectorTestCase.class);
  private String CONTROLLER_PATH = "conf/standalone/controller.xml";
  private Router router;
  private SearchContext context;

  public void setUp() throws Exception {
    super.setUp();
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

    Identity userA = new Identity(OrganizationIdentityProvider.NAME, "demo");
    identityManager.saveIdentity(userA);

    Space sFoo = new Space();
    sFoo.setDisplayName("foo");
    sFoo.setPrettyName("foo");
    sFoo.setDescription("foo description");
    sFoo.setManagers(new String[]{"demo"});
    sFoo.setMembers(new String[]{"demo"});
    sFoo.setType(DefaultSpaceApplicationHandler.NAME);
    sFoo.setRegistration(Space.OPEN);
    createSpaceNonInitApps(sFoo, userA.getRemoteId(), null);
    tearDown.add(sFoo);

    Space sBar = new Space();
    sBar.setDisplayName("bar");
    sBar.setPrettyName("bar");
    sBar.setDescription("bar description");
    sBar.setManagers(new String[]{"demo"});
    sBar.setMembers(new String[]{"demo"});
    sBar.setType(DefaultSpaceApplicationHandler.NAME);
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    sBar.setAvatarAttachment(avatarAttachment);
    createSpaceNonInitApps(sBar, userA.getRemoteId(), null);
    tearDown.add(sBar);

    Space sAnother = new Space();
    sAnother.setDisplayName("another");
    sAnother.setPrettyName("another");
    sAnother.setDescription("another details");
    sAnother.setManagers(new String[]{"demo"});
    sAnother.setMembers(new String[]{"demo"});
    sAnother.setType(DefaultSpaceApplicationHandler.NAME);
    createSpaceNonInitApps(sAnother, userA.getRemoteId(), null);
    tearDown.add(sAnother);

    InitParams params = new InitParams();
    params.put("constructor.params", new PropertiesParam());
    spaceSearchConnector = new SpaceSearchConnector(params, spaceService) {};
    
    loadController();
  }

  @Override
  public void tearDown() throws Exception {
    for(Space space : tearDown) {
      spaceService.deleteSpace(space);
    }

    identityManager.deleteIdentity(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo"));
    super.tearDown();
  }

  public void testCurrentUser() throws Exception {
    setCurrentUser("demo");
    assertEquals("demo", getCurrentUserName());
  }

  public void testFilter() throws Exception {
    setCurrentUser("demo");
    assertEquals(2, spaceSearchConnector.search(context, "foo bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search(context, "foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search(context, "bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(2, spaceSearchConnector.search(context, "foo description", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search(context, "foo space", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(2, spaceSearchConnector.search(context, "description", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
  }

  public void testData() throws Exception {
    setCurrentUser("demo");
    Collection<SearchResult> cFoo = spaceSearchConnector.search(context, "foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC");
    SearchResult rFoo = cFoo.iterator().next();
    assertEquals("foo", rFoo.getTitle());
    assertTrue(rFoo.getExcerpt().indexOf("foo description") >= 0);
    assertTrue(rFoo.getRelevancy() > 0);
    log.info(" rFoo.getUrl() " + rFoo.getUrl());
    assertEquals("/portal/g/:spaces:foo/foo", rFoo.getUrl());
    assertEquals(LinkProvider.SPACE_DEFAULT_AVATAR_URL, rFoo.getImageUrl());
    assertEquals("foo - 1 Member(s) - Free to Join", rFoo.getDetail());

    Collection<SearchResult> cBar = spaceSearchConnector.search(context, "bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC");
    SearchResult rBar = cBar.iterator().next();
    Profile pBar = identityManager.getProfile(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "bar"));
    Space sBar = spaceService.getSpaceByDisplayName("bar");
    assertEquals(pBar.getAvatarUrl(), rBar.getImageUrl());
    assertTrue(rBar.getDate() != 0);
    assertEquals(sBar.getCreatedTime(), rBar.getDate());
  }

  public void testOrder() throws Exception {
    setCurrentUser("demo");
    List<SearchResult> rTitleAsc = (List<SearchResult>) spaceSearchConnector.search(context, "description", Collections.EMPTY_LIST, 0, 10, "title", "ASC");
    assertEquals("bar", rTitleAsc.get(0).getTitle());
    assertEquals("foo", rTitleAsc.get(1).getTitle());

    List<SearchResult> rTitleDesc = (List<SearchResult>) spaceSearchConnector.search(context, "description", Collections.EMPTY_LIST, 0, 10, "title", "DESC");
    assertEquals("foo", rTitleDesc.get(0).getTitle());
    assertEquals("bar", rTitleDesc.get(1).getTitle());

    List<SearchResult> rDateAsc = (List<SearchResult>) spaceSearchConnector.search(context, "description", Collections.EMPTY_LIST, 0, 10, "date", "ASC");
    assertEquals("foo", rDateAsc.get(0).getTitle());
    assertEquals("bar", rDateAsc.get(1).getTitle());

    List<SearchResult> rDateDesc = (List<SearchResult>) spaceSearchConnector.search(context, "description", Collections.EMPTY_LIST, 0, 10, "date", "DESC");
    assertEquals("bar", rDateDesc.get(0).getTitle());
    assertEquals("foo", rDateDesc.get(1).getTitle());
  }
  
  public void testUnicodeData() throws Exception {
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    
    Space space = new Space();
    space.setDisplayName("広いニーズ");
    space.setPrettyName("広いニーズ");
    space.setDescription(StringEscapeUtils.escapeHtml("広いニーズに応えます。"));
    space.setManagers(new String[]{"mary"});
    space.setMembers(new String[]{"mary"});
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setRegistration(Space.OPEN);
    createSpaceNonInitApps(space, maryIdentity.getRemoteId(), null);
    tearDown.add(space);
    
    Space newSpace = new Space();
    newSpace.setDisplayName("close");
    newSpace.setPrettyName("close");
    newSpace.setDescription("closed");
    newSpace.setManagers(new String[]{"mary"});
    newSpace.setMembers(new String[]{"mary"});
    newSpace.setType(DefaultSpaceApplicationHandler.NAME);
    newSpace.setRegistration(Space.OPEN);
    createSpaceNonInitApps(newSpace, maryIdentity.getRemoteId(), null);
    tearDown.add(newSpace);
    
    setCurrentUser("mary");
    assertEquals(1, spaceSearchConnector.search(context, "広", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search(context, "%広いニー", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search(context, "広いニーズ", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search(context, "広いニーズに応えます。", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    
    assertEquals(1, spaceSearchConnector.search(context, "clo", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    identityManager.deleteIdentity(maryIdentity);
  }

  private void setCurrentUser(final String name) {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(name)));
  }
  
  private String getCurrentUserName() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }
  
  private void loadController() throws Exception {
    ClassLoader loader = getClass().getClassLoader();
    InputStream in = loader.getResourceAsStream(CONTROLLER_PATH);
    try {
      ControllerDescriptor routerDesc = new DescriptorBuilder().build(in);
      router = new Router(routerDesc);
      context = new SearchContext(router, "");
    } catch (RouterConfigException e) {
      log.info(e.getMessage());
    } finally {
      in.close();
    }
  }
}

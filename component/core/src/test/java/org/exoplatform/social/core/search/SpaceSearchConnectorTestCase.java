package org.exoplatform.social.core.search;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SpaceSearchConnectorTestCase extends AbstractCoreTest {

  private IdentityManager identityManager;
  private SpaceSearchConnector spaceSearchConnector;

  private List<Space> tearDown = new ArrayList<Space>();
  private final Log LOG = ExoLogger.getLogger(SpaceSearchConnectorTestCase.class);

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
    spaceSearchConnector = new SpaceSearchConnector(params, spaceService) {
      @Override
      protected String getSpaceUrl(Space space) {
        return "url://" + space.getPrettyName();
      }
    };
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
    assertEquals("demo", spaceSearchConnector.getCurrentUserName());
  }

  public void testFilter() throws Exception {
    setCurrentUser("demo");
    assertEquals(1, spaceSearchConnector.search("foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search("bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(1, spaceSearchConnector.search("foo description", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
    assertEquals(2, spaceSearchConnector.search("description", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC").size());
  }

  public void testData() throws Exception {
    setCurrentUser("demo");
    Collection<SearchResult> cFoo = spaceSearchConnector.search("foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC");
    SearchResult rFoo = cFoo.iterator().next();
    assertEquals("foo", rFoo.getTitle());
    assertEquals("foo description", rFoo.getExcerpt());
    assertEquals("url://foo", rFoo.getUrl());
    assertEquals(LinkProvider.SPACE_DEFAULT_AVATAR_URL, rFoo.getImageUrl());
    assertEquals("foo - 1 Member(s) - Free to Join", rFoo.getDetail());

    Collection<SearchResult> cBar = spaceSearchConnector.search("bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "ASC");
    SearchResult rBar = cBar.iterator().next();
    Profile pBar = identityManager.getProfile(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "bar"));
    Space sBar = spaceService.getSpaceByDisplayName("bar");
    assertEquals(pBar.getAvatarUrl(), rBar.getImageUrl());
    assertTrue(rBar.getDate() != 0);
    assertEquals(sBar.getCreatedTime(), rBar.getDate());
  }

  public void testOrder() throws Exception {
    setCurrentUser("demo");
    List<SearchResult> rTitleAsc = (List<SearchResult>) spaceSearchConnector.search("description", Collections.EMPTY_LIST, 0, 10, "title", "ASC");
    assertEquals("bar", rTitleAsc.get(0).getTitle());
    assertEquals("foo", rTitleAsc.get(1).getTitle());

    List<SearchResult> rTitleDesc = (List<SearchResult>) spaceSearchConnector.search("description", Collections.EMPTY_LIST, 0, 10, "title", "DESC");
    assertEquals("foo", rTitleDesc.get(0).getTitle());
    assertEquals("bar", rTitleDesc.get(1).getTitle());

    List<SearchResult> rDateAsc = (List<SearchResult>) spaceSearchConnector.search("description", Collections.EMPTY_LIST, 0, 10, "date", "ASC");
    assertEquals("foo", rDateAsc.get(0).getTitle());
    assertEquals("bar", rDateAsc.get(1).getTitle());

    List<SearchResult> rDateDesc = (List<SearchResult>) spaceSearchConnector.search("description", Collections.EMPTY_LIST, 0, 10, "date", "DESC");
    assertEquals("bar", rDateDesc.get(0).getTitle());
    assertEquals("foo", rDateDesc.get(1).getTitle());
  }

  private void setCurrentUser(final String name) {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(name)));
  }
}

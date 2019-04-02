/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.search;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 30, 2015
 */
public class SearchTestIT extends BaseESTest {

  protected final Log                               LOG    = ExoLogger.getLogger(SearchTestIT.class);

  private SpaceStorage                              spaceStorage;
  
  private SearchContext searchContext = Mockito.mock(SearchContext.class);

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    ConversationState.setCurrent(new ConversationState(identity));
    
    Mockito.when(searchContext.handler(Mockito.anyString())).thenReturn(searchContext);
    Mockito.when(searchContext.lang(Mockito.anyString())).thenReturn(searchContext);
    Mockito.when(searchContext.siteName(Mockito.anyString())).thenReturn(searchContext);
    Mockito.when(searchContext.siteType(Mockito.anyString())).thenReturn(searchContext);
    Mockito.when(searchContext.path(Mockito.anyString())).thenReturn(searchContext);
    Mockito.doReturn("spaceLink").when(searchContext).renderLink();

    identityManager = getService(IdentityManager.class);
    spaceStorage = getService(SpaceStorage.class);

    rootIdentity = createIdentity("root", "root@platform.com");
    johnIdentity = createIdentity("john", "john@platform.com");
    maryIdentity = createIdentity("mary", "mary@platform.com");
    demoIdentity = createIdentity("demo", "demo@platform.com");
  }

  @Override
  public void tearDown() throws Exception {
    List<Space> spaces = spaceStorage.getAllSpaces();
    for (Space space : spaces) {
      spaceStorage.deleteSpace(space.getId());
    }
    super.tearDown();
  }

  public void test_indexedProfile_isReturnedBySearch() throws IOException {
    // Given
    Identity ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
    reindexProfileById(paulIdentity.getId());

    ProfileFilter filter = new ProfileFilter();
    // When
    List<Identity> results = searchConnector.search(ghostIdentity, filter, null, 0, 10);
    // Then
    assertThat(results.size(), is(1));
  }

  public void test_outgoingConnection_isReturnedBySearch() throws IOException {
    // Given
    relationshipManager.inviteToConnect(johnIdentity, maryIdentity);

    reindexProfileById(johnIdentity.getId());
    reindexProfileById(maryIdentity.getId());

    ProfileFilter filter = new ProfileFilter();
    // When
    // All the users that have an incoming request from John
    List<Identity> resultsOutJohn = searchConnector.search(johnIdentity, filter, Relationship.Type.INCOMING, 0, 10);
    // All the users that have sent an outgoing request to John
    List<Identity> resultsInJohn = searchConnector.search(johnIdentity, filter, Relationship.Type.OUTGOING, 0, 10);
    // All the users that have an incoming request from Mary
    List<Identity> resultsOutMary = searchConnector.search(maryIdentity, filter, Relationship.Type.INCOMING, 0, 10);
    // All the users that have sent an outgoing request to Mary
    List<Identity> resultsInMary = searchConnector.search(maryIdentity, filter, Relationship.Type.OUTGOING, 0, 10);

    // Then
    assertThat(resultsOutJohn.size(), is(0));
    assertThat(resultsInJohn.size(), is(1));
    assertThat(resultsOutMary.size(), is(1));
    assertThat(resultsInMary.size(), is(0));
  }

  
  public void testPeopleName() throws Exception {    
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", true);

    // ROOT
    Profile profile = rootIdentity.getProfile();
    profile.setListUpdateTypes(Arrays.asList(UpdateType.ABOUT_ME));
    profile.setProperty(Profile.FULL_NAME, "Root Root");
    identityManager.updateProfile(profile);

    reindexProfileById(rootIdentity.getId());

    assertEquals(1, peopleSearchConnector.search(searchContext, "Root Root", null, 0, 10, null, null).size());
  }

  public void testPeopleNameOrEmail() throws Exception {    
    reindexProfileById(rootIdentity.getId());
    reindexProfileById(johnIdentity.getId());
    reindexProfileById(maryIdentity.getId());
    reindexProfileById(demoIdentity.getId());


    ProfileFilter filter = new ProfileFilter();
    filter.setName("mary@platform.com");

    List<Identity> results = searchConnector.search(rootIdentity, filter, null, 0, 10);
    assertEquals(0, results.size());

    filter.setSearchEmail(true);
    results = searchConnector.search(rootIdentity, filter, null, 0, 10);
    assertEquals(1, results.size());
    assertEquals("mary", results.get(0).getRemoteId());
  }

  public void testSpaceName() throws Exception {
    createSpace("testSpaceName abcd efgh", null, null);

    assertEquals(1, spaceSearchConnector.search(searchContext, "*testSpaceName*", null, 0, 10, null, null).size());

    assertEquals(1, spaceSearchConnector.search(searchContext, "*abcd*", null, 0, 10, null, null).size());

    assertEquals(1, spaceSearchConnector.search(searchContext, "*efgh*", null, 0, 10, null, null).size());
  }

  public void testSpaceDisplayName() throws Exception {
    createSpace("pretty", "displayName abc def", null);

    assertEquals(1, spaceSearchConnector.search(searchContext, "diSplayName*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*abc*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*def*", null, 0, 10, null, null).size());
  }

  public void testSpaceDescription() throws Exception {
    createSpace("pretty", null, "spaceDescription 123 456");

    assertEquals(1, spaceSearchConnector.search(searchContext, "*spaceDescription*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*123*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*456*", null, 0, 10, null, null).size());
  }

  private Space createSpace(String prettyName, String displayName, String description) throws Exception {
    Space space = new Space();
    space.setPrettyName(prettyName);
    displayName = displayName == null ? prettyName : displayName; 
    space.setDisplayName(displayName);
    space.setDescription(description);
    space.setManagers(new String[] { "root" });
    space.setGroupId("/platform/users");
    space.setRegistration(Space.OPEN);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setUrl(space.getPrettyName());
    spaceStorage.saveSpace(space, true);

    reindexSpaceById(space.getId());
    return space;
  }

}

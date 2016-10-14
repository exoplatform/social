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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.social.core.jpa.updater.RelationshipMigrationService;
import org.mockito.Mockito;

import org.exoplatform.addons.es.index.IndexingOperationProcessor;
import org.exoplatform.addons.es.index.IndexingService;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 30, 2015
 */
public class SearchTestIT extends AbstractCoreTest {

  protected final Log                               LOG    = ExoLogger.getLogger(SearchTestIT.class);

  private IndexingService                           indexingService;

  private IndexingOperationProcessor                indexingProcessor;

  private ProfileSearchConnector                    searchConnector;
  
  private PeopleElasticUnifiedSearchServiceConnector peopleSearchConnector;

  private SpaceElasticUnifiedSearchServiceConnector spaceSearchConnector;

  private String                                    urlClient;

  private HttpClient                                client = new DefaultHttpClient();

  private SpaceStorage                              spaceStorage;

  private IdentityStorageImpl                       identityStorageImpl;

  private RelationshipStorageImpl                   relationshipStorageImpl;

  private RelationshipMigrationService relationshipMigration;

  private IdentityProvider<User>                    identityProvider;
  
  private SearchContext searchContext = Mockito.mock(SearchContext.class);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    indexingService = getService(IndexingService.class);
    indexingProcessor = getService(IndexingOperationProcessor.class);
    identityManager = getService(IdentityManager.class);
    searchConnector = getService(ProfileSearchConnector.class);
    peopleSearchConnector = getService(PeopleElasticUnifiedSearchServiceConnector.class);
    spaceSearchConnector = getService(SpaceElasticUnifiedSearchServiceConnector.class);
    deleteAllProfilesInES();
    deleteAllSpaceInES();

    assertNotNull("identityManager must not be null", identityManager);
    urlClient = PropertyManager.getProperty("exo.es.search.server.url");

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
    //
    identityProvider = getService(IdentityProvider.class);
    identityStorageImpl = getService(IdentityStorageImpl.class);
    relationshipStorageImpl = getService(RelationshipStorageImpl.class);
    relationshipMigration = getService(RelationshipMigrationService.class);
  }

  @Override
  public void tearDown() throws Exception {
    List<Space> spaces = spaceStorage.getAllSpaces();
    for (Space space : spaces) {
      spaceStorage.deleteSpace(space.getId());
    }
    deleteAllSpaceInES();
    super.tearDown();
  }

  public void test_indexedProfile_isReturnedBySearch() throws IOException {
    // Given
    Identity ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
    indexingService.index(ProfileIndexingServiceConnector.TYPE, paulIdentity.getId());
    indexingProcessor.process();
    refreshIndices();
    ProfileFilter filter = new ProfileFilter();
    // When
    List<Identity> results = searchConnector.search(ghostIdentity, filter, null, 0, 10);
    // Then
    assertThat(results.size(), is(1));
  }

  public void test_outgoingConnection_isReturnedBySearch() throws IOException {
    // Given
    relationshipManager.inviteToConnect(johnIdentity, maryIdentity);

    indexingService.index(ProfileIndexingServiceConnector.TYPE, johnIdentity.getId());
    indexingService.index(ProfileIndexingServiceConnector.TYPE, maryIdentity.getId());
    indexingProcessor.process();
    refreshIndices();
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
    assertThat(resultsOutJohn.size(), is(1));
    assertThat(resultsInJohn.size(), is(0));
    assertThat(resultsOutMary.size(), is(0));
    assertThat(resultsInMary.size(), is(1));
  }

  
  public void testPeopleName() throws Exception {    
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", true);

    // ROOT
    Profile profile = rootIdentity.getProfile();
    profile.setListUpdateTypes(Arrays.asList(UpdateType.ABOUT_ME));
    profile.setProperty(Profile.FULL_NAME, "Root Root");
    identityManager.updateProfile(profile);
    indexingService.index(ProfileIndexingServiceConnector.TYPE, rootIdentity.getId());
    
    indexingProcessor.process();
    refreshIndices();
    
    assertEquals(1, peopleSearchConnector.search(searchContext, "Root Root", null, 0, 10, null, null).size());
  }

  public void testSpaceName() throws Exception {
    createSpace("testSpaceName abcd efgh", null, null);

    assertEquals(1, spaceSearchConnector.search(searchContext, "*space*", null, 0, 10, null, null).size());

    assertEquals(1, spaceSearchConnector.search(searchContext, "*name*", null, 0, 10, null, null).size());

    assertEquals(1, spaceSearchConnector.search(searchContext, "*abcd*", null, 0, 10, null, null).size());
  }

  public void testSpaceDisplayName() throws Exception {
    createSpace("pretty", "displayName abc def", null);

    assertEquals(1, spaceSearchConnector.search(searchContext, "diSplayName*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*abc*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*ef*", null, 0, 10, null, null).size());
  }

  public void testSpaceDescription() throws Exception {
    createSpace("pretty", null, "spaceDescription 123 456");

    assertEquals(1, spaceSearchConnector.search(searchContext, "*scription* *23*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*123*", null, 0, 10, null, null).size());
    assertEquals(1, spaceSearchConnector.search(searchContext, "*56*", null, 0, 10, null, null).size());
  }

  private Space createSpace(String prettyName, String displayName, String description) throws Exception {
    Space space = new Space();
    space.setPrettyName(prettyName);
    displayName = displayName == null ? prettyName : displayName; 
    space.setDisplayName(displayName);
    space.setDescription(description);
    space.setManagers(new String[] { "root" });
    space.setGroupId("/platform/users");
    spaceStorage.saveSpace(space, true);
    space = spaceStorage.getAllSpaces().get(0);

    indexingService.index(SpaceIndexingServiceConnector.TYPE, space.getId());
    indexingProcessor.process();
    refreshSpaceIndices();
    return space;
  }

  private void deleteAllSpaceInES() {
    indexingService.unindexAll(SpaceIndexingServiceConnector.TYPE);
    indexingProcessor.process();
  }

  private void refreshSpaceIndices() throws IOException {
    HttpPost request = new HttpPost(urlClient + "/space/_refresh");
    LOG.info("Refreshing ES by calling {}", request.getURI());
    HttpResponse response = client.execute(request);
    assertThat(response.getStatusLine().getStatusCode(), is(200));
  }

  private void refreshIndices() throws IOException {
    HttpPost request = new HttpPost(urlClient + "/profile/_refresh");
    LOG.info("Refreshing ES by calling {}", request.getURI());
    HttpResponse response = client.execute(request);
    assertThat(response.getStatusLine().getStatusCode(), is(200));
  }

  private void deleteAllProfilesInES() {
    indexingService.unindexAll(ProfileIndexingServiceConnector.TYPE);
    indexingProcessor.process();
  }

}

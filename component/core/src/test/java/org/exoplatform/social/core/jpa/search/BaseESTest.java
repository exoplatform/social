/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.manager.IdentityManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-configuration.xml"),
})
public class BaseESTest extends AbstractCoreTest {

  protected final Log                                  LOG               = ExoLogger.getLogger(BaseESTest.class);

  protected IndexingService                            indexingService;

  protected IndexingOperationProcessor                 indexingProcessor;

  protected ProfileSearchConnector                     searchConnector;

  protected PeopleElasticUnifiedSearchServiceConnector peopleSearchConnector;

  protected SpaceElasticUnifiedSearchServiceConnector  spaceSearchConnector;

  private PoolingHttpClientConnectionManager           connectionManager = null;

  private HttpClient                                   client            = null;

  private String                                       urlClient;

  @Override
  protected void beforeRunBare() {
    super.beforeRunBare();

    urlClient = PropertyManager.getProperty("exo.es.search.server.url");

    connectionManager = new PoolingHttpClientConnectionManager();
    // Used to allow multiple HTTP connections to same host
    String hostAndPort = urlClient.replaceAll("http(s)?://", "");
    String[] urlParts = hostAndPort.split(":");
    HttpHost localhost = new HttpHost(urlParts[0], Integer.parseInt(urlParts[1]));
    connectionManager.setMaxPerRoute(new HttpRoute(localhost), 50);
    connectionManager.closeIdleConnections(2, TimeUnit.SECONDS);

    client = HttpClients.custom().setConnectionManager(connectionManager).build();
  }

  @Override
  protected void afterRunBare() {
    connectionManager.closeExpiredConnections();
    connectionManager.shutdown();

    super.afterRunBare();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    indexingService = getService(IndexingService.class);
    indexingProcessor = getService(IndexingOperationProcessor.class);
    identityManager = getService(IdentityManager.class);
    searchConnector = getService(ProfileSearchConnector.class);
    peopleSearchConnector = getService(PeopleElasticUnifiedSearchServiceConnector.class);
    spaceSearchConnector = getService(SpaceElasticUnifiedSearchServiceConnector.class);

    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    ConversationState.setCurrent(new ConversationState(identity));

    initProfileIndexes();
    initSpaceIndexes();
  }

  @Override
  public void tearDown() throws Exception {
    deleteAllProfilesInES();
    deleteAllSpaceInES();
    super.tearDown();
  }

  protected void refreshSpaceIndices() throws IOException {
    HttpGet request = new HttpGet(urlClient + "/space_alias/_refresh");
    LOG.info("Refreshing ES by calling {}", request.getURI());
    HttpResponse response = client.execute(request);
    assertThat(response.getStatusLine().getStatusCode(), is(200));
  }

  protected void refreshProfileIndices() throws IOException {
    HttpGet request = new HttpGet(urlClient + "/profile_alias/_refresh");
    LOG.info("Refreshing ES by calling {}", request.getURI());
    HttpResponse response = client.execute(request);
    assertThat(response.getStatusLine().getStatusCode(), is(200));
  }

  protected void deleteAllSpaceInES() throws IOException {
    indexingService.unindexAll(SpaceIndexingServiceConnector.TYPE);
    indexingProcessor.process();

    refreshSpaceIndices();
  }

  protected void initSpaceIndexes() throws IOException {
    indexingService.init(SpaceIndexingServiceConnector.TYPE);
    indexingProcessor.process();

    refreshSpaceIndices();
  }

  protected void reindexSpaceById(String id) throws IOException {
    indexingService.unindex(SpaceIndexingServiceConnector.TYPE, id);
    indexingService.index(SpaceIndexingServiceConnector.TYPE, id);
    indexingProcessor.process();

    refreshSpaceIndices();
  }

  protected void unindexSpaceById(String id2) throws IOException {
    indexingService.unindex(SpaceIndexingServiceConnector.TYPE, id2);
    indexingProcessor.process();

    refreshSpaceIndices();
  }

  protected void deleteAllProfilesInES() throws IOException {
    indexingService.unindexAll(ProfileIndexingServiceConnector.TYPE);
    indexingProcessor.process();

    refreshProfileIndices();
  }

  protected void initProfileIndexes() throws IOException {
    indexingService.init(ProfileIndexingServiceConnector.TYPE);
    indexingProcessor.process();

    refreshProfileIndices();
  }

  protected void reindexProfileById(String id) throws IOException {
    indexingService.unindex(ProfileIndexingServiceConnector.TYPE, id);
    indexingService.index(ProfileIndexingServiceConnector.TYPE, id);
    indexingProcessor.process();

    refreshProfileIndices();
  }

  protected void unindexProfileById(String id2) throws IOException {
    indexingService.unindex(ProfileIndexingServiceConnector.TYPE, id2);
    indexingProcessor.process();

    refreshProfileIndices();
  }

}

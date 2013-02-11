package org.exoplatform.social.core.search;

import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PeopleSearchConnectorTestCase extends AbstractCoreTest {

  private IdentityManager identityManager;
  private PeopleSearchConnector peopleSearchConnector;

  private List<String> tearDown = new ArrayList<String>();

  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

    Identity iFoo = new Identity(OrganizationIdentityProvider.NAME, "foo");
    Profile pFoo = new Profile(iFoo);

    pFoo.setProperty(Profile.FULL_NAME, "foo");
    pFoo.setProperty(Profile.EMAIL, "foo@mail.com");
    pFoo.setProperty(Profile.GENDER, "Male");
    pFoo.setProperty(Profile.POSITION, "foo position");

    Map<String, String> xFoo = new HashMap<String, String>();
    List<Map<String, String>> xFoos = new ArrayList<Map<String, String>>();
    xFoo.put(Profile.EXPERIENCES_SKILLS, "doSomething");
    xFoo.put(Profile.EXPERIENCES_POSITION, "dev");
    xFoo.put(Profile.EXPERIENCES_COMPANY, "exo");
    xFoo.put(Profile.EXPERIENCES_DESCRIPTION, "job description");
    xFoos.add(xFoo);
    pFoo.setProperty(Profile.EXPERIENCES, xFoos);

    List<Map<String, String>> phones = new ArrayList<Map<String, String>>();
    Map<String, String> phone1 = new HashMap<String, String>();
    phone1.put("key", "Work");
    phone1.put("value", "+17889989");
    phones.add(phone1);
    pFoo.setProperty(Profile.CONTACT_PHONES, phones);

    identityManager.saveIdentity(iFoo);
    identityManager.saveProfile(pFoo);
    tearDown.add(iFoo.getId());

    Identity iBar = new Identity(OrganizationIdentityProvider.NAME, "bar");
    Profile pBar = new Profile(iBar);
    pBar.setProperty(Profile.FULL_NAME, "bar");
    pBar.setProperty(Profile.POSITION, "bar position");
    Map<String, String> xBar = new HashMap<String, String>();
    List<Map<String, String>> xBars = new ArrayList<Map<String, String>>();
    xBar.put(Profile.EXPERIENCES_SKILLS, "doSomething");
    xBar.put(Profile.EXPERIENCES_POSITION, "dev");
    xBar.put(Profile.EXPERIENCES_COMPANY, "exo");
    xBar.put(Profile.EXPERIENCES_DESCRIPTION, "job description");
    xBars.add(xBar);
    pBar.setProperty(Profile.EXPERIENCES, xBars);
    identityManager.saveIdentity(iBar);
    identityManager.saveProfile(pBar);
    tearDown.add(iBar.getId());

    Identity iDoesExist = new Identity(OrganizationIdentityProvider.NAME, "doesExist");
    Profile pDoesExist = new Profile(iDoesExist);
    identityManager.saveIdentity(iDoesExist);
    identityManager.saveProfile(pDoesExist);
    tearDown.add(iDoesExist.getId());

    InitParams params = new InitParams();
    params.put("constructor.params", new PropertiesParam());
    peopleSearchConnector = new PeopleSearchConnector(params, identityManager);

  }

  @Override
  protected void tearDown() throws Exception {
    for (String id : tearDown) {
      identityManager.deleteIdentity(new Identity(id));
    }
    super.tearDown();
  }

  public void testFilter() throws Exception {
    assertEquals(1, peopleSearchConnector.search("foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(1, peopleSearchConnector.search("bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(1, peopleSearchConnector.search("bar position", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search("position", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search("doSomething", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search("dev", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search("exo", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search("job description", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
  }

  public void testData() throws Exception {
    Collection<SearchResult> c = peopleSearchConnector.search("foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc");
    SearchResult r = c.iterator().next();
    assertEquals("foo", r.getTitle());
    assertEquals("foo position", r.getExcerpt());
    assertEquals("foo@mail.com - +17889989 - Male", r.getDetail());

    Profile p = identityManager.getProfile(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "foo"));
    assertEquals(p.getUrl(), r.getUrl());
    assertEquals(LinkProvider.PROFILE_DEFAULT_AVATAR_URL, r.getImageUrl());
  }

}

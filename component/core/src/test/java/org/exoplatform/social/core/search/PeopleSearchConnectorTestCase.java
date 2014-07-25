package org.exoplatform.social.core.search;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.io.InputStream;
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

    pFoo.setProperty(Profile.LAST_NAME, "foo");
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
    pBar.setProperty(Profile.LAST_NAME, "bar");
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

    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    pBar.setProperty(Profile.AVATAR, avatarAttachment);
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
    assertEquals(1, peopleSearchConnector.search(null, "foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(1, peopleSearchConnector.search(null, "bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "bar position", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "position", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "doSomething", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "dev", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "exo", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "job description", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    //
    assertEquals(2, peopleSearchConnector.search(null, "posi", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
    assertEquals(2, peopleSearchConnector.search(null, "do", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
  }

  public void testData() throws Exception {
    Collection<SearchResult> cFoo = peopleSearchConnector.search(null, "foo", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc");
    SearchResult rFoo = cFoo.iterator().next();
    assertEquals("foo", rFoo.getTitle());
    assertTrue(rFoo.getExcerpt().indexOf("foo") >= 0);
    assertTrue(rFoo.getRelevancy() > 0);
    assertEquals("foo@mail.com - +17889989 - Male", rFoo.getDetail());

    Profile pFoo = identityManager.getProfile(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "foo"));
    assertEquals(pFoo.getUrl(), rFoo.getUrl());
    assertEquals(LinkProvider.PROFILE_DEFAULT_AVATAR_URL, rFoo.getImageUrl());
    assertTrue(rFoo.getDate() != 0);
    assertEquals(pFoo.getCreatedTime(), rFoo.getDate());

    Collection<SearchResult> cBar = peopleSearchConnector.search(null, "bar", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc");
    SearchResult rBar = cBar.iterator().next();
    Profile pBar = identityManager.getProfile(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "bar"));
    assertEquals(pBar.getAvatarUrl(), rBar.getImageUrl());
  }

  public void testOrder() throws Exception {
    
    List<SearchResult> rTitleAsc = (List<SearchResult>) peopleSearchConnector.search(null, "position", Collections.EMPTY_LIST, 0, 10, "title", "asc");
    assertEquals("bar", rTitleAsc.get(0).getTitle());
    assertEquals("foo", rTitleAsc.get(1).getTitle());

    List<SearchResult> rTitleDesc = (List<SearchResult>) peopleSearchConnector.search(null, "position", Collections.EMPTY_LIST, 0, 10, "title", "desc");
    assertEquals("foo", rTitleDesc.get(0).getTitle());
    assertEquals("bar", rTitleDesc.get(1).getTitle());

    List<SearchResult> rDateAsc = (List<SearchResult>) peopleSearchConnector.search(null, "position", Collections.EMPTY_LIST, 0, 10, "date", "asc");
    assertEquals("foo", rDateAsc.get(0).getTitle());
    assertEquals("bar", rDateAsc.get(1).getTitle());

    List<SearchResult> rDateDesc = (List<SearchResult>) peopleSearchConnector.search(null, "position", Collections.EMPTY_LIST, 0, 10, "date", "desc");
    assertEquals("bar", rDateDesc.get(0).getTitle());
    assertEquals("foo", rDateDesc.get(1).getTitle());
  }
  
  public void testSearchSpecialCharacters() throws Exception {
    Identity rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    Profile rootProfile = new Profile(rootIdentity);

    rootProfile.setProperty(Profile.FIRST_NAME, "広いニーズ");
    rootProfile.setProperty(Profile.EMAIL, "root@mail.com");
    rootProfile.setProperty(Profile.GENDER, "Male");
    rootProfile.setProperty(Profile.POSITION, "worker");

    Map<String, String> xFoo = new HashMap<String, String>();
    List<Map<String, String>> xFoos = new ArrayList<Map<String, String>>();
    xFoo.put(Profile.EXPERIENCES_SKILLS, "cheating");
    xFoo.put(Profile.EXPERIENCES_POSITION, "ceo");
    xFoo.put(Profile.EXPERIENCES_COMPANY, "at home");
    xFoo.put(Profile.EXPERIENCES_DESCRIPTION, "play games");
    xFoos.add(xFoo);
    rootProfile.setProperty(Profile.EXPERIENCES, xFoos);

    List<Map<String, String>> phones = new ArrayList<Map<String, String>>();
    Map<String, String> phone1 = new HashMap<String, String>();
    phone1.put("key", "Work");
    phone1.put("value", "+17889989");
    phones.add(phone1);
    rootProfile.setProperty(Profile.CONTACT_PHONES, phones);

    identityManager.saveIdentity(rootIdentity);
    identityManager.saveProfile(rootProfile);
    tearDown.add(rootIdentity.getId());
    
    assertEquals(1, peopleSearchConnector.search(null, "広いニーズ", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());

    rootProfile.setProperty(Profile.POSITION, StringEscapeUtils.escapeHtml("広いニーズ"));
    rootProfile.setProperty(Profile.FIRST_NAME, "root");
    identityManager.saveProfile(rootProfile);
    assertEquals(1, peopleSearchConnector.search(null, "広いニーズ", Collections.EMPTY_LIST, 0, 10, "relevancy", "asc").size());
  }
}

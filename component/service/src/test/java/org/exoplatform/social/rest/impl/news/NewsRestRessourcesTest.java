package org.exoplatform.social.rest.impl.news;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.HashMap;
import java.util.Map;

public class NewsRestRessourcesTest extends AbstractResourceTest {

  private IdentityManager      identityManager;

  private UserACL              userACL;

  private ActivityManager      activityManager;

  private SpaceService         spaceService;

  private NewsRestRessourcesV1 newsRestRessourcesV1;

  private Identity             rootIdentity;


  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    newsRestRessourcesV1 = new NewsRestRessourcesV1();
    registry(newsRestRessourcesV1);
  }

  public void testclickOnNews() throws Exception {
    // Given
    startSessionAs("root");
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    try {
      ExoSocialActivity newsActivity = new ExoSocialActivityImpl();
      Map<String, String> newsTemplateParams = new HashMap<>();
      newsActivity.setType("news");
      newsActivity.setTitle("Activity news Title");
      newsActivity.setBody("Activity news Content");
      newsActivity.setTemplateParams(newsTemplateParams);
      activityManager.saveActivityNoReturn(spaceIdentity, newsActivity);

      // When
      activityManager.saveActivityNoReturn(spaceIdentity, newsActivity);
      assertEquals(0, activityManager.getActivityFilesIds(newsActivity).size());
      ExoSocialActivity createdNews = activityManager.getActivity(newsActivity.getId());
      String input = "{\"name\":readMore}";
      // Then
      ContainerResponse response = getResponse("POST", getURLResource("news/" + createdNews.getId() + "/click"), input);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  private Space getSpaceInstance(int number, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName("space" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    this.spaceService.createSpace(space, creator);
    return space;
  }

}

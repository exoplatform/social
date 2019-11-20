package org.exoplatform.social.rest.impl.comment;

import org.exoplatform.services.rest.impl.ContainerResponse;
import java.util.ArrayList;
import java.util.Collection;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class CommentRestResourcesTest extends AbstractResourceTest {
  private CommentRestResourcesV1 commentRestResourcesV1;

  private IdentityStorage        identityStorage;

  private ActivityManager        activityManager;

  private SpaceService           spaceService;

  private Identity               rootIdentity;

  private Identity               johnIdentity;

  private Identity               testSpaceIdentity;

  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);

    commentRestResourcesV1 = new CommentRestResourcesV1(activityManager, null);
    registry(commentRestResourcesV1);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(commentRestResourcesV1.getClass());
  }

  public void testShouldReturnCommentWhenEntityIsFound() throws Exception {
    // Given
    startSessionAs("root");
    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
    testSpaceActivity.setTitle("activity title");
    testSpaceActivity.setPosterId(rootIdentity.getId());
    testSpaceActivity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);
    ExoSocialActivity testComment = new ExoSocialActivityImpl();
    testComment.setPosterId(rootIdentity.getId());
    testComment.setUserId(rootIdentity.getId());
    testComment.setTitle("Test Comment");
    ContainerResponse response = null;
    activityManager.saveComment(testSpaceActivity, testComment);

    // When
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/comments/" + testComment.getId(), "", null, null);

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity commentEntity = getBaseEntity((DataEntity) response.getEntity(), CommentEntity.class);
    assertNotNull(commentEntity);
    assertNotNull(commentEntity.getTitle());
    assertEquals(commentEntity.getTitle(), "Test Comment");

    // Finally
    if (space != null) {
      spaceService.deleteSpace(space);
    }
  }

  public void testShouldNotReturnCommentWhenEntityIsNotFound() throws Exception {
    // Given
    startSessionAs("root");
    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
    testSpaceActivity.setTitle("activity title");
    testSpaceActivity.setPosterId(rootIdentity.getId());
    testSpaceActivity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);
    activityManager.deleteActivity(testSpaceActivity);
    ContainerResponse response = null;

    // When
    response =
             service("GET", "/" + VersionResources.VERSION_ONE + "/social/comments/" + testSpaceActivity.getId(), "", null, null);

    // Then
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    // Finally
    if (space != null) {
      spaceService.deleteSpace(space);
    }
  }

  public void testShouldReturnNotAuthorizedResponseWhenUserIsNotMemberOfSpace() throws Exception {
    // Given
    startSessionAs("john");
    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
    testSpaceActivity.setTitle("activity title");
    testSpaceActivity.setPosterId(rootIdentity.getId());
    testSpaceActivity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);
    ExoSocialActivity testComment = new ExoSocialActivityImpl();
    testComment.setPosterId(rootIdentity.getId());
    testComment.setUserId(rootIdentity.getId());
    testComment.setTitle("Test Comment");
    ContainerResponse response = null;
    activityManager.saveComment(testSpaceActivity, testComment);

    // When
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/comments/" + testComment.getId(), "", null, null);

    // Then
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    // Finally
    if (space != null) {
      spaceService.deleteSpace(space);
    }
  }
  public void testGetSpaceActivity() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);

    try {
      ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
      testSpaceActivity.setTitle("Test activity");
      activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);
      ExoSocialActivity testComment = new ExoSocialActivityImpl();
      testComment.setTitle("Test Comment");
      ContainerResponse response = null;
      activityManager.saveComment(testSpaceActivity, testComment);
      // Test get a comment
      response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/comments/" + testComment.getId(), "", null, null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      CommentEntity commentEntity = getBaseEntity((DataEntity) response.getEntity(), CommentEntity.class);
      assertNotNull(commentEntity);
      assertNotNull(commentEntity.getTitle());
      assertEquals(commentEntity.getTitle(), "Test Comment");

      // Test get an activity which is not a comment
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/comments/" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());

      // Test get a comment when logged user is not a member of space in which
      // the comment is posted
      startSessionAs("John");
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/comments/" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(401, response.getStatus());
    } catch (Exception exc) {
      log.error(exc);
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  private Space getSpaceInstance(String prettyName, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName(prettyName);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + prettyName);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    this.spaceService.createSpace(space, creator);
    return space;
  }
}

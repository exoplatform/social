package org.exoplatform.social.rest.impl.spacesadministration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.rest.entity.SpacesAdministrationMembershipsEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpacesAdministrationRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private UserACL userACL;
  private SpacesAdministrationService spacesAdministrationService;

  private SpacesAdministrationRestResourcesV1 spacesAdministrationRestResourcesV1;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    userACL = getContainer().getComponentInstanceOfType(UserACL.class);
    spacesAdministrationService = getContainer().getComponentInstanceOfType(SpacesAdministrationService.class);

    identityManager.getOrCreateIdentity("organization", "root", true);
    identityManager.getOrCreateIdentity("organization", "john", true);
    identityManager.getOrCreateIdentity("organization", "mary", true);
    identityManager.getOrCreateIdentity("organization", "demo", true);

    spacesAdministrationRestResourcesV1 = new SpacesAdministrationRestResourcesV1(spacesAdministrationService, userACL);
    registry(spacesAdministrationRestResourcesV1);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(spacesAdministrationRestResourcesV1.getClass());
  }

  public void testShouldReturnAllSpacesAdministratorsSettings() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));
    spacesAdministrationService.updateSpacesCreatorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/administrators", "*"),
            new MembershipEntry("/platform/users", "member")
    ));

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<SpacesAdministrationMembershipsEntity> membershipsEntities = (List<SpacesAdministrationMembershipsEntity>) response.getEntity();
    assertNotNull(membershipsEntities);
    assertEquals(2, membershipsEntities.size());
    List<SpacesAdministrationMembershipsEntity> spacesAdministrators = membershipsEntities.stream().filter(m -> m.getId().equals("spacesAdministrators")).collect(Collectors.toList());
    assertNotNull(spacesAdministrators);
    assertEquals(1, spacesAdministrators.size());
    List<MembershipEntry> spacesAdministratorsMemberships = spacesAdministrators.get(0).getMemberships();
    assertNotNull(spacesAdministratorsMemberships);
    assertEquals(2, spacesAdministratorsMemberships.size());
    assertTrue(spacesAdministratorsMemberships.contains(new MembershipEntry("/platform/users", "manager")));
    assertTrue(spacesAdministratorsMemberships.contains(new MembershipEntry("/platform/administrators", "*")));
    List<SpacesAdministrationMembershipsEntity> spacesCreators = membershipsEntities.stream().filter(m -> m.getId().equals("spacesCreators")).collect(Collectors.toList());
    assertNotNull(spacesCreators);
    assertEquals(1, spacesCreators.size());
    List<MembershipEntry> spacesCreatorsMemberships = spacesCreators.get(0).getMemberships();
    assertNotNull(spacesCreatorsMemberships);
    assertEquals(2, spacesCreatorsMemberships.size());
    assertTrue(spacesCreatorsMemberships.contains(new MembershipEntry("/platform/administrators", "*")));
    assertTrue(spacesCreatorsMemberships.contains(new MembershipEntry("/platform/users", "member")));
  }

  public void testShouldNotAuthorizedWhenGettingAllSettingsAsNotPlatformAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));
    spacesAdministrationService.updateSpacesCreatorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/administrators", "*"),
            new MembershipEntry("/platform/users", "member")
    ));

    startSessionAs("mary");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions"), "", null, null, "mary");

    // Then
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }

  public void testShouldReturnEmptySpacesAdministratorsWhenSettingIsEmpty() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Collections.EMPTY_LIST);

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions/spacesAdministrators"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    SpacesAdministrationMembershipsEntity membershipsEntity = (SpacesAdministrationMembershipsEntity) response.getEntity();
    List<MembershipEntry> memberships = membershipsEntity.getMemberships();
    assertNotNull(memberships);
    assertEquals(0, memberships.size());
  }

  public void testShouldReturnSpacesAdministratorsWhenSettingIsNotNull() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions/spacesAdministrators"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    SpacesAdministrationMembershipsEntity membershipsEntity = (SpacesAdministrationMembershipsEntity) response.getEntity();
    List<MembershipEntry> memberships = membershipsEntity.getMemberships();
    assertNotNull(memberships);
    assertEquals(2, memberships.size());
    assertTrue(memberships.contains(new MembershipEntry("/platform/users", "manager")));
    assertTrue(memberships.contains(new MembershipEntry("/platform/administrators", "*")));
  }

  public void testShouldReturnNotAuthorizedWhenGettingSpacesAdministratorsAsNotSpacesAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));

    startSessionAs("mary");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions/spacesAdministrators"), "", null, null, "mary");

    // Then
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }

  public void testShouldUpdateSpacesAdministratorsWhenSpacesAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));
    String newMemberships = "[" +
            " {" +
            "   \"membershipType\": \"member\"," +
            "   \"group\": \"/platform/users\"" +
            " }" +
            "]";
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");

    startSessionAs("root");

    // When
    ContainerResponse response = service("PUT", getURLResource("spacesAdministration/permissions/spacesAdministrators"), "", headers, newMemberships.getBytes(), "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<MembershipEntry> spacesAdministratorsMemberships = spacesAdministrationService.getSpacesAdministratorsMemberships();
    assertNotNull(spacesAdministratorsMemberships);
    assertEquals(1, spacesAdministratorsMemberships.size());
    assertTrue(spacesAdministratorsMemberships.contains(new MembershipEntry("/platform/users", "member")));
  }

  public void testShouldNotAuthorizedWhenUpdatingSpacesAdministratorsAsNotPlatformAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));
    String newMemberships = "[" +
            " {" +
            "   \"membershipType\": \"member\"," +
            "   \"group\": \"/platform/users\"" +
            " }" +
            "]";
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");

    startSessionAs("mary");

    // When
    ContainerResponse response = service("PUT", getURLResource("spacesAdministration/permissions/spacesAdministrators"), "", headers, newMemberships.getBytes(), "mary");

    // Then
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }


  public void testShouldReturnEmptySpacesCreatorsWhenSettingIsEmpty() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesCreatorsMemberships(Collections.EMPTY_LIST);

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions/spacesCreators"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    SpacesAdministrationMembershipsEntity membershipsEntity = (SpacesAdministrationMembershipsEntity) response.getEntity();
    List<MembershipEntry> memberships = membershipsEntity.getMemberships();
    assertNotNull(memberships);
    assertEquals(0, memberships.size());
  }

  public void testShouldReturnSpacesCreatorsWhenSettingIsNotNull() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesCreatorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions/spacesCreators"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    SpacesAdministrationMembershipsEntity membershipsEntity = (SpacesAdministrationMembershipsEntity) response.getEntity();
    List<MembershipEntry> memberships = membershipsEntity.getMemberships();
    assertNotNull(memberships);
    assertEquals(2, memberships.size());
    assertTrue(memberships.contains(new MembershipEntry("/platform/users", "manager")));
    assertTrue(memberships.contains(new MembershipEntry("/platform/administrators", "*")));
  }

  public void testShouldReturnNotAuthorizedWhenGettingSpacesCreatorsAsNotPlatformAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));

    startSessionAs("mary");

    // When
    ContainerResponse response = service("GET", getURLResource("spacesAdministration/permissions/spacesCreators"), "", null, null, "mary");

    // Then
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }

  public void testShouldUpdateSpacesCreatorsWhenSpacesAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesCreatorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));
    String newMemberships = "[" +
            " {" +
            "   \"membershipType\": \"member\"," +
            "   \"group\": \"/platform/users\"" +
            " }" +
            "]";
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");

    startSessionAs("root");

    // When
    ContainerResponse response = service("PUT", getURLResource("spacesAdministration/permissions/spacesCreators"), "", headers, newMemberships.getBytes(), "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<MembershipEntry> spacesCreatorsMemberships = spacesAdministrationService.getSpacesAdministratorsMemberships();
    assertNotNull(spacesCreatorsMemberships);
    assertEquals(1, spacesCreatorsMemberships.size());
    assertTrue(spacesCreatorsMemberships.contains(new MembershipEntry("/platform/users", "member")));
  }

  public void testShouldNotAuthorizedWhenUpdatingSpacesCreatorsAsNotPlatformAdministrator() throws Exception {
    // Given
    spacesAdministrationService.updateSpacesCreatorsMemberships(Arrays.asList(
            new MembershipEntry("/platform/users", "manager"),
            new MembershipEntry("/platform/administrators", "*")
    ));
    String newMemberships = "[" +
            " {" +
            "   \"membershipType\": \"member\"," +
            "   \"group\": \"/platform/users\"" +
            " }" +
            "]";
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");

    startSessionAs("mary");

    // When
    ContainerResponse response = service("PUT", getURLResource("spacesAdministration/permissions/spacesCreators"), "", headers, newMemberships.getBytes(), "mary");

    // Then
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }
}

package org.exoplatform.social.core.space.spi;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpacesAdministrationServiceTest extends AbstractCoreTest {

  private SpacesAdministrationService spacesAdministrationService;

  public void setUp() {
    spacesAdministrationService = CommonsUtils.getService(SpacesAdministrationService.class);
  }

  public void tearDown() {
  }

  public void testShouldReturnTrueWhenUserIsSuperUser() {
    // Given
    startSessionAs("root");

    // When
    boolean spaceCreator = spacesAdministrationService.canCreateSpace("root");

    // Then
    assertTrue(spaceCreator);
  }
  
  public void testReturnTrueWhenUserIsMemberofSecondGroup() {
    //Given
    startSessionAs("mary");
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(new MembershipEntry("/organization/management",
                                                                                                        "manager"),
                                                                                    new MembershipEntry("/platform/users",
                                                                                                        "*")));  

    // When
    boolean spaceCreator = spacesAdministrationService.canCreateSpace("mary");
    
    //Then
    assertTrue(spaceCreator);
  }
  
  public void testReturnFalseWhenUserIsNotMember() {
    //Given
    startSessionAs("leo");
    List<MembershipEntry> spaceCreatorsMemberships = new ArrayList<>();
    spaceCreatorsMemberships.add(new MembershipEntry("/organization/management", "*"));
    
    // When
    spacesAdministrationService.updateSpacesCreatorsMemberships(spaceCreatorsMemberships);
    boolean spaceCreator = spacesAdministrationService.canCreateSpace("leo");
    
    //Then
    assertFalse(spaceCreator); 
  }

}

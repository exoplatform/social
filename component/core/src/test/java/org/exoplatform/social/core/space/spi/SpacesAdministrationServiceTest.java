package org.exoplatform.social.core.space.spi;

import org.exoplatform.commons.utils.CommonsUtils;
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

}

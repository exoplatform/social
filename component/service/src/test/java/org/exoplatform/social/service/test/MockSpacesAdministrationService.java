package org.exoplatform.social.service.test;

import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import java.util.ArrayList;
import java.util.List;

public class MockSpacesAdministrationService implements SpacesAdministrationService {

  private List<MembershipEntry> spacesAdministrators = new ArrayList<>();

  private List<MembershipEntry> spacesCreators = new ArrayList<>();

  @Override
  public List<MembershipEntry> getSuperManagersMemberships() {
    return spacesAdministrators;
  }

  @Override
  public void updateSuperManagersMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions != null) {
      spacesAdministrators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesAdministrators.clear();
    }
  }

  @Override
  public List<MembershipEntry> getSuperCreatorsMemberships() {
    return spacesCreators;
  }

  @Override
  public void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions != null) {
      spacesCreators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesCreators.clear();
    }
  }

  @Override
  public boolean IsSpaceCreator(String Username) {
    return false;
  }

  @Override
  public boolean checkUsernameInSpaceCreators(String Username) {
    return false;
  }
}

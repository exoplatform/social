package org.exoplatform.social.service.test;

import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import java.util.List;

public class MockSpacesAdministrationService implements SpacesAdministrationService {
  @Override
  public List<MembershipEntry> getSuperManagersMemberships() {
    return null;
  }

  @Override
  public void addSuperManagersMembership(String permissionExpression) {

  }

  @Override
  public void removeSuperManagersMembership(String permissionExpression) {

  }

  @Override
  public List<MembershipEntry> getSuperCreatorsMemberships() {
    return null;
  }

  @Override
  public void addSpacesCreatorsMembership(String permissionExpression) {

  }

  @Override
  public void removeSpacesCreatorsMembership(String permissionExpression) {

  }
}

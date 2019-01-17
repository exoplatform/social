package org.exoplatform.social.core.space;

import org.exoplatform.services.security.MembershipEntry;

import java.util.List;

/**
 * Service to manage administration of spaces
 */
public interface SpacesAdministrationService {
  /**
   * Returns the list of super managers memberships (permission expressions)
   *
   * @return a {@link List} of memberships of type {@link String}
   */
  List<MembershipEntry> getSpacesAdministratorsMemberships();

  /**
   * Update spaces super manager memberships
   *
   * @param permissionsExpressions permission expression of type {@link String} with format 'mstype:groupId'
   */
  void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions);

  /**
   * Returns the list of creators memberships (permission expressions)
   *
   * @return a {@link List} of memberships of type {@link String}
   */
  List<MembershipEntry> getSpacesCreatorsMemberships();

  /**
   * Update spaces super creator memberships
   *
   * @param permissionsExpressions
   */
  void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions);


  /**
   * Check if the user can create spaces
   * @param username
   * @return true if the user can create spaces
   */
  boolean canCreateSpace(String username) ;

}

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
  List<MembershipEntry> getSuperManagersMemberships();

  /**
   * Update spaces super manager memberships
   *
   * @param permissionsExpressions permission expression of type {@link String} with format 'mstype:groupId'
   */
  void updateSuperManagersMemberships(List<MembershipEntry> permissionsExpressions);

  /**
   * Returns the list of creators memberships (permission expressions)
   *
   * @return a {@link List} of memberships of type {@link String}
   */
  List<MembershipEntry> getSuperCreatorsMemberships();

  /**
   * Update spaces super creator memberships
   *
   * @param permissionsExpressions
   */
  void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions);

  
  /**
   * check if username exist in spacesCreatorsMembership
   * 
   * @param Username
   * @return
   */
  boolean IsSpaceCreator(String Username) ;

}

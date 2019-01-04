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
   * Add spaces super manager membership
   *
   * @param permissionExpression permission expression of type {@link String} with format 'mstype:groupId'
   */
  void addSuperManagersMembership(String permissionExpression);

  /**
   * Remove spaces super manager membership
   *
   * @param permissionExpression permission expression of type {@link String} with format 'mstype:groupId'
   */
  void removeSuperManagersMembership(String permissionExpression);

  /**
   * Returns the list of creators memberships (permission expressions)
   *
   * @return a {@link List} of memberships of type {@link String}
   */
  List<MembershipEntry> getSuperCreatorsMemberships();

  /**
   * Add spaces super creator membership
   *
   * @param permissionExpression
   */
  void addSpacesCreatorsMembership(String permissionExpression);

  /**
   * remove spaces super creator membership
   *
   * @param permissionExpression
   */
  void removeSpacesCreatorsMembership(String permissionExpression);
}

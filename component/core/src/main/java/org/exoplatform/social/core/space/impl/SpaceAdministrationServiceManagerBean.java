package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

@Managed
@ManagedDescription("Social Space Administration Service manager bean")
@NameTemplate({ @Property(key = "service", value = "social"), @Property(key = "view", value = "SpaceAdministrationService") })
@RESTEndpoint(path = "spaceadministrationservice")
public class SpaceAdministrationServiceManagerBean {
  private SpacesAdministrationService spacesAdministrationService;

  public SpaceAdministrationServiceManagerBean(SpacesAdministrationServiceImpl spacesAdministrationServiceImpl) {
    this.spacesAdministrationService = spacesAdministrationServiceImpl;
  }

  /**
   * Gets the list of permission expressions of space super managers.
   * See {@link SpacesAdministrationService#getSuperManagersMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces super administrators")
  @Impact(ImpactType.READ)
  public List<String> getSpaceManager() {
    return spacesAdministrationService.getSuperManagersMemberships()
                       .stream()
                       .map(membership -> membership.toString())
                       .collect(Collectors.toList());
  }

  /**
   * Adds a membership in spaces administrators
   *
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces super managers membership")
  @Impact(ImpactType.WRITE)
  public void addSpaceManager(@ManagedDescription("Spaces super manger role") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superManagersMemberships = new ArrayList<>(spacesAdministrationService.getSuperManagersMemberships());
    superManagersMemberships.add(MembershipEntry.parse(permissionExpression));
    spacesAdministrationService.updateSuperManagersMemberships(superManagersMemberships);
  }

  /**
   * Removes a membership from spaces administrators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces super managers membership")
  @Impact(ImpactType.WRITE)
  public void removeSpaceManager(@ManagedDescription("Spaces super manger memberships") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superManagersMemberships = spacesAdministrationService.getSuperManagersMemberships();
    List<MembershipEntry> updatedMemberships = superManagersMemberships.stream()
            .filter(m -> !m.toString().equals(permissionExpression))
            .collect(Collectors.toList());
    spacesAdministrationService.updateSuperManagersMemberships(updatedMemberships);
  }
  
  /**
   * Gets the list of permission expressions of space creators.
   * See {@link SpacesAdministrationService#getSuperCreatorsMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces creators memberships")
  @Impact(ImpactType.READ)
  public List<String> getSpacesCreatorsMemberships() {
    return spacesAdministrationService.getSuperCreatorsMemberships()
                       .stream()
                       .map(membership -> membership.toString())
                       .collect(Collectors.toList());
  }
  
  /**
   * Adds a membership in spaces creators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces creators membership")
  @Impact(ImpactType.WRITE)
  public void addSpacesCreatorsMembership(@ManagedDescription("Spaces creator membership") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superCreatorsMemberships = new ArrayList<>(spacesAdministrationService.getSuperCreatorsMemberships());
    superCreatorsMemberships.add(MembershipEntry.parse(permissionExpression));
    spacesAdministrationService.updateSpacesCreatorsMemberships(superCreatorsMemberships);
  }
  
  /**
   * Removes a membership in spaces creators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces creators membership")
  @Impact(ImpactType.WRITE)
  public void removeSpacesCreatorsMembership(@ManagedDescription("Spaces creator membership") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superCreatorsMemberships = spacesAdministrationService.getSuperCreatorsMemberships();
    List<MembershipEntry> updatedMemberships = superCreatorsMemberships.stream()
            .filter(m -> !m.toString().equals(permissionExpression))
            .collect(Collectors.toList());
    spacesAdministrationService.updateSpacesCreatorsMemberships(updatedMemberships);
  }
}

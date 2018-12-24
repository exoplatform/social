package org.exoplatform.social.core.space.impl;

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
import org.exoplatform.social.core.space.spi.SpaceService;

@Managed
@ManagedDescription("Social Service manager bean")
@NameTemplate({ @Property(key = "service", value = "social"), @Property(key = "view", value = "SpaceService") })
@RESTEndpoint(path = "spaceservice")
public class SpaceServiceManagerBean {
  SpaceService spaceService;

  public SpaceServiceManagerBean(SpaceServiceImpl spaceServiceImpl) {
    this.spaceService = spaceServiceImpl;
  }

  /**
   * Gets the list of permission expressions of space super managers.
   * See {@link SpaceService#getSuperManagersMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces super administrators")
  @Impact(ImpactType.READ)
  public List<String> getSpaceManager() {
    return spaceService.getSuperManagersMemberships()
                       .stream()
                       .map(membership -> membership.getMembershipType() + ":" + membership.getGroup())
                       .collect(Collectors.toList());
  }

  /**
   * See {@link SpaceService#addSuperManagersMembership(String)}
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces super managers membership")
  @Impact(ImpactType.WRITE)
  public void addSpaceManager(@ManagedDescription("Spaces super manger role") @ManagedName("permissionExpression") String permissionExpression) {
    spaceService.addSuperManagersMembership(permissionExpression);
  }

  /**
   * See {@link SpaceService#removeSuperManagersMembership(String)}
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces super managers membership")
  @Impact(ImpactType.WRITE)
  public void removeSpaceManager(@ManagedDescription("Spaces super manger memberships") @ManagedName("permissionExpression") String permissionExpression) {
    spaceService.removeSuperManagersMembership(permissionExpression);
  }
  
  /**
   * Gets the list of permission expressions of space creators.
   * See {@link SpaceService#getSuperCreatorsMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces creators memberships")
  @Impact(ImpactType.READ)
  public List<String> getSpacesCreatorsMemberships() {
    return spaceService.getSuperCreatorsMemberships()
                       .stream()
                       .map(membership -> membership.getMembershipType() + ":" + membership.getGroup())
                       .collect(Collectors.toList());
  }
  
  /**
   * See {@link SpaceService#addSuperCreatorsMembership(String)}
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces creators membership")
  @Impact(ImpactType.WRITE)
  public void addSpacesCreatorsMembership(@ManagedDescription("Spaces creator membership") @ManagedName("permissionExpression") String permissionExpression) {
    spaceService.addSpacesCreatorsMembership(permissionExpression);
  }
  
  /**
   * See {@link SpaceService#removeSuperManagersMembership(String)}
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces creators membership")
  @Impact(ImpactType.WRITE)
  public void removeSpacesCreatorsMembership(@ManagedDescription("Spaces creator membership") @ManagedName("permissionExpression") String permissionExpression) {
    spaceService.removeSpacesCreatorsMembership(permissionExpression);
  }
}

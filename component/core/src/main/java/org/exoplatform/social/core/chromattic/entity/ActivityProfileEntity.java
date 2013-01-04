package org.exoplatform.social.core.chromattic.entity;

import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

@PrimaryType(name = "soc:activityinfodefinition")
public abstract class ActivityProfileEntity {
  
  @Id
  public abstract String getId();

  @Property(name = "soc:userProfileActivityId")
  public abstract String getUserProfileActivityId();
  public abstract void setUserProfileActivityId(String userProfileActivityId);
  
  @Property(name = "soc:spaceProfileActivityId")
  public abstract String getSpaceProfileActivityId();
  public abstract void setSpaceProfileActivityId(String spaceProfileActivityId);
  
  @Property(name = "soc:relationProfileActivityId")
  public abstract String getRelationActivityId();
  public abstract void setRelationActivityId(String relationActivityId);
}

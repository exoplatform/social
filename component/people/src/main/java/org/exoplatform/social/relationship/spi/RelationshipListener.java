package org.exoplatform.social.relationship.spi;

import org.exoplatform.social.lifecycle.LifeCycleListener;

public interface RelationshipListener extends LifeCycleListener<RelationshipEvent> {
  
  void confirmed(RelationshipEvent event);
  
  void ignored(RelationshipEvent event);
  
  void removed(RelationshipEvent event);
  
}

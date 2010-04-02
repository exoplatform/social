package org.exoplatform.social.core.relationship;

import org.exoplatform.social.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.relationship.spi.RelationshipEvent;
import org.exoplatform.social.relationship.spi.RelationshipListener;
import org.exoplatform.social.relationship.spi.RelationshipEvent.Type;


public class RelationshipLifeCycle extends AbstractLifeCycle<RelationshipListener,RelationshipEvent> {

  @Override
  protected void dispatchEvent(RelationshipListener listener,
                               RelationshipEvent event) {
    switch (event.getType()) {
    case CONFIRM:
      listener.confirmed(event);
      break;
    case REMOVE:
      listener.removed(event);
      break;
    case IGNORE:
      listener.ignored(event);
      break;
 
    default:
      break;
    }
    
  }
  
  public void relationshipConfirmed(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.CONFIRM, relationshipManager, relationship));
  }

  public void relationshipRemoved(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.REMOVE, relationshipManager, relationship));
  }

  public void relationshipIgnored(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.IGNORE, relationshipManager, relationship));
  }

}

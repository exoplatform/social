package org.exoplatform.social.core.relationship.lifecycle;

import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
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
    case PENDING:
      listener.requested(event);
      break; 
    case DENIED:
      listener.denied(event);
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

  public void relationshipRequested(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.PENDING, relationshipManager, relationship));
  }

  public void relationshipDenied(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.DENIED, relationshipManager, relationship));
  }
  
}

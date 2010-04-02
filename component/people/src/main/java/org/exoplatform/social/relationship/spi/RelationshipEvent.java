package org.exoplatform.social.relationship.spi;

import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.lifecycle.LifeCycleEvent;

public class RelationshipEvent extends LifeCycleEvent<RelationshipManager, Relationship> {

  public enum Type {
    REMOVE, IGNORE, CONFIRM, PENDING, DENIED
  }

  private Type type;

  public RelationshipEvent(Type type, RelationshipManager source, Relationship payload) {
    super(source, payload);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public String toString() {
    return payload.getIdentity1().getProfile().getFullName() + " " + type + " "
        + payload.getIdentity2().getProfile().getFullName();
  }

}

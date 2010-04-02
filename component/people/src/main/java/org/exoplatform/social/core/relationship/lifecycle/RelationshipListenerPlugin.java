package org.exoplatform.social.core.relationship.lifecycle;

import org.exoplatform.social.lifecycle.AbstractListenerPlugin;
import org.exoplatform.social.relationship.spi.RelationshipEvent;
import org.exoplatform.social.relationship.spi.RelationshipListener;

public abstract class RelationshipListenerPlugin extends AbstractListenerPlugin implements RelationshipListener {

  public abstract void confirmed(RelationshipEvent event);
  public abstract void ignored(RelationshipEvent event);
  public abstract void removed(RelationshipEvent event);

}

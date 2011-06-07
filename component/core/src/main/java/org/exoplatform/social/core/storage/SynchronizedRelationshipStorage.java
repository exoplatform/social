/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedRelationshipStorage extends RelationshipStorage {

  @Override
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.saveRelationship(relationship);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void removeRelationship(final Relationship relationship) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      super.removeRelationship(relationship);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Relationship getRelationship(final String uuid) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      return super.getRelationship(uuid);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<Relationship> getSenderRelationships(final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      return super.getSenderRelationships(sender, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Relationship> getReceiverRelationships(final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      return super.getReceiverRelationships(receiver, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Relationship getRelationship(final Identity identity1, final Identity identity2) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getRelationship(identity1, identity2);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Relationship> getRelationships(final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      return super.getRelationships(identity, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Identity> getConnections(final Identity identity, final int offset, final int limit) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      return super.getConnections(identity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getConnectionsCount(final Identity identity) throws RelationshipStorageException {


    boolean created = startSynchronization();
    try {
      return super.getConnectionsCount(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

}

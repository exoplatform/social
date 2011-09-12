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

package org.exoplatform.social.extras.migration.io;

import org.exoplatform.social.extras.migration.MigrationException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Writer context.
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WriterContext {

  public enum DataType {
    IDENTITIES,
    SPACES,
    PROFILES,
    RELATIONSHIPS,
    ACTIVITIES;

    public String nodeName() {
      return toString().toLowerCase();
    }
  }

  private final String CONTEXT_FROM_VERSION = "from";
  private final String CONTEXT_TO_VERSION = "to";
  private final String CONTEXT_NODE_NAME = "migration_context";
  private final String CONTEXT_VALUE_NAME = "value";
  private final String CONTEXT_COMPLETION_NAME = "completion";
  private final String CONTEXT_DONE_NAME = "done";


  private final Session session;

  public WriterContext(final Session session, final String from, final String to) {

    this.session = session;

    if (exists()) {
      throw new MigrationException("Unable to init context because it already exists. Please use rollback command.");
    }

    set(CONTEXT_FROM_VERSION, from);
    set(CONTEXT_TO_VERSION, to);

  }

  public WriterContext(final Session session) {

    this.session = session;

    if (!exists()) {
      throw new MigrationException("Unable to restore context because it doesn't exists.");
    }

  }

  public String get(String key) {

    try {
      return getContextNode().getNode(key).getProperty(CONTEXT_VALUE_NAME).getString();
    }
    catch (RepositoryException e) {
      return null;
    }

  }

  public void put(String key, String value) {

    if (key == null) {
      throw new NullPointerException();
    }

    Node keyNode;

    try {
      keyNode = getContextNode().getNode(key);
    }
    catch (RepositoryException e) {
      try {
        keyNode = getContextNode().addNode(key);
      }
      catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }
    }

    try {
      keyNode.setProperty(CONTEXT_VALUE_NAME, value);
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  public String getFrom() {

    return getString(CONTEXT_FROM_VERSION);

  }

  public String getTo() {

    return getString(CONTEXT_TO_VERSION);

  }

  public boolean isCompleted(DataType type) {

    return getBoolean(CONTEXT_COMPLETION_NAME + "_" + type.nodeName());

  }

  public void setCompleted(DataType type) {

    set(CONTEXT_COMPLETION_NAME + "_" + type.nodeName(), true);

  }

  public Long getDone(DataType type) {

    return getLong(CONTEXT_DONE_NAME + "_" + type.nodeName());

  }
  
  public void incDone(DataType type) {

    Long current = getDone(type);
    set(CONTEXT_DONE_NAME + "_" + type.nodeName(), ++current);

  }

  public void cleanup() {

    try {
      NodeIterator it = getContextNode().getNodes();
      while (it.hasNext()) {
        it.nextNode().remove();
        session.save();
      }
      getContextNode().remove();
      session.save();
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  private Node getContextNode() {

    try {
      return session.getRootNode().getNode(CONTEXT_NODE_NAME);
    }
    catch (RepositoryException e) {
      try {
        return session.getRootNode().addNode(CONTEXT_NODE_NAME);
      }
      catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }
    }

  }

  private boolean exists() {

    try {
      session.getRootNode().getNode(CONTEXT_NODE_NAME);
      return true;
    }
    catch (RepositoryException e) {
      return false;
    }

  }

  private void set(String key, String value) {

    try {
      getContextNode().setProperty(key, value);
      session.save();
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  private void set(String key, boolean value) {

    try {
      getContextNode().setProperty(key, value);
      session.save();
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  private void set(String key, Long value) {

    try {
      getContextNode().setProperty(key, value);
      session.save();
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  private String getString(String key) {

    try {
      return getContextNode().getProperty(key).getString();
    }
    catch (RepositoryException e) {
      return null;
    }

  }

  private Boolean getBoolean(String key) {

    try {
      return getContextNode().getProperty(key).getBoolean();
    }
    catch (RepositoryException e) {
      return false;
    }

  }

  private Long getLong(String key) {

    try {
      return getContextNode().getProperty(key).getLong();
    }
    catch (RepositoryException e) {
      return 0L;
    }

  }

  
}

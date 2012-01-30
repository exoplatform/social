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

package org.exoplatform.social.extras.migration.rw;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RemoveManager {

  private int size;
  private int current;
  private Session session;
  private Set<String> names;

  public RemoveManager(final int size, final Session session) {
    
    this.size = size;
    this.session = session;
    this.current = 0;
    this.names = new HashSet<String>();

  }

  public void remove(final Node node) throws RepositoryException {

    if (node.getName().contains(" ")) return;

    if (names.contains(node.getName())) {
      commit();
    }

    node.remove();
    ++current;
    names.add(node.getName());

    if (current >= size) {
      commit();
    }

  }

  public void complete() throws RepositoryException {
    commit();
  }

  private void commit() throws RepositoryException {

    session.save();
    this.current = 0;
    names.clear();

  }

}

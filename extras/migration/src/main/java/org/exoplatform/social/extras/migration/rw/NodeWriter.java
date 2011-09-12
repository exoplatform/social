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

import org.exoplatform.social.extras.migration.io.WriterContext;

import javax.jcr.RepositoryException;
import java.io.InputStream;

/**
 * Write migraiton data.
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface NodeWriter {

  /**
   *  Identities writing.
   * @param is data stream
   * @param ctx migration context
   */
  void writeIdentities(InputStream is, WriterContext ctx);

  /**
   *  Spaces writing.
   * @param is data stream
   * @param ctx migration context
   */
  void writeSpaces(InputStream is, WriterContext ctx);

  /**
   *  Profiles writing.
   * @param is data stream
   * @param ctx migration context
   */
  void writeProfiles(InputStream is, WriterContext ctx);

  /**
   *  Activities writing.
   * @param is data stream
   * @param ctx migration context
   */
  void writeActivities(InputStream is, WriterContext ctx);

  /**
   *  Relationship writing.
   * @param is data stream
   * @param ctx migration context
   */
  void writeRelationships(InputStream is, WriterContext ctx);

  /**
   *  Rollback changes.
   */
  void rollback(WriterContext ctx) throws RepositoryException;

  /**
   *  Commit changes.
   */
  void commit(WriterContext ctx) throws RepositoryException;

}

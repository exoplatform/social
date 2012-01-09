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

package org.exoplatform.social.extras.migration;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Constructor;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.exoplatform.social.extras.migration.io.WriterContext;
import org.exoplatform.social.extras.migration.rw.NodeReader;
import org.exoplatform.social.extras.migration.rw.NodeWriter;

/**
 * Migration entry point.
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class MigrationTool {

  public static final int START_NODE        = 1;
  public static final int PROPERTY_SINGLE   = 2;
  public static final int PROPERTY_MULTI    = 3;
  public static final int END_NODE          = 4;

  private final IdentityStorage identityStorage;
  private final RelationshipStorage relationshipStorage;
  private final SpaceStorage spaceStorage;
  private final ActivityStorage activityStorage;

  private final OrganizationService organizationService;

  public MigrationTool() {

    PortalContainer container = PortalContainer.getInstance();

    identityStorage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorageImpl.class);
    relationshipStorage = (RelationshipStorage) container.getComponentInstanceOfType(RelationshipStorageImpl.class);
    spaceStorage = (SpaceStorage) container.getComponentInstanceOfType(SpaceStorageImpl.class);
    activityStorage = (ActivityStorage) container.getComponentInstanceOfType(ActivityStorageImpl.class);
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

  }

  /**
   * Run full migration.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws IOException
   * @throws RepositoryException
   */
  public void runAll(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws IOException, RepositoryException {

    if (!ctx.isCompleted(WriterContext.DataType.IDENTITIES)) {
      runIdentities(reader, writer, ctx);
    }

    if (!ctx.isCompleted(WriterContext.DataType.SPACES)) {
      runSpaces(reader, writer, ctx);
    }

    if (!ctx.isCompleted(WriterContext.DataType.PROFILES)) {
      runProfiles(reader, writer, ctx);
    }

    if (!ctx.isCompleted(WriterContext.DataType.RELATIONSHIPS)) {
      runRelationships(reader, writer, ctx);
    }

    if (!ctx.isCompleted(WriterContext.DataType.ACTIVITIES)) {
      runActivities(reader, writer, ctx);
    }

  }

  /**
   * Run identities migration.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws IOException
   * @throws RepositoryException
   */
  public void runIdentities(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readIdentities(os, ctx);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    writer.writeIdentities(is, ctx);
    RequestLifeCycle.end();

  }

  /**
   * Run profiles migration.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws IOException
   * @throws RepositoryException
   */
  public void runProfiles(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readProfiles(os, ctx);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    writer.writeProfiles(is, ctx);
    RequestLifeCycle.end();

  }

  /**
   * Run spaces migration.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws IOException
   * @throws RepositoryException
   */
  public void runSpaces(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readSpaces(os, ctx);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    writer.writeSpaces(is, ctx);
    RequestLifeCycle.end();

  }

  /**
   * Run relationship migration.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws IOException
   * @throws RepositoryException
   */
  public void runRelationships(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readRelationships(os, ctx);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    writer.writeRelationships(is, ctx);
    RequestLifeCycle.end();

  }

  /**
   * Run activities migration.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws IOException
   * @throws RepositoryException
   */
  public void runActivities(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readActivities(os, ctx);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    writer.writeActivities(is, ctx);
    RequestLifeCycle.end();

  }

  /**
   * Rollback changes.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws RepositoryException
   */
  public void rollback(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws RepositoryException {

    writer.rollback(ctx);

  }

  /**
   * Run commit changes.
   * @param reader data reader
   * @param writer data writer
   * @param ctx mavigation context
   * @throws RepositoryException
   */
  public void commit(NodeReader reader, NodeWriter writer, WriterContext ctx)
      throws RepositoryException {

    writer.commit(ctx);

  }

  /**
   * Run create new reader.
   * @throws IOException
   */
  public NodeReader createReader(String from, String to, Session session)
      throws RepositoryException {

    try {
      Class clazz = Class.forName("org.exoplatform.social.extras.migration.rw." + buildName(from, to, "Reader"));
      Constructor c = clazz.getConstructor(Session.class);
      return (NodeReader) c.newInstance(session);
    }
    catch (Exception e) {
      return null;
    }

  }

  /**
   * Run create new writer.
   * @throws IOException
   */
  public NodeWriter createWriter(String from, String to, Session session)
      throws RepositoryException {

    try {

      Class clazz = Class.forName("org.exoplatform.social.extras.migration.rw." + buildName(from, to, "Writer"));
      Constructor c = clazz.getConstructor(
          IdentityStorage.class,
          RelationshipStorage.class,
          SpaceStorage.class,
          ActivityStorage.class,
          OrganizationService.class,
          Session.class);
      return (NodeWriter)c.newInstance(
          identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session
      );

    }
    catch (Exception e) {
      return null;
    }

  }

  private String buildName(String from, String to, String type) {
    return String.format("Node%s_%s_%s", type, from, to);
  }

}

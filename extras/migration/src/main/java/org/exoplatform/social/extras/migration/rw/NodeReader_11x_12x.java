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

import org.exoplatform.social.extras.migration.MigrationException;
import org.exoplatform.social.extras.migration.io.NodeStreamHandler;
import org.exoplatform.social.extras.migration.io.WriterContext;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeReader_11x_12x implements NodeReader {

  private Session session;
  private Node rootNode;
  private NodeStreamHandler writer;

  public NodeReader_11x_12x(final Session session) throws RepositoryException {
    this.session = session;
    this.rootNode = session.getRootNode();
    this.writer = new NodeStreamHandler();
  }

  /**
   * {@inheritDoc}
   */
  public void readIdentities(OutputStream os, WriterContext ctx) throws RepositoryException, IOException {
    run(new IdentityRunnable(os, ctx));
  }

  /**
   * {@inheritDoc}
   */
  public void readSpaces(OutputStream os, WriterContext ctx) throws RepositoryException, IOException {
    run(new SpaceRunnable(os, ctx));
  }

  /**
   * {@inheritDoc}
   */
  public void readProfiles(OutputStream os, WriterContext ctx) throws RepositoryException, IOException {
    run(new ProfileRunnable(os, ctx));
  }

  /**
   * {@inheritDoc}
   */
  public void readActivities(OutputStream os, WriterContext ctx) throws RepositoryException, IOException {
    run(new ActivityRunnable(os, ctx));
  }

  /**
   * {@inheritDoc}
   */
  public void readRelationships(OutputStream os, WriterContext ctx) throws RepositoryException, IOException {
    run(new RelationshipRunnable(os, ctx));
  }

  private void readFrom(NodeIterator nodes, OutputStream os) throws RepositoryException, IOException {

    while(nodes.hasNext()) {
      writer.writeNode((Node) nodes.next(), os);
    }

  }

  public void checkData() throws MigrationException {

    try {
      rootNode.getNode("exo:applications");
    }
    catch (RepositoryException e) {
      throw new MigrationException("No data found for this version");
    }

  }

  private void run(Runnable r) {
    new Thread(r).start();
  }

  class IdentityRunnable implements Runnable {
    
    private OutputStream os;
    private WriterContext ctx;

    IdentityRunnable(final OutputStream os, final WriterContext ctx) {
      this.os = os;
      this.ctx = ctx;
    }

    public void run() {
      try {
        Node rootIdentity = rootNode.getNode("exo:applications/Social_Identity");
        NodeIterator it = rootIdentity.getNodes();
        it.skip(ctx.getDone(WriterContext.DataType.IDENTITIES));
        readFrom(it, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

  class RelationshipRunnable implements Runnable {

    private OutputStream os;
    private WriterContext ctx;

    RelationshipRunnable(final OutputStream os, final WriterContext ctx) {
      this.os = os;
      this.ctx = ctx;
    }

    public void run() {
      try {
        Node rootRelationship = rootNode.getNode("exo:applications/Social_Relationship");
        NodeIterator it = rootRelationship.getNodes();
        it.skip(ctx.getDone(WriterContext.DataType.RELATIONSHIPS));
        readFrom(it, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

  class SpaceRunnable implements Runnable {

    private OutputStream os;
    private WriterContext ctx;

    SpaceRunnable(final OutputStream os, final WriterContext ctx) {
      this.os = os;
      this.ctx = ctx;
    }

    public void run() {
      try {
        Node rootSpace = rootNode.getNode("exo:applications/Social_Space/Space");
        NodeIterator it = rootSpace.getNodes();
        it.skip(ctx.getDone(WriterContext.DataType.SPACES));
        readFrom(it, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }
  }

  class ActivityRunnable implements Runnable {

    private OutputStream os;
    private WriterContext ctx;

    ActivityRunnable(final OutputStream os, final WriterContext ctx) {
      this.os = os;
      this.ctx = ctx;
    }

    public void run() {

      long remaining = ctx.getDone(WriterContext.DataType.ACTIVITIES);

      try {
        Node rootOrganizationActivity = rootNode.getNode("exo:applications/Social_Activity/organization");
        NodeIterator userItOrganization = rootOrganizationActivity.getNodes();
        while (userItOrganization.hasNext()) {

          Node currentUser = userItOrganization.nextNode();
          Node publishedNode = currentUser.getNode("published");
          NodeIterator it = publishedNode.getNodes();

          long size = it.getSize();
          if (remaining >= size) {
            remaining -= size;
            continue;
          }
          else if (remaining > 0) {
            it.skip(remaining);
            readFrom(it, os);
            remaining = 0;
          }
          else {
            readFrom(it, os);
          }

        }

        Node rootSpaceActivity = rootNode.getNode("exo:applications/Social_Activity/space");
        NodeIterator userItSpace = rootSpaceActivity.getNodes();
        while(userItSpace.hasNext()) {
          Node currentUser = userItSpace.nextNode();
          Node publishedNode = currentUser.getNode("published");
          NodeIterator it = publishedNode.getNodes();

          long size = it.getSize();
          if (remaining >= size) {
            remaining -= size;
            continue;
          }
          else if (remaining > 0) {
            it.skip(remaining);
            readFrom(it, os);
            remaining = 0;
          }
          else {
            readFrom(it, os);
          }

        }
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

  class ProfileRunnable implements Runnable {

    private OutputStream os;
    private WriterContext ctx;

    ProfileRunnable(final OutputStream os, final WriterContext ctx) {
      this.os = os;
      this.ctx = ctx;
    }

    public void run() {

      try {

        //
        Node rootProfile = rootNode.getNode("exo:applications/Social_Profile");
        NodeIterator it = rootProfile.getNodes();
        it.skip(ctx.getDone(WriterContext.DataType.PROFILES));

        while(it.hasNext()) {

          //
          Node currentProfile = it.nextNode();
          writer.writeNode(currentProfile, os);

          //
          NodeIterator profileDetail = currentProfile.getNodes();
          while (profileDetail.hasNext()) {
            writer.writeNode(profileDetail.nextNode(), os);
          }

        }

        //
        os.close();

      }
      catch (Exception e) {
        throw new MigrationException(e);
      }

    }

  }

}

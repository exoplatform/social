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

import org.exoplatform.social.extras.migration.MigrationTool;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stream handler.
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeStreamHandler {

  /**
   * Write a node to a stream
   * @param node the node
   * @param os the stream
   * @throws RepositoryException
   * @throws IOException
   */
  public void writeNode(Node node, OutputStream os) throws RepositoryException, IOException {

    // Init stream.
    DataOutputStream dos = new DataOutputStream(os);

    // Write node data.
    dos.writeInt(MigrationTool.START_NODE);
    dos.writeUTF(node.getPath());

    // Write properties.
    PropertyIterator it = node.getProperties();
    while (it.hasNext()) {
      Property p = it.nextProperty();

      if (p.getDefinition().isMultiple()) {
        dos.writeInt(MigrationTool.PROPERTY_MULTI);
        dos.writeInt(p.getValues().length);
        dos.writeUTF(p.getName());
        for (Value v : p.getValues()) {
          dos.writeUTF(v.getString());
        }
      }
      else {
        dos.writeInt(MigrationTool.PROPERTY_SINGLE);
        dos.writeUTF(p.getName());
        dos.writeUTF(p.getString());
      }

    }

    dos.writeInt(MigrationTool.END_NODE);

    dos.flush();
    
  }

  /**
   * Read data from a stream
   * @param is the stream
   * @return
   */
  public NodeData readNode(InputStream is) {

    NodeData data = new NodeData();

    DataInputStream dis = new DataInputStream(is);

    try {
      while (readData(dis, data) != MigrationTool.END_NODE);
    }
    catch (IOException e) {
      return null;
    }

    return data;
  }

  /**
   * Read binary data from a stream.
   * @param dis the stream
   * @param data data to full
   * @return readed type
   * @throws IOException
   */
  private int readData(DataInputStream dis, NodeData data) throws IOException {

    int type = dis.readInt();
    switch (type) {

      case MigrationTool.START_NODE :
        data.setPath(dis.readUTF());
        break;

      case MigrationTool.PROPERTY_SINGLE :
        data.put(dis.readUTF(), dis.readUTF());
        break;

      case MigrationTool.PROPERTY_MULTI :
        int length = dis.readInt();
        String[] values = new String[length];
        String propertyName = dis.readUTF();
        for (int i = 0; i < length; ++i) {
          values[i] = dis.readUTF();
        }
        data.put(propertyName, values);
        break;

      case MigrationTool.END_NODE :
        break;

    }

    return type;

  }

}

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

package org.exoplatform.social.extras.migration.v11x_12x;

import org.exoplatform.social.extras.migration.loading.DataLoader;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class LoadDataTestCase extends AbstractMigrationTestCase {

  private DataLoader loader;
  private Node rootNode;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    loader = new DataLoader("testLoader.xml", session);
    loader.load();
    rootNode = session.getRootNode();

  }

  @Override
  public void tearDown() throws Exception {

    rootNode.getNode("fooA").remove();
    rootNode.getNode("fooB").remove();
    rootNode.getNode("fooC").remove();
    rootNode.getNode("fooD").remove();
    rootNode.getNode("exo:foo").remove();

    super.tearDown();

  }

  public void testRoot() throws Exception {

    assertNotNull(rootNode.getNode("fooA"));
    assertNotNull(rootNode.getNode("fooB"));
    assertNotNull(rootNode.getNode("fooC"));
    assertNotNull(rootNode.getNode("fooD"));
    assertNotNull(rootNode.getNode("exo:foo"));

  }

  public void testChildBar() throws Exception {

    assertNotNull(rootNode.getNode("fooA/barAA"));
    assertNotNull(rootNode.getNode("fooA/barAB"));
    assertNotNull(rootNode.getNode("fooA/barAC"));
    assertNotNull(rootNode.getNode("fooB/barBA"));
    assertNotNull(rootNode.getNode("fooB/barBB"));
    assertNotNull(rootNode.getNode("fooB/barBC"));
    assertNotNull(rootNode.getNode("fooC/barCA"));
    assertNotNull(rootNode.getNode("fooC/barCB"));
    assertNotNull(rootNode.getNode("fooC/barCC"));

  }

  public void testChildFooBar() throws Exception {

    assertNotNull(rootNode.getNode("fooA/barAA/foobarAAA"));
    assertNotNull(rootNode.getNode("fooA/barAA/foobarAAB"));
    assertNotNull(rootNode.getNode("fooA/barAA/foobarAAC"));
    assertNotNull(rootNode.getNode("fooA/barAB/foobarABA"));
    assertNotNull(rootNode.getNode("fooA/barAB/foobarABB"));
    assertNotNull(rootNode.getNode("fooA/barAB/foobarABC"));
    assertNotNull(rootNode.getNode("fooA/barAC/foobarACA"));
    assertNotNull(rootNode.getNode("fooA/barAC/foobarACB"));
    assertNotNull(rootNode.getNode("fooA/barAC/foobarACC"));

  }

  public void testSibling() throws Exception {

    assertNotNull(rootNode.getNode("fooC/barCA/sibling"));
    assertNotNull(rootNode.getNode("fooC/barCA/sibling[2]"));
    assertNotNull(rootNode.getNode("fooC/barCA/sibling[3]"));

  }

  public void testProperties() throws Exception {

    assertNotNull(rootNode.getNode("fooB/barBB").getProperty("nameBBA"));
    assertEquals("valueBBA", rootNode.getNode("fooB/barBB").getProperty("nameBBA").getString());

    assertNotNull(rootNode.getNode("fooB/barBB").getProperty("nameBBB"));
    assertEquals("valueBBB", rootNode.getNode("fooB/barBB").getProperty("nameBBB").getString());

    assertNotNull(rootNode.getNode("fooB/barBB").getProperty("nameBBC"));
    assertEquals("valueBBC", rootNode.getNode("fooB/barBB").getProperty("nameBBC").getString());

    assertNotNull(rootNode.getNode("fooA/barAB").getProperty("somewhere"));
    assertEquals("somewhereValue", rootNode.getNode("fooA/barAB").getProperty("somewhere").getString());
    
  }

  public void testNamespace() throws Exception {

    assertNotNull(rootNode.getNode("exo:foo"));
    assertEquals("value", rootNode.getNode("fooC").getProperty("exo:p").getString());

  }

  public void testRef() throws Exception {

    Property p = rootNode.getNode("fooC/barCB").getProperty("ref");
    String id = rootNode.getNode("fooA/barAB/foobarABC").getUUID();

    assertNotNull(p.getString());
    assertEquals(id, p.getString());

  }

  public void testMixin() throws Exception {

    NodeType[] mixins = rootNode.getNode("fooA/barAB/foobarABC").getMixinNodeTypes();

    assertEquals(1, mixins.length);
    assertEquals("mix:referenceable", mixins[0].getName());

  }

  public void testMultiple() throws Exception {

    Value[] values = rootNode.getNode("fooD").getProperty("multiple").getValues();

    assertEquals(5, values.length);
    assertEquals("a", values[0].getString());
    assertEquals("b", values[1].getString());
    assertEquals("c", values[2].getString());
    assertEquals("d", values[3].getString());
    assertEquals("e", values[4].getString());

  }
}

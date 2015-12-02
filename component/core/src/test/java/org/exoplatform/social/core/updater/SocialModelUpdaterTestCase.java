package org.exoplatform.social.core.updater;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.test.AbstractCoreTest;

import javax.jcr.Node;
import javax.jcr.nodetype.ConstraintViolationException;
import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SocialModelUpdaterTestCase extends AbstractCoreTest {

  public void testModelIS() throws Exception {
    assertNotNull(new SocialModelUpdaterPlugin(new InitParams()).getModelIS());
  }

  public void testUpgradeNodetype() throws Exception {
    assertEquals(false, session.getWorkspace().getNodeTypeManager().getNodeType("aTypeName").hasOrderableChildNodes());

    Node node = session.getRootNode().addNode("foo", "aTypeName");

    try {
      session.getRootNode().addNode("foo", "soc:isDisabled");
      fail("should fail");
    } catch (ConstraintViolationException expected) {
      // ok
    }

    new SocialModelUpdaterPlugin(new InitParams()) {
      public InputStream getModelIS() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/standalone/upgrade-nodetypes.xml");
      }
    }.processUpgrade(null, null);


    assertEquals(true, session.getWorkspace().getNodeTypeManager().getNodeType("soc:isDisabled").isMixin());
  }
}

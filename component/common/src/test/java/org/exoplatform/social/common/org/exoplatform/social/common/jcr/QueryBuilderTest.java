/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.org.exoplatform.social.common.jcr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.AbstractCommonTest;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.QueryBuilder;

/**
 * QueryBuilder unit test
 * @author zun
 * @since  Jun 15, 2010
 */
public class QueryBuilderTest extends AbstractCommonTest {
  private final Log LOG = ExoLogger.getLogger(QueryBuilderTest.class);
  PortalContainer portalContainer;
  RepositoryService repositoryService;
  JCRSessionManager sessionManager;
  ExtendedNodeTypeManager nodeTypeManager;
  private Session session;
  private final String WORKSPACE = "portal-test";

  @Override
  protected void beforeRunBare() throws Exception {
    super.beforeRunBare();

    portalContainer = PortalContainer.getInstance();
    repositoryService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
    sessionManager = new JCRSessionManager(WORKSPACE, repositoryService);

    Session session = sessionManager.getOrOpenSession();
    try {
      nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
      addNodeTypes();
    } finally {
      sessionManager.closeSession();
    }
  }

  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
    sessionManager.closeSession();
    Session session = sessionManager.getOrOpenSession();
    try {
      QueryManager queryManager = session.getWorkspace().getQueryManager();

      Query query = queryManager.createQuery("select * from exo:book", Query.SQL);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();

      while (nodeIterator.hasNext()){
        nodeIterator.nextNode().remove();
      }

      session.save();
    } finally {
      sessionManager.closeSession();
    }
  }

  private void addNodeTypes() throws RepositoryException {
    NodeTypeValue zTypeValue = new NodeTypeValue();
    List<String> superTypes = new ArrayList<String>();
    superTypes.add("nt:base");

    zTypeValue.setName("exo:book");
    zTypeValue.setPrimaryItemName("exo:primary");
    zTypeValue.setDeclaredSupertypeNames(superTypes);

    List<PropertyDefinitionValue> properties = new ArrayList<PropertyDefinitionValue>();
    properties.add( new PropertyDefinitionValue("title", false, true, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
    properties.add( new PropertyDefinitionValue("publisher", false, true, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
    properties.add( new PropertyDefinitionValue("date", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
    properties.add( new PropertyDefinitionValue("owner", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
    properties.add( new PropertyDefinitionValue("price", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
    properties.add( new PropertyDefinitionValue("isborrowed", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));

    zTypeValue.setDeclaredPropertyDefinitionValues(properties);

    nodeTypeManager.registerNodeType(zTypeValue, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
  }

  private void removeNodeTypes() throws RepositoryException {
    nodeTypeManager.unregisterNodeType("exo:book");
  }

  public void testInitSQLTest(){
    assertTrue(true);
    assertNotNull(portalContainer);
    assertNotNull(repositoryService);
    assertNotNull(sessionManager);
    assertNotNull(nodeTypeManager);
  }

  public void testQueryAll() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free");
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String querystr = queryBuilder.select("exo:book").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(querystr, "SELECT * FROM exo:book");
      assertEquals(total,results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryProperty() throws Exception {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free");
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String querystr = queryBuilder.select("exo:book", "title").getSQL();

      List<Value> results = queryBuilder.execProperty();

      assertEquals(querystr, "SELECT title FROM exo:book");
      assertEquals(total,results.size());
      for (int i = 0; i < total; i++) {
        String title = results.get(i).getString();
        assertEquals("zun"+ i, title);
      }      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryProperties() throws Exception {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free");
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String querystr = queryBuilder.select("exo:book", "title", "owner", "publisher").getSQL();

      List<Map<String, Value>> results = queryBuilder.execProperties();

      assertEquals(querystr, "SELECT title,owner,publisher FROM exo:book");
      assertEquals(total,results.size());
      for (int i = 0; i < total; i++) {
        String title = results.get(i).get("title").getString();
        String owner = results.get(i).get("owner").getString();
        String publisher = results.get(i).get("publisher").getString();
        assertEquals("zun"+ i, title);
        assertEquals("zunPub", publisher);
        assertEquals("Free", owner);
      }
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  public void testFindNode() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 10, false);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.select("exo:book").getSQL();

      Node result = queryBuilder.findNode();
      assertEquals(queryStr, "SELECT * FROM exo:book");
      assertNotNull(result);
    } catch (Exception e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByBooleanProperty() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 10, false);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.select("exo:book").equal("isborrowed",false).getSQL();

      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE isborrowed='false'");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }
                           
  public void testQueryByStringProperty() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free");

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.select("exo:book").equal("owner","Free").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE owner='Free'");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByNumberProperty() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 10, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.select("exo:book").equal("price",10).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price=10");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByANDConstraint() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 10, true);
    insertBook("zunbook", "MIT pub","everyone", 10, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book")
                              .equal("title", "zunbook")
                              .and()
                              .equal("publisher","zunPub").getSQL();

      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE title='zunbook' AND publisher='zunPub'");
      assertEquals(1, results.size());      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByORConstraint() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 10, true);
    insertBook("zunbook", "MIT pub","everyone", 10, true);
    insertBook("zunbook", "SUN pub","everyone", 10, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book")
                              .equal("publisher", "MIT pub")
                              .or()
                              .equal("publisher", "SUN pub").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE publisher='MIT pub' OR publisher='SUN pub'");
      assertEquals(2, results.size());      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByANDORConstraint() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 7, true);
    insertBook("zunbook", "MIT pub","everyone", 8, true);
    insertBook("zunbook", "SUN pub","everyone", 9, true);
    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book")
                              .group()
                                .equal("publisher", "MIT pub")
                                .or()
                                .equal("publisher", "SUN pub")
                              .endGroup()
                              .and()
                              .equal("price", 9).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE ( publisher='MIT pub' OR publisher='SUN pub' ) AND price=9");
      assertEquals(1, results.size());      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByNOTConstraint() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 7, true);
    insertBook("zunbook", "MIT pub","everyone", 8, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").not().equal("price", 7).getSQL();

      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE NOT price=7");
      assertEquals(1, results.size());      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByCONTAINSConstraint() throws RepositoryException {
    insertBook("zunbook", "zunPub","Free", 7, true);
    insertBook("zunbook", "MIT pub","everyone", 8, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").contains("publisher", "MIT").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE CONTAINS(publisher, 'MIT')");
      assertEquals(1, results.size());      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByLIKEConstraint() throws RepositoryException {
    insertBook("zunbook", "MIT","everyone", 8, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").like("owner", "%ery%").getSQL();

      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE owner LIKE '%ery%'");
      assertEquals(1, results.size());      
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByGreaterValueComparer() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zunbook", "MIT","no-one", i, true);
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").greater("price", 4).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price>4");
      assertEquals(5, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByGreaterOrEqualValueComparer() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zunbook", "MIT","no-one", i, true);
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").greaterEqual("price", 4).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price>=4");
      assertEquals(6, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByLessValueComparer() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zunbook", "MIT","no-one", i, true);
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").less("price", 5).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price<5");
      assertEquals(5, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByLessOrEqualValueComparer() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zunbook", "MIT","no-one", i, true);
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").lessEqual("price", 5).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price<=5");
      assertEquals(6, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByDiffirentiateValueComparer() throws RepositoryException {
    insertBook("zunbook1", "MIT1","no-one1", 8, true);
    insertBook("zunbook2", "MIT2","no-one2", 9, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").notEqual("price", 8).getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price<>8");
      assertEquals(1, results.size());

    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByLowercaseValueComparer() throws RepositoryException {
    insertBook("zunbook", "MIT","no-one", 8, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").equal(queryBuilder.lower("publisher"), "mit").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE LOWER(publisher)='mit'");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByUppercaseValueComparer() throws RepositoryException {
    insertBook("zunbook", "mit","no-one", 8, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").equal(queryBuilder.upper("publisher"), "MIT").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE UPPER(publisher)='MIT'");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByNullValueComparer() throws RepositoryException {
    insertBook("zunbook", "mit","no-one");
    insertBook("abc", "pub","my", 20, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").isNull("price").getSQL();
      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price IS NULL");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryNotNullValueComparer() throws RepositoryException {
    insertBook("zunbook", "mit","no-one");
    insertBook("abc", "pub","my", 20, true);

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.
                          select("exo:book").isNotNull("price").getSQL();

      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book WHERE price IS NOT NULL");
      assertEquals(1, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByOrderingDESC() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun", "zunpub","Free", i, false);
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder
                          .select("exo:book").orderBy("price", QueryBuilder.DESC).getSQL();
      List<Node> nodes = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book ORDER BY price DESC");
      int maxValue = 9;
      for (int i = 0; i < 10; i++) {
        assertEquals(maxValue--, nodes.get(i).getProperty("price").getLong());
      }
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryByOrderingASC() throws RepositoryException {
    int total = 10;
    for (int i = total-1; i >= 0; i--) {
      insertBook("zun", "zunpub","Free", i, false);
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder
                          .select("exo:book").orderBy("price", QueryBuilder.ASC).getSQL();
      List<Node> nodes = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book ORDER BY price ASC");

      for (int i = 0; i < 10; i++) {
        assertEquals(i, nodes.get(i).getProperty("price").getLong());
      }
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testQueryWithPaging() throws RepositoryException {
    int offset = 2;
    int count = 4;

    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free");
    }

    session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      String queryStr = queryBuilder.select("exo:book", offset, count).getSQL();

      queryBuilder.setOffset(offset);
      queryBuilder.setLimit(count);

      List<Node> results = queryBuilder.exec();

      assertEquals(queryStr, "SELECT * FROM exo:book");
      assertEquals(count, results.size());
    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      sessionManager.closeSession();
    }
  }

  private void insertBook(String title, String publisher,String owner ) throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();

      Node book = rootNode.addNode("book", "exo:book");
      book.setProperty("title", title);
      book.setProperty("publisher", publisher);
      book.setProperty("date", new Date().toString());
      book.setProperty("owner", owner);

      rootNode.save();
    } finally {
      sessionManager.closeSession();
    }
  }

  private void insertBook(String title, String publisher,String owner,int price,boolean isborrowed) throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();

      Node book = rootNode.addNode("book", "exo:book");
      book.setProperty("title", title);
      book.setProperty("publisher", publisher);
      book.setProperty("date", new Date().toString());
      book.setProperty("owner", owner);
      book.setProperty("price", price);
      book.setProperty("isborrowed", isborrowed);
      rootNode.save();
    } finally {
      sessionManager.closeSession();
    }
  }
} 

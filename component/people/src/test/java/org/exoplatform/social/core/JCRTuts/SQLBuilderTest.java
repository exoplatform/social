//package org.exoplatform.social.core.JCRTuts;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import javax.jcr.Node;
//import javax.jcr.NodeIterator;
//import javax.jcr.RepositoryException;
//import javax.jcr.Session;
//import javax.jcr.query.Query;
//import javax.jcr.query.QueryManager;
//import javax.jcr.query.QueryResult;
//
//import org.exoplatform.container.PortalContainer;
//import org.exoplatform.services.jcr.RepositoryService;
//import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
//import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
//import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
//import org.exoplatform.social.jcr.JCRSessionManager;
//import org.exoplatform.social.utils.QueryBuilder;
//
///**
// * Created by IntelliJ IDEA.
// * User: zun
// * Date: Jun 15, 2010
// * Time: 5:56:40 PM
// * To change this template use File | Settings | File Templates.
// */
//public class SQLBuilderTest extends ZAbstractTest{
//  PortalContainer portalContainer;
//  RepositoryService repositoryService;
//  JCRSessionManager sessionManager;
//  ExtendedNodeTypeManager nodeTypeManager;
//
//  private final String WORKSPACE = "portal-test";
//
//  @Override
//  protected void beforeRunBare() throws Exception {
//    super.beforeRunBare();
//
//    portalContainer = PortalContainer.getInstance();
//    repositoryService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
//    sessionManager = new JCRSessionManager(WORKSPACE, repositoryService);
//
//    Session session = sessionManager.getOrOpenSession();
//    try {
//      nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
//      addNodeTypes();
//    } finally {
//      sessionManager.closeSession();
//    }
//  }
//
//  @Override
//  protected void afterRunBare() {
//    super.afterRunBare();
//  }
//
//  @Override
//  protected void setUp() throws Exception {
//  }
//
//  @Override
//  protected void tearDown() throws Exception {
//    sessionManager.closeSession();
//    Session session = sessionManager.getOrOpenSession();
//    try {
//      QueryManager queryManager = session.getWorkspace().getQueryManager();
//
//      Query query = queryManager.createQuery("select * from exo:book", Query.SQL);
//      QueryResult result = query.execute();
//      NodeIterator nodeIterator = result.getNodes();
//
//      while (nodeIterator.hasNext()){
//        nodeIterator.nextNode().remove();
//      }
//
//      session.save();
//    } finally {
//      sessionManager.closeSession();
//    }
//  }
//
//  private void addNodeTypes() throws RepositoryException {
//    NodeTypeValue zTypeValue = new NodeTypeValue();
//    List<String> superTypes = new ArrayList<String>();
//    superTypes.add("nt:base");
//
//    zTypeValue.setName("exo:book");
//    zTypeValue.setPrimaryItemName("exo:primary");
//    zTypeValue.setDeclaredSupertypeNames(superTypes);
//
//    List<PropertyDefinitionValue> properties = new ArrayList<PropertyDefinitionValue>();
//    properties.add( new PropertyDefinitionValue("title", false, true, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
//    properties.add( new PropertyDefinitionValue("publisher", false, true, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
//    properties.add( new PropertyDefinitionValue("date", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
//    properties.add( new PropertyDefinitionValue("owner", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
//    properties.add( new PropertyDefinitionValue("price", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
//    properties.add( new PropertyDefinitionValue("isborrowed", false, false, 1, false, new ArrayList<String>(),false, 0, new ArrayList<String>() ));
//
//    zTypeValue.setDeclaredPropertyDefinitionValues(properties);
//
//    nodeTypeManager.registerNodeType(zTypeValue, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
//  }
//
//  private void removeNodeTypes() throws RepositoryException {
//    nodeTypeManager.unregisterNodeType("exo:book");
//  }
//
//  public void testInitSQLTest(){
//    assertTrue(true);
//    assertNotNull(portalContainer);
//    assertNotNull(repositoryService);
//    assertNotNull(sessionManager);
//    assertNotNull(nodeTypeManager);
//  }
//
//  public void test_query_all_via_query_builder() throws RepositoryException {
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zun"+i, "zunPub","Free");
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String querystr = queryBuilder.select("exo:book").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(querystr, "SELECT * FROM exo:book");
//    assertEquals(total,results.size());
//  }
//
//  public void test_query_by_boolean_property() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 10, false);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String queryStr = queryBuilder.select("exo:book").equal("isborrowed",false).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE isborrowed='false'");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_string_property() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free");
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String queryStr = queryBuilder.select("exo:book").equal("owner","Free").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE owner='Free'");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_number_property() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 10, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String queryStr = queryBuilder.select("exo:book").equal("price",10).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price=10");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_AND_constraint() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 10, true);
//    insertBook("zunbook", "MIT pub","everyone", 10, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book")
//                            .equal("title", "zunbook")
//                            .and()
//                            .equal("publisher","zunPub").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE title='zunbook' AND publisher='zunPub'");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_OR_constraint() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 10, true);
//    insertBook("zunbook", "MIT pub","everyone", 10, true);
//    insertBook("zunbook", "SUN pub","everyone", 10, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book")
//                            .equal("publisher", "MIT pub")
//                            .or()
//                            .equal("publisher", "SUN pub").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE publisher='MIT pub' OR publisher='SUN pub'");
//    assertEquals(2, results.size());
//  }
//
//  public void test_query_by_AND_OR_constraint() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 7, true);
//    insertBook("zunbook", "MIT pub","everyone", 8, true);
//    insertBook("zunbook", "SUN pub","everyone", 9, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book")
//                            .group()
//                              .equal("publisher", "MIT pub")
//                              .or()
//                              .equal("publisher", "SUN pub")
//                            .endGroup()
//                            .and()
//                            .equal("price", 9).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE ( publisher='MIT pub' OR publisher='SUN pub' ) AND price=9");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_NOT_constraint() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 7, true);
//    insertBook("zunbook", "MIT pub","everyone", 8, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").not().equal("price", 7).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE NOT price=7");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_CONTAINS_constraint() throws RepositoryException {
//    insertBook("zunbook", "zunPub","Free", 7, true);
//    insertBook("zunbook", "MIT pub","everyone", 8, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").contains("publisher", "MIT").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE CONTAINS(publisher, 'MIT')");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_LIKE_constraint() throws RepositoryException {
//    insertBook("zunbook", "MIT","e_v_e_r_y_o_n_e", 8, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").like("owner", "%_e_r_y_%").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE owner LIKE '%_e_r_y_%'");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_greater_value_comparer() throws RepositoryException {
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zunbook", "MIT","no-one", i, true);
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").greater("price", 4).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price>4");
//    assertEquals(5, results.size());
//  }
//
//  public void test_query_by_greater_or_equal_value_comparer() throws RepositoryException {
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zunbook", "MIT","no-one", i, true);
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").greaterEqual("price", 4).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price>=4");
//    assertEquals(6, results.size());
//  }
//
//  public void test_query_by_less_value_comparer() throws RepositoryException {
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zunbook", "MIT","no-one", i, true);
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").less("price", 5).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price<5");
//    assertEquals(5, results.size());
//  }
//
//  public void test_query_by_less_or_equal_value_comparer() throws RepositoryException {
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zunbook", "MIT","no-one", i, true);
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").lessEqual("price", 5).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price<=5");
//    assertEquals(6, results.size());
//  }
//
//  public void test_query_by_diffirentiate_value_comparer() throws RepositoryException {
//    insertBook("zunbook1", "MIT1","no-one1", 8, true);
//    insertBook("zunbook2", "MIT2","no-one2", 9, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").notEqual("price", 8).getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price<>8");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_lowercase_value_comparer() throws RepositoryException {
//    insertBook("zunbook", "MIT","no-one", 8, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").equal(queryBuilder.lower("publisher"), "mit").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE LOWER(publisher)='mit'");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_uppercase_value_comparer() throws RepositoryException {
//    insertBook("zunbook", "mit","no-one", 8, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").equal(queryBuilder.upper("publisher"), "MIT").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE UPPER(publisher)='MIT'");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_null_value_comparer() throws RepositoryException {
//    insertBook("zunbook", "mit","no-one");
//    insertBook("abc", "pub","my", 20, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").isNull("price").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price IS NULL");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_not_null_value_comparer() throws RepositoryException {
//    insertBook("zunbook", "mit","no-one");
//    insertBook("abc", "pub","my", 20, true);
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//
//    String queryStr = queryBuilder.
//                        select("exo:book").isNotNull("price").getSQL();
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book WHERE price IS NOT NULL");
//    assertEquals(1, results.size());
//  }
//
//  public void test_query_by_ordering_desc() throws RepositoryException {
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zun", "zunpub","Free", i, false);
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String queryStr = queryBuilder
//                        .select("exo:book").orderBy("price", QueryBuilder.DESC).getSQL();
//
//    List<Node> nodes = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book ORDER BY price DESC");
//    int maxValue = 9;
//    for (int i = 0; i < 10; i++) {
//      assertEquals(maxValue--, nodes.get(i).getProperty("price").getLong());
//    }
//  }
//
//  public void test_query_by_ordering_asc() throws RepositoryException {
//    int total = 10;
//    for (int i = total-1; i >= 0; i--) {
//      insertBook("zun", "zunpub","Free", i, false);
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String queryStr = queryBuilder
//                        .select("exo:book").orderBy("price", QueryBuilder.ASC).getSQL();
//
//    List<Node> nodes = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book ORDER BY price ASC");
//
//    for (int i = 0; i < 10; i++) {
//      assertEquals(i, nodes.get(i).getProperty("price").getLong());
//    }
//  }
//
//  public void test_query_with_paging() throws RepositoryException {
//    int offset = 2;
//    int count = 4;
//
//    int total = 10;
//    for (int i = 0; i < total; i++) {
//      insertBook("zun"+i, "zunPub","Free");
//    }
//
//    QueryBuilder queryBuilder = new QueryBuilder(sessionManager);
//    String queryStr = queryBuilder.select("exo:book", offset, count).getSQL();
//
//    queryBuilder.setOffset(offset);
//    queryBuilder.setLimit(count);
//
//    List<Node> results = queryBuilder.exec();
//
//    assertEquals(queryStr, "SELECT * FROM exo:book");
//    assertEquals(count, results.size());
//  }
//
//  private void insertBook(String title, String publisher,String owner ) throws RepositoryException {
//    Session session = sessionManager.getOrOpenSession();
//    try {
//      Node rootNode = session.getRootNode();
//
//      Node book = rootNode.addNode("book", "exo:book");
//      book.setProperty("title", title);
//      book.setProperty("publisher", publisher);
//      book.setProperty("date", new Date().toString());
//      book.setProperty("owner", owner);
//
//      rootNode.save();
//    } finally {
//      sessionManager.closeSession();
//    }
//  }
//
//  private void insertBook(String title, String publisher,String owner,int price,boolean isborrowed) throws RepositoryException {
//    Session session = sessionManager.getOrOpenSession();
//    try {
//      Node rootNode = session.getRootNode();
//
//      Node book = rootNode.addNode("book", "exo:book");
//      book.setProperty("title", title);
//      book.setProperty("publisher", publisher);
//      book.setProperty("date", new Date().toString());
//      book.setProperty("owner", owner);
//      book.setProperty("price", price);
//      book.setProperty("isborrowed", isborrowed);
//      rootNode.save();
//    } finally {
//      sessionManager.closeSession();
//    }
//  }
//}
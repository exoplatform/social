package org.exoplatform.social.core.JCRTuts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.social.jcr.JCRSessionManager;

public class SQLTest extends ZAbstractTest {
  PortalContainer portalContainer;
  RepositoryService repositoryService;
  JCRSessionManager sessionManager;
  ExtendedNodeTypeManager nodeTypeManager;

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
    try {
      removeNodeTypes();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    portalContainer = null;
    repositoryService = null;
    sessionManager = null;
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();
      NodeIterator iterator = rootNode.getNodes();
      while (iterator.hasNext()){
        Node node = iterator.nextNode();
        node.remove();
      }
      rootNode.save();
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

  public void test_add_nt_base_node() throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();
      rootNode.addNode("book");
      rootNode.save();
    } finally {
      sessionManager.closeSession();
    }

    session = sessionManager.getOrOpenSession();
    NodeIterator nodeIterator = session.getRootNode().getNodes();
    List<Node> nodes = new ArrayList<Node>();
    while (nodeIterator.hasNext()){
      nodes.add(nodeIterator.nextNode());
    }

    assertEquals(1, nodes.size());
  }

  public void test_add_many_nt_base_nodes() throws RepositoryException {
    int total = 5;
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();
      for (int i = 0; i < total; i++) {
        rootNode.addNode("book");
      }
      rootNode.save();
    } finally {
      sessionManager.closeSession();
    }

    session = sessionManager.getOrOpenSession();
    NodeIterator nodeIterator = session.getRootNode().getNodes();
    List<Node> nodes = new ArrayList<Node>();
    while (nodeIterator.hasNext()){
      nodes.add(nodeIterator.nextNode());
    }

    assertEquals(total, nodes.size());
  }

  public void test_query_nt_base_node() throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();
      rootNode.addNode("book");
      rootNode.save();
    } finally {
      sessionManager.closeSession();
    }

    String queryStr = "select * from nt:base";
    List<Node> nodes = queryResults(queryStr);
    
    //we plus 1 to the total because the root node is always exist and is nt:base type
    assertEquals(1 + 1, nodes.size());
  }

  public void test_cannot_query_unregistered_node_type() throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    NoSuchNodeTypeException exception = null;
    try {
      Node rootNode = session.getRootNode();
      rootNode.addNode("book","exo:unregistered-type");
      rootNode.save();
    }
    catch(NoSuchNodeTypeException e){
      exception = e;
    } finally {
      sessionManager.closeSession();
    }

    assertNotNull(exception);
  }

  public void test_query_many_nt_base_nodes() throws RepositoryException {
    int total = 5 ;
    Session session = sessionManager.getOrOpenSession();
    try {
      Node rootNode = session.getRootNode();
      for (int i = 0; i < total; i++) {
        rootNode.addNode("book");
      }
      rootNode.save();
    } finally {
      sessionManager.closeSession();
    }

    String queryStr = "select * from nt:base";
    List<Node> nodes = queryResults(queryStr);

    //we plus 1 to the total because the root node is always exist and is nt:base type
    assertEquals(total + 1, nodes.size());
  }

  public void test_add_node_with_mandatory_properties() throws RepositoryException {
    insertBook("sicp", "MIT", "everyone");

    String queryStr = "select * from exo:book";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_add_node_without_mandatory_properties() throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    Exception exception = null;
    try {
      Node rootNode = session.getRootNode();
      rootNode.addNode("book", "exo:book");
      rootNode.save();
    }
    catch (Exception e){
      exception = e;
    }
    finally {
      sessionManager.closeSession();
    }

    assertNotNull(exception);
  }

  public void test_add_many_nodes() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free");
    }

    String queryStr = "select * from exo:book";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(total, nodes.size());
  }

  public void test_query_node_by_string_property() throws RepositoryException {
    String title = "sicp";
    insertBook(title, "MIT", "everyone");

    Session session = sessionManager.getOrOpenSession();

    String queryStr = "select * from exo:book where title='"+title+"'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_int_property() throws RepositoryException {
    int price = 10;
    insertBook("sicp", "MIT", "everyone", price, false);

    Session session = sessionManager.getOrOpenSession();

    String queryStr = "select * from exo:book where price="+price;
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_boolean_property() throws RepositoryException {
    boolean isborrowed = false;
    insertBook("sicp", "MIT", "everyone", 10, isborrowed);

    String value = String.valueOf(isborrowed);
    String queryStr = "select * from exo:book where isborrowed='"+ value + "'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_greater_number_value_compare() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free", i, false);
    }

    String queryStr = "select * from exo:book where price>4";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(total/2, nodes.size());
  }

  public void test_query_node_by_not_null_constraint() throws RepositoryException {
    insertBook("zun", "zunPub","Free",10, false);
    insertBook("zun", "zunPub","Free");

    String queryStr = "select * from exo:book where price is not null";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_lowercase_string_compare() throws RepositoryException {
    insertBook("zun", "ZUNPUB","Free",10, false);

    String queryStr = "select * from exo:book where lower(publisher)='zunpub'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_uppercase_string_compare() throws RepositoryException {
    insertBook("zun", "zunpub","Free",10, false);

    String queryStr = "select * from exo:book where upper(publisher)='ZUNPUB'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_ordering_desc() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun", "zunpub","Free", i, false);   
    }

    String queryStr = "select * from exo:book order by price desc";
    List<Node> nodes = queryResults(queryStr);

    int maxValue = 9;
    for (int i = 0; i < 10; i++) {
      assertEquals(maxValue--, nodes.get(i).getProperty("price").getLong());
    }
  }

  public void test_query_node_by_ordering_asc() throws RepositoryException {
    int total = 10;
    for (int i = total-1; i >= 0; i--) {
      insertBook("zun", "zunpub","Free", i, false);
    }

    String queryStr = "select * from exo:book order by price asc";
    List<Node> nodes = queryResults(queryStr);

    for (int i = 0; i < 10; i++) {
      assertEquals(i, nodes.get(i).getProperty("price").getLong());
    }
  }
  
  public void test_query_node_by_greater_and_equal_number_value_compare() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free", i, false);
    }

    String queryStr = "select * from exo:book where price>=4";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(total/2 + 1, nodes.size());

  }

  public void test_query_node_by_less_number_value_compare() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free", i, false);
    }

    String queryStr = "select * from exo:book where price<5";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(total/2, nodes.size());
  }

  public void test_query_node_by_less_and_equal_number_value_compare() throws RepositoryException {
    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free", i, false);
    }

    String queryStr = "select * from exo:book where price<=5";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(total/2 + 1, nodes.size());
  }

  public void test_query_node_by_diffirent__value_compare() throws RepositoryException {
    insertBook("sicp", "MIT", "everyone", 10, false);
    insertBook("sicp", "MIT", "everyone", 5, false);

    String queryStr = "select * from exo:book where price<>"+ 5;
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_and_constraint() throws RepositoryException {
    insertBook("zunbook", "MIT", "everyone");
    insertBook("the final game", "MIT", "zun");

    String queryStr = "select * from exo:book where title='zunbook' and publisher='MIT'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_or_constraint() throws RepositoryException {
    insertBook("zunbook", "MIT", "everyone");
    insertBook("the final game", "NXB", "zun");

    String queryStr = "select * from exo:book where title='zunbook' or publisher='NXB'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(2, nodes.size());
  }

  public void test_query_columnm_of_node() throws RepositoryException {
    String publisher = "MIT";
    insertBook("zunbook", publisher, "everyone");

    String queryStr = "select publisher from exo:book";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());

    //NOTE : inspite of querying only one column, we get the full node with all its properties
    Node node = nodes.get(0);
    assertEquals(publisher, node.getProperty("publisher").getString());
    assertNotNull(node.getProperty("title"));
    assertNotNull(node.getProperty("owner"));

    //check null property
    Exception exception= null;
    try {
      long price = node.getProperty("price").getLong();
    } catch (RepositoryException e) {
      //this exception happened because the property PRICE is null, was not initialized.
      exception = e;
    }

    assertNotNull(exception);
  }

  public void test_query_node_by_contains_constraint() throws RepositoryException {
    insertBook("zunbook", "MIT", "me you and everyone");

    String queryStr = "select * from exo:book where contains(owner, 'you')";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_by_like_constraint() throws RepositoryException {
    insertBook("zunbook", "MIT", "e_v_e_r_y_o_n_e");

    String queryStr = "select * from exo:book where owner like '%e_r_y_o%'";
    List<Node> nodes = queryResults(queryStr);

    assertEquals(1, nodes.size());
  }

  public void test_query_node_with_limit() throws RepositoryException {
    int offset = 2;
    int count = 4;

    int total = 10;
    for (int i = 0; i < total; i++) {
      insertBook("zun"+i, "zunPub","Free");
    }

    String queryStr = "select * from exo:book";

    Session session = sessionManager.getOrOpenSession();
    QueryManager queryManager = session.getWorkspace().getQueryManager();

    QueryImpl query = (QueryImpl) queryManager.createQuery(queryStr, Query.SQL);
     query.setOffset(offset);
    query.setLimit(count);
    QueryResult result = query.execute();
    NodeIterator nodeIterator = result.getNodes();
    List<Node> nodes = new ArrayList<Node>();

    while (nodeIterator.hasNext()){
      nodes.add(nodeIterator.nextNode());
    }

    assertEquals(count, nodes.size());
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

  private List<Node> queryResults(String queryStr) throws RepositoryException {
    Session session = sessionManager.getOrOpenSession();
    QueryManager queryManager = session.getWorkspace().getQueryManager();

    Query query = queryManager.createQuery(queryStr, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator nodeIterator = result.getNodes();
    List<Node> nodes = new ArrayList<Node>();

    while (nodeIterator.hasNext()){
      nodes.add(nodeIterator.nextNode());
    }
    return nodes;
  }
}
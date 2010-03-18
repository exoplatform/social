package org.exoplatform.social.core.test;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;


/**
 * Created by The eXo Platform SAS
 * Author : Tung Dang
 *          tungcnw@gmail.com 					
 * Mar 01, 2010  
 */
@ConfiguredBy({
	  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
	  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration.xml"),
	  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration1.xml"),
	  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.people.test-configuration.xml"),
	  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.people.portal-configuration.xml")
	})
public abstract class BasicPeopleServiceTestCase extends AbstractKernelTest {
 
  protected static Log          log = ExoLogger.getLogger("sample.services.test");

  protected static RepositoryService   repositoryService;
  protected static PortalContainer container;
  
  protected final static String REPO_NAME = "repository".intern();
  protected final static String SYSTEM_WS = "system".intern();
  protected final static String SOCIAL_WS = "portal-test".intern();
  protected static Node root_ = null;
  protected SessionProvider sessionProvider;
  private static SessionProviderService sessionProviderService = null;
  
  protected static ChromatticManager chromatticManager;

  
  public BasicPeopleServiceTestCase() throws Exception {
  }
  
  public void setUp() throws Exception {
    initContainer();
    initJCR();
    startSystemSession();
    begin();
  }
  
  protected void begin() {
    super.begin();
  }
  
  protected void end() {
    super.end();
  }
  
  public void tearDown() throws Exception {
    chromatticManager.getSynchronization().setSaveOnClose(false);
    end();
  }
  protected void startSystemSession() {
    sessionProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sessionProvider = sessionProviderService.getSessionProvider(null);
  }
  protected void endSession() {
    sessionProviderService.removeSessionProvider(null);
    startSystemSession();
  }
  
  
  /**
   * All elements of a list should be contained in the expected array of String
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    assertEquals(message, expected.size(), actual.size());
    assertTrue(message,expected.containsAll(actual));
  } 
  
  /**
   * Assertion method on string arrays
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String []expected, String []actual) {
    assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(message, expected[i], actual[i]);
    }
  }
  private static void initContainer() {
    try {
      container = PortalContainer.getInstance();
      
      chromatticManager = (ChromatticManager)container.getComponentInstanceOfType(ChromatticManager.class);
      
      if (System.getProperty("java.security.auth.login.config") == null)
          System.setProperty("java.security.auth.login.config", "src/test/java/conf/standalone/login.conf");
      }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(),e);
    }
  }

  private static void initJCR() {
    try {
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    
    // Initialize datas
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(SOCIAL_WS);
    root_ = session.getRootNode();   
    
    sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;   
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: " + e.getMessage(),e);
    }
  }
}
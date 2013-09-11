package org.exoplatform.social.core.updater;

import java.io.InputStream;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ModelOverriderUpdaterPlugin extends UpgradeProductPlugin {

  private static final Log LOG = ExoLogger.getLogger(ModelOverriderUpdaterPlugin.class);

  public ModelOverriderUpdaterPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    //
    PortalContainer portalContainer = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) portalContainer.getComponentInstanceOfType(ChromatticManager.class);
    SocialChromatticLifeCycle lifeCycle = (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    
    RepositoryService repositoryService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
    Session session = null;
    try {
      ManageableRepository repository = repositoryService.getCurrentRepository();
      SessionProviderService sessionProviderService = (SessionProviderService) portalContainer.getComponentInstanceOfType(SessionProviderService.class);
      
      SessionProvider sProvider = sessionProviderService.getSystemSessionProvider(null);
      session = sProvider.getSession(lifeCycle.getWorkspaceName(), repository);
      

      ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
      nodeTypeManager.registerNodeTypes(getModelIS(), ExtendedNodeTypeManager.REPLACE_IF_EXISTS, "text/xml");
      session.save();

    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      if (session != null && session.isLive()) {
        session.logout();
      }
    }

  }

  public InputStream getModelIS() {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/portal/chromattic-nodetypes.xml");
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isAfter(newVersion, previousVersion);
  }

}

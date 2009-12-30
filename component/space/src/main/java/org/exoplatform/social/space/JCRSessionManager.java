/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.space;

import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 16, 2009  
 */
public class JCRSessionManager {

  /** . */
  private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
  
  
  String workspaceName = "portal-system";
  String repositoryName = "repository";
  
 
  
  public JCRSessionManager(String repository, String workspace) {
    this.workspaceName = workspace;
    this.repositoryName = repository;
  }
  
  public String getWorkspaceName() {
    return workspaceName;
  }

  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  /**
   * <p>Returns the session currently associated with the current thread of execution.<br/>
   * The current session is set with {@link #openSession()} </p>
   *
   * @return the current session if exists, null otherwhise
   */
  public static Session getCurrentSession()
  {
     return currentSession.get();
  }
  
  public Session getSession(SessionProvider sessionProvider) {
    Session session = null;
    try {
     PortalContainer currentContainer = PortalContainer.getInstance(); 
     RepositoryService repositoryService = (RepositoryService) currentContainer.getComponentInstanceOfType(RepositoryService.class);
     ManageableRepository repository = repositoryService.getRepository(repositoryName);
     session = sessionProvider.getSession(workspaceName, repository);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return session;
  }

  /**
   * <p>Open and returns a session to the model. When the current thread is already associated with a previously
   * opened session the method will throw an <tt>IllegalStateException</tt>.</p>
   *
   * @return a session to the model.
   */
  public Session openSession()
  {
     Session session = currentSession.get();
     if (session == null)
     {
        //session = new POMSession(this);
       session = createSession();
       currentSession.set(session);
     }
     else
     { 
       throw new IllegalStateException("A session is already opened.");
     }
     return session;
  }

  private Session createSession() {
    Session session = null;
    try {

     PortalContainer currentContainer = PortalContainer.getInstance();
      
     RepositoryService repositoryService = (RepositoryService) currentContainer.getComponentInstanceOfType(RepositoryService.class);
     ManageableRepository repository = repositoryService.getRepository(repositoryName);
     session = repository.getSystemSession(workspaceName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return session;
  }

  /**
   * <p>Closes the current session and discard the changes done during the session.</p>
   *
   * @return a boolean indicating if the session was closed
   * @see #closeSession(boolean)
   */
  public boolean closeSession()
  {
     return closeSession(false);
  }

  /**
   * <p>Closes the current session and optionally saves its content. If no session is associated
   * then this method has no effects and returns false.</p>
   *
   * @param save if the session must be saved
   * @return a boolean indicating if the session was closed
   */
  public boolean closeSession(boolean save)
  {
     Session session = currentSession.get();
     if (session == null)
     {
        // Should warn
        return false;
     }
     else
     {
       currentSession.set(null);
        try
        {
           if (save)
           {
              session.save();
           }
        }
        catch(Exception e)
        {
        return false;
        }
        finally
        {
           session.logout();
        }
        return true;
     }
  }
  
}
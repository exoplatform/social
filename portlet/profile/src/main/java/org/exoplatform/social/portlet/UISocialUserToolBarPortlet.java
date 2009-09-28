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
package org.exoplatform.social.portlet;


import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;


/**
 * Created by The eXo Platform SAS
 * Author : @gmail.com
 * Sep 21, 2009  
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class,
                 template = "app:/groovy/portal/webui/component/UISocialUserToolBarPortlet.gtmpl",
                 events = {
                	 @EventConfig(name = "AddDefaultDashboard", listeners = UISocialUserToolBarPortlet.AddDashboardActionListener.class)
                 }
)
public class UISocialUserToolBarPortlet extends UIPortletApplication {
  
  public UISocialUserToolBarPortlet() throws Exception {  }
  
  private SpaceService spaceService = null;
  private String userId = null;
  
  public List<String> getAllPortalNames() throws Exception {
    List<String> list = new ArrayList<String>();
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class) ;
    PageList pageList = dataStorage.find(query) ;
    UserACL userACL = getApplicationComponent(UserACL.class) ;
    List<PortalConfig> configs = pageList.getAll();    
    for(PortalConfig ele : configs) {
      if(userACL.hasPermission(ele)) {
        list.add(ele.getName());                
      }
    }         
    return list;
  }

  public List<PageNavigation> getGroupNavigations() throws Exception {    
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
    List<PageNavigation> allNavigations = Util.getUIPortal().getNavigations();
    List<PageNavigation> navigations = new ArrayList<PageNavigation>();
    for (PageNavigation navigation : allNavigations) {      
      if (navigation.getOwnerType().equals(PortalConfig.GROUP_TYPE)) {
        navigations.add(PageNavigationUtils.filter(navigation, remoteUser));
      }
    }
    return navigations;
  }

  public String getCurrentPortal() {
    return Util.getUIPortal().getName();
  }
  
  public PageNavigation getCurrentPortalNavigation() throws Exception {
    PageNavigation navi = getPageNavigation(PortalConfig.PORTAL_TYPE + "::" + Util.getUIPortal().getName());
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
    return PageNavigationUtils.filter(navi, remoteUser); 
  }
  
  public String getPortalURI(String portalName) {
    return Util.getPortalRequestContext().getPortalURI().replace(getCurrentPortal(), portalName);
  }
  
  private PageNavigation getPageNavigation(String owner){
    List<PageNavigation> allNavigations = Util.getUIPortal().getNavigations();
    for(PageNavigation nav: allNavigations){
      if(nav.getOwner().equals(owner)) return nav;
    }
    return null;
  }
  
  public PageNavigation getCurrentUserNavigation(){
  	String remoteUser = Util.getPortalRequestContext().getRemoteUser();
  	return getPageNavigation(PortalConfig.USER_TYPE + "::" + remoteUser);
  }
  
  @SuppressWarnings("unused")
  private List<Space> getAllUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getAccessibleSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }
  
  private SpaceService getSpaceService() {
    if(spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService; 
  }
  
  /**
   * Get remote user Id
   * @return userId
   */
  private String getUserId() {
    if(userId == null) 
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  
  static public class AddDashboardActionListener extends EventListener<UISocialUserToolBarPortlet>{
  	
  	private final static String PAGE_TEMPLATE = "dashboard";
    private static Log logger = ExoLogger.getExoLogger(AddDashboardActionListener.class);
    
  	public void execute(Event<UISocialUserToolBarPortlet> event) throws Exception {
  		UISocialUserToolBarPortlet toolBarPortlet = event.getSource();
  		String nodeName = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
  	  PageNavigation userNavigation = toolBarPortlet.getCurrentUserNavigation();
  	  UserPortalConfigService configService = toolBarPortlet.getApplicationComponent(UserPortalConfigService.class);
  	  if(userNavigation != null && configService !=null && userNavigation.getNodes().size() < 1){
  	  	createDashboard(nodeName,userNavigation,configService);
  	  }
  	}
  	
  	private static void createDashboard(String _nodeName, PageNavigation _pageNavigation, UserPortalConfigService _configService){
  		try{
  			if(_nodeName == null){
  				logger.debug("Parsed nodeName is null, hence use Tab_0 as default name");
  				_nodeName = "Tab_0";
  			}
  			Page page = _configService.createPageTemplate(PAGE_TEMPLATE, _pageNavigation.getOwnerType(), _pageNavigation.getOwnerId());
  			page.setTitle(_nodeName);
  			page.setName(_nodeName);
  			
  			PageNode pageNode = new PageNode();
  			pageNode.setName(_nodeName);
  			pageNode.setLabel(_nodeName);
  	    pageNode.setUri(_nodeName);
  			pageNode.setPageReference(page.getPageId());
  				
  			_pageNavigation.addNode(pageNode);
  			_configService.create(page);
  			_configService.update(_pageNavigation);
  			
  			UIPortal uiPortal = Util.getUIPortal();
  			uiPortal.setSelectedNode(pageNode);
  			
  			PortalRequestContext prContext = Util.getPortalRequestContext();
  			prContext.getResponse().sendRedirect(prContext.getPortalURI() + _nodeName);
  		}catch(Exception ex){
  			logger.info("Could not create default dashboard page",ex);
  		}
  	}
  }
}
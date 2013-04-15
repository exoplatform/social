package org.exoplatform.social.extras.migration.plugin;

import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceUtils;

public class PreferenceRemoval extends UpgradeProductPlugin {

	private final PortalContainer container;
	private final OrganizationService service;
	private static final Log LOG = ExoLogger.getLogger(PreferenceRemoval.class);

	public PreferenceRemoval(InitParams initParams) {
		super(initParams);
		this.container = PortalContainer.getInstance();
		this.service = (OrganizationService) container
				.getComponentInstanceOfType(OrganizationService.class);
	}

	public void processUpgrade(String oldVersion, String newVersion) {
		try {
			RequestLifeCycle.begin(PortalContainer.getInstance());

			GroupHandler groupHandler = service.getGroupHandler();
			Group spaces = groupHandler.findGroupById("/spaces");
			Collection<Group> groups = groupHandler.findGroups(spaces);

			for (Group group : groups) {
				String groupId;
				groupId = group.getId();
				Query query = new Query<Page>("group", groupId, null, null, Page.class);
				DataStorage dataStorage = SpaceUtils.getDataStorage();
				List<Page> pages = dataStorage.find(query).getAll();

				for (Page page : pages) {       	
					String prettyName = groupId.substring(groupId.lastIndexOf("/")+1);
					List<ModelObject> childrens =page.getChildren();
					for (ModelObject child : childrens) {
						removePortletPreferenceHelper((Container)child,dataStorage,prettyName);
					}
					dataStorage.save(page);
				}
			}

		}
		catch (Exception e) {
			LOG.info("Error during template migration : " + e.getMessage(), e);
		}
		finally {
			RequestLifeCycle.end();
		}
	}

	private void removePortletPreferenceHelper(Container container,
			DataStorage dataStorage, String prettyName) {
		if (container != null && !container.getChildren().isEmpty()) {
			List<ModelObject> childrens = container.getChildren();
			for (ModelObject child : childrens) {
				if (child instanceof Application
						&& ((Application) child).getType() == ApplicationType.PORTLET) {
					ApplicationState<Portlet> state = ((org.exoplatform.portal.config.model.Application<Portlet>) child)
							.getState();
					Portlet portletPreference = null;
					try {
						portletPreference = dataStorage
								.load(state, ApplicationType.PORTLET);
						if (portletPreference != null) {
							portletPreference.setValue(SpaceUtils.SPACE_URL, prettyName);
							portletPreference.setReadOnly(SpaceUtils.SPACE_URL, true);
						}
						dataStorage.save(state, portletPreference);
					} catch (Exception e) {
					}
				} else if (child instanceof Container) {
					removePortletPreferenceHelper((Container) child, dataStorage,
							prettyName);
				}
			}
		}
	}

	public boolean shouldProceedToUpgrade(String previousVersion,
			String newVersion) {
		return true;
	}

}

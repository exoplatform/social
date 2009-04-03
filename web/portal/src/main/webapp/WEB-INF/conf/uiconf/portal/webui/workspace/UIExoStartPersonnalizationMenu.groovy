/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.util.ArrayList;
import org.exoplatform.portal.webui.workspace.UIExoStart ;

ArrayList menus = new ArrayList(4) ;
UIExoStart.MenuItemContainer menu = new UIExoStart.MenuItemContainer("Administration").
  add(new UIExoStart.MenuItemContainer("Basic").
      add(new UIExoStart.MenuItemAction("PageCreationWizard", "PageCreationWizardIcon","PageCreationWizard", true)).
      add(new UIExoStart.MenuItemAction("EditCurrentPage","EditCurrentPageIcon","EditCurrentPage", true))).
      
  add(new UIExoStart.MenuItemContainer("Advanced").
      add(new UIExoStart.MenuItemAction("EditPage","EditNavigationIcon", "EditPage", true)).
      add(new UIExoStart.MenuItemAction("EditPortal", "EditCurrentPortalIcon", "EditPortal", true)).
      add(new UIExoStart.MenuItemAction("BrowsePage", "BrowsePagesIcon", "BrowsePage", true)).
      add(new UIExoStart.MenuItemAction("BrowsePortal", "BrowsePortalsIcon", "BrowsePortal", true))).  
      
  add(new UIExoStart.MenuItemAction("SkinSettings", "SkinSettingIcon", "SkinSettings", true)).
  add(new UIExoStart.MenuItemAction("LanguageSettings", "LanguageSettingIcon", "LanguageSettings", true)).
  add(new UIExoStart.MenuItemAction("ChangePortal", "ChangePortalIcon", "ChangePortal", true)).
  add(new UIExoStart.MenuItemAction("AccountSettings", "AccountSettingIcon", "AccountSettings", true)) ;
 	
menus.add(menu) ;
return menus ;

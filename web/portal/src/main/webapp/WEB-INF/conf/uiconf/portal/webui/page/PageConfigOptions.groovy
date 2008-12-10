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
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.application.WebuiRequestContext;
import java.util.ResourceBundle;

List categories = new ArrayList(); 
WebuiRequestContext contextres = WebuiRequestContext.getCurrentInstance();
ResourceBundle res = contextres.getApplicationResourceBundle();
    
SelectItemCategory normalPageConfigs = new SelectItemCategory("normalPageConfigs") ;
categories.add(normalPageConfigs);
normalPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.normalPage.EmptyLayout"), "empty", "EmptyLayout"));
normalPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.normalPage.DesktopImage"), "desktop", "DesktopImage"));

SelectItemCategory columnPageConfigs = new SelectItemCategory("columnPageConfigs") ;
categories.add(columnPageConfigs);  
columnPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.columnPage.TwoColumnsLayout"), "two-columns", "TwoColumnsLayout"));
columnPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.columnPage.ThreeColumnsLayout"), "three-columns", "ThreeColumnsLayout"));

SelectItemCategory rowPageConfigs = new SelectItemCategory("rowPageConfigs") ;
categories.add(rowPageConfigs); 
rowPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.rowPage.TwoRowsLayout"), "two-rows", "TwoRowsLayout"));
rowPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.rowPage.ThreeRowsLayout"), "three-rows", "ThreeRowsLayout"));

SelectItemCategory tabsPageConfigs = new SelectItemCategory("tabsPageConfigs") ;
categories.add(tabsPageConfigs) ;
tabsPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.tabsPage.TwoTabsLayout"), "two-tabs", "TwoTabsLayout")) ;
tabsPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.tabsPage.ThreeTabsLayout"), "three-tabs", "ThreeTabsLayout")) ;

SelectItemCategory mixPageConfigs = new SelectItemCategory("mixPageConfigs") ;
categories.add(mixPageConfigs); 
mixPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.mixPage.TwoColumnsOneRowLayout"), "two-columns-one-row", "TwoColumnsOneRowLayout"));
mixPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.mixPage.OneRowTwoColumnsLayout"), "one-row-two-columns", "OneRowTwoColumnsLayout"));
mixPageConfigs.addSelectItemOption(new SelectItemOption(res.getString("UIWizardPageSelectLayoutForm.label.mixPage.ThreeRowsTwoColumnsLayout"), "three-rows-two-columns", "ThreeRowsTwoColumnsLayout"));

return categories;


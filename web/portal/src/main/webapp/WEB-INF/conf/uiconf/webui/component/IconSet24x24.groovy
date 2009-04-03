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
import org.exoplatform.webui.form.UIFormInputIconSelector.IconSet ;
import org.exoplatform.webui.form.UIFormInputIconSelector.IconCategory;
import org.exoplatform.webui.form.UIFormInputIconSelector.CategoryIconSet ;
import org.exoplatform.webui.form.UIFormInputIconSelector.CategoryIcon ;

CategoryIcon categorySet = new CategoryIcon("misc","24x24");

IconSet misc = 
  new IconSet("misc").
  addCategories(
      new IconCategory("Show").
      addIcon("BoxBlueArrowDown").addIcon("BoxGreenPlus").    
      addIcon("BoxPencil").addIcon("DoubleWindowsUpArrow").
      addIcon("PointedHandBrownBox").addIcon("HammerDataBox").
      addIcon("MiscColorBevelRetangles").addIcon("PageGreenArrowDown").
      addIcon("PageGreenArrowUp").addIcon("BlackMagnifier").
      addIcon("GreenPlusBluePage").addIcon("GreenPlusBlueTicket").
      addIcon("GreenPlusFolder").addIcon("Paste").
      addIcon("GreenUpArrowDisk").addIcon("BlueBallMultiplePage").
      addIcon("GraySwitcher").addIcon("MiscColorRegtangleRing").
      addIcon("SmallBallColorPlate").addIcon("SmallBallFlyingPages").
      addIcon("SmallBallRedShield")        
  );

IconSet office = 
  new IconSet("offices").
  addCategories(
      new IconCategory("Show").
      addIcon("Save").addIcon("DustBin").    
      addIcon("RedDustBin").addIcon("LightBlueFolder").
      addIcon("LightBlueOpenFolder").addIcon("LightBlueFolderHome")
  );

IconSet navigation = 
  new IconSet("navigation").
  addCategories(
      new IconCategory("Show").
      addIcon("BlueLevelUpArrow").addIcon("BlueSyncArrow")
  );

IconSet tool = 
  new IconSet("tool").
  addCategories(
      new IconCategory("Show").
      addIcon("")
  );

IconSet user = 
  new IconSet("user").
  addCategories(
      new IconCategory("Show").
      addIcon("")
  );

categorySet.addCategory(misc) ;
categorySet.addCategory(office) ;	
categorySet.addCategory(navigation) ;
categorySet.addCategory(tool) ;
categorySet.addCategory(user) ;

return categorySet;

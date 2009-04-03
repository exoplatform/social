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
import java.util.List;
import java.util.ArrayList;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;

List categories = new ArrayList();

  SelectItemCategory skinDefault = new  SelectItemCategory("Default", false);
  skinDefault.addSelectItemOption(new SelectItemOption("Default", "Default", "Default"));
  skinDefault.setSelected(true);
  categories.add(skinDefault);
  
  SelectItemCategory skinMac = new  SelectItemCategory("Mac", false);
  skinMac.addSelectItemOption(new SelectItemOption("Mac", "Mac", "Mac"));
  categories.add(skinMac);
  
  SelectItemCategory skinVista = new  SelectItemCategory("Vista", false);
  skinVista.addSelectItemOption(new SelectItemOption("Vista", "Vista", "Vista"));
  categories.add(skinVista);
  
return categories;  
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
import org.exoplatform.webui.core.model.SelectItemOption ;
import org.exoplatform.webui.core.model.SelectItemCategory ;
import java.util.List;
import java.util.ArrayList;

  List options = new ArrayList() ;
  
  SelectItemCategory defaultTemp  = new SelectItemCategory("Default");
  defaultTemp.addSelectItemOption(new SelectItemOption("Container template",
                                  "system:/groovy/portal/webui/container/UIContainer.gtmpl",
                                  "Description", "Default"));  
  options.add(defaultTemp);
  
  SelectItemCategory tableTemp  = new SelectItemCategory("Table Column");
  tableTemp.addSelectItemOption(new SelectItemOption("Table Column template",
                                "system:/groovy/portal/webui/container/UITableColumnContainer.gtmpl",
                                "Description","Default"));  
  options.add(tableTemp);

return options;
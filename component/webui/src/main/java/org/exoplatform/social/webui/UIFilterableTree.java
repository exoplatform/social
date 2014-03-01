/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.webui;

import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UITree;

@ComponentConfig(
  template = "war:/groovy/social/webui/UIFilterableTree.gtmpl",
  events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)
)
@Serialized
public class UIFilterableTree extends UITree {

  private TreeNodeFilter nodeFilter;

  /**
   * Constructor.
   *
   * @throws Exception
   */
  public UIFilterableTree() throws Exception {
    super();
  }

  public boolean displayThisNode(Object nodeObject,
                                 WebuiRequestContext context) {
    if (nodeFilter == null) {
      return true;
    }
    return !nodeFilter.filterThisNode(nodeObject, context);
  }

  /**
   * Returns index ( relative to unfiltered list of sibblings ) of most right displayed node.
   * The index is needed for a fine UI
   *
   * @param sibblings
   * @param context
   * @return
   */
  public int getRightMostDisplayedNodeIndex(List<Object> sibblings,
                                            WebuiRequestContext context) {
    if (sibblings == null) {
      return -1;
    }
    int numberOfSibblings = sibblings.size();
    if (nodeFilter == null) {
      return (numberOfSibblings - 1);
    } else {
      for (int i = (numberOfSibblings - 1); i >= 0; i--) {
        if (!nodeFilter.filterThisNode(sibblings.get(i), context)) {
          return i;
        }
      }
      return -1;
    }
  }

  /**
   * Method returns index ( relative to unfiltered list of sibblings ) of most left displayed node.
   *
   * @param sibblings
   * @param context
   * @return
   */
  public int getLeftMostDisplayedNodeIndex(List<Object> sibblings,
                                           WebuiRequestContext context) {
    if (sibblings == null) {
      return -1;
    }
    int numberOfSibblings = sibblings.size();
    if (nodeFilter == null) {
      return 0;
    } else {
      for (int i = 0; i < numberOfSibblings; i++) {
        if (!nodeFilter.filterThisNode(sibblings.get(i), context)) {
          return i;
        }
      }
      return numberOfSibblings;
    }
  }

  public void setTreeNodeFilter(TreeNodeFilter _nodeFilter) {
    this.nodeFilter = _nodeFilter;
  }

  public static interface TreeNodeFilter {
    public boolean filterThisNode(Object nodeObject,
                                  WebuiRequestContext context);
  }
}

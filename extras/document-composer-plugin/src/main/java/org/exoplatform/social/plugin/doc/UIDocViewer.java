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

package org.exoplatform.social.plugin.doc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * UIDocViewer <p></p>
 *
 * @author Zuanoc
 * @since Aug 10, 2010
 */

@ComponentConfig(
  lifecycle = Lifecycle.class
)
public class UIDocViewer extends UIBaseNodePresentation {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(UIDocViewer.class);
  private Node originalNode;

  /**
   * Sets the original node.
   *
   * @param originalNode
   */
  public void setOriginalNode(Node originalNode) {
    this.originalNode = originalNode;
  }

  /**
   * Gets the original node.
   *
   * @return
   * @throws Exception
   */
  public Node getOriginalNode() throws Exception {
    return originalNode;
  }

  /**
   * Sets the node.
   *
   * @param node
   */
  public void setNode(Node node) {
    originalNode = node;
  }

  @Override
  public Node getNode() throws Exception {
    return originalNode;
  }

  public String getTemplate() {
    try {
      return getTemplatePath();
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(getOriginalNode(), false);
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    String repository = getRepositoryName();
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig().getSystemWorkspace();
    return new JCRResourceResolver(repository, workspace);
  }

  public String getNodeType() {
    return null;
  }

  public boolean isNodeTypeSupported() {
    return false;
  }

  public UIComponent getCommentComponent() {
    return null;
  }

  public UIComponent getRemoveAttach() {
    return null;
  }

  public UIComponent getRemoveComment() {
    return null;
  }

  public UIComponent getUIComponent(String mimeType) throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    List<UIExtension> extensions = manager.getUIExtensions(Utils.FILE_VIEWER_EXTENSION_TYPE);
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(Utils.MIME_TYPE, mimeType);
    for (UIExtension extension : extensions) {
      UIComponent uiComponent = manager.addUIExtension(extension, context, this);
      if (uiComponent != null) {
        return uiComponent;
      }
    }
    return null;
  }

  public String getRepositoryName() {
    return UIDocActivityComposer.REPOSITORY;
  }
}
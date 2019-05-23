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
package org.exoplatform.social.webui.space;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.*;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;


public class UISpaceSettings extends UIFormInputSet {
  public static final String  SPACE_DISPLAY_NAME         = "displayName";

  public static final String   SPACE_DESCRIPTION         = "description";
  public static final String   SPACE_TEMPLATE            = "template";
  public static final String   SPACE_TEMPLATES_FEATURE   = "space-templates";
  private static final String TEMPLATES_ONCHANGE         = "ChangeTemplate";

  // Message
  private final String        MSG_INVALID_SPACE_NAME     = "UISpaceSettings.msg.invalid_space_name";

  private ExoFeatureService featureService;
  private SpaceTemplateService spaceTemplateService;

  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_PLACEHOLDER = "placeholder";

  private static final Log LOG = ExoLogger.getLogger(UISpaceSetting.class);

  /**
   * constructor
   *
   * @param name
   * @throws Exception
   */
  public UISpaceSettings(String name) throws Exception {
    super(name);
    featureService = CommonsUtils.getService(ExoFeatureService.class);
    spaceTemplateService = CommonsUtils.getService(SpaceTemplateService.class);
    //
    addUIFormInput(new UIFormStringInput(SPACE_DISPLAY_NAME, SPACE_DISPLAY_NAME, "").
            addValidator(MandatoryValidator.class).
            addValidator(ExpressionValidator.class, "^([\\p{L}\\s\\d\'_&]+[\\s]?)+$", MSG_INVALID_SPACE_NAME).
            addValidator(StringLengthValidator.class, 3, 200));
    UIFormSelectBox uiFormTypesSelectBox = new UIFormSelectBox(SPACE_TEMPLATE, SPACE_TEMPLATE, null);
    initTypeSelectBox(uiFormTypesSelectBox);
    uiFormTypesSelectBox.setOnChange(TEMPLATES_ONCHANGE);
    addUIFormInput(uiFormTypesSelectBox);
    addChild(UISpaceTemplateDescription.class, null, "UISpaceTemplateDescription");
    addUIFormInput(new UIFormTextAreaInput(SPACE_DESCRIPTION, SPACE_DESCRIPTION, ""));
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    ResourceBundle resourceBundle = context.getApplicationResourceBundle();
    getUIStringInput(SPACE_DISPLAY_NAME).setHTMLAttribute(HTML_ATTRIBUTE_PLACEHOLDER, resourceBundle.getString("UISpaceSettings.label.spaceDisplayName"));
    UIFormSelectBox uiFormTypesSelectBox = getUIFormSelectBox(SPACE_TEMPLATE);
    //Fix bug SOC-4821
    String scripts = new StringBuilder("(function(jq){jq(\"textarea#")
                        .append(SPACE_DESCRIPTION)
                        .append("\").attr(\"placeholder\", \"")
                        .append(resourceBundle.getString("UISpaceSettings.label.spaceDescription"))
                        .append("\");})(jq);").toString();
    context.getJavascriptManager().getRequireJS()
           .require("SHARED/jquery", "jq")
           .addScripts(scripts);
    boolean isActive = featureService.isActiveFeature(SPACE_TEMPLATES_FEATURE);
    boolean hasTemplates = uiFormTypesSelectBox.getOptions().size() > 0;
    uiFormTypesSelectBox.setRendered(isActive && hasTemplates);
    UISpaceTemplateDescription uiSpaceTemplateDescription = getChild(UISpaceTemplateDescription.class);
    uiSpaceTemplateDescription.setTemplateName(uiFormTypesSelectBox.getValue());
    uiSpaceTemplateDescription.setRendered(isActive && hasTemplates);
    super.processRender(context);
  }

  private void initTypeSelectBox(UIFormSelectBox typeSelectBox) throws Exception {
    List<SelectItemOption<String>> templates = new ArrayList<SelectItemOption<String>>();
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    List<SpaceTemplate> spaceTemplates = spaceTemplateService.getSpaceTemplates(userId);
    for (SpaceTemplate spaceTemplate : spaceTemplates) {
      String spaceType = spaceTemplate.getName();
      String translation = null;
      try {
        ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
        String key = "space.template." + spaceType;
        translation = resourceBundle.getString(key);
      } catch (MissingResourceException e) {
        translation = StringUtils.capitalize(spaceType);
      } catch (Exception e) {
        LOG.debug("Could not get resource bundle.");
      }
      SelectItemOption<String> option = new SelectItemOption<String>(translation, spaceType);
      templates.add(option);
    }
    String defaultSpaceTemplate = spaceTemplateService.getDefaultSpaceTemplate();
    typeSelectBox.setOptions(templates);
    SpaceTemplate defaultTemplate = spaceTemplateService.getSpaceTemplateByName(defaultSpaceTemplate);
    if (defaultTemplate != null && spaceTemplates.stream().anyMatch(st -> st.getName().equals(defaultSpaceTemplate))) {
      typeSelectBox.setValue(defaultSpaceTemplate);
    }
  }
}

/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.portlet.userNotification;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.View;
import juzu.impl.common.JSON;
import juzu.request.RenderContext;
import juzu.template.Template;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;


public class UserNotificationSetting {
  private static final Log LOG = ExoLogger.getLogger(UserNotificationSetting.class);

  @Inject
  @Path("index.gtmpl") Template index;
  
  @Inject
  ResourceBundle bundle;  
  
  @Inject
  PluginSettingService providerSettingService;

  @Inject
  UserSettingService     userSettingService;
  
  private static final String DAILY                   = "Daily";

  private static final String WEEKLY                  = "Weekly";

  private static final String NEVER                   = "Never";

  private static final String CHECK_BOX_DEACTIVATE_ID = "checkBoxDeactivate";

  private final static String SELECT_BOX_PREFIX       = "SelectBox";
  
  private Locale locale = Locale.ENGLISH;

  @View
  public void index(RenderContext renderContext) {
    //Redirect to the home's page if the feature is off
    if(CommonsUtils.isFeatureActive(NotificationUtils.FEATURE_NAME) == false) {
      redirectToHomePage();
      return;
    }
    
    if (renderContext != null) {
      locale = renderContext.getUserContext().getLocale();
    }
    if (bundle == null) {
      bundle = renderContext.getApplicationContext().resolveBundle(locale);
    }

    index.render(parameters());
  }
 
  
  @Ajax
  @Resource
  public Response saveSetting(String params) {
    JSON data = new JSON();
    try {
      UserSetting setting = UserSetting.getInstance();
      Map<String, String> datas = parserParams(params);
      for (String pluginId : datas.keySet()) {
        if (pluginId.indexOf(SELECT_BOX_PREFIX) > 0) {
          String value = datas.get(pluginId);
          pluginId = pluginId.replaceFirst(SELECT_BOX_PREFIX, "");
          //
          if (WEEKLY.equals(value)) {
            setting.addProvider(pluginId, FREQUENCY.WEEKLY);
          }
          if (DAILY.equals(value)) {
            setting.addProvider(pluginId, FREQUENCY.DAILY);
          }
        } else if (CHECK_BOX_DEACTIVATE_ID.equals(pluginId) == false) {
          setting.addProvider(pluginId, FREQUENCY.INSTANTLY);
        }
      }
      boolean status = "on".equals(datas.get(CHECK_BOX_DEACTIVATE_ID)) == false;
      setting.setUserId(Utils.getOwnerRemoteId()).setActive(status);
      //
      userSettingService.save(setting);
      data.set("ok", "true");
      data.set("status", String.valueOf(status));
    } catch (Exception e) {
      data.set("ok", "false");
      data.set("status", e.toString());
    }
   
    return Response.ok(data.toString()).withMimeType("application/json");
  }
  
  @Ajax
  @Resource
  public Response resetSetting(String params) {
    try {
      UserSetting setting = UserSetting.getDefaultInstance();
      //
      userSettingService.save(setting.setUserId(Utils.getOwnerRemoteId()));
    } catch (Exception e) {
      return new Response.Error("Error to save default notification user setting" + e.toString());
    }
    
    return index.ok(parameters()).withMimeType("text/html");
  }

  private Map<String, Object> parameters() {
    
    Map<String, Object> parameters = new HashMap<String, Object>();

    //
    Context context = new Context(bundle);
    parameters.put("_ctx", context);

    UserSetting setting = userSettingService.get(Utils.getOwnerRemoteId());
    //
    String checkbox = (setting.isActive() == true) ? "" : "checked";
    parameters.put("checkbox", checkbox);
    parameters.put("checkboxId", CHECK_BOX_DEACTIVATE_ID);
    //
    List<GroupProvider> groups = providerSettingService.getGroupPlugins();
    parameters.put("groups", groups);
    //
    boolean hasActivePlugin = false;
    for (GroupProvider group : groups) {
      for (PluginInfo plugin : group.getProviderDatas()) {
        if (plugin.isActive()) {
          hasActivePlugin = true;
          break;
        }
      }
    }
    parameters.put("hasActivePlugin", hasActivePlugin);
    
    Map<String, String> selectBoxList = new HashMap<String, String>();
    Map<String, String> checkBoxList = new HashMap<String, String>();
    
    Map<String, String> options = buildOptions(context);
    
    for (String pluginId : providerSettingService.getActivePluginIds()) {
      selectBoxList.put(pluginId, buildSelectBox(pluginId, options, getValue(setting, pluginId)));
      checkBoxList.put(pluginId, buildCheckBox(pluginId, isInInstantly(setting, pluginId)));
    }
    
    parameters.put("checkBoxs", checkBoxList);
    parameters.put("selectBoxs", selectBoxList);

    return parameters;
  }
  
  
  private boolean isInInstantly(UserSetting setting, String pluginId) {
    return (setting.isInInstantly(pluginId)) ? true : false;
  }

  private String makeSelectBoxId(String providerId) {
    return new StringBuffer(providerId).append(SELECT_BOX_PREFIX).toString();
  }
  
  private String getValue(UserSetting setting, String providerId) {
    if (setting.isInWeekly(providerId)) {
      return WEEKLY;
    } else if (setting.isInDaily(providerId)) {
      return DAILY;
    } else {
      return NEVER;
    }
  }
  
  private Map<String, String> buildOptions(Context context) {
    Map<String, String> options = new HashMap<String, String>();
    options.put("Daily", context.appRes("UINotification.label.Daily"));
    options.put("Weekly", context.appRes("UINotification.label.Weekly"));
    options.put("Never", context.appRes("UINotification.label.Never"));
    return options;
  }
  
  private String buildCheckBox(String name, boolean isChecked) {
    StringBuffer buffer = new StringBuffer("<span class=\"uiCheckbox\">");
    buffer.append(("<input type=\"checkbox\" class=\"checkbox\" "))
          .append((isChecked == true) ? "checked " : "")
          .append("name=\"").append(name).append("\" id=\"").append(name).append("\" />")
          .append("<span></span></span>");
    return buffer.toString();
  }

  private String buildSelectBox(String name, Map<String, String> options, String selectedId) {
    String selected = "";
    String id = makeSelectBoxId(name);
    StringBuffer buffer = new StringBuffer("<span class=\"uiSelectbox\">");
    buffer.append("<select name=\"").append(id).append("\" id=\"").append(id).append("\" class=\"selectbox\">");
    for (String key : options.keySet()) {
      selected = (key.equals(selectedId) == true) ? " selected=\"selected\" " : "";
      buffer.append("<option value=\"").append(key).append("\" class=\"option\"").append(selected).append(">").append(options.get(key)).append("</option>");
    }
    buffer.append("</select>").append("</span>");
    return buffer.toString();
  }
  
  private Map<String, String> parserParams(String params) {
    Map<String, String> datas = new HashMap<String, String>();
    String[] arrays = params.split("&");
    for (int i = 0; i < arrays.length; i++) {
      String[] data = arrays[i].split("=");
      datas.put(data[0], data[1]);
    }
    return datas;
  }
  
  private void redirectToHomePage() {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest currentServletRequest = portalRequestContext.getRequest();
    StringBuilder sb = new StringBuilder();
    sb.append(currentServletRequest.getScheme()).append("://")
      .append(currentServletRequest.getServerName())
      .append(":").append(currentServletRequest.getServerPort())
      .append("/").append(PortalContainer.getCurrentPortalContainerName())
      .append("/").append(portalRequestContext.getPortalOwner());
    
    WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
    JavascriptManager jsManager = ctx.getJavascriptManager();
    jsManager.addJavascript("try { window.location.href='" + sb.toString() + "' } catch(e) {" +
            "window.location.href('" + sb.toString() + "') }");
  }

  public class Context {
    ResourceBundle rs;

    public Context(ResourceBundle rs) {
      this.rs = rs;
    }

    public String appRes(String key) {
      try {
        return rs.getString(key).replaceAll("'", "&#39;").replaceAll("\"", "&#34;");
      } catch (java.util.MissingResourceException e) {
        LOG.warn("Can't find resource for bundle key " + key);
      } catch (Exception e) {
        LOG.debug("Error when get resource bundle key " + key, e);
      }
      return key;
    }
    
    private String getBundlePath(String id) {
      PluginConfig pluginConfig = providerSettingService.getPluginConfig(id);
      if (pluginConfig != null) {
        return pluginConfig.getTemplateConfig().getBundlePath();
      }
      //
      if (GroupProvider.defaultGroupIds.contains(id)) {
        return providerSettingService.getPluginConfig(DigestDailyPlugin.ID)
            .getTemplateConfig().getBundlePath();
      }
      //
      List<GroupProvider> groups = providerSettingService.getGroupPlugins();
      for (GroupProvider groupProvider : groups) {
        if (groupProvider.getGroupId().equals(id)) {
          return groupProvider.getProviderDatas().get(0).getBundlePath();
        }
      }
      return "";
    }

    public String pluginRes(String key, String id) {
      String path = getBundlePath(id);
      return TemplateUtils.getResourceBundle(key, locale, path);
    }
  }
     
}

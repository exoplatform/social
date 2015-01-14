package org.exoplatform.social.user.form;

import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputSet;

public class UIInputSection extends UIFormInputSet {
  private Map<String, List<ActionData>> actionFields = new HashMap<String, List<ActionData>>();
  private boolean useGroupControl = true;
  private String title;
  private String cssClass;

  public UIInputSection() {
  }

  public String getCssClass() {
    return cssClass;
  }

  public UIInputSection setCssClass(String cssClass) {
    this.cssClass = cssClass;
    return this;
  }

  public UIInputSection(String name) {
    super(name);
  }

  public UIInputSection(String name, String title) {
    this(name);
    this.title = title;
  }

  public UIInputSection(String name, String title, String cssClass) {
    this(name, title);
    this.cssClass = cssClass;
  }

  public UIFormDateTimeInput getUIFormDateTimeInput(String name) {
    return (UIFormDateTimeInput) findComponentById(name);
  }

  public UIMultiValueSelection getUIMultiValueSelection(String id) {
    return (UIMultiValueSelection) findComponentById(id);
  }

  public UIFormInput<?> addUIFormInput(UIFormInput<?> input, List<ActionData> actions) {
    addUIFormInput(input);
    actionFields.put(input.getName(), actions);
    return input;
  }
  
  public UIInputSection setActionField(String name, List<ActionData> actions) {
    actionFields.put(name, actions);
    return this;
  }
  
  public UIFormInputBase<?> addUIFormInput(UIFormInputBase<?> input, String label) {
    input.setLabel(label);
    addUIFormInput(input);
    return input;
  }

  public boolean useGroupControl() {
    return useGroupControl;
  }

  public UIInputSection useGroupControl(boolean useGroupControl) {
    this.useGroupControl = useGroupControl;
    return this;
  }

  public UIInputSection setTitle(String title) {
    this.title = title;
    return this;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    UIForm uiForm = getParent();
    Writer w = context.getWriter();
    
    w.append("<div class=\"form-horizontal ").append((cssClass != null) ? cssClass : "").append("\" id=\"").append(getId()).append("\">");
    //The title
    w.append("<h4 class=\"titleWithBorder\">");
    if(title != null && title.length() > 0) {
      w.append("<span class=\"nameTitle\">").append(uiForm.getLabel(title)).append("</span>");
    }
    w.append("</h4>");
    String classLable = (useGroupControl) ? "control-label" : "input-label";
    String classControlGroup = (useGroupControl) ? "control-group" : "input-group";
    String classControl = (useGroupControl) ? "controls" : "input-controls";
    
    for (UIComponent inputEntry : getChildren()) {
      if (inputEntry.isRendered() == false) {
        continue;
      }
      //
      String label;
      if (inputEntry instanceof UIFormInputBase) {
        label = ((UIFormInputBase<?>) inputEntry).getLabel();
      } else {
        label = inputEntry.getId();
      }
      w.append("<div class=\"").append(classControlGroup).append("\">");
      if (label != null && !label.isEmpty()) {
        w.append("<label class=\"").append(classLable).append("\" for=\"" + inputEntry.getId() + "\">");
        w.append(uiForm.getLabel(label)).append(":");
        w.append("</label>");
      }
      w.append("<div class=\"").append(classControl).append("\">");
      renderUIComponent(inputEntry);
      List<ActionData> actions = actionFields.get(inputEntry.getName());
      if(actions != null) {
        for (ActionData action : actions) {
          //
          String link = action.getLink();
          if (link == null && action.getAction() != null) {
            link = uiForm.event(action.getAction(), action.getObjectId());
          }
          if(link != null) {
            w.append("<a title=\"").append(action.getTooltip())
            .append("\" class=\"actionIcon\" rel=\"tooltip\" data-placement=\"bottom\" href=\"")
            .append(link).append("\">");
            if (action.getIcon() != null) {
              w.append("<i class=\"" + action.getIcon() + " uiIconLightGray\"></i>");
            }
            if (action.getActionLabel() != null) {
              w.append(action.getActionLabel());
            }
            w.append("</a>&nbsp;");
          } else if(action.getActionLabel() != null) {
            w.append("<span class=\"info-label\">").append(action.getActionLabel()).append("</span>&nbsp;");
          }
        }
      }
      w.append("  </div>");// end group
      w.append("</div>"); // end control
    }
    w.append("</div>");
  }

  public static class ActionData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String link;
    private String action;
    private String objectId = "";
    private String actionLabel;
    private String tooltip;
    private String icon;
    
    public ActionData() {
    }

    public String getLink() {
      return link;
    }

    public ActionData setLink(String link) {
      this.link = link;
      return this;
    }

    public String getAction() {
      return action;
    }

    public ActionData setAction(String action) {
      this.action = action;
      return this;
    }

    public String getActionLabel() {
      return actionLabel;
    }

    public ActionData setActionLabel(String actionLabel) {
      this.actionLabel = actionLabel;
      return this;
    }

    public String getTooltip() {
      return (tooltip == null) ? actionLabel : tooltip;
    }

    public ActionData setTooltip(String tooltip) {
      this.tooltip = tooltip;
      return this;
    }

    public String getIcon() {
      return icon;
    }

    public ActionData setIcon(String icon) {
      this.icon = icon;
      return this;
    }

    public String getObjectId() {
      return objectId;
    }

    public ActionData setObjectId(String objectId) {
      this.objectId = objectId;
      return this;
    }
  }
}

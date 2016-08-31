package org.exoplatform.social.core.activity.model;

/**
 * Enum of eXo activity plugin types
 * This enum is a hack to apply customizations based on the activity plugin type.
 * Ideally these customizations are done only by the plugins, not by the core.
 * The existence of this class means there is a lack of pluggability.
 */
public enum ActivityPluginType {
  DEFAULT(""),
  LINK("LINK_ACTIVITY"),
  DOC("DOC_ACTIVITY"),
  SPACE("SPACE_ACTIVITY"),
  PROFILE("USER_PROFILE_ACTIVITY"),
  FILE("files:spaces"),
  SHARE_FILE("sharefiles:spaces"),
  CONTENT("contents:spaces"),
  CALENDAR("cs-calendar:spaces"),
  TASK("TaskAdded"),
  FORUM("ks-forum:spaces"),
  ANSWER("ks-answer:spaces"),
  POLL("ks-poll:spaces"),
  WIKI("ks-wiki:spaces");

  private final String name;

  ActivityPluginType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}

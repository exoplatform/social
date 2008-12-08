package org.exoplatform.social.space;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Dec 4, 2008
 * Time: 11:50:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpaceException extends Exception{
  public enum Code {
    INTERNAL_SERVER_ERROR,
    UNABLE_TO_LIST_AVAILABLE_APPLICATIONS,
    UNABLE_TO_ADD_APPLICATION,    
    UNABLE_TO_REMOVE_APPLICATION,
    UNABLE_TO_CREATE_GROUP,
    UNABLE_TO_ADD_CREATOR,
    UNABLE_TO_REMOVE_USER,
    USER_NOT_MEMBER,
    USER_NOT_INVITED,
    ERROR_DATASTORE,
    SPACE_ALREADY_EXIST,
    UNKNOWN_SPACE_TYPE,
    UNABLE_TO_CREAT_NAV,
    UNABLE_TO_ADD_USER,
    ERROR_RETRIEVING_MEMBER_LIST,
    ERROR_RETRIEVING_USER,
    USER_ALREADY_INVITED,
    USER_ALREADY_MEMBER,
    ERROR_SENDING_CONFIRMATION_EMAIL,
    USER_NOT_EXIST,
    ERROR_SETTING_LEADER_STATUS
  }

  private final Code code;

  public SpaceException(Code code) {
    this.code = code;
  }

  public SpaceException(Code code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public SpaceException(Code code, String msg, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }

  public SpaceException(Code code, String msg) {
    super(msg);
    this.code = code;
  }

  public Code getCode() {
    return code;
  }
}


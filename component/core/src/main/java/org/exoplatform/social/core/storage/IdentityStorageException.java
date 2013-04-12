/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.storage;

import org.exoplatform.social.common.ExoSocialException;

/**
 * Handles Runtime Exception when performing data in Identity Storage.
 * 
 * @since 1.2.0-GA
 */
public class IdentityStorageException extends ExoSocialException {

  private static final String MESSAGE_BUNDLE_DELIMITER = ".";

  public static enum Type {
    FAIL_TO_SAVE_IDENTITY("Failed_To_Save_Identity"),
    FAIL_TO_UPDATE_IDENTITY("Failed_To_Update_Identity"),
    FAIL_TO_DELETE_IDENTITY("Failed_To_Update_Identity"),
    FAIL_TO_DELETE_PROFILE("Failed_To_Delete_Profile"),
    FAIL_TO_FIND_IDENTITY_BY_NODE_ID("Failed_To_Find_Identity_By_Node_Id"),
    FAIL_TO_FIND_IDENTITY("Failed_To_Find_Identity"),
    FAIL_TO_GET_IDENTITY_BY_FIRSTCHAR_COUNT("Failed_To_Get_Identity_By_FirstChar_Count"),
    FAIL_TO_GET_IDENTITY_BY_FIRSTCHAR("Failed_To_Get_Identity_By_FirstChar"),
    FAIL_TO_GET_IDENTITY_BY_PROFILE_FILTER_COUNT("Failed_To_Get_Identity_By_Profile_Filter_Count"),
    FAIL_TO_GET_IDENTITY_BY_PROFILE_FILTER("Failed_To_Get_Identity_By_Profile_Filter"),
    FAIL_TO_SAVE_PROFILE("Failed_To_Save_Profile"),
    FAIL_TO_UPDATE_PROFILE("Failed_To_Update_Profile"),
    FAIL_TO_ADD_OR_MODIFY_PROPERTIES("Failed_To_Add_Or_Modifiy_Properties"),
    FAIL_TO_GET_IDENTITIES_COUNT("Failed_To_Get_Identities"),
    FAIL_TO_LOAD_PROFILE("Failed_To_Load_Profile"),
    FAIL_TO_LOAD_AVATAR("Failed_To_Load_Avatar"),
    FAIL_TO_GET_IDENTITY_SERVICE_HOME("Failed_To_Get_Identity_Service_Home"),
    FAIL_TO_GET_PROFILE_SERVICE_HOME("Failed_To_Get_Profile_Service_Home"),
    FAIL_TO_GET_OR_CREAT_PROFILE_HOME_NODE("Failed_To_Get_Or_Creat_Pofile_Home_Node"),
    FAIL_TO_SET_PROPERTIES("Failed_To_Set_Properties");
    
    private final String msgKey;

    private Type(String msgKey) {
      this.msgKey = msgKey;
    }

    @Override
    public String toString() {
      return this.getClass() + MESSAGE_BUNDLE_DELIMITER + msgKey;
    }
  }

  /**
   * Initializes the IdentityStorageException.
   * 
   * @param type
   */
  public IdentityStorageException(Type type) {
    super(type.toString());
  }
  
  /**
   * Initializes the IdentityStorageException.
   * 
   * @param type
   * @param msg
   */
  public IdentityStorageException(Type type, String msg) {
    super(type.toString(), msg);
  }
  
  /**
   * Initializes the IdentityStorageException.
   * 
   * @param type
   * @param msgArgs
   */
  public IdentityStorageException(Type type, String[] msgArgs) {
    super(type.toString(), msgArgs);
  }
  
  /**
   * Initializes the IdentityStorageException.
   * 
   * @param type
   * @param msg
   * @param cause
   */
  public IdentityStorageException(Type type, String msg, Throwable cause) {
    super(type.toString(), msg, cause);
  }
  
  /**
   * Initializes the IdentityStorageException.
   * 
   * @param type
   * @param msgArgs
   * @param msg
   * @param cause
   */
  public IdentityStorageException(Type type, String[] msgArgs, String msg, Throwable cause) {
    super(type.toString(), msgArgs, msg, cause);
  }
}

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
 * @since 1.2.0-GA
 */
public class SpaceStorageException extends ExoSocialException {
  private static final String MESSAGE_BUNDLE_DELIMITER = ".";
  
  public static enum Type {
    FAILED_TO_GET_ALL_SPACES("Failed_To_Get_All_Spaces"),
    FAILED_TO_GET_SPACES_WITH_OFFSET("Failed_To_Get_Spaces_With_Offset"),
    FAILED_TO_GET_SPACES_COUNT("Failed_To_Get_Spaces_Count"),
    FAILED_TO_GET_SPACES_BY_SEARCH_CONDITION_WITH_OFFSET("Failed_To_Get_Spaces_By_Search_Condition_With_Offset"),
    FAILED_TO_GET_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_SPACES_BY_FIRST_CHARACTER_WITH_OFFSET("Failed_To_Get_Spaces_By_First_Character_With_Offset"),
    FAILED_TO_GET_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_ACCESSIBLE_SPACES("Failed_To_Get_Accessible_Spaces"),
    FAILED_TO_GET_ACCESSIBLE_SPACES_WITH_OFFSET("Failed_To_Get_Accessible_Spaces_With_Offset"),
    FAILED_TO_GET_ACCESSIBLE_SPACES_COUNT("Failed_To_Get_Accessible_Spaces_Count"),
    FAILED_TO_GET_ACCESSIBLE_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Accessible_Spaces_By_Search_Condition"),
    FAILED_TO_GET_ACCESSIBLE_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Accessible_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_ACCESSIBLE_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Accessible_Spaces_By_First_Character"),
    FAILED_TO_GET_ACCESSIBLE_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Accessible_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_EDITABLE_SPACES("Failed_To_Get_Editable_Spaces"),
    FAILED_TO_GET_EDITABLE_SPACES_WITH_OFFSET("Failed_To_Get_Editable_Spaces_With_Offset"),
    FAILED_TO_GET_EDITABLE_SPACES_COUNT("Failed_To_Get_Editable_Spaces_Count"),
    FAILED_TO_GET_EDITABLE_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Editable_Spaces_By_Search_Condition"),
    FAILED_TO_GET_EDITABLE_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Editable_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_EDITABLE_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Editable_Spaces_By_First_Character"),
    FAILED_TO_GET_EDITABLE_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Editable_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_INVITED_SPACES("Failed_To_Get_Invited_Spaces"),
    FAILED_TO_GET_INVITED_SPACES_WITH_OFFSET("Failed_To_Get_Invited_Spaces_With_Offset"),
    FAILED_TO_GET_INVITED_SPACES_COUNT("Failed_To_Get_Invited_Spaces_Count"),
    FAILED_TO_GET_INVITED_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Invited_Spaces_By_Search_Condition"),
    FAILED_TO_GET_INVITED_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Invited_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_INVITED_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Invited_Spaces_By_First_Character"),
    FAILED_TO_GET_INVITED_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Invited_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_PENDING_SPACES("Failed_To_Get_Pending_Spaces"),
    FAILED_TO_GET_PENDING_SPACES_WITH_OFFSET("Failed_To_Get_Pending_Spaces_With_Offset"),
    FAILED_TO_GET_PENDING_SPACES_COUNT("Failed_To_Get_Pending_Spaces_Count"),
    FAILED_TO_GET_PENDING_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Pending_Spaces_By_Search_Condition"),
    FAILED_TO_GET_PENDING_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Pending_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_PENDING_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Pending_Spaces_By_First_Character"),
    FAILED_TO_GET_PENDING_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Pending_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_PUBLIC_SPACES("Failed_To_Get_Public_Spaces"),
    FAILED_TO_GET_PUBLIC_SPACES_COUNT("Failed_To_Get_Public_Spaces_Count"),
    FAILED_TO_GET_PUBLIC_SPACES_WITH_OFFSET("Failed_To_Get_Public_Spaces_With_Offset"),
    FAILED_TO_GET_PUBLIC_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Public_Spaces_By_Search_Condition"),
    FAILED_TO_GET_PUBLIC_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Public_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_PUBLIC_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Public_Spaces_By_First_Character"),
    FAILED_TO_GET_PUBLIC_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Public_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_MEMBER_SPACES("Failed_To_Get_Member_Spaces"),
    FAILED_TO_GET_MEMBER_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Member_Spaces_By_First_Character"),
    FAILED_TO_GET_MEMBER_SPACES_BY_FIRST_CHARACTER_COUNT("Failed_To_Get_Member_Spaces_By_First_Character_Count"),
    FAILED_TO_GET_MEMBER_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Member_Spaces_By_Search_Condition"),
    FAILED_TO_GET_MEMBER_SPACES_BY_SEARCH_CONDITION_COUNT("Failed_To_Get_Member_Spaces_By_Search_Condition_Count"),
    FAILED_TO_GET_MEMBER_SPACES_WITH_OFFSET("Failed_To_Get_Member_Spaces_With_Offset"),
    FAILED_TO_GET_MEMBER_SPACES_COUNT("Failed_To_Get_Member_Spaces_Count"),
    FAILED_TO_GET_SPACE_BY_GROUP_ID("Failed_To_Get_Space_By_Group_Id"),
    FAILED_TO_GET_SPACE_BY_ID("Failed_To_Get_Space_By_Id"),
    FAILED_TO_GET_SPACES_BY_SEARCH_CONDITION("Failed_To_Get_Spaces_By_Search_Condition"),
    FAILED_TO_GET_SPACES_BY_FIRST_CHARACTER("Failed_To_Get_Spaces_By_First_Character"),
    FAILED_TO_GET_SPACE_BY_DISPLAY_NAME("Failed_To_Get_Space_By_Display_Name"),
    FAILED_TO_GET_SPACE_BY_NAME("Failed_To_Get_Space_By_Name"),
    FAILED_TO_GET_SPACE_BY_PRETTY_NAME("Failed_To_Get_Space_By_Pretty_Name"),
    FAILED_TO_GET_SPACE_BY_URL("Failed_To_Get_Space_By_Url"),
    FAILED_TO_DELETE_SPACE("Failed_To_Delete_Space"),
    FAILED_TO_SAVE_SPACE("Failed_To_Save_Space"),
    FAILED_TO_RENAME_SPACE("Failed_To_Rename_Space");
    
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
   * The constructor.
   * 
   * @param type
   */
  public SpaceStorageException(Type type) {
    super(type.toString());
  }

  /**
   * The constructor.
   * 
   * @param type
   * @param messageArguments
   */
  public SpaceStorageException(Type type, String[] messageArguments) {
    super(type.toString(), messageArguments);
  }

  /**
   * The constructor.
   * 
   * @param type
   * @param message
   */
  public SpaceStorageException(Type type, String message) {
    super(type.toString(), message);
  }

  /**
   * The constructor.
   * 
   * @param type
   * @param message
   * @param cause
   */
  public SpaceStorageException(Type type, String message, Throwable cause) {
    super(type.toString(), message, cause);
  }

  /**
   * The constructor.
   * 
   * @param type
   * @param messageArguments
   * @param message
   * @param cause
   */
  public SpaceStorageException(Type type, String[] messageArguments, String message, Throwable cause) {
    super(type.toString(), messageArguments, message, cause);
  }
}

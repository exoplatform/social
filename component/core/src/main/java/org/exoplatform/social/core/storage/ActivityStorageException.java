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
package org.exoplatform.social.core.storage;

import org.exoplatform.social.common.ExoSocialException;

/**
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @copyright eXo SAS
 * @since Nov 9, 2010
 */
public class ActivityStorageException extends ExoSocialException {

  private static final String MESSAGE_BUNDLE_DELIMITER = ".";

  public static enum Type {
    ILLEGAL_ARGUMENTS("Illegal_Arguments"),
    FAILED_TO_SAVE_ACTIVITY("Failed_To_Save_Activity"),
    FAILED_TO_GET_ACTIVITY("Failed_To_Get_Activity"),
    FAILED_TO_UPDATE_ACTIVITY("Failed_To_Update_Activity"),
    FAILED_TO_DELETE_ACTIVITY("Failed_To_Delete_Activity"),
    FAILED_TO_SAVE_COMMENT("Failed_To_Save_Comment"),
    FAILED_TO_DELETE_COMMENT("Failed_To_Delete_Comment"),
    FAILED_TO_GET_NUMBER_OF_COMMENTS("Failed_To_Get_Number_Of_Comments"),
    FAILED_TO_GET_ACTIVITIES("Failed_To_Get_Activities"),
    FAILED_TO_GET_ACTIVITIES_OF_CONNECTIONS("Failed_To_Get_Activities_Of_Connections"),
    FAILED_TO_GET_ACTIVITIES_OF_CONNECTIONS_WITH_OFFSET_LIMIT("Failed_To_Get_Activities_Of_Connections_With_Offset_Limit"),
    FAILED_TO_GET_NUMBER_OF_ACTIVITIES_OF_CONNECTIONS("Failed_To_Get_Number_Of_Activities_Of_Connections"),
    FAILED_TO_GET_ACTIVITIES_COUNT("Failed_To_Get_Activities_Count"),
    FAILED_TO_GET_NUMBER_OF_NEWER_ON_ACTIVITIES_OF_CONNECTIONS("Failed_To_Get_Number_Of_Newer_On_Activities_Of_Connections"),
    FAILED_TO_GET_NEWER_ON_ACTIVITIES_OF_CONNECTIONS("Failed_To_Get_Newer_On_Activities_Of_Connections"),
    FAILED_TO_GET_OLDER_ON_ACTIVITIES_OF_CONNECTIONS("Failed_To_Get_Older_On_Activities_Of_Connections"),
    FAILED_TO_GET_NUMBER_OF_OLDER_ON_ACTIVITIES_OF_CONNECTIONS("Failed_To_Get_Number_Of_Older_On_Activities_Of_Connections"),
    FAILED_TO_GET_USER_SPACE_ACTIVITIES_WITH_OFFSET_LIMIT("Failed_To_Get_User_Space_Activities_With_Offset_Limit"),
    FAILED_TO_GET_NUMBER_OF_USER_SPACE_ACTIVITIES("Failed_To_Get_Number_Of_User_Space_Activities");;
    
    private final String msgKey;

    private Type(String msgKey) {
      this.msgKey = msgKey;
    }

    @Override
    public String toString() {
      return this.getClass() + MESSAGE_BUNDLE_DELIMITER + msgKey;
    }
  }


  public ActivityStorageException(Type type) {
    super(type.toString());
  }


  public ActivityStorageException(Type type, String[] messageArguments) {
    super(type.toString(), messageArguments);
  }

  public ActivityStorageException(Type type, String message) {
    super(type.toString(), message);
  }

  public ActivityStorageException(Type type, String message, Throwable cause) {
    super(type.toString(), message, cause);
  }

  public ActivityStorageException(Type type, String[] messageArguments, String message, Throwable cause) {
    super(type.toString(), messageArguments, message, cause);
  }

}

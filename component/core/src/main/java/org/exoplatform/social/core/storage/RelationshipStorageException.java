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
 * @author tuan_nguyenxuan
 * @copyright eXo SAS
 * @since Nov 25, 2010
 */
public class RelationshipStorageException extends ExoSocialException {

  private static final String MESSAGE_BUNDLE_DELIMITER = ".";

  public static enum Type {
    ILLEGAL_ARGUMENTS("Illegal_Arguments"),
    FAILED_TO_SAVE_RELATIONSHIP("Failed_To_Save_Relationship"),
    FAILED_TO_GET_RELATIONSHIP("Failed_To_Get_Relationship"),
    FAILED_TO_GET_SUGGESTION("Failed_To_Get_Suggestion"),
    FAILED_TO_GET_RELATIONSHIP_OF_THEM("Failed_To_Get_Relationship_Of_Them"),
    FAILED_TO_UPDATE_RELATIONSHIP("Failed_To_Update_Relationship"),
    FAILED_TO_DELETE_RELATIONSHIP("Failed_To_Delete_Relationship"),
    MORE_THAN_ONE_RELATIONSHIP("More_Than_One_Relationship"),
    FAILED_TO_DELETE_RELATIONSHIP_ITEM_NOT_FOUND("Failed_To_Delete_Relationship_Item_Not_Found");

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
   * Initializes the RelationshipStorageException
   * 
   * @param type
   */
  public RelationshipStorageException(Type type) {
    super(type.toString());
  }

  /**
   * Initializes the RelationshipStorageException
   * 
   * @param type
   * @param cause
   */
  public RelationshipStorageException(Type type, Throwable cause) {
    super(type.toString(), cause);
  }

  /**
   * Initializes the RelationshipStorageException
   * 
   * @param type
   * @param messageArguments
   */
  public RelationshipStorageException(Type type, String... messageArguments) {
    super(type.toString(), messageArguments);
  }

  /**
   * Initializes the RelationshipStorageException
   * 
   * @param type
   * @param message
   */
  public RelationshipStorageException(Type type, String message) {
    super(type.toString(), message);
  }

  /**
   * Initializes the RelationshipStorageException
   * 
   * @param type
   * @param message
   * @param cause
   */
  public RelationshipStorageException(Type type, String message, Throwable cause) {
    super(type.toString(), message, cause);
  }

  /**
   * Initializes the RelationshipStorageException
   * 
   * @param type
   * @param message
   * @param cause
   * @param messageArguments
   */
  public RelationshipStorageException(Type type, String message, Throwable cause, String... messageArguments) {
    super(type.toString(), messageArguments, message, cause);
  }
}
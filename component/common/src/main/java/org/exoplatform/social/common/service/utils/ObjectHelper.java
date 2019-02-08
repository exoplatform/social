/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.common.service.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ObjectHelper {
  
  @SuppressWarnings("unchecked")
  private static final List<?> PRIMITIVE_ARRAY_TYPES = Arrays.asList(byte[].class, short[].class, int[].class, long[].class,
                                                                     float[].class, double[].class, char[].class, boolean[].class);
  
  
  
  /**
   * Retrieves the given exception type from the exception.
   * <br>
   * Is used to get the caused exception that typically have been wrapped in some sort
   * of Camel wrapper exception
   * <br>
   * The strategy is to look in the exception hierarchy to find the first given cause that matches the type.
   * Will start from the bottom (the real cause) and walk upwards.
   *
   * @param type the exception type wanted to retrieve
   * @param exception the caused exception
   * @return the exception found (or <tt>null</tt> if not found in the exception hierarchy)
   */
  public static <T> T getException(Class<T> type, Throwable exception) {
      if (exception == null) {
          return null;
      }

      // walk the hierarchy and look for it
      Iterator<Throwable> it = createExceptionIterator(exception);
      while (it.hasNext()) {
          Throwable e = it.next();
          if (type.isInstance(e)) {
              return type.cast(e);
          }
      }

      // not found
      return null;
  }
  
  /**
   * Creates an iterator to walk the exception from the bottom up
   * (the last caused by going upwards to the root exception).
   *
   * @param exception  the exception
   * @return the iterator
   */
  public static Iterator<Throwable> createExceptionIterator(Throwable exception) {
      return new ExceptionIterator(exception);
  }
  
  /**
   * Converts the given value to the required type or throw a meaningful exception
   */
  @SuppressWarnings("unchecked")
  public static <T> T cast(Class<T> toType, Object value) {
      if (toType == boolean.class) {
          return (T)cast(Boolean.class, value);
      } else if (toType.isPrimitive()) {
          Class<?> newType = convertPrimitiveTypeToWrapperType(toType);
          if (!toType.equals(newType)) {
              return (T)cast(newType, value);
          }
      }
      try {
          return toType.cast(value);
      } catch (ClassCastException e) {
          throw new IllegalArgumentException("Failed to convert: " 
              + value + " to type: " + toType.getName() + " due to: " + e, e);
      }
  }
  
  /**
   * Converts primitive types such as int to its wrapper type like
   * {@link Integer}
   */
  public static Class<?> convertPrimitiveTypeToWrapperType(Class<?> type) {
      Class<?> rc = type;
      if (type.isPrimitive()) {
          if (type == int.class) {
              rc = Integer.class;
          } else if (type == long.class) {
              rc = Long.class;
          } else if (type == double.class) {
              rc = Double.class;
          } else if (type == float.class) {
              rc = Float.class;
          } else if (type == short.class) {
              rc = Short.class;
          } else if (type == byte.class) {
              rc = Byte.class;
          } else if (type == boolean.class) {
              rc = Boolean.class;
          }
      }
      return rc;
  }
  
  private static final class ExceptionIterator implements Iterator<Throwable> {
    private List<Throwable> tree = new ArrayList<Throwable>();
    private Iterator<Throwable> it;

    public ExceptionIterator(Throwable exception) {
        Throwable current = exception;
        // spool to the bottom of the caused by tree
        while (current != null) {
            tree.add(current);
            current = current.getCause();
        }

        // reverse tree so we go from bottom to top
        Collections.reverse(tree);
        it = tree.iterator();
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    public Throwable next() {
        return it.next();
    }

    public void remove() {
        it.remove();
    }
}

}

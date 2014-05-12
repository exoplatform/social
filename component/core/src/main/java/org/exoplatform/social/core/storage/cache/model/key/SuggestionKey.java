/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage.cache.model.key;

public class SuggestionKey <T> extends ScopeCacheKey {
  
  /**
   * The serial version UID
   */
  private static final long serialVersionUID = 716295319216334677L;
  private final T key;
  private final int maxConnections;
  private final int maxConnectionsToLoad;
  private final int maxSuggestions;

  public SuggestionKey(final T key, int maxConnections, 
                        int maxConnectionsToLoad, 
                        int maxSuggestions) {
    this.key = key;
    this.maxConnections = maxConnections;
    this.maxConnectionsToLoad = maxConnectionsToLoad;
    this.maxSuggestions = maxSuggestions;
  }

  public T getKey() {
    return this.key;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + maxConnections;
    result = prime * result + maxConnectionsToLoad;
    result = prime * result + maxSuggestions;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    SuggestionKey other = (SuggestionKey)obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (maxConnections != other.maxConnections)
      return false;
    if (maxConnectionsToLoad != other.maxConnectionsToLoad)
      return false;
    if (maxSuggestions != other.maxSuggestions)
      return false;
    return true;
  }
}

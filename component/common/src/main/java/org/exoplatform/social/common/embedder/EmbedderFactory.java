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
package org.exoplatform.social.common.embedder;

import java.util.regex.Matcher;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

public class EmbedderFactory {

  private static Embedder youtubeEmbedder = null;
  private static Embedder oembedEmbedder = null;
  
  static {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    youtubeEmbedder = (YoutubeEmbedder) container.getComponentInstanceOfType(YoutubeEmbedder.class);
    oembedEmbedder = (OembedEmbedder) container.getComponentInstanceOfType(OembedEmbedder.class);
  }
  
  public static Embedder getInstance(String url) {
    Matcher youTubeMatcher = ((YoutubeEmbedder)youtubeEmbedder).getYouTubeURLPattern().matcher(url);
    //
    Embedder  embedder = youTubeMatcher.find() ? youtubeEmbedder : oembedEmbedder;
    
    //
    embedder.setUrl(url);
    
   //
    return embedder;
  }

  
}
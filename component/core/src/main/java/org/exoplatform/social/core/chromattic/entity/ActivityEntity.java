/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.exoplatform.social.core.chromattic.entity;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.DefaultValue;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:activity", orderable = true)
public abstract class ActivityEntity {

  @Id
  public abstract String getId();

  @Name
  public abstract String getName();

  @OneToMany
  public abstract List<ActivityEntity> getComments();

  @ManyToOne
  public abstract ActivityDayEntity getDay();

  @MappedBy("soc:identity")
  @ManyToOne(type = RelationshipType.REFERENCE)
  public abstract IdentityEntity getIdentity();
  public abstract void setIdentity(IdentityEntity identity);
  public static final PropertyLiteralExpression<String> identity = new PropertyLiteralExpression<String>(String.class, "soc:identity");

  @Property(name = "soc:title")
  public abstract String getTitle();
  public abstract void setTitle(String title);

  @Property(name = "soc:body")
  public abstract String getBody();
  public abstract void setBody(String body);

  @Property(name = "soc:likes")
  public abstract String[] getLikes();
  public abstract void setLikes(String[] title);

  @Property(name = "soc:isComment")
  @DefaultValue("false")
  public abstract Boolean isComment();
  public abstract void setComment(Boolean isComment);
  public static final PropertyLiteralExpression<Boolean> isComment = new PropertyLiteralExpression<Boolean>(Boolean.class, "soc:isComment");

  @Property(name = "soc:postedTime")
  public abstract Long getPostedTime();
  public abstract void setPostedTime(Long postedTime);
  public static final PropertyLiteralExpression<Long> postedTime = new PropertyLiteralExpression<Long>(Long.class, "soc:postedTime");

  @MappedBy("soc:params")
  @OneToOne
  @Owner
  public abstract ActivityParameters getParams();
  public abstract void setParams(ActivityParameters params);

  @Path
  public abstract String getPath();

  @Create
  public abstract ActivityEntity createComment(String name);

  @Create
  public abstract ActivityParameters createParams();

  public void putParams(Map<String, String> parameters) {

    ActivityParameters params = getParams();
    if (params == null) {
      setParams(params = createParams());
    }

    //
    Map<String, String> chromatticMap = params.getParams();
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      if (!entry.getKey().startsWith("jcr:"))
      chromatticMap.put(entry.getKey(), entry.getValue());
    }
    
  }
}

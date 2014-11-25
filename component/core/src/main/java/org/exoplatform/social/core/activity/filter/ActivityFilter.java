/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.core.activity.filter;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.filter.FilterLiteral;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.filter.JCRFilterLiteral;
import org.exoplatform.social.core.chromattic.filter.JCRFilterOption;
import org.exoplatform.social.core.chromattic.filter.JCROrderByOption;
import org.exoplatform.social.core.storage.api.ActivityStorage.TimestampType;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;


public class ActivityFilter extends JCRFilterLiteral {
  
  private static final Log LOG = ExoLogger.getLogger(ActivityFilter.class);
  
  
  //
  private static PropertyLiteralExpression<String> MENTION_TITLE = new PropertyLiteralExpression<String>(String.class, "soc:title");
  private static PropertyLiteralExpression<TimestampType> ACTIVITY_UPDATED_POINT = new PropertyLiteralExpression<TimestampType>(TimestampType.class, ActivityEntity.lastUpdated.toString());
  private static PropertyLiteralExpression<TimestampType> ACTIVITY_FROM_UPDATED_POINT = new PropertyLiteralExpression<TimestampType>(TimestampType.class, "From_Last_Updated");
  private static PropertyLiteralExpression<TimestampType> ACTIVITY_TO_UPDATED_POINT = new PropertyLiteralExpression<TimestampType>(TimestampType.class, "To_Last_Updated");

  //
  public static JCRFilterOption IS_COMMENT_FIELD = new JCRFilterOption(ActivityEntity.isComment);
  public static JCRFilterOption POSTED_TIME_FIELD = new JCRFilterOption(ActivityEntity.postedTime);
  public static JCRFilterOption IDENTITY_FIELD = new JCRFilterOption(ActivityEntity.identity);
  public static JCRFilterOption POSTER_FIELD = new JCRFilterOption(ActivityEntity.poster);
  public static JCRFilterOption TITLE_FIELD = new JCRFilterOption(ActivityEntity.title);
  public static JCRFilterOption MENTIONERS_FIELD = new JCRFilterOption(ActivityEntity.mentioners);
  public static JCRFilterOption COMMENTERS_FIELD = new JCRFilterOption(ActivityEntity.commenters);
  public static JCRFilterOption LIKES_FIELD = new JCRFilterOption(ActivityEntity.likes);
  

  public static JCRFilterOption TITLE_MENTION_FIELD = new JCRFilterOption(MENTION_TITLE) {
      public FilterLiteral<PropertyLiteralExpression<?>> value(Object value) {
        String newValue = null;
        if (this.getLiteral().getType() == value.getClass()) {
          newValue = "% @" + value.toString() + " %";
        }
        
        return super.value(newValue);
      };
  };
  
  public static JCRFilterOption ACTIVITY_UPDATED_POINT_FIELD = new JCRFilterOption(ACTIVITY_UPDATED_POINT);
  public static JCRFilterOption ACTIVITY_FROM_UPDATED_POINT_FIELD = new JCRFilterOption(ACTIVITY_FROM_UPDATED_POINT);
  public static JCRFilterOption ACTIVITY_TO_UPDATED_POINT_FIELD = new JCRFilterOption(ACTIVITY_TO_UPDATED_POINT);
  //public static JCRFilterOption ACTIVITY_POSTED_POINT_FIELD = new JCRFilterOption(ACTIVITY_POSTED_POINT);
  

  //ORDER_BY
  public static JCROrderByOption POSTED_TIME_ORDERBY = new JCROrderByOption(ActivityEntity.postedTime);
  public static JCROrderByOption LAST_UPDATED_ORDERBY = new JCROrderByOption(ActivityEntity.lastUpdated);
  
  @Override
  protected void start() {
    try {
      this.append(ACTIVITY_UPDATED_POINT_FIELD.clone())
      .append(MENTIONERS_FIELD.clone())
      .append(COMMENTERS_FIELD.clone())
      .append(LIKES_FIELD.clone())
      .with(IS_COMMENT_FIELD).value(Boolean.FALSE)
      .with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.DESC)
      .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.DESC);
    } catch (Exception ex) {
      LOG.warn(ex);
    }
  }
  
  @Override
  public void destroy() {
    this.with(ACTIVITY_UPDATED_POINT_FIELD).value(null);
    this.with(MENTIONERS_FIELD).value(null);
    this.with(COMMENTERS_FIELD).value(null);
    this.with(LIKES_FIELD).value(null);
  }
  
  
  //DEFINE FILTER
  public static ActivityFilter space() {
    return new ActivityFilter() {
      @Override
      protected void start() {
        try {
          super.start();

          //
          this.append(ACTIVITY_UPDATED_POINT_FIELD.clone())
          .with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.DESC)
          .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.DESC);
        } catch (Exception ex) {
          LOG.warn(ex);
        }
      }
    };
  }
  
  public static ActivityFilter spaceNewer() {
    return new ActivityFilter() {

      @Override
      protected void start() {
        try {
          super.start();
          this.remove(POSTED_TIME_ORDERBY.clone());

          //
          this.append(ACTIVITY_UPDATED_POINT_FIELD.clone())
              .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.ASC)
              .with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.ASC);
        } catch (Exception ex) {
          LOG.warn(ex);
        }
      }
    };
  }
  
  public static ActivityFilter spaceOlder() {
    return new ActivityFilter() {
      
      @Override
      protected void start() {
        try {
          super.start();
          this.remove(POSTED_TIME_ORDERBY);
          
          //
          this.append(ACTIVITY_UPDATED_POINT_FIELD.clone())
          .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.DESC)
          .with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.DESC);
        } catch (Exception ex) {
          LOG.warn(ex);
        }
      }
    };
  }
  
  public static ActivityFilter newer() {
    return new ActivityFilter() {
      @Override
      protected void start() {
        try {
          super.start();
          //destroy old value;
          super.destroy();
          
          this.with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.ASC)
          .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.ASC);
          
        } catch (Exception ex) {
          LOG.warn(ex);
        }
      }
    };
  }

  public static ActivityFilter older() {
    return new ActivityFilter() {

      @Override
      protected void start() {
        try {
          super.start();
          super.destroy();
          //
          this.with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.DESC)
              .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.DESC);
        } catch (Exception ex) {
          LOG.warn(ex);
        }
      }
    };
  }
  
  
 public static JCRFilterLiteral ACTIVITY_NEW_UPDATED_FILTER = new JCRFilterLiteral() {
    
    @Override
    protected void start() {
      try {
        //
        this.append(ACTIVITY_UPDATED_POINT_FIELD.clone())
        .with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.DESC)
        .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.DESC);
      } catch (Exception ex) {
        LOG.warn(ex);
      }
    }

    @Override
    public void destroy() {
      
    }
  };
  
public static JCRFilterLiteral ACTIVITY_VIEWED_RANGE_FILTER = new JCRFilterLiteral() {
    
    @Override
    protected void start() {
      try {
        //
        this.append(ACTIVITY_FROM_UPDATED_POINT_FIELD.clone())
        .append(ACTIVITY_TO_UPDATED_POINT_FIELD.clone())
        .with(LAST_UPDATED_ORDERBY.clone()).direction(DIRECTION.ASC)
        .with(POSTED_TIME_ORDERBY.clone()).direction(DIRECTION.ASC);
      } catch (Exception ex) {
        LOG.warn(ex);
      }
    }

    @Override
    public void destroy() {
      
    }
  };
}

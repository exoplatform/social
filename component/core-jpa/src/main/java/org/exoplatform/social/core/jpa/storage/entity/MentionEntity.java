/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by bdechateauvieux on 7/7/15.
 */
@Entity(name = "SocMention")
@ExoEntity
@Table(name="SOC_MENTIONS")
@NamedQueries({
        @NamedQuery(name = "SocMention.migrateMentionId",
                query = "UPDATE SocMention m SET m.mentionId = :newId WHERE m.mentionId = :oldId"),
        @NamedQuery(name = "SocMention.selectMentionByOldId",
                query = "SELECT m FROM SocMention m WHERE m.mentionId LIKE :oldId"),
})
public class MentionEntity {

  @Id
  @SequenceGenerator(name="SEQ_SOC_MENTIONS_ID", sequenceName="SEQ_SOC_MENTIONS_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_SOC_MENTIONS_ID")
  @Column(name="MENTION_ID")
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="ACTIVITY_ID", nullable = false)
  private ActivityEntity activity;

  @Column(name="MENTIONER_ID", nullable = false)
  private String mentionId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ActivityEntity getActivity() {
    return activity;
  }

  public void setActivity(ActivityEntity activity) {
    this.activity = activity;
  }

  public String getMentionId() {
    return mentionId;
  }

  public void setMentionId(String mentionId) {
    this.mentionId = mentionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MentionEntity that = (MentionEntity) o;

    if (!activity.equals(that.activity)) return false;
    return mentionId.equals(that.mentionId);

  }

  @Override
  public int hashCode() {
    int result = activity.hashCode();
    result = 31 * result + mentionId.hashCode();
    return result;
  }
}

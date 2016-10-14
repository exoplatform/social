/*
 * Copyright (C) 2015 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.updater.listener;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.jpa.storage.entity.MentionEntity;
import org.exoplatform.social.core.identity.model.Identity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class IdentityReferenceUpdaterListener extends Listener<Identity, String> {
  @Override
  public void onEvent(Event<Identity, String> event) throws Exception {
    EntityManagerService emService = CommonsUtils.getService(EntityManagerService.class);
    EntityManager em = emService.getEntityManager();
    if (em == null) {
      return;
    }

    boolean startTx = false;
    try {
      if (!em.getTransaction().isActive()) {
        em.getTransaction().begin();
        startTx = true;
      }

      Identity identity = event.getSource();
      String newId = event.getData();
      String oldId = identity.getId();

      Query query;

      // Update activity poster
      query = em.createNamedQuery("SocActivity.migratePosterId");
      query.setParameter("newId", newId);
      query.setParameter("oldId", oldId);
      query.executeUpdate();

      // Activity owner
      query = em.createNamedQuery("SocActivity.migrateOwnerId");
      query.setParameter("newId", newId);
      query.setParameter("oldId", oldId);
      query.executeUpdate();

      //activity mention
      query = em.createNamedQuery("SocMention.migrateMentionId");
      query.setParameter("newId", newId);
      query.setParameter("oldId", oldId);
      query.executeUpdate();

      query = em.createNamedQuery("SocMention.selectMentionByOldId", MentionEntity.class);
      query.setParameter("oldId", oldId + "@%");
      List<MentionEntity> list = query.getResultList();
      if (list != null && list.size() > 0) {
        for (MentionEntity m : list) {
          String mentionId = m.getMentionId();
          mentionId = mentionId.replace(oldId, newId);
          m.setMentionId(mentionId);
          em.merge(m);
        }
      }


      //TODO: Can not use the JPQL for this?
      // Activity Liker
      query = em.createNativeQuery("UPDATE SOC_ACTIVITY_LIKERS SET LIKER_ID = ? WHERE LIKER_ID = ?");
      query.setParameter(1, newId);
      query.setParameter(2, oldId);
      query.executeUpdate();

      // Stream Item
      /*query = em.createNamedQuery("SocStreamItem.migrateOwner");
      query.setParameter("newId", newId);
      query.setParameter("oldId", oldId);
      query.executeUpdate();*/
    } finally {
      if (startTx) {
        em.getTransaction().commit();
      }
    }
  }
}

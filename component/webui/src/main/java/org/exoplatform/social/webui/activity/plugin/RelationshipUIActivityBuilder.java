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
package org.exoplatform.social.webui.activity.plugin;

import java.util.Map;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.application.RelationshipPublisher;
import org.exoplatform.social.core.application.RelationshipPublisher.TitleId;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;

/**
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Aug 31, 2010
 * @copyright eXo SAS
 */
public class RelationshipUIActivityBuilder extends BaseUIActivityBuilder {

  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    UIRelationshipActivity uiRelationshipActivity = (UIRelationshipActivity) uiActivity;
    String titleId = activity.getTitleId();
    if (titleId.equals(TitleId.CONNECTION_CONFIRMED.toString())) {
      uiRelationshipActivity.setTitleId(TitleId.CONNECTION_CONFIRMED);
    } else if (titleId.equals(TitleId.CONNECTION_REQUESTED.toString())) {
      uiRelationshipActivity.setTitleId(TitleId.CONNECTION_REQUESTED);
    }

    Map<String, String> templateParams = activity.getTemplateParams();
    uiRelationshipActivity.setSenderName(templateParams.get(RelationshipPublisher.SENDER_PARAM));
    uiRelationshipActivity.setReceiverName(templateParams.get(RelationshipPublisher.RECEIVER_PARAM));
    uiRelationshipActivity.setRelationshipUUID(templateParams.get(RelationshipPublisher.RELATIONSHIP_UUID_PARAM));
  }

}

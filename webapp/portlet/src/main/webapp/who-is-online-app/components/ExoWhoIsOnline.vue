<template>
  <v-app v-if="users && users.length > 0" id="OnlinePortlet">
    <div class="onlinePortlet">
      <div id="onlineContent" class="uiBox">
        <v-card-title class="title center">
          <span :class="firstLoadingOnLineUsers && 'skeleton-background skeleton-text skeleton-header skeleton-border-radius'">{{ $t('header.label') }}</span>
        </v-card-title>
        <ul id="onlineList" class="gallery uiContentBox">
          <li v-for="user in users" :key="user" :id="user.id">
            <a :href="user.href" class="avatarXSmall">
              <v-avatar size="37" class="mx-1">
                <v-img :src="!firstLoadingOnLineUsers && user.avatar || ''"
                       :class="firstLoadingOnLineUsers && 'skeleton-background'"></v-img>
              </v-avatar>
            </a>
          </li>
        </ul>
      </div>
    </div>
  </v-app>
</template>

<script>
import * as whoIsOnlineServices from '../whoIsOnlineServices';
import { spacesConstants } from '../../js/spacesConstants.js';

export default {
  data() {
    return {
      users: [],
      firstLoadingOnLineUsers: true
    };
  },
  created() {
    this.initOnlineUsers();
    // And we should use setInterval with 60 seconds
    const delay = 60000;
    setInterval(function () {
      this.initOnlineUsers();
    }.bind(this), delay);
  },
  updated() {
    this.initPopup();
  },
  methods: {
    initOnlineUsers() {
      whoIsOnlineServices.getOnlineUsers(eXo.env.portal.spaceId).then(response => {
        let got;
        if (response) {
          got = response.users;
          if (got && got.length > 0) {
            this.users = [];
            for (const el of got) {
              el.href = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_NAME}/profile/${el.username}`;
              if (!el.avatar) {
                el.avatar = `${spacesConstants.SOCIAL_USER_API}/${el.username}/avatar`;
              }
              this.users.push(el);
            }
            $('#OnlinePortlet').show();
          } else {
            $('#OnlinePortlet').hide();
          }
          if(this.firstLoadingOnLineUsers) {
            this.firstLoadingOnLineUsers = false;
          }
        }
      });
    },
    initPopup() {
      const restUrl = `//${spacesConstants.HOST_NAME}${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/social/people/getPeopleInfo/{0}.json`;
      const labels = {
        youHaveSentAnInvitation: this.$t('message.label'),
        StatusTitle: this.$t('Loading.label'),
        Connect: this.$t('Connect.label'),
        Confirm: this.$t('Confirm.label'),
        CancelRequest: this.$t('CancelRequest.label'),
        RemoveConnection: this.$t('RemoveConnection.label'),
        Ignore: this.$t('Ignore.label'),
        Disabled: this.$t('Disabled.label')
      };
      $('#onlineList').find('a').each(function (idx, el) {
        $(el).userPopup({
          restURL: restUrl,
          labels: labels,
          content: false,
          defaultPosition: 'left',
          keepAlive: true,
          maxWidth: '240px'
        });
      });
    }
  }
};
</script>
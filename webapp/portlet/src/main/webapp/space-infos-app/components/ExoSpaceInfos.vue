<template>
  <div id="spaceInfosApp" class="uiBox">
    <h5 class="center">{{ $t("social.space.description.title") }}</h5>
    <p id="spaceDescription" v-html="description"></p>
    <div id="spaceManagersList">
      <h5>{{ $t("social.space.description.managers") }}</h5>
      <ul id="spaceManagers">
        <li v-for="manager in managers" :key="manager" class="spaceManagerEntry">
          <a :href="manager.href">
            <img :src="manager.avatar" alt="avatar"/> {{ manager.fullname }}
          </a>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import * as spaceInfosServices from '../spaceInfosServices.js';
import { spacesConstants } from '../../js/spacesConstants.js';

export default {
  data() {
    return {
      description: '',
      managers: []
    };
  },
  created() {
    this.initSpaceDescription();
    this.initSpaceManagers();
  },
  updated() {
    this.initPopup();
  },
  methods: {
    initSpaceDescription() {
      spaceInfosServices.getSpaceDescriptionByPrettyName().then(data => {
        if(data) {
          this.description = data.description;
        }
      });
    },
    initSpaceManagers() {
      spaceInfosServices.getSpaceManagersByPrettyName().then(response => {
        if(response) {
          const got = response.users;
          if (got && got.length > 0) {
            this.managers = [];
            for (const el of got) {
              el.href = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_NAME}/profile/${el.username}`;
              if (!el.avatar) {
                el.avatar = `${spacesConstants.SOCIAL_USER_API}/${el.username}/avatar`;
              }
              if(el.enabled && !el.deleted) {
                this.managers.push(el);
              }
            }
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
        Ignore: this.$t('Ignore.label')
      };
      $('#spaceManagers').find('a').each(function (idx, el) {
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
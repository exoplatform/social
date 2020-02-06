<template>
  <div class="activityComposer">
    <div class="openLink">
      <a @click="showMessageComposer = true">
        <i class="uiIconPlus"></i>{{ $t('activity.composer.link').replace('{0}', postTarget) }}
      </a>
    </div>

    <v-app id="activityComposerApp" class="VuetifyApp">
      <div :class="showMessageComposer ? 'open' : ''" class="drawer">
        <div class="header">
          <img src="/eXoSkin/skin/images/system/composer/composer.png">
          <span> {{ $t('activity.composer.title') }}</span>
          <a class="closebtn" href="javascript:void(0)" @click="closeMessageComposer()">×</a>
        </div>
        <div class="content">
          <exo-activity-rich-editor v-model="message" :max-length="MESSAGE_MAX_LENGTH" :placeholder="$t('activity.composer.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"></exo-activity-rich-editor>
          <div class="composerActions">
            <button :disabled="postDisabled" type="button" class="btn btn-primary ignore-vuetify-classes" @click="postMessage()">{{ $t('activity.composer.post') }}</button>
          </div>
          <transition name="fade">
            <div v-show="showErrorMessage" class="alert alert-error">
              <i class="uiIconError"></i>{{ $t('activity.composer.post.error') }}
            </div>
          </transition>
        </div>
      </div>
    </v-app>
    <div v-show="showMessageComposer" class="drawer-backdrop" @click="closeMessageComposer()"></div>
  </div>
</template>

<script>
import * as composerServices from '../composerServices';

export default {
  data() {
    return {
      MESSAGE_MAX_LENGTH: 2000,
      MESSAGE_TIMEOUT: 5000,
      postTarget: '',
      showMessageComposer: false,
      message: '',
      showErrorMessage: false
    };
  },
  computed: {
    postDisabled: function() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length === 0 || pureText.length > this.MESSAGE_MAX_LENGTH;
    }
  },
  watch: {
    showErrorMessage: function(newVal) {
      if(newVal) {
        setTimeout(() => this.showErrorMessage = false, this.MESSAGE_TIMEOUT);
      }
    }

  },
  mounted() {
    this.postTarget = eXo.env.portal.spaceDisplayName;
    if(!this.postTarget) {
      this.postTarget = this.$t('activity.composer.link.network');
    }
  },
  methods: {
    postMessage() {
      if(eXo.env.portal.spaceId) {
        composerServices.postMessageInSpace(this.message, eXo.env.portal.spaceId)
          .then(() => this.refreshActivityStream())
          .then(() => this.closeMessageComposer())
          .then(() => this.message = '')
          .catch(error => {
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          });
      } else {
        composerServices.postMessageInUserStream(this.message, eXo.env.portal.userName)
          .then(() => this.refreshActivityStream())
          .then(() => this.closeMessageComposer())
          .then(() => this.message = '')
          .catch(error => {
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          });
      }
    },
    refreshActivityStream() {
      const refreshButton = document.querySelector('.uiActivitiesDisplay #RefreshButton');
      if(refreshButton) {
        refreshButton.click();
      }
    },
    closeMessageComposer: function() {
      this.showMessageComposer = false;
    }
  }
};
</script>
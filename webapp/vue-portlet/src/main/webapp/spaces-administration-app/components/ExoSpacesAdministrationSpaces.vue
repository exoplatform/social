<template>
  <v-app
    id="appId"
    class="VuetifyApp"
    flat>
    <div class="spacesAdministration">
      <div class="uiTabAdvanced uiTabInPage">
        <ul class="nav nav-tabs">
          <li :class="{active: activeTab === 1}" @click="activeTab=1">
            <a href="#manage" data-toggle="tab">{{ $t('social.spaces.administration.manageSpaces') }}</a>
          </li>
          <li v-show="canChangePermissions" :class="{active: activeTab === 2}" @click="activeTab=2" >
            <a href="#permissions" data-toggle="tab">{{ $t('social.spaces.administration.permissions') }}</a>
          </li>
        </ul> 
        <div class="tab-content">
          <div v-if="activeTab === 1" id="manage" class="tab-pane fade in active">
            <exo-spaces-administration-manage-spaces></exo-spaces-administration-manage-spaces>
          </div>
          <div v-if="canChangePermissions && activeTab === 2" id="permissions" class="tab-pane fade in active">
            <exo-spaces-administration-manage-permissions></exo-spaces-administration-manage-permissions>
          </div>
        </div> 
      </div>
    </div>
  </v-app>
</template>

<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';

export default {
  data() { 
    return {
      activeTab: 1,
      canChangePermissions: false
    };
  },
  created() {
    spacesAdministrationServices.getUserPermissions(eXo.env.portal.userName).then(data => {
      if(data && data.platformAdministrator) {
        this.canChangePermissions = data.platformAdministrator;
      }
    });
  },
  mounted() {
    const windowLocationHash = window.location.hash;
    if (windowLocationHash === '#permissions') {
      this.activeTab = 2;
    } else {
      this.activeTab = 1;
    }
  }
};
</script>


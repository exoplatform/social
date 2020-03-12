<template>
  <v-app
    class="spacesAdministration"
    flat>
    <div class="uiTabAdvanced uiTabInPage">
      <ul class="nav nav-tabs">
        <li :class="{active: activeTab === 1}" @click="activeTab=1">
          <a href="#manage" data-toggle="tab">{{ $t('social.spaces.administration.manageSpaces') }}</a>
        </li>
        <li v-show="canChangePermissions" :class="{active: activeTab === 2}" @click="activeTab=2" >
          <a href="#permissions" data-toggle="tab">{{ $t('social.spaces.administration.permissions') }}</a>
        </li>
        <li v-show="canChangePermissions" :class="{active: activeTab === 3}" @click="activeTab=3" >
          <a href="#bindingReports" data-toggle="tab">{{ $t('social.spaces.administration.bindingReports') }}</a>
        </li>
      </ul>
      <div class="tab-content">
        <div v-if="showManageSpaces" id="manage" class="tab-pane fade in active">
          <exo-spaces-administration-manage-spaces :can-bind-groups-and-spaces="canChangePermissions" @bindingReports="activeTab = 3"></exo-spaces-administration-manage-spaces>
        </div>
        <div v-if="showPermissions" id="permissions" class="tab-pane fade in active">
          <exo-spaces-administration-manage-permissions></exo-spaces-administration-manage-permissions>
        </div>
        <div v-if="showBindingReports" id="bindingReports" class="tab-pane fade in active">
          <exo-spaces-administration-binding-reports></exo-spaces-administration-binding-reports>
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
  computed: {
    showManageSpaces() {
      return this.activeTab && this.activeTab === 1;
    },
    showPermissions() {
      const permissionTabNumber = 2;
      return this.canChangePermissions && this.activeTab && this.activeTab === permissionTabNumber;
    },
    showBindingReports() {
      const bindingReportsTabNumber = 3;
      return this.canChangePermissions && this.activeTab && this.activeTab === bindingReportsTabNumber && this.canChangePermissions;
    }
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
    if (windowLocationHash === '#bindingReports') {
      this.activeTab = 3;
    } else if (windowLocationHash === '#permissions') {
      this.activeTab = 2;
    } else {
      this.activeTab = 1;
    }
  }
};
</script>


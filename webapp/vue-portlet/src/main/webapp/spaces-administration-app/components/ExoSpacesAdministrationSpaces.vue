<template>
  <div class="uiTabNormal uiTabInPage">
    <ul class="nav nav-tabs">
      <li :class="{active: activeTab === 1}" @click="activeTab=1">
        <a data-toggle="tab">Manage Spaces</a>
      </li>
      <li :class="{active: activeTab === 2}" @click="activeTab=2" >
        <a data-toggle="tab">Permissions</a>
      </li>
    </ul> 
    <div class="tab-content">
      <div v-if="activeTab === 1" class="tab-pane fade in active">
        <exo-spaces-administration-manage-spaces></exo-spaces-administration-manage-spaces>
      </div>
      <div v-if="activeTab === 2" class="tab-pane fade in active">
        <h1>permissssssssssss</h1>
      </div>
    </div> 
  </div>
</template>

<script>
import ExoSpacesAdministrationManageSpaces from './ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './ExoSpacesAdministrationSpacesPermissions.vue';

export default {
  components: {
    'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
    'exo-spaces-administration-manage-permissions' : ExoSpacesAdministrationSpacesPermissions
  },
  data() { 
    return {
      activeTab: 1
    }
  }
};
</script>


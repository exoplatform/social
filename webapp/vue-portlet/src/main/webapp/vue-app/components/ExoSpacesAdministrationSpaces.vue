<template>
  <div class="uiTabNormal uiTabInPage">
    <ul class="nav nav-tabs">
      <li :class="[ activetab === 1 ? 'active' : '' ]" @click="activetab=1">
        <a data-toggle="tab">Manage Spaces</a>
      </li>
      <li :class="[ activetab === 2 ? 'active' : '' ]" @click="activetab=2" >
        <a data-toggle="tab">Permissions</a>
      </li>
    </ul> 
    <div class="tab-content">
      <div v-if="activetab === 1" class="tab-pane fade in active">
        <exo-spaces-administration-manage-spaces></exo-spaces-administration-manage-spaces>
      </div>
      <div v-if="activetab === 2" class="tab-pane fade in active">
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
      activetab: 1
    }
  }
};
</script>
<style>
.uiTabInPage .nav-tabs{
    background: #fff;
    width: 1024px;
    border-bottom: 0;
}
.nav-tabs > li{
    width: 185px;
    border:none;
}
.uiTabInPage{
    border: 1px solid #ccc;
    max-width: 1026px;
    margin-top: 30px;
    margin-left: auto;
    margin-right: auto;
}
.uiTabInPage .nav-tabs>li a {
    border:none;
    color: #578dc9;
    background-color: #fff;
    border: none;
    border-right: none;
} 
.uiTabInPage .nav-tabs>li a:hover {
    border:none;
    color: #578dc9;
    background-color: #fff;
    border: none;
    border-right: none;
} 
.uiTabInPage .nav-tabs>li.active a {
    background-color: #fff;
    color: #578dc9;
    border: none;
    border-right: none;
    border-bottom: 2px solid #578dc9;
}
.uiTabInPage .nav-tabs>li.active a:hover {
    background-color: #fff;
    border: none;
    border-right: none;
    border-bottom: 2px solid #578dc9;
}
.uiTabNormal .tab-content {
  border:none;
}
.uiTabNormal .nav-tabs>li:not(.active):last-child a, .uiTabInPage .nav-tabs>li:not(.active):last-child a{
  border: none;
  border-right: none;
  background-color:#fff;
}
</style>

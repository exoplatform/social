<template>
  <div id="UITabPane" class="uiTabNormal uiTabInPage">
    <ul class="nav nav-tabs">
      <li :class="[ activetab === 1 ? 'active' : '' ]" @click="activetab=1">
        <a data-target="#tab1" data-toggle="tab">Manage Spaces</a>
      </li>
      <li :class="[ activetab === 2 ? 'active' : '' ]" @click="activetab=2" >
        <a data-target="#tab2" data-toggle="tab">Permissions</a>
      </li>
    </ul> 
    <div class="tab-content">
      <div v-if="activetab === 1" id="tab1" class="tab-pane fade in active">
        <exo-space-administration-manage-spaces></exo-space-administration-manage-spaces>
      </div>
      <div v-if="activetab === 2" id="tab2" class="tab-pane fade in active">
        <h1>permissssssssssss</h1>
      </div>
    </div> 
  </div>
</template>

<script>
import ExoSpaceAdministrationManageSpaces from './ExoSpaceAdministrationManageSpaces.vue';
import ExoSpaceAdministrationSpacesPermissions from './ExoSpaceAdministrationSpacesPermissions.vue';

export default {
  components: {ExoSpaceAdministrationManageSpaces,ExoSpaceAdministrationSpacesPermissions},
  data() { 
    return {
      activetab: 1
    }
  }
};
</script>
<style>
.nav-tabs{
    background: #ffffff;
}
.nav-tabs > li{
    width: 185px;
    border-width: 2px;
}
.uiTabInPage{
    max-width: 782px;
    margin-top: 30px;
    margin-left: auto;
    margin-right: auto;
}
.uiTabNormal .nav-tabs>li a {
    border-color: #e1e8ee;
    color: #578dc9;
    background-color: #fff;
} 
.uiTabNormal .nav-tabs>li.active a{
    border-color: #e1e8ee;
    border-bottom-color: #578dc9;
    background-color: #fff;
}
.uiTabNormal .nav-tabs>.active>a{
    background-color: #fff;
    border-color: #e1e8ee;
    border-bottom-color: #578dc9;
    color: #578dc9;
}
.uiTabNormal .nav-tabs>.active>a{
    background-color: #fff;
    border-color: #e1e8ee;
    border-bottom-color: #578dc9;
    color: #578dc9;
}
.uiTabNormal .nav-tabs>li.active a {
    background-color: #fff;
    border-color: #e1e8ee;
    border-bottom-color: #578dc9;
}
.uiTabNormal .nav-tabs>li.active a:hover {
    background-color: #fff;
    border-color: #e1e8ee;
    border-bottom-color: #578dc9;
}
</style>

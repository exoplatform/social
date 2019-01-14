<template>
  <div class="spacesAdministration">
    <div class="uiTabNormal uiTabInPage">
      <ul class="nav nav-tabs">
        <li :class="{active: activeTab === 1}" @click="activeTab=1">
          <a data-toggle="tab">{{ $t('social.spaces.administration.manageSpaces') }}</a>
        </li>
        <li :class="{active: activeTab === 2}" @click="activeTab=2" >
          <a data-toggle="tab">{{ $t('social.spaces.administration.permissions') }}</a>
        </li>
      </ul> 
      <div class="tab-content">
        <div v-if="activeTab === 1" class="tab-pane fade in active">
          <exo-spaces-administration-manage-spaces></exo-spaces-administration-manage-spaces>
        </div>
        <div v-if="activeTab === 2" class="tab-pane fade in active">
          <exo-spaces-administration-manage-permissions></exo-spaces-administration-manage-permissions>
        </div>
      </div> 
    </div>
  </div>
</template>

<script>
export default {
  data() { 
    return {
      activeTab: 1
    };
  }
};
</script>


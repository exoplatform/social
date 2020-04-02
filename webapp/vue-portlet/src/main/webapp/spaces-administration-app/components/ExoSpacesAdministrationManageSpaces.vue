<template>
  <div class="manageSpaces">
    <div class="uiSearchInput">
      <input v-model="searchText" :placeholder="$t('social.spaces.administration.manageSpaces.search')" class="showInputSearch" type="text" @keyup.enter="searchSpaces()"/>
      <a v-exo-tooltip.bottom.body="Search" class="advancedSearch" href="#">
        <i class="uiIconPLF24x24Search" @click="searchSpaces()"></i>
      </a>
    </div>
    <table class="uiGrid table table-hover table-striped">
      <tr>          
        <th>
          {{ $t('social.spaces.administration.manageSpaces.space') }}
        </th>
        <th>
          {{ $t('social.spaces.administration.manageSpaces.description') }}
        </th>
        <th>
          {{ $t('social.spaces.administration.manageSpaces.visibility') }}
        </th>
        <th>
          {{ $t('social.spaces.administration.manageSpaces.registration') }}
        </th>
        <th>
          <span v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.users.tooltip')">
            {{ $t('social.spaces.administration.manageSpaces.users') }}
          </span>
        </th>
        <th>
          {{ $t('social.spaces.administration.manageSpaces.actions') }}
        </th>
      </tr>
      <tr v-if="spaces.length === 0"> 
        <td class="empty center" colspan="12"> {{ $t('social.spaces.administration.manageSpaces.noSpaces') }} </td>
      </tr>
      <tr v-for="(space, index) in spaces" :key="space.id">
        <td><img v-if="space.avatarUrl != null" :src="space.avatarUrl" class="avatar" /> <img v-else :src="avatar" class="avatar" />  {{ space.displayName }}</td>
        <td v-html="space.description"></td>
        <td class="center"> {{ $t('social.spaces.administration.manageSpaces.visibility.'+space.visibility) }} </td>
        <td class="center"> {{ $t('social.spaces.administration.manageSpaces.registration.'+space.subscription) }} </td>
        <td class="center"> {{ space.totalBoundUsers }}/{{ space.members.length }} </td>
        <td class="center actionContainer" >
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.actions.bind')" v-if="canBindGroupsAndSpaces" class="actionIcon" @click="openSpaceBindingDrawer(space, index)">
            <i :class="{'bound': space.hasBindings}" class="uiIconSpaceBinding uiIconGroup"></i>
          </a>
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.actions.edit')" :href="getSpaceLinkSetting(space.displayName,space.groupId)" class="actionIcon" target="_blank">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.actions.delete')" class="actionIcon" @click="deleteSpaceById(space.id, index)">
            <i class="uiIconDeleteUser uiIconLightGray"></i>
          </a>
        </td>
      </tr>
    </table> 
    <div v-if="totalPages > 1" class="pagination uiPageIterator">
      <ul class="pull-right">
        <li :class="{'disabled': currentPage === 1}">
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.pagination.previous')" href="#" @click="getSpacesPerPage(currentPage-1)">
            <i class="uiIconPrevArrow"></i>
          </a>
        </li>
        <li v-if="isInFirstPage">
          <a href="#" @click="getSpacesPerPage(1)">{{ 1 }}</a>
        </li>
        <li v-if="isInFirstPage">
          <a >
            <span>...</span>
          </a>
        </li>
        <li v-for="(page,i) in pages" :key="i" :class="{'active': currentPage === page.name}">
          <a href="#" @click="getSpacesPerPage(page.name)">{{ page.name }}</a>
        </li>
        <li v-if="isInLastPage">
          <a >
            <span>...</span>
          </a>
        </li>
        <li v-if="isInLastPage">
          <a href="#" @click="getSpacesPerPage(totalPages)">{{ totalPages }}</a>
        </li>
        <li :class="[currentPage === totalPages ? 'disabled': '' ]">
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.pagination.next')" href="#" @click="getSpacesPerPage(currentPage+1)">
            <i class="uiIconNextArrow"></i>
          </a>
        </li>
      </ul>
    </div> 
    <exo-modal v-show="showConfirmMessageModal" :title="$t('social.spaces.administration.delete.spaces.confirm.title')" @modal-closed="closeModal">
      <p>{{ $t('social.spaces.administration.delete.spaces.confirm') }}</p>
      <div class="uiAction uiActionBorder">
        <div class="btn btn-primary" @click="confirmDelete">{{ $t('social.spaces.administration.delete.spaces.button.delete') }}</div>
        <div class="btn" @click="closeModal">{{ $t('social.spaces.administration.delete.spaces.button.cancel') }}</div>
      </div>
    </exo-modal>
    <v-navigation-drawer
      id="GroupBindingDrawer"        
      v-model="showGroupBindingForm"
      absolute
      right
      stateless
      temporary
      width="500"
      max-width="100vw">
      <exo-group-binding-drawer :key="groupBindingDrawerKey" :group-space-bindings="groupSpaceBindings" :bound-groups-loading="bindingsLoading" :space-to-bind="spaceToBind" @close="closeGroupBindingDrawer" @openBindingModal="openBindingModal" @openRemoveBindingModal="openRemoveBindingModal" />
    </v-navigation-drawer>
    <exo-modal 
      v-show="showConfirmMessageBindingModal"
      :title="$t('social.spaces.administration.manageSpaces.spaceBindingForm.confirmation.title')"
      :display-close="false"
      class="bindingModal"
      @modal-closed="closeBindingModal">
      <p>{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.confirmation', {0: spaceName}) }}</p>
      <div class="uiAction uiActionBorder">
        <div class="btn btn-primary" @click="confirmBinding">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.confirmation.confirm') }}</div>
        <div class="btn" @click="closeBindingModal">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</div>
      </div>
    </exo-modal>
    <exo-modal
      v-show="showConfirmMessageRemoveBindingModal"
      :title="$t('social.spaces.administration.manageSpaces.spaceBindingForm.confirmation.title')"
      :display-close="false"
      class="bindingModal"
      @modal-closed="closeRemoveBindingModal">
      <p>{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.removeBinding.confirmation', {0: groupPrettyName}) }}</p>
      <div class="uiAction uiActionBorder">
        <div class="btn btn-primary" @click="confirmRemoveBinding">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.removeBinding.confirm') }}</div>
        <div class="btn" @click="closeRemoveBindingModal">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</div>
      </div>
    </exo-modal>
  </div>
</template>
<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';
import { spacesConstants } from '../../js/spacesConstants';

export default {
  props: {
    canBindGroupsAndSpaces: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showGroupBindingForm: false,
      showConfirmMessageModal: false,
      spaces: [],
      spaceName: '',
      spaceToBind: null,
      spaceToBindIndex: null,
      spaceToDeleteId: null,
      spaceToDeleteIndex: null,
      totalPages: 1,
      currentPage: 1,
      searchText: '',
      maxVisiblePagesButtons: 3,
      maxVisibleButtons: 5,
      groupBindingDrawerKey: 0,
      showConfirmMessageBindingModal : false,
      showConfirmMessageRemoveBindingModal: false,
      groupsToBind: [],
      groupSpaceBindings: [],
      bindingsLoading: true,
      binding: {},
      groupPrettyName: '',
      avatar : spacesConstants.DEFAULT_SPACE_AVATAR
    };
  },
  computed: {
    startPage() {
      if (this.currentPage === 1) {
        return 1;
      }
      if (this.currentPage === this.totalPages && this.totalPages > this.maxVisibleButtons){
        if(this.totalPages - this.maxVisiblePagesButtons +1 < 0) {
          return 1;
        }
        else {
          return this.totalPages - this.maxVisiblePagesButtons + 1;
        }
      }
      if(this.totalPages > this.maxVisibleButtons){
        if(this.totalPages - this.currentPage < this.maxVisibleButtons && this.totalPages - this.currentPage >= this.maxVisiblePagesButtons){
          return this.totalPages - this.maxVisibleButtons +1;
        }
        if(this.totalPages - this.currentPage < this.maxVisiblePagesButtons-1) {
          return this.currentPage-1;
        }
      }
      else{
        return 1;
      }
      return this.currentPage ;
    },
    endPage() {
      if( this.totalPages - this.startPage <= this.maxVisibleButtons -1 ){
        return this.totalPages;
      }
      else{
        return Math.min(this.startPage + this.maxVisiblePagesButtons - 1, this.totalPages);
      }
    },
    pages() {
      const range = [];
      for (let i = this.startPage; i <= this.endPage; i += 1) {
        range.push({
          name: i
        });
      }
      return range;
    },
    isInFirstPage() {
      return this.totalPages > this.maxVisibleButtons && this.totalPages - this.currentPage < this.maxVisiblePagesButtons;
    },
    isInLastPage() {
      return this.totalPages - this.currentPage >= this.maxVisibleButtons;
    },
  },
  created() {
    this.initSpaces();	
  },

  methods: {
    initSpaces() {
      spacesAdministrationServices.getSpaces().then(data =>{
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(data.size / spacesConstants.SPACES_PER_PAGE);
      });
    },
    getSpacesPerPage(currentPage) {
      const offset = ( currentPage - 1 ) * spacesConstants.SPACES_PER_PAGE;
      spacesAdministrationServices.getSpacesPerPage(offset).then(data =>{
        this.spaces = data.spaces;
        this.currentPage = currentPage;
      }); 
    },
    deleteSpaceById(id, index){
      this.showConfirmMessageModal = true;
      this.spaceToDeleteId = id;
      this.spaceToDeleteIndex = index;
    },
    confirmDelete(){
      spacesAdministrationServices.deleteSpaceById(this.spaceToDeleteId).then(() => {
        this.spaces.splice(this.spaceToDeleteIndex, 1);
      });
      this.showConfirmMessageModal = false;
    },
    getSpaceLinkSetting(spaceDisplayName,groupId){
      return spacesAdministrationServices.getSpaceLinkSetting(spaceDisplayName,groupId);
    },
    searchSpaces(){
      spacesAdministrationServices.searchSpaces(this.searchText).then(data => {
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(data.size / spacesConstants.SPACES_PER_PAGE);
      });
    },
    closeModal(){
      this.showConfirmMessageModal = false;
    },
    openSpaceBindingDrawer(space, index) {
      this.spaceToBind = space;
      this.spaceName = space.displayName;
      this.spaceToBindIndex = index;
      this.showGroupBindingForm = true;
      if (space.hasBindings) {
        spacesAdministrationServices.getGroupSpaceBindings(space.id).then(data => {
          this.groupSpaceBindings = data.groupSpaceBindings;
        }).finally(() => this.bindingsLoading = false);
      } else {
        this.bindingsLoading = false;
      }
    },
    closeGroupBindingDrawer() {
      this.showGroupBindingForm = false;
      this.groupSpaceBindings = [];
      this.forceRerender();
    },
    openBindingModal(groups) {
      this.groupsToBind = groups;
      this.showConfirmMessageBindingModal = true;
    },
    openRemoveBindingModal(binding) {
      this.binding = binding;
      this.groupPrettyName = this.renderGroupName(binding.group);
      this.showConfirmMessageRemoveBindingModal = true;
    },
    closeBindingModal() {
      this.showConfirmMessageBindingModal = false;
    },
    closeRemoveBindingModal() {
      this.showConfirmMessageRemoveBindingModal = false;
    },
    confirmBinding() {
      spacesAdministrationServices.saveGroupsSpaceBindings(this.spaceToBind.id, this.groupsToBind).finally(() => this.goToBindingReports());
      this.showConfirmMessageBindingModal = false;
    },
    confirmRemoveBinding() {
      spacesAdministrationServices.removeBinding(this.binding.id).finally(() => this.goToBindingReports());
      this.showConfirmMessageRemoveBindingModal = false;
    },
    goToBindingReports() {
      this.showGroupBindingForm = false;
      this.$emit('bindingReports');
      this.navigateTo('g/:platform:users/spacesAdministration#bindingReports');
      this.forceRerender();
    },
    navigateTo(pagelink) {
      location.href=`${ eXo.env.portal.context }/${ pagelink }` ;
    },
    renderGroupName(groupName) {
      let groupPrettyName = groupName.slice(groupName.lastIndexOf('/') + 1, groupName.length);
      groupPrettyName = groupPrettyName.charAt(0).toUpperCase() + groupPrettyName.slice(1);
      return `${groupPrettyName} (${groupName})`;
    },
    forceRerender() {
      this.groupBindingDrawerKey += 1;
    }
  }
};
</script>

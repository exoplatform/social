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
          {{ $t('social.spaces.administration.manageSpaces.actions') }}
        </th>
      </tr>
      <tr v-if="spaces.length === 0"> 
        <td class="empty center" colspan="12"> {{ $t('social.spaces.administration.manageSpaces.noSpaces') }} </td>
      </tr>
      <tr v-for="(space, index) in spaces" :key="space.id">
        <td><img v-if="space.avatarUrl != null" :src="space.avatarUrl" class="avatar" /> <img v-else :src="avatar" class="avatar" />  {{ space.displayName }}</td>
        <td>{{ space.description }}</td>
        <td class="center actionContainer" >
          <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.actions.edit')" :href="getSpaceLinkSetting(space.displayName)" class="actionIcon" target="_blank">
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
        <li v-for="i in totalPages" :key="i" :class="{'active': currentPage === i}">
          <a href="#" @click="getSpacesPerPage(i)">{{ i }}</a>
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
  </div>  
</template>
<script>
import * as spaceAdministrationServices from '../spaceAdministrationServices';
import * as spaceAdministrationConstants from '../spaceAdministrationConstants';

export default {
  data() {
    return {
      showConfirmMessageModal: false,
      spaces: [],
      spaceToDeleteId: null,
      spaceToDeleteIndex: null,
      totalPages: 1,
      currentPage: 1,
      searchText: '',
      avatar : spaceAdministrationConstants.spaceConstants.DEFAULT_SPACE_AVATAR
    };
  },
  created() {
    this.initSpaces();	
  },
  methods: {
    initSpaces() {
      spaceAdministrationServices.getSpaces().then(data =>{
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(data.size / spaceAdministrationConstants.spaceConstants.SPACES_PER_PAGE);
      });
    },
    getSpacesPerPage(currentPage) {
      const offset = ( currentPage - 1 ) * spaceAdministrationConstants.spaceConstants.SPACES_PER_PAGE;
      spaceAdministrationServices.getSpacesPerPage(offset).then(data =>{
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
      spaceAdministrationServices.deleteSpaceById(this.spaceToDeleteId).then(() => {
        this.spaces.splice(this.spaceToDeleteIndex, 1);
      });
      this.showConfirmMessageModal = false;
    },
    getSpaceLinkSetting(spaceDisplayName){
      return spaceAdministrationServices.getSpaceLinkSetting(spaceDisplayName);
    },
    searchSpaces(){
      spaceAdministrationServices.searchSpaces(this.searchText).then(data =>{
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(data.size / spaceAdministrationConstants.spaceConstants.SPACES_PER_PAGE);
      });
    },
    closeModal(){
      this.showConfirmMessageModal = false;
    }
  }
};
</script>


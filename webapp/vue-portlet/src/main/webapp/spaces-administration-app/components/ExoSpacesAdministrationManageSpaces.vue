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
        <td v-html="space.description"></td>
        <td class="center actionContainer" >
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
  </div>  
</template>
<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';
import { spacesConstants } from '../../js/spacesConstants';

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
      maxVisiblePagesButtons: 3,
      maxVisibleButtons: 5,
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
      spacesAdministrationServices.searchSpaces(this.searchText).then(data =>{
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(data.size / spacesConstants.SPACES_PER_PAGE);
      });
    },
    closeModal(){
      this.showConfirmMessageModal = false;
    }
  }
};
</script>


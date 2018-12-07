<template>
  <div>
    <div class="uiSearchInput">
      <input v-model="search" class="showInputSearch" type="text" placeholder="search here"/>
      <a data-original-title="Search" class="advancedSearch" rel="tooltip" data-placement="bottom" href="#">
        <i class="uiIconPLF24x24Search" @click="searchSpaces()"></i>
      </a>
    </div>
    <table class="uiGrid table table-hover table-striped">
      <tr>          
        <th>
          Space
        </th>
        <th>
          Description
        </th>
        <th>
          Actions
        </th>
      </tr>
      <tr v-if="spaces.length === 0"> 
        <td class="empty center" colspan="12"> Empty data </td>
      </tr>
      <tr v-for="(space, index) in spaces" :key="space.id">
        <td><img v-if="space.avatarUrl != null" :src="space.avatarUrl" class="avatar" /> <img v-else :src="avatar" class="avatar" />  {{ space.displayName }}</td>
        <td>{{ space.description }}</td>
        <td class="center actionContainer" >
          <a :href="getSpaceLinkSetting(space.displayName)" target="_blank" data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" >
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Delete" @click="deleteSpaceById(space.id, index)">
            <i class="uiIconDeleteUser uiIconLightGray"></i>
          </a>
        </td>
      </tr>
    </table> 
    <div v-if="totalPages > 1" class="pagination uiPageIterator">
      <ul>
        <li :class="{'disabled': currentPage === 1}">
          <a data-placement="bottom" rel="tooltip" href="#" data-original-title="Previous Page" @click="getSpacesPerPage(currentPage-1)">
            <i class="uiIconPrevArrow"></i>
          </a>
        </li>
        <li v-for="i in totalPages" :key="i">
          <a :class="{'active': currentPage === i}" href="#" @click="getSpacesPerPage(i)">{{ i }}</a>
        </li>
        <li :class="[currentPage === totalPages ? 'disabled': '' ]">
          <a data-placement="bottom" rel="tooltip" href="#" data-original-title="Next Page" @click="getSpacesPerPage(currentPage+1)">
            <i class="uiIconNextArrow"></i>
          </a>
        </li>
      </ul>
    </div> 
    <exo-modal v-show="showConfirmMessageModal" :title="ConfirmationDeleteMessage" @modal-closed="closeModal">
      <h3>{{ $t('exoplatform.modal.confirmation.delete') }}</h3>
      <div class="uiAction uiActionBorder">
        <div class="btn btn-primary" @click="confirmDelete">{{ $t('exoplatform.modal.button.delete') }}</div>
        <div class="btn" @click="closeModal">{{ $t('exoplatform.modal.button.annuler') }}</div>
      </div>
    </exo-modal>
  </div>  
</template>
<script>
import * as spaceAdministrationServices from '../spaceAdministrationServices';
import * as spaceAdministrationConstants from '../spaceAdministrationConstants';

import ExoModal from './modal/ExoModal.vue';

export default {
  components: {
    'exo-modal': ExoModal
  },
  data() {
    return {
      ConfirmationDeleteMessage: 'Confirmation',
      showConfirmMessageModal: false,
      spaces: [],
      id: null,
      index: null,
      totalPages: 1,
      offset: 0,
      currentPage: 1,
      search: '',
      avatar : spaceAdministrationConstants.spaceConstants.DEFAULT_SPACE_AVATAR
    }
  },
  created() {
    this.initSpaces();	
  },
  methods: {
    initSpaces() {
      spaceAdministrationServices.getSpaces().then(data =>{
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(data.size/spaceAdministrationConstants.spaceConstants.SPACES_PER_PAGE);
      });
    },
    getSpacesPerPage(currentPage) {
      this.offset = ( currentPage - 1 ) * spaceAdministrationConstants.spaceConstants.SPACES_PER_PAGE;
      spaceAdministrationServices.getSpacesPerPage(this.offset).then(data =>{
        this.spaces = data.spaces;
        this.currentPage = currentPage;
      }); 
    },
    deleteSpaceById(id, index){
      this.showConfirmMessageModal = true;
      this.id = id;
      this.index = index;
    },
    confirmDelete(){
      spaceAdministrationServices.deleteSpaceById(this.id).then(()=> {
        this.spaces.splice(this.index,1);
      });
      this.showConfirmMessageModal = false;
    },
    getSpaceLinkSetting(spaceDisplayName){
      return spaceAdministrationServices.getSpaceLinkSetting(spaceDisplayName);
    },
    searchSpaces(){
      spaceAdministrationServices.searchSpaces(this.search).then(data =>{
        this.spaces = data.spaces;
        this.totalPages = Math.ceil(this.spaces.length/spaceAdministrationConstants.spaceConstants.SPACES_PER_PAGE);
      });
    },
    closeModal(){
      this.showConfirmMessageModal = false;
    }
  }
}
</script>

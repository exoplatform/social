<template>
  <div>
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
      <tr v-for="(space, index) in spaces" :key="space.id">
        <td><img :src="space.avatarUrl" class="avatar"/>  {{ space.displayName }}</td>
        <td>{{ space.description }}</td>
        <td class="center actionContainer">
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Delete" @click="deleteSpaceById(space.id, index)">
            <i class="uiIconDeleteUser uiIconLightGray"></i>
          </a>
        </td>
      </tr>
    </table>  
    <exo-modal v-show="showConfirmMessageModal" :title="ConfirmationDeleteMessage" @modal-closed="closeModal">
      <h3>Souhaitez vous supprimer cet espace</h3>
      <div class="uiAction uiActionBorder">
        <div class="btn btn-primary" @click="confirmDelete">Delete</div>
        <div class="btn" @click="closeModal">Annuler</div>
      </div>
    </exo-modal>
  </div>  
</template>
<script>
import * as spaceAdministrationServices from '../spaceAdministrationServices';

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
      index: null
    }
  },
  created() {
    this.fetchData();	
  },
  methods: {
    fetchData() {
      spaceAdministrationServices.getSpaces().then(data =>{
        this.spaces = data.spaces;
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
    closeModal(){
      this.showConfirmMessageModal = false;
    }
  }
}
</script>
<style>
.avatar{
  width: 20px;
}
.table tr td {
  width: 50px;
}
</style>

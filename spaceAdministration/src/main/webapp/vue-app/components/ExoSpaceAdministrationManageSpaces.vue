<template>
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
</template>
<script>
import * as spaceAdministrationServices from '../spaceAdministrationServices';

export default {
  data() {
    return {
      spaces: []
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
      spaceAdministrationServices.deleteSpaceById(id).then(()=> {
        this.spaces.splice(index,1);
      });
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

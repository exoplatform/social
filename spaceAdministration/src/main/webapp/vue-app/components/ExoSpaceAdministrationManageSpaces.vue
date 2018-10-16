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
    <tr v-for="space in spaces" :key="space.id">
      <td><img :src="space.avatarUrl" class="avatar"/>  {{ space.displayName }}</td>
      <td>{{ space.description }}</td>
      <td class="center actionContainer">
        <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit">
          <i class="uiIconEdit uiIconLightGray"></i>
        </a>
        <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Delete">
          <i class="uiIconDeleteUser uiIconLightGray"></i>
        </a>
      </td>
    </tr>
  </table>  
</template>
<script>
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
      fetch('/rest/v1/social/spaces', {credentials: 'include'}).then(response => response.json()).then(data =>{
        console.log("cccc",data.spaces)
        this.spaces = data.spaces;
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

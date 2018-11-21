<template>
  <div>
    <table class="uiGrid table table-hover table-striped">
      <tr>          
        <th>
          Permissions
        </th>
        <th>
          User or Group
        </th>
        <th>
          Actions
        </th>
      </tr>
      <tr>
        <td><h2>Create spaces</h2> <h5>Ability to create spaces</h5></td>
        <td>
          <h3 v-if="displayEditCreate === 1">test</h3>
          <div v-if="displayEditCreate === 0" class="selectize-input items not-full has-options">
            <input id="add-user-suggestor" type="text" autocomplete="off" style="width: 121.047px; opacity: 1; position: relative; left: 0px;">
          </div>
        </td>
        <td v-if="displayEditCreate === 1" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editCreateSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="displayEditCreate === 0" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="deleteSpaceById(space.id, index)">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editCreateSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>
      </tr>
      <tr>
        <td><h2>Manage spaces</h2> <h5>Ability to edit spaces</h5></td>
        <td><h3>test</h3></td>
        <td v-if="displayEditManage === 1" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editManageSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="displayEditManage === 0" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="deleteSpaceById(space.id, index)">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editManageSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>
      </tr>
    </table>
  </div>
</template>
<script>
export default {
  data() {
    return {
      displayEditCreate: 1,
      displayEditManage: 1
    }
  },
  methods: {
    editCreateSpace(){
      if(this.displayEditCreate === 1) {
        this.displayEditCreate = 0;
      } else {
        this.displayEditCreate = 1;
      }
    },
    editManageSpace(){
      if(this.displayEditManage === 1) {
        this.displayEditManage = 0;
      } else {
        this.displayEditManage = 1;
      }
    }
  }
}
</script>

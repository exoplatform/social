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
          <h4 v-if="displayEditCreate === 1">test</h4>
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
        <td><h4>test</h4></td>
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
import * as spaceAdministrationServices from '../spaceAdministrationServices';
export default {
  data() {
    return {
      guests: [],
      displayEditCreate: 1,
      displayEditManage: 1
    }
  },
  created() {
    this.initSuggester();	
  },
  methods: {
    initSuggester() {
      const $guestsFormsSuggestor = $('add-user-suggestor');
      const component = this;
      const suggesterData = {
        type: 'tag',
        create: false,
        createOnBlur: false,
        highlight: false,
        openOnFocus: false,
        sourceProviders: ['exo:social'],
        valueField: 'name',
        labelField: 'fullname',
        searchField: ['fullname', 'name'],
        closeAfterSelect: true,
        dropdownParent: 'body',
        hideSelected: true,
        renderMenuItem (item, escape) {
          return component.renderMenuItem(item, escape);
        },
        renderItem(item) {
          return `<span class="hidden" data-value="${item.value}"></span>`;
        },
        onItemAdd(item) {
          component.addSuggestedItem(item);
        }
      };
      $guestsFormsSuggestor.suggester(suggesterData);
    },   
    renderMenuItem (item, escape) {
      const avatar = spaceAdministrationServices.getAvatar(item.name);
      const defaultAvatar = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
      return `
        <div class="avatar">
          <img src="${avatar}" onerror="this.src='${defaultAvatar}'">
        </div>
        <div class="user-name">${escape(item.fullname)} (${item.name})</div>
      `;
    },
    addSuggestedItem(item) {
      if($('#add-user-suggestor') && $('#add-user-suggestor').length && $('#add-user-suggestor')[0].selectize) {
        const selectize = $('#add-user-suggestor')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.guests.find(guest => guest.name === item.name)) {
        this.guests.push(item);
      }
    },
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

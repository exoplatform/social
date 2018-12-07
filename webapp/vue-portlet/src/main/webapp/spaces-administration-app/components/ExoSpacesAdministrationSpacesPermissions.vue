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
          <h4 v-show="displayEditCreate === 1">{{ permissionAdministrators }}</h4>
          <div v-show="displayEditCreate === 0" class="inputUser">
            <input id="add-user-suggestor" type="text"/>
          </div>
        </td>
        <td v-if="displayEditCreate === 1" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editCreateSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="displayEditCreate === 0" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissions()">
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
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissions()">
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
      displayEditManage: 1,
      index: 0,
      settingValue: '',
      permissionAdministrators:'No Assignment'
    }
  },
  created() {
    this.getsSettingValue();
  },
  methods: {
    initSuggester() {
      const $guestsFormsSuggestor = $('#add-user-suggestor');
      if($guestsFormsSuggestor && $guestsFormsSuggestor.length && $guestsFormsSuggestor.suggester) {
        const component = this;
        const suggesterData = {
          type: 'tag',
          plugins: ['remove_button', 'restore_on_backspace'],
          create: false,
          createOnBlur: false,
          highlight: false,
          openOnFocus: false,
          sourceProviders: ['exo:social'],
          valueField: 'text',
          labelField: 'text',
          searchField: ['text'],
          closeAfterSelect: true,
          dropdownParent: 'body',
          hideSelected: true,
          renderMenuItem (item, escape) {
            return component.renderMenuItem(item, escape);
          },
          renderItem(item) {
            return `<div class="item">${item.text}</div>`;
          },
          onItemAdd(item) {
            component.addSuggestedItem(item);
          },
          onItemRemove(item) {
            component.removeSuggestedItem(item);
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:social': component.findGuests
          }
        };
        $guestsFormsSuggestor.suggester(suggesterData);
        if(this.permissionAdministrators && this.permissionAdministrators !== 'No Assignment') {
          const permissions = this.permissionAdministrators.split(',');  
          for(const permission of permissions) {
            $guestsFormsSuggestor[0].selectize.addOption({text: permission});
            $guestsFormsSuggestor[0].selectize.addItem(permission);
          }
        }      
      }
    },
    findGuests (query, callback) {
      if (!query.length) {
        return callback(); 
      }
      spaceAdministrationServices.getGuests(query).then(data => {
        spaceAdministrationServices.getGuestsGroups(query).then(group => {
          if(group){
            if(data.options != null) {
              group = group.concat(data.options);
            }
            callback(group);
          } else {
            if(data.options != null) {
              callback(data.options);
            }
          }
        });
      });
    },   
    renderMenuItem (item, escape) {
      if(item.avatarUrl == null) {
        item.avatarUrl = spaceAdministrationServices.getAvatar(item.value);
        if (item.type === 'user') {
          item.avatarUrl = '/eXoSkin/skin/images/system/UserAvtDefault.png';
        } else {
          item.avatarUrl = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
        }
      }
      return `
        <div class="item"> <img class="avatarMini" src="${item.avatarUrl}"> ${escape(item.text)}</div>
      `;
    },
    addSuggestedItem(item) {
      if($('#add-user-suggestor') && $('#add-user-suggestor').length && $('#add-user-suggestor')[0].selectize) {
        const selectize = $('#add-user-suggestor')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.guests.find(guest => guest.text === item.text)) {
        this.guests.push(item.text);
      }
    },
    removeSuggestedItem(item) {
      if(this.guests.find(guest => guest === item)) {
        this.guests.splice(this.guests.indexOf(item), 1);
      }
    },
    savePermissions() {
      this.settingValue = this.guests.join();
      if(this.guests){
        spaceAdministrationServices.createSetting('GLOBAL','GLOBAL','exo:social_spaces_administrators',this.settingValue);
      }
      this.getsSettingValue();
      this.editCreateSpace();
    },
    getsSettingValue(){
      spaceAdministrationServices.getsSettingValue('GLOBAL','GLOBAL','exo:social_spaces_administrators').then(data => {
        if(data) {
          this.permissionAdministrators = data.value;
        }
      });
    },
    editCreateSpace(){
      if(this.displayEditCreate === 1) {
        this.displayEditCreate = 0;
        this.initSuggester();
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




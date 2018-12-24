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
          <div v-show="displayEditManage !== 0">
            <div v-if="managers.length > 0">
              <div v-for="manager in managers" :key="manager">
                <h4 v-if="manager.startsWith('*/')"> Group: {{ manager }}</h4>
                <h4 v-else-if="!manager.startsWith('No assign') && manager !== ''"> User: {{ manager }}</h4>
                <h4 v-else>No assignement</h4>
              </div>
            </div>
            <h4 v-if="managers.length === 0">No assignement</h4>
          </div>
          <div v-show="displayEditManage === 0" class="inputUser">
            <input id="add-guest-suggestor" type="text"/>
          </div>
        </td>
        <td v-if="displayEditManage === 1" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editManageSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="displayEditManage === 0" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissionsManageSpace()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editManageSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>       
      </tr>
      <tr>
        <td><h2>Manage spaces</h2> <h5>Ability to edit spaces</h5></td>
        <td>
          <div v-show="displayEditCreate !== 0">
            <div v-if="guests.length > 0">
              <div v-for="guest in guests" :key="guest">
                <h4 v-if="guest.startsWith('*/')"> Group: {{ guest }}</h4>
                <h4 v-else-if="!guest.startsWith('No assign')"> User: {{ guest }}</h4>
                <h4 v-else>No assignement</h4>
              </div>
            </div>
            <h4 v-if="guests.length === 0">No assignement</h4>
          </div>
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
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissionsCreateSpace()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editCreateSpace()">
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
      managers: [],
      displayEditCreate: 1,
      displayEditManage: 1,
      index: 0,
      settingValue: '',
      permissionAdministrators:'No Assignment',
      permissionManagers:'No Assignment'
    }
  },
  created() {
    this.initSuggester();
    this.initSuggesterManageSpace();
    this.getSettingValueCreateSpace();
    this.getSettingValueManageSpace();
  },
  methods: {
    initSuggesterManageSpace() {
      const $guestsFormsSuggestor = $('#add-guest-suggestor');
      if($guestsFormsSuggestor && $guestsFormsSuggestor.length && $guestsFormsSuggestor.suggester) {
        const component = this;
        const suggesterData = {
          type: 'tag',
          plugins: ['remove_button', 'restore_on_backspace'],
          create: false,
          createOnBlur: false,
          highlight: false,
          openOnFocus: false,
          sourceProviders: ['exo:spacesAdministration'],
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
            component.addSuggestedItemManage(item);
          },
          onItemRemove(item) {
            component.removeSuggestedItemManage(item);
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:spacesAdministration': component.findGuests
          }
        };
        $guestsFormsSuggestor.suggester(suggesterData);
        if(this.permissionManagers && this.permissionManagers !== 'No Assignment') {
          const permissions = this.permissionManagers.split(',');  
          for(const permission of permissions) {
            $guestsFormsSuggestor[0].selectize.addOption({text: permission});
            $guestsFormsSuggestor[0].selectize.addItem(permission);
          }
        }      
      }
    },
    addSuggestedItemManage(item) {
      if($('#add-guest-suggestor') && $('#add-guest-suggestor').length && $('#add-guest-suggestor')[0].selectize) {
        const selectize = $('#add-guest-suggestor')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.managers.find(manager => manager.text === item.text)) {
        this.managers.push(item.text);
      }
    },
    removeSuggestedItemManage(item) {
      if(this.managers.find(manager => manager === item)) {
        this.managers.splice(this.managers.indexOf(item), 1);
      }
    },
    savePermissionsManageSpace() {
      this.settingValue = this.managers.join();
      if(this.managers){
        spaceAdministrationServices.createSetting('GLOBAL','GLOBAL','exo:social_spaces_creators',this.settingValue);
      }
      this.displayEditManage = 1;
    },
    getSettingValueManageSpace() {
      spaceAdministrationServices.getsSettingValue('GLOBAL','GLOBAL','exo:social_spaces_creators').then(data => {
        if(data && data.value !== '') {
          this.permissionManagers = data.value;
        }
        this.initSuggesterManageSpace();
      });
    },
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
          sourceProviders: ['exo:spacesAdministration'],
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
            'exo:spacesAdministration': component.findGuests
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
      Promise.all([
        spaceAdministrationServices.getGuests(query), 
        spaceAdministrationServices.getGuestsGroups(query) 
      ])
      .then(function (result) {
        console.log(' resultPromise '+result)  
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
        <div class="item"> <img class="avatarMini" src="${item.avatarUrl}"> ${escape(item.value)}</div>
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
    savePermissionsCreateSpace() {
      this.settingValue = this.guests.join();
      if(this.guests){
        spaceAdministrationServices.createSetting('GLOBAL','GLOBAL','exo:social_spaces_administrators',this.settingValue);
      }
      this.displayEditCreate = 1;
    },
    getSettingValueCreateSpace(){
      spaceAdministrationServices.getsSettingValue('GLOBAL','GLOBAL','exo:social_spaces_administrators').then(data => {
        if(data && data.value !== '') {
          this.permissionAdministrators = data.value;
        }
        this.initSuggester();
      });
    },
    editCreateSpace(){
      if(this.displayEditCreate === 1) {
        this.displayEditCreate = 0;
      } else {
        this.getSettingValueCreateSpace();
        this.displayEditCreate = 1;
        this.displayEditManage = 1;
      }
    },
    editManageSpace(){
      if(this.displayEditManage === 1) {
        this.displayEditManage = 0;
      } else {
        this.getSettingValueManageSpace();
        this.displayEditManage = 1;
        this.displayEditCreate = 1;
      }
    }
  }
}
</script>




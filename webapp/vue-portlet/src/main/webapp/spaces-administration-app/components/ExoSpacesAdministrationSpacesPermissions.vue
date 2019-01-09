<template>
  <div>
    <table class="uiGrid table table-hover table-striped">
      <tr>          
        <th>
          {{ $t('exoplatform.permission.spaces.permissions') }}
        </th>
        <th>
          {{ $t('exoplatform.permission.spaces.userOrGroup') }}
        </th>
        <th>
          {{ $t('exoplatform.manage.spaces.actions') }}
        </th>
      </tr>
      <tr>
        <td><h5 class="title">{{ $t('exoplatform.permission.spaces.createSpace') }}</h5> <h5>{{ $t('exoplatform.permission.spaces.descriptionCreateSpace') }}</h5></td>
        <td>
          <div v-show="spacesCreatorsEditMode">
            <div v-if="creators.length > 0">
              <div v-for="creator in creators" :key="creator">
                <h5 v-if="creator.startsWith('*:/')"> {{ $t('exoplatform.permission.spaces.group') }}: {{ creator }}</h5>
                <h5 v-else-if="!creator.startsWith('No assign') && creator !== ''"> {{ $t('exoplatform.permission.spaces.user') }}: {{ creator }}</h5>
                <h5 v-else>{{ $t('exoplatform.permission.spaces.noAssignement') }}</h5>
              </div>
            </div>
            <h5 v-if="creators.length === 0">{{ $t('exoplatform.permission.spaces.noAssignement') }}</h5>
          </div>
          <div v-show="!spacesCreatorsEditMode" class="inputUser">
            <input id="add-creators-suggester" type="text"/>
          </div>
        </td>
        <td v-if="spacesCreatorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editCreateSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="!spacesCreatorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissionsCreateSpace()">
            <i class="uiIconSave uiIconLightGray"></i>
          </a>
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Close" @click="editCreateSpace()">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </td>       
      </tr>
      <tr>
        <td><h5 class="title">{{ $t('exoplatform.permission.spaces.manageSpace') }}</h5> <h5>{{ $t('exoplatform.permission.spaces.descriptionManageSpace') }}</h5></td>
        <td>
          <div v-show="spacesAdministratorsEditMode">
            <div v-if="administrators.length > 0">
              <div v-for="administrator in administrators" :key="administrator">
                <h5 v-if="administrator.startsWith('*:/')"> {{ $t('exoplatform.permission.spaces.group') }}: {{ administrator }}</h5>
                <h5 v-else-if="!administrator.startsWith('No assign')"> {{ $t('exoplatform.permission.spaces.user') }}: {{ administrator }}</h5>
                <h5 v-else>{{ $t('exoplatform.permission.spaces.noAssignement') }}</h5>
              </div>
            </div>
            <h5 v-if="administrators.length === 0">{{ $t('exoplatform.permission.spaces.noAssignement') }}</h5>
          </div>
          <div v-show="!spacesAdministratorsEditMode" class="inputUser">
            <input id="add-administrators-suggester" type="text"/>
          </div>
        </td>
        <td v-if="spacesAdministratorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Edit" @click="editManageSpace()">
            <i class="uiIconEdit uiIconLightGray"></i>
          </a>
        </td>
        <td v-if="!spacesAdministratorsEditMode" class="center actionContainer" >
          <a data-placement="bottom" rel="tooltip" class="actionIcon" data-original-title="Save" @click="savePermissionsSpacesAdministrators()">
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
      creators: [],
      administrators: [],
      spacesCreatorsEditMode: true,
      spacesAdministratorsEditMode: true,
      index: 0,
      settingValue: '',
      creatorsPermissions: null,
      administratorsPermissions: null
    };
  },
  created() {
    this.initSuggesterSpacesCreators();
    this.initSuggesterSpacesAdministrators();
    this.getSettingValueCreateSpace();
    this.getSettingValueSpaceAdministrators();
  },
  methods: {
    initSuggesterSpacesCreators() {
      const suggesterContainer = $('#add-creators-suggester');
      if(suggesterContainer && suggesterContainer.length && suggesterContainer.suggester) {
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
            component.addSuggestedItemCreate(item);
          },
          onItemRemove(item) {
            component.removeSuggestedItemManage(item);
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:spacesAdministration': component.findUsersAndGroups
          }
        };
        suggesterContainer.suggester(suggesterData);
        if(this.creatorsPermissions && this.creatorsPermissions !== null) {
          for(const permission of this.creatorsPermissions) {
            const permissionExpression = `${permission.membershipType}:${permission.group}`;
            suggesterContainer[0].selectize.addOption({text: permissionExpression});
            suggesterContainer[0].selectize.addItem(permissionExpression);
          }
        }
      }
    },
    addSuggestedItemCreate(item) {
      if($('#add-creators-suggester') && $('#add-creators-suggester').length && $('#add-creators-suggester')[0].selectize) {
        const selectize = $('#add-creators-suggester')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.creators.find(creator => creator.text === item.text)) {
        this.creators.push(item.text);
      }
    },
    removeSuggestedItemCreate(item) {
      if(this.creators.find(creator => creator === item)) {
        this.creators.splice(this.creators.indexOf(item), 1);
      }
    },
    savePermissionsCreateSpace() {
      if(this.creators){
        spaceAdministrationServices.updateSpacesAdministrationSetting('spacesCreators',
          this.creators.map(creator => {
            const splitCreator = creator.split(':');
            return { 'membershipType': splitCreator[0], 'group': splitCreator[1] };
          }));
      }
      this.spacesCreatorsEditMode = true;
    },
    getSettingValueCreateSpace() {
      spaceAdministrationServices.getSpacesAdministrationSetting('spacesCreators').then(data => {
        if(data) {
          this.creatorsPermissions = data.memberships;
        }
        this.initSuggesterSpacesCreators();
      });
    },
    initSuggesterSpacesAdministrators() {
      const suggesterContainer = $('#add-administrators-suggester');
      if(suggesterContainer && suggesterContainer.length && suggesterContainer.suggester) {
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
            'exo:spacesAdministration': component.findUsersAndGroups
          }
        };
        suggesterContainer.suggester(suggesterData);
        if(this.administratorsPermissions && this.administratorsPermissions !== null) {
          for(const permission of this.administratorsPermissions) {
            const permissionExpression = `${permission.membershipType}:${permission.group}`;
            suggesterContainer[0].selectize.addOption({text: permissionExpression});
            suggesterContainer[0].selectize.addItem(permissionExpression);
          }
        }      
      }
    },
    findUsersAndGroups (query, callback) {
      if (!query.length) {
        return callback(); 
      }
      const promises = [];     
      promises.push(spaceAdministrationServices.getUsers(query));
      promises.push(spaceAdministrationServices.getGroups(query));

      Promise.all(promises).then( data => {
        const users = data[0];
        const groups = data[1];
        const usersAndGroups = users && users.options || [];
        for(const group of groups) {
          usersAndGroups.push({
            avatarUrl: null,
            text: `*:${group.id}`,
            value: `*:${group.id}`,
            type: 'group'
          });
        }
        callback(usersAndGroups);
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
      if($('#add-administrators-suggester') && $('#add-administrators-suggester').length && $('#add-administrators-suggester')[0].selectize) {
        const selectize = $('#add-administrators-suggester')[0].selectize;
        item = selectize.options[item];
      }
      if(!this.administrators.find(administrator => administrator.text === item.text)) {
        this.administrators.push(item.text);
      }
    },
    removeSuggestedItem(item) {
      if(this.administrators.find(administrator => administrator === item)) {
        this.administrators.splice(this.administrators.indexOf(item), 1);
      }
    },
    savePermissionsSpacesAdministrators() {
      if(this.administrators){
        spaceAdministrationServices.updateSpacesAdministrationSetting('spacesAdministrators',
          this.administrators.map(administrator => {
            const splitAdministrators = administrator.split(':');
            return { 'membershipType': splitAdministrators[0], 'group': splitAdministrators[1] };
          }));
      }
      this.spacesAdministratorsEditMode = true;
    },
    getSettingValueSpaceAdministrators(){
      spaceAdministrationServices.getSpacesAdministrationSetting('spacesAdministrators').then(data => {
        if(data) {
          this.administratorsPermissions = data.memberships;
        }
        this.initSuggesterSpacesAdministrators();
      });
    },
    editCreateSpace(){
      if(this.spacesCreatorsEditMode) {
        this.spacesCreatorsEditMode = false;
      } else {
        this.getSettingValueCreateSpace();
        this.spacesCreatorsEditMode = true;
        this.spacesAdministratorsEditMode = true;
      }
    },
    editManageSpace(){
      if(this.spacesAdministratorsEditMode) {
        this.spacesAdministratorsEditMode = false;
      } else {
        this.getSettingValueSpaceAdministrators();
        this.spacesAdministratorsEditMode = true;
        this.spacesCreatorsEditMode = true;
      }
    }
  }
};
</script>




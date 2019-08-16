<template>
  <div class="modal-mask uiPopupWrapper">
    <div :class="modalClass" class="uiPopup modal-spacebinding">
      <div class="popupHeader">
        <span class="PopupTitle popupTitle">{{ title }}</span>
        <a class="uiIconClose pull-right" @click="closeModal"></a>
      </div>
      <div class="PopupContent popupContent">
        <table class="uiGrid table table-hover table-striped">
          <tr>
            <th>
              {{ $t('social.spaces.administration.binding.spacerole') }}
            </th>
            <th>
              {{ $t('social.spaces.administration.binding.groupmapping') }}
            </th>
          </tr>
          <tr>
            <td>
              <h5 class="title">{{ $t('social.spaces.administration.binding.manager') }}</h5>
              <h5>{{ $t('social.spaces.administration.binding.descriptionManager') }}</h5>
            </td>
            <td>
              <table>
                <tr v-for="(binding, index) in bindingsManager" :key="binding">
                  <div>
                    <div class="uiBindingInput">
                      <h5 v-exo-tooltip.bottom.body="binding.groupRole+':'+binding.group" v-show="index != indexBindingManagerEditing"><b>{{ binding.groupRole }} in {{ binding.group }}</b></h5>
                      <div v-show="index == indexBindingManagerEditing" class="inputUser">
                        <span class="uiSelectbox">
                          <select v-model="binding.groupRole" class="selectbox" name="template">
                            <option v-for="(membership) in memberships" :key="membership" :value="membership">{{ membership }}</option>
                          </select>
                        </span>
                        <input :id="`group-suggester-manager${index}`" class="uiGroupBindingInput" type="text"/>
                      </div>
                    </div>
                    <div class="uiBindingAction">
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.edit')" v-show="index != indexBindingManagerEditing" class="actionIcon" @click="editBinding(index,'manager')">
                        <i class="uiIconEdit uiIconLightGray"></i>
                      </a>
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.save')" v-show="index == indexBindingManagerEditing" class="actionIcon" @click="saveBinding(index,'manager')">
                        <i class="uiIconSave uiIconLightGray"></i>
                      </a>
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.cancel')" v-show="index == indexBindingManagerEditing" class="actionIcon" @click="cancelBinding(index,'manager')">
                        <i class="uiIconClose uiIconLightGray"></i>
                      </a>
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.delete')" v-show="index != indexBindingManagerEditing" class="actionIcon" @click="deleteBinding(index,'manager')">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </a>
                    </div>
                  </div>
                </tr>
              </table>
              <div v-show="indexBindingManagerEditing == -1" class="center actionContainer" >
                <div class="btn" @click="addBinding('manager')">{{ $t('social.spaces.administration.binding.actions.addBinding') }}</div>
              </div>
            </td>
          </tr>
          <tr>
            <td>
              <h5 class="title">{{ $t('social.spaces.administration.binding.member') }}</h5>
              <h5>{{ $t('social.spaces.administration.binding.descriptionMember') }}</h5>
            </td>
            <td>
              <table>
                <tr v-for="(binding, index) in bindingsMember" :key="binding">
                  <div>
                    <div class="uiBindingInput">
                      <h5 v-exo-tooltip.bottom.body="binding.groupRole+':'+binding.group" v-show="index != indexBindingMemberEditing"><b>{{ binding.groupRole }} in {{ binding.group }}</b></h5>
                      <div v-show="index == indexBindingMemberEditing" class="inputUser">
                        <span class="uiSelectbox">
                          <select v-model="binding.groupRole" class="selectbox" name="template">
                            <option v-for="(membership) in memberships" :key="membership" :value="membership">{{ membership }}</option>
                          </select>
                        </span>
                        <input :id="`group-suggester-member${index}`" class="uiGroupBindingInput" type="text"/>
                      </div>
                    </div>
                    <div class="uiBindingAction">
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.edit')" v-show="index != indexBindingMemberEditing" class="actionIcon" @click="editBinding(index,'member')">
                        <i class="uiIconEdit uiIconLightGray"></i>
                      </a>
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.save')" v-show="index == indexBindingMemberEditing" class="actionIcon" @click="saveBinding(index,'member')">
                        <i class="uiIconSave uiIconLightGray"></i>
                      </a>
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.cancel')" v-show="index == indexBindingMemberEditing" class="actionIcon" @click="cancelBinding(index,'member')">
                        <i class="uiIconClose uiIconLightGray"></i>
                      </a>
                      <a v-exo-tooltip.bottom.body="$t('social.spaces.administration.binding.actions.delete')" v-show="index != indexBindingMemberEditing" class="actionIcon" @click="deleteBinding(index,'member')">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </a>
                    </div>
                  </div>
                </tr>
              </table>
              <div v-show="indexBindingMemberEditing == -1" class="center actionContainer" >
                <div class="btn" @click="addBinding('member')">{{ $t('social.spaces.administration.binding.actions.addBinding') }}</div>
              </div>
            </td>
          </tr>
        </table>
        <div class="uiAction uiActionBorder">
          <div class="btn btn-primary" @click="saveBindings">{{ $t('social.spaces.administration.binding.modal.save') }}</div>
          <div class="btn" @click="closeModal">{{ $t('social.spaces.administration.binding.modal.cancel') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../../spacesAdministrationServices';
export default {
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  data(){
    return{
      indexBindingManagerEditing : -1,
      indexBindingMemberEditing : -1,
      groupBindingManagerEditing : '',
      groupBindingMemberEditing : '',
      roleBindingManagerEditing : '',
      roleBindingMemberEditing : '',
      memberships : ['*','member','manager'],
      bindingsManager:[{
        id:'-1',
        group:'/platform/administrators',
        groupRole:'*',
        spaceRole:'manager',
        space:'1',
      },
      {
        id:'-1',
        group:'/organization/employees',
        groupRole:'*',
        spaceRole:'manager',
        space:'1',
      }],
      bindingsMember:[{
        id:'-1',
        group:'/platform/users',
        groupRole:'member',
        spaceRole:'member',
        space:'1'
      },
      {
        id:'-1',
        group:'/platform/administrators',
        groupRole:'member',
        spaceRole:'member',
        space:'1'
      }
      ]
    };
  },
  methods: {
    closeModal() {
      // Emit the click event of close icon
      this.$emit('modal-closed');
    },
    initSuggesterGroup(index,type) {
      const suggesterContainer = $(`#group-suggester-${type}${index}`);
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
          maxItems: 1,
          mode: 'multi',
          allowEmptyOption : false,
          renderMenuItem (item, escape) {
            return component.renderMenuItem(item, escape);
          },
          renderItem(item) {
            return `<div class="item">${item.text}</div>`;
          },
          onItemAdd(item) {
            component.addSuggestedGroup(item,index,type);
          },
          onItemRemove(item) {
            component.removeSuggestedGroup(item,index,type);
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:spacesAdministration': component.findGroups
          }
        };
        suggesterContainer.suggester(suggesterData);
        suggesterContainer[0].selectize.clear();
        let currentGroup = '';
        if (type === 'manager') { currentGroup = this.bindingsManager[this.indexBindingManagerEditing].group;}
        else { currentGroup = this.bindingsMember[this.indexBindingMemberEditing].group;}
        suggesterContainer[0].selectize.addOption({text: currentGroup});
        suggesterContainer[0].selectize.addItem(currentGroup);
      }
    },
    findGroups (query, callback) {
      if (!query.length) {
        return callback();
      }
      spacesAdministrationServices.getGroups(query).then(data => {
        const groups = [];
        for(const group of data) {
          groups.push({
            avatarUrl: null,
            text: `${group.id}`,
            value: `${group.id}`,
            type: 'group'
          });
        }
        callback(groups);
      });
    },
    renderMenuItem (item, escape) {
      return `
       <div class="item">${escape(item.value)}</div>
      `;
    },
    addSuggestedGroup(item,index,type) {
      if($(`#group-suggester-${type}${index}`) && $(`#group-suggester-${type}${index}`).length && $(`#group-suggester-${type}${index}`)[0].selectize) {
        const selectize = $(`#group-suggester-${type}${index}`)[0].selectize;
        item = selectize.options[item];
      }
      if (type === 'manager') {this.bindingsManager[this.indexBindingManagerEditing].group = item.text;}
      else {this.bindingsMember[this.indexBindingMemberEditing].group = item.text;}
    },
    removeSuggestedGroup(item,index,type) {
      const suggesterContainer = $(`#group-suggester-${type}${index}`);
      if (type === 'manager') {this.bindingsManager[this.indexBindingManagerEditing].group = '';}
      else {this.bindingsMember[this.indexBindingMemberEditing].group = '';}
      suggesterContainer[0].selectize.removeOption(item);
      suggesterContainer[0].selectize.removeItem(item);
    },
    saveBindings(){
      // Emit the click event of close icon
      this.$emit('modal-closed');
    },
    editBinding: function(index,type) {
      if (type === 'member')
      {
        this.indexBindingMemberEditing = index;
        this.groupBindingMemberEditing = this.bindingsMember[this.indexBindingMemberEditing].group;
        this.roleBindingMemberEditing = this.bindingsMember[this.indexBindingMemberEditing].groupRole;
      }
      else
      {
        this.indexBindingManagerEditing = index;
        this.groupBindingManagerEditing = this.bindingsManager[this.indexBindingManagerEditing].group;
        this.roleBindingManagerEditing = this.bindingsManager[this.indexBindingManagerEditing].groupRole;
      }
      this.initSuggesterGroup(index,type);
    },
    saveBinding: function(index,type) {
      if (type === 'member') {this.indexBindingMemberEditing = -1;}
      else {this.indexBindingManagerEditing = -1;}
    },
    deleteBinding: function(index,type) {
      if (type === 'member'){this.bindingsMember.splice(index,1);}
      else {this.bindingsManager.splice(index,1);}
    },
    cancelBinding: function(index,type) {
      if (type === 'member')
      {
        this.bindingsMember[this.indexBindingMemberEditing].group = this.groupBindingMemberEditing;
        this.bindingsMember[this.indexBindingMemberEditing].groupRole = this.roleBindingMemberEditing;
        this.indexBindingMemberEditing = -1;
      }
      else
      {
        this.bindingsManager[this.indexBindingManagerEditing].group = this.groupBindingManagerEditing;
        this.bindingsManager[this.indexBindingManagerEditing].groupRole = this.roleBindingManagerEditing;
        this.indexBindingManagerEditing = -1;
      }
    },
    addBinding: function(type) {
      if (type === 'member')
      {
        this.bindingsMember.push({});
        this.editBinding(this.bindingsMember.length-1,type);
      }
      else
      {
        this.bindingsManager.push({});
        this.editBinding(this.bindingsManager.length-1,type);
      }

    }
  }
};
</script>



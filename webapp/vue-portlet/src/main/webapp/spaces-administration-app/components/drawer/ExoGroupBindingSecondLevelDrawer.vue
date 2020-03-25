<template>
  <div class="secondDrawer">
    <v-card-title
      class="title">
      <v-layout
        v-show="!searchMode"
        pa-2
        justify-center
        align-baseline
        wrap
        row>
        <v-flex
          align-self-end
          xs1>
          <v-btn
            icon
            class="leftIcon"
            @click="$emit('back')">
            <v-icon small>arrow_back</v-icon>
          </v-btn>
        </v-flex>
        <v-flex>
          <span class="subtitle-1">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.selectGroups') }}
          </span>
        </v-flex>
        <v-flex xs1>
          <v-btn icon @click="searchMode = true">
            <v-icon class="closeIcon">search</v-icon>
          </v-btn>
        </v-flex>
        <v-flex xs1 mr-2>
          <v-btn
            icon
            class="rightIcon"
            @click="closeDrawer">
            <v-icon
              large
              class="closeIcon">
              close
            </v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
      <v-layout
        v-show="searchMode"
        pa-2
        justify-center
        align-baseline
        wrap
        row>
        <v-flex pl-4>
          <v-text-field
            v-model="search"
            :placeholder="$t(`social.spaces.administration.manageSpaces.spaceBindingForm.selectGroups.search`)"
            append-icon="search"
            class="treeGroupSearch"
            single-line
            clearable
            solo
            flat
            hide-details>
          </v-text-field>
        </v-flex>
        <v-flex xs1 mr-2>
          <v-btn
            icon
            class="rightIcon"
            @click="searchMode = false">
            <v-icon
              large
              class="closeIcon">
              close
            </v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
    </v-card-title>
    <v-layout pt-4 pl-2 class="content">
      <v-flex>
        <div v-show="loading">
          <v-flex pt-4 pl-4 pr-8>
            <v-skeleton-loader
              class="mx-auto"
              type="paragraph@3">
            </v-skeleton-loader>
          </v-flex>
        </div>
        <v-treeview
          v-show="!loading && !searching"
          id="treeDisplayMode"
          v-model="selection"
          :items="items"
          :open="openedNodes"
          :active="selection"
          selection-type="independent"
          expand-icon="mdi-chevron-down"
          dense
          shaped
          hoverable
          selectable
          open-on-click
          return-object
          @update:open="updateBoundNodes">
        </v-treeview>
        <v-treeview
          v-show="searching"
          id="treeSearchMode"
          v-model="selection"
          :items="items"
          :search="search"
          :open="openItems"
          :active="active"
          selection-type="independent"
          expand-icon="mdi-chevron-down"
          dense
          shaped
          hoverable
          selectable
          activatable
          multiple-active
          open-on-click
          return-object
          @update:open="updateBoundNodes">
        </v-treeview>
      </v-flex>
    </v-layout>
    <v-card-actions absolute class="drawerActions">
      <v-layout>
        <v-flex class="xs6"></v-flex>
        <button type="button" class="btn ml-2" @click="cancelSelection">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</button>
        <button :disabled="!isAllowToSave" type="button" class="btn btn-primary ml-6" @click="saveSelection">
          {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.save') }}
        </button>
      </v-layout>
    </v-card-actions>      
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../../spacesAdministrationServices';

export default {
  props: {
    alreadySelected: {
      type: Array,
      default: () => [],
    },
    groupSpaceBindings: {
      type: Array,
      default: null,
    },
  },
  data() {
    return {
      loading: true,
      searchMode: false,
      selection: [],
      items: [],
      search: null,
      allItems: [],
      openedNodes: [],
      openItems: [],
      active: [],
      confirmedSelection: [],
    };
  },
  computed : {
    isAllowToSave() {
      // retrieve none confirmed selections
      const nonConfirmedSelection = [];
      const confirmed = this.confirmedSelection.map(group => group.id);
      const allSelection = this.selection.map(group => group.id);
      // check for non confirmed changes
      allSelection.forEach(groupId => {
        if (!confirmed.includes(groupId)) {
          nonConfirmedSelection.push(groupId);
        }
      });
      confirmed.forEach(groupId => {
        if (!allSelection.includes(groupId)) {
          nonConfirmedSelection.push(groupId);
        }
      });
      return nonConfirmedSelection.length - this.groupSpaceBindings.length > 0;
    },
    searching() {
      return this.search;
    },
  },
  watch: {
    alreadySelected() {
      const selected = this.alreadySelected.map(groupId => ({
        id: groupId,
        name: groupId,
      }));
      if (this.confirmedSelection.length === 0) {
        this.confirmedSelection.push(...selected);
        this.selection.push(...selected);
      } else {
        const removedGroupIds = [];
        const  addedGroupIds = [];
        
        // retrieve removed items
        const confirmed = this.confirmedSelection.map(item => item.id);
        confirmed.forEach(groupId => {
          if (!this.alreadySelected.includes(groupId)) {
            removedGroupIds.push(groupId);
          }
        });
        
        // retrieve added items
        this.alreadySelected.forEach(groupId => {
          if (!confirmed.includes(groupId)) {
            addedGroupIds.push(groupId);
          }
        });
        
        // synchronize confirmed selection
        removedGroupIds.forEach(groupId => {
          this.selection = this.selection.filter(group => group.id !== groupId);
        });
        addedGroupIds.forEach(groupId => {
          this.selection.push({
            id: groupId,
            name: groupId
          });
        });
        // update confirmed selection
        this.confirmedSelection = [];
        this.confirmedSelection.push(...selected);
      }
    },
    search() {
      if (this.search) {
        this.active = this.allItems.filter(item => item.name.toLowerCase().match(this.search.toLowerCase()));
        if (this.active.length > 0) {
          this.openItems = this.getItemsToOpen();
        }
      } else {
        // init activate.
        this.active = [];
        this.openItems = [];
      }
    },
    groupSpaceBindings() {
      this.selection = [];
      this.openedNodes = [];
      // make sure that bound groups are already selected and disabled.
      const boundItems = this.getBoundItems();
      this.selection.push(...boundItems);
      this.disableBoundNodes(document.getElementById('treeDisplayMode'));
    },
  },
  mounted() {
    this.getRootChildGroups();
  },
  methods : {
    getRootChildGroups() {
      spacesAdministrationServices.getGroupsTree(null).then(data => {
        this.items = data.childGroups;
      }).finally(() => {
        this.loading = false;
        this.getAllGroups();
      });
    },
    getAllGroups() {
      //TODO optimize.
      // Get all groups by level, max set to 5.
      const firstChildren = [];
      const secondChildren = [];
      const thirdChildren = [];
      const fourthChildren = [];
      const fifthChildren = [];

      this.allItems.push(...this.items);

      // level 1:
      this.items.forEach(child => {
        const children = child.children;
        firstChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 2:
      firstChildren.forEach(child => {
        const children = child.children;
        secondChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 3:
      secondChildren.forEach(child => {
        const children = child.children;
        thirdChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 4:
      thirdChildren.forEach(child => {
        const children = child.children;
        fourthChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 5:
      fourthChildren.forEach(child => {
        const children = child.children;
        fifthChildren.push(...children);
        this.allItems.push(...children);
      });
    },
    getItemsToOpen() {
      // count parents of each active element
      const toOpenIds = [];
      this.active.forEach(item => {
        // skip root parent
        let currentItem = item;
        const countParents = currentItem.id.split('/').length - 1;
        let parent;
        for (let i = countParents - 1; i > 0; i--) {
          parent = this.getItem(currentItem.parentId);
          if (!toOpenIds.includes(parent.id)) {
            toOpenIds.push(parent.id);
          }
          currentItem = parent;
        }
      });
      // retrieve items to be opened by ids
      const toOpen = [];
      toOpenIds.forEach(id => {
        toOpen.push(this.getItem(id));
      });
      return toOpen;
    },
    getItem(id) {
      return this.allItems.filter(item => item.id === id)[0];
    },
    getBoundItems() {
      const boundItems = [];
      if (this.groupSpaceBindings && this.groupSpaceBindings.length > 0) {
        const boundGroupIds = this.groupSpaceBindings.map(binding => binding.group);
        boundGroupIds.forEach(groupId => {
          boundItems.push(this.getItem(groupId));
        });
      }
      return boundItems;
    },
    disableBoundNodes(node) {
      const boundItems = this.getBoundItems();
      const parentNode = node ? node : document;
      if (this.groupSpaceBindings && this.groupSpaceBindings.length > 0) {
        const labels = parentNode.getElementsByClassName('v-treeview-node__label');
        // disable bound groups selection
        const boundGroupsLabels = boundItems.map(item => item.name);
        if (labels) {
          Array.prototype.forEach.call(labels, function (label) {
            if (boundGroupsLabels.includes(label.innerHTML)) {
              const content = label.parentElement;
              content.setAttribute('class', 'v-treeview-node__content bound');
              const parentNode = label.parentElement.parentElement;
              parentNode.setAttribute('class', 'v-treeview-node__root bound');
              const checkboxButton = parentNode.getElementsByClassName('v-treeview-node__checkbox')[0];
              checkboxButton.disabled = true;
            }
          });
        }
      }
    },
    updateBoundNodes(openNodes) {
      let opened;
      const self = this;
      // wait till child elements are rendered
      Vue.nextTick()
        .finally(function () {
          const openedNode = self.getLastOpened(openNodes);
          const hasBoundNodes = openedNode ? self.hasBoundNodes(openedNode.id) : false;
          if (openedNode && hasBoundNodes) {
            if (!self.searching) {
              // in display mode
              const displayModeDiv = document.getElementById('treeDisplayMode');

              // get opened node
              opened = self.getNodeByLabel(openedNode.name, displayModeDiv);
              self.waitForElement(opened.getElementsByClassName('v-treeview-node__children')[0]).then(() => {
                self.disableBoundNodes(opened.getElementsByClassName('v-treeview-node__children')[0]);
              });
            }
          }
          if (self.searching) {
            // in search mode
            const searchModeDiv = document.getElementById('treeSearchMode');
            self.disableBoundNodes(searchModeDiv);
          }
        });
    },
    getNodeByLabel(name, parent) {
      const labels = parent.getElementsByClassName('v-treeview-node__label');
      let node = null;
      if (labels) {
        Array.prototype.forEach.call(labels, function (label) {
          if (name === label.innerHTML) {
            node = label.parentElement.parentElement.parentElement;
          }
        });
      }
      return node;
    },
    getLastOpened(nodes) {
      if (this.openedNodes && this.openedNodes.length > 0) {
        const nodesIds = nodes.map(node => node.id);
        const openedIds = this.openedNodes.map(node => node.id);
        let lastOpened;
        nodesIds.forEach(nodeId => {
          if (!openedIds.includes(nodeId)) {
            lastOpened = this.getItem(nodeId);
          }
        });
        // update opened nodes
        this.openedNodes = [];
        this.openedNodes.push(...nodes);
        return lastOpened;
      } else {
        this.openedNodes.push(...nodes);
        return this.openedNodes[0];
      }
    },
    hasBoundNodes(id) {
      let result = false;
      this.groupSpaceBindings.forEach(binding => {
        if (binding.group.startsWith(id)) {
          result = true;
        }
      });
      return result;
    },
    waitForElement(element) {
      return new Promise(function(resolve) {
        if(element) {
          resolve(element);
          return;
        }
        const observer = new MutationObserver(function(mutations) {
          mutations.forEach(function(mutation) {
            const nodes = Array.from(mutation.addedNodes);
            for(const node of nodes) {
              if(node.matches && node.matches(element)) {
                observer.disconnect();
                resolve(node);
                return;
              }
            }
          });
        });

        observer.observe(document.documentElement, { childList: true, subtree: true });
      });
    },
    closeDrawer() {
      this.selection = [];
      this.search = '';
      this.$emit('close');
    },
    cancelSelection() {
      // get last index of bound groups
      const index = this.groupSpaceBindings.length -1;
      // count unbound groups
      const count = this.selection.length - this.groupSpaceBindings.length;
      // deselect only unbound groups
      this.selection.splice(index + 1, count);
    },
    saveSelection() {
      // selection data shouldn't be modified directly. 
      const saved = [];
      saved.push(...this.selection);
      // get last index of bound groups
      const index = this.groupSpaceBindings.length -1;
      // deselect only unbound groups
      saved.splice(0, index + 1);
      // clear open items, active items, search and search mode.
      this.openItems = [];
      this.active = [];
      this.search = '';
      this.searchMode = false;      
      this.$emit('selectionSaved', saved.map(group => group.id));
    },
  },
};
</script>

<style scoped>

</style>
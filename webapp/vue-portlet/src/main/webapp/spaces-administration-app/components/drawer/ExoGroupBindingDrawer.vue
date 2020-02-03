<template>
  <div v-if="!showSelectGroupsTree">
    <v-flex id="GroupBindingForm">
      <v-card-title 
        class="title">
        <v-layout class="pa-2" row>
          <v-flex>
            <span>
              {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.title') }}
            </span>
          </v-flex>
          <v-flex class="xs1 mr-2">
            <v-btn
              icon
              class="rightIcon"
              @click="$emit('close')">
              <v-icon 
                large
                class="closeIcon">
                close
              </v-icon>
            </v-btn>          
          </v-flex>
        </v-layout>
      </v-card-title>
      <div class="content">
        <v-layout 
          align-center
          justify-center
          align-baseline
          wrap>
          <v-flex xs9>
            <v-textarea
              :value="value"
              rounded
              auto-grow
              rows="2"
              row-height="35"
              placeholder="@ Type a group name"
            ></v-textarea>
          </v-flex>
          <v-flex xs3>
            <v-btn
              v-exo-tooltip.bottom.body="$t('social.spaces.administration.manageSpaces.spaceBindingForm.selectList')"
              icon
              class="rightIcon"
              @click="showSelectGroupsTree = true">
              <i class="uiIconSpaceBinding uiIconGroup"></i>
            </v-btn>
          </v-flex>
        </v-layout>
        <v-layout column>
          <v-flex>
            <span class="subtitle-1">
              {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.boundGroups') }}
            </span>
          </v-flex>
          <v-flex class="boundGroups">
            <v-list flat subheader dense>
              <v-list-item-group>
                <div v-for="i in items" :key="i">
                  <v-list-item>
                    <v-list-item-content>
                      *:/platform/users {{ i }}
                    </v-list-item-content>
                    <v-list-item-action class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon"
                        @click="$emit('close')">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      *:/organization/employees {{ i }}
                    </v-list-item-content>
                    <v-list-item-action class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon"
                        @click="$emit('close')">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      *:/platform {{ i }}
                    </v-list-item-content>
                    <v-list-item-action absolute class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon"
                        @click="$emit('close')">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </div>              
              </v-list-item-group>
            </v-list>
          </v-flex>
        </v-layout>
      </div>
      <v-card-actions absolute class="drawerActions">
        <v-layout>
          <v-flex class="xs7"></v-flex>
          <button type="button" class="btn ml-2" @click="$emit('close')">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</button>
          <button :disabled="!isAllowToSave" type="button" class="btn btn-primary ml-6" @click="isAllowToSave = false">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.apply') }}
          </button>
        </v-layout>
      </v-card-actions>      
    </v-flex>
  </div>
  <div v-else>
    <v-card-title 
      class="title">
      <v-layout 
        class="pa-2" 
        justify-center
        align-baseline 
        row>
        <v-flex
          align-self-end
          class="xs1">
          <v-btn
            icon
            class="leftIcon"
            @click="showSelectGroupsTree = !showSelectGroupsTree">
            <v-icon small>arrow_back</v-icon>
          </v-btn>
        </v-flex>
        <v-flex>
          <span class="subtitle-1">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.selectGroups') }}
          </span>
        </v-flex>
        <v-flex class="xs1 mr-2">
          <v-btn
            icon
            class="rightIcon"
            @click="$emit('close')">
            <v-icon 
              large
              class="closeIcon">
              close
            </v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
    </v-card-title>      
  </div>
</template>

<script>
export default {
  data() {
    return {
      value: '',
      showSelectGroupsTree : false,
      isAllowToSave : false,
      items: [1]
    };
  }
};
</script>
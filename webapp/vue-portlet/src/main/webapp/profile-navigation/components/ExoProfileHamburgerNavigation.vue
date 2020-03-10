<template>
  <v-flex id="ProfileHamburgerNavigation">
    <v-row class="accountTitleWrapper mx-0">
      <v-list-item
        class="accountTitleItem py-3 "
        @click="navigateTo('profile')">
        <v-list-item-avatar size="44" class="mr-3 mt-0 mb-0 elevation-1">
          <v-img :src="avatar"/>
        </v-list-item-avatar>
        <v-list-item-content class="py-0 accountTitleLabel">
          <v-list-item-title class="font-weight-bold body-2 mb-0">{{ fullName }}</v-list-item-title>
          <v-list-item-subtitle class="font-italic caption">{{ position }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-row>
  </v-flex>
</template>
<script>
export default {
  data() {
    return {
      IDENTITY_REST_API_URI: '/portal/rest/v1/social/identities/',
      avatar: `/portal/rest/v1/social/users/${eXo.env.portal.userName}/avatar`,
      profile: null,
    };
  },
  computed: {
    fullName() {
      return this.profile && this.profile.fullname;
    },
    position() {
      return this.profile && this.profile.position;
    },
  },
  created() {
    fetch(`${this.IDENTITY_REST_API_URI}${eXo.env.portal.userIdentityId}`, {
      method: 'GET',
      credentials: 'include',
    })
      .then(data => data && data.ok && data.json())
      .then(data => this.profile = data && data.profile);
  },
  methods: {
    
  }
};
</script>

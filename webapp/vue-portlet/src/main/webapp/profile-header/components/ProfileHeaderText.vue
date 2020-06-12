<template>
  <v-flex>
    <v-card-title
      :class="skeleton && 'skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3' || ''"
      class="headline white--text">
      {{ !skeleton && user && user.fullname || '&nbsp;' }} {{ !skeleton && user.enabled ? '' : '(' + $t('profileHeader.label.disabled') + ')' }}    </v-card-title>
    <v-card-subtitle
      :class="skeleton && 'skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3' || ''"
      class="subtitle white--text"
      dark>
      {{ !skeleton && user && user.position || '&nbsp;' }}
    </v-card-subtitle>
  </v-flex>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    skeleton: {
      type: Boolean,
      default: () => true,
    },
  },
};
</script>

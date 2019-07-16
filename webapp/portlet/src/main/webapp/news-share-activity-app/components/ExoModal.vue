<template>
  <div v-show="show" class="modal-mask uiPopupWrapper">
    <div class="uiPopup modal-content">
      <div class="popupHeader">
        <a class="uiIconClose pull-right" @click="$emit('close')"></a>
        <span class="popupTitle">{{ title }}</span>
      </div>
      <div class="PopupContent popupContent">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
const EVEN = 2;
export default {
  props: {
    title: {
      type: String,
      default: ''
    },
    show: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    show() {
      if (this.show) {
        //this is workaround to fix chrome bug
        //popup is blurring when using translate() and the size is odd
        Vue.nextTick(function() {
          const width = this.$el.offsetWidth;
          if (width % EVEN !== 0)
          {
            this.$el.style.width = `${width + 1}px`;
          }

          const height = this.$el.offsetHeight;
          if (height % EVEN !== 0)
          {
            this.$el.style.height = `${height + 1}px`;
          }
        }, this);
      }
    }
  }
};
</script>

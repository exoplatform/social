<template>
  <div class="modal-mask uiPopupWrapper">
    <div :class="modalClass" class="uiPopup modal-content">
      <div class="popupHeader">
        <span class="PopupTitle popupTitle">{{ title }}</span>
        <a v-if="displayClose" class="uiIconClose pull-right" @click="closeModal"></a> 
      </div>
      <div class="PopupContent popupContent">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    displayClose: {
      type: Boolean,
      default: true
    },
    title: {
      type: String,
      default: ''
    },
    modalClass: {
      type: String,
      default: ''
    }
  },
  methods: {
    closeModal() {
      // Emit the click event of close icon
      this.$emit('modal-closed');
    }
  }
};
</script>
<style>
.modal-mask .uiPopupWrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}
.uiPopup {
  width: 600px;
  position: relative;
  user-select: none;
  display: block;
  visibility: visible;
  z-index: 4;
  top: 158px;
  left: 336px;
  } 
.modal-content .popupTitle {
    cursor: auto;
}
</style>



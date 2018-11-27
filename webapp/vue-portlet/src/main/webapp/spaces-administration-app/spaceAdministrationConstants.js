export const spaceConstants = {
  ENV: eXo.env.portal || '',
  PORTAL: eXo.env.portal.context || '',
  PORTAL_NAME: eXo.env.portal.portalName || '',
  PROFILE_SPACE_LINK: '/g/:spaces:',
  SOCIAL_USER_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/`,
  SPACES_PER_PAGE: 2,
  DEFAULT_SPACE_AVATAR: '/eXoSkin/skin/images/system/SpaceAvtDefault.png'
}
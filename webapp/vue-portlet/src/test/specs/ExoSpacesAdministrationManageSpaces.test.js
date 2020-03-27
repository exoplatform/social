import { createLocalVue, shallowMount } from '@vue/test-utils';
import { spacesConstants } from '../../main/webapp/js/spacesConstants.js';
import ExoSpacesAdministrationManageSpaces from '../../main/webapp/spaces-administration-app/components/ExoSpacesAdministrationManageSpaces';
import ExoModal from '../../main/webapp/spaces-administration-app/components/modal/ExoModal';

const localVue = createLocalVue();

// mock exo-tooltip directive
localVue.directive('exo-tooltip', function() {});

describe('ExoSpacesAdministrationManageSpaces.test.js', () => {
  let cmp;
  const data = {
    spaces: [
      {
        'id': '1',
        'displayName': 'space1',
        'description': 'space1 is the first space',
        'members' : [
          { 'id': '1' },
          { 'id': '2' }
        ]
      },
      {
        'id': '2',
        'displayName': 'space2',
        'description': 'space2 is the second space',
        'members' : [
          { 'id': '1' },
          { 'id': '2' }
        ]
      }
    ],
    showConfirmMessageModal: false
  };
  beforeEach(() => {
    cmp = shallowMount(ExoSpacesAdministrationManageSpaces, {
      localVue,
      stubs: {
        'exo-modal': ExoModal
      },
      mocks: {
        $t: () => {},
        $constants : spacesConstants
      }
    });
        
  });

  it('should display 2 spaces in table when 2 spaces in data', () => {
    cmp.vm.spaces = data.spaces;
    const spacesTableRows = cmp.findAll('.manageSpaces table tr');
    expect(spacesTableRows).toHaveLength(3); // header + 2 rows
  });
  
  it('should show confirm message modal when deleting space', () => {
    cmp.vm.deleteSpaceById(1, 0);
    const confirmPopup = cmp.find('.uiPopup');
    expect(confirmPopup.isVisible()).toBeTruthy();
  });
  
  it('should close confirm message modal when clicking on close icon', () => {
    cmp.vm.showConfirmMessageModal = true;
    const confirmPopup = cmp.find('.uiPopup');
    expect(confirmPopup.isVisible()).toBeTruthy();
    const closeButton = confirmPopup.find('.uiIconClose');
    closeButton.trigger('click');
    expect(confirmPopup.isVisible()).toBe(false);
  });

  it('should fetch spaces with keyword when a search is triggered', () => {
    const mockJsonPromise = Promise.resolve({'spaces': [
      {
        'id': '1',
        'displayName': 'space1',
        'description': 'space1 is the first space'
      }]
    });
    const mockFetchPromise = Promise.resolve({
      json: () => mockJsonPromise,
    });
    global.fetch = jest.fn().mockImplementation(() => mockFetchPromise);

    const showInputSearch = cmp.find('.showInputSearch');
    const showIconSearch = cmp.find('.uiIconPLF24x24Search');
    
    showInputSearch.element.value = 'space1';
    showInputSearch.trigger('input');

    showIconSearch.trigger('click');

    expect(global.fetch).toHaveBeenCalledTimes(1);
    expect(global.fetch).toHaveBeenCalledWith('portal/rest/v1/social/spaces?q=space1&sort=date&order=desc&limit=30&returnSize=true&expand=members', {'credentials': 'include'});

    global.fetch.mockClear();
  });
});

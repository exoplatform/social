import { shallow } from 'vue-test-utils';
import {spaceConstants} from '../../main/webapp/spaces-administration-app/spaceAdministrationConstants.js';
import ExoSpacesAdministrationManageSpaces from '../../main/webapp/spaces-administration-app/components/ExoSpacesAdministrationManageSpaces';
import ExoModal from '../../main/webapp/spaces-administration-app/components/modal/ExoModal';

describe('ExoSpacesAdministrationManageSpaces.test.js', () => {
  let cmp;
  const data = {
    spaces: [
      {
        'id': '1',
        'displayName': 'space1',
        'description': 'space1 is the first space'
      },
      {
        'id': '2',
        'displayName': 'space2',
        'description': 'space2 is the second space'
      }
    ],
    showConfirmMessageModal: false
  };
  beforeEach(() => {
    cmp = shallow(ExoSpacesAdministrationManageSpaces, {
      stubs: {
        'exo-modal': ExoModal
      },
      mocks: {
        $t: () => {},
        $constants : spaceConstants
      }
    });
        
  });

  it('should display 2 spaces in table when 2 spaces in data', () => {
    cmp.vm.spaces = data.spaces;
    cmp.vm.$nextTick(() => {
      const spacesTableRows = cmp.findAll('.manageSpaces table tr');
      expect(spacesTableRows).toHaveLength(3); // header + 2 rows
    });
  });
  
  it('should show confirm message modal when deleting space', () => {
    cmp.vm.deleteSpaceById(1, 0);
    expect(cmp.vm.showConfirmMessageModal).toBeTruthy();
  });
  
  it('should close confirm message modal when clicking on close icon', () => {
    cmp.vm.showConfirmMessageModal = true;
    const closeButton = cmp.find('.btn');
    expect(cmp.vm.showConfirmMessageModal).toBe(true);
    closeButton.trigger('click');
    expect(cmp.vm.showConfirmMessageModal).toBe(false);
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
    expect(global.fetch).toHaveBeenCalledWith('portal/rest/v1/social/spaces?q=space1&limit=30&returnSize=true', {'credentials': 'include'});

    global.fetch.mockClear();
  });
});
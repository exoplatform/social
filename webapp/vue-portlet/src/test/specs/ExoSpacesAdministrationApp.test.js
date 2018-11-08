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
  }
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

  it('2 displayed spaces', () => {
    cmp.vm.spaces = data.spaces;
    expect(cmp.vm.spaces).toHaveLength(2);
  });
  
  it('show confirm message modal', () => {
    cmp.vm.deleteSpaceById(1, 0);
    expect(cmp.vm.showConfirmMessageModal).toBeTruthy();
  });
  
  it('close confirm message when click on close icon', () => {
    cmp.vm.showConfirmMessageModal = true;
    const closeButton = cmp.find('.btn');
    expect(cmp.vm.showConfirmMessageModal).toBe(true);
    closeButton.trigger('click');
    expect(cmp.vm.showConfirmMessageModal).toBe(false);
  });
});
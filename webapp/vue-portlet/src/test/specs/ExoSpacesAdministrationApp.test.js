import { shallow } from 'vue-test-utils';
import {spaceConstants} from '../../main/webapp/spaces-administration-app/spaceAdministrationConstants.js';
import ExoSpacesAdministrationManageSpaces from '../../main/webapp/spaces-administration-app/components/ExoSpacesAdministrationManageSpaces';

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
    ]

  }
  beforeEach(() => {
    cmp = shallow(ExoSpacesAdministrationManageSpaces, {
      mocks: {
        $t: () => {},
        $constants : spaceConstants
      }
    });
        
  });

  it('2 displayed spaces', () => {
    cmp.vm.spaces = data.spaces;
    expect(cmp.vm.spaces[0].displayName).toEqual('space1');
  })
});
import { createLocalVue, shallowMount } from '@vue/test-utils';
import { spacesConstants } from '../../main/webapp/spaces-administration-app/spacesAdministrationConstants.js';
import ExoSpacesAdministrationSpacesPermissions from '../../main/webapp/spaces-administration-app/components/ExoSpacesAdministrationSpacesPermissions';

const localVue = createLocalVue();

// mock exo-tooltip directive
localVue.directive('exo-tooltip', function() {});

describe('ExoSpacesAdministrationSpacesPermissions.test.js', () => {
  let cmp;
  const data = {
    creators: ['*:/group1', '*:/group2'],
    administrators: ['*:/group3'],
  };
  beforeEach(() => {
    cmp = shallowMount(ExoSpacesAdministrationSpacesPermissions, {
      localVue,
      stubs: {
      },
      mocks: {
        $t: () => {},
        $constants : spacesConstants
      }
    });
        
  });

  it('should display 2 permissions rows with the right values', () => {
    cmp.vm.creators = data.creators;
    cmp.vm.administrators = data.administrators;

    const permissionsTableRow = cmp.findAll('.spacesPermissions table tr');
    expect(permissionsTableRow).toHaveLength(3); // header + 2 rows

    const creatorsPermissions = permissionsTableRow.at(1).findAll('td').at(1);
    expect(creatorsPermissions.html().indexOf('*:/group1') >= 0).toBe(true);
    expect(creatorsPermissions.html().indexOf('*:/group2') >= 0).toBe(true);
    expect(creatorsPermissions.html().indexOf('*:/group3') >= 0).toBe(false);

    const manageSpacesPermissions = permissionsTableRow.at(2).findAll('td').at(1);
    expect(manageSpacesPermissions.html().indexOf('*:/group1') >= 0).toBe(false);
    expect(manageSpacesPermissions.html().indexOf('*:/group2') >= 0).toBe(false);
    expect(manageSpacesPermissions.html().indexOf('*:/group3') >= 0).toBe(true);
  });
});
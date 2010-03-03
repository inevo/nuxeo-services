package org.nuxeo.ecm.platform.computedgroups.test;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.platform.computedgroups.AbstractGroupComputer;
import org.nuxeo.ecm.platform.computedgroups.GroupComputer;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;

public class DummyGroupComputer extends AbstractGroupComputer implements GroupComputer {

    List<String> grpNames = new ArrayList<String>();

    public DummyGroupComputer() {
        grpNames.add("Grp1");
        grpNames.add("Grp2");
    }

    public List<String> getGroupsForUser(NuxeoPrincipalImpl nuxeoPrincipal)
            throws Exception {

        List<String> grps = new ArrayList<String>();
        if (nuxeoPrincipal.getName().contains("1")) {
            grps.add("Grp1");
        }
        if (nuxeoPrincipal.getName().contains("2")) {
            grps.add("Grp2");
        }

        return grps;
    }

    public List<String> getGroupMembers(String groupName) throws Exception {

        List<String> names = new ArrayList<String>();

        if ("Grp1".equals(groupName)) {
            names.add("User1");
            names.add("User12");
        }
        else if ("Grp2".equals(groupName)) {
            names.add("User2");
            names.add("User12");
        }
        return names;
    }

    public List<String> getParentsGroupNames(String groupName) throws Exception {
        return null;
    }

    public List<String> getSubGroupsNames(String groupName) throws Exception {
        return null;
    }

    public List<String> getAllGroupIds() throws Exception {
        return grpNames;
    }

}
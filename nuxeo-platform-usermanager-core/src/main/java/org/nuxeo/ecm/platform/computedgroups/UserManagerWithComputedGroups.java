/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thierry Delprat
 *
 */

package org.nuxeo.ecm.platform.computedgroups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.UserManagerImpl;
import org.nuxeo.runtime.api.Framework;

/**
*
* {@link UserManager} implementation that is aware of {@link ComputedGroup}.
*
* @author Thierry Delprat
*
*/

public class UserManagerWithComputedGroups extends UserManagerImpl implements
        UserManager {

    private static final long serialVersionUID = 1L;

    protected static ComputedGroupsService cgs = null;

    protected static Boolean useComputedGroup = null;

    public static final String VIRTUAL_GROUP_MARKER = "virtual";


    protected ComputedGroupsService getService() {
        if (cgs == null) {
            cgs = Framework.getLocalService(ComputedGroupsService.class);
        }
        return cgs;
    }

    protected boolean activateComputedGroup() {
        if (useComputedGroup==null) {
            useComputedGroup = getService().activateComputedGroups();
        }
        return useComputedGroup;
    }

    @Override
    protected NuxeoPrincipal makePrincipal(DocumentModel userEntry,
            boolean anonymous, List<String> groups) throws ClientException {

        NuxeoPrincipal principal = super.makePrincipal(userEntry, anonymous,
                groups);
        if (activateComputedGroup() && principal instanceof NuxeoPrincipalImpl) {
            NuxeoPrincipalImpl nuxPrincipal = (NuxeoPrincipalImpl) principal;

            List<String> vGroups = getService().computeGroupsForUser(
                    nuxPrincipal);

            if (vGroups == null) {
                vGroups = new ArrayList<String>();
            }

            List<String> origVGroups = nuxPrincipal.getVirtualGroups();
            if (origVGroups == null) {
                origVGroups = new ArrayList<String>();
            }

            // MERGE!
            origVGroups.addAll(vGroups);

            nuxPrincipal.setVirtualGroups(origVGroups);

            // This a hack to work around the problem of running tests
            if (Framework.isTestModeSet() == false) {
                nuxPrincipal.updateAllGroups();
            } else {
                List<String> allGroups = nuxPrincipal.getGroups();
                for (String vGroup : vGroups) {
                    if (!allGroups.contains(vGroup)) {
                        allGroups.add(vGroup);
                    }
                }
                nuxPrincipal.setGroups(allGroups);
            }
        }
        return principal;
    }

    @Override
    public NuxeoGroup getGroup(String groupName) throws ClientException {
        NuxeoGroup grp = super.getGroup(groupName, false);
        if (activateComputedGroup() && (grp == null || getService().allowGroupOverride())) {
            grp = getService().getComputedGroup(groupName);
        }
        return grp;
    }

    @Override
    public List<String> getGroupIds() throws ClientException {

        List<String> ids = super.getGroupIds();
        if (activateComputedGroup()) {
            List<String> vGroups = getService().computeGroupIds();
            for (String vGroup : vGroups) {
                if (!ids.contains(vGroup)) {
                    ids.add(vGroup);
                }
            }
        }
        return ids;
    }

    @Override
    public DocumentModel getGroupModel(String groupName) throws ClientException {
        DocumentModel grpDoc = super.getGroupModel(groupName);
        if (activateComputedGroup() && grpDoc == null) {
            return getComputedGroupAsDocumentModel(groupName);
        }
        return grpDoc;
    }

    @Override
    public DocumentModelList searchGroups(Map<String, Serializable> filter,
            HashSet<String> fulltext) throws ClientException {

        boolean searchInVirtualGroups = activateComputedGroup();
        if (Boolean.FALSE.equals(filter.get(VIRTUAL_GROUP_MARKER))) {
            searchInVirtualGroups=false;
        }
        if (filter.containsKey(VIRTUAL_GROUP_MARKER)) {
            filter.remove(VIRTUAL_GROUP_MARKER);
        }

        DocumentModelList groups = super.searchGroups(filter, fulltext);

        if (searchInVirtualGroups) {
            for (String vGroupName : getService().searchComputedGroups(filter, fulltext)) {
                DocumentModel vGroup = getComputedGroupAsDocumentModel(vGroupName);
                if (vGroup != null) {
                    if (!groups.contains(vGroup)) {
                        groups.add(vGroup);
                    }
                }
            }
        }
        return groups;
    }

    protected DocumentModel getComputedGroupAsDocumentModel(String grpName)
            throws ClientException {

        String schemaName = super.getGroupSchemaName();
        String id = super.getGroupIdField();
        DocumentModel groupDoc = BaseSession.createEntryModel(null, schemaName,
                grpName, null);

        NuxeoGroup grp = getService().getComputedGroup(grpName);

        groupDoc.setProperty(schemaName, getGroupMembersField(), grp.getMemberUsers());
        groupDoc.setProperty(schemaName, id, grp.getName());
        groupDoc.setProperty(schemaName, getGroupIdField(), grp.getName());

        groupDoc.getContextData().putScopedValue("virtual", true);

        return groupDoc;
    }

}
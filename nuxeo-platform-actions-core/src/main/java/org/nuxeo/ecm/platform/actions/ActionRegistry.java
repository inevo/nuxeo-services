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
 *     Nuxeo - initial API and implementation
 *
 * $Id: ActionRegistry.java 20637 2007-06-17 12:37:03Z sfermigier $
 */

package org.nuxeo.ecm.platform.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class ActionRegistry implements Serializable {

    private static final Log log = LogFactory.getLog(ActionRegistry.class);

    private static final long serialVersionUID = 8425627293154848041L;

    private final Map<String, Action> actions;

    private final Map<String, List<String>> categories;

    private List<TypeCompatibility> typeCategoryRelations;

    public ActionRegistry() {
        actions = new HashMap<String, Action>();
        categories = new HashMap<String, List<String>>();
        typeCategoryRelations = new ArrayList<TypeCompatibility>();
    }

    public synchronized void addAction(Action action) {
        String id = action.getId();
        if (log.isDebugEnabled()) {
            if (actions.containsKey(id)) {
                log.debug("Overriding action: " + action);
            } else {
                log.debug("Registering action: " + action);
            }
        }
        // add a default label if not set
        if (action.getLabel() == null) {
            action.setLabel(action.getId());
        }
        actions.put(id, action);
        for (String category : action.getCategories()) {
            List<String> acts = categories.get(category);
            if (acts == null) {
                acts = new ArrayList<String>();
            }
            if (!acts.contains(id)) {
                acts.add(id);
            }
            categories.put(category, acts);
        }
    }

    public synchronized Action removeAction(String id) {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering action: " + id);
        }

        Action action = actions.remove(id);
        if (action != null) {
            for (String category : action.getCategories()) {
                List<String> acts = categories.get(category);
                if (acts != null) {
                    acts.remove(id);
                }
            }
        }
        return action;
    }

    public synchronized Collection<Action> getActions() {
        return Collections.unmodifiableCollection(sortActions(actions.values()));
    }

    public List<Action> getActions(String category) {
        List<Action> result = new LinkedList<Action>();
        Collection<String> ids;
        synchronized (this) {
            ids = categories.get(category);
        }
        if (ids != null) {
            for (String id : ids) {
                Action action = actions.get(id);
                if (action != null && action.isEnabled()) {
                    // UI type action compat check
                    Action finalAction = getClonedAction(action);
                    applyCompatibility(category, finalAction);
                    // return only enabled actions
                    result.add(finalAction);
                }
            }
        }
        result = sortActions(result);
        return result;
    }

    protected void applyCompatibility(Action finalAction) {
        if (finalAction.getType() == null) {
            // iterate over all categories to apply compat
            String[] cats = finalAction.getCategories();
            if (cats != null) {
                for (String cat : cats) {
                    if (applyCompatibility(cat, finalAction)) {
                        break;
                    }
                }
            }
        }
    }

    protected boolean applyCompatibility(String category, Action finalAction) {
        if (finalAction.getType() == null) {
            for (TypeCompatibility compat : typeCategoryRelations) {
                for (String compatCategory : compat.getCategories()) {
                    if (StringUtils.equals(compatCategory, category)) {
                        finalAction.setType(compat.getType());
                        if (applyCustomCompatibility(compat.getType(),
                                finalAction)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper for migration of specific actions.
     *
     * @since 5.9.6
     */
    protected boolean applyCustomCompatibility(String compatType, Action action) {
        if ("admin_rest_document_link".equals(compatType)) {
            boolean applied = false;
            String link = action.getLink();
            if (link != null && !link.startsWith("/")) {
                action.setLink("/" + link);
                applied = true;
            }
            Map<String, Serializable> props = action.getProperties();
            if (props == null) {
                props = new HashMap<>();
            }
            if (!props.containsKey("view")) {
                props.put("view", "view_admin");
                action.setProperties(props);
                applied = true;
            }
            if (applied) {
                log.warn(String.format(
                        "Applied compatibility to action '%s': its configuration "
                                + "should be changed, make sure the link references an "
                                + "absolute path, and set the property 'view' to 'view_admin'",
                        action.getId()));
                return true;
            }
        }
        return false;
    }

    public synchronized Action getAction(String id) {
        Action action = actions.get(id);
        Action finalAction = getClonedAction(action);
        applyCompatibility(finalAction);
        return finalAction;
    }

    protected Action getClonedAction(Action action) {
        if (action == null) {
            return null;
        }
        return action.clone();
    }

    protected static List<Action> sortActions(Collection<Action> actions) {
        List<Action> sortedActions = new ArrayList<Action>();
        if (actions != null) {
            sortedActions.addAll(actions);
            Collections.sort(sortedActions);
        }
        return sortedActions;
    }

    public List<TypeCompatibility> getTypeCategoryRelations() {
        return typeCategoryRelations;
    }

    public void setTypeCategoryRelations(
            List<TypeCompatibility> typeCategoryRelations) {
        this.typeCategoryRelations = typeCategoryRelations;
    }

}

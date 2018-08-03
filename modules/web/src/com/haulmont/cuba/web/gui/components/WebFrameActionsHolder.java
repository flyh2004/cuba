/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.web.gui.components;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;

import java.util.*;

import static com.haulmont.cuba.gui.ComponentsHelper.findActionById;

/**
 * Encapsulates {@link com.haulmont.cuba.gui.components.ActionsHolder} functionality for web frames and windows.
 */
public class WebFrameActionsHolder implements com.vaadin.event.Action.Handler {
    protected List<Action> actionList = new ArrayList<>(4);
    protected BiMap<com.vaadin.event.Action, Action> actions = HashBiMap.create();

    protected Component actionSource;

    public WebFrameActionsHolder(Component actionSource) {
        this.actionSource = actionSource;
    }

    public void addAction(Action action) {
        int index = findActionById(actionList, action.getId());
        if (index < 0) {
            index = actionList.size();
        }

        addAction(action, index);
    }

    public void addAction(Action action, int index) {
        int oldIndex = findActionById(actionList, action.getId());
        if (oldIndex >= 0) {
            removeAction(actionList.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        if (action.getShortcutCombination() != null) {
            actions.put(WebComponentsHelper.createShortcutAction(action), action);
        }

        actionList.add(index, action);
    }

    public void removeAction(Action action) {
        if (actionList.remove(action)) {
            actions.inverse().remove(action);
        }
    }

    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    public void removeAllActions() {
        actionList.clear();
        actions.clear();
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actionList);
    }

    public Action getAction(String id) {
        for (Action action : getActions()) {
            if (Objects.equals(action.getId(), id)) {
                return action;
            }
        }
        return null;
    }

    public com.vaadin.event.Action[] getActionImplementations() {
        List<com.vaadin.event.Action> orderedActions = new ArrayList<>(actionList.size());
        for (Action action : actionList) {
            com.vaadin.event.Action e = actions.inverse().get(action);
            if (e != null) {
                orderedActions.add(e);
            }
        }
        return orderedActions.toArray(new com.vaadin.event.Action[0]);
    }

    public Action getAction(com.vaadin.event.Action actionImpl) {
        return actions.get(actionImpl);
    }

    @Override
    public com.vaadin.event.Action[] getActions(Object target, Object sender) {
        return getActionImplementations();
    }

    @Override
    public void handleAction(com.vaadin.event.Action actionImpl, Object sender, Object target) {
        Action cubaAction = getAction(actionImpl);
        if (cubaAction != null && cubaAction.isEnabled() && cubaAction.isVisible()) {
            cubaAction.actionPerform(actionSource);
        }
    }
}
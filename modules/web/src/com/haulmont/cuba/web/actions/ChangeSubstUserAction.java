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

package com.haulmont.cuba.web.actions;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.App;

public class ChangeSubstUserAction extends AbstractAction {
    protected User user;

    public ChangeSubstUserAction(User user) {
        super("changeSubstUserAction");
        this.user = user;
        setIconFromSet(CubaIcon.OK);
    }

    @Override
    public void actionPerform(com.haulmont.cuba.gui.components.Component component) {
        App app = App.getInstance();
        app.getWindowManager().checkModificationsAndCloseAll(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            app.getConnection().substituteUser(user);
                            doAfterChangeUser();
                        } catch (javax.persistence.NoResultException e) {
                            Messages messages = AppBeans.get(Messages.NAME);
                            app.getWindowManager().showNotification(
                                    messages.formatMainMessage("substitutionNotPerformed", user.getName()),
                                    Frame.NotificationType.WARNING
                            );
                            doRevert();
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        doRevert();
                    }
                }
        );
    }

    public void doAfterChangeUser() {
    }

    public void doRevert() {
    }
}
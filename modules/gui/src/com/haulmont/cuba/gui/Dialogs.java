/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.gui;

import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.ContentMode;
import com.haulmont.cuba.gui.components.SizeUnit;

/**
 * Utility dialogs API.
 */
public interface Dialogs {

    String NAME = "cuba_Dialogs";

    OptionDialog createOptionDialog();

    MessageDialog createMessageDialog();

    ExceptionDialog createExceptionDialog();

    /**
     * JavaDoc
     */
    interface OptionDialog {
        OptionDialog setCaption(String caption);
        String getCaption();

        OptionDialog setMessage(String message);
        String getMessage();

        OptionDialog setType(MessageType messageType);
        MessageType getType();

        OptionDialog setContentMode(ContentMode contentMode);
        ContentMode getContentMode();

        OptionDialog setActions(Action... actions);
        Action[] getActions();

        OptionDialog setWidth(String width);
        float getWidth();
        SizeUnit getWidthSizeUnit();

        OptionDialog setHeight(String height);
        float getHeight();
        SizeUnit getHeightSizeUnit();

        void show();
    }

    /**
     * JavaDoc
     */
    interface MessageDialog {
        MessageDialog setCaption(String caption);
        String getCaption();

        MessageDialog setMessage(String message);
        String getMessage();

        MessageDialog setType(MessageType messageType);
        MessageType getType();

        MessageDialog setContentMode(ContentMode contentMode);
        ContentMode getContentMode();

        MessageDialog setWidth(String width);
        float getWidth();
        SizeUnit getWidthSizeUnit();

        MessageDialog setHeight(String height);
        float getHeight();
        SizeUnit getHeightSizeUnit();

        void show();
    }

    /**
     * JavaDoc
     */
    interface ExceptionDialog {
        ExceptionDialog setThrowable(Throwable throwable);
        Throwable getThrowable();

        ExceptionDialog setCaption(String caption);
        String getCaption();

        ExceptionDialog setDescription(String description);
        String getDescription();
    }

    /**
     * JavaDoc
     */
    enum MessageType {
        CONFIRMATION,
        WARNING
    }
}
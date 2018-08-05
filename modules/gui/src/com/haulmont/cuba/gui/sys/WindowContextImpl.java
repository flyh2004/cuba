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

package com.haulmont.cuba.gui.sys;

import com.haulmont.cuba.gui.WindowContext;
import com.haulmont.cuba.gui.components.Frame;

import java.util.Map;

import static com.haulmont.cuba.gui.WindowManager.OpenType;

public class WindowContextImpl extends FrameContextImpl implements WindowContext {

    private OpenType openType;

    public WindowContextImpl(Frame window, OpenType openType, Map<String, Object> params) {
        super(window, params);
        this.openType = openType;
    }

    @Override
    public OpenType getOpenType() {
        return openType;
    }
}
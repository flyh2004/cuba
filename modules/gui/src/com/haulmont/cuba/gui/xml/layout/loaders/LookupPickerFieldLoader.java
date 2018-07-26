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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.ActionsHolder;
import com.haulmont.cuba.gui.components.LookupPickerField;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class LookupPickerFieldLoader extends LookupFieldLoader {

    @Override
    public void createComponent() {
        resultComponent = (LookupPickerField) factory.createComponent(LookupPickerField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        LookupPickerField lookupPickerField = (LookupPickerField) resultComponent;

        String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            lookupPickerField.setMetaClass(getMetadata().getClass(metaClass));
        }

        loadActions(lookupPickerField, element);

        if (lookupPickerField.getActions().isEmpty()) {
            boolean actionsByMetaAnnotations = ComponentsHelper.createActionsByMetaAnnotations(lookupPickerField);
            if (!actionsByMetaAnnotations) {
                lookupPickerField.addLookupAction();
                lookupPickerField.addOpenAction();
            }
        }

        String refreshOptionsOnLookupClose = element.attributeValue("refreshOptionsOnLookupClose");
        if (refreshOptionsOnLookupClose != null) {
            lookupPickerField.setRefreshOptionsOnLookupClose(Boolean.valueOf(refreshOptionsOnLookupClose));
        }
    }

    protected Metadata getMetadata() {
        return beanLocator.get(Metadata.NAME);
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadPickerDeclarativeAction(actionsHolder, element);
    }
}
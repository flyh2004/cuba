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

import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.components.DateField;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFieldLoader extends AbstractFieldLoader<DateField> {
    protected static final String DATE_PATTERN_DAY = "yyyy-MM-dd";
    protected static final String DATE_PATTERN_MIN = "yyyy-MM-dd HH:mm";

    @Override
    public void createComponent() {
        resultComponent = (DateField) factory.createComponent(DateField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        final String resolution = element.attributeValue("resolution");
        String dateFormat = element.attributeValue("dateFormat");
        String mainDateFormat = null;
        if (StringUtils.isNotEmpty(resolution)) {
            DateField.Resolution res = DateField.Resolution.valueOf(resolution);
            resultComponent.setResolution(res);
            if (dateFormat == null) {
                switch (res) {
                    case YEAR:
                    case MONTH:
                    case DAY:
                        mainDateFormat = "dateFormat";
                        break;
                    case HOUR:
                    case MIN:
                    case SEC:
                        mainDateFormat = "dateTimeFormat";
                        break;
                }
            }
        }

        String formatStr = null;
        if (StringUtils.isNotEmpty(dateFormat)) {
            formatStr = loadResourceString(dateFormat);
        } else if (StringUtils.isNotEmpty(mainDateFormat)) {
            formatStr = messages.getMainMessage(mainDateFormat);
        }
        if (StringUtils.isNotEmpty(formatStr)) {
            resultComponent.setDateFormat(formatStr);
        }

        loadBuffered(resultComponent, element);

        loadRangeStart(resultComponent, element);
        loadRangeEnd(resultComponent, element);
    }

    protected void loadRangeStart(DateField resultComponent, Element element) {
        String rangeStart = element.attributeValue("rangeStart");
        if (StringUtils.isNotEmpty(rangeStart)) {
            try {
                resultComponent.setRangeStart(parseDateOrDateTime(rangeStart));
            } catch (ParseException e) {
                throw new GuiDevelopmentException(
                        "'rangeStart' parsing error for date picker: " +
                                rangeStart, context.getFullFrameId(), "DatePicker ID", resultComponent.getId());
            }
        }
    }

    protected void loadRangeEnd(DateField resultComponent, Element element) {
        String rangeEnd = element.attributeValue("rangeEnd");
        if (StringUtils.isNotEmpty(rangeEnd)) {
            try {
                resultComponent.setRangeEnd(parseDateOrDateTime(rangeEnd));
            } catch (ParseException e) {
                throw new GuiDevelopmentException(
                        "'rangeEnd' parsing error for date picker: " +
                                rangeEnd, context.getFullFrameId(), "DatePicker ID", resultComponent.getId());
            }
        }
    }

    protected Date parseDateOrDateTime(String value) throws ParseException {
        SimpleDateFormat rangeDF;
        if (value.length() == 10) {
            rangeDF = new SimpleDateFormat(DATE_PATTERN_DAY);
        } else {
            rangeDF = new SimpleDateFormat(DATE_PATTERN_MIN);
        }
        return rangeDF.parse(value);
    }
}
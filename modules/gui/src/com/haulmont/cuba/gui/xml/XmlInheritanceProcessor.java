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
package com.haulmont.cuba.gui.xml;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.gui.xml.layout.ScreenXmlParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Provides inheritance of screen XML descriptors.
 */
@Component(XmlInheritanceProcessor.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XmlInheritanceProcessor {

    public static final String NAME = "cuba_XmlInheritanceProcessor";

    private static final Logger log = LoggerFactory.getLogger(XmlInheritanceProcessor.class);

    private Document document;
    private Namespace extNs;
    private Map<String, Object> params;

    private List<ElementTargetLocator> targetLocators;

    @Inject
    protected Resources resources;
    @Inject
    protected ScreenXmlParser screenXmlParser;
    @Inject
    protected BeanLocator beanLocator;

    public XmlInheritanceProcessor(Document document, Map<String, Object> params) {
        this.document = document;
        this.params = params;

        extNs = document.getRootElement().getNamespaceForPrefix("ext");

        this.targetLocators = Arrays.asList(
                new ViewPropertyElementTargetLocator(),
                new ViewElementTargetLocator(),
                new ButtonElementTargetLocator(),
                new CommonElementTargetLocator()
        );
    }

    public Element getResultRoot() {
        Element result;

        Element root = document.getRootElement();
        String ancestorTemplate = root.attributeValue("extends");
        if (!StringUtils.isBlank(ancestorTemplate)) {
            InputStream ancestorStream = resources.getResourceAsStream(ancestorTemplate);
            if (ancestorStream == null) {
                ancestorStream = getClass().getResourceAsStream(ancestorTemplate);
                if (ancestorStream == null) {
                    throw new DevelopmentException("Template is not found", "Ancestor's template path", ancestorTemplate);
                }
            }
            Document ancestorDocument;
            try {
                ancestorDocument = screenXmlParser.parseDescriptor(ancestorStream);
            } finally {
                IOUtils.closeQuietly(ancestorStream);
            }
            XmlInheritanceProcessor processor = beanLocator.getPrototype(XmlInheritanceProcessor.NAME,
                    ancestorDocument, params);
            result = processor.getResultRoot();
            process(result, root);

            if (log.isTraceEnabled()) {
                StringWriter writer = new StringWriter();
                Dom4j.writeDocument(result.getDocument(), true, writer);
                log.trace("Resulting template:\n" + writer.toString());
            }
        } else {
            result = root;
        }

        return result;
    }

    protected void process(Element resultElem, Element extElem) {
        // set text
        if (!StringUtils.isBlank(extElem.getText()))
            resultElem.setText(extElem.getText());

        // add all attributes from extension
        for (Attribute attribute : Dom4j.attributes(extElem)) {
            if (resultElem == document.getRootElement() && attribute.getName().equals("extends")) {
                // ignore "extends" in root element
                continue;
            }
            resultElem.addAttribute(attribute.getName(), attribute.getValue());
        }

        String idx = extElem.attributeValue(new QName("index", extNs));
        if (resultElem != document.getRootElement() && StringUtils.isNotBlank(idx)) {
            int index = Integer.parseInt(idx);

            Element parent = resultElem.getParent();
            if (index < 0 || index > parent.elements().size()) {
                String message = String.format(
                        "Incorrect extension XML for screen. Could not move existing element %s to position %s",
                        resultElem.getName(), index);

                throw new DevelopmentException(message,
                        ParamsMap.of("element", resultElem.getName(), "index", index));
            }

            parent.remove(resultElem);
            //noinspection unchecked
            parent.elements().add(index, resultElem);
        }

        // add and process elements
        Set<Element> justAdded = new HashSet<>();
        for (Element element : Dom4j.elements(extElem)) {
            // look for suitable locator
            ElementTargetLocator locator = null;
            for (ElementTargetLocator l : targetLocators) {
                if (l.suitableFor(element)) {
                    locator = l;
                    break;
                }
            }
            if (locator != null) {
                Element target = locator.locate(resultElem, element);
                // process target or a new element if target not found
                if (target != null) {
                    process(target, element);
                } else {
                    addNewElement(resultElem, element, justAdded);
                }
            } else {
                // if no suitable locator found, look for a single element with the same name
                List<Element> list = Dom4j.elements(resultElem, element.getName());
                if (list.size() == 1 && !justAdded.contains(list.get(0))) {
                    process(list.get(0), element);
                } else {
                    addNewElement(resultElem, element, justAdded);
                }
            }
        }
    }

    protected void addNewElement(Element resultElem, Element element, Set<Element> justAdded) {
        String idx = element.attributeValue(new QName("index", extNs));
        Element newElement;
        if (StringUtils.isBlank(idx)) {
            newElement = resultElem.addElement(element.getName());
        } else {
            newElement = DocumentHelper.createElement(element.getName());

            @SuppressWarnings("unchecked")
            List<Element> elements = resultElem.elements();
            int index = Integer.parseInt(idx);
            if (index < 0 || index > elements.size()) {
                String message = String.format(
                        "Incorrect extension XML for screen. Could not paste new element %s to position %s",
                        newElement.getName(), index);

                throw new DevelopmentException(message,
                        ParamsMap.of("element", newElement.getName(), "index", index));
            }
            elements.add(index, newElement);
        }
        justAdded.add(newElement);
        process(newElement, element);
    }

    protected interface ElementTargetLocator {
        boolean suitableFor(Element extElem);
        Element locate(Element resultParentElem, Element extElem);
    }

    protected static class CommonElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return !StringUtils.isBlank(extElem.attributeValue("id"));
        }

        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String id = extElem.attributeValue("id");
            for (Element e : Dom4j.elements(resultParentElem)) {
                if (id.equals(e.attributeValue("id"))) {
                    return e;
                }
            }
            return null;
        }
    }

    protected static class ViewElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return "view".equals(extElem.getName());
        }

        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String entity = extElem.attributeValue("entity");
            String clazz = extElem.attributeValue("class");
            String name = extElem.attributeValue("name");
            for (Element e : Dom4j.elements(resultParentElem)) {
                if (name.equals(e.attributeValue("name"))
                        && ((entity != null && entity.equals(e.attributeValue("entity")))
                            || (clazz != null && clazz.equals(e.attributeValue("class")))))
                {
                    return e;
                }
            }

            return null;
        }
    }

    protected static class ViewPropertyElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return "property".equals(extElem.getName());
        }

        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String name = extElem.attributeValue("name");
            for (Element e : Dom4j.elements(resultParentElem)) {
                if (name.equals(e.attributeValue("name"))) {
                    return e;
                }
            }
            return null;
        }
    }

    protected static class ButtonElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return "button".equals(extElem.getName())
                    && extElem.attributeValue("id") == null
                    && extElem.attributeValue("action") != null;
        }

        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String action = extElem.attributeValue("action");
            for (Element e : Dom4j.elements(resultParentElem)) {
                if (action.equals(e.attributeValue("action"))) {
                    return e;
                }
            }
            return null;
        }
    }
}
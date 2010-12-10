/*
 * (C) Copyright 2009-2010 Nuxeo SA (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.platform.forms.layout.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetTypeConfiguration;

public class WidgetTypeConfigurationImpl implements WidgetTypeConfiguration {

    private static final long serialVersionUID = 1L;

    protected String title;

    protected String description;

    protected Map<String, Serializable> properties;

    protected boolean list = false;

    protected boolean complex = false;

    protected List<String> supportedFieldTypes;

    protected List<String> defaultFieldTypes;

    protected List<FieldDefinition> defaultFieldDefinitions;

    protected List<String> categories;

    protected Map<String, List<LayoutDefinition>> propertyLayouts;

    // needed by GWT serialization
    public WidgetTypeConfigurationImpl() {
        super();
    }

    public WidgetTypeConfigurationImpl(String title, String description,
            Map<String, Serializable> properties, boolean list,
            boolean complex, List<String> supportedFieldTypes,
            List<String> defaultFieldTypes,
            List<FieldDefinition> defaultFieldDefinitions,
            List<String> categories,
            Map<String, List<LayoutDefinition>> propertyLayouts) {
        super();
        this.title = title;
        this.description = description;
        this.properties = properties;
        this.list = list;
        this.complex = complex;
        this.supportedFieldTypes = supportedFieldTypes;
        this.defaultFieldTypes = defaultFieldTypes;
        this.defaultFieldDefinitions = defaultFieldDefinitions;
        this.categories = categories;
        this.propertyLayouts = propertyLayouts;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, Serializable> getConfProperties() {
        return properties;
    }

    @Override
    public Serializable getConfProperty(String propName) {
        if (properties == null) {
            return null;
        }
        return properties.get(propName);
    }

    public boolean isList() {
        return list;
    }

    public boolean isComplex() {
        return complex;
    }

    public List<String> getSupportedFieldTypes() {
        return supportedFieldTypes;
    }

    public List<String> getDefaultFieldTypes() {
        return defaultFieldTypes;
    }

    public List<FieldDefinition> getDefaultFieldDefinitions() {
        return defaultFieldDefinitions;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Map<String, List<LayoutDefinition>> getPropertyLayouts() {
        return propertyLayouts;
    }

    public List<LayoutDefinition> getPropertyLayouts(String mode,
            String additionalMode) {
        if (propertyLayouts != null) {
            List<LayoutDefinition> res = new ArrayList<LayoutDefinition>();
            if (additionalMode != null) {
                List<LayoutDefinition> defaultLayouts = propertyLayouts.get(additionalMode);
                if (defaultLayouts != null) {
                    res.addAll(defaultLayouts);
                }
            }
            List<LayoutDefinition> modeLayouts = propertyLayouts.get(mode);
            if (modeLayouts != null) {
                res.addAll(modeLayouts);
            }
            return res;
        }
        return null;
    }

}
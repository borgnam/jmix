/*
 * Copyright 2020 Haulmont.
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
 */

package io.jmix.dynattrflowui.view.categoryattr;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.utils.GridHelper;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_CategoryAttribute.fragment")
@ViewDescriptor("category-attributes-view-fragment.xml")
public class CategoryAttributesViewFragment extends StandardView {

    @Autowired
    protected Messages messages;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected GridHelper gridHelper;

    @ViewComponent
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;
    @ViewComponent
    protected CollectionLoader<CategoryAttribute> categoryAttributesDl;
    @ViewComponent
    protected DataGrid<CategoryAttribute> categoryAttrsGrid;
    @ViewComponent
    protected InstanceContainer<Category> categoryDc;
    @ViewComponent
    protected Button moveUpBtn;
    @ViewComponent
    protected Button moveDownBtn;

    @Subscribe
    public void onInitEvent(InitEvent event) {
        sortCategoryAttrsGridByOrderNo();
        categoryAttrsGrid
                .addColumn(createCategoryAttrsGridDataTypeRenderer())
                .setHeader(messageTools.getPropertyCaption(metadata.getClass(CategoryAttribute.class), "dataType"));

        categoryAttrsGrid
                .addColumn(createCategoryAttrsGridDefaultValueRenderer())
                .setHeader(messages.getMessage(getClass(), "categoryAttrsGrid.defaultValue"));

    }

    protected ComponentRenderer<Span, CategoryAttribute> createCategoryAttrsGridDefaultValueRenderer() {
        return new ComponentRenderer<>(this::categoryAttrsGridDefaultValueColumnComponent,
                this::categoryAttrsGridDefaultValueColumnUpdater);
    }

    protected Span categoryAttrsGridDefaultValueColumnComponent() {
        return uiComponents.create(Span.class);
    }

    protected void categoryAttrsGridDefaultValueColumnUpdater(Span defaultValueLabel, CategoryAttribute attribute) {
        String defaultValue = "";

        AttributeType dataType = attribute.getDataType();
        switch (dataType) {
            case BOOLEAN -> {
                Boolean b = attribute.getDefaultBoolean();
                if (b != null)
                    defaultValue = BooleanUtils.isTrue(b)
                            ? messages.getMessage("trueString")
                            : messages.getMessage("falseString");
            }
            case DATE -> {
                Date dateTime = attribute.getDefaultDate();
                if (dateTime != null) {
                    String dateTimeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateTimeFormat();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                    defaultValue = simpleDateFormat.format(dateTime);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(CategoryAttributesViewFragment.class, "categoryAttrsGrid.currentDate");
                }
            }
            case DATE_WITHOUT_TIME -> {
                LocalDate dateWoTime = attribute.getDefaultDateWithoutTime();
                if (dateWoTime != null) {
                    String dateWoTimeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateFormat();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateWoTimeFormat);
                    defaultValue = dateWoTime.format(formatter);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(CategoryAttributesViewFragment.class, "categoryAttrsGrid.currentDate");
                }
            }
            case DECIMAL -> {
                BigDecimal defaultDecimal = attribute.getDefaultDecimal();
                if (defaultDecimal != null) {
                    defaultValue = defaultDecimal.toString();
                }
            }
            case DOUBLE -> {
                Double defaultDouble = attribute.getDefaultDouble();
                if (defaultDouble != null) {
                    defaultValue = defaultDouble.toString();
                }
            }
            case ENTITY -> {
                Class<?> entityClass = attribute.getJavaType();
                if (entityClass != null) {
                    defaultValue = "";
                    if (attribute.getObjectDefaultEntityId() != null) {
                        MetaClass metaClass = metadata.getClass(entityClass);
                        LoadContext<?> lc = new LoadContext<>(metadata.getClass(attribute.getJavaType()));
                        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.INSTANCE_NAME);
                        lc.setFetchPlan(fetchPlan);
                        String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
                        lc.setQueryString(String.format("select e from %s e where e.%s = :entityId", metaClass.getName(), pkName))
                                .setParameter("entityId", attribute.getObjectDefaultEntityId());
                        Object entity = dataManager.load(lc);
                        if (entity != null) {
                            defaultValue = metadataTools.getInstanceName(entity);
                        }
                    }
                } else {
                    defaultValue = messages.getMessage(CategoryAttributesViewFragment.class, "categoryAttrsGrid.entityNotFound");
                }
            }
            case ENUMERATION, STRING -> defaultValue = attribute.getDefaultString();
            case INTEGER -> {
                Integer defaultInt = attribute.getDefaultInt();
                if (defaultInt != null) {
                    defaultValue = defaultInt.toString();
                }
            }
        }

        defaultValueLabel.setText(defaultValue);
    }

    protected ComponentRenderer<Text, CategoryAttribute> createCategoryAttrsGridDataTypeRenderer() {
        return new ComponentRenderer<>(this::categoryAttrsGridDataTypeComponent,
                this::categoryAttrsGridDataTypeUpdater);
    }

    protected Text categoryAttrsGridDataTypeComponent() {
        return new Text(null);
    }

    protected void categoryAttrsGridDataTypeUpdater(Text text, CategoryAttribute categoryAttribute) {
        String dataType;
        if (BooleanUtils.isTrue(categoryAttribute.getIsEntity())) {
            Class<?> javaType = categoryAttribute.getJavaType();
            if (javaType != null) {
                MetaClass metaClass = metadata.getClass(javaType);
                dataType = messageTools.getEntityCaption(metaClass);
            } else {
                dataType = messages.getMessage("classNotFound");
            }
        } else {
            String key = AttributeType.class.getSimpleName() + "." + categoryAttribute.getDataType().toString();
            dataType = messages.getMessage(AttributeType.class, key);
        }

        text.setText(dataType);
    }

    @Subscribe("categoryAttrsGrid.create")
    protected void categoryAttrsGridCreateListener(ActionPerformedEvent event) {
        dialogWindows.detail(this, CategoryAttribute.class)
                .withViewClass(CategoryAttributesDetailView.class)
                .withAfterCloseListener(e -> {
                    if (e.getView().isCommited) {
                        CategoryAttribute categoryAttribute = e.getView().getEditedEntity();
                        categoryAttribute.setCategory(categoryDc.getItem());
                        getViewData().getDataContext().merge(categoryAttribute);

                        categoryAttributesDc.replaceItem(categoryAttribute);

//                        int orderNo = getMaxOrderNo(categoryAttribute) + 1;
//                        categoryAttributesDc.getItem(categoryAttribute.getId()).setOrderNo(orderNo);
                        categoryAttrsGrid.getDataProvider().refreshAll();
                    }
                })
                .newEntity()
                .build()
                .open();
    }

    @Subscribe("categoryAttrsGrid.edit")
    protected void categoryAttrsGridEditListener(ActionPerformedEvent event) {
        dialogWindows.detail(this, CategoryAttribute.class)
                .withViewClass(CategoryAttributesDetailView.class)
                .withAfterCloseListener(e -> {
                    if (e.getView().isCommited) {
                        CategoryAttribute categoryAttribute = e.getView().getEditedEntity();
                        getViewData().getDataContext().setModified(categoryAttribute, true);
                        getViewData().getDataContext().merge(categoryAttribute);
                        categoryAttributesDc.replaceItem(categoryAttribute);
                        categoryAttrsGrid.getDataProvider().refreshAll();
                    }
                })
                .editEntity(Objects.requireNonNull(categoryAttrsGrid.getSingleSelectedItem()))
                .build()
                .open();
    }

    @Subscribe("categoryAttrsGrid.remove")
    protected void categoryAttrsGridRemoveListener(ActionPerformedEvent event) {
        CategoryAttribute selected = Objects.requireNonNull(categoryAttrsGrid.getSingleSelectedItem());
        categoryAttributesDc.getMutableItems().remove(selected);
        getViewData().getDataContext().remove(selected);
        categoryAttrsGrid.getDataProvider().refreshAll();
    }


    @Subscribe("categoryAttrsGrid")
    protected void onCategoryAttrsGridSelection(SelectionEvent<DataGrid<CategoryAttribute>, CategoryAttribute> event) {
        Set<CategoryAttribute> selected = categoryAttrsGrid.getSelectedItems();
        if (selected.isEmpty()) {
            refreshMoveButtonsEnabled(null);
        } else {
            refreshMoveButtonsEnabled(selected.iterator().next());
        }
    }

    @Subscribe("categoryAttrsGrid.moveUp")
    protected void onCategoryAttrsGridMoveUp(ActionPerformedEvent event) {
        gridHelper.moveTableItemUp(categoryAttributesDc, categoryAttrsGrid, () ->
                categoryAttributesDc.getMutableItems().forEach(item -> {
                    item.setOrderNo(categoryAttributesDc.getMutableItems().indexOf(item));
                    getViewData().getDataContext().setModified(item, true);
                    getViewData().getDataContext().merge(item);
                }));

    }

    @Subscribe("categoryAttrsGrid.moveDown")
    protected void onCategoryAttrsGridMoveDown(ActionPerformedEvent event) {
        gridHelper.moveTableItemDown(categoryAttributesDc, categoryAttrsGrid, () ->
                categoryAttributesDc.getMutableItems().forEach(item -> {
                    item.setOrderNo(categoryAttributesDc.getMutableItems().indexOf(item));
                    getViewData().getDataContext().setModified(item, true);
                    getViewData().getDataContext().merge(item);
                }));
    }

    protected CategoryAttribute getPrevAttribute(Integer orderNo) {
        return categoryAttributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) > 0)
                .max(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected CategoryAttribute getNextAttribute(Integer orderNo) {
        return categoryAttributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) < 0)
                .min(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected void sortCategoryAttrsGridByOrderNo() {
        Objects.requireNonNull(categoryAttributesDc.getSorter())
                .sort(Sort.by(Sort.Direction.ASC, "orderNo"));
    }

    protected void refreshMoveButtonsEnabled(@Nullable CategoryAttribute categoryAttribute) {
        moveUpBtn.setEnabled(categoryAttribute != null && getPrevAttribute(categoryAttribute.getOrderNo()) != null);
        moveDownBtn.setEnabled(categoryAttribute != null && getNextAttribute(categoryAttribute.getOrderNo()) != null);
    }

    public void setCategory(Category category) {
        categoryDc.setItem(category);
        categoryAttributesDl.setQuery("select e from dynat_CategoryAttribute e where e.category = :category");
        categoryAttributesDl.setParameter("category", category);
        categoryAttributesDl.load();
    }

    public void setDataContext(DataContext dataContext) {
        this.getViewData().setDataContext(dataContext);
    }

    public CollectionContainer<CategoryAttribute> getAttributes() {
        return categoryAttributesDc;
    }
}

/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web.common;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.entities.Configuration;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.entities.LDAPConfiguration;
import org.libreplan.business.common.entities.ProgressType;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Configuration} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Cristina Alavarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public class ConfigurationController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(ConfigurationController.class);

    private final ProgressTypeRenderer progressTypeRenderer = new ProgressTypeRenderer();

    private Window configurationWindow;

    private BandboxSearch defaultCalendarBandboxSearch;

    private Listbox lbTypeProgress;

    private IConfigurationModel configurationModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Grid entitySequencesGrid;

    private Combobox entityCombo;

    private Intbox numDigitBox;

    private Textbox prefixBox;

    private Checkbox scenariosVisible;

    private UserRole roles;

    private Textbox ldapGroupPath;

    private Radiogroup strategy;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        configurationModel.init();

        defaultCalendarBandboxSearch.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        Listitem selectedItem = (Listitem) ((SelectEvent) event)
                                .getSelectedItems().iterator().next();
                        setDefaultCalendar((BaseCalendar) selectedItem
                                .getValue());
                    }
                });
        initializeProgressTypeList();
        messages = new MessagesForUser(messagesContainer);
        reloadEntitySequences();
        if (moreScenariosThanMasterCreated()) {
            scenariosVisible.setChecked(true);
            scenariosVisible.setDisabled(true);
            scenariosVisible
                    .setTooltiptext(_("Scenarios must be enabled as more elements than master exist"));
        }
        loadRoleStrategyRows();
    }

    public void changeRoleStrategy() {
        this.getLdapConfiguration().setLdapGroupStrategy(
                strategy.getSelectedItem().getValue().equals("group"));
        loadRoleStrategyRows();
    }

    private void loadRoleStrategyRows() {
        if (getLdapConfiguration().getLdapGroupStrategy()) {
            strategy.setSelectedIndex(0);
            ldapGroupPath.setDisabled(false);
        } else {
            strategy.setSelectedIndex(1);
            ldapGroupPath.setDisabled(true);
        }
    }

    private void initializeProgressTypeList() {
        lbTypeProgress.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                Listitem selectedItem = getSelectedItem((SelectEvent) event);
                if (selectedItem != null) {
                    ProgressType progressType = (ProgressType) selectedItem
                            .getValue();
                    configurationModel.setProgressType(progressType);
                }
            }

            private Listitem getSelectedItem(SelectEvent event) {
                final Set<Listitem> selectedItems = event.getSelectedItems();
                return selectedItems.iterator().next();
            }

        });
    }

    public List<ProgressType> getProgressTypes() {
        return configurationModel.getProgresTypes();
    }

    public ProgressType getSelectedProgressType() {
        return configurationModel.getProgressType();
    }

    public void setSelectedProgressType(ProgressType progressType) {
        configurationModel.setProgressType(progressType);
    }

    public List<BaseCalendar> getCalendars() {
        return configurationModel.getCalendars();
    }

    public BaseCalendar getDefaultCalendar() {
        return configurationModel.getDefaultCalendar();
    }

    public void setDefaultCalendar(BaseCalendar calendar) {
        configurationModel.setDefaultCalendar(calendar);
    }

    public void save() throws InterruptedException {
        ConstraintChecker.isValid(configurationWindow);
        if (checkValidEntitySequenceRows()) {
            try {
                configurationModel.confirm();
                configurationModel.init();
                messages.showMessage(Level.INFO, _("Changes saved"));
                reloadWindow();
                reloadEntitySequences();
            } catch (ValidationException e) {
                messages.showInvalidValues(e);
            } catch (ConcurrentModificationException e) {
                messages.showMessage(Level.ERROR, e.getMessage());
                configurationModel.init();
                reloadWindow();
                reloadEntitySequences();
            }
        }
    }

    public void cancel() throws InterruptedException {
        configurationModel.cancel();
        messages.showMessage(Level.INFO, _("Changes have been canceled"));
        reloadWindow();
        reloadEntitySequences();
    }

    public void testLDAPConnection() {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(configurationModel.getLdapConfiguration().getLdapHost()
                + ":" + configurationModel.getLdapConfiguration().getLdapPort());
        source.setBase(configurationModel.getLdapConfiguration().getLdapBase());
        source.setUserDn(configurationModel.getLdapConfiguration()
                .getLdapUserDn());
        source.setPassword(configurationModel.getLdapConfiguration()
                .getLdapPassword());
        source.setDirObjectFactory(DefaultDirObjectFactory.class);
        source.setPooled(false);
        try {
            source.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LdapTemplate template = new LdapTemplate(source);
        try {
            template.authenticate(DistinguishedName.EMPTY_PATH,
                    new EqualsFilter(configurationModel.getLdapConfiguration()
                            .getLdapUserId(), "test").toString(), "test");
            messages.showMessage(Level.INFO,
                    _("LDAP connection was successful"));
        } catch (Exception e) {
            LOG.info(e);
            messages.showMessage(Level.ERROR,
                    _("Cannot connect to LDAP server"));
        }
    }

    private boolean checkValidEntitySequenceRows() {
        Rows rows = entitySequencesGrid.getRows();
        for (Row row : (List<Row>) rows.getChildren()) {

                EntitySequence seq = (EntitySequence) row.getValue();
                if (seq != null) {
                Textbox prefixBox = (Textbox) row.getChildren().get(2);
                    if (!seq.isAlreadyInUse()) {
                        String errorMessage = this.validPrefix(seq,
                                prefixBox.getValue());
                        if (errorMessage != null) {
                            throw new WrongValueException(prefixBox,
                                    errorMessage);
                        }
                    }

                Intbox digitsBox = (Intbox) row.getChildren().get(3);
                    try {
                        if (!seq.isAlreadyInUse()) {
                            seq.setNumberOfDigits(digitsBox.getValue());
                        }
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(digitsBox, _(
                                "number of digits must be between {0} and {1}",
                                EntitySequence.MIN_NUMBER_OF_DIGITS,
                                EntitySequence.MAX_NUMBER_OF_DIGITS));
                    }
                }

        }
        return true;
    }

    private void reloadWindow() {
        Util.reloadBindings(configurationWindow);
    }

    private void reloadEntitySequences() {
        entitySequencesGrid.setModel(new SimpleListModel(
                getAllEntitySequences().toArray()));
        entitySequencesGrid.invalidate();
    }

    public String getCompanyCode() {
        return configurationModel.getCompanyCode();
    }

    public void setCompanyCode(String companyCode) {
        configurationModel.setCompanyCode(companyCode);
    }

    public String getCompanyLogoURL() {
        return configurationModel.getCompanyLogoURL();
    }

    public void setCompanyLogoURL(String companyLogoURL) {
        configurationModel.setCompanyLogoURL(companyLogoURL);
    }

    public Boolean getGenerateCodeForCriterion() {
        return configurationModel.getGenerateCodeForCriterion();
    }

    public void setGenerateCodeForCriterion(Boolean generateCodeForCriterion) {
        configurationModel
                .setGenerateCodeForCriterion(generateCodeForCriterion);
    }

    public Boolean getGenerateCodeForWorkReportType() {
        return configurationModel.getGenerateCodeForWorkReportType();
    }

    public void setGenerateCodeForWorkReportType(
            Boolean generateCodeForWorkReportType) {
        configurationModel
                .setGenerateCodeForWorkReportType(generateCodeForWorkReportType);
    }

    public Boolean getGenerateCodeForCalendarExceptionType() {
        return configurationModel.getGenerateCodeForCalendarExceptionType();
    }

    public void setGenerateCodeForCalendarExceptionType(
            Boolean generateCodeForCalendarExceptionType) {
        configurationModel
                .setGenerateCodeForCalendarExceptionType(generateCodeForCalendarExceptionType);
    }

    public Boolean getGenerateCodeForCostCategory() {
        return configurationModel.getGenerateCodeForCostCategory();
    }

    public void setGenerateCodeForCostCategory(
            Boolean generateCodeForCostCategory) {
        configurationModel
                .setGenerateCodeForCostCategory(generateCodeForCostCategory);
    }

    public Boolean getGenerateCodeForLabel() {
        return configurationModel.getGenerateCodeForLabel();
    }

    public void setGenerateCodeForLabel(Boolean generateCodeForLabel) {
        configurationModel.setGenerateCodeForLabel(generateCodeForLabel);
    }

    public Boolean getGenerateCodeForWorkReport() {
        return configurationModel.getGenerateCodeForWorkReport();
    }

    public void setGenerateCodeForWorkReport(Boolean generateCodeForWorkReport) {
        configurationModel
                .setGenerateCodeForWorkReport(generateCodeForWorkReport);
    }

    public Boolean getGenerateCodeForResources() {
        return configurationModel.getGenerateCodeForResources();
    }

    public void setGenerateCodeForResources(Boolean generateCodeForResources) {
        configurationModel
                .setGenerateCodeForResources(generateCodeForResources);
    }

    public Boolean getGenerateCodeForTypesOfWorkHours() {
        return configurationModel.getGenerateCodeForTypesOfWorkHours();
    }

    public void setGenerateCodeForTypesOfWorkHours(
            Boolean generateCodeForTypesOfWorkHours) {
        configurationModel
                .setGenerateCodeForTypesOfWorkHours(generateCodeForTypesOfWorkHours);
    }

    public Boolean getGenerateCodeForMaterialCategories() {
        return configurationModel.getGenerateCodeForMaterialCategories();
    }

    public void setGenerateCodeForMaterialCategories(
            Boolean generateCodeForMaterialCategories) {
        configurationModel
                .setGenerateCodeForMaterialCategories(generateCodeForMaterialCategories);
    }

    public void reloadGeneralConfiguration() {
        reloadWindow();
    }

    public Boolean getGenerateCodeForUnitTypes() {
        return configurationModel.getGenerateCodeForUnitTypes();
    }

    public void setGenerateCodeForUnitTypes(Boolean generateCodeForUnitTypes) {
        configurationModel
                .setGenerateCodeForUnitTypes(generateCodeForUnitTypes);
    }

    public Boolean getGenerateCodeForBaseCalendars() {
        return configurationModel.getGenerateCodeForBaseCalendars();
    }

    public void setGenerateCodeForBaseCalendars(
            Boolean generateCodeForBaseCalendars) {
        configurationModel
                .setGenerateCodeForBaseCalendars(generateCodeForBaseCalendars);
    }

    public Boolean isAutocompleteLogin() {
        return configurationModel.isAutocompleteLogin();
    }

    public void setAutocompleteLogin(Boolean autocompleteLogin) {
        configurationModel.setAutocompleteLogin(autocompleteLogin);
    }

    public void removeEntitySequence(EntitySequence entitySequence) {
        try {
            configurationModel.removeEntitySequence(entitySequence);
        } catch (IllegalArgumentException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
        }
        reloadEntitySequences();
    }


    public void setMonteCarloMethodTabVisible(
            Boolean expandResourceLoadViewCharts) {
        configurationModel
                .setMonteCarloMethodTabVisible(expandResourceLoadViewCharts);
    }

    public Boolean isMonteCarloMethodTabVisible() {
        return configurationModel.isMonteCarloMethodTabVisible();
    }

    public void setScenariosVisible(Boolean scenariosVisible) {
        configurationModel.setScenariosVisible(scenariosVisible);
    }

    public Boolean isScenariosVisible() {
        return configurationModel.isScenariosVisible();
    }

    public ProgressTypeRenderer getProgressTypeRenderer() {
        return progressTypeRenderer;
    }

    private static class ProgressTypeRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) {
            ProgressType progressType = (ProgressType) data;
            item.setLabel(_(progressType.getValue()));
            item.setValue(progressType);
        }

    }

    public class EntitySequenceGroupRenderer implements RowRenderer {
        @Override
        public void render(Row row, Object data) {

            EntitySequence entitySequence = (EntitySequence) data;
            final EntityNameEnum entityName = entitySequence.getEntityName();

            row.setValue(entityName);
            row.appendChild(new Label(_(entityName.getSequenceLiteral())));

            row.setValue(entitySequence);
            appendActiveRadiobox(row, entitySequence);
            appendPrefixTextbox(row, entitySequence);
            appendNumberOfDigitsInbox(row, entitySequence);
            appendLastValueInbox(row, entitySequence);
            appendOperations(row, entitySequence);

            if (entitySequence.isAlreadyInUse()) {
                row.setTooltiptext(_("The code sequence is already in use and it can not be updated."));
            }

            if ((row.getPreviousSibling() != null)
                    && !((EntitySequence) ((Row) row.getPreviousSibling())
                            .getValue()).getEntityName().equals(entityName)) {
                row.setClass("separator");
            }
        }
    }

        private void appendActiveRadiobox(final Row row,
                final EntitySequence entitySequence) {

            final Radio radiobox = Util.bind(new Radio(),
                    new Util.Getter<Boolean>() {

                        @Override
                        public Boolean get() {
                            return entitySequence.isActive();
                        }
                    }, new Util.Setter<Boolean>() {

                        @Override
                        public void set(Boolean value) {
                            updateOtherSequences(entitySequence);
                            entitySequence.setActive(value);
                            Util.reloadBindings(entitySequencesGrid);
                            reloadEntitySequences();
                        }
                    });

            row.appendChild(radiobox);
        }

        private void updateOtherSequences(final EntitySequence activeSequence) {
            for (EntitySequence sequence : getEntitySequences(activeSequence
                    .getEntityName())) {
                sequence.setActive(false);
            }
        }

        private void appendPrefixTextbox(Row row,
                final EntitySequence entitySequence) {
            final Textbox tempTextbox = new Textbox();
            tempTextbox.setWidth("200px");
            Textbox textbox = Util.bind(tempTextbox, new Util.Getter<String>() {

                @Override
                public String get() {
                    return entitySequence.getPrefix();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    try {
                        entitySequence.setPrefix(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempTextbox, e
                                .getMessage());
                    }
                }
            });
            textbox.setConstraint(checkConstraintFormatPrefix());

            if (entitySequence.isAlreadyInUse()) {
                textbox.setDisabled(true);
            }

            row.appendChild(textbox);
        }

        private void appendNumberOfDigitsInbox(Row row,
                final EntitySequence entitySequence) {
            final Intbox tempIntbox = new Intbox();
            Intbox intbox = Util.bind(tempIntbox, new Util.Getter<Integer>() {

                @Override
                public Integer get() {
                    return entitySequence.getNumberOfDigits();
                }
            }, new Util.Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    try {
                        entitySequence.setNumberOfDigits(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempIntbox, _(
                                "number of digits must be between {0} and {1}",
                                EntitySequence.MIN_NUMBER_OF_DIGITS,
                                EntitySequence.MAX_NUMBER_OF_DIGITS));
                    }
                }
            });
            intbox.setConstraint(checkConstraintNumberOfDigits());

            if (entitySequence.isAlreadyInUse()) {
                intbox.setDisabled(true);
            }

            row.appendChild(intbox);
        }

        private void appendLastValueInbox(Row row,
                final EntitySequence entitySequence) {
            Textbox textbox = Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return EntitySequence.formatValue(
                                    entitySequence.getNumberOfDigits(),
                                    entitySequence.getLastValue());
                        }
                    });

            row.appendChild(textbox);
        }

        private void appendOperations(final Row row,
                final EntitySequence entitySequence) {
            final Button removeButton = Util
                    .createRemoveButton(new EventListener() {

                        @Override
                        public void onEvent(Event event) {
                            if (isLastOne(entitySequence)) {
                                showMessageNotDelete();
                            } else {
                                removeEntitySequence(entitySequence);
                            }
                        }
                    });

            if (entitySequence.isAlreadyInUse()) {
                removeButton.setDisabled(true);
            }

            row.appendChild(removeButton);
        }


    public Constraint checkConstraintFormatPrefix() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {

                Row row = (Row) comp.getParent();
                EntitySequence sequence = (EntitySequence) row.getValue();
                if (!sequence.isAlreadyInUse()) {
                    String errorMessage = validPrefix(sequence, (String) value);
                    if (errorMessage != null) {
                        throw new WrongValueException(comp, errorMessage);
                    }
                }
            }
        };
    }

    private String validPrefix(EntitySequence sequence, String prefixValue) {
        sequence.setPrefix(prefixValue);
        if (!configurationModel.checkFrefixFormat(sequence)) {
            String message = _("format prefix invalid. It cannot be empty or contain '_' or whitespaces.");
            if (sequence.getEntityName().canContainLowBar()) {
                message = _("format prefix invalid. It cannot be empty or contain whitespaces.");
            }
            return message;
        }
        return null;
    }

    public Constraint checkConstraintNumberOfDigits() {
        return new Constraint() {

            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Row row = (Row) comp.getParent();
                EntitySequence sequence = (EntitySequence) row.getValue();
                if (!sequence.isAlreadyInUse()) {
                    Integer numberOfDigits = (Integer) value;
                    try {
                        sequence.setNumberOfDigits(numberOfDigits);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(comp, _(
                                "number of digits must be between {0} and {1}",
                                EntitySequence.MIN_NUMBER_OF_DIGITS,
                                EntitySequence.MAX_NUMBER_OF_DIGITS));
                    }
                }
            }
        };
    }

    public void addEntitySequence(EntityNameEnum entityName, String prefix,
            Integer digits) {
        configurationModel.addEntitySequence(entityName, prefix, digits);
        reloadEntitySequences();
    }

    public List<EntitySequence> getEntitySequences(EntityNameEnum entityName) {
        return configurationModel.getEntitySequences(entityName);
    }

    private boolean isLastOne(EntitySequence sequence) {
        return (getEntitySequences(sequence.getEntityName()).size() == 1);
    }

    private void showMessageNotDelete() {
        try {
            Messagebox
                    .show(_("It can not be deleted. At least one sequence is necessary."),
                            _("Deleting sequence"), Messagebox.OK,
                            Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
        }
    }

    public static class EntitySequenceComparator implements
            Comparator<EntitySequence> {

        @Override
        public int compare(EntitySequence seq1, EntitySequence seq2) {
            return seq1.getEntityName().compareTo(seq2.getEntityName());
        }
    }

    public EntitySequenceGroupRenderer getEntitySequenceGroupRenderer() {
        return new EntitySequenceGroupRenderer();
    }

    private List<EntitySequence> getAllEntitySequences() {
        List<EntitySequence> allSequences = new ArrayList<EntitySequence>();
        for (final EntityNameEnum entityName : EntityNameEnum.values()) {
            allSequences.addAll(this.getEntitySequences(entityName));
        }
        return allSequences;
    }

    public void addNewEntitySequence() {
        if (entityCombo != null && numDigitBox != null) {
            if (entityCombo.getSelectedItem() == null) {
                throw new WrongValueException(entityCombo,
                        _("Select entity, please"));
            }

            if (prefixBox.getValue() == null || prefixBox.getValue().isEmpty()) {
                throw new WrongValueException(prefixBox,
                        _("cannot be null or empty"));
            }

            try {
                addEntitySequence((EntityNameEnum) entityCombo
                        .getSelectedItem().getValue(), prefixBox.getValue(),
                        numDigitBox.getValue());
            } catch (IllegalArgumentException e) {
                throw new WrongValueException(numDigitBox, e.getMessage());
            }
        }
    }

    public EntityNameEnum[] getEntityNames() {
        return EntityNameEnum.values();
    }

    public boolean moreScenariosThanMasterCreated() {
        return configurationModel.moreScenariosThanMasterCreated();
    }

    // Tab ldap properties
    public LDAPConfiguration getLdapConfiguration() {
        return configurationModel.getLdapConfiguration();
    }

    public void setLdapConfiguration(LDAPConfiguration ldapConfiguration) {
        configurationModel.setLdapConfiguration(ldapConfiguration);
    }

    public RowRenderer getAllUserRolesRenderer() {
        return new RowRenderer() {
            @Override
            public void render(Row row, Object data) throws Exception {

                final UserRole role = (UserRole) data;
                row.appendChild(new Label(role.getDisplayName()));

                final Textbox tempTextbox = new Textbox();
                Textbox textbox = Util.bind(tempTextbox, new Util.Getter<String>() {
                    @Override
                    public String get() {
                        List<String> listRoles = configurationModel.
                            getLdapConfiguration().getMapMatchingRoles().get(role.name());
                        Collections.sort(listRoles);
                        return StringUtils.join(listRoles, ";");
                    }
                }, new Util.Setter<String>() {
                    @Override
                    public void set(String value) {
                                // Created a set in order to avoid duplicates
                                Set<String> rolesLdap = new HashSet<String>(
                                        Arrays.asList(StringUtils.split(value,
                                                ";")));
                                configurationModel.getLdapConfiguration()
                                        .setConfigurationRolesLdap(role.name(),
                                                rolesLdap);
                    }
                });
                textbox.setWidth("300px");
                row.appendChild(textbox);
            }
        };
    }

    public UserRole[] getRoles() {
        return roles.values();
    }

    public void setRoles(UserRole roles) {
        this.roles = roles;
    }

    public boolean isChangedDefaultPasswdAdmin() {
        return configurationModel.isChangedDefaultPasswdAdmin();
    }

    public boolean isLdapGroupStrategy() {
        return getLdapConfiguration().getLdapGroupStrategy();
    }

    public boolean isLdapPropertyStrategy() {
        return !getLdapConfiguration().getLdapGroupStrategy();
    }

    public boolean isCheckNewVersionEnabled() {
        return configurationModel.isCheckNewVersionEnabled();
    }

    public void setCheckNewVersionEnabled(boolean checkNewVersionEnabled) {
        configurationModel.setCheckNewVersionEnabled(checkNewVersionEnabled);
    }

    public boolean isAllowToGatherUsageStatsEnabled() {
        return configurationModel.isAllowToGatherUsageStatsEnabled();
    }

    public void setAllowToGatherUsageStatsEnabled(
            boolean allowToGatherUsageStatsEnabled) {
        configurationModel
                .setAllowToGatherUsageStatsEnabled(allowToGatherUsageStatsEnabled);
    }

}

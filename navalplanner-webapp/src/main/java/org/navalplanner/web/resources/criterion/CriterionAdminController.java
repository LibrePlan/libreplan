package org.navalplanner.web.resources.criterion;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.GroupsModel;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleGroupsModel;
import org.zkoss.zul.api.Grid;
import org.zkoss.zul.api.Group;

/**
 * Controller for Criterions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionAdminController extends GenericForwardComposer {

    private static final Log log = LogFactory
            .getLog(CriterionAdminController.class);

    private ICriterionsModel criterionsModel;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private Grid listing;

    private Component editComponent;

    private Component createComponent;

    private OnlyOneVisible onlyOneVisible;

    private Component workersComponent;

    private CriterionEditController edition;

    private CriterionWorkersController workers;

    public CriterionAdminController() {

    }

    private class GroupRenderer implements RowRenderer {
        public void render(Row row, java.lang.Object data) {
            if (data instanceof Criterion) {
                final Criterion criterion = (Criterion) data;
                Hbox operations = new Hbox();
                operations.setParent(row);
                Button editButton = new Button("Editar");
                editButton.setParent(operations);
                editButton.setDisabled(!criterionsModel.getTypeFor(criterion)
                        .allowEditing());
                editButton.addEventListener("onClick", new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        goToEditForm(criterion);
                    }
                });
                Button traballadoresButton = new Button("Traballadores");
                traballadoresButton.setParent(operations);
                traballadoresButton.setDisabled(!criterionsModel
                        .isApplyableToWorkers(criterion));
                traballadoresButton.addEventListener("onClick",
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                showWorkers(criterion);
                            }
                        });
                new Label(criterion.getName()).setParent(row);
                Checkbox checkbox = new Checkbox();
                checkbox.setChecked(criterion.isActive());
                checkbox.setDisabled(true);
                checkbox.setParent(row);
            } else if (data instanceof ICriterionType) {
                final ICriterionType<?> type = (ICriterionType<?>) data;
                Div div = new Div();
                Button createButton = new Button("Engadir");
                createButton.setDisabled(!type.allowAdding());
                createButton.addEventListener("onClick", new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        goToCreateForm((ICriterionType<Criterion>) type);
                    }
                });
                div.appendChild(createButton);
                div.setParent(row);
                row.setSpans("3");
            } else {
                Group group = (Group) row;
                group.setLabel(data.toString());
                group.setSpans("3");
            }
        }

    }

    private void goToCreateForm(ICriterionType<Criterion> type) {
        onlyOneVisible.showOnly(createComponent);
        criterionsModel.prepareForCreate(type);
        Util.reloadBindings(createComponent);
    }

    private void goToEditForm(Criterion criterion) {
        onlyOneVisible.showOnly(editComponent);
        criterionsModel.workOn(criterion);
        Util.reloadBindings(editComponent);
    }

    private void showWorkers(Criterion criterion) {
        criterionsModel.workOn(criterion);
        Util.reloadBindings(workersComponent);
        onlyOneVisible.showOnly(workersComponent);
    }

    public CriterionEditController getEdition() {
        return edition;
    }

    public CriterionWorkersController getWorkers() {
        return workers;
    }

    public void save() {
        onlyOneVisible.showOnly(listing);
        try {
            criterionsModel.saveCriterion();
            messagesForUser.showMessage(Level.INFO, "Criterio gardado");
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        } finally {
            reload();
        }
    }

    public void cancel() {
        onlyOneVisible.showOnly(listing);
    }

    private RowRenderer getRowRenderer() {
        return new GroupRenderer();
    }

    private GroupsModel getTypesWithCriterions() {
        List<CriterionType> types = criterionsModel.getTypes();
        Object[][] groups = new Object[types.size()][];
        int i = 0;
        for (CriterionType type : types) {
            groups[i] = criterionsModel.getCriterionsFor(type).toArray();
            i++;
        }
        return new SimpleGroupsModel(groups, asStrings(types), types.toArray());
    }

    private String[] asStrings(List<CriterionType> types) {
        String[] result = new String[types.size()];
        int i = 0;
        for (CriterionType criterionType : types) {
            result[i++] = criterionType.getName();
        }
        return result;
    }

    public Criterion getCriterion() {
        return criterionsModel.getCriterion();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        onlyOneVisible = new OnlyOneVisible(listing, editComponent,
                createComponent, workersComponent);
        onlyOneVisible.showOnly(listing);
        comp.setVariable("controller", this, false);
        workers = new CriterionWorkersController(criterionsModel);
        workers.doAfterCompose(comp.getFellow("workersComponent"));
        messagesForUser = new MessagesForUser(messagesContainer);
        listing = (Grid) comp.getFellow("listing");
        reload();
        listing.setRowRenderer(getRowRenderer());
        edition = new CriterionEditController(criterionsModel);
    }

    private void reload() {
        listing.setModel(getTypesWithCriterions());
    }
}

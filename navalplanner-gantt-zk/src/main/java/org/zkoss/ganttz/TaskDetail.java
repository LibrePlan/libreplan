package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;

public class TaskDetail extends HtmlMacroComponent implements AfterCompose {

    private static final Log LOG = LogFactory.getLog(TaskDetail.class);

    private final TaskBean taskBean;

    private Textbox nameBox;

    private Textbox startDateTextBox;

    private Textbox endDateTextBox;

    private Datebox startDateBox;

    private Datebox endDateBox;

    private DateFormat dateFormat;

    public static TaskDetail create(TaskBean bean) {
        return new TaskDetail(bean);
    }

    private TaskDetail(TaskBean task) {
        this.taskBean = task;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locales
                .getCurrent());
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }

    public Textbox getNameBox() {
        return nameBox;
    }

    public void setNameBox(Textbox nameBox) {
        this.nameBox = nameBox;
    }

    public Datebox getStartDateBox() {
        return startDateBox;
    }

    public void setStartDateBox(Datebox startDateBox) {
        this.startDateBox = startDateBox;
        this.startDateBox.setCompact(true);
        this.startDateBox.setFormat("dd/MM/yyyy");
    }

    public Datebox getEndDateBox() {
        return endDateBox;
    }

    public void setEndDateBox(Datebox endDateBox) {
        this.endDateBox = endDateBox;
        this.endDateBox.setFormat("dd/MM/yyyy");
    }

    public TaskBean getData() {
        return taskBean;
    }

    /**
     * When a text box associated to a datebox is requested to show the datebox,
     * the corresponding datebox is shown
     * @param component
     *            the component that has received focus
     */
    public void userWantsDateBox(Component component) {
        if (component == startDateTextBox) {
            showDateBox(startDateBox, startDateTextBox);
        }
        if (component == endDateTextBox) {
            showDateBox(endDateBox, endDateTextBox);
        }
    }

    private void showDateBox(Datebox dateBox, Textbox associatedTextBox) {
        associatedTextBox.setVisible(false);
        dateBox.setVisible(true);
        dateBox.setFocus(true);
        dateBox.setOpen(true);
    }

    private enum Navigation {
        LEFT, UP, RIGHT, DOWN;
        public static Navigation getIntentFrom(KeyEvent keyEvent) {
            return values()[keyEvent.getKeyCode() - 37];
        }
    }

    TaskDetail getAboveDetail() {
        List<Component> parentChildren = getParent().getChildren();
        // TODO can be optimized
        int positionInParent = parentChildren.indexOf(this);
        if (positionInParent == 0)
            return null;
        return (TaskDetail) parentChildren.get(positionInParent - 1);
    }

    TaskDetail getBelowDetail() {
        List<Component> parentChildren = getParent().getChildren();
        int positionInParent = parentChildren.indexOf(this);
        if (positionInParent == parentChildren.size() - 1)
            return null;
        return (TaskDetail) parentChildren.get(positionInParent + 1);
    }

    private Textbox[] getTextBoxes() {
        return new Textbox[] { nameBox, startDateTextBox, endDateTextBox };
    }

    public void focusGoUp(int position) {
        TaskDetail aboveDetail = getAboveDetail();
        if (aboveDetail != null) {
            aboveDetail.receiveFocus(position);
        }
    }

    public void receiveFocus() {
        receiveFocus(0);
    }

    public void receiveFocus(int position) {
        this.getTextBoxes()[position].focus();
    }

    public void focusGoDown(int position) {
        TaskDetail belowDetail = getBelowDetail();
        if (belowDetail != null) {
            belowDetail.receiveFocus(position);
        }
    }

    public void userWantsToMove(Textbox textbox, KeyEvent keyEvent) {
        Navigation navigation = Navigation.getIntentFrom(keyEvent);
        List<Textbox> textBoxSiblingsIncludedItself = getTextBoxSiblingsIncludedItself(textbox);
        int position = textBoxSiblingsIncludedItself.indexOf(textbox);
        switch (navigation) {
        case UP:
            focusGoUp(position);
            break;
        case DOWN:
            focusGoDown(position);
            break;
        case LEFT:
            if (position == 0) {
                focusGoUp(getTextBoxes().length - 1);
            } else {
                textBoxSiblingsIncludedItself.get(position - 1).focus();
            }
            break;
        case RIGHT:
            if (position < textBoxSiblingsIncludedItself.size() - 1)
                textBoxSiblingsIncludedItself.get(position + 1).focus();
            else {
                focusGoDown(0);
            }
            break;
        default:
            throw new RuntimeException("case not covered: " + navigation);
        }
    }

    private List<Textbox> getTextBoxSiblingsIncludedItself(Textbox textbox) {
        Component parent = textbox.getParent();
        List<Component> children = parent.getChildren();
        List<Textbox> textboxes = Planner.findComponentsOfType(Textbox.class,
                children);
        return textboxes;
    }

    /**
     * When the dateBox loses focus the corresponding textbox is shown instead.
     * @param dateBox
     *            the component that has lost focus
     */
    public void dateBoxHasLostFocus(Datebox dateBox) {
        if (dateBox == startDateBox) {
            hideDateBox(startDateBox, startDateTextBox);
        }
        if (dateBox == endDateBox) {
            hideDateBox(endDateBox, endDateTextBox);
        }
    }

    private void hideDateBox(Datebox dateBoxToDissapear,
            Textbox associatedTextBox) {
        dateBoxToDissapear.setVisible(false);
        associatedTextBox.setVisible(true);
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        updateComponents();
        taskBean.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateComponents();
            }
        });
    }

    public void updateBean() {
        if (getEndDateBox().getValue().before(getStartDateBox().getValue())) {
            updateComponents();
            return;
        }
        taskBean.setName(getNameBox().getValue());
        taskBean.setBeginDate(getStartDateBox().getValue());
        taskBean.setEndDate(getEndDateBox().getValue());
    }

    private void updateComponents() {
        getNameBox().setValue(taskBean.getName());
        getStartDateBox().setValue(taskBean.getBeginDate());
        getEndDateBox().setValue(taskBean.getEndDate());
        getStartDateTextBox().setValue(asString(taskBean.getBeginDate()));
        getEndDateTextBox().setValue(asString(taskBean.getEndDate()));
    }

    private String asString(Date date) {
        return dateFormat.format(date);
    }

    public Textbox getStartDateTextBox() {
        return startDateTextBox;
    }

    public void setStartDateTextBox(Textbox startDateTextBox) {
        this.startDateTextBox = startDateTextBox;
    }

    public Textbox getEndDateTextBox() {
        return endDateTextBox;
    }

    public void setEndDateTextBox(Textbox endDateTextBox) {
        this.endDateTextBox = endDateTextBox;
    }

}

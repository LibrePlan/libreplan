package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;

public class TaskDetail extends HtmlMacroComponent implements AfterCompose {

    static String format(Date date) {
        return dateFormat.format(date);
    }

    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private static final Log LOG = LogFactory.getLog(TaskDetail.class);

    private static Pattern lengthPattern = Pattern
            .compile("\\s*(\\d+)\\s*(\\w+)\\s*");

    private enum LengthType {
        HOUR(3600, "h", "hour", "hora", "horas"), DAYS(3600 * 24, "day", "dia",
                "dias", "días", "día", "days");

        private final long milliseconds;

        private Set<String> set;

        private LengthType(int seconds, String... sufixes) {
            milliseconds = seconds * 1000;
            set = new HashSet<String>(Arrays.asList(sufixes));
        }

        public static long getTimeInMilliseconds(String spec) {
            Matcher matcher = lengthPattern.matcher(spec);
            if (!matcher.matches())
                throw new IllegalArgumentException("spec " + spec
                        + " is not matched by " + lengthPattern.pattern());
            long number = Integer.parseInt(matcher.group(1));
            String specifier = matcher.group(2).toLowerCase();
            for (LengthType type : LengthType.values()) {
                if (type.set.contains(specifier)) {
                    return number * type.milliseconds;
                }
            }
            throw new IllegalArgumentException(specifier + " not found");
        }
    }

    private String taskId;

    private final TaskBean taskBean;

    public TaskBean getTaskBean() {
        return taskBean;
    }

    private Textbox nameBox;

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

    private Datebox startDateBox;

    private Datebox endDateBox;

    public static TaskDetail create(TaskBean bean) {
        return new TaskDetail(bean);
    }

    private TaskDetail(TaskBean task) {
        this.taskBean = task;
    }

    public TaskBean getData() {
        return taskBean;
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
    }

}

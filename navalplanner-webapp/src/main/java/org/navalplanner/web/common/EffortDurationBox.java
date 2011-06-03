package org.navalplanner.web.common;

import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.workingday.EffortDuration;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Textbox;

public class EffortDurationBox extends Textbox {

    public static EffortDurationBox notEditable() {
        EffortDurationBox result = new EffortDurationBox();
        result.setDisabled(true);
        return result;
    }

    public EffortDurationBox() {
        setValueDirectly(zero());
    }

    public EffortDurationBox(EffortDuration effortDuration) {
        setValue(effortDuration);
    }

    public void setValue(EffortDuration effortDuration) {
        setText(coerceToString(effortDuration));
    }

    public EffortDuration getEffortDurationValue() {
        return (EffortDuration) getTargetValue();
    }

    @Override
    protected Object coerceFromString(String value) throws WrongValueException {
        EffortDuration result = EffortDuration.parseFromFormattedString(value);
        if (result == null) {
            throw new WrongValueException(this,
                    _("Not a valid effort duration"));
        }
        return result;
    }

    @Override
    protected String coerceToString(Object value) {
        if (value instanceof EffortDuration) {
            EffortDuration effort = (EffortDuration) value;
            return effort.toFormattedString();
        }
        return "";
    }

}

package org.zkoss.ganttz.resourceload;

import java.util.List;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.impl.XulElement;

public class ResourceLoadLeftPane extends XulElement {

    public ResourceLoadLeftPane(List<LoadTimelinesGroup> groups) {
        for (LoadTimelinesGroup loadTimelinesGroup : groups) {
            LoadTimeLine principal = loadTimelinesGroup.getPrincipal();
            appendChild(createFirstLevel(principal));
            for (LoadTimeLine loadTimeLine : loadTimelinesGroup.getChildren()) {
                appendChild(createSecondLevel(loadTimeLine));
            }
        }
    }

    private Component createFirstLevel(LoadTimeLine principal) {
        Div result = createLabelWithName(principal);
        result.setSclass("firstlevel");
        return result;
    }


    private Component createSecondLevel(LoadTimeLine loadTimeLine) {
        Div result = createLabelWithName(loadTimeLine);
        result.setSclass("secondlevel");
        return result;
    }

    private Div createLabelWithName(LoadTimeLine principal) {
        Div result = new Div();
        Label label = new Label();
        label.setValue(principal.getConceptName());
        result.appendChild(label);
        return result;
    }
}

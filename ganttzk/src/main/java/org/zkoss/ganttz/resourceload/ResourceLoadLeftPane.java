package org.zkoss.ganttz.resourceload;

import java.util.List;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.api.Box;

public class ResourceLoadLeftPane extends HtmlMacroComponent {

    private final List<LoadTimelinesGroup> groups;

    public ResourceLoadLeftPane(List<LoadTimelinesGroup> groups) {
        this.groups = groups;
    }

    private void addGroups(Box container) {
        for (LoadTimelinesGroup loadTimelinesGroup : this.groups) {
            LoadTimeLine principal = loadTimelinesGroup.getPrincipal();
            container.appendChild(createFirstLevel(principal));
            for (LoadTimeLine loadTimeLine : loadTimelinesGroup.getChildren()) {
                container.appendChild(createSecondLevel(loadTimeLine));
            }
        }
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        addGroups(getContainer());
    }

    private Box getContainer() {
        return (Box) getFellow("leftVBox");
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

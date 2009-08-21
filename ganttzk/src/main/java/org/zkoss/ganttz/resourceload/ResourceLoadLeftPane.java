package org.zkoss.ganttz.resourceload;

import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.impl.XulElement;

public class ResourceLoadLeftPane extends XulElement {

    public ResourceLoadLeftPane() {
        appendChild(createFakeRow());
        appendChild(createFakeRow());
    }

    private Div createFakeRow() {
        Div result = new Div();
        Label label = new Label();
        label.setValue("test");
        result.appendChild(label);
        return result;
    }
}

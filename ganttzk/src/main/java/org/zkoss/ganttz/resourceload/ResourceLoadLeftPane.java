package org.zkoss.ganttz.resourceload;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Tree;

public class ResourceLoadLeftPane extends HtmlMacroComponent {

    private MutableTreeModel<LoadTimeLine> modelForTree;
    private final ResourceLoadList resourceLoadList;

    public ResourceLoadLeftPane(MutableTreeModel<LoadTimeLine> modelForTree,
            ResourceLoadList resourceLoadList) {
        this.resourceLoadList = resourceLoadList;
        this.modelForTree = modelForTree;
    }


    @Override
    public void afterCompose() {
        super.afterCompose();
        getContainerTree().setModel(modelForTree);
        getContainerTree().setTreeitemRenderer(getRendererForTree());
    }

    private TreeitemRenderer getRendererForTree() {
        return new TreeitemRenderer() {
            @Override
            public void render(Treeitem item, Object data) throws Exception {
                LoadTimeLine line = (LoadTimeLine) data;
                Treerow row = new Treerow();
                Treecell cell = new Treecell();
                Component component = createComponent(line);
                item.appendChild(row);
                row.appendChild(cell);
                cell.appendChild(component);
                addExpandedListener(item, line);
            }

            private void addExpandedListener(Treeitem item,
                    final LoadTimeLine line) {
                item.addEventListener("onOpen", new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        OpenEvent openEvent = (OpenEvent) event;
                        if (openEvent.isOpen()) {
                            expand(line);
                        } else {
                            collapse(line);
                        }
                    }
                });
            }

            private Component createComponent(LoadTimeLine line) {
                return isTopLevel(line) ? createFirstLevel(line)
                        : createSecondLevel(line);
            }

            private boolean isTopLevel(LoadTimeLine line) {
                int[] path = modelForTree.getPath(modelForTree.getRoot(), line);
                return path.length == 0;
            }
        };
    }

    private void collapse(LoadTimeLine line) {
        // TODO do collapse
    }

    private void expand(LoadTimeLine line) {
        // TODO do expand

    }



    private Tree getContainerTree() {
        return (Tree) getFellow("loadsTree");
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

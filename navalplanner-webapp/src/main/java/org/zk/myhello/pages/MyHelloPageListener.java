package org.zk.myhello.pages;

import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Francisco Javier Moran Rúa
 * 
 */
public class MyHelloPageListener extends Div implements Initiator {

    private ZoomLevel currentDetailLevel;

    public MyHelloPageListener() {
        setCurrentDetailLevel(ZoomLevel.DETAIL_ONE);
    }

    public void doInit(Page arg0, Object[] arg1) throws Exception {
        setCurrentDetailLevel(ZoomLevel.DETAIL_ONE);
    }

    public void doAfterCompose(Page arg0) throws Exception {
        setCurrentDetailLevel(ZoomLevel.DETAIL_ONE);
    }

    public boolean doCatch(Throwable arg0) throws Exception {
        return true;
    }

    public void doFinally() throws Exception {
    }

    /**
     * @return the currentDetailLevel
     */
    public ZoomLevel getCurrentDetailLevel() {
        return this.currentDetailLevel;
    }

    /**
     * @param currentDetailLevel
     *            the currentDetailLevel to set
     */
    public void setCurrentDetailLevel(ZoomLevel currentDetailLevel) {
        this.currentDetailLevel = currentDetailLevel;
    }

    public void repaint() {
        Label l = (Label) getFellow("valor");
        l.invalidate();
    }

}

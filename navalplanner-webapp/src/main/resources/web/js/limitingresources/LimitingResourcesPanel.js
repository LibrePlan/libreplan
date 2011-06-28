zk.$package("limitingresources");

limitingresources.LimitingResourcesPanel = zk.$extends(zk.Macro,{
    $init : function(){
        this.$supers('$init', arguments);

    },
    bind_ : function(){
        this.$supers('bind_', arguments);
        /* When the LimitingResourcesPanel widget is created we might be on another
         * perspective and so all the elements that we are trying to select (as they have the same name),
         * belong to this other perspective . We have to wait until everything
         * is rendered to be sure that we are picking the proper ones
         *
         * So the _initializeProperties method can't be placed on $init
         */

        this._initializeProperties();
        this.domListen_(this._rightpanellayout,'onScroll', '_listenToScroll');
    },
    unbind_ : function(){
        this.domUnlisten_(this._rightpanellayout,'onScroll', '_listenToScroll');
        this.$supers('unbind_', arguments);
    },
    _initializeProperties : function(){
        this._rightpanellayout = jq('.rightpanellayout div:first');
        this._timetrackergap = jq('.timetrackergap');
        this._resourcesload = jq('.resourcesloadgraph div:first');
        this._leftpanel = jq('.leftpanelgap .z-tree-body');
    },
    _listenToScroll : function(){
        this._timetrackergap.css('left', '-' + this._rightpanellayout.scrollLeft() + 'px');
        this._leftpanel.css('top', '-' + this._rightpanellayout.scrollTop() + 'px');
        this._resourcesload.scrollLeft(this._rightpanellayout.scrollLeft());
    }
})
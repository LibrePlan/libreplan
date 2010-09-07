zk.$package("ganttz");

ganttz.TaskComponent = zk.$extends(zk.Widget, {
    $define :{
        resourcesText    : null,
        labelsText    : null,
        tooltipText : null
    },
    bind_ : function(event){
        this.$supers('bind_', arguments);
        this.domListen_(this.$n(), "onMouseover", '_showTooltip');
        this.domListen_(this.$n(), "onMouseout", '_hideTooltip');
    },
    unbind_ : function(event){
        this.domUnlisten_(this.$n(), "onMouseout", '_hideTooltip');
        this.domUnlisten_(this.$n(), "onMouseover", '_showTooltip');
        this.$supers('unbind_', arguments);
    },
    addRelatedDependency : function(dependency){
        if(this._dependencies == undefined) this._dependencies = [];
        this._dependencies.push(dependency);
    },
    _createArrow : function(){
        var dependencylist = ganttz.DependencyList.$(jq('#ganttpanel > div[z\\.type="ganttz.dependencylist.Dependencylist"]').attr('id'));
        var unlinkedDependency = new ganttz.UnlinkedDependencyComponent();
        unlinkedDependency.setOrigin(this.$n());

        WGTdependencylist.appendChild(unlinkedDependency, true);
        WGTdependencylist.rerender();

        unlinkedDependency.draw();
    },
    _showTooltip : function(){
        this.mouseOverTask = true;
        this._tooltipTimeout = setTimeout(jq.proxy(function(offset) {
            var element = jq("#tasktooltip" + this.uuid);
            if (element!=null) {
                element.show();
                offset = ganttz.GanttPanel.getInstance().getXMouse()
                        - element.parent().offset().left
                        - jq('.leftpanelcontainer').offsetWidth
                        - this.$class._PERSPECTIVES_WIDTH
                        + jq('.rightpanellayout div').scrollLeft();
                element.css( 'left' , offset +'px' );
            }
        }, this), this.$class._TOOLTIP_DELAY);
    },
    _hideTooltip : function(){
        this.mouseOverTask = false;
        if (this._tooltipTimeout) {
            clearTimeout(this._tooltipTimeout);
        }
        jq('#tasktooltip' + this.uuid).hide();
    },
    moveDeadline : function(width){
        jq('#deadline' + this.parent.uuid).css('left', width);
    },
    moveConsolidatedline : function(width){
        jq('#consolidatedline' + this.parent.uuid).css('left', width);
    },
    resizeCompletionAdvance : function(width){
        jq('#' + this.uuid + ' > .completion:first').css('width', width);
    },
    resizeCompletion2Advance : function(width){
        jq('#' + this.uuid + ' > .completion2:first').css('width', width);
    },
    setClass : function(){},
    showTaskLabel : function(){
        jq('#'+ this.uuid + ' .task-labels').show();
    },
    hideTaskLabel : function(){
        jq('#'+ this.uuid + ' .task-labels').hide();
    },
    showResourceTooltip : function(){
        jq('#'+ this.uuid + ' .task-resources').show();
    },
    hideResourceTooltip : function(){
        jq('#'+ this.uuid + ' .task-resources').hide();
    }
},{
    //"Class" methods and properties
    _TOOLTIP_DELAY : 10, // 10 milliseconds
    _PERSPECTIVES_WIDTH : 80,
    CORNER_WIDTH : 20,
    HEIGHT : 10,
    HALF_HEIGHT : 5
});
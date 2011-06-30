zk.$package("ganttz");

ganttz.Planner = zk.$extends(zk.Macro,{
    $init : function(){
        this.$supers('$init', arguments);
        this.$class.setInstance(this);
    },
    bind_ : function(){
        this.$supers('bind_', arguments);
        this.adjustScrollableDimensions();
        //Zoomlevel selector
        this.domListen_(jq('.plannerlayout .toolbar-box select'), 'onChange', '_zoomLevelChanged');
    },
    unbind_ : function(){
        this.$supers('unbind_', arguments);
        this.domUnlisten_(jq('.plannerlayout .toolbar-box select'), 'onChange', '_zoomLevelChanged');
    },
    adjustScrollableDimensions : function(){

        // Timetracker is recalculated when the window is resized and when zoom
        // level is changed as the component is recreated

        var DOMTimetracker = jq('#timetracker');
        var DOMWatermark = jq('#watermark');
        var DOMScrollContainer = jq('#scroll_container');

        DOMTimetracker.width(
                jq(window).width() -
                this.$class.TASKDETAILS_WIDTH -
                this.$class.SCROLLBAR_WIDTH * 2);

        DOMScrollContainer.width(
                jq('.second_level_ :first').innerWidth());

        DOMTimetracker.width(DOMScrollContainer.innerWidth());
        DOMTimetracker.height(
                Math.max(
                        jq(window).height() -
                        this.$class.TIMETRACKER_OFFSET_TOP +
                        26),
                        (
                        jq('#listdetails_container').height() +
                        12));

        DOMScrollContainer.height(
                jq(window).height() -
                this.$class.TIMETRACKER_OFFSET_TOP -
                this.$class.FOOTER_HEIGHT +
                this.$class.SCROLLBAR_WIDTH * 2);


        // Inner divs need recalculation to adjust to new scroll displacement lenght
        ganttz.GanttPanel.getInstance().reScrollY(jq('#listdetails_container').height());

        // Inner divs need recalculation to adjust to new scroll displacement lenght
        ganttz.GanttPanel.getInstance().reScrollX(DOMWatermark.outerWidth());
    },
    _zoomLevelChanged : function(event){
        var zoomindex = event.domTarget.selectedIndex;
        var scrollLeft = parseFloat(jq('.timetrackergap').css('left').replace(/px/, ""));
        zAu.send(new zk.Event(this, 'onZoomLevelChange', {zoomindex : zoomindex, scrollLeft : scrollLeft}));
    }
},{
    FOOTER_HEIGHT : 40, // Design-relative footer height
    TIMETRACKER_OFFSET_TOP : 120,   // Design-relative height above timetracker
    TASKDETAILS_WIDTH : 300, // Taskdetails column fixed width (300)
    TASKDETAILS_HEIGHT : 180, // 260 // Design-relative reservated height for taskdetails (300,260)
    SCROLLBAR_WIDTH : 15, // Scrollbars default width
    getInstance : function(){
        return this._instance;
    },
    setInstance : function(instance){
        this._instance = instance;
    }
})
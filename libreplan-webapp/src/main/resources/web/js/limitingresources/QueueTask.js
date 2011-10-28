zk.$package("limitingresources");

limitingresources.QueueTask = zk.$extends(zul.wgt.Div,{
    bind_ : function(evt) {
        this.$supers('bind_', arguments);
        this.domListen_(this.$n(), 'onmouseover', 'showRelatedElementsForQueueElement');
        this.domListen_(this.$n(), 'onmouseout', 'hideRelatedElementsForQueueElement');
        this.domListen_(this.$n(), 'onclick', 'toggleDependenciesForQueueElement');
    },
    unbind_ : function(evt) {
        this.domUnlisten_(this.$n(), 'onmouseover', 'showRelatedElementsForQueueElement');
        this.domUnlisten_(this.$n(), 'onmouseout', 'hideRelatedElementsForQueueElement');
        this.domUnlisten_(this.$n(), 'onclick', 'toggleDependenciesForQueueElement');
        this.$supers('unbind_', arguments);
    },
    showRelatedElementsForQueueElement: function(evt) {
        this.showDependenciesForQueueElement(this.$n().id);
        this.setVisibleDeadlineForQueueElement(this.$n().id, "inline");
    },
    hideRelatedElementsForQueueElement: function (evt) {
        this.hideDependenciesForQueueElement(this.$n().id);
        this.setVisibleDeadlineForQueueElement(this.$n().id, "none");
    },
    toggleDependenciesForQueueElement: function (evt) {
        var task = this.$n().id;
        jq('.dependency').each(function () {
            if ( (jq(this).attr("idTaskOrig") ==  task) || (jq(this).attr("idTaskEnd") ==  task) ) {
                jq(this).toggleClass("toggled");
            }
        });
    },
    showDependenciesForQueueElement: function (task) {
        jq('.dependency').each(function () {
            if ( (jq(this).attr("idTaskOrig") ==  task) || (jq(this).attr("idTaskEnd") ==  task) ) {
                jq(this).css("display", "inline");
                jq(this).css("opacity", "1");
            }
        });
    },
    hideDependenciesForQueueElement: function (task) {
        jq('.dependency').each(function () {
            if ( (jq(this).attr("idTaskOrig") ==  task) || (jq(this).attr("idTaskEnd") ==  task) ) {
                jq(this).css("display", "none");
                jq(this).css("opacity", "");
            }
        });
    },
    setVisibleDeadlineForQueueElement: function(task, visible) {
        jq('.deadline').each(function () {
            if ($(this).parent().attr('id') == task) {
                $(this).css('display', visible);
            }
        });
    }
});
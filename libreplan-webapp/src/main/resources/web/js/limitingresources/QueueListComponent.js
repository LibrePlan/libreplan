zk.$package("limitingresources");

limitingresources.QueueListComponent = zk.$extends(
    zk.Widget,
    {
        $init: function() {
            this.$supers('$init', arguments);
            common.Common.mixInDayPositionRestorer(this);
        },

        bind_: function() {
            this.$supers('bind_', arguments);
        },

        unbind_ : function() {
            this.$supers('unbind_', arguments);
        },

        _divsToRestoreDayInto: function() {
            var first = this.$n();
            return [first, first.parentNode, first.parentNode.parentNode];
        },

        adjustScrollHorizontalPosition : function (offsetInPx) {
            jq(this.$n()).scrollLeft(offsetInPx);
        },

        adjustResourceLoadRows: function () {
            var width = jq('.rightpanel-layout #timetracker .z-grid-header:first').innerWidth();

            jq('.row_resourceload').each(function (index, element) {
                jq(element).width(width);
            });
        },

        adjustTimeTrackerSize: function() {

            /* After ZK migrated from ZK5 to ZK8, this.$n() started to return undefined.
             * Possible reason: not enough time to load library.
             */
            if ( typeof this.$n() !== "undefined" ) {

                jq('#watermark').height(this.$n().clientHeight);

                jq('#timetracker').width(this.$n().clientWidth).each(function() {

                    // Timetracker exists
                    var limitingResourcesList = jq('.limitingresources-list');

                    limitingResourcesList.width(jq('.second_level_').get(0).clientWidth);

                    jq('.rightpanel-layout tr#watermark td').height(limitingResourcesList.get(0).clientHeight + 120);

                });
            }
        }
    });
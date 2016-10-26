function(out) {

    /* 
     * After ZK migrated from ZK5 to ZK8, this.domAttrs_() started to return NaN.
     * Possible reason: not enough time to load library. 
     */
    if ( !isNaN(this.domAttrs_()) ) {
        out.push(
            '<div ',
            this.domAttrs_(),
            'class="limitingresources-list"',
            'z.type="limitingresources.limitingresourceslist.LimitingResourcesList"',
            '>');
    } else {
        out.push(
            '<div ',
            'class="limitingresources-list"',
            'z.type="limitingresources.limitingresourceslist.LimitingResourcesList"',
            '>');
    }

    for (var w = this.firstChild; w; w = w.nextSibling) {
        w.redraw(out);
    }

    out.push('</div>');
}
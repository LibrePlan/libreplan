function(out) {

    /* After ZK migrated from 5 to 8, this.domAttrs_() started to return NaN.
     * Possible reason: not enough time to load library. 
     */
    if ( !isNaN(this.domAttrs_()) ) {
        out.push(
            '<div ',
            this.domAttrs_(),
            'z.type="limitingresources.limitingdependencylist.LimitingDependencylist"',
            'z.autoz="true"',
            '>');
    } else {
        out.push(
            '<div ',
            'z.type="limitingresources.limitingdependencylist.LimitingDependencylist"',
            'z.autoz="true"',
            '>');
    }

    out.push('<div id="listlimitingdependencies">');

    for (var w = this.firstChild; w; w = w.nextSibling)
        w.redraw(out);

    out.push('</div>');

    out.push('</div>');
}
function (out) {
    /* 
     * This method draws graphic lines ( charts ) for every resource, if needed.
     * 
     * After we migrated from ZK5 to ZK8, this.domAttrs_() started to return NaN.
     * Possible reason: not enough time to load library. 
     */
    if ( !isNaN(this.domAttrs_()) ) {
        out.push(
            '<div ',
            this.domAttrs_(),
            ' class="row_resourceload resourceload-'+ this.getResourceLoadType(),'"',
            ' z.autoz="true"',
            '>');
    } else {
        out.push(
            '<div ',
            ' class="row_resourceload resourceload-'+ this.getResourceLoadType(),'"',
            ' z.autoz="true"',
            '>');
    }

    out.push('<span class="resourceload_name">', this.getResourceLoadName(),'</span>');

    for (var w = this.firstChild; w; w = w.nextSibling) {
        w.redraw(out);
    }

    out.push('</div>');
}
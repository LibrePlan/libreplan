function(out) {

    /* After ZK migrated from 5 to 8, this.domAttrs_() started to return NaN.
     * Possible reason: not enough time to load library. 
     */
    if ( !isNaN(this.domAttrs_()) ) {
        out.push(
            '<div ',
            this.domAttrs_(),
            'class="dependency"',
            'z.type="limitingresources.limitingdependency.LimitingDependency"',
            'idTaskOrig="', this.getIdTaskOrig(), '"',
            'idTaskEnd="', this.getIdTaskEnd(), '"',
            '>');
    } else {
        out.push(
            '<div ',
            'class="dependency"',
            'z.type="limitingresources.limitingdependency.LimitingDependency"',
            'idTaskOrig="', this.getIdTaskOrig(), '"',
            'idTaskEnd="', this.getIdTaskEnd(), '"',
            '>');
    }

    out.push('</div>');
}
function(out){
    out.push('<div ', this.domAttrs_(),
            'class="dependency"',
            'z.type="limitingresources.limitingdependency.LimitingDependency"',
            'idTaskOrig="', this.getIdTaskOrig(), '"',
            'idTaskEnd="', this.getIdTaskEnd(), '"',
            '>');
    out.push('</div>');
}
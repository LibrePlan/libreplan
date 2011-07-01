function(out){
    out.push('<div ', this.domAttrs_(),
            'class="limitingresourceslist"',
            'z.type="limitingresources.limitingresourceslist.LimitingResourcesList"',
            '>');
        for (var w = this.firstChild; w; w = w.nextSibling)
            w.redraw(out);
    out.push('</div>');
}
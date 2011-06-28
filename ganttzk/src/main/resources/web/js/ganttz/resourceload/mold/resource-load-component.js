function(out){
    out.push('<div ', this.domAttrs_(),
            ' class="row_resourceload resourceload-'+ this.getResourceLoadType(),'"',
            ' z.autoz="true"',
            '>');
        out.push('<span class="resourceload_name">', this.getResourceLoadName(),'</span>');
        for(var w = this.firstChild; w; w = w.nextSibling)
            w.redraw(out);
    out.push('</div>');
}
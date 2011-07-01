function(out){
    out.push('<div ' + this.domAttrs_(),
            ' class="resourceloadlist"',
            ' z.type="ganttz.resourceload.resourceloadlist.ResourceLoadList">');
    for(var w = this.firstChild; w; w = w.nextSibling)
        w.redraw(out);
    out.push('</div>');
}
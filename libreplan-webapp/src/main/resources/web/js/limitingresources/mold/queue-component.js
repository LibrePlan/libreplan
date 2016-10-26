function(out) {

    /*
     * After we migrated from ZK5 to ZK8, this.domAttrs_() started to return NaN.
     * Possible reason: not enough time to load library.
     * But, without this attribute ZK cannot insert any other component inside this one.
     */

    out.push(
        '<div ',
        this.domAttrs_(),
        'class="row_resourceload"',
        'z.autoz="true"',
        '>');

    out.push('<span class="resourceload_name">');
    out.push(this.getResourceName());
    out.push('</span>');

    for (var w = this.firstChild; w; w = w.nextSibling) {
        w.redraw(out);
    }

    out.push('</div>');

    /* 
     * If we insert this.domAttrs_(), we are loosing class row_resourceload, because ZK inserts class z-queuecomponent
     */
    zk.afterMount(function() {
        var list = jq('.z-queuecomponent');

        jq.each( list, function (key, value) {
            jq(value).removeClass('z-queuecomponent');
            jq(value).addClass('row_resourceload');
        } );
    });
}
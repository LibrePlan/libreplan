function(out){
    out.push('<script type="text/javascript">',
            'document.body.class = "yui-skin-sam";',
            '</script>');

    out.push('<div id="scroll_container">');
        out.push('<div z.type="gantt.tasklist.TaskList" z.autoz="true" ' + this.domAttrs_() + '>');
            out.push('<div id="listtasks">');
                for(var w = this.firstChild; w; w = w.nextSibling)
                    w.redraw(out);
            out.push('</div>');
        out.push('</div>');
    out.push('</div>');
}
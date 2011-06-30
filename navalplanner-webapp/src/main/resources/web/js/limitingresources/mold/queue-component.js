function(out){
	out.push('<div ', this.domAttrs_(),
			'class="row_resourceload"',
			'z.autoz="true"',
			'>');
		out.push('<span class="resourceload_name">');
			out.push(this.getResourceName());
		out.push('</span>');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
	out.push('</div>');
}
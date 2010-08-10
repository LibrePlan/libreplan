function(out){
	out.push('<div id="'+ this.uuid + '" class="row" z.valor="boxid=' + this.uuid +'">');
		for(var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('<div id="deadline'+ this.uuid + '" class="deadline"></div>');
		out.push('<div id="consolidatedline' + this.uuid + '" class="consolidatedline"></div>');
	out.push('</div>');
}
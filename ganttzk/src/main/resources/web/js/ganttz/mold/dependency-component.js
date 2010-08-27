function(out){
	out.push('<div ', this.domAttrs_(),
			'class="dependency"',
			'z.type="ganttz.dependency.Dependency"',
			'idTaskOrig="', this.getIdTaskOrig(),'"',
			'idTaskEnd="', this.getIdTaskEnd(),'"',
			'type="', this.getDependencyType(),'"',
			'>');
	out.push('</div>');
}
function(out){
        out.push('<div ', this.domAttrs_(),
                'z.type="ganttz.task.Task"',
                'idTask="', this.id,'"',
                'z.autoz="true"',
                'class="milestone"',
                '>');
            out.push('<div class="completionMoneyCostBar"></div>');
            out.push('<div class="completion"></div>');
            out.push('<div class="completion2"></div>');
            out.push('<div class="milestone_end"></div>');
        out.push('</div>');
}
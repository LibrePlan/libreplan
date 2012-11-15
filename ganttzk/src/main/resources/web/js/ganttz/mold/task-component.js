function(out){
    out.push('<div ',this.domAttrs_(),
            ' z.type="ganttz.task.Task" idTask="', this.id,'"',
            ' class="box" >');

        out.push('<div class="task-labels" ',
                this.parent._labelsHidden?'>':'style="display:block;">',
                this.getLabelsText(),'</div>');
        out.push('<div class="task-resources" ',
                this.parent._resourcesHidden?'>':'style="display:block;">');
            out.push('<div class="task-resources-inner">', this.getResourcesText(),'</div>');
        out.push('</div>');

        out.push('<div class="completionMoneyCostBar"></div>');
        out.push('<div class="completion"></div>');
        out.push('<div class="completion2"></div>');
        out.push('<div class="timesheet-date-mark first-timesheet-date">|</div>');
        out.push('<div class="timesheet-date-mark last-timesheet-date">|</div>');

        out.push('<div id="tasktooltip', this.uuid,'" class="task_tooltip">',
                this.getTooltipText(),
                '</div>');

    out.push('</div>');
}
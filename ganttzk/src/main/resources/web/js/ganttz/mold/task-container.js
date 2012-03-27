function(out){
    out.push('<div ', this.domAttrs_(),
            'z.type="ganttz.taskcontainer.TaskContainer"',
            'idTask="', this.id,'"',
            'z.autoz="true"',
            'class="taskgroup"',
            '>');

        out.push('<div class="border-container">',
            '<div class="taskgroup_start"></div>',
            '<div class="taskgroup_end"></div>',
        '</div>');

        out.push('<div class="task-labels">',
                    this.getLabelsText(),
                '</div>');

        out.push('<div class="task-resources">',
                    '<div class="task-resources-inner">',
                        this.getResourcesText(),
                    '</div>',
                '</div>');

        out.push('<div class="taskcontainer_completion">',
                    '<div class="completionMoneyCostBar"></div>',
                    '<div class="completion"></div>',
                    '<div class="completion2"></div>',
                '</div>');

        out.push('<div id="tasktooltip', this.uuid, '"',
                'class="task_tooltip">',
                    this.getTooltipText(),
                '</div>');

    out.push('</div>');
}
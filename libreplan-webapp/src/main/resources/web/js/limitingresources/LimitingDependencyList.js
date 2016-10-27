zk.$package("limitingresources");

limitingresources.LimitingDependencyList = zk.$extends(
    zk.Widget,
    {
        appendChild: function(ao,al) {
            this.$supers('appendChild', arguments);
            
            // By default, appedChild appends the child (the .dependency object) as a direct child of the 
            // LimitingDependencylist object, but we want to to be added inside div#listlimitingdependencies,
            // so we move the dependency once it's created.
            jq('#'+ao.uuid).appendTo('#listlimitingdependencies');
        }
    });

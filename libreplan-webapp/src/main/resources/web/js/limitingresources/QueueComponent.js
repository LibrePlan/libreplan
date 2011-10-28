zk.$package("limitingresources");

limitingresources.QueueComponent = zk.$extends(zk.Widget,{
    $define : {
        resourceName : null
    }
})
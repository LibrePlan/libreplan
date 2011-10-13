zk.$package("webcommon");

webcommon.LenientDecimalBox = zk.$extends(zul.inp.Decimalbox,{

    coerceFromString_: function (b) {
        if(!b) {
            return null;
        }
        //replace decimal comma with dot
        b = b.replace(',','.');
        //process normally
        return this.$supers('coerceFromString_', arguments);
    }
});

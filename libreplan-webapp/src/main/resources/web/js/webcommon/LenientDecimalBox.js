zk.$package("webcommon");

webcommon.LenientDecimalBox = zk.$extends(zul.inp.Decimalbox,{

    coerceFromString_: function (b) {
        if(!b) {
            return null;
        }
        //to allow using . as decimal separator, independently of the locale
        //we replace . with the actual decimal separator of the current locale
        b = b.replace('.', zk.DECIMAL);
        //process normally
        return this.$supers('coerceFromString_', arguments);
    }
});

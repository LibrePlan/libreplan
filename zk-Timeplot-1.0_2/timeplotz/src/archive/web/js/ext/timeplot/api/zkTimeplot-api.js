/*==================================================
 *  Timeplot API
 *
 *  This file will load all the Javascript files
 *  necessary to make the standard timeplot work.
 *  It also detects the default locale.
 *
 *  Include this file in your HTML file as follows:
 *
 *    <script src="http://simile.mit.edu/timeplot/api/scripts/timeplot-api.js" type="text/javascript"></script>
 *
 *==================================================
 */
 
var Timeplot = new Object();
Timeplot = {
            loaded:     false,
            params:     { bundle: true, autoCreate: true },
            namespace:  "http://simile.mit.edu/2007/06/timeplot#",
            importers:  {}
        };
Timeplot.Platform = new Object();
    /*
        HACK: We need these 2 things here because we cannot simply append
        a <script> element containing code that accesses Timeplot.Platform
        to initialize it because IE executes that <script> code first
        before it loads timeplot.js and util/platform.js.
    */

(function() {
	/*
		HACK: If We load the bundle.js and bundle.css in ZK then there will be abnormal in IE.
	*/
    var bundle = false;
    var javascriptFiles = [
    		"excanvas",
    		"oop",
            "timeplot",
            "plot",
            "sources",
            "geometry",
            
            "color",
            "math",
            "processor"
            
    ];
    var cssFiles = [
        //"timeplot.css"
        //"ethers.css",
        //"events.css",
        //"timeline.css"
    ];
    
    var localizedJavascriptFiles = [
       // "timeplot",
       // "labellers"
    ];
    var localizedCssFiles = [
    				//"local"
    ];
    
    // ISO-639 language codes, ISO-3166 country codes (2 characters)
    var supportedLocales = [
        "cs",       // Czech
        "en",       // English
        "es",       // Spanish
        "fr",       // French
        "it",       // Italian
        "ru",       // Russian
        "se",       // Swedish
        "vi",       // Vietnamese
        "zh"        // Chinese
    ];
    
    try {
        var desiredLocales = [ "en" ];
        var defaultServerLocale = "en";
        
        var parseURLParameters = function(parameters) {
            var params = parameters.split("&");
            for (var p = 0; p < params.length; p++) {
                var pair = params[p].split("=");
                if (pair[0] == "locales") {
                    desiredLocales = desiredLocales.concat(pair[1].split(","));
                } else if (pair[0] == "defaultLocale") {
                    defaultServerLocale = pair[1];
                } else if (pair[0] == "bundle") {
                    bundle = pair[1] != "false";
                }
            }
        };
        
        (function() {
            if (typeof Timeplot_urlPrefix == "string") {
                Timeplot.urlPrefix = Timeplot_urlPrefix;
                if (typeof Timeplot_parameters == "string") {
                    parseURLParameters(Timeplot_parameters);
                }
            } else {
                var heads = document.documentElement.getElementsByTagName("head");
                for (var h = 0; h < heads.length; h++) {
                    var scripts = heads[h].getElementsByTagName("script");
                    for (var s = 0; s < scripts.length; s++) {
                        var url = scripts[s].src;
                        var i = url.indexOf("zkTimeplot-api.js");
                        if (i >= 0) {
                            Timeplot.urlPrefix = url.substr(0, i);
                            var q = url.indexOf("?");
                            if (q > 0) {
                                parseURLParameters(url.substr(q + 1));
                            }
                            return;
                        }
                    }
                }
                throw new Error("Failed to derive URL prefix for Timeplot API code files");
            }
        })();
        
        var includeJavascriptFiles;
        var includeCssFiles;

            var includeJavascriptFile = function(url) {
            	zk.load(url);
         	};
            var includeCssFile = function(url) {
				zk.loadCSS(url);
            };
            
            includeJavascriptFiles = function(urlPrefix, filenames) {
                for (var i = 0; i < filenames.length; i++) {
                    includeJavascriptFile(urlPrefix + filenames[i]);
                }
            };
            includeCssFiles = function(urlPrefix, filenames) {
                for (var i = 0; i < filenames.length; i++) {
                    includeCssFile(urlPrefix + filenames[i]);
                }
            };
    
        
        ///for ZK : using "zk.load" and "zk.loadCSS" .
        Timeplot.cssUrlPrefix="js/ext/timeplot/api/";
        Timeplot.jsUrlPrefix="ext.timeplot.api.";
        
        /*
         *  Include non-localized files
         */
        if (bundle) {
            includeJavascriptFiles(Timeplot.jsUrlPrefix, ["bundle"]);
            includeCssFiles(Timeplot.cssUrlPrefix,["bundle.css"]);
        } else {
            includeJavascriptFiles(Timeplot.jsUrlPrefix + "scripts.", javascriptFiles);
            includeCssFiles(Timeplot.cssUrlPrefix + "styles/", cssFiles);
        }
        
        /*
         *  Include localized files
         */
        var loadLocale = [];
        loadLocale[defaultServerLocale] = true;
        
        var tryExactLocale = function(locale) {
            for (var l = 0; l < supportedLocales.length; l++) {
                if (locale == supportedLocales[l]) {
                    loadLocale[locale] = true;
                    return true;
                }
            }
            return false;
        };
        var tryLocale = function(locale) {
            if (tryExactLocale(locale)) {
                return locale;
            }
            
            var dash = locale.indexOf("-");
            if (dash > 0 && tryExactLocale(locale.substr(0, dash))) {
                return locale.substr(0, dash);
            }
            
            return null;
        };
        
        for (var l = 0; l < desiredLocales.length; l++) {
            tryLocale(desiredLocales[l]);
        }
        
        var defaultClientLocale = defaultServerLocale;
        var defaultClientLocales = ("language" in navigator ? navigator.language : navigator.browserLanguage).split(";");
        for (var l = 0; l < defaultClientLocales.length; l++) {
            var locale = tryLocale(defaultClientLocales[l]);
            if (locale != null) {
                defaultClientLocale = locale;
                break;
            }
        }
        
        for (var l = 0; l < supportedLocales.length; l++) {
            var locale = supportedLocales[l];
            if (loadLocale[locale]) {
                includeJavascriptFiles(Timeplot.jsUrlPrefix + "locales." + locale + ".", localizedJavascriptFiles);
                includeCssFiles(Timeplot.cssUrlPrefix + "styles/l10n/" + locale + "/", localizedCssFiles);
            }
        }
        
        Timeplot.Platform.serverLocale = defaultServerLocale;
        Timeplot.Platform.clientLocale = defaultClientLocale;
    } catch (e) {
        alert(e);
    }
})();
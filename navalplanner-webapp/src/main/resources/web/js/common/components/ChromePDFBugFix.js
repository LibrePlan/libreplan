/*
 * This function is a temporary fix
 * for a bug with chrome.
 * 
 * bug-url: http://code.google.com/p/chromium/issues/detail?id=65395
 * 
 * bug-description: chrome PDF Viewer plugin shrinks PDF and entire surrounding page
 * when including a pdf inside an iframe
 * 
 * bug-notes: this bug seems to be fixed since version 9.0.597.84 
 * but we should keep this fix for a while to support people with
 * older versions
 * 
 * */
zk.afterLoad(function(){
	old = zAu.cmd0.download;
	zAu.cmd0.download = function(url){
		if( ! url.match(/\.pdf$/) ){
			return old.apply(url);
		}
		
		window.open(url, "Report");
	};
});
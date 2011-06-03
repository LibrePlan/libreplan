/*timeplot.js
{{IS_NOTE
	Purpose:

	Description:

	History:
		Wed Jan 17 13:19:47     2008, Created by Gu WeiXing
}}IS_NOTE

Copyright (C) Gu WeiXing. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

var DependencyChecker = {};
DependencyChecker.onAvailable = function(depedencyCheck, callback, retriesLeft) {
    if (retriesLeft === undefined) {
        retriesLeft = 6 * 200;
    }
    if (retriesLeft === 0) {
        alert("the dependency check " + depedencyCheck + " always fails");
        return;
    }
    try {
        var result = depedencyCheck();
    } catch (e) {
        setTimeout(function() {
            DependencyChecker.onAvailable(depedencyCheck, callback, retriesLeft - 1);
        }, 50);
        return;
    }
    callback();
};

/////////////////load  timeplot js library////////////////////////////////
//zk.load("ext.timeline.api.zkTimeline-api-bundle");
//zk.load("ext.timeline.api.zkTimeline-api");

//zkTimeline-api.js

var Timeline = new Object();
Timeline.Platform = new Object();
    /*
        HACK: We need these 2 things here because we cannot simply append
        a <script> element containing code that accesses Timeline.Platform
        to initialize it because IE executes that <script> code first
        before it loads timeline.js and util/platform.js.
    */
//bundle.js
/* timeline.js */



Timeline.strings={};

Timeline.create=function(elmt,bandInfos,orientation,unit){
return new Timeline._Impl(elmt,bandInfos,orientation,unit);
};

Timeline.HORIZONTAL=0;
Timeline.VERTICAL=1;

Timeline._defaultTheme=null;

Timeline.createBandInfo=function(params){
var theme=("theme"in params)?params.theme:Timeline.getDefaultTheme();

var eventSource=("eventSource"in params)?params.eventSource:null;

var ether=new Timeline.LinearEther({
centersOn:("date"in params)?params.date:new Date(),
interval:Timeline.DateTime.gregorianUnitLengths[params.intervalUnit],
pixelsPerInterval:params.intervalPixels
});

var etherPainter=new Timeline.GregorianEtherPainter({
unit:params.intervalUnit,
multiple:("multiple"in params)?params.multiple:1,
theme:theme,
align:("align"in params)?params.align:undefined
});

var layout=new Timeline.StaticTrackBasedLayout({
eventSource:eventSource,
ether:ether,
showText:("showEventText"in params)?params.showEventText:true,
theme:theme
});

var eventPainterParams={
showText:("showEventText"in params)?params.showEventText:true,
layout:layout,
theme:theme
};
if("trackHeight"in params){
eventPainterParams.trackHeight=params.trackHeight;
}
if("trackGap"in params){
eventPainterParams.trackGap=params.trackGap;
}
var eventPainter=new Timeline.DurationEventPainter(eventPainterParams);

return{
width:params.width,
eventSource:eventSource,
timeZone:("timeZone"in params)?params.timeZone:0,
ether:ether,
etherPainter:etherPainter,
eventPainter:eventPainter
};
};

Timeline.createHotZoneBandInfo=function(params){
var theme=("theme"in params)?params.theme:Timeline.getDefaultTheme();

var eventSource=("eventSource"in params)?params.eventSource:null;

var ether=new Timeline.HotZoneEther({
centersOn:("date"in params)?params.date:new Date(),
interval:Timeline.DateTime.gregorianUnitLengths[params.intervalUnit],
pixelsPerInterval:params.intervalPixels,
zones:params.zones
});

var etherPainter=new Timeline.HotZoneGregorianEtherPainter({
unit:params.intervalUnit,
zones:params.zones,
theme:theme,
align:("align"in params)?params.align:undefined
});

var layout=new Timeline.StaticTrackBasedLayout({
eventSource:eventSource,
ether:ether,
theme:theme
});

var eventPainterParams={
showText:("showEventText"in params)?params.showEventText:true,
layout:layout,
theme:theme
};
if("trackHeight"in params){
eventPainterParams.trackHeight=params.trackHeight;
}
if("trackGap"in params){
eventPainterParams.trackGap=params.trackGap;
}
var eventPainter=new Timeline.DurationEventPainter(eventPainterParams);

return{
width:params.width,
eventSource:eventSource,
timeZone:("timeZone"in params)?params.timeZone:0,
ether:ether,
etherPainter:etherPainter,
eventPainter:eventPainter
};
};

Timeline.getDefaultTheme=function(){
if(Timeline._defaultTheme==null){
Timeline._defaultTheme=Timeline.ClassicTheme.create(Timeline.Platform.getDefaultLocale());
}
return Timeline._defaultTheme;
};

Timeline.setDefaultTheme=function(theme){
Timeline._defaultTheme=theme;
};

Timeline.loadXML=function(url,f){
var fError=function(statusText,status,xmlhttp){
alert("Failed to load data xml from "+url+"\n"+statusText);
};
var fDone=function(xmlhttp){
var xml=xmlhttp.responseXML;
if(!xml.documentElement&&xmlhttp.responseStream){
xml.load(xmlhttp.responseStream);
}
f(xml,url);
};
Timeline.XmlHttp.get(url,fError,fDone);
};


Timeline.loadJSON=function(url,f){
var fError=function(statusText,status,xmlhttp){
alert("Failed to load json data from "+url+"\n"+statusText);
};
var fDone=function(xmlhttp){
f(eval('('+xmlhttp.responseText+')'),url);
};
Timeline.XmlHttp.get(url,fError,fDone);
};


Timeline._Impl=function(elmt,bandInfos,orientation,unit){
this._containerDiv=elmt;

this._bandInfos=bandInfos;
this._orientation=orientation==null?Timeline.HORIZONTAL:orientation;
this._unit=(unit!=null)?unit:Timeline.NativeDateUnit;

this._initialize();
};

Timeline._Impl.prototype.dispose=function(){
for(var i=0;i<this._bands.length;i++){
this._bands[i].dispose();
}
this._bands=null;
this._bandInfos=null;
this._containerDiv.innerHTML="";
};

Timeline._Impl.prototype.getBandCount=function(){
return this._bands.length;
};

Timeline._Impl.prototype.getBand=function(index){
return this._bands[index];
};

Timeline._Impl.prototype.layout=function(){
this._distributeWidths();
};

Timeline._Impl.prototype.paint=function(){
for(var i=0;i<this._bands.length;i++){
this._bands[i].paint();
}
};

Timeline._Impl.prototype.getDocument=function(){
return this._containerDiv.ownerDocument;
};

Timeline._Impl.prototype.addDiv=function(div){
this._containerDiv.appendChild(div);
};

Timeline._Impl.prototype.removeDiv=function(div){
this._containerDiv.removeChild(div);
};

Timeline._Impl.prototype.isHorizontal=function(){
return this._orientation==Timeline.HORIZONTAL;
};

Timeline._Impl.prototype.isVertical=function(){
return this._orientation==Timeline.VERTICAL;
};

Timeline._Impl.prototype.getPixelLength=function(){
return this._orientation==Timeline.HORIZONTAL?
this._containerDiv.offsetWidth:this._containerDiv.offsetHeight;
};

Timeline._Impl.prototype.getPixelWidth=function(){
return this._orientation==Timeline.VERTICAL?
this._containerDiv.offsetWidth:this._containerDiv.offsetHeight;
};

Timeline._Impl.prototype.getUnit=function(){
return this._unit;
};

Timeline._Impl.prototype.loadXML=function(url,f){
var tl=this;


var fError=function(statusText,status,xmlhttp){
alert("Failed to load data xml from "+url+"\n"+statusText);
tl.hideLoadingMessage();
};
var fDone=function(xmlhttp){
try{
var xml=xmlhttp.responseXML;
if(!xml.documentElement&&xmlhttp.responseStream){
xml.load(xmlhttp.responseStream);
}
f(xml,url);
}finally{
tl.hideLoadingMessage();
}
};

this.showLoadingMessage();
window.setTimeout(function(){Timeline.XmlHttp.get(url,fError,fDone);},0);
};

Timeline._Impl.prototype.loadJSON=function(url,f){
var tl=this;


var fError=function(statusText,status,xmlhttp){
alert("Failed to load json data from "+url+"\n"+statusText);
tl.hideLoadingMessage();
};
var fDone=function(xmlhttp){
try{
f(eval('('+xmlhttp.responseText+')'),url);
}finally{
tl.hideLoadingMessage();
}
};

this.showLoadingMessage();
window.setTimeout(function(){Timeline.XmlHttp.get(url,fError,fDone);},0);
};

Timeline._Impl.prototype._initialize=function(){
var containerDiv=this._containerDiv;
var doc=containerDiv.ownerDocument;

containerDiv.className=
containerDiv.className.split(" ").concat("timeline-container").join(" ");

while(containerDiv.firstChild){
containerDiv.removeChild(containerDiv.firstChild);
}


var elmtCopyright=Timeline.Graphics.createTranslucentImage(doc,Timeline.urlPrefix+(this.isHorizontal()?"images/copyright-vertical.png":"images/copyright.png"));
elmtCopyright.className="timeline-copyright";
elmtCopyright.title="Timeline (c) SIMILE - http://simile.mit.edu/timeline/";
Timeline.DOM.registerEvent(elmtCopyright,"click",function(){window.location="http://simile.mit.edu/timeline/";});
containerDiv.appendChild(elmtCopyright);


this._bands=[];
for(var i=0;i<this._bandInfos.length;i++){
var band=new Timeline._Band(this,this._bandInfos[i],i);
this._bands.push(band);
}
this._distributeWidths();


for(var i=0;i<this._bandInfos.length;i++){
var bandInfo=this._bandInfos[i];
if("syncWith"in bandInfo){
this._bands[i].setSyncWithBand(
this._bands[bandInfo.syncWith],
("highlight"in bandInfo)?bandInfo.highlight:false
);
}
}


var message=Timeline.Graphics.createMessageBubble(doc);
message.containerDiv.className="timeline-message-container";
containerDiv.appendChild(message.containerDiv);

message.contentDiv.className="timeline-message";
message.contentDiv.innerHTML="<img src='"+Timeline.urlPrefix+"images/progress-running.gif' /> Loading...";

this.showLoadingMessage=function(){message.containerDiv.style.display="block";};
this.hideLoadingMessage=function(){message.containerDiv.style.display="none";};
};

Timeline._Impl.prototype._distributeWidths=function(){
var length=this.getPixelLength();
var width=this.getPixelWidth();
var cumulativeWidth=0;

for(var i=0;i<this._bands.length;i++){
var band=this._bands[i];
var bandInfos=this._bandInfos[i];
var widthString=bandInfos.width;

var x=widthString.indexOf("%");
if(x>0){
var percent=parseInt(widthString.substr(0,x));
var bandWidth=percent*width/100;
}else{
var bandWidth=parseInt(widthString);
}

band.setBandShiftAndWidth(cumulativeWidth,bandWidth);
band.setViewLength(length);

cumulativeWidth+=bandWidth;
}
};


Timeline._Band=function(timeline,bandInfo,index){
this._timeline=timeline;
this._bandInfo=bandInfo;
this._index=index;

this._locale=("locale"in bandInfo)?bandInfo.locale:Timeline.Platform.getDefaultLocale();
this._timeZone=("timeZone"in bandInfo)?bandInfo.timeZone:0;
this._labeller=("labeller"in bandInfo)?bandInfo.labeller:
timeline.getUnit().createLabeller(this._locale,this._timeZone);

this._dragging=false;
this._changing=false;
this._originalScrollSpeed=5;
this._scrollSpeed=this._originalScrollSpeed;
this._onScrollListeners=[];

var b=this;
this._syncWithBand=null;
this._syncWithBandHandler=function(band){
b._onHighlightBandScroll();
};
this._selectorListener=function(band){
b._onHighlightBandScroll();
};


var inputDiv=this._timeline.getDocument().createElement("div");
inputDiv.className="timeline-band-input";
this._timeline.addDiv(inputDiv);

this._keyboardInput=document.createElement("input");
this._keyboardInput.type="text";
inputDiv.appendChild(this._keyboardInput);
Timeline.DOM.registerEventWithObject(this._keyboardInput,"keydown",this,this._onKeyDown);
Timeline.DOM.registerEventWithObject(this._keyboardInput,"keyup",this,this._onKeyUp);


this._div=this._timeline.getDocument().createElement("div");
this._div.className="timeline-band";
this._timeline.addDiv(this._div);

Timeline.DOM.registerEventWithObject(this._div,"mousedown",this,this._onMouseDown);
Timeline.DOM.registerEventWithObject(this._div,"mousemove",this,this._onMouseMove);
Timeline.DOM.registerEventWithObject(this._div,"mouseup",this,this._onMouseUp);
Timeline.DOM.registerEventWithObject(this._div,"mouseout",this,this._onMouseOut);
Timeline.DOM.registerEventWithObject(this._div,"dblclick",this,this._onDblClick);


this._innerDiv=this._timeline.getDocument().createElement("div");
this._innerDiv.className="timeline-band-inner";
this._div.appendChild(this._innerDiv);


this._ether=bandInfo.ether;
bandInfo.ether.initialize(timeline);

this._etherPainter=bandInfo.etherPainter;
bandInfo.etherPainter.initialize(this,timeline);

this._eventSource=bandInfo.eventSource;
if(this._eventSource){
this._eventListener={
onAddMany:function(){b._onAddMany();},
onClear:function(){b._onClear();}
}
this._eventSource.addListener(this._eventListener);
}

this._eventPainter=bandInfo.eventPainter;
bandInfo.eventPainter.initialize(this,timeline);

this._decorators=("decorators"in bandInfo)?bandInfo.decorators:[];
for(var i=0;i<this._decorators.length;i++){
this._decorators[i].initialize(this,timeline);
}

this._bubble=null;
};

Timeline._Band.SCROLL_MULTIPLES=5;

Timeline._Band.prototype.dispose=function(){
this.closeBubble();

if(this._eventSource){
this._eventSource.removeListener(this._eventListener);
this._eventListener=null;
this._eventSource=null;
}

this._timeline=null;
this._bandInfo=null;

this._labeller=null;
this._ether=null;
this._etherPainter=null;
this._eventPainter=null;
this._decorators=null;

this._onScrollListeners=null;
this._syncWithBandHandler=null;
this._selectorListener=null;

this._div=null;
this._innerDiv=null;
this._keyboardInput=null;
this._bubble=null;
};

Timeline._Band.prototype.addOnScrollListener=function(listener){
this._onScrollListeners.push(listener);
};

Timeline._Band.prototype.removeOnScrollListener=function(listener){
for(var i=0;i<this._onScrollListeners.length;i++){
if(this._onScrollListeners[i]==listener){
this._onScrollListeners.splice(i,1);
break;
}
}
};

Timeline._Band.prototype.setSyncWithBand=function(band,highlight){
if(this._syncWithBand){
this._syncWithBand.removeOnScrollListener(this._syncWithBandHandler);
}

this._syncWithBand=band;
this._syncWithBand.addOnScrollListener(this._syncWithBandHandler);
this._highlight=highlight;
this._positionHighlight();
};

Timeline._Band.prototype.getLocale=function(){
return this._locale;
};

Timeline._Band.prototype.getTimeZone=function(){
return this._timeZone;
};

Timeline._Band.prototype.getLabeller=function(){
return this._labeller;
};

Timeline._Band.prototype.getIndex=function(){
return this._index;
};

Timeline._Band.prototype.getEther=function(){
return this._ether;
};

Timeline._Band.prototype.getEtherPainter=function(){
return this._etherPainter;
};

Timeline._Band.prototype.getEventSource=function(){
return this._eventSource;
};

Timeline._Band.prototype.getEventPainter=function(){
return this._eventPainter;
};

Timeline._Band.prototype.layout=function(){
this.paint();
};

Timeline._Band.prototype.paint=function(){
this._etherPainter.paint();
this._paintDecorators();
this._paintEvents();
};

Timeline._Band.prototype.softLayout=function(){
this.softPaint();
};

Timeline._Band.prototype.softPaint=function(){
this._etherPainter.softPaint();
this._softPaintDecorators();
this._softPaintEvents();
};

Timeline._Band.prototype.setBandShiftAndWidth=function(shift,width){
var inputDiv=this._keyboardInput.parentNode;
var middle=shift+Math.floor(width/2);
if(this._timeline.isHorizontal()){
this._div.style.top=shift+"px";
this._div.style.height=width+"px";

inputDiv.style.top=middle+"px";
inputDiv.style.left="-1em";
}else{
this._div.style.left=shift+"px";
this._div.style.width=width+"px";

inputDiv.style.left=middle+"px";
inputDiv.style.top="-1em";
}
};

Timeline._Band.prototype.getViewWidth=function(){
if(this._timeline.isHorizontal()){
return this._div.offsetHeight;
}else{
return this._div.offsetWidth;
}
};

Timeline._Band.prototype.setViewLength=function(length){
this._viewLength=length;
this._recenterDiv();
this._onChanging();
};

Timeline._Band.prototype.getViewLength=function(){
return this._viewLength;
};

Timeline._Band.prototype.getTotalViewLength=function(){
return Timeline._Band.SCROLL_MULTIPLES*this._viewLength;
};

Timeline._Band.prototype.getViewOffset=function(){
return this._viewOffset;
};

Timeline._Band.prototype.getMinDate=function(){
return this._ether.pixelOffsetToDate(this._viewOffset);
};

Timeline._Band.prototype.getMaxDate=function(){
return this._ether.pixelOffsetToDate(this._viewOffset+Timeline._Band.SCROLL_MULTIPLES*this._viewLength);
};

Timeline._Band.prototype.getMinVisibleDate=function(){
return this._ether.pixelOffsetToDate(0);
};

Timeline._Band.prototype.getMaxVisibleDate=function(){
return this._ether.pixelOffsetToDate(this._viewLength);
};

Timeline._Band.prototype.getCenterVisibleDate=function(){
return this._ether.pixelOffsetToDate(this._viewLength/2);
};

Timeline._Band.prototype.setMinVisibleDate=function(date){
if(!this._changing){
this._moveEther(Math.round(-this._ether.dateToPixelOffset(date)));
}
};

Timeline._Band.prototype.setMaxVisibleDate=function(date){
if(!this._changing){
this._moveEther(Math.round(this._viewLength-this._ether.dateToPixelOffset(date)));
}
};

Timeline._Band.prototype.setCenterVisibleDate=function(date){
if(!this._changing){
this._moveEther(Math.round(this._viewLength/2-this._ether.dateToPixelOffset(date)));
}
};

Timeline._Band.prototype.dateToPixelOffset=function(date){
return this._ether.dateToPixelOffset(date)-this._viewOffset;
};

Timeline._Band.prototype.pixelOffsetToDate=function(pixels){
return this._ether.pixelOffsetToDate(pixels+this._viewOffset);
};

Timeline._Band.prototype.createLayerDiv=function(zIndex){
var div=this._timeline.getDocument().createElement("div");
div.className="timeline-band-layer";
div.style.zIndex=zIndex;
this._innerDiv.appendChild(div);

var innerDiv=this._timeline.getDocument().createElement("div");
innerDiv.className="timeline-band-layer-inner";
if(Timeline.Platform.browser.isIE){
innerDiv.style.cursor="move";
}else{
innerDiv.style.cursor="-moz-grab";
}
div.appendChild(innerDiv);

return innerDiv;
};

Timeline._Band.prototype.removeLayerDiv=function(div){
this._innerDiv.removeChild(div.parentNode);
};

Timeline._Band.prototype.closeBubble=function(){
if(this._bubble!=null){
this._bubble.close();
this._bubble=null;
}
};

Timeline._Band.prototype.openBubbleForPoint=function(pageX,pageY,width,height){
this.closeBubble();

this._bubble=Timeline.Graphics.createBubbleForPoint(
this._timeline.getDocument(),pageX,pageY,width,height);

return this._bubble.content;
};

Timeline._Band.prototype.scrollToCenter=function(date){
var pixelOffset=this._ether.dateToPixelOffset(date);
if(pixelOffset<-this._viewLength/2){
this.setCenterVisibleDate(this.pixelOffsetToDate(pixelOffset+this._viewLength));
}else if(pixelOffset>3*this._viewLength/2){
this.setCenterVisibleDate(this.pixelOffsetToDate(pixelOffset-this._viewLength));
}
this._autoScroll(Math.round(this._viewLength/2-this._ether.dateToPixelOffset(date)));
};

Timeline._Band.prototype._onMouseDown=function(innerFrame,evt,target){
this.closeBubble();

this._dragging=true;
this._dragX=evt.clientX;
this._dragY=evt.clientY;
};

Timeline._Band.prototype._onMouseMove=function(innerFrame,evt,target){
if(this._dragging){
var diffX=evt.clientX-this._dragX;
var diffY=evt.clientY-this._dragY;

this._dragX=evt.clientX;
this._dragY=evt.clientY;

this._moveEther(this._timeline.isHorizontal()?diffX:diffY);
this._positionHighlight();
}
};

Timeline._Band.prototype._onMouseUp=function(innerFrame,evt,target){
this._dragging=false;
this._keyboardInput.focus();
};

Timeline._Band.prototype._onMouseOut=function(innerFrame,evt,target){
var coords=Timeline.DOM.getEventRelativeCoordinates(evt,innerFrame);
coords.x+=this._viewOffset;
if(coords.x<0||coords.x>innerFrame.offsetWidth||
coords.y<0||coords.y>innerFrame.offsetHeight){
this._dragging=false;
}
};

Timeline._Band.prototype._onDblClick=function(innerFrame,evt,target){
var coords=Timeline.DOM.getEventRelativeCoordinates(evt,innerFrame);
var distance=coords.x-(this._viewLength/2-this._viewOffset);

this._autoScroll(-distance);
};

Timeline._Band.prototype._onKeyDown=function(keyboardInput,evt,target){
if(!this._dragging){
switch(evt.keyCode){
case 27:
break;
case 37:
case 38:
this._scrollSpeed=Math.min(50,Math.abs(this._scrollSpeed*1.05));
this._moveEther(this._scrollSpeed);
break;
case 39:
case 40:
this._scrollSpeed=-Math.min(50,Math.abs(this._scrollSpeed*1.05));
this._moveEther(this._scrollSpeed);
break;
default:
return true;
}
this.closeBubble();

Timeline.DOM.cancelEvent(evt);
return false;
}
return true;
};

Timeline._Band.prototype._onKeyUp=function(keyboardInput,evt,target){
if(!this._dragging){
this._scrollSpeed=this._originalScrollSpeed;

switch(evt.keyCode){
case 35:
this.setCenterVisibleDate(this._eventSource.getLatestDate());
break;
case 36:
this.setCenterVisibleDate(this._eventSource.getEarliestDate());
break;
case 33:
this._autoScroll(this._timeline.getPixelLength());
break;
case 34:
this._autoScroll(-this._timeline.getPixelLength());
break;
default:
return true;
}

this.closeBubble();

Timeline.DOM.cancelEvent(evt);
return false;
}
return true;
};

Timeline._Band.prototype._autoScroll=function(distance){
var b=this;
var a=Timeline.Graphics.createAnimation(function(abs,diff){
b._moveEther(diff);
},0,distance,1000);
a.run();
};

Timeline._Band.prototype._moveEther=function(shift){
this.closeBubble();

this._viewOffset+=shift;
this._ether.shiftPixels(-shift);
if(this._timeline.isHorizontal()){
this._div.style.left=this._viewOffset+"px";
}else{
this._div.style.top=this._viewOffset+"px";
}

if(this._viewOffset>-this._viewLength*0.5||
this._viewOffset<-this._viewLength*(Timeline._Band.SCROLL_MULTIPLES-1.5)){

this._recenterDiv();
}else{
this.softLayout();
}

this._onChanging();
}

Timeline._Band.prototype._onChanging=function(){
this._changing=true;

this._fireOnScroll();
this._setSyncWithBandDate();

this._changing=false;
};

Timeline._Band.prototype._fireOnScroll=function(){
for(var i=0;i<this._onScrollListeners.length;i++){
this._onScrollListeners[i](this);
}
};

Timeline._Band.prototype._setSyncWithBandDate=function(){
if(this._syncWithBand){
var centerDate=this._ether.pixelOffsetToDate(this.getViewLength()/2);
this._syncWithBand.setCenterVisibleDate(centerDate);
}
};

Timeline._Band.prototype._onHighlightBandScroll=function(){
if(this._syncWithBand){
var centerDate=this._syncWithBand.getCenterVisibleDate();
var centerPixelOffset=this._ether.dateToPixelOffset(centerDate);

this._moveEther(Math.round(this._viewLength/2-centerPixelOffset));

if(this._highlight){
this._etherPainter.setHighlight(
this._syncWithBand.getMinVisibleDate(),
this._syncWithBand.getMaxVisibleDate());
}
}
};

Timeline._Band.prototype._onAddMany=function(){
this._paintEvents();
};

Timeline._Band.prototype._onClear=function(){
this._paintEvents();
};

Timeline._Band.prototype._positionHighlight=function(){
if(this._syncWithBand){
var startDate=this._syncWithBand.getMinVisibleDate();
var endDate=this._syncWithBand.getMaxVisibleDate();

if(this._highlight){
this._etherPainter.setHighlight(startDate,endDate);
}
}
};

Timeline._Band.prototype._recenterDiv=function(){
this._viewOffset=-this._viewLength*(Timeline._Band.SCROLL_MULTIPLES-1)/2;
if(this._timeline.isHorizontal()){
this._div.style.left=this._viewOffset+"px";
this._div.style.width=(Timeline._Band.SCROLL_MULTIPLES*this._viewLength)+"px";
}else{
this._div.style.top=this._viewOffset+"px";
this._div.style.height=(Timeline._Band.SCROLL_MULTIPLES*this._viewLength)+"px";
}
this.layout();
};

Timeline._Band.prototype._paintEvents=function(){
this._eventPainter.paint();
};

Timeline._Band.prototype._softPaintEvents=function(){
this._eventPainter.softPaint();
};

Timeline._Band.prototype._paintDecorators=function(){
for(var i=0;i<this._decorators.length;i++){
this._decorators[i].paint();
}
};

Timeline._Band.prototype._softPaintDecorators=function(){
for(var i=0;i<this._decorators.length;i++){
this._decorators[i].softPaint();
}
};


/* platform.js */



Timeline.Platform.os={
isMac:false,
isWin:false,
isWin32:false,
isUnix:false
};
Timeline.Platform.browser={
isIE:false,
isNetscape:false,
isMozilla:false,
isFirefox:false,
isOpera:false,
isSafari:false,

majorVersion:0,
minorVersion:0
};

(function(){
var an=navigator.appName.toLowerCase();
var ua=navigator.userAgent.toLowerCase();



Timeline.Platform.os.isMac=(ua.indexOf('mac')!=-1);

Timeline.Platform.os.isWin=(ua.indexOf('win')!=-1);

Timeline.Platform.os.isWin32=Timeline.Platform.isWin&&(
ua.indexOf('95')!=-1||
ua.indexOf('98')!=-1||
ua.indexOf('nt')!=-1||
ua.indexOf('win32')!=-1||
ua.indexOf('32bit')!=-1
);
Timeline.Platform.os.isUnix=(ua.indexOf('x11')!=-1);



Timeline.Platform.browser.isIE=(an.indexOf("microsoft")!=-1);
Timeline.Platform.browser.isNetscape=(an.indexOf("netscape")!=-1);
Timeline.Platform.browser.isMozilla=(ua.indexOf("mozilla")!=-1);
Timeline.Platform.browser.isFirefox=(ua.indexOf("firefox")!=-1);
Timeline.Platform.browser.isOpera=(an.indexOf("opera")!=-1);


var parseVersionString=function(s){
var a=s.split(".");
Timeline.Platform.browser.majorVersion=parseInt(a[0]);
Timeline.Platform.browser.minorVersion=parseInt(a[1]);
};
var indexOf=function(s,sub,start){
var i=s.indexOf(sub,start);
return i>=0?i:s.length;
};

if(Timeline.Platform.browser.isMozilla){
var offset=ua.indexOf("mozilla/");
if(offset>=0){
parseVersionString(ua.substring(offset+8,indexOf(ua," ",offset)));
}
}

if(Timeline.Platform.browser.isIE){
var offset=ua.indexOf("msie ");
if(offset>=0){
parseVersionString(ua.substring(offset+5,indexOf(ua,";",offset)));
}
}
if(Timeline.Platform.browser.isNetscape){
var offset=ua.indexOf("rv:");
if(offset>=0){
parseVersionString(ua.substring(offset+3,indexOf(ua,")",offset)));
}
}

if(Timeline.Platform.browser.isFirefox){
var offset=ua.indexOf("firefox/");
if(offset>=0){
parseVersionString(ua.substring(offset+8,indexOf(ua," ",offset)));
}
}
})();

Timeline.Platform.getDefaultLocale=function(){
return Timeline.Platform.clientLocale;
};

/* data-structure.js */



Timeline.SortedArray=function(compare,initialArray){
this._a=(initialArray instanceof Array)?initialArray:[];
this._compare=compare;
};

Timeline.SortedArray.prototype.add=function(elmt){
var sa=this;
var index=this.find(function(elmt2){
return sa._compare(elmt2,elmt);
});

if(index<this._a.length){
this._a.splice(index,0,elmt);
}else{
this._a.push(elmt);
}
};

Timeline.SortedArray.prototype.remove=function(elmt){
var sa=this;
var index=this.find(function(elmt2){
return sa._compare(elmt2,elmt);
});

while(index<this._a.length&&this._compare(this._a[index],elmt)==0){
if(this._a[index]==elmt){
this._a.splice(index,1);
return true;
}else{
index++;
}
}
return false;
};

Timeline.SortedArray.prototype.removeAll=function(){
this._a=[];
};

Timeline.SortedArray.prototype.elementAt=function(index){
return this._a[index];
};

Timeline.SortedArray.prototype.length=function(){
return this._a.length;
};

Timeline.SortedArray.prototype.find=function(compare){
var a=0;
var b=this._a.length;

while(a<b){
var mid=Math.floor((a+b)/2);
var c=compare(this._a[mid]);
if(mid==a){
return c<0?a+1:a;
}else if(c<0){
a=mid;
}else{
b=mid;
}
}
return a;
};

Timeline.SortedArray.prototype.getFirst=function(){
return(this._a.length>0)?this._a[0]:null;
};

Timeline.SortedArray.prototype.getLast=function(){
return(this._a.length>0)?this._a[this._a.length-1]:null;
};



Timeline.EventIndex=function(unit){
var eventIndex=this;

this._unit=(unit!=null)?unit:Timeline.NativeDateUnit;
this._events=new Timeline.SortedArray(
function(event1,event2){
return eventIndex._unit.compare(event1.getStart(),event2.getStart());
}
);
this._indexed=true;
};

Timeline.EventIndex.prototype.getUnit=function(){
return this._unit;
};

Timeline.EventIndex.prototype.add=function(evt){
this._events.add(evt);
this._indexed=false;
};

Timeline.EventIndex.prototype.removeAll=function(){
this._events.removeAll();
this._indexed=false;
};

Timeline.EventIndex.prototype.getCount=function(){
return this._events.length();
};

Timeline.EventIndex.prototype.getIterator=function(startDate,endDate){
if(!this._indexed){
this._index();
}
return new Timeline.EventIndex._Iterator(this._events,startDate,endDate,this._unit);
};

Timeline.EventIndex.prototype.getAllIterator=function(){
return new Timeline.EventIndex._AllIterator(this._events);
};

Timeline.EventIndex.prototype.getEarliestDate=function(){
var evt=this._events.getFirst();
return(evt==null)?null:evt.getStart();
};

Timeline.EventIndex.prototype.getLatestDate=function(){
var evt=this._events.getLast();
if(evt==null){
return null;
}

if(!this._indexed){
this._index();
}

var index=evt._earliestOverlapIndex;
var date=this._events.elementAt(index).getEnd();
for(var i=index+1;i<this._events.length();i++){
date=this._unit.later(date,this._events.elementAt(i).getEnd());
}

return date;
};

Timeline.EventIndex.prototype._index=function(){


var l=this._events.length();
for(var i=0;i<l;i++){
var evt=this._events.elementAt(i);
evt._earliestOverlapIndex=i;
}

var toIndex=1;
for(var i=0;i<l;i++){
var evt=this._events.elementAt(i);
var end=evt.getEnd();

toIndex=Math.max(toIndex,i+1);
while(toIndex<l){
var evt2=this._events.elementAt(toIndex);
var start2=evt2.getStart();

if(this._unit.compare(start2,end)<0){
evt2._earliestOverlapIndex=i;
toIndex++;
}else{
break;
}
}
}
this._indexed=true;
};

Timeline.EventIndex._Iterator=function(events,startDate,endDate,unit){
this._events=events;
this._startDate=startDate;
this._endDate=endDate;
this._unit=unit;

this._currentIndex=events.find(function(evt){
return unit.compare(evt.getStart(),startDate);
});
if(this._currentIndex-1>=0){
this._currentIndex=this._events.elementAt(this._currentIndex-1)._earliestOverlapIndex;
}
this._currentIndex--;

this._maxIndex=events.find(function(evt){
return unit.compare(evt.getStart(),endDate);
});

this._hasNext=false;
this._next=null;
this._findNext();
};

Timeline.EventIndex._Iterator.prototype={
hasNext:function(){return this._hasNext;},
next:function(){
if(this._hasNext){
var next=this._next;
this._findNext();

return next;
}else{
return null;
}
},
_findNext:function(){
var unit=this._unit;
while((++this._currentIndex)<this._maxIndex){
var evt=this._events.elementAt(this._currentIndex);
if(unit.compare(evt.getStart(),this._endDate)<0&&
unit.compare(evt.getEnd(),this._startDate)>0){

this._next=evt;
this._hasNext=true;
return;
}
}
this._next=null;
this._hasNext=false;
}
};

Timeline.EventIndex._AllIterator=function(events){
this._events=events;
this._index=0;
};

Timeline.EventIndex._AllIterator.prototype={
hasNext:function(){
return this._index<this._events.length();
},
next:function(){
return this._index<this._events.length()?
this._events.elementAt(this._index++):null;
}
};

/* date-time.js */



Timeline.DateTime=new Object();

Timeline.DateTime.MILLISECOND=0;
Timeline.DateTime.SECOND=1;
Timeline.DateTime.MINUTE=2;
Timeline.DateTime.HOUR=3;
Timeline.DateTime.DAY=4;
Timeline.DateTime.WEEK=5;
Timeline.DateTime.MONTH=6;
Timeline.DateTime.YEAR=7;
Timeline.DateTime.DECADE=8;
Timeline.DateTime.CENTURY=9;
Timeline.DateTime.MILLENNIUM=10;

Timeline.DateTime.EPOCH=-1;
Timeline.DateTime.ERA=-2;

Timeline.DateTime.gregorianUnitLengths=[];
(function(){
var d=Timeline.DateTime;
var a=d.gregorianUnitLengths;

a[d.MILLISECOND]=1;
a[d.SECOND]=1000;
a[d.MINUTE]=a[d.SECOND]*60;
a[d.HOUR]=a[d.MINUTE]*60;
a[d.DAY]=a[d.HOUR]*24;
a[d.WEEK]=a[d.DAY]*7;
a[d.MONTH]=a[d.DAY]*31;
a[d.YEAR]=a[d.DAY]*365;
a[d.DECADE]=a[d.YEAR]*10;
a[d.CENTURY]=a[d.YEAR]*100;
a[d.MILLENNIUM]=a[d.YEAR]*1000;
})();

Timeline.DateTime.parseGregorianDateTime=function(o){
if(o==null){
return null;
}else if(o instanceof Date){
return o;
}

var s=o.toString();
if(s.length>0&&s.length<8){
var space=s.indexOf(" ");
if(space>0){
var year=parseInt(s.substr(0,space));
var suffix=s.substr(space+1);
if(suffix.toLowerCase()=="bc"){
year=1-year;
}
}else{
var year=parseInt(s);
}

var d=new Date(0);
d.setUTCFullYear(year);

return d;
}

try{
return new Date(Date.parse(s));
}catch(e){
return null;
}
};

Timeline.DateTime._iso8601DateRegExp="^(-?)([0-9]{4})("+[
"(-?([0-9]{2})(-?([0-9]{2}))?)",
"(-?([0-9]{3}))",
"(-?W([0-9]{2})(-?([1-7]))?)"
].join("|")+")?$";

Timeline.DateTime.setIso8601Date=function(dateObject,string){


var regexp=Timeline.DateTime._iso8601DateRegExp;
var d=string.match(new RegExp(regexp));
if(!d){
throw new Error("Invalid date string: "+string);
}

var sign=(d[1]=="-")?-1:1;
var year=sign*d[2];
var month=d[5];
var date=d[7];
var dayofyear=d[9];
var week=d[11];
var dayofweek=(d[13])?d[13]:1;

dateObject.setUTCFullYear(year);
if(dayofyear){
dateObject.setUTCMonth(0);
dateObject.setUTCDate(Number(dayofyear));
}else if(week){
dateObject.setUTCMonth(0);
dateObject.setUTCDate(1);
var gd=dateObject.getUTCDay();
var day=(gd)?gd:7;
var offset=Number(dayofweek)+(7*Number(week));

if(day<=4){
dateObject.setUTCDate(offset+1-day);
}else{
dateObject.setUTCDate(offset+8-day);
}
}else{
if(month){
dateObject.setUTCDate(1);
dateObject.setUTCMonth(month-1);
}
if(date){
dateObject.setUTCDate(date);
}
}

return dateObject;
};

Timeline.DateTime.setIso8601Time=function(dateObject,string){



var timezone="Z|(([-+])([0-9]{2})(:?([0-9]{2}))?)$";
var d=string.match(new RegExp(timezone));

var offset=0;
if(d){
if(d[0]!='Z'){
offset=(Number(d[3])*60)+Number(d[5]);
offset*=((d[2]=='-')?1:-1);
}
string=string.substr(0,string.length-d[0].length);
}


var regexp="^([0-9]{2})(:?([0-9]{2})(:?([0-9]{2})(\.([0-9]+))?)?)?$";
var d=string.match(new RegExp(regexp));
if(!d){
dojo.debug("invalid time string: "+string);
return false;
}
var hours=d[1];
var mins=Number((d[3])?d[3]:0);
var secs=(d[5])?d[5]:0;
var ms=d[7]?(Number("0."+d[7])*1000):0;

dateObject.setUTCHours(hours);
dateObject.setUTCMinutes(mins);
dateObject.setUTCSeconds(secs);
dateObject.setUTCMilliseconds(ms);

return dateObject;
};

Timeline.DateTime.setIso8601=function(dateObject,string){


var comps=(string.indexOf("T")==-1)?string.split(" "):string.split("T");

Timeline.DateTime.setIso8601Date(dateObject,comps[0]);
if(comps.length==2){
Timeline.DateTime.setIso8601Time(dateObject,comps[1]);
}
return dateObject;
};

Timeline.DateTime.parseIso8601DateTime=function(string){
try{
return Timeline.DateTime.setIso8601(new Date(0),string);
}catch(e){
return null;
}
};

Timeline.DateTime.roundDownToInterval=function(date,intervalUnit,timeZone,multiple,firstDayOfWeek){
var timeShift=timeZone*
Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.HOUR];

var date2=new Date(date.getTime()+timeShift);
var clearInDay=function(d){
d.setUTCMilliseconds(0);
d.setUTCSeconds(0);
d.setUTCMinutes(0);
d.setUTCHours(0);
};
var clearInYear=function(d){
clearInDay(d);
d.setUTCDate(1);
d.setUTCMonth(0);
};

switch(intervalUnit){
case Timeline.DateTime.MILLISECOND:
var x=date2.getUTCMilliseconds();
date2.setUTCMilliseconds(x-(x%multiple));
break;
case Timeline.DateTime.SECOND:
date2.setUTCMilliseconds(0);

var x=date2.getUTCSeconds();
date2.setUTCSeconds(x-(x%multiple));
break;
case Timeline.DateTime.MINUTE:
date2.setUTCMilliseconds(0);
date2.setUTCSeconds(0);

var x=date2.getUTCMinutes();
date2.setTime(date2.getTime()-
(x%multiple)*Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.MINUTE]);
break;
case Timeline.DateTime.HOUR:
date2.setUTCMilliseconds(0);
date2.setUTCSeconds(0);
date2.setUTCMinutes(0);

var x=date2.getUTCHours();
date2.setUTCHours(x-(x%multiple));
break;
case Timeline.DateTime.DAY:
clearInDay(date2);
break;
case Timeline.DateTime.WEEK:
clearInDay(date2);
var d=(date2.getUTCDay()+7-firstDayOfWeek)%7;
date2.setTime(date2.getTime()-
d*Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.DAY]);
break;
case Timeline.DateTime.MONTH:
clearInDay(date2);
date2.setUTCDate(1);

var x=date2.getUTCMonth();
date2.setUTCMonth(x-(x%multiple));
break;
case Timeline.DateTime.YEAR:
clearInYear(date2);

var x=date2.getUTCFullYear();
date2.setUTCFullYear(x-(x%multiple));
break;
case Timeline.DateTime.DECADE:
clearInYear(date2);
date2.setUTCFullYear(Math.floor(date2.getUTCFullYear()/10)*10);
break;
case Timeline.DateTime.CENTURY:
clearInYear(date2);
date2.setUTCFullYear(Math.floor(date2.getUTCFullYear()/100)*100);
break;
case Timeline.DateTime.MILLENNIUM:
clearInYear(date2);
date2.setUTCFullYear(Math.floor(date2.getUTCFullYear()/1000)*1000);
break;
}

date.setTime(date2.getTime()-timeShift);
};

Timeline.DateTime.roundUpToInterval=function(date,intervalUnit,timeZone,multiple,firstDayOfWeek){
var originalTime=date.getTime();
Timeline.DateTime.roundDownToInterval(date,intervalUnit,timeZone,multiple,firstDayOfWeek);
if(date.getTime()<originalTime){
date.setTime(date.getTime()+
Timeline.DateTime.gregorianUnitLengths[intervalUnit]*multiple);
}
};

Timeline.DateTime.incrementByInterval=function(date,intervalUnit){
switch(intervalUnit){
case Timeline.DateTime.MILLISECOND:
date.setTime(date.getTime()+1)
break;
case Timeline.DateTime.SECOND:
date.setTime(date.getTime()+1000);
break;
case Timeline.DateTime.MINUTE:
date.setTime(date.getTime()+
Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.MINUTE]);
break;
case Timeline.DateTime.HOUR:
date.setTime(date.getTime()+
Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.HOUR]);
break;
case Timeline.DateTime.DAY:
date.setUTCDate(date.getUTCDate()+1);
break;
case Timeline.DateTime.WEEK:
date.setUTCDate(date.getUTCDate()+7);
break;
case Timeline.DateTime.MONTH:
date.setUTCMonth(date.getUTCMonth()+1);
break;
case Timeline.DateTime.YEAR:
date.setUTCFullYear(date.getUTCFullYear()+1);
break;
case Timeline.DateTime.DECADE:
date.setUTCFullYear(date.getUTCFullYear()+10);
break;
case Timeline.DateTime.CENTURY:
date.setUTCFullYear(date.getUTCFullYear()+100);
break;
case Timeline.DateTime.MILLENNIUM:
date.setUTCFullYear(date.getUTCFullYear()+1000);
break;
}
};

Timeline.DateTime.removeTimeZoneOffset=function(date,timeZone){
return new Date(date.getTime()+
timeZone*Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.HOUR]);
};



/* debug.js */



Timeline.Debug=new Object();

Timeline.Debug.log=function(msg){
};

Timeline.Debug.exception=function(e){
alert("Caught exception: "+(Timeline.Platform.isIE?e.message:e));
};



/* dom.js */



Timeline.DOM=new Object();

Timeline.DOM.registerEventWithObject=function(elmt,eventName,obj,handler){
Timeline.DOM.registerEvent(elmt,eventName,function(elmt2,evt,target){
return handler.call(obj,elmt2,evt,target);
});
};

Timeline.DOM.registerEvent=function(elmt,eventName,handler){
var handler2=function(evt){
evt=(evt)?evt:((event)?event:null);
if(evt){
var target=(evt.target)?
evt.target:((evt.srcElement)?evt.srcElement:null);

if(target){

target=(target.nodeType==1||target.nodeType==9)?
target:target.parentNode;

}

return handler(elmt,evt,target);
}
return true;
}

if(Timeline.Platform.browser.isIE){
elmt.attachEvent("on"+eventName,handler2);
}else{
elmt.addEventListener(eventName,handler2,false);
}
};

Timeline.DOM.getPageCoordinates=function(elmt){
var left=0;
var top=0;

if(elmt.nodeType!=1){
elmt=elmt.parentNode;
}

while(elmt!=null){
left+=elmt.offsetLeft;
top+=elmt.offsetTop;

elmt=elmt.offsetParent;
}
return{left:left,top:top};
};

Timeline.DOM.getEventRelativeCoordinates=function(evt,elmt){
if(Timeline.Platform.browser.isIE){
return{
x:evt.offsetX,
y:evt.offsetY
};
}else{
var coords=Timeline.DOM.getPageCoordinates(elmt);
return{
x:evt.pageX-coords.left,
y:evt.pageY-coords.top
};
}
};

Timeline.DOM.cancelEvent=function(evt){
evt.returnValue=false;
evt.cancelBubble=true;
if("preventDefault"in evt){
evt.preventDefault();
}
};



/* graphics.js */



Timeline.Graphics=new Object();
Timeline.Graphics.pngIsTranslucent=(!Timeline.Platform.browser.isIE)||(Timeline.Platform.browser.majorVersion>6);


Timeline.Graphics.createTranslucentImage=function(doc,url,verticalAlign){
var elmt;
if(Timeline.Graphics.pngIsTranslucent){
elmt=doc.createElement("img");
elmt.setAttribute("src",url);
}else{
elmt=doc.createElement("img");
elmt.style.display="inline";
elmt.style.width="1px";
elmt.style.height="1px";
elmt.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"', sizingMethod='image')";
}
elmt.style.verticalAlign=(verticalAlign!=null)?verticalAlign:"middle";
return elmt;
};

Timeline.Graphics.setOpacity=function(elmt,opacity){
if(Timeline.Platform.browser.isIE){
elmt.style.filter="progid:DXImageTransform.Microsoft.Alpha(Style=0,Opacity="+opacity+")";
}else{
var o=(opacity/100).toString();
elmt.style.opacity=o;
elmt.style.MozOpacity=o;
}
};

Timeline.Graphics._bubbleMargins={
top:33,
bottom:42,
left:33,
right:40
}


Timeline.Graphics._arrowOffsets={
top:0,
bottom:9,
left:1,
right:8
}

Timeline.Graphics._bubblePadding=15;
Timeline.Graphics._bubblePointOffset=6;
Timeline.Graphics._halfArrowWidth=18;

Timeline.Graphics.createBubbleForPoint=function(doc,pageX,pageY,contentWidth,contentHeight){
function getWindowDims(){
if(typeof window.innerWidth=='number'){
return{w:window.innerWidth,h:window.innerHeight};
}else if(document.documentElement&&document.documentElement.clientWidth){
return{
w:document.documentElement.clientWidth,
h:document.documentElement.clientHeight
};
}else if(document.body&&document.body.clientWidth){
return{
w:document.body.clientWidth,
h:document.body.clientHeight
};
}
}

var bubble={
_closed:false,
_doc:doc,
close:function(){
if(!this._closed){
this._doc.body.removeChild(this._div);
this._doc=null;
this._div=null;
this._content=null;
this._closed=true;
}
}
};

var dims=getWindowDims();
var docWidth=dims.w;
var docHeight=dims.h;

var margins=Timeline.Graphics._bubbleMargins;
contentWidth=parseInt(contentWidth,10);
contentHeight=parseInt(contentHeight,10);
var bubbleWidth=margins.left+contentWidth+margins.right;
var bubbleHeight=margins.top+contentHeight+margins.bottom;

var pngIsTranslucent=Timeline.Graphics.pngIsTranslucent;
var urlPrefix=Timeline.urlPrefix;

var setImg=function(elmt,url,width,height){
elmt.style.position="absolute";
elmt.style.width=width+"px";
elmt.style.height=height+"px";
if(pngIsTranslucent){
elmt.style.background="url("+url+")";
}else{
elmt.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"', sizingMethod='crop')";
}
}
var div=doc.createElement("div");
div.style.width=bubbleWidth+"px";
div.style.height=bubbleHeight+"px";
div.style.position="absolute";
div.style.zIndex=1000;
bubble._div=div;

var divInner=doc.createElement("div");
divInner.style.width="100%";
divInner.style.height="100%";
divInner.style.position="relative";
div.appendChild(divInner);

var createImg=function(url,left,top,width,height){
var divImg=doc.createElement("div");
divImg.style.left=left+"px";
divImg.style.top=top+"px";
setImg(divImg,url,width,height);
divInner.appendChild(divImg);
}

createImg(urlPrefix+"images/bubble-top-left.png",0,0,margins.left,margins.top);
createImg(urlPrefix+"images/bubble-top.png",margins.left,0,contentWidth,margins.top);
createImg(urlPrefix+"images/bubble-top-right.png",margins.left+contentWidth,0,margins.right,margins.top);

createImg(urlPrefix+"images/bubble-left.png",0,margins.top,margins.left,contentHeight);
createImg(urlPrefix+"images/bubble-right.png",margins.left+contentWidth,margins.top,margins.right,contentHeight);

createImg(urlPrefix+"images/bubble-bottom-left.png",0,margins.top+contentHeight,margins.left,margins.bottom);
createImg(urlPrefix+"images/bubble-bottom.png",margins.left,margins.top+contentHeight,contentWidth,margins.bottom);
createImg(urlPrefix+"images/bubble-bottom-right.png",margins.left+contentWidth,margins.top+contentHeight,margins.right,margins.bottom);

var divClose=doc.createElement("div");
divClose.style.left=(bubbleWidth-margins.right+Timeline.Graphics._bubblePadding-16-2)+"px";
divClose.style.top=(margins.top-Timeline.Graphics._bubblePadding+1)+"px";
divClose.style.cursor="pointer";
setImg(divClose,urlPrefix+"images/close-button.png",16,16);
Timeline.DOM.registerEventWithObject(divClose,"click",bubble,bubble.close);
divInner.appendChild(divClose);

var divContent=doc.createElement("div");
divContent.style.position="absolute";
divContent.style.left=margins.left+"px";
divContent.style.top=margins.top+"px";
divContent.style.width=contentWidth+"px";
divContent.style.height=contentHeight+"px";
divContent.style.overflow="auto";
divContent.style.background="white";
divInner.appendChild(divContent);
bubble.content=divContent;

(function(){
if(pageX-Timeline.Graphics._halfArrowWidth-Timeline.Graphics._bubblePadding>0&&
pageX+Timeline.Graphics._halfArrowWidth+Timeline.Graphics._bubblePadding<docWidth){

var left=pageX-Math.round(contentWidth/2)-margins.left;
left=pageX<(docWidth/2)?
Math.max(left,-(margins.left-Timeline.Graphics._bubblePadding)):
Math.min(left,docWidth+(margins.right-Timeline.Graphics._bubblePadding)-bubbleWidth);

if(pageY-Timeline.Graphics._bubblePointOffset-bubbleHeight>0){
var divImg=doc.createElement("div");

divImg.style.left=(pageX-Timeline.Graphics._halfArrowWidth-left)+"px";
divImg.style.top=(margins.top+contentHeight)+"px";
setImg(divImg,urlPrefix+"images/bubble-bottom-arrow.png",37,margins.bottom);
divInner.appendChild(divImg);

div.style.left=left+"px";
div.style.top=(pageY-Timeline.Graphics._bubblePointOffset-bubbleHeight+
Timeline.Graphics._arrowOffsets.bottom)+"px";

return;
}else if(pageY+Timeline.Graphics._bubblePointOffset+bubbleHeight<docHeight){
var divImg=doc.createElement("div");

divImg.style.left=(pageX-Timeline.Graphics._halfArrowWidth-left)+"px";
divImg.style.top="0px";
setImg(divImg,urlPrefix+"images/bubble-top-arrow.png",37,margins.top);
divInner.appendChild(divImg);

div.style.left=left+"px";
div.style.top=(pageY+Timeline.Graphics._bubblePointOffset-
Timeline.Graphics._arrowOffsets.top)+"px";

return;
}
}

var top=pageY-Math.round(contentHeight/2)-margins.top;
top=pageY<(docHeight/2)?
Math.max(top,-(margins.top-Timeline.Graphics._bubblePadding)):
Math.min(top,docHeight+(margins.bottom-Timeline.Graphics._bubblePadding)-bubbleHeight);

if(pageX-Timeline.Graphics._bubblePointOffset-bubbleWidth>0){
var divImg=doc.createElement("div");

divImg.style.left=(margins.left+contentWidth)+"px";
divImg.style.top=(pageY-Timeline.Graphics._halfArrowWidth-top)+"px";
setImg(divImg,urlPrefix+"images/bubble-right-arrow.png",margins.right,37);
divInner.appendChild(divImg);

div.style.left=(pageX-Timeline.Graphics._bubblePointOffset-bubbleWidth+
Timeline.Graphics._arrowOffsets.right)+"px";
div.style.top=top+"px";
}else{
var divImg=doc.createElement("div");

divImg.style.left="0px";
divImg.style.top=(pageY-Timeline.Graphics._halfArrowWidth-top)+"px";
setImg(divImg,urlPrefix+"images/bubble-left-arrow.png",margins.left,37);
divInner.appendChild(divImg);

div.style.left=(pageX+Timeline.Graphics._bubblePointOffset-
Timeline.Graphics._arrowOffsets.left)+"px";
div.style.top=top+"px";
}
})();

doc.body.appendChild(div);

return bubble;
};

Timeline.Graphics.createMessageBubble=function(doc){
var containerDiv=doc.createElement("div");
if(Timeline.Graphics.pngIsTranslucent){
var topDiv=doc.createElement("div");
topDiv.style.height="33px";
topDiv.style.background="url("+Timeline.urlPrefix+"images/message-top-left.png) top left no-repeat";
topDiv.style.paddingLeft="44px";
containerDiv.appendChild(topDiv);

var topRightDiv=doc.createElement("div");
topRightDiv.style.height="33px";
topRightDiv.style.background="url("+Timeline.urlPrefix+"images/message-top-right.png) top right no-repeat";
topDiv.appendChild(topRightDiv);

var middleDiv=doc.createElement("div");
middleDiv.style.background="url("+Timeline.urlPrefix+"images/message-left.png) top left repeat-y";
middleDiv.style.paddingLeft="44px";
containerDiv.appendChild(middleDiv);

var middleRightDiv=doc.createElement("div");
middleRightDiv.style.background="url("+Timeline.urlPrefix+"images/message-right.png) top right repeat-y";
middleRightDiv.style.paddingRight="44px";
middleDiv.appendChild(middleRightDiv);

var contentDiv=doc.createElement("div");
middleRightDiv.appendChild(contentDiv);

var bottomDiv=doc.createElement("div");
bottomDiv.style.height="55px";
bottomDiv.style.background="url("+Timeline.urlPrefix+"images/message-bottom-left.png) bottom left no-repeat";
bottomDiv.style.paddingLeft="44px";
containerDiv.appendChild(bottomDiv);

var bottomRightDiv=doc.createElement("div");
bottomRightDiv.style.height="55px";
bottomRightDiv.style.background="url("+Timeline.urlPrefix+"images/message-bottom-right.png) bottom right no-repeat";
bottomDiv.appendChild(bottomRightDiv);
}else{
containerDiv.style.border="2px solid #7777AA";
containerDiv.style.padding="20px";
containerDiv.style.background="white";
Timeline.Graphics.setOpacity(containerDiv,90);

var contentDiv=doc.createElement("div");
containerDiv.appendChild(contentDiv);
}

return{
containerDiv:containerDiv,
contentDiv:contentDiv
};
};

Timeline.Graphics.createAnimation=function(f,from,to,duration){
return new Timeline.Graphics._Animation(f,from,to,duration);
};

Timeline.Graphics._Animation=function(f,from,to,duration){
this.f=f;

this.from=from;
this.to=to;
this.current=from;

this.duration=duration;
this.start=new Date().getTime();
this.timePassed=0;
};

Timeline.Graphics._Animation.prototype.run=function(){
var a=this;
window.setTimeout(function(){a.step();},100);
};

Timeline.Graphics._Animation.prototype.step=function(){
this.timePassed+=100;

var timePassedFraction=this.timePassed/this.duration;
var parameterFraction=-Math.cos(timePassedFraction*Math.PI)/2+0.5;
var current=parameterFraction*(this.to-this.from)+this.from;

try{
this.f(current,current-this.current);
}catch(e){
}
this.current=current;

if(this.timePassed<this.duration){
this.run();
}
};


/* html.js */



Timeline.HTML=new Object();

Timeline.HTML._e2uHash={};
(function(){
e2uHash=Timeline.HTML._e2uHash;
e2uHash['nbsp']='\u00A0[space]';
e2uHash['iexcl']='\u00A1';
e2uHash['cent']='\u00A2';
e2uHash['pound']='\u00A3';
e2uHash['curren']='\u00A4';
e2uHash['yen']='\u00A5';
e2uHash['brvbar']='\u00A6';
e2uHash['sect']='\u00A7';
e2uHash['uml']='\u00A8';
e2uHash['copy']='\u00A9';
e2uHash['ordf']='\u00AA';
e2uHash['laquo']='\u00AB';
e2uHash['not']='\u00AC';
e2uHash['shy']='\u00AD';
e2uHash['reg']='\u00AE';
e2uHash['macr']='\u00AF';
e2uHash['deg']='\u00B0';
e2uHash['plusmn']='\u00B1';
e2uHash['sup2']='\u00B2';
e2uHash['sup3']='\u00B3';
e2uHash['acute']='\u00B4';
e2uHash['micro']='\u00B5';
e2uHash['para']='\u00B6';
e2uHash['middot']='\u00B7';
e2uHash['cedil']='\u00B8';
e2uHash['sup1']='\u00B9';
e2uHash['ordm']='\u00BA';
e2uHash['raquo']='\u00BB';
e2uHash['frac14']='\u00BC';
e2uHash['frac12']='\u00BD';
e2uHash['frac34']='\u00BE';
e2uHash['iquest']='\u00BF';
e2uHash['Agrave']='\u00C0';
e2uHash['Aacute']='\u00C1';
e2uHash['Acirc']='\u00C2';
e2uHash['Atilde']='\u00C3';
e2uHash['Auml']='\u00C4';
e2uHash['Aring']='\u00C5';
e2uHash['AElig']='\u00C6';
e2uHash['Ccedil']='\u00C7';
e2uHash['Egrave']='\u00C8';
e2uHash['Eacute']='\u00C9';
e2uHash['Ecirc']='\u00CA';
e2uHash['Euml']='\u00CB';
e2uHash['Igrave']='\u00CC';
e2uHash['Iacute']='\u00CD';
e2uHash['Icirc']='\u00CE';
e2uHash['Iuml']='\u00CF';
e2uHash['ETH']='\u00D0';
e2uHash['Ntilde']='\u00D1';
e2uHash['Ograve']='\u00D2';
e2uHash['Oacute']='\u00D3';
e2uHash['Ocirc']='\u00D4';
e2uHash['Otilde']='\u00D5';
e2uHash['Ouml']='\u00D6';
e2uHash['times']='\u00D7';
e2uHash['Oslash']='\u00D8';
e2uHash['Ugrave']='\u00D9';
e2uHash['Uacute']='\u00DA';
e2uHash['Ucirc']='\u00DB';
e2uHash['Uuml']='\u00DC';
e2uHash['Yacute']='\u00DD';
e2uHash['THORN']='\u00DE';
e2uHash['szlig']='\u00DF';
e2uHash['agrave']='\u00E0';
e2uHash['aacute']='\u00E1';
e2uHash['acirc']='\u00E2';
e2uHash['atilde']='\u00E3';
e2uHash['auml']='\u00E4';
e2uHash['aring']='\u00E5';
e2uHash['aelig']='\u00E6';
e2uHash['ccedil']='\u00E7';
e2uHash['egrave']='\u00E8';
e2uHash['eacute']='\u00E9';
e2uHash['ecirc']='\u00EA';
e2uHash['euml']='\u00EB';
e2uHash['igrave']='\u00EC';
e2uHash['iacute']='\u00ED';
e2uHash['icirc']='\u00EE';
e2uHash['iuml']='\u00EF';
e2uHash['eth']='\u00F0';
e2uHash['ntilde']='\u00F1';
e2uHash['ograve']='\u00F2';
e2uHash['oacute']='\u00F3';
e2uHash['ocirc']='\u00F4';
e2uHash['otilde']='\u00F5';
e2uHash['ouml']='\u00F6';
e2uHash['divide']='\u00F7';
e2uHash['oslash']='\u00F8';
e2uHash['ugrave']='\u00F9';
e2uHash['uacute']='\u00FA';
e2uHash['ucirc']='\u00FB';
e2uHash['uuml']='\u00FC';
e2uHash['yacute']='\u00FD';
e2uHash['thorn']='\u00FE';
e2uHash['yuml']='\u00FF';
e2uHash['quot']='\u0022';
e2uHash['amp']='\u0026';
e2uHash['lt']='\u003C';
e2uHash['gt']='\u003E';
e2uHash['OElig']='';
e2uHash['oelig']='\u0153';
e2uHash['Scaron']='\u0160';
e2uHash['scaron']='\u0161';
e2uHash['Yuml']='\u0178';
e2uHash['circ']='\u02C6';
e2uHash['tilde']='\u02DC';
e2uHash['ensp']='\u2002';
e2uHash['emsp']='\u2003';
e2uHash['thinsp']='\u2009';
e2uHash['zwnj']='\u200C';
e2uHash['zwj']='\u200D';
e2uHash['lrm']='\u200E';
e2uHash['rlm']='\u200F';
e2uHash['ndash']='\u2013';
e2uHash['mdash']='\u2014';
e2uHash['lsquo']='\u2018';
e2uHash['rsquo']='\u2019';
e2uHash['sbquo']='\u201A';
e2uHash['ldquo']='\u201C';
e2uHash['rdquo']='\u201D';
e2uHash['bdquo']='\u201E';
e2uHash['dagger']='\u2020';
e2uHash['Dagger']='\u2021';
e2uHash['permil']='\u2030';
e2uHash['lsaquo']='\u2039';
e2uHash['rsaquo']='\u203A';
e2uHash['euro']='\u20AC';
e2uHash['fnof']='\u0192';
e2uHash['Alpha']='\u0391';
e2uHash['Beta']='\u0392';
e2uHash['Gamma']='\u0393';
e2uHash['Delta']='\u0394';
e2uHash['Epsilon']='\u0395';
e2uHash['Zeta']='\u0396';
e2uHash['Eta']='\u0397';
e2uHash['Theta']='\u0398';
e2uHash['Iota']='\u0399';
e2uHash['Kappa']='\u039A';
e2uHash['Lambda']='\u039B';
e2uHash['Mu']='\u039C';
e2uHash['Nu']='\u039D';
e2uHash['Xi']='\u039E';
e2uHash['Omicron']='\u039F';
e2uHash['Pi']='\u03A0';
e2uHash['Rho']='\u03A1';
e2uHash['Sigma']='\u03A3';
e2uHash['Tau']='\u03A4';
e2uHash['Upsilon']='\u03A5';
e2uHash['Phi']='\u03A6';
e2uHash['Chi']='\u03A7';
e2uHash['Psi']='\u03A8';
e2uHash['Omega']='\u03A9';
e2uHash['alpha']='\u03B1';
e2uHash['beta']='\u03B2';
e2uHash['gamma']='\u03B3';
e2uHash['delta']='\u03B4';
e2uHash['epsilon']='\u03B5';
e2uHash['zeta']='\u03B6';
e2uHash['eta']='\u03B7';
e2uHash['theta']='\u03B8';
e2uHash['iota']='\u03B9';
e2uHash['kappa']='\u03BA';
e2uHash['lambda']='\u03BB';
e2uHash['mu']='\u03BC';
e2uHash['nu']='\u03BD';
e2uHash['xi']='\u03BE';
e2uHash['omicron']='\u03BF';
e2uHash['pi']='\u03C0';
e2uHash['rho']='\u03C1';
e2uHash['sigmaf']='\u03C2';
e2uHash['sigma']='\u03C3';
e2uHash['tau']='\u03C4';
e2uHash['upsilon']='\u03C5';
e2uHash['phi']='\u03C6';
e2uHash['chi']='\u03C7';
e2uHash['psi']='\u03C8';
e2uHash['omega']='\u03C9';
e2uHash['thetasym']='\u03D1';
e2uHash['upsih']='\u03D2';
e2uHash['piv']='\u03D6';
e2uHash['bull']='\u2022';
e2uHash['hellip']='\u2026';
e2uHash['prime']='\u2032';
e2uHash['Prime']='\u2033';
e2uHash['oline']='\u203E';
e2uHash['frasl']='\u2044';
e2uHash['weierp']='\u2118';
e2uHash['image']='\u2111';
e2uHash['real']='\u211C';
e2uHash['trade']='\u2122';
e2uHash['alefsym']='\u2135';
e2uHash['larr']='\u2190';
e2uHash['uarr']='\u2191';
e2uHash['rarr']='\u2192';
e2uHash['darr']='\u2193';
e2uHash['harr']='\u2194';
e2uHash['crarr']='\u21B5';
e2uHash['lArr']='\u21D0';
e2uHash['uArr']='\u21D1';
e2uHash['rArr']='\u21D2';
e2uHash['dArr']='\u21D3';
e2uHash['hArr']='\u21D4';
e2uHash['forall']='\u2200';
e2uHash['part']='\u2202';
e2uHash['exist']='\u2203';
e2uHash['empty']='\u2205';
e2uHash['nabla']='\u2207';
e2uHash['isin']='\u2208';
e2uHash['notin']='\u2209';
e2uHash['ni']='\u220B';
e2uHash['prod']='\u220F';
e2uHash['sum']='\u2211';
e2uHash['minus']='\u2212';
e2uHash['lowast']='\u2217';
e2uHash['radic']='\u221A';
e2uHash['prop']='\u221D';
e2uHash['infin']='\u221E';
e2uHash['ang']='\u2220';
e2uHash['and']='\u2227';
e2uHash['or']='\u2228';
e2uHash['cap']='\u2229';
e2uHash['cup']='\u222A';
e2uHash['int']='\u222B';
e2uHash['there4']='\u2234';
e2uHash['sim']='\u223C';
e2uHash['cong']='\u2245';
e2uHash['asymp']='\u2248';
e2uHash['ne']='\u2260';
e2uHash['equiv']='\u2261';
e2uHash['le']='\u2264';
e2uHash['ge']='\u2265';
e2uHash['sub']='\u2282';
e2uHash['sup']='\u2283';
e2uHash['nsub']='\u2284';
e2uHash['sube']='\u2286';
e2uHash['supe']='\u2287';
e2uHash['oplus']='\u2295';
e2uHash['otimes']='\u2297';
e2uHash['perp']='\u22A5';
e2uHash['sdot']='\u22C5';
e2uHash['lceil']='\u2308';
e2uHash['rceil']='\u2309';
e2uHash['lfloor']='\u230A';
e2uHash['rfloor']='\u230B';
e2uHash['lang']='\u2329';
e2uHash['rang']='\u232A';
e2uHash['loz']='\u25CA';
e2uHash['spades']='\u2660';
e2uHash['clubs']='\u2663';
e2uHash['hearts']='\u2665';
e2uHash['diams']='\u2666';
})();

Timeline.HTML.deEntify=function(s){
e2uHash=Timeline.HTML._e2uHash;

var re=/&(\w+?);/;
while(re.test(s)){
var m=s.match(re);
s=s.replace(re,e2uHash[m[1]]);
}
return s;
};

/* xmlhttp.js */



Timeline.XmlHttp=new Object();


Timeline.XmlHttp._onReadyStateChange=function(xmlhttp,fError,fDone){
switch(xmlhttp.readyState){









case 4:

try{
if(xmlhttp.status==0
||xmlhttp.status==200
){
if(fDone){
fDone(xmlhttp);
}

}else{
if(fError){
fError(
xmlhttp.statusText,

xmlhttp.status,

xmlhttp

);
}

}

}catch(e){

Timeline.Debug.exception(e);

}

break;
}
};


Timeline.XmlHttp._createRequest=function(){
if(Timeline.Platform.browser.isIE){
var programIDs=[
"Msxml2.XMLHTTP",
"Microsoft.XMLHTTP",
"Msxml2.XMLHTTP.4.0"
];
for(var i=0;i<programIDs.length;i++){
try{
var programID=programIDs[i];
var f=function(){
return new ActiveXObject(programID);
};
var o=f();






Timeline.XmlHttp._createRequest=f;

return o;
}catch(e){

}
}
throw new Error("Failed to create an XMLHttpRequest object");
}else{
try{
var f=function(){
return new XMLHttpRequest();
};
var o=f();






Timeline.XmlHttp._createRequest=f;

return o;
}catch(e){
throw new Error("Failed to create an XMLHttpRequest object");
}
}
};


Timeline.XmlHttp.get=function(url,fError,fDone){
var xmlhttp=Timeline.XmlHttp._createRequest();

xmlhttp.open("GET",url,true);
xmlhttp.onreadystatechange=function(){
Timeline.XmlHttp._onReadyStateChange(xmlhttp,fError,fDone);
};
xmlhttp.send(null);
};


Timeline.XmlHttp.post=function(url,body,fError,fDone){
var xmlhttp=Timeline.XmlHttp._createRequest();

xmlhttp.open("POST",url,true);
xmlhttp.onreadystatechange=function(){
Timeline.XmlHttp._onReadyStateChange(xmlhttp,fError,fDone);
};
xmlhttp.send(body);
};

Timeline.XmlHttp._forceXML=function(xmlhttp){
try{
xmlhttp.overrideMimeType("text/xml");
}catch(e){
xmlhttp.setrequestheader("Content-Type","text/xml");
}
};

/* decorators.js */



Timeline.SpanHighlightDecorator=function(params){
this._unit=("unit"in params)?params.unit:Timeline.NativeDateUnit;
this._startDate=(typeof params.startDate=="string")?
this._unit.parseFromObject(params.startDate):params.startDate;
this._endDate=(typeof params.endDate=="string")?
this._unit.parseFromObject(params.endDate):params.endDate;
this._startLabel=params.startLabel;
this._endLabel=params.endLabel;
this._color=params.color;
this._opacity=("opacity"in params)?params.opacity:100;
};

Timeline.SpanHighlightDecorator.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;

this._layerDiv=null;
};

Timeline.SpanHighlightDecorator.prototype.paint=function(){
if(this._layerDiv!=null){
this._band.removeLayerDiv(this._layerDiv);
}
this._layerDiv=this._band.createLayerDiv(10);
this._layerDiv.setAttribute("name","span-highlight-decorator");
this._layerDiv.style.display="none";

var minDate=this._band.getMinDate();
var maxDate=this._band.getMaxDate();

if(this._unit.compare(this._startDate,maxDate)<0&&
this._unit.compare(this._endDate,minDate)>0){

minDate=this._unit.later(minDate,this._startDate);
maxDate=this._unit.earlier(maxDate,this._endDate);

var minPixel=this._band.dateToPixelOffset(minDate);
var maxPixel=this._band.dateToPixelOffset(maxDate);

var doc=this._timeline.getDocument();

var createTable=function(){
var table=doc.createElement("table");
table.insertRow(0).insertCell(0);
return table;
};

var div=doc.createElement("div");
div.style.position="absolute";
div.style.overflow="hidden";
div.style.background=this._color;
if(this._opacity<100){
Timeline.Graphics.setOpacity(div,this._opacity);
}
this._layerDiv.appendChild(div);

var tableStartLabel=createTable();
tableStartLabel.style.position="absolute";
tableStartLabel.style.overflow="hidden";
tableStartLabel.style.fontSize="300%";
tableStartLabel.style.fontWeight="bold";
tableStartLabel.style.color=this._color;
tableStartLabel.rows[0].cells[0].innerHTML=this._startLabel;
this._layerDiv.appendChild(tableStartLabel);

var tableEndLabel=createTable();
tableEndLabel.style.position="absolute";
tableEndLabel.style.overflow="hidden";
tableEndLabel.style.fontSize="300%";
tableEndLabel.style.fontWeight="bold";
tableEndLabel.style.color=this._color;
tableEndLabel.rows[0].cells[0].innerHTML=this._endLabel;
this._layerDiv.appendChild(tableEndLabel);

if(this._timeline.isHorizontal()){
div.style.left=minPixel+"px";
div.style.width=(maxPixel-minPixel)+"px";
div.style.top="0px";
div.style.height="100%";

tableStartLabel.style.right=(this._band.getTotalViewLength()-minPixel)+"px";
tableStartLabel.style.width=(this._startLabel.length)+"em";
tableStartLabel.style.top="0px";
tableStartLabel.style.height="100%";
tableStartLabel.style.textAlign="right";

tableEndLabel.style.left=maxPixel+"px";
tableEndLabel.style.width=(this._endLabel.length)+"em";
tableEndLabel.style.top="0px";
tableEndLabel.style.height="100%";
}else{
div.style.top=minPixel+"px";
div.style.height=(maxPixel-minPixel)+"px";
div.style.left="0px";
div.style.width="100%";

tableStartLabel.style.bottom=minPixel+"px";
tableStartLabel.style.height="1.5px";
tableStartLabel.style.left="0px";
tableStartLabel.style.width="100%";

tableEndLabel.style.top=maxPixel+"px";
tableEndLabel.style.height="1.5px";
tableEndLabel.style.left="0px";
tableEndLabel.style.width="100%";
}
}
this._layerDiv.style.display="block";
};

Timeline.SpanHighlightDecorator.prototype.softPaint=function(){
};



Timeline.PointHighlightDecorator=function(params){
this._unit=("unit"in params)?params.unit:Timeline.NativeDateUnit;
this._date=(typeof params.date=="string")?
this._unit.parseFromObject(params.date):params.date;
this._width=("width"in params)?params.width:10;
this._color=params.color;
this._opacity=("opacity"in params)?params.opacity:100;
};

Timeline.PointHighlightDecorator.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;

this._layerDiv=null;
};

Timeline.PointHighlightDecorator.prototype.paint=function(){
if(this._layerDiv!=null){
this._band.removeLayerDiv(this._layerDiv);
}
this._layerDiv=this._band.createLayerDiv(10);
this._layerDiv.setAttribute("name","span-highlight-decorator");
this._layerDiv.style.display="none";

var minDate=this._band.getMinDate();
var maxDate=this._band.getMaxDate();

if(this._unit.compare(this._date,maxDate)<0&&
this._unit.compare(this._date,minDate)>0){

var pixel=this._band.dateToPixelOffset(this._date);
var minPixel=pixel-Math.round(this._width/2);

var doc=this._timeline.getDocument();

var div=doc.createElement("div");
div.style.position="absolute";
div.style.overflow="hidden";
div.style.background=this._color;
if(this._opacity<100){
Timeline.Graphics.setOpacity(div,this._opacity);
}
this._layerDiv.appendChild(div);

if(this._timeline.isHorizontal()){
div.style.left=minPixel+"px";
div.style.width=this._width+"px";
div.style.top="0px";
div.style.height="100%";
}else{
div.style.top=minPixel+"px";
div.style.height=this._width+"px";
div.style.left="0px";
div.style.width="100%";
}
}
this._layerDiv.style.display="block";
};

Timeline.PointHighlightDecorator.prototype.softPaint=function(){
};


/* ether-painters.js */



Timeline.GregorianEtherPainter=function(params){
this._params=params;
this._theme=params.theme;
this._unit=params.unit;
this._multiple=("multiple"in params)?params.multiple:1;
};

Timeline.GregorianEtherPainter.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;

this._backgroundLayer=band.createLayerDiv(0);
this._backgroundLayer.setAttribute("name","ether-background");
this._backgroundLayer.style.background=this._theme.ether.backgroundColors[band.getIndex()];

this._markerLayer=null;
this._lineLayer=null;

var align=("align"in this._params&&this._params.align!=undefined)?this._params.align:
this._theme.ether.interval.marker[timeline.isHorizontal()?"hAlign":"vAlign"];
var showLine=("showLine"in this._params)?this._params.showLine:
this._theme.ether.interval.line.show;

this._intervalMarkerLayout=new Timeline.EtherIntervalMarkerLayout(
this._timeline,this._band,this._theme,align,showLine);

this._highlight=new Timeline.EtherHighlight(
this._timeline,this._band,this._theme,this._backgroundLayer);
}

Timeline.GregorianEtherPainter.prototype.setHighlight=function(startDate,endDate){
this._highlight.position(startDate,endDate);
}

Timeline.GregorianEtherPainter.prototype.paint=function(){
if(this._markerLayer){
this._band.removeLayerDiv(this._markerLayer);
}
this._markerLayer=this._band.createLayerDiv(100);
this._markerLayer.setAttribute("name","ether-markers");
this._markerLayer.style.display="none";

if(this._lineLayer){
this._band.removeLayerDiv(this._lineLayer);
}
this._lineLayer=this._band.createLayerDiv(1);
this._lineLayer.setAttribute("name","ether-lines");
this._lineLayer.style.display="none";

var minDate=this._band.getMinDate();
var maxDate=this._band.getMaxDate();

var timeZone=this._band.getTimeZone();
var labeller=this._band.getLabeller();

Timeline.DateTime.roundDownToInterval(minDate,this._unit,timeZone,this._multiple,this._theme.firstDayOfWeek);

var p=this;
var incrementDate=function(date){
for(var i=0;i<p._multiple;i++){
Timeline.DateTime.incrementByInterval(date,p._unit);
}
};

while(minDate.getTime()<maxDate.getTime()){
this._intervalMarkerLayout.createIntervalMarker(
minDate,labeller,this._unit,this._markerLayer,this._lineLayer);

incrementDate(minDate);
}
this._markerLayer.style.display="block";
this._lineLayer.style.display="block";
};

Timeline.GregorianEtherPainter.prototype.softPaint=function(){
};



Timeline.HotZoneGregorianEtherPainter=function(params){
this._params=params;
this._theme=params.theme;

this._zones=[{
startTime:Number.NEGATIVE_INFINITY,
endTime:Number.POSITIVE_INFINITY,
unit:params.unit,
multiple:1
}];
for(var i=0;i<params.zones.length;i++){
var zone=params.zones[i];
var zoneStart=Timeline.DateTime.parseGregorianDateTime(zone.start).getTime();
var zoneEnd=Timeline.DateTime.parseGregorianDateTime(zone.end).getTime();

for(var j=0;j<this._zones.length&&zoneEnd>zoneStart;j++){
var zone2=this._zones[j];

if(zoneStart<zone2.endTime){
if(zoneStart>zone2.startTime){
this._zones.splice(j,0,{
startTime:zone2.startTime,
endTime:zoneStart,
unit:zone2.unit,
multiple:zone2.multiple
});
j++;

zone2.startTime=zoneStart;
}

if(zoneEnd<zone2.endTime){
this._zones.splice(j,0,{
startTime:zoneStart,
endTime:zoneEnd,
unit:zone.unit,
multiple:(zone.multiple)?zone.multiple:1
});
j++;

zone2.startTime=zoneEnd;
zoneStart=zoneEnd;
}else{
zone2.multiple=zone.multiple;
zone2.unit=zone.unit;
zoneStart=zone2.endTime;
}
}
}
}
};

Timeline.HotZoneGregorianEtherPainter.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;

this._backgroundLayer=band.createLayerDiv(0);
this._backgroundLayer.setAttribute("name","ether-background");
this._backgroundLayer.style.background=this._theme.ether.backgroundColors[band.getIndex()];

this._markerLayer=null;
this._lineLayer=null;

var align=("align"in this._params&&this._params.align!=undefined)?this._params.align:
this._theme.ether.interval.marker[timeline.isHorizontal()?"hAlign":"vAlign"];
var showLine=("showLine"in this._params)?this._params.showLine:
this._theme.ether.interval.line.show;

this._intervalMarkerLayout=new Timeline.EtherIntervalMarkerLayout(
this._timeline,this._band,this._theme,align,showLine);

this._highlight=new Timeline.EtherHighlight(
this._timeline,this._band,this._theme,this._backgroundLayer);
}

Timeline.HotZoneGregorianEtherPainter.prototype.setHighlight=function(startDate,endDate){
this._highlight.position(startDate,endDate);
}

Timeline.HotZoneGregorianEtherPainter.prototype.paint=function(){
if(this._markerLayer){
this._band.removeLayerDiv(this._markerLayer);
}
this._markerLayer=this._band.createLayerDiv(100);
this._markerLayer.setAttribute("name","ether-markers");
this._markerLayer.style.display="none";

if(this._lineLayer){
this._band.removeLayerDiv(this._lineLayer);
}
this._lineLayer=this._band.createLayerDiv(1);
this._lineLayer.setAttribute("name","ether-lines");
this._lineLayer.style.display="none";

var minDate=this._band.getMinDate();
var maxDate=this._band.getMaxDate();

var timeZone=this._band.getTimeZone();
var labeller=this._band.getLabeller();

var p=this;
var incrementDate=function(date,zone){
for(var i=0;i<zone.multiple;i++){
Timeline.DateTime.incrementByInterval(date,zone.unit);
}
};

var zStart=0;
while(zStart<this._zones.length){
if(minDate.getTime()<this._zones[zStart].endTime){
break;
}
zStart++;
}
var zEnd=this._zones.length-1;
while(zEnd>=0){
if(maxDate.getTime()>this._zones[zEnd].startTime){
break;
}
zEnd--;
}

for(var z=zStart;z<=zEnd;z++){
var zone=this._zones[z];

var minDate2=new Date(Math.max(minDate.getTime(),zone.startTime));
var maxDate2=new Date(Math.min(maxDate.getTime(),zone.endTime));

Timeline.DateTime.roundDownToInterval(minDate2,zone.unit,timeZone,zone.multiple,this._theme.firstDayOfWeek);
Timeline.DateTime.roundUpToInterval(maxDate2,zone.unit,timeZone,zone.multiple,this._theme.firstDayOfWeek);

while(minDate2.getTime()<maxDate2.getTime()){
this._intervalMarkerLayout.createIntervalMarker(
minDate2,labeller,zone.unit,this._markerLayer,this._lineLayer);

incrementDate(minDate2,zone);
}
}
this._markerLayer.style.display="block";
this._lineLayer.style.display="block";
};

Timeline.HotZoneGregorianEtherPainter.prototype.softPaint=function(){
};



Timeline.YearCountEtherPainter=function(params){
this._params=params;
this._theme=params.theme;
this._startDate=Timeline.DateTime.parseGregorianDateTime(params.startDate);
this._multiple=("multiple"in params)?params.multiple:1;
};

Timeline.YearCountEtherPainter.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;

this._backgroundLayer=band.createLayerDiv(0);
this._backgroundLayer.setAttribute("name","ether-background");
this._backgroundLayer.style.background=this._theme.ether.backgroundColors[band.getIndex()];

this._markerLayer=null;
this._lineLayer=null;

var align=("align"in this._params)?this._params.align:
this._theme.ether.interval.marker[timeline.isHorizontal()?"hAlign":"vAlign"];
var showLine=("showLine"in this._params)?this._params.showLine:
this._theme.ether.interval.line.show;

this._intervalMarkerLayout=new Timeline.EtherIntervalMarkerLayout(
this._timeline,this._band,this._theme,align,showLine);

this._highlight=new Timeline.EtherHighlight(
this._timeline,this._band,this._theme,this._backgroundLayer);
};

Timeline.YearCountEtherPainter.prototype.setHighlight=function(startDate,endDate){
this._highlight.position(startDate,endDate);
};

Timeline.YearCountEtherPainter.prototype.paint=function(){
if(this._markerLayer){
this._band.removeLayerDiv(this._markerLayer);
}
this._markerLayer=this._band.createLayerDiv(100);
this._markerLayer.setAttribute("name","ether-markers");
this._markerLayer.style.display="none";

if(this._lineLayer){
this._band.removeLayerDiv(this._lineLayer);
}
this._lineLayer=this._band.createLayerDiv(1);
this._lineLayer.setAttribute("name","ether-lines");
this._lineLayer.style.display="none";

var minDate=new Date(this._startDate.getTime());
var maxDate=this._band.getMaxDate();
var yearDiff=this._band.getMinDate().getUTCFullYear()-this._startDate.getUTCFullYear();
minDate.setUTCFullYear(this._band.getMinDate().getUTCFullYear()-yearDiff%this._multiple);

var p=this;
var incrementDate=function(date){
for(var i=0;i<p._multiple;i++){
Timeline.DateTime.incrementByInterval(date,Timeline.DateTime.YEAR);
}
};
var labeller={
labelInterval:function(date,intervalUnit){
var diff=date.getUTCFullYear()-p._startDate.getUTCFullYear();
return{
text:diff,
emphasized:diff==0
};
}
};

while(minDate.getTime()<maxDate.getTime()){
this._intervalMarkerLayout.createIntervalMarker(
minDate,labeller,Timeline.DateTime.YEAR,this._markerLayer,this._lineLayer);

incrementDate(minDate);
}
this._markerLayer.style.display="block";
this._lineLayer.style.display="block";
};

Timeline.YearCountEtherPainter.prototype.softPaint=function(){
};



Timeline.QuarterlyEtherPainter=function(params){
this._params=params;
this._theme=params.theme;
this._startDate=Timeline.DateTime.parseGregorianDateTime(params.startDate);
};

Timeline.QuarterlyEtherPainter.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;

this._backgroundLayer=band.createLayerDiv(0);
this._backgroundLayer.setAttribute("name","ether-background");
this._backgroundLayer.style.background=this._theme.ether.backgroundColors[band.getIndex()];

this._markerLayer=null;
this._lineLayer=null;

var align=("align"in this._params)?this._params.align:
this._theme.ether.interval.marker[timeline.isHorizontal()?"hAlign":"vAlign"];
var showLine=("showLine"in this._params)?this._params.showLine:
this._theme.ether.interval.line.show;

this._intervalMarkerLayout=new Timeline.EtherIntervalMarkerLayout(
this._timeline,this._band,this._theme,align,showLine);

this._highlight=new Timeline.EtherHighlight(
this._timeline,this._band,this._theme,this._backgroundLayer);
};

Timeline.QuarterlyEtherPainter.prototype.setHighlight=function(startDate,endDate){
this._highlight.position(startDate,endDate);
};

Timeline.QuarterlyEtherPainter.prototype.paint=function(){
if(this._markerLayer){
this._band.removeLayerDiv(this._markerLayer);
}
this._markerLayer=this._band.createLayerDiv(100);
this._markerLayer.setAttribute("name","ether-markers");
this._markerLayer.style.display="none";

if(this._lineLayer){
this._band.removeLayerDiv(this._lineLayer);
}
this._lineLayer=this._band.createLayerDiv(1);
this._lineLayer.setAttribute("name","ether-lines");
this._lineLayer.style.display="none";

var minDate=new Date(0);
var maxDate=this._band.getMaxDate();

minDate.setUTCFullYear(Math.max(this._startDate.getUTCFullYear(),this._band.getMinDate().getUTCFullYear()));
minDate.setUTCMonth(this._startDate.getUTCMonth());

var p=this;
var incrementDate=function(date){
date.setUTCMonth(date.getUTCMonth()+3);
};
var labeller={
labelInterval:function(date,intervalUnit){
var quarters=(4+(date.getUTCMonth()-p._startDate.getUTCMonth())/3)%4;
if(quarters!=0){
return{text:"Q"+(quarters+1),emphasized:false};
}else{
return{text:"Y"+(date.getUTCFullYear()-p._startDate.getUTCFullYear()+1),emphasized:true};
}
}
};

while(minDate.getTime()<maxDate.getTime()){
this._intervalMarkerLayout.createIntervalMarker(
minDate,labeller,Timeline.DateTime.YEAR,this._markerLayer,this._lineLayer);

incrementDate(minDate);
}
this._markerLayer.style.display="block";
this._lineLayer.style.display="block";
};

Timeline.QuarterlyEtherPainter.prototype.softPaint=function(){
};



Timeline.EtherIntervalMarkerLayout=function(timeline,band,theme,align,showLine){
var horizontal=timeline.isHorizontal();
if(horizontal){
if(align=="Top"){
this.positionDiv=function(div,offset){
div.style.left=offset+"px";
div.style.top="0px";
};
}else{
this.positionDiv=function(div,offset){
div.style.left=offset+"px";
div.style.bottom="0px";
};
}
}else{
if(align=="Left"){
this.positionDiv=function(div,offset){
div.style.top=offset+"px";
div.style.left="0px";
};
}else{
this.positionDiv=function(div,offset){
div.style.top=offset+"px";
div.style.right="0px";
};
}
}

var markerTheme=theme.ether.interval.marker;
var lineTheme=theme.ether.interval.line;
var weekendTheme=theme.ether.interval.weekend;

var stylePrefix=(horizontal?"h":"v")+align;
var labelStyler=markerTheme[stylePrefix+"Styler"];
var emphasizedLabelStyler=markerTheme[stylePrefix+"EmphasizedStyler"];
var day=Timeline.DateTime.gregorianUnitLengths[Timeline.DateTime.DAY];

this.createIntervalMarker=function(date,labeller,unit,markerDiv,lineDiv){
var offset=Math.round(band.dateToPixelOffset(date));

if(showLine&&unit!=Timeline.DateTime.WEEK){
var divLine=timeline.getDocument().createElement("div");
divLine.style.position="absolute";

if(lineTheme.opacity<100){
Timeline.Graphics.setOpacity(divLine,lineTheme.opacity);
}

if(horizontal){
divLine.style.borderLeft="1px solid "+lineTheme.color;
divLine.style.left=offset+"px";
divLine.style.width="1px";
divLine.style.top="0px";
divLine.style.height="100%";
}else{
divLine.style.borderTop="1px solid "+lineTheme.color;
divLine.style.top=offset+"px";
divLine.style.height="1px";
divLine.style.left="0px";
divLine.style.width="100%";
}
lineDiv.appendChild(divLine);
}
if(unit==Timeline.DateTime.WEEK){
var firstDayOfWeek=theme.firstDayOfWeek;

var saturday=new Date(date.getTime()+(6-firstDayOfWeek-7)*day);
var monday=new Date(saturday.getTime()+2*day);

var saturdayPixel=Math.round(band.dateToPixelOffset(saturday));
var mondayPixel=Math.round(band.dateToPixelOffset(monday));
var length=Math.max(1,mondayPixel-saturdayPixel);

var divWeekend=timeline.getDocument().createElement("div");
divWeekend.style.position="absolute";

divWeekend.style.background=weekendTheme.color;
if(weekendTheme.opacity<100){
Timeline.Graphics.setOpacity(divWeekend,weekendTheme.opacity);
}

if(horizontal){
divWeekend.style.left=saturdayPixel+"px";
divWeekend.style.width=length+"px";
divWeekend.style.top="0px";
divWeekend.style.height="100%";
}else{
divWeekend.style.top=saturdayPixel+"px";
divWeekend.style.height=length+"px";
divWeekend.style.left="0px";
divWeekend.style.width="100%";
}
lineDiv.appendChild(divWeekend);
}

var label=labeller.labelInterval(date,unit);

var div=timeline.getDocument().createElement("div");
div.innerHTML=label.text;
div.style.position="absolute";
(label.emphasized?emphasizedLabelStyler:labelStyler)(div);

this.positionDiv(div,offset);
markerDiv.appendChild(div);

return div;
};
};



Timeline.EtherHighlight=function(timeline,band,theme,backgroundLayer){
var horizontal=timeline.isHorizontal();

this._highlightDiv=null;
this._createHighlightDiv=function(){
if(this._highlightDiv==null){
this._highlightDiv=timeline.getDocument().createElement("div");
this._highlightDiv.setAttribute("name","ether-highlight");
this._highlightDiv.style.position="absolute";
this._highlightDiv.style.background=theme.ether.highlightColor;

var opacity=theme.ether.highlightOpacity;
if(opacity<100){
Timeline.Graphics.setOpacity(this._highlightDiv,opacity);
}

backgroundLayer.appendChild(this._highlightDiv);
}
}

this.position=function(startDate,endDate){
this._createHighlightDiv();

var startPixel=Math.round(band.dateToPixelOffset(startDate));
var endPixel=Math.round(band.dateToPixelOffset(endDate));
var length=Math.max(endPixel-startPixel,3);
if(horizontal){
this._highlightDiv.style.left=startPixel+"px";
this._highlightDiv.style.width=length+"px";
this._highlightDiv.style.top="2px";
this._highlightDiv.style.height=(band.getViewWidth()-4)+"px";
}else{
this._highlightDiv.style.top=startPixel+"px";
this._highlightDiv.style.height=length+"px";
this._highlightDiv.style.left="2px";
this._highlightDiv.style.width=(band.getViewWidth()-4)+"px";
}
}
};



/* ethers.js */



Timeline.LinearEther=function(params){
this._params=params;
this._interval=params.interval;
this._pixelsPerInterval=params.pixelsPerInterval;
};

Timeline.LinearEther.prototype.initialize=function(timeline){
this._timeline=timeline;
this._unit=timeline.getUnit();

if("startsOn"in this._params){
this._start=this._unit.parseFromObject(this._params.startsOn);
}else if("endsOn"in this._params){
this._start=this._unit.parseFromObject(this._params.endsOn);
this.shiftPixels(-this._timeline.getPixelLength());
}else if("centersOn"in this._params){
this._start=this._unit.parseFromObject(this._params.centersOn);
this.shiftPixels(-this._timeline.getPixelLength()/2);
}else{
this._start=this._unit.makeDefaultValue();
this.shiftPixels(-this._timeline.getPixelLength()/2);
}
};

Timeline.LinearEther.prototype.setDate=function(date){
this._start=this._unit.cloneValue(date);
};

Timeline.LinearEther.prototype.shiftPixels=function(pixels){
var numeric=this._interval*pixels/this._pixelsPerInterval;
this._start=this._unit.change(this._start,numeric);
};

Timeline.LinearEther.prototype.dateToPixelOffset=function(date){
var numeric=this._unit.compare(date,this._start);
return this._pixelsPerInterval*numeric/this._interval;
};

Timeline.LinearEther.prototype.pixelOffsetToDate=function(pixels){
var numeric=pixels*this._interval/this._pixelsPerInterval;
return this._unit.change(this._start,numeric);
};



Timeline.HotZoneEther=function(params){
this._params=params;
this._interval=params.interval;
this._pixelsPerInterval=params.pixelsPerInterval;
};

Timeline.HotZoneEther.prototype.initialize=function(timeline){
this._timeline=timeline;
this._unit=timeline.getUnit();

this._zones=[{
startTime:Number.NEGATIVE_INFINITY,
endTime:Number.POSITIVE_INFINITY,
magnify:1
}];
var params=this._params;
for(var i=0;i<params.zones.length;i++){
var zone=params.zones[i];
var zoneStart=this._unit.parseFromObject(zone.start);
var zoneEnd=this._unit.parseFromObject(zone.end);

for(var j=0;j<this._zones.length&&this._unit.compare(zoneEnd,zoneStart)>0;j++){
var zone2=this._zones[j];

if(this._unit.compare(zoneStart,zone2.endTime)<0){
if(this._unit.compare(zoneStart,zone2.startTime)>0){
this._zones.splice(j,0,{
startTime:zone2.startTime,
endTime:zoneStart,
magnify:zone2.magnify
});
j++;

zone2.startTime=zoneStart;
}

if(this._unit.compare(zoneEnd,zone2.endTime)<0){
this._zones.splice(j,0,{
startTime:zoneStart,
endTime:zoneEnd,
magnify:zone.magnify*zone2.magnify
});
j++;

zone2.startTime=zoneEnd;
zoneStart=zoneEnd;
}else{
zone2.magnify*=zone.magnify;
zoneStart=zone2.endTime;
}
}
}
}

if("startsOn"in this._params){
this._start=this._unit.parseFromObject(this._params.startsOn);
}else if("endsOn"in this._params){
this._start=this._unit.parseFromObject(this._params.endsOn);
this.shiftPixels(-this._timeline.getPixelLength());
}else if("centersOn"in this._params){
this._start=this._unit.parseFromObject(this._params.centersOn);
this.shiftPixels(-this._timeline.getPixelLength()/2);
}else{
this._start=this._unit.makeDefaultValue();
this.shiftPixels(-this._timeline.getPixelLength()/2);
}
};

Timeline.HotZoneEther.prototype.setDate=function(date){
this._start=this._unit.cloneValue(date);
};

Timeline.HotZoneEther.prototype.shiftPixels=function(pixels){
this._start=this.pixelOffsetToDate(pixels);
};

Timeline.HotZoneEther.prototype.dateToPixelOffset=function(date){
return this._dateDiffToPixelOffset(this._start,date);
};

Timeline.HotZoneEther.prototype.pixelOffsetToDate=function(pixels){
return this._pixelOffsetToDate(pixels,this._start);
};

Timeline.HotZoneEther.prototype._dateDiffToPixelOffset=function(fromDate,toDate){
var scale=this._getScale();
var fromTime=fromDate;
var toTime=toDate;

var pixels=0;
if(this._unit.compare(fromTime,toTime)<0){
var z=0;
while(z<this._zones.length){
if(this._unit.compare(fromTime,this._zones[z].endTime)<0){
break;
}
z++;
}

while(this._unit.compare(fromTime,toTime)<0){
var zone=this._zones[z];
var toTime2=this._unit.earlier(toTime,zone.endTime);

pixels+=(this._unit.compare(toTime2,fromTime)/(scale/zone.magnify));

fromTime=toTime2;
z++;
}
}else{
var z=this._zones.length-1;
while(z>=0){
if(this._unit.compare(fromTime,this._zones[z].startTime)>0){
break;
}
z--;
}

while(this._unit.compare(fromTime,toTime)>0){
var zone=this._zones[z];
var toTime2=this._unit.later(toTime,zone.startTime);

pixels+=(this._unit.compare(toTime2,fromTime)/(scale/zone.magnify));

fromTime=toTime2;
z--;
}
}
return pixels;
};

Timeline.HotZoneEther.prototype._pixelOffsetToDate=function(pixels,fromDate){
var scale=this._getScale();
var time=fromDate;
if(pixels>0){
var z=0;
while(z<this._zones.length){
if(this._unit.compare(time,this._zones[z].endTime)<0){
break;
}
z++;
}

while(pixels>0){
var zone=this._zones[z];
var scale2=scale/zone.magnify;

if(zone.endTime==Number.POSITIVE_INFINITY){
time=this._unit.change(time,pixels*scale2);
pixels=0;
}else{
var pixels2=this._unit.compare(zone.endTime,time)/scale2;
if(pixels2>pixels){
time=this._unit.change(time,pixels*scale2);
pixels=0;
}else{
time=zone.endTime;
pixels-=pixels2;
}
}
z++;
}
}else{
var z=this._zones.length-1;
while(z>=0){
if(this._unit.compare(time,this._zones[z].startTime)>0){
break;
}
z--;
}

pixels=-pixels;
while(pixels>0){
var zone=this._zones[z];
var scale2=scale/zone.magnify;

if(zone.startTime==Number.NEGATIVE_INFINITY){
time=this._unit.change(time,-pixels*scale2);
pixels=0;
}else{
var pixels2=this._unit.compare(time,zone.startTime)/scale2;
if(pixels2>pixels){
time=this._unit.change(time,-pixels*scale2);
pixels=0;
}else{
time=zone.startTime;
pixels-=pixels2;
}
}
z--;
}
}
return time;
};

Timeline.HotZoneEther.prototype._getScale=function(){
return this._interval/this._pixelsPerInterval;
};


/* labellers.js */



Timeline.GregorianDateLabeller=function(locale,timeZone){
this._locale=locale;
this._timeZone=timeZone;
};

Timeline.GregorianDateLabeller.monthNames=[];
Timeline.GregorianDateLabeller.dayNames=[];
Timeline.GregorianDateLabeller.labelIntervalFunctions=[];

Timeline.GregorianDateLabeller.getMonthName=function(month,locale){
return Timeline.GregorianDateLabeller.monthNames[locale][month];
};

Timeline.GregorianDateLabeller.prototype.labelInterval=function(date,intervalUnit){
var f=Timeline.GregorianDateLabeller.labelIntervalFunctions[this._locale];
if(f==null){
f=Timeline.GregorianDateLabeller.prototype.defaultLabelInterval;
}
return f.call(this,date,intervalUnit);
};

Timeline.GregorianDateLabeller.prototype.labelPrecise=function(date){
return Timeline.DateTime.removeTimeZoneOffset(
date,
this._timeZone
).toUTCString();
};

Timeline.GregorianDateLabeller.prototype.defaultLabelInterval=function(date,intervalUnit){
var text;
var emphasized=false;

date=Timeline.DateTime.removeTimeZoneOffset(date,this._timeZone);

switch(intervalUnit){
case Timeline.DateTime.MILLISECOND:
text=date.getUTCMilliseconds();
break;
case Timeline.DateTime.SECOND:
text=date.getUTCSeconds();
break;
case Timeline.DateTime.MINUTE:
var m=date.getUTCMinutes();
if(m==0){
text=date.getUTCHours()+":00";
emphasized=true;
}else{
text=m;
}
break;
case Timeline.DateTime.HOUR:
text=date.getUTCHours()+"hr";
break;
case Timeline.DateTime.DAY:
text=Timeline.GregorianDateLabeller.getMonthName(date.getUTCMonth(),this._locale)+" "+date.getUTCDate();
break;
case Timeline.DateTime.WEEK:
text=Timeline.GregorianDateLabeller.getMonthName(date.getUTCMonth(),this._locale)+" "+date.getUTCDate();
break;
case Timeline.DateTime.MONTH:
var m=date.getUTCMonth();
if(m!=0){
text=Timeline.GregorianDateLabeller.getMonthName(m,this._locale);
break;
}
case Timeline.DateTime.YEAR:
case Timeline.DateTime.DECADE:
case Timeline.DateTime.CENTURY:
case Timeline.DateTime.MILLENNIUM:
var y=date.getUTCFullYear();
if(y>0){
text=date.getUTCFullYear();
}else{
text=(1-y)+"BC";
}
emphasized=
(intervalUnit==Timeline.DateTime.MONTH)||
(intervalUnit==Timeline.DateTime.DECADE&&y%100==0)||
(intervalUnit==Timeline.DateTime.CENTURY&&y%1000==0);
break;
default:
text=date.toUTCString();
}
return{text:text,emphasized:emphasized};
}



/* layouts.js */




Timeline.StaticTrackBasedLayout=function(params){
this._eventSource=params.eventSource;
this._ether=params.ether;
this._theme=params.theme;
this._showText=("showText"in params)?params.showText:true;

this._laidout=false;

var layout=this;
if(this._eventSource!=null){
this._eventSource.addListener({
onAddMany:function(){
layout._laidout=false;
}
});
}
};

Timeline.StaticTrackBasedLayout.prototype.initialize=function(timeline){
this._timeline=timeline;
};

Timeline.StaticTrackBasedLayout.prototype.getTrack=function(evt){
if(!this._laidout){
this._tracks=[];
this._layout();
this._laidout=true;
}
return this._tracks[evt.getID()];
};

Timeline.StaticTrackBasedLayout.prototype.getTrackCount=function(){
if(!this._laidout){
this._tracks=[];
this._layout();
this._laidout=true;
}
return this._trackCount;
};

Timeline.StaticTrackBasedLayout.prototype._layout=function(){
if(this._eventSource==null){
return;
}

var streams=[Number.NEGATIVE_INFINITY];
var layout=this;
var showText=this._showText;
var theme=this._theme;
var eventTheme=theme.event;

var layoutInstant=function(evt,startPixel,endPixel,streamOffset){
var finalPixel=startPixel-1;
if(evt.isImprecise()){
finalPixel=endPixel;
}
if(showText){
finalPixel=Math.max(finalPixel,startPixel+eventTheme.label.width);
}

return finalPixel;
};
var layoutDuration=function(evt,startPixel,endPixel,streamOffset){
if(evt.isImprecise()){
var startDate=evt.getStart();
var endDate=evt.getEnd();

var startPixel2=Math.round(layout._ether.dateToPixelOffset(startDate));
var endPixel2=Math.round(layout._ether.dateToPixelOffset(endDate));
}else{
var startPixel2=startPixel;
var endPixel2=endPixel;
}

var finalPixel=endPixel2;
var length=Math.max(endPixel2-startPixel2,1);

if(showText){
if(length<eventTheme.label.width){
finalPixel=endPixel2+eventTheme.label.width;
}
}

return finalPixel;
};
var layoutEvent=function(evt){
var startDate=evt.getStart();
var endDate=evt.getEnd();

var startPixel=Math.round(layout._ether.dateToPixelOffset(startDate));
var endPixel=Math.round(layout._ether.dateToPixelOffset(endDate));

var streamIndex=0;
for(;streamIndex<streams.length;streamIndex++){
if(streams[streamIndex]<startPixel){
break;
}
}
if(streamIndex>=streams.length){
streams.push(Number.NEGATIVE_INFINITY);
}

var streamOffset=(eventTheme.track.offset+
streamIndex*(eventTheme.track.height+eventTheme.track.gap))+"em";

layout._tracks[evt.getID()]=streamIndex;

if(evt.isInstant()){
streams[streamIndex]=layoutInstant(evt,startPixel,endPixel,streamOffset);
}else{
streams[streamIndex]=layoutDuration(evt,startPixel,endPixel,streamOffset);
}
};

var iterator=this._eventSource.getAllEventIterator();
while(iterator.hasNext()){
var evt=iterator.next();
layoutEvent(evt);
}

this._trackCount=streams.length;
};

/* painters.js */



Timeline.DurationEventPainter=function(params){
this._params=params;
this._theme=params.theme;
this._layout=params.layout;

this._showText=params.showText;
this._showLineForNoText=("showLineForNoText"in params)?
params.showLineForNoText:params.theme.event.instant.showLineForNoText;

this._filterMatcher=null;
this._highlightMatcher=null;
};

Timeline.DurationEventPainter.prototype.initialize=function(band,timeline){
this._band=band;
this._timeline=timeline;
this._layout.initialize(band,timeline);

this._eventLayer=null;
this._highlightLayer=null;
};

Timeline.DurationEventPainter.prototype.getLayout=function(){
return this._layout;
};

Timeline.DurationEventPainter.prototype.setLayout=function(layout){
this._layout=layout;
};

Timeline.DurationEventPainter.prototype.getFilterMatcher=function(){
return this._filterMatcher;
};

Timeline.DurationEventPainter.prototype.setFilterMatcher=function(filterMatcher){
this._filterMatcher=filterMatcher;
};

Timeline.DurationEventPainter.prototype.getHighlightMatcher=function(){
return this._highlightMatcher;
};

Timeline.DurationEventPainter.prototype.setHighlightMatcher=function(highlightMatcher){
this._highlightMatcher=highlightMatcher;
};

Timeline.DurationEventPainter.prototype.paint=function(){
var eventSource=this._band.getEventSource();
if(eventSource==null){
return;
}

if(this._highlightLayer!=null){
this._band.removeLayerDiv(this._highlightLayer);
}
this._highlightLayer=this._band.createLayerDiv(105);
this._highlightLayer.setAttribute("name","event-highlights");
this._highlightLayer.style.display="none";

if(this._eventLayer!=null){
this._band.removeLayerDiv(this._eventLayer);
}
this._eventLayer=this._band.createLayerDiv(110);
this._eventLayer.setAttribute("name","events");
this._eventLayer.style.display="none";

var minDate=this._band.getMinDate();
var maxDate=this._band.getMaxDate();

var doc=this._timeline.getDocument();

var p=this;
var eventLayer=this._eventLayer;
var highlightLayer=this._highlightLayer;

var showText=this._showText;
var theme=this._params.theme;
var eventTheme=theme.event;
var trackOffset=eventTheme.track.offset;
var trackHeight=("trackHeight"in this._params)?this._params.trackHeight:eventTheme.track.height;
var trackGap=("trackGap"in this._params)?this._params.trackGap:eventTheme.track.gap;


var appendIcon=function(evt,div){
var icon=evt.getIcon();
var img=Timeline.Graphics.createTranslucentImage(
doc,icon!=null?icon:eventTheme.instant.icon
);
div.appendChild(img);
div.style.cursor="pointer";

Timeline.DOM.registerEvent(div,"mousedown",function(elmt,domEvt,target){
p._onClickInstantEvent(img,domEvt,evt);
});
};
var createHighlightDiv=function(highlightIndex,startPixel,length,highlightOffset,highlightWidth){
if(highlightIndex>=0){
var color=eventTheme.highlightColors[Math.min(highlightIndex,eventTheme.highlightColors.length-1)];

var div=doc.createElement("div");
div.style.position="absolute";
div.style.overflow="hidden";
div.style.left=(startPixel-3)+"px";
div.style.width=(length+6)+"px";
div.style.top=highlightOffset+"em";
div.style.height=highlightWidth+"em";
div.style.background=color;


highlightLayer.appendChild(div);
}
};

var createInstantDiv=function(evt,startPixel,endPixel,streamOffset,highlightIndex,highlightOffset,highlightWidth){
if(evt.isImprecise()){
var length=Math.max(endPixel-startPixel,1);

var divImprecise=doc.createElement("div");
divImprecise.style.position="absolute";
divImprecise.style.overflow="hidden";

divImprecise.style.top=streamOffset;
divImprecise.style.height=trackHeight+"em";
divImprecise.style.left=startPixel+"px";
divImprecise.style.width=length+"px";

divImprecise.style.background=eventTheme.instant.impreciseColor;
if(eventTheme.instant.impreciseOpacity<100){
Timeline.Graphics.setOpacity(divImprecise,eventTheme.instant.impreciseOpacity);
}

eventLayer.appendChild(divImprecise);
}

var div=doc.createElement("div");
div.style.position="absolute";
div.style.overflow="hidden";
eventLayer.appendChild(div);

var foreground=evt.getTextColor();
var background=evt.getColor();

var realign=-8;
var length=16;
if(showText){
div.style.width=eventTheme.label.width+"px";
div.style.color=foreground!=null?foreground:eventTheme.label.outsideColor;

appendIcon(evt,div);
div.appendChild(doc.createTextNode(evt.getText()));
}else{
if(p._showLineForNoText){
div.style.width="1px";
div.style.borderLeft="1px solid "+(background!=null?background:eventTheme.instant.lineColor);
realign=0;
length=1;
}else{
appendIcon(evt,div);
}
}

div.style.top=streamOffset;
div.style.height=trackHeight+"em";
div.style.left=(startPixel+realign)+"px";

createHighlightDiv(highlightIndex,(startPixel+realign),length,highlightOffset,highlightWidth);
};
var createDurationDiv=function(evt,startPixel,endPixel,streamOffset,highlightIndex,highlightOffset,highlightWidth){
var attachClickEvent=function(elmt){
elmt.style.cursor="pointer";
Timeline.DOM.registerEvent(elmt,"mousedown",function(elmt,domEvt,target){
p._onClickDurationEvent(domEvt,evt,target);
});
};

var length=Math.max(endPixel-startPixel,1);
if(evt.isImprecise()){
var div=doc.createElement("div");
div.style.position="absolute";
div.style.overflow="hidden";

div.style.top=streamOffset;
div.style.height=trackHeight+"em";
div.style.left=startPixel+"px";
div.style.width=length+"px";

div.style.background=eventTheme.duration.impreciseColor;
if(eventTheme.duration.impreciseOpacity<100){
Timeline.Graphics.setOpacity(div,eventTheme.duration.impreciseOpacity);
}

eventLayer.appendChild(div);

var startDate=evt.getLatestStart();
var endDate=evt.getEarliestEnd();

var startPixel2=Math.round(p._band.dateToPixelOffset(startDate));
var endPixel2=Math.round(p._band.dateToPixelOffset(endDate));
}else{
var startPixel2=startPixel;
var endPixel2=endPixel;
}

var foreground=evt.getTextColor();
var outside=true;
if(startPixel2<=endPixel2){
length=Math.max(endPixel2-startPixel2,1);
outside=!(length>eventTheme.label.width);

div=doc.createElement("div");
div.style.position="absolute";
div.style.overflow="hidden";

div.style.top=streamOffset;
div.style.height=trackHeight+"em";
div.style.left=startPixel2+"px";
div.style.width=length+"px";

var background=evt.getColor();

div.style.background=background!=null?background:eventTheme.duration.color;
if(eventTheme.duration.opacity<100){
Timeline.Graphics.setOpacity(div,eventTheme.duration.opacity);
}

eventLayer.appendChild(div);
}else{
var temp=startPixel2;
startPixel2=endPixel2;
endPixel2=temp;
}
if(div==null){
console.log(evt);
}
attachClickEvent(div);

if(showText){
var divLabel=doc.createElement("div");
divLabel.style.position="absolute";

divLabel.style.top=streamOffset;
divLabel.style.height=trackHeight+"em";
divLabel.style.left=((length>eventTheme.label.width)?startPixel2:endPixel2)+"px";
divLabel.style.width=eventTheme.label.width+"px";
divLabel.style.color=foreground!=null?foreground:(outside?eventTheme.label.outsideColor:eventTheme.label.insideColor);
divLabel.style.overflow="hidden";
divLabel.appendChild(doc.createTextNode(evt.getText()));

eventLayer.appendChild(divLabel);
attachClickEvent(divLabel);
}

createHighlightDiv(highlightIndex,startPixel,endPixel-startPixel,highlightOffset,highlightWidth);
};

var createEventDiv=function(evt,highlightIndex){
var startDate=evt.getStart();
var endDate=evt.getEnd();

var startPixel=Math.round(p._band.dateToPixelOffset(startDate));
var endPixel=Math.round(p._band.dateToPixelOffset(endDate));

var streamOffset=(trackOffset+
p._layout.getTrack(evt)*(trackHeight+trackGap));

if(evt.isInstant()){
createInstantDiv(evt,startPixel,endPixel,streamOffset+"em",
highlightIndex,streamOffset-trackGap,trackHeight+2*trackGap);
}else{
createDurationDiv(evt,startPixel,endPixel,streamOffset+"em",
highlightIndex,streamOffset-trackGap,trackHeight+2*trackGap);
}
};

var filterMatcher=(this._filterMatcher!=null)?
this._filterMatcher:
function(evt){return true;};
var highlightMatcher=(this._highlightMatcher!=null)?
this._highlightMatcher:
function(evt){return-1;};

var iterator=eventSource.getEventIterator(minDate,maxDate);
while(iterator.hasNext()){
var evt=iterator.next();
if(filterMatcher(evt)){
createEventDiv(evt,highlightMatcher(evt));
}
}

this._highlightLayer.style.display="block";
this._eventLayer.style.display="block";
};

Timeline.DurationEventPainter.prototype.softPaint=function(){
};

Timeline.DurationEventPainter.prototype._onClickInstantEvent=function(icon,domEvt,evt){
domEvt.cancelBubble=true;

var c=Timeline.DOM.getPageCoordinates(icon);
this._showBubble(
c.left+Math.ceil(icon.offsetWidth/2),
c.top+Math.ceil(icon.offsetHeight/2),
evt
);
};

Timeline.DurationEventPainter.prototype._onClickDurationEvent=function(domEvt,evt,target){
domEvt.cancelBubble=true;
if("pageX"in domEvt){
var x=domEvt.pageX;
var y=domEvt.pageY;
}else{
var c=Timeline.DOM.getPageCoordinates(target);
var x=domEvt.offsetX+c.left;
var y=domEvt.offsetY+c.top;
}
this._showBubble(x,y,evt);
};

Timeline.DurationEventPainter.prototype._showBubble=function(x,y,evt){
var div=this._band.openBubbleForPoint(
x,y,
this._theme.event.bubble.width,
this._theme.event.bubble.height
);

evt.fillInfoBubble(div,this._theme,this._band.getLabeller());
};

/* sources.js */




Timeline.DefaultEventSource=function(eventIndex){
this._events=(eventIndex instanceof Object)?eventIndex:new Timeline.EventIndex();
this._listeners=[];
};

Timeline.DefaultEventSource.prototype.addListener=function(listener){
this._listeners.push(listener);
};

Timeline.DefaultEventSource.prototype.removeListener=function(listener){
for(var i=0;i<this._listeners.length;i++){
if(this._listeners[i]==listener){
this._listeners.splice(i,1);
break;
}
}
};

Timeline.DefaultEventSource.prototype.loadXML=function(xml,url){
var base=this._getBaseURL(url);

var wikiURL=xml.documentElement.getAttribute("wiki-url");
var wikiSection=xml.documentElement.getAttribute("wiki-section");

var dateTimeFormat=xml.documentElement.getAttribute("date-time-format");
var parseDateTimeFunction=this._events.getUnit().getParser(dateTimeFormat);

var node=xml.documentElement.firstChild;
var added=false;
while(node!=null){
if(node.nodeType==1){
var description="";
if(node.firstChild!=null&&node.firstChild.nodeType==3){
description=node.firstChild.nodeValue;
}
var evt=new Timeline.DefaultEventSource.Event(
parseDateTimeFunction(node.getAttribute("start")),
parseDateTimeFunction(node.getAttribute("end")),
parseDateTimeFunction(node.getAttribute("latestStart")),
parseDateTimeFunction(node.getAttribute("earliestEnd")),
node.getAttribute("isDuration")!="true",
node.getAttribute("title"),
description,
this._resolveRelativeURL(node.getAttribute("image"),base),
this._resolveRelativeURL(node.getAttribute("link"),base),
this._resolveRelativeURL(node.getAttribute("icon"),base),
node.getAttribute("color"),
node.getAttribute("textColor")
);
evt._node=node;
evt.getProperty=function(name){
return this._node.getAttribute(name);
};
evt.setWikiInfo(wikiURL,wikiSection);

this._events.add(evt);

added=true;
}
node=node.nextSibling;
}

if(added){
this._fire("onAddMany",[]);
}
};


Timeline.DefaultEventSource.prototype.loadJSON=function(data,url){
var base=this._getBaseURL(url);
var added=false;
if(data&&data.events){
var wikiURL=("wikiURL"in data)?data.wikiURL:null;
var wikiSection=("wikiSection"in data)?data.wikiSection:null;

var dateTimeFormat=("dateTimeFormat"in data)?data.dateTimeFormat:null;
var parseDateTimeFunction=this._events.getUnit().getParser(dateTimeFormat);

for(var i=0;i<data.events.length;i++){
var event=data.events[i];
var evt=new Timeline.DefaultEventSource.Event(
parseDateTimeFunction(event.start),
parseDateTimeFunction(event.end),
parseDateTimeFunction(event.latestStart),
parseDateTimeFunction(event.earliestEnd),
event.isDuration||false,
event.title,
event.description,
this._resolveRelativeURL(event.image,base),
this._resolveRelativeURL(event.link,base),
this._resolveRelativeURL(event.icon,base),
event.color,
event.textColor
);
evt._obj=event;
evt.getProperty=function(name){
return this._obj[name];
};
evt.setWikiInfo(wikiURL,wikiSection);

this._events.add(evt);
added=true;
}
}

if(added){
this._fire("onAddMany",[]);
}
};


Timeline.DefaultEventSource.prototype.loadSPARQL=function(xml,url){
var base=this._getBaseURL(url);

var dateTimeFormat='iso8601';
var parseDateTimeFunction=this._events.getUnit().getParser(dateTimeFormat);

if(xml==null){
return;
}


var node=xml.documentElement.firstChild;
while(node!=null&&(node.nodeType!=1||node.nodeName!='results')){
node=node.nextSibling;
}

var wikiURL=null;
var wikiSection=null;
if(node!=null){
wikiURL=node.getAttribute("wiki-url");
wikiSection=node.getAttribute("wiki-section");

node=node.firstChild;
}

var added=false;
while(node!=null){
if(node.nodeType==1){
var bindings={};
var binding=node.firstChild;
while(binding!=null){
if(binding.nodeType==1&&
binding.firstChild!=null&&
binding.firstChild.nodeType==1&&
binding.firstChild.firstChild!=null&&
binding.firstChild.firstChild.nodeType==3){
bindings[binding.getAttribute('name')]=binding.firstChild.firstChild.nodeValue;
}
binding=binding.nextSibling;
}

if(bindings["start"]==null&&bindings["date"]!=null){
bindings["start"]=bindings["date"];
}

var evt=new Timeline.DefaultEventSource.Event(
parseDateTimeFunction(bindings["start"]),
parseDateTimeFunction(bindings["end"]),
parseDateTimeFunction(bindings["latestStart"]),
parseDateTimeFunction(bindings["earliestEnd"]),
bindings["isDuration"]!="true",
bindings["title"],
bindings["description"],
this._resolveRelativeURL(bindings["image"],base),
this._resolveRelativeURL(bindings["link"],base),
this._resolveRelativeURL(bindings["icon"],base),
bindings["color"],
bindings["textColor"]
);
evt._bindings=bindings;
evt.getProperty=function(name){
return this._bindings[name];
};
evt.setWikiInfo(wikiURL,wikiSection);

this._events.add(evt);
added=true;
}
node=node.nextSibling;
}

if(added){
this._fire("onAddMany",[]);
}
};

Timeline.DefaultEventSource.prototype.add=function(evt){
this._events.add(evt);
this._fire("onAddOne",[evt]);
};

Timeline.DefaultEventSource.prototype.addMany=function(events){
for(var i=0;i<events.length;i++){
this._events.add(events[i]);
}
this._fire("onAddMany",[]);
};

Timeline.DefaultEventSource.prototype.clear=function(){
this._events.removeAll();
this._fire("onClear",[]);
};

Timeline.DefaultEventSource.prototype.getEventIterator=function(startDate,endDate){
return this._events.getIterator(startDate,endDate);
};

Timeline.DefaultEventSource.prototype.getAllEventIterator=function(){
return this._events.getAllIterator();
};

Timeline.DefaultEventSource.prototype.getCount=function(){
return this._events.getCount();
};

Timeline.DefaultEventSource.prototype.getEarliestDate=function(){
return this._events.getEarliestDate();
};

Timeline.DefaultEventSource.prototype.getLatestDate=function(){
return this._events.getLatestDate();
};

Timeline.DefaultEventSource.prototype._fire=function(handlerName,args){
for(var i=0;i<this._listeners.length;i++){
var listener=this._listeners[i];
if(handlerName in listener){
try{
listener[handlerName].apply(listener,args);
}catch(e){
Timeline.Debug.exception(e);
}
}
}
};

Timeline.DefaultEventSource.prototype._getBaseURL=function(url){
if(url.indexOf("://")<0){
var url2=this._getBaseURL(document.location.href);
if(url.substr(0,1)=="/"){
url=url2.substr(0,url2.indexOf("/",url2.indexOf("://")+3))+url;
}else{
url=url2+url;
}
}

var i=url.lastIndexOf("/");
if(i<0){
return"";
}else{
return url.substr(0,i+1);
}
};

Timeline.DefaultEventSource.prototype._resolveRelativeURL=function(url,base){
if(url==null||url==""){
return url;
}else if(url.indexOf("://")>0){
return url;
}else if(url.substr(0,1)=="/"){
return base.substr(0,base.indexOf("/",base.indexOf("://")+3))+url;
}else{
return base+url;
}
};


Timeline.DefaultEventSource.Event=function(
start,end,latestStart,earliestEnd,instant,
text,description,image,link,
icon,color,textColor){

this._id="e"+Math.floor(Math.random()*1000000);

this._instant=instant||(end==null);

this._start=start;
this._end=(end!=null)?end:start;

this._latestStart=(latestStart!=null)?latestStart:(instant?this._end:this._start);
this._earliestEnd=(earliestEnd!=null)?earliestEnd:(instant?this._start:this._end);

this._text=Timeline.HTML.deEntify(text);
this._description=Timeline.HTML.deEntify(description);
this._image=(image!=null&&image!="")?image:null;
this._link=(link!=null&&link!="")?link:null;

this._icon=(icon!=null&&icon!="")?icon:null;
this._color=(color!=null&&color!="")?color:null;
this._textColor=(textColor!=null&&textColor!="")?textColor:null;

this._wikiURL=null;
this._wikiSection=null;
};

Timeline.DefaultEventSource.Event.prototype={
getID:function(){return this._id;},

isInstant:function(){return this._instant;},
isImprecise:function(){return this._start!=this._latestStart||this._end!=this._earliestEnd;},

getStart:function(){return this._start;},
getEnd:function(){return this._end;},
getLatestStart:function(){return this._latestStart;},
getEarliestEnd:function(){return this._earliestEnd;},

getText:function(){return this._text;},
getDescription:function(){return this._description;},
getImage:function(){return this._image;},
getLink:function(){return this._link;},

getIcon:function(){return this._icon;},
getColor:function(){return this._color;},
getTextColor:function(){return this._textColor;},

getProperty:function(name){return null;},

getWikiURL:function(){return this._wikiURL;},
getWikiSection:function(){return this._wikiSection;},
setWikiInfo:function(wikiURL,wikiSection){
this._wikiURL=wikiURL;
this._wikiSection=wikiSection;
},

fillDescription:function(elmt){
elmt.innerHTML=this._description;
},
fillWikiInfo:function(elmt){
if(this._wikiURL!=null&&this._wikiSection!=null){
var wikiID=this.getProperty("wikiID");
if(wikiID==null||wikiID.length==0){
wikiID=this.getText();
}
wikiID=wikiID.replace(/\s/g,"_");

var url=this._wikiURL+this._wikiSection.replace(/\s/g,"_")+"/"+wikiID;
var a=document.createElement("a");
a.href=url;
a.target="new";
a.innerHTML=Timeline.strings[Timeline.Platform.clientLocale].wikiLinkLabel;

elmt.appendChild(document.createTextNode("["));
elmt.appendChild(a);
elmt.appendChild(document.createTextNode("]"));
}else{
elmt.style.display="none";
}
},
fillTime:function(elmt,labeller){
if(this._instant){
if(this.isImprecise()){
elmt.appendChild(elmt.ownerDocument.createTextNode(labeller.labelPrecise(this._start)));
elmt.appendChild(elmt.ownerDocument.createElement("br"));
elmt.appendChild(elmt.ownerDocument.createTextNode(labeller.labelPrecise(this._end)));
}else{
elmt.appendChild(elmt.ownerDocument.createTextNode(labeller.labelPrecise(this._start)));
}
}else{
if(this.isImprecise()){
elmt.appendChild(elmt.ownerDocument.createTextNode(
labeller.labelPrecise(this._start)+" ~ "+labeller.labelPrecise(this._latestStart)));
elmt.appendChild(elmt.ownerDocument.createElement("br"));
elmt.appendChild(elmt.ownerDocument.createTextNode(
labeller.labelPrecise(this._earliestEnd)+" ~ "+labeller.labelPrecise(this._end)));
}else{
elmt.appendChild(elmt.ownerDocument.createTextNode(labeller.labelPrecise(this._start)));
elmt.appendChild(elmt.ownerDocument.createElement("br"));
elmt.appendChild(elmt.ownerDocument.createTextNode(labeller.labelPrecise(this._end)));
}
}
},
fillInfoBubble:function(elmt,theme,labeller){
var doc=elmt.ownerDocument;

var title=this.getText();
var link=this.getLink();
var image=this.getImage();

if(image!=null){
var img=doc.createElement("img");
img.src=image;

theme.event.bubble.imageStyler(img);
elmt.appendChild(img);
}

var divTitle=doc.createElement("div");
var textTitle=doc.createTextNode(title);
if(link!=null){
var a=doc.createElement("a");
a.href=link;
a.appendChild(textTitle);
divTitle.appendChild(a);
}else{
divTitle.appendChild(textTitle);
}
theme.event.bubble.titleStyler(divTitle);
elmt.appendChild(divTitle);

var divBody=doc.createElement("div");
this.fillDescription(divBody);
theme.event.bubble.bodyStyler(divBody);
elmt.appendChild(divBody);

var divTime=doc.createElement("div");
this.fillTime(divTime,labeller);
theme.event.bubble.timeStyler(divTime);
elmt.appendChild(divTime);

var divWiki=doc.createElement("div");
this.fillWikiInfo(divWiki);
theme.event.bubble.wikiStyler(divWiki);
elmt.appendChild(divWiki);
}
};

/* themes.js */




Timeline.ClassicTheme=new Object();

Timeline.ClassicTheme.implementations=[];

Timeline.ClassicTheme.create=function(locale){
if(locale==null){
locale=Timeline.Platform.getDefaultLocale();
}

var f=Timeline.ClassicTheme.implementations[locale];
if(f==null){
f=Timeline.ClassicTheme._Impl;
}
return new f();
};

Timeline.ClassicTheme._Impl=function(){
this.firstDayOfWeek=0;

this.ether={
backgroundColors:[
"#EEE",
"#DDD",
"#CCC",
"#AAA"
],
highlightColor:"white",
highlightOpacity:50,
interval:{
line:{
show:true,
color:"#aaa",
opacity:25
},
weekend:{
color:"#FFFFE0",
opacity:30
},
marker:{
hAlign:"Bottom",
hBottomStyler:function(elmt){
elmt.className="timeline-ether-marker-bottom";
},
hBottomEmphasizedStyler:function(elmt){
elmt.className="timeline-ether-marker-bottom-emphasized";
},
hTopStyler:function(elmt){
elmt.className="timeline-ether-marker-top";
},
hTopEmphasizedStyler:function(elmt){
elmt.className="timeline-ether-marker-top-emphasized";
},

vAlign:"Right",
vRightStyler:function(elmt){
elmt.className="timeline-ether-marker-right";
},
vRightEmphasizedStyler:function(elmt){
elmt.className="timeline-ether-marker-right-emphasized";
},
vLeftStyler:function(elmt){
elmt.className="timeline-ether-marker-left";
},
vLeftEmphasizedStyler:function(elmt){
elmt.className="timeline-ether-marker-left-emphasized";
}
}
}
};

this.event={
track:{
offset:0.5,
height:1.5,
gap:0.5
},
instant:{
icon:Timeline.urlPrefix+"images/dull-blue-circle.png",
lineColor:"#58A0DC",
impreciseColor:"#58A0DC",
impreciseOpacity:20,
showLineForNoText:true
},
duration:{
color:"#58A0DC",
opacity:100,
impreciseColor:"#58A0DC",
impreciseOpacity:20
},
label:{
insideColor:"white",
outsideColor:"black",
width:200
},
highlightColors:[
"#FFFF00",
"#FFC000",
"#FF0000",
"#0000FF"
],
bubble:{
width:250,
height:125,
titleStyler:function(elmt){
elmt.className="timeline-event-bubble-title";
},
bodyStyler:function(elmt){
elmt.className="timeline-event-bubble-body";
},
imageStyler:function(elmt){
elmt.className="timeline-event-bubble-image";
},
wikiStyler:function(elmt){
elmt.className="timeline-event-bubble-wiki";
},
timeStyler:function(elmt){
elmt.className="timeline-event-bubble-time";
}
}
};
};

/* units.js */





Timeline.NativeDateUnit=new Object();

Timeline.NativeDateUnit.createLabeller=function(locale,timeZone){
return new Timeline.GregorianDateLabeller(locale,timeZone);
};


Timeline.NativeDateUnit.makeDefaultValue=function(){
return new Date();
};

Timeline.NativeDateUnit.cloneValue=function(v){
return new Date(v.getTime());
};

Timeline.NativeDateUnit.getParser=function(format){
if(typeof format=="string"){
format=format.toLowerCase();
}
return(format=="iso8601"||format=="iso 8601")?

Timeline.DateTime.parseIso8601DateTime:

Timeline.DateTime.parseGregorianDateTime;

};

Timeline.NativeDateUnit.parseFromObject=function(o){
return Timeline.DateTime.parseGregorianDateTime(o);
};


Timeline.NativeDateUnit.toNumber=function(v){
return v.getTime();
};

Timeline.NativeDateUnit.fromNumber=function(n){
return new Date(n);
};

Timeline.NativeDateUnit.compare=function(v1,v2){
var n1,n2;
if(typeof v1=="object"){
n1=v1.getTime();
}else{
n1=Number(v1);
}
if(typeof v2=="object"){
n2=v2.getTime();
}else{
n2=Number(v2);
}

return n1-n2;
};

Timeline.NativeDateUnit.earlier=function(v1,v2){
return Timeline.NativeDateUnit.compare(v1,v2)<0?v1:v2;
};

Timeline.NativeDateUnit.later=function(v1,v2){
return Timeline.NativeDateUnit.compare(v1,v2)>0?v1:v2;
};

Timeline.NativeDateUnit.change=function(v,n){
return new Date(v.getTime()+n);
};
// end of bundle.js

(function() {
    var cssFiles = [
        "timeline.css",
        "ethers.css",
        "events.css"
    ];

    var localizedJavascriptFiles = [
        "timeline",
        "labellers"
    ];
    var localizedCssFiles = [
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

        var includeCssFile = function(url) {
            zk.loadCSS(url);
        };

        var includeJavascriptFile = function(url) {
            zk.load(url);
        };

        var includeCssFiles = function(urlPrefix, filenames) {
            for (var i = 0; i < filenames.length; i++) {
                includeCssFile(urlPrefix + filenames[i]);
            }
        };

        var includeJavascriptFiles = function(urlPrefix, filenames) {
            for (var i = 0; i < filenames.length; i++) {
                includeJavascriptFile(urlPrefix + filenames[i]);
            }
        };

        ///for ZK : using "zk.load" and "zk.loadCSS" .
        Timeline.cssUrlPrefix="js/ext/timeline/api/";
        Timeline.jsUrlPrefix="ext.timeline.api.";

        /*
         *  Include non-localized files
         */
        includeCssFiles(Timeline.cssUrlPrefix,["bundle.css.dsp"]);

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
        }
        var tryLocale = function(locale) {
            if (tryExactLocale(locale)) {
                return locale;
            }

            var dash = locale.indexOf("-");
            if (dash > 0 && tryExactLocale(locale.substr(0, dash))) {
                return locale.substr(0, dash);
            }

            return null;
        }

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
                includeJavascriptFiles(Timeline.jsUrlPrefix + "scripts.l10n." + locale + ".", localizedJavascriptFiles);
                includeCssFiles(Timeline.cssUrlPrefix + "styles/l10n/" + locale + "/", localizedCssFiles);
            }
        }

        Timeline.Platform.serverLocale = defaultServerLocale;
        Timeline.Platform.clientLocale = defaultClientLocale;
    } catch (e) {
        alert(e);
    }
})();

// end of zkTimeline-api.js

/*==================================================
 *  Simile Ajax API
 *
 *  Include this file in your HTML file as follows:
 *
 *    <script src="http://simile.mit.edu/ajax/api/simile-ajax-api.js" type="text/javascript"></script>
 *
 *==================================================
 */

if (typeof SimileAjax == "undefined") {
    var SimileAjax = {
        loaded:                 false,
        loadingScriptsCount:    0,
        error:                  null,
        params:                 { bundle:"true" }
    };

    SimileAjax.Platform = new Object();
    /*
        HACK: We need these 2 things here because we cannot simply append
        a <script> element containing code that accesses SimileAjax.Platform
        to initialize it because IE executes that <script> code first
        before it loads ajax.js and platform.js.
    */

    // simile-ajax-bundle.js
    /* debug.js */
    SimileAjax.Debug={
    silent:false
    };

    SimileAjax.Debug.log=function(msg){
    var f;
    if("console"in window&&"log"in window.console){
    f=function(msg2){
    console.log(msg2);
    }
    }else{
    f=function(msg2){
    if(!SimileAjax.Debug.silent){
    alert(msg2);
    }
    }
    }
    SimileAjax.Debug.log=f;
    f(msg);
    };

    SimileAjax.Debug.warn=function(msg){
    var f;
    if("console"in window&&"warn"in window.console){
    f=function(msg2){
    console.warn(msg2);
    }
    }else{
    f=function(msg2){
    if(!SimileAjax.Debug.silent){
    alert(msg2);
    }
    }
    }
    SimileAjax.Debug.warn=f;
    f(msg);
    };

    SimileAjax.Debug.exception=function(e,msg){
    var f,params=SimileAjax.parseURLParameters();
    if(params.errors=="throw"||SimileAjax.params.errors=="throw"){
    f=function(e2,msg2){
    throw(e2);
    };
    }else if("console"in window&&"error"in window.console){
    f=function(e2,msg2){
    if(msg2!=null){
    console.error(msg2+" %o",e2);
    }else{
    console.error(e2);
    }
    throw(e2);
    };
    }else{
    f=function(e2,msg2){
    if(!SimileAjax.Debug.silent){
    alert("Caught exception: "+msg2+"\n\nDetails: "+("description"in e2?e2.description:e2));
    }
    throw(e2);
    };
    }
    SimileAjax.Debug.exception=f;
    f(e,msg);
    };

    SimileAjax.Debug.objectToString=function(o){
    return SimileAjax.Debug._objectToString(o,"");
    };

    SimileAjax.Debug._objectToString=function(o,indent){
    var indent2=indent+" ";
    if(typeof o=="object"){
    var s="{";
    for(n in o){
    s+=indent2+n+": "+SimileAjax.Debug._objectToString(o[n],indent2)+"\n";
    }
    s+=indent+"}";
    return s;
    }else if(typeof o=="array"){
    var s="[";
    for(var n=0;n<o.length;n++){
    s+=SimileAjax.Debug._objectToString(o[n],indent2)+"\n";
    }
    s+=indent+"]";
    return s;
    }else{
    return o;
    }
    };

    /* platform.js */

    SimileAjax.Platform.os={
    isMac:false,
    isWin:false,
    isWin32:false,
    isUnix:false
    };
    SimileAjax.Platform.browser={
    isIE:false,
    isNetscape:false,
    isMozilla:false,
    isFirefox:false,
    isOpera:false,
    isSafari:false,

    majorVersion:0,
    minorVersion:0
    };

    (function(){
    var an=navigator.appName.toLowerCase();
    var ua=navigator.userAgent.toLowerCase();


    SimileAjax.Platform.os.isMac=(ua.indexOf('mac')!=-1);
    SimileAjax.Platform.os.isWin=(ua.indexOf('win')!=-1);
    SimileAjax.Platform.os.isWin32=SimileAjax.Platform.isWin&&(
    ua.indexOf('95')!=-1||
    ua.indexOf('98')!=-1||
    ua.indexOf('nt')!=-1||
    ua.indexOf('win32')!=-1||
    ua.indexOf('32bit')!=-1
    );
    SimileAjax.Platform.os.isUnix=(ua.indexOf('x11')!=-1);


    SimileAjax.Platform.browser.isIE=(an.indexOf("microsoft")!=-1);
    SimileAjax.Platform.browser.isNetscape=(an.indexOf("netscape")!=-1);
    SimileAjax.Platform.browser.isMozilla=(ua.indexOf("mozilla")!=-1);
    SimileAjax.Platform.browser.isFirefox=(ua.indexOf("firefox")!=-1);
    SimileAjax.Platform.browser.isOpera=(an.indexOf("opera")!=-1);
    SimileAjax.Platform.browser.isSafari=(an.indexOf("safari")!=-1);

    var parseVersionString=function(s){
    var a=s.split(".");
    SimileAjax.Platform.browser.majorVersion=parseInt(a[0]);
    SimileAjax.Platform.browser.minorVersion=parseInt(a[1]);
    };
    var indexOf=function(s,sub,start){
    var i=s.indexOf(sub,start);
    return i>=0?i:s.length;
    };

    if(SimileAjax.Platform.browser.isMozilla){
    var offset=ua.indexOf("mozilla/");
    if(offset>=0){
    parseVersionString(ua.substring(offset+8,indexOf(ua," ",offset)));
    }
    }
    if(SimileAjax.Platform.browser.isIE){
    var offset=ua.indexOf("msie ");
    if(offset>=0){
    parseVersionString(ua.substring(offset+5,indexOf(ua,";",offset)));
    }
    }
    if(SimileAjax.Platform.browser.isNetscape){
    var offset=ua.indexOf("rv:");
    if(offset>=0){
    parseVersionString(ua.substring(offset+3,indexOf(ua,")",offset)));
    }
    }
    if(SimileAjax.Platform.browser.isFirefox){
    var offset=ua.indexOf("firefox/");
    if(offset>=0){
    parseVersionString(ua.substring(offset+8,indexOf(ua," ",offset)));
    }
    }

    if(!("localeCompare"in String.prototype)){
    String.prototype.localeCompare=function(s){
    if(this<s)return-1;
    else if(this>s)return 1;
    else return 0;
    };
    }
    })();

    SimileAjax.Platform.getDefaultLocale=function(){
    return SimileAjax.Platform.clientLocale;
    };

    /* ajax.js */



    SimileAjax.ListenerQueue=function(wildcardHandlerName){
    this._listeners=[];
    this._wildcardHandlerName=wildcardHandlerName;
    };

    SimileAjax.ListenerQueue.prototype.add=function(listener){
    this._listeners.push(listener);
    };

    SimileAjax.ListenerQueue.prototype.remove=function(listener){
    var listeners=this._listeners;
    for(var i=0;i<listeners.length;i++){
    if(listeners[i]==listener){
    listeners.splice(i,1);
    break;
    }
    }
    };

    SimileAjax.ListenerQueue.prototype.fire=function(handlerName,args){
    var listeners=[].concat(this._listeners);
    for(var i=0;i<listeners.length;i++){
    var listener=listeners[i];
    if(handlerName in listener){
    try{
    listener[handlerName].apply(listener,args);
    }catch(e){
    SimileAjax.Debug.exception("Error firing event of name "+handlerName,e);
    }
    }else if(this._wildcardHandlerName!=null&&
    this._wildcardHandlerName in listener){
    try{
    listener[this._wildcardHandlerName].apply(listener,[handlerName]);
    }catch(e){
    SimileAjax.Debug.exception("Error firing event of name "+handlerName+" to wildcard handler",e);
    }
    }
    }
    };



    /* data-structure.js */


    SimileAjax.Set=function(a){
    this._hash={};
    this._count=0;

    if(a instanceof Array){
    for(var i=0;i<a.length;i++){
    this.add(a[i]);
    }
    }else if(a instanceof SimileAjax.Set){
    this.addSet(a);
    }
    }


    SimileAjax.Set.prototype.add=function(o){
    if(!(o in this._hash)){
    this._hash[o]=true;
    this._count++;
    return true;
    }
    return false;
    }


    SimileAjax.Set.prototype.addSet=function(set){
    for(o in set._hash){
    this.add(o);
    }
    }


    SimileAjax.Set.prototype.remove=function(o){
    if(o in this._hash){
    delete this._hash[o];
    this._count--;
    return true;
    }
    return false;
    }


    SimileAjax.Set.prototype.removeSet=function(set){
    for(o in set._hash){
    this.remove(o);
    }
    }


    SimileAjax.Set.prototype.retainSet=function(set){
    for(o in this._hash){
    if(!set.contains(o)){
    delete this._hash[o];
    this._count--;
    }
    }
    }


    SimileAjax.Set.prototype.contains=function(o){
    return(o in this._hash);
    }


    SimileAjax.Set.prototype.size=function(){
    return this._count;
    }


    SimileAjax.Set.prototype.toArray=function(){
    var a=[];
    for(o in this._hash){
    a.push(o);
    }
    return a;
    }


    SimileAjax.Set.prototype.visit=function(f){
    for(o in this._hash){
    if(f(o)==true){
    break;
    }
    }
    }


    SimileAjax.SortedArray=function(compare,initialArray){
    this._a=(initialArray instanceof Array)?initialArray:[];
    this._compare=compare;
    };

    SimileAjax.SortedArray.prototype.add=function(elmt){
    var sa=this;
    var index=this.find(function(elmt2){
    return sa._compare(elmt2,elmt);
    });

    if(index<this._a.length){
    this._a.splice(index,0,elmt);
    }else{
    this._a.push(elmt);
    }
    };

    SimileAjax.SortedArray.prototype.remove=function(elmt){
    var sa=this;
    var index=this.find(function(elmt2){
    return sa._compare(elmt2,elmt);
    });

    while(index<this._a.length&&this._compare(this._a[index],elmt)==0){
    if(this._a[index]==elmt){
    this._a.splice(index,1);
    return true;
    }else{
    index++;
    }
    }
    return false;
    };

    SimileAjax.SortedArray.prototype.removeAll=function(){
    this._a=[];
    };

    SimileAjax.SortedArray.prototype.elementAt=function(index){
    return this._a[index];
    };

    SimileAjax.SortedArray.prototype.length=function(){
    return this._a.length;
    };

    SimileAjax.SortedArray.prototype.find=function(compare){
    var a=0;
    var b=this._a.length;

    while(a<b){
    var mid=Math.floor((a+b)/2);
    var c=compare(this._a[mid]);
    if(mid==a){
    return c<0?a+1:a;
    }else if(c<0){
    a=mid;
    }else{
    b=mid;
    }
    }
    return a;
    };

    SimileAjax.SortedArray.prototype.getFirst=function(){
    return(this._a.length>0)?this._a[0]:null;
    };

    SimileAjax.SortedArray.prototype.getLast=function(){
    return(this._a.length>0)?this._a[this._a.length-1]:null;
    };



    SimileAjax.EventIndex=function(unit){
    var eventIndex=this;

    this._unit=(unit!=null)?unit:SimileAjax.NativeDateUnit;
    this._events=new SimileAjax.SortedArray(
    function(event1,event2){
    return eventIndex._unit.compare(event1.getStart(),event2.getStart());
    }
    );
    this._idToEvent={};
    this._indexed=true;
    };

    SimileAjax.EventIndex.prototype.getUnit=function(){
    return this._unit;
    };

    SimileAjax.EventIndex.prototype.getEvent=function(id){
    return this._idToEvent[id];
    };

    SimileAjax.EventIndex.prototype.add=function(evt){
    this._events.add(evt);
    this._idToEvent[evt.getID()]=evt;
    this._indexed=false;
    };

    SimileAjax.EventIndex.prototype.removeAll=function(){
    this._events.removeAll();
    this._idToEvent={};
    this._indexed=false;
    };

    SimileAjax.EventIndex.prototype.getCount=function(){
    return this._events.length();
    };

    SimileAjax.EventIndex.prototype.getIterator=function(startDate,endDate){
    if(!this._indexed){
    this._index();
    }
    return new SimileAjax.EventIndex._Iterator(this._events,startDate,endDate,this._unit);
    };

    SimileAjax.EventIndex.prototype.getReverseIterator=function(startDate,endDate){
    if(!this._indexed){
    this._index();
    }
    return new SimileAjax.EventIndex._ReverseIterator(this._events,startDate,endDate,this._unit);
    };

    SimileAjax.EventIndex.prototype.getAllIterator=function(){
    return new SimileAjax.EventIndex._AllIterator(this._events);
    };

    SimileAjax.EventIndex.prototype.getEarliestDate=function(){
    var evt=this._events.getFirst();
    return(evt==null)?null:evt.getStart();
    };

    SimileAjax.EventIndex.prototype.getLatestDate=function(){
    var evt=this._events.getLast();
    if(evt==null){
    return null;
    }

    if(!this._indexed){
    this._index();
    }

    var index=evt._earliestOverlapIndex;
    var date=this._events.elementAt(index).getEnd();
    for(var i=index+1;i<this._events.length();i++){
    date=this._unit.later(date,this._events.elementAt(i).getEnd());
    }

    return date;
    };

    SimileAjax.EventIndex.prototype._index=function(){


    var l=this._events.length();
    for(var i=0;i<l;i++){
    var evt=this._events.elementAt(i);
    evt._earliestOverlapIndex=i;
    }

    var toIndex=1;
    for(var i=0;i<l;i++){
    var evt=this._events.elementAt(i);
    var end=evt.getEnd();

    toIndex=Math.max(toIndex,i+1);
    while(toIndex<l){
    var evt2=this._events.elementAt(toIndex);
    var start2=evt2.getStart();

    if(this._unit.compare(start2,end)<0){
    evt2._earliestOverlapIndex=i;
    toIndex++;
    }else{
    break;
    }
    }
    }
    this._indexed=true;
    };

    SimileAjax.EventIndex._Iterator=function(events,startDate,endDate,unit){
    this._events=events;
    this._startDate=startDate;
    this._endDate=endDate;
    this._unit=unit;

    this._currentIndex=events.find(function(evt){
    return unit.compare(evt.getStart(),startDate);
    });
    if(this._currentIndex-1>=0){
    this._currentIndex=this._events.elementAt(this._currentIndex-1)._earliestOverlapIndex;
    }
    this._currentIndex--;

    this._maxIndex=events.find(function(evt){
    return unit.compare(evt.getStart(),endDate);
    });

    this._hasNext=false;
    this._next=null;
    this._findNext();
    };

    SimileAjax.EventIndex._Iterator.prototype={
    hasNext:function(){return this._hasNext;},
    next:function(){
    if(this._hasNext){
    var next=this._next;
    this._findNext();

    return next;
    }else{
    return null;
    }
    },
    _findNext:function(){
    var unit=this._unit;
    while((++this._currentIndex)<this._maxIndex){
    var evt=this._events.elementAt(this._currentIndex);
    if(unit.compare(evt.getStart(),this._endDate)<0&&
    unit.compare(evt.getEnd(),this._startDate)>0){

    this._next=evt;
    this._hasNext=true;
    return;
    }
    }
    this._next=null;
    this._hasNext=false;
    }
    };

    SimileAjax.EventIndex._ReverseIterator=function(events,startDate,endDate,unit){
    this._events=events;
    this._startDate=startDate;
    this._endDate=endDate;
    this._unit=unit;

    this._minIndex=events.find(function(evt){
    return unit.compare(evt.getStart(),startDate);
    });
    if(this._minIndex-1>=0){
    this._minIndex=this._events.elementAt(this._minIndex-1)._earliestOverlapIndex;
    }

    this._maxIndex=events.find(function(evt){
    return unit.compare(evt.getStart(),endDate);
    });

    this._currentIndex=this._maxIndex;
    this._hasNext=false;
    this._next=null;
    this._findNext();
    };

    SimileAjax.EventIndex._ReverseIterator.prototype={
    hasNext:function(){return this._hasNext;},
    next:function(){
    if(this._hasNext){
    var next=this._next;
    this._findNext();

    return next;
    }else{
    return null;
    }
    },
    _findNext:function(){
    var unit=this._unit;
    while((--this._currentIndex)>=this._minIndex){
    var evt=this._events.elementAt(this._currentIndex);
    if(unit.compare(evt.getStart(),this._endDate)<0&&
    unit.compare(evt.getEnd(),this._startDate)>0){

    this._next=evt;
    this._hasNext=true;
    return;
    }
    }
    this._next=null;
    this._hasNext=false;
    }
    };

    SimileAjax.EventIndex._AllIterator=function(events){
    this._events=events;
    this._index=0;
    };

    SimileAjax.EventIndex._AllIterator.prototype={
    hasNext:function(){
    return this._index<this._events.length();
    },
    next:function(){
    return this._index<this._events.length()?
    this._events.elementAt(this._index++):null;
    }
    };

    /* date-time.js */



    SimileAjax.DateTime=new Object();

    SimileAjax.DateTime.MILLISECOND=0;
    SimileAjax.DateTime.SECOND=1;
    SimileAjax.DateTime.MINUTE=2;
    SimileAjax.DateTime.HOUR=3;
    SimileAjax.DateTime.DAY=4;
    SimileAjax.DateTime.WEEK=5;
    SimileAjax.DateTime.MONTH=6;
    SimileAjax.DateTime.YEAR=7;
    SimileAjax.DateTime.DECADE=8;
    SimileAjax.DateTime.CENTURY=9;
    SimileAjax.DateTime.MILLENNIUM=10;

    SimileAjax.DateTime.EPOCH=-1;
    SimileAjax.DateTime.ERA=-2;


    SimileAjax.DateTime.gregorianUnitLengths=[];
    (function(){
    var d=SimileAjax.DateTime;
    var a=d.gregorianUnitLengths;

    a[d.MILLISECOND]=1;
    a[d.SECOND]=1000;
    a[d.MINUTE]=a[d.SECOND]*60;
    a[d.HOUR]=a[d.MINUTE]*60;
    a[d.DAY]=a[d.HOUR]*24;
    a[d.WEEK]=a[d.DAY]*7;
    a[d.MONTH]=a[d.DAY]*31;
    a[d.YEAR]=a[d.DAY]*365;
    a[d.DECADE]=a[d.YEAR]*10;
    a[d.CENTURY]=a[d.YEAR]*100;
    a[d.MILLENNIUM]=a[d.YEAR]*1000;
    })();

    SimileAjax.DateTime._dateRegexp=new RegExp(
    "^(-?)([0-9]{4})("+[
    "(-?([0-9]{2})(-?([0-9]{2}))?)",
    "(-?([0-9]{3}))",
    "(-?W([0-9]{2})(-?([1-7]))?)"
    ].join("|")+")?$"
    );
    SimileAjax.DateTime._timezoneRegexp=new RegExp(
    "Z|(([-+])([0-9]{2})(:?([0-9]{2}))?)$"
    );
    SimileAjax.DateTime._timeRegexp=new RegExp(
    "^([0-9]{2})(:?([0-9]{2})(:?([0-9]{2})(\.([0-9]+))?)?)?$"
    );


    SimileAjax.DateTime.setIso8601Date=function(dateObject,string){


    var d=string.match(SimileAjax.DateTime._dateRegexp);
    if(!d){
    throw new Error("Invalid date string: "+string);
    }

    var sign=(d[1]=="-")?-1:1;
    var year=sign*d[2];
    var month=d[5];
    var date=d[7];
    var dayofyear=d[9];
    var week=d[11];
    var dayofweek=(d[13])?d[13]:1;

    dateObject.setUTCFullYear(year);
    if(dayofyear){
    dateObject.setUTCMonth(0);
    dateObject.setUTCDate(Number(dayofyear));
    }else if(week){
    dateObject.setUTCMonth(0);
    dateObject.setUTCDate(1);
    var gd=dateObject.getUTCDay();
    var day=(gd)?gd:7;
    var offset=Number(dayofweek)+(7*Number(week));

    if(day<=4){
    dateObject.setUTCDate(offset+1-day);
    }else{
    dateObject.setUTCDate(offset+8-day);
    }
    }else{
    if(month){
    dateObject.setUTCDate(1);
    dateObject.setUTCMonth(month-1);
    }
    if(date){
    dateObject.setUTCDate(date);
    }
    }

    return dateObject;
    };


    SimileAjax.DateTime.setIso8601Time=function(dateObject,string){


    var d=string.match(SimileAjax.DateTime._timeRegexp);
    if(!d){
    SimileAjax.Debug.warn("Invalid time string: "+string);
    return false;
    }
    var hours=d[1];
    var mins=Number((d[3])?d[3]:0);
    var secs=(d[5])?d[5]:0;
    var ms=d[7]?(Number("0."+d[7])*1000):0;

    dateObject.setUTCHours(hours);
    dateObject.setUTCMinutes(mins);
    dateObject.setUTCSeconds(secs);
    dateObject.setUTCMilliseconds(ms);

    return dateObject;
    };


    SimileAjax.DateTime.timezoneOffset=new Date().getTimezoneOffset();


    SimileAjax.DateTime.setIso8601=function(dateObject,string){


    var offset=null;
    var comps=(string.indexOf("T")==-1)?string.split(" "):string.split("T");

    SimileAjax.DateTime.setIso8601Date(dateObject,comps[0]);
    if(comps.length==2){

    var d=comps[1].match(SimileAjax.DateTime._timezoneRegexp);
    if(d){
    if(d[0]=='Z'){
    offset=0;
    }else{
    offset=(Number(d[3])*60)+Number(d[5]);
    offset*=((d[2]=='-')?1:-1);
    }
    comps[1]=comps[1].substr(0,comps[1].length-d[0].length);
    }

    SimileAjax.DateTime.setIso8601Time(dateObject,comps[1]);
    }
    if(offset==null){
    offset=dateObject.getTimezoneOffset();
    }
    dateObject.setTime(dateObject.getTime()+offset*60000);

    return dateObject;
    };


    SimileAjax.DateTime.parseIso8601DateTime=function(string){
    try{
    return SimileAjax.DateTime.setIso8601(new Date(0),string);
    }catch(e){
    return null;
    }
    };


    SimileAjax.DateTime.parseGregorianDateTime=function(o){
    if(o==null){
    return null;
    }else if(o instanceof Date){
    return o;
    }

    var s=o.toString();
    if(s.length>0&&s.length<8){
    var space=s.indexOf(" ");
    if(space>0){
    var year=parseInt(s.substr(0,space));
    var suffix=s.substr(space+1);
    if(suffix.toLowerCase()=="bc"){
    year=1-year;
    }
    }else{
    var year=parseInt(s);
    }

    var d=new Date(0);
    d.setUTCFullYear(year);

    return d;
    }

    try{
    return new Date(Date.parse(s));
    }catch(e){
    return null;
    }
    };


    SimileAjax.DateTime.roundDownToInterval=function(date,intervalUnit,timeZone,multiple,firstDayOfWeek){
    //var timeShift=timeZone*
    //SimileAjax.DateTime.gregorianUnitLengths[SimileAjax.DateTime.HOUR];
    //
    //var doTimeShifting = function(newDate) {
//        newDate.setTime(newDate.getTime() - timeShift);
//        var offsetInMinutesDueToSummerTime = Math.abs(date.getTimezoneOffset() - newDate.getTimezoneOffset());
//        newDate.setTime(newDate.getTime() + offsetInMinutesDueToSummerTime * 60 * 1000);
    //};

    //var date2=new Date(date.getTime()+timeShift);
    var date2 = new Date(date.getTime());
    var clearInDay=function(d){
    d.setMilliseconds(0);
    d.setSeconds(0);
    d.setMinutes(0);
    d.setHours(0);
    };
    var clearInYear=function(d){
    clearInDay(d);
    d.setDate(1);
    d.setMonth(0);
    };

    switch(intervalUnit){
    case SimileAjax.DateTime.MILLISECOND:
    var x=date2.getMilliseconds();
    date2.setMilliseconds(x-(x%multiple));
    break;
    case SimileAjax.DateTime.SECOND:
    date2.setMilliseconds(0);

    var x=date2.getSeconds();
    date2.setSeconds(x-(x%multiple));
    break;
    case SimileAjax.DateTime.MINUTE:
    date2.setMilliseconds(0);
    date2.setSeconds(0);

    var x=date2.getMinutes();
    date2.setTime(date2.getTime()-
    (x%multiple)*SimileAjax.DateTime.gregorianUnitLengths[SimileAjax.DateTime.MINUTE]);
    break;
    case SimileAjax.DateTime.HOUR:
    date2.setMilliseconds(0);
    date2.setSeconds(0);
    date2.setMinutes(0);

    var x=date2.getHours();
    date2.setHours(x-(x%multiple));
    break;
    case SimileAjax.DateTime.DAY:
    clearInDay(date2);
    break;
    case SimileAjax.DateTime.WEEK:
    clearInDay(date2);
    var d=(date2.getDay()+7-firstDayOfWeek)%7;
    date2.setTime(date2.getTime()-
    d*SimileAjax.DateTime.gregorianUnitLengths[SimileAjax.DateTime.DAY]);
    break;
    case SimileAjax.DateTime.MONTH:
    clearInDay(date2);
    date2.setDate(1);

    var x=date2.getMonth();
    date2.setMonth(x-(x%multiple));
    break;
    case SimileAjax.DateTime.YEAR:
    clearInYear(date2);

    var x=date2.getFullYear();
    date2.setFullYear(x-(x%multiple));
    break;
    case SimileAjax.DateTime.DECADE:
    clearInYear(date2);
    date2.setFullYear(Math.floor(date2.getFullYear()/10)*10);
    break;
    case SimileAjax.DateTime.CENTURY:
    clearInYear(date2);
    date2.setYear(Math.floor(date2.getFullYear()/100)*100);
    break;
    case SimileAjax.DateTime.MILLENNIUM:
    clearInYear(date2);
    date2.setFullYear(Math.floor(date2.getFullYear()/1000)*1000);
    break;
    }
    //doTimeShifting(date2);
    date.setTime(date2.getTime());
    };


    SimileAjax.DateTime.roundUpToInterval=function(date,intervalUnit,timeZone,multiple,firstDayOfWeek){
    var originalTime=date.getTime();
    SimileAjax.DateTime.roundDownToInterval(date,intervalUnit,timeZone,multiple,firstDayOfWeek);
    if(date.getTime()<originalTime){
    date.setTime(date.getTime()+
    SimileAjax.DateTime.gregorianUnitLengths[intervalUnit]*multiple);
    }
    };


    SimileAjax.DateTime.incrementByInterval=function(date,intervalUnit){
    switch(intervalUnit){
    case SimileAjax.DateTime.MILLISECOND:
    date.setTime(date.getTime()+1)
    break;
    case SimileAjax.DateTime.SECOND:
    date.setTime(date.getTime()+1000);
    break;
    case SimileAjax.DateTime.MINUTE:
    date.setTime(date.getTime()+
    SimileAjax.DateTime.gregorianUnitLengths[SimileAjax.DateTime.MINUTE]);
    break;
    case SimileAjax.DateTime.HOUR:
    date.setTime(date.getTime()+
    SimileAjax.DateTime.gregorianUnitLengths[SimileAjax.DateTime.HOUR]);
    break;
    case SimileAjax.DateTime.DAY:
    date.setDate(date.getDate()+1);
    break;
    case SimileAjax.DateTime.WEEK:
    date.setDate(date.getDate()+7);
    break;
    case SimileAjax.DateTime.MONTH:
    date.setMonth(date.getMonth()+1);
    break;
    case SimileAjax.DateTime.YEAR:
    date.setFullYear(date.getFullYear()+1);
    break;
    case SimileAjax.DateTime.DECADE:
    date.setFullYear(date.getFullYear()+10);
    break;
    case SimileAjax.DateTime.CENTURY:
    date.setFullYear(date.getFullYear()+100);
    break;
    case SimileAjax.DateTime.MILLENNIUM:
    date.setFullYear(date.getFullYear()+1000);
    break;
    }
    };


    SimileAjax.DateTime.removeTimeZoneOffset=function(date,timeZone){
    return new Date(date.getTime()+
    timeZone*SimileAjax.DateTime.gregorianUnitLengths[SimileAjax.DateTime.HOUR]);
    };


    SimileAjax.DateTime.getTimezone=function(){
    var d=new Date();
    var utcHours=d.getUTCHours();
    var utcDay=d.getUTCDate();
    var localHours=d.getHours();
    var localDay=d.getDate();
    if(utcDay==localDay){
    return localHours-utcHours;
    }else if(utcHours>12){
    return 24-utcHours+localHours;
    }else{
    return-(utcHours+24-localHours);
    }
    };

    /* dom.js */



    SimileAjax.DOM=new Object();

    SimileAjax.DOM.registerEventWithObject=function(elmt,eventName,obj,handlerName){
    SimileAjax.DOM.registerEvent(elmt,eventName,function(elmt2,evt,target){
    return obj[handlerName].call(obj,elmt2,evt,target);
    });
    };

    SimileAjax.DOM.registerEvent=function(elmt,eventName,handler){
    var handler2=function(evt){
    evt=(evt)?evt:((event)?event:null);
    if(evt){
    var target=(evt.target)?
    evt.target:((evt.srcElement)?evt.srcElement:null);
    if(target){
    target=(target.nodeType==1||target.nodeType==9)?
    target:target.parentNode;
    }

    return handler(elmt,evt,target);
    }
    return true;
    }

    if(SimileAjax.Platform.browser.isIE){
    elmt.attachEvent("on"+eventName,handler2);
    }else{
    elmt.addEventListener(eventName,handler2,false);
    }
    };

    SimileAjax.DOM.getPageCoordinates=function(elmt){
    var left=0;
    var top=0;

    if(elmt.nodeType!=1){
    elmt=elmt.parentNode;
    }

    var elmt2=elmt;
    while(elmt2!=null){
    left+=elmt2.offsetLeft;
    top+=elmt2.offsetTop;
    elmt2=elmt2.offsetParent;
    }

    var body=document.body;
    while(elmt!=null&&elmt!=body){
    if("scrollLeft"in elmt){
    left-=elmt.scrollLeft;
    top-=elmt.scrollTop;
    }
    elmt=elmt.parentNode;
    }

    return{left:left,top:top};
    };

    SimileAjax.DOM.getSize=function(elmt){
    var w=this.getStyle(elmt,"width");
    var h=this.getStyle(elmt,"height");
    if(w.indexOf("px")>-1)w=w.replace("px","");
    if(h.indexOf("px")>-1)h=h.replace("px","");
    return{
    w:w,
    h:h
    }
    }

    SimileAjax.DOM.getStyle=function(elmt,styleProp){
    if(elmt.currentStyle){
    var style=elmt.currentStyle[styleProp];
    }else if(window.getComputedStyle){
    var style=document.defaultView.getComputedStyle(elmt,null).getPropertyValue(styleProp);
    }else{
    var style="";
    }
    return style;
    }

    SimileAjax.DOM.getEventRelativeCoordinates=function(evt,elmt){
    if(SimileAjax.Platform.browser.isIE){
    return{
    x:evt.offsetX,
    y:evt.offsetY
    };
    }else{
    var coords=SimileAjax.DOM.getPageCoordinates(elmt);
    return{
    x:evt.pageX-coords.left,
    y:evt.pageY-coords.top
    };
    }
    };

    SimileAjax.DOM.getEventPageCoordinates=function(evt){
    if(SimileAjax.Platform.browser.isIE){
    return{
    x:evt.clientX+document.body.scrollLeft,
    y:evt.clientY+document.body.scrollTop
    };
    }else{
    return{
    x:evt.pageX,
    y:evt.pageY
    };
    }
    };

    SimileAjax.DOM.hittest=function(x,y,except){
    return SimileAjax.DOM._hittest(document.body,x,y,except);
    };

    SimileAjax.DOM._hittest=function(elmt,x,y,except){
    var childNodes=elmt.childNodes;
    outer:for(var i=0;i<childNodes.length;i++){
    var childNode=childNodes[i];
    for(var j=0;j<except.length;j++){
    if(childNode==except[j]){
    continue outer;
    }
    }

    if(childNode.offsetWidth==0&&childNode.offsetHeight==0){

    var hitNode=SimileAjax.DOM._hittest(childNode,x,y,except);
    if(hitNode!=childNode){
    return hitNode;
    }
    }else{
    var top=0;
    var left=0;

    var node=childNode;
    while(node){
    top+=node.offsetTop;
    left+=node.offsetLeft;
    node=node.offsetParent;
    }

    if(left<=x&&top<=y&&(x-left)<childNode.offsetWidth&&(y-top)<childNode.offsetHeight){
    return SimileAjax.DOM._hittest(childNode,x,y,except);
    }else if(childNode.nodeType==1&&childNode.tagName=="TR"){

    var childNode2=SimileAjax.DOM._hittest(childNode,x,y,except);
    if(childNode2!=childNode){
    return childNode2;
    }
    }
    }
    }
    return elmt;
    };

    SimileAjax.DOM.cancelEvent=function(evt){
    evt.returnValue=false;
    evt.cancelBubble=true;
    if("preventDefault"in evt){
    evt.preventDefault();
    }
    };

    SimileAjax.DOM.appendClassName=function(elmt,className){
    var classes=elmt.className.split(" ");
    for(var i=0;i<classes.length;i++){
    if(classes[i]==className){
    return;
    }
    }
    classes.push(className);
    elmt.className=classes.join(" ");
    };

    SimileAjax.DOM.createInputElement=function(type){
    var div=document.createElement("div");
    div.innerHTML="<input type='"+type+"' />";

    return div.firstChild;
    };

    SimileAjax.DOM.createDOMFromTemplate=function(template){
    var result={};
    result.elmt=SimileAjax.DOM._createDOMFromTemplate(template,result,null);

    return result;
    };

    SimileAjax.DOM._createDOMFromTemplate=function(templateNode,result,parentElmt){
    if(templateNode==null){

    return null;
    }else if(typeof templateNode!="object"){
    var node=document.createTextNode(templateNode);
    if(parentElmt!=null){
    parentElmt.appendChild(node);
    }
    return node;
    }else{
    var elmt=null;
    if("tag"in templateNode){
    var tag=templateNode.tag;
    if(parentElmt!=null){
    if(tag=="tr"){
    elmt=parentElmt.insertRow(parentElmt.rows.length);
    }else if(tag=="td"){
    elmt=parentElmt.insertCell(parentElmt.cells.length);
    }
    }
    if(elmt==null){
    elmt=tag=="input"?
    SimileAjax.DOM.createInputElement(templateNode.type):
    document.createElement(tag);

    if(parentElmt!=null){
    parentElmt.appendChild(elmt);
    }
    }
    }else{
    elmt=templateNode.elmt;
    if(parentElmt!=null){
    parentElmt.appendChild(elmt);
    }
    }

    for(var attribute in templateNode){
    var value=templateNode[attribute];

    if(attribute=="field"){
    result[value]=elmt;

    }else if(attribute=="className"){
    elmt.className=value;
    }else if(attribute=="id"){
    elmt.id=value;
    }else if(attribute=="title"){
    elmt.title=value;
    }else if(attribute=="type"&&elmt.tagName=="input"){

    }else if(attribute=="style"){
    for(n in value){
    var v=value[n];
    if(n=="float"){
    n=SimileAjax.Platform.browser.isIE?"styleFloat":"cssFloat";
    }
    elmt.style[n]=v;
    }
    }else if(attribute=="children"){
    for(var i=0;i<value.length;i++){
    SimileAjax.DOM._createDOMFromTemplate(value[i],result,elmt);
    }
    }else if(attribute!="tag"&&attribute!="elmt"){
    elmt.setAttribute(attribute,value);
    }
    }
    return elmt;
    }
    }

    SimileAjax.DOM._cachedParent=null;
    SimileAjax.DOM.createElementFromString=function(s){
    if(SimileAjax.DOM._cachedParent==null){
    SimileAjax.DOM._cachedParent=document.createElement("div");
    }
    SimileAjax.DOM._cachedParent.innerHTML=s;
    return SimileAjax.DOM._cachedParent.firstChild;
    };

    SimileAjax.DOM.createDOMFromString=function(root,s,fieldElmts){
    var elmt=typeof root=="string"?document.createElement(root):root;
    elmt.innerHTML=s;

    var dom={elmt:elmt};
    SimileAjax.DOM._processDOMChildrenConstructedFromString(dom,elmt,fieldElmts!=null?fieldElmts:{});

    return dom;
    };

    SimileAjax.DOM._processDOMConstructedFromString=function(dom,elmt,fieldElmts){
    var id=elmt.id;
    if(id!=null&&id.length>0){
    elmt.removeAttribute("id");
    if(id in fieldElmts){
    var parentElmt=elmt.parentNode;
    parentElmt.insertBefore(fieldElmts[id],elmt);
    parentElmt.removeChild(elmt);

    dom[id]=fieldElmts[id];
    return;
    }else{
    dom[id]=elmt;
    }
    }

    if(elmt.hasChildNodes()){
    SimileAjax.DOM._processDOMChildrenConstructedFromString(dom,elmt,fieldElmts);
    }
    };

    SimileAjax.DOM._processDOMChildrenConstructedFromString=function(dom,elmt,fieldElmts){
    var node=elmt.firstChild;
    while(node!=null){
    var node2=node.nextSibling;
    if(node.nodeType==1){
    SimileAjax.DOM._processDOMConstructedFromString(dom,node,fieldElmts);
    }
    node=node2;
    }
    };


    /* graphics.js */



    SimileAjax.Graphics=new Object();


    SimileAjax.Graphics.pngIsTranslucent=(!SimileAjax.Platform.browser.isIE)||(SimileAjax.Platform.browser.majorVersion>6);


    SimileAjax.Graphics._createTranslucentImage1=function(url,verticalAlign){
    elmt=document.createElement("img");
    elmt.setAttribute("src",url);
    if(verticalAlign!=null){
    elmt.style.verticalAlign=verticalAlign;
    }
    return elmt;
    };
    SimileAjax.Graphics._createTranslucentImage2=function(url,verticalAlign){
    elmt=document.createElement("img");
    elmt.style.width="1px";
    elmt.style.height="1px";
    elmt.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"', sizingMethod='image')";
    elmt.style.verticalAlign=(verticalAlign!=null)?verticalAlign:"middle";
    return elmt;
    };


    SimileAjax.Graphics.createTranslucentImage=SimileAjax.Graphics.pngIsTranslucent?
    SimileAjax.Graphics._createTranslucentImage1:
    SimileAjax.Graphics._createTranslucentImage2;

    SimileAjax.Graphics._createTranslucentImageHTML1=function(url,verticalAlign){
    return"<img src=\""+url+"\""+
    (verticalAlign!=null?" style=\"vertical-align: "+verticalAlign+";\"":"")+
    " />";
    };
    SimileAjax.Graphics._createTranslucentImageHTML2=function(url,verticalAlign){
    var style=
    "width: 1px; height: 1px; "+
    "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"', sizingMethod='image');"+
    (verticalAlign!=null?" vertical-align: "+verticalAlign+";":"");

    return"<img src='"+url+"' style=\""+style+"\" />";
    };


    SimileAjax.Graphics.createTranslucentImageHTML=SimileAjax.Graphics.pngIsTranslucent?
    SimileAjax.Graphics._createTranslucentImageHTML1:
    SimileAjax.Graphics._createTranslucentImageHTML2;


    SimileAjax.Graphics.setOpacity=function(elmt,opacity){
    if(SimileAjax.Platform.browser.isIE){
    elmt.style.filter="progid:DXImageTransform.Microsoft.Alpha(Style=0,Opacity="+opacity+")";
    }else{
    var o=(opacity/100).toString();
    elmt.style.opacity=o;
    elmt.style.MozOpacity=o;
    }
    };


    SimileAjax.Graphics._bubbleMargins={
    top:33,
    bottom:42,
    left:33,
    right:40
    }


    SimileAjax.Graphics._arrowOffsets={
    top:0,
    bottom:9,
    left:1,
    right:8
    }

    SimileAjax.Graphics._bubblePadding=15;
    SimileAjax.Graphics._bubblePointOffset=6;
    SimileAjax.Graphics._halfArrowWidth=18;


    SimileAjax.Graphics.createBubbleForContentAndPoint=function(div,pageX,pageY,contentWidth,orientation){
    if(typeof contentWidth!="number"){
    contentWidth=300;
    }

    div.style.position="absolute";
    div.style.left="-5000px";
    div.style.top="0px";
    div.style.width=contentWidth+"px";
    document.body.appendChild(div);

    window.setTimeout(function(){
    var width=div.scrollWidth+10;
    var height=div.scrollHeight+10;

    var bubble=SimileAjax.Graphics.createBubbleForPoint(pageX,pageY,width,height,orientation);

    document.body.removeChild(div);
    div.style.position="static";
    div.style.left="";
    div.style.top="";
    div.style.width=width+"px";
    bubble.content.appendChild(div);
    },200);
    };


    SimileAjax.Graphics.createBubbleForPoint=function(pageX,pageY,contentWidth,contentHeight,orientation){
    function getWindowDims(){
    if(typeof window.innerHeight=='number'){
    return{w:window.innerWidth,h:window.innerHeight};
    }else if(document.documentElement&&document.documentElement.clientHeight){
    return{
    w:document.documentElement.clientWidth,
    h:document.documentElement.clientHeight
    };
    }else if(document.body&&document.body.clientHeight){
    return{
    w:document.body.clientWidth,
    h:document.body.clientHeight
    };
    }
    }

    var close=function(){
    if(!bubble._closed){
    document.body.removeChild(bubble._div);
    bubble._doc=null;
    bubble._div=null;
    bubble._content=null;
    bubble._closed=true;
    }
    }
    var bubble={
    _closed:false
    };

    var dims=getWindowDims();
    var docWidth=dims.w;
    var docHeight=dims.h;

    var margins=SimileAjax.Graphics._bubbleMargins;
    contentWidth=parseInt(contentWidth,10);
    contentHeight=parseInt(contentHeight,10);
    var bubbleWidth=margins.left+contentWidth+margins.right;
    var bubbleHeight=margins.top+contentHeight+margins.bottom;

    var pngIsTranslucent=SimileAjax.Graphics.pngIsTranslucent;
    var urlPrefix=SimileAjax.urlPrefix;

    var setImg=function(elmt,url,width,height){
    elmt.style.position="absolute";
    elmt.style.width=width+"px";
    elmt.style.height=height+"px";
    if(pngIsTranslucent){
    elmt.style.background="url("+url+")";
    }else{
    elmt.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"', sizingMethod='crop')";
    }
    }
    var div=document.createElement("div");
    div.style.width=bubbleWidth+"px";
    div.style.height=bubbleHeight+"px";
    div.style.position="absolute";
    div.style.zIndex=1000;

    var layer=SimileAjax.WindowManager.pushLayer(close,true,div);
    bubble._div=div;
    bubble.close=function(){SimileAjax.WindowManager.popLayer(layer);}

    var divInner=document.createElement("div");
    divInner.style.width="100%";
    divInner.style.height="100%";
    divInner.style.position="relative";
    div.appendChild(divInner);

    var createImg=function(url,left,top,width,height){
    var divImg=document.createElement("div");
    divImg.style.left=left+"px";
    divImg.style.top=top+"px";
    setImg(divImg,url,width,height);
    divInner.appendChild(divImg);
    }

    createImg(urlPrefix+"images/bubble-top-left.png",0,0,margins.left,margins.top);
    createImg(urlPrefix+"images/bubble-top.png",margins.left,0,contentWidth,margins.top);
    createImg(urlPrefix+"images/bubble-top-right.png",margins.left+contentWidth,0,margins.right,margins.top);

    createImg(urlPrefix+"images/bubble-left.png",0,margins.top,margins.left,contentHeight);
    createImg(urlPrefix+"images/bubble-right.png",margins.left+contentWidth,margins.top,margins.right,contentHeight);

    createImg(urlPrefix+"images/bubble-bottom-left.png",0,margins.top+contentHeight,margins.left,margins.bottom);
    createImg(urlPrefix+"images/bubble-bottom.png",margins.left,margins.top+contentHeight,contentWidth,margins.bottom);
    createImg(urlPrefix+"images/bubble-bottom-right.png",margins.left+contentWidth,margins.top+contentHeight,margins.right,margins.bottom);

    var divClose=document.createElement("div");
    divClose.style.left=(bubbleWidth-margins.right+SimileAjax.Graphics._bubblePadding-16-2)+"px";
    divClose.style.top=(margins.top-SimileAjax.Graphics._bubblePadding+1)+"px";
    divClose.style.cursor="pointer";
    setImg(divClose,urlPrefix+"images/close-button.png",16,16);
    SimileAjax.WindowManager.registerEventWithObject(divClose,"click",bubble,"close");
    divInner.appendChild(divClose);

    var divContent=document.createElement("div");
    divContent.style.position="absolute";
    divContent.style.left=margins.left+"px";
    divContent.style.top=margins.top+"px";
    divContent.style.width=contentWidth+"px";
    divContent.style.height=contentHeight+"px";
    divContent.style.overflow="auto";
    divContent.style.background="white";
    divInner.appendChild(divContent);
    bubble.content=divContent;

    (function(){
    if(pageX-SimileAjax.Graphics._halfArrowWidth-SimileAjax.Graphics._bubblePadding>0&&
    pageX+SimileAjax.Graphics._halfArrowWidth+SimileAjax.Graphics._bubblePadding<docWidth){

    var left=pageX-Math.round(contentWidth/2)-margins.left;
    left=pageX<(docWidth/2)?
    Math.max(left,-(margins.left-SimileAjax.Graphics._bubblePadding)):
    Math.min(left,docWidth+(margins.right-SimileAjax.Graphics._bubblePadding)-bubbleWidth);

    if((orientation&&orientation=="top")||(!orientation&&(pageY-SimileAjax.Graphics._bubblePointOffset-bubbleHeight>0))){
    var divImg=document.createElement("div");

    divImg.style.left=(pageX-SimileAjax.Graphics._halfArrowWidth-left)+"px";
    divImg.style.top=(margins.top+contentHeight)+"px";
    setImg(divImg,urlPrefix+"images/bubble-bottom-arrow.png",37,margins.bottom);
    divInner.appendChild(divImg);

    div.style.left=left+"px";
    div.style.top=(pageY-SimileAjax.Graphics._bubblePointOffset-bubbleHeight+
    SimileAjax.Graphics._arrowOffsets.bottom)+"px";

    return;
    }else if((orientation&&orientation=="bottom")||(!orientation&&(pageY+SimileAjax.Graphics._bubblePointOffset+bubbleHeight<docHeight))){
    var divImg=document.createElement("div");

    divImg.style.left=(pageX-SimileAjax.Graphics._halfArrowWidth-left)+"px";
    divImg.style.top="0px";
    setImg(divImg,urlPrefix+"images/bubble-top-arrow.png",37,margins.top);
    divInner.appendChild(divImg);

    div.style.left=left+"px";
    div.style.top=(pageY+SimileAjax.Graphics._bubblePointOffset-
    SimileAjax.Graphics._arrowOffsets.top)+"px";

    return;
    }
    }

    var top=pageY-Math.round(contentHeight/2)-margins.top;
    top=pageY<(docHeight/2)?
    Math.max(top,-(margins.top-SimileAjax.Graphics._bubblePadding)):
    Math.min(top,docHeight+(margins.bottom-SimileAjax.Graphics._bubblePadding)-bubbleHeight);

    if((orientation&&orientation=="left")||(!orientation&&(pageX-SimileAjax.Graphics._bubblePointOffset-bubbleWidth>0))){
    var divImg=document.createElement("div");

    divImg.style.left=(margins.left+contentWidth)+"px";
    divImg.style.top=(pageY-SimileAjax.Graphics._halfArrowWidth-top)+"px";
    setImg(divImg,urlPrefix+"images/bubble-right-arrow.png",margins.right,37);
    divInner.appendChild(divImg);

    div.style.left=(pageX-SimileAjax.Graphics._bubblePointOffset-bubbleWidth+
    SimileAjax.Graphics._arrowOffsets.right)+"px";
    div.style.top=top+"px";
    }else if((orientation&&orientation=="right")||(!orientation&&(pageX-SimileAjax.Graphics._bubblePointOffset-bubbleWidth<docWidth))){
    var divImg=document.createElement("div");

    divImg.style.left="0px";
    divImg.style.top=(pageY-SimileAjax.Graphics._halfArrowWidth-top)+"px";
    setImg(divImg,urlPrefix+"images/bubble-left-arrow.png",margins.left,37);
    divInner.appendChild(divImg);

    div.style.left=(pageX+SimileAjax.Graphics._bubblePointOffset-
    SimileAjax.Graphics._arrowOffsets.left)+"px";
    div.style.top=top+"px";
    }
    })();

    document.body.appendChild(div);

    return bubble;
    };


    SimileAjax.Graphics.createMessageBubble=function(doc){
    var containerDiv=doc.createElement("div");
    if(SimileAjax.Graphics.pngIsTranslucent){
    var topDiv=doc.createElement("div");
    topDiv.style.height="33px";
    topDiv.style.background="url("+SimileAjax.urlPrefix+"images/message-top-left.png) top left no-repeat";
    topDiv.style.paddingLeft="44px";
    containerDiv.appendChild(topDiv);

    var topRightDiv=doc.createElement("div");
    topRightDiv.style.height="33px";
    topRightDiv.style.background="url("+SimileAjax.urlPrefix+"images/message-top-right.png) top right no-repeat";
    topDiv.appendChild(topRightDiv);

    var middleDiv=doc.createElement("div");
    middleDiv.style.background="url("+SimileAjax.urlPrefix+"images/message-left.png) top left repeat-y";
    middleDiv.style.paddingLeft="44px";
    containerDiv.appendChild(middleDiv);

    var middleRightDiv=doc.createElement("div");
    middleRightDiv.style.background="url("+SimileAjax.urlPrefix+"images/message-right.png) top right repeat-y";
    middleRightDiv.style.paddingRight="44px";
    middleDiv.appendChild(middleRightDiv);

    var contentDiv=doc.createElement("div");
    middleRightDiv.appendChild(contentDiv);

    var bottomDiv=doc.createElement("div");
    bottomDiv.style.height="55px";
    bottomDiv.style.background="url("+SimileAjax.urlPrefix+"images/message-bottom-left.png) bottom left no-repeat";
    bottomDiv.style.paddingLeft="44px";
    containerDiv.appendChild(bottomDiv);

    var bottomRightDiv=doc.createElement("div");
    bottomRightDiv.style.height="55px";
    bottomRightDiv.style.background="url("+SimileAjax.urlPrefix+"images/message-bottom-right.png) bottom right no-repeat";
    bottomDiv.appendChild(bottomRightDiv);
    }else{
    containerDiv.style.border="2px solid #7777AA";
    containerDiv.style.padding="20px";
    containerDiv.style.background="white";
    SimileAjax.Graphics.setOpacity(containerDiv,90);

    var contentDiv=doc.createElement("div");
    containerDiv.appendChild(contentDiv);
    }

    return{
    containerDiv:containerDiv,
    contentDiv:contentDiv
    };
    };




    SimileAjax.Graphics.createAnimation=function(f,from,to,duration,cont){
    return new SimileAjax.Graphics._Animation(f,from,to,duration,cont);
    };

    SimileAjax.Graphics._Animation=function(f,from,to,duration,cont){
    this.f=f;
    this.cont=(typeof cont=="function")?cont:function(){};

    this.from=from;
    this.to=to;
    this.current=from;

    this.duration=duration;
    this.start=new Date().getTime();
    this.timePassed=0;
    };


    SimileAjax.Graphics._Animation.prototype.run=function(){
    var a=this;
    window.setTimeout(function(){a.step();},50);
    };


    SimileAjax.Graphics._Animation.prototype.step=function(){
    this.timePassed+=50;

    var timePassedFraction=this.timePassed/this.duration;
    var parameterFraction=-Math.cos(timePassedFraction*Math.PI)/2+0.5;
    var current=parameterFraction*(this.to-this.from)+this.from;

    try{
    this.f(current,current-this.current);
    }catch(e){
    }
    this.current=current;

    if(this.timePassed<this.duration){
    this.run();
    }else{
    this.f(this.to,0);
    this["cont"]();
    }
    };




    SimileAjax.Graphics.createStructuredDataCopyButton=function(image,width,height,createDataFunction){
    var div=document.createElement("div");
    div.style.position="relative";
    div.style.display="inline";
    div.style.width=width+"px";
    div.style.height=height+"px";
    div.style.overflow="hidden";
    div.style.margin="2px";

    if(SimileAjax.Graphics.pngIsTranslucent){
    div.style.background="url("+image+") no-repeat";
    }else{
    div.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+image+"', sizingMethod='image')";
    }

    var style;
    if(SimileAjax.Platform.browser.isIE){
    style="filter:alpha(opacity=0)";
    }else{
    style="opacity: 0";
    }
    div.innerHTML="<textarea rows='1' autocomplete='off' value='none' style='"+style+"' />";

    var textarea=div.firstChild;
    textarea.style.width=width+"px";
    textarea.style.height=height+"px";
    textarea.onmousedown=function(evt){
    evt=(evt)?evt:((event)?event:null);
    if(evt.button==2){
    textarea.value=createDataFunction();
    textarea.select();
    }
    };

    return div;
    };

    SimileAjax.Graphics.getFontRenderingContext=function(elmt,width){
    return new SimileAjax.Graphics._FontRenderingContext(elmt,width);
    };

    SimileAjax.Graphics._FontRenderingContext=function(elmt,width){
    this._elmt=elmt;
    this._elmt.style.visibility="hidden";
    if(typeof width=="string"){
    this._elmt.style.width=width;
    }else if(typeof width=="number"){
    this._elmt.style.width=width+"px";
    }
    };

    SimileAjax.Graphics._FontRenderingContext.prototype.dispose=function(){
    this._elmt=null;
    };

    SimileAjax.Graphics._FontRenderingContext.prototype.update=function(){
    this._elmt.innerHTML="A";
    this._lineHeight=this._elmt.offsetHeight;
    };

    SimileAjax.Graphics._FontRenderingContext.prototype.computeSize=function(text){
    this._elmt.innerHTML=text;
    return{
    width:this._elmt.offsetWidth,
    height:this._elmt.offsetHeight
    };
    };

    SimileAjax.Graphics._FontRenderingContext.prototype.getLineHeight=function(){
    return this._lineHeight;
    };


    /* history.js */



    SimileAjax.History={
    maxHistoryLength:10,
    historyFile:"__history__.html",
    enabled:true,

    _initialized:false,
    _listeners:new SimileAjax.ListenerQueue(),

    _actions:[],
    _baseIndex:0,
    _currentIndex:0,

    _plainDocumentTitle:document.title
    };

    SimileAjax.History.formatHistoryEntryTitle=function(actionLabel){
    return SimileAjax.History._plainDocumentTitle+" {"+actionLabel+"}";
    };

    SimileAjax.History.initialize=function(){
    if(SimileAjax.History._initialized){
    return;
    }

    if(SimileAjax.History.enabled){
    var iframe=document.createElement("iframe");
    iframe.id="simile-ajax-history";
    iframe.style.position="absolute";
    iframe.style.width="10px";
    iframe.style.height="10px";
    iframe.style.top="0px";
    iframe.style.left="0px";
    iframe.style.visibility="hidden";
    iframe.src=SimileAjax.History.historyFile+"?0";

    document.body.appendChild(iframe);
    SimileAjax.DOM.registerEvent(iframe,"load",SimileAjax.History._handleIFrameOnLoad);

    SimileAjax.History._iframe=iframe;
    }
    SimileAjax.History._initialized=true;
    };

    SimileAjax.History.addListener=function(listener){
    SimileAjax.History.initialize();

    SimileAjax.History._listeners.add(listener);
    };

    SimileAjax.History.removeListener=function(listener){
    SimileAjax.History.initialize();

    SimileAjax.History._listeners.remove(listener);
    };

    SimileAjax.History.addAction=function(action){
    SimileAjax.History.initialize();

    SimileAjax.History._listeners.fire("onBeforePerform",[action]);
    window.setTimeout(function(){
    try{
    action.perform();
    SimileAjax.History._listeners.fire("onAfterPerform",[action]);

    if(SimileAjax.History.enabled){
    SimileAjax.History._actions=SimileAjax.History._actions.slice(
    0,SimileAjax.History._currentIndex-SimileAjax.History._baseIndex);

    SimileAjax.History._actions.push(action);
    SimileAjax.History._currentIndex++;

    var diff=SimileAjax.History._actions.length-SimileAjax.History.maxHistoryLength;
    if(diff>0){
    SimileAjax.History._actions=SimileAjax.History._actions.slice(diff);
    SimileAjax.History._baseIndex+=diff;
    }

    try{
    SimileAjax.History._iframe.contentWindow.location.search=
    "?"+SimileAjax.History._currentIndex;
    }catch(e){

    var title=SimileAjax.History.formatHistoryEntryTitle(action.label);
    document.title=title;
    }
    }
    }catch(e){
    SimileAjax.Debug.exception(e,"Error adding action {"+action.label+"} to history");
    }
    },0);
    };

    SimileAjax.History.addLengthyAction=function(perform,undo,label){
    SimileAjax.History.addAction({
    perform:perform,
    undo:undo,
    label:label,
    uiLayer:SimileAjax.WindowManager.getBaseLayer(),
    lengthy:true
    });
    };

    SimileAjax.History._handleIFrameOnLoad=function(){


    try{
    var q=SimileAjax.History._iframe.contentWindow.location.search;
    var c=(q.length==0)?0:Math.max(0,parseInt(q.substr(1)));

    var finishUp=function(){
    var diff=c-SimileAjax.History._currentIndex;
    SimileAjax.History._currentIndex+=diff;
    SimileAjax.History._baseIndex+=diff;

    SimileAjax.History._iframe.contentWindow.location.search="?"+c;
    };

    if(c<SimileAjax.History._currentIndex){
    SimileAjax.History._listeners.fire("onBeforeUndoSeveral",[]);
    window.setTimeout(function(){
    while(SimileAjax.History._currentIndex>c&&
    SimileAjax.History._currentIndex>SimileAjax.History._baseIndex){

    SimileAjax.History._currentIndex--;

    var action=SimileAjax.History._actions[SimileAjax.History._currentIndex-SimileAjax.History._baseIndex];

    try{
    action.undo();
    }catch(e){
    SimileAjax.Debug.exception(e,"History: Failed to undo action {"+action.label+"}");
    }
    }

    SimileAjax.History._listeners.fire("onAfterUndoSeveral",[]);
    finishUp();
    },0);
    }else if(c>SimileAjax.History._currentIndex){
    SimileAjax.History._listeners.fire("onBeforeRedoSeveral",[]);
    window.setTimeout(function(){
    while(SimileAjax.History._currentIndex<c&&
    SimileAjax.History._currentIndex-SimileAjax.History._baseIndex<SimileAjax.History._actions.length){

    var action=SimileAjax.History._actions[SimileAjax.History._currentIndex-SimileAjax.History._baseIndex];

    try{
    action.perform();
    }catch(e){
    SimileAjax.Debug.exception(e,"History: Failed to redo action {"+action.label+"}");
    }

    SimileAjax.History._currentIndex++;
    }

    SimileAjax.History._listeners.fire("onAfterRedoSeveral",[]);
    finishUp();
    },0);
    }else{
    var index=SimileAjax.History._currentIndex-SimileAjax.History._baseIndex-1;
    var title=(index>=0&&index<SimileAjax.History._actions.length)?
    SimileAjax.History.formatHistoryEntryTitle(SimileAjax.History._actions[index].label):
    SimileAjax.History._plainDocumentTitle;

    SimileAjax.History._iframe.contentWindow.document.title=title;
    document.title=title;
    }
    }catch(e){

    }
    };

    SimileAjax.History.getNextUndoAction=function(){
    try{
    var index=SimileAjax.History._currentIndex-SimileAjax.History._baseIndex-1;
    return SimileAjax.History._actions[index];
    }catch(e){
    return null;
    }
    };

    SimileAjax.History.getNextRedoAction=function(){
    try{
    var index=SimileAjax.History._currentIndex-SimileAjax.History._baseIndex;
    return SimileAjax.History._actions[index];
    }catch(e){
    return null;
    }
    };


    /* html.js */



    SimileAjax.HTML=new Object();

    SimileAjax.HTML._e2uHash={};
    (function(){
    e2uHash=SimileAjax.HTML._e2uHash;
    e2uHash['nbsp']='\u00A0[space]';
    e2uHash['iexcl']='\u00A1';
    e2uHash['cent']='\u00A2';
    e2uHash['pound']='\u00A3';
    e2uHash['curren']='\u00A4';
    e2uHash['yen']='\u00A5';
    e2uHash['brvbar']='\u00A6';
    e2uHash['sect']='\u00A7';
    e2uHash['uml']='\u00A8';
    e2uHash['copy']='\u00A9';
    e2uHash['ordf']='\u00AA';
    e2uHash['laquo']='\u00AB';
    e2uHash['not']='\u00AC';
    e2uHash['shy']='\u00AD';
    e2uHash['reg']='\u00AE';
    e2uHash['macr']='\u00AF';
    e2uHash['deg']='\u00B0';
    e2uHash['plusmn']='\u00B1';
    e2uHash['sup2']='\u00B2';
    e2uHash['sup3']='\u00B3';
    e2uHash['acute']='\u00B4';
    e2uHash['micro']='\u00B5';
    e2uHash['para']='\u00B6';
    e2uHash['middot']='\u00B7';
    e2uHash['cedil']='\u00B8';
    e2uHash['sup1']='\u00B9';
    e2uHash['ordm']='\u00BA';
    e2uHash['raquo']='\u00BB';
    e2uHash['frac14']='\u00BC';
    e2uHash['frac12']='\u00BD';
    e2uHash['frac34']='\u00BE';
    e2uHash['iquest']='\u00BF';
    e2uHash['Agrave']='\u00C0';
    e2uHash['Aacute']='\u00C1';
    e2uHash['Acirc']='\u00C2';
    e2uHash['Atilde']='\u00C3';
    e2uHash['Auml']='\u00C4';
    e2uHash['Aring']='\u00C5';
    e2uHash['AElig']='\u00C6';
    e2uHash['Ccedil']='\u00C7';
    e2uHash['Egrave']='\u00C8';
    e2uHash['Eacute']='\u00C9';
    e2uHash['Ecirc']='\u00CA';
    e2uHash['Euml']='\u00CB';
    e2uHash['Igrave']='\u00CC';
    e2uHash['Iacute']='\u00CD';
    e2uHash['Icirc']='\u00CE';
    e2uHash['Iuml']='\u00CF';
    e2uHash['ETH']='\u00D0';
    e2uHash['Ntilde']='\u00D1';
    e2uHash['Ograve']='\u00D2';
    e2uHash['Oacute']='\u00D3';
    e2uHash['Ocirc']='\u00D4';
    e2uHash['Otilde']='\u00D5';
    e2uHash['Ouml']='\u00D6';
    e2uHash['times']='\u00D7';
    e2uHash['Oslash']='\u00D8';
    e2uHash['Ugrave']='\u00D9';
    e2uHash['Uacute']='\u00DA';
    e2uHash['Ucirc']='\u00DB';
    e2uHash['Uuml']='\u00DC';
    e2uHash['Yacute']='\u00DD';
    e2uHash['THORN']='\u00DE';
    e2uHash['szlig']='\u00DF';
    e2uHash['agrave']='\u00E0';
    e2uHash['aacute']='\u00E1';
    e2uHash['acirc']='\u00E2';
    e2uHash['atilde']='\u00E3';
    e2uHash['auml']='\u00E4';
    e2uHash['aring']='\u00E5';
    e2uHash['aelig']='\u00E6';
    e2uHash['ccedil']='\u00E7';
    e2uHash['egrave']='\u00E8';
    e2uHash['eacute']='\u00E9';
    e2uHash['ecirc']='\u00EA';
    e2uHash['euml']='\u00EB';
    e2uHash['igrave']='\u00EC';
    e2uHash['iacute']='\u00ED';
    e2uHash['icirc']='\u00EE';
    e2uHash['iuml']='\u00EF';
    e2uHash['eth']='\u00F0';
    e2uHash['ntilde']='\u00F1';
    e2uHash['ograve']='\u00F2';
    e2uHash['oacute']='\u00F3';
    e2uHash['ocirc']='\u00F4';
    e2uHash['otilde']='\u00F5';
    e2uHash['ouml']='\u00F6';
    e2uHash['divide']='\u00F7';
    e2uHash['oslash']='\u00F8';
    e2uHash['ugrave']='\u00F9';
    e2uHash['uacute']='\u00FA';
    e2uHash['ucirc']='\u00FB';
    e2uHash['uuml']='\u00FC';
    e2uHash['yacute']='\u00FD';
    e2uHash['thorn']='\u00FE';
    e2uHash['yuml']='\u00FF';
    e2uHash['quot']='\u0022';
    e2uHash['amp']='\u0026';
    e2uHash['lt']='\u003C';
    e2uHash['gt']='\u003E';
    e2uHash['OElig']='';
    e2uHash['oelig']='\u0153';
    e2uHash['Scaron']='\u0160';
    e2uHash['scaron']='\u0161';
    e2uHash['Yuml']='\u0178';
    e2uHash['circ']='\u02C6';
    e2uHash['tilde']='\u02DC';
    e2uHash['ensp']='\u2002';
    e2uHash['emsp']='\u2003';
    e2uHash['thinsp']='\u2009';
    e2uHash['zwnj']='\u200C';
    e2uHash['zwj']='\u200D';
    e2uHash['lrm']='\u200E';
    e2uHash['rlm']='\u200F';
    e2uHash['ndash']='\u2013';
    e2uHash['mdash']='\u2014';
    e2uHash['lsquo']='\u2018';
    e2uHash['rsquo']='\u2019';
    e2uHash['sbquo']='\u201A';
    e2uHash['ldquo']='\u201C';
    e2uHash['rdquo']='\u201D';
    e2uHash['bdquo']='\u201E';
    e2uHash['dagger']='\u2020';
    e2uHash['Dagger']='\u2021';
    e2uHash['permil']='\u2030';
    e2uHash['lsaquo']='\u2039';
    e2uHash['rsaquo']='\u203A';
    e2uHash['euro']='\u20AC';
    e2uHash['fnof']='\u0192';
    e2uHash['Alpha']='\u0391';
    e2uHash['Beta']='\u0392';
    e2uHash['Gamma']='\u0393';
    e2uHash['Delta']='\u0394';
    e2uHash['Epsilon']='\u0395';
    e2uHash['Zeta']='\u0396';
    e2uHash['Eta']='\u0397';
    e2uHash['Theta']='\u0398';
    e2uHash['Iota']='\u0399';
    e2uHash['Kappa']='\u039A';
    e2uHash['Lambda']='\u039B';
    e2uHash['Mu']='\u039C';
    e2uHash['Nu']='\u039D';
    e2uHash['Xi']='\u039E';
    e2uHash['Omicron']='\u039F';
    e2uHash['Pi']='\u03A0';
    e2uHash['Rho']='\u03A1';
    e2uHash['Sigma']='\u03A3';
    e2uHash['Tau']='\u03A4';
    e2uHash['Upsilon']='\u03A5';
    e2uHash['Phi']='\u03A6';
    e2uHash['Chi']='\u03A7';
    e2uHash['Psi']='\u03A8';
    e2uHash['Omega']='\u03A9';
    e2uHash['alpha']='\u03B1';
    e2uHash['beta']='\u03B2';
    e2uHash['gamma']='\u03B3';
    e2uHash['delta']='\u03B4';
    e2uHash['epsilon']='\u03B5';
    e2uHash['zeta']='\u03B6';
    e2uHash['eta']='\u03B7';
    e2uHash['theta']='\u03B8';
    e2uHash['iota']='\u03B9';
    e2uHash['kappa']='\u03BA';
    e2uHash['lambda']='\u03BB';
    e2uHash['mu']='\u03BC';
    e2uHash['nu']='\u03BD';
    e2uHash['xi']='\u03BE';
    e2uHash['omicron']='\u03BF';
    e2uHash['pi']='\u03C0';
    e2uHash['rho']='\u03C1';
    e2uHash['sigmaf']='\u03C2';
    e2uHash['sigma']='\u03C3';
    e2uHash['tau']='\u03C4';
    e2uHash['upsilon']='\u03C5';
    e2uHash['phi']='\u03C6';
    e2uHash['chi']='\u03C7';
    e2uHash['psi']='\u03C8';
    e2uHash['omega']='\u03C9';
    e2uHash['thetasym']='\u03D1';
    e2uHash['upsih']='\u03D2';
    e2uHash['piv']='\u03D6';
    e2uHash['bull']='\u2022';
    e2uHash['hellip']='\u2026';
    e2uHash['prime']='\u2032';
    e2uHash['Prime']='\u2033';
    e2uHash['oline']='\u203E';
    e2uHash['frasl']='\u2044';
    e2uHash['weierp']='\u2118';
    e2uHash['image']='\u2111';
    e2uHash['real']='\u211C';
    e2uHash['trade']='\u2122';
    e2uHash['alefsym']='\u2135';
    e2uHash['larr']='\u2190';
    e2uHash['uarr']='\u2191';
    e2uHash['rarr']='\u2192';
    e2uHash['darr']='\u2193';
    e2uHash['harr']='\u2194';
    e2uHash['crarr']='\u21B5';
    e2uHash['lArr']='\u21D0';
    e2uHash['uArr']='\u21D1';
    e2uHash['rArr']='\u21D2';
    e2uHash['dArr']='\u21D3';
    e2uHash['hArr']='\u21D4';
    e2uHash['forall']='\u2200';
    e2uHash['part']='\u2202';
    e2uHash['exist']='\u2203';
    e2uHash['empty']='\u2205';
    e2uHash['nabla']='\u2207';
    e2uHash['isin']='\u2208';
    e2uHash['notin']='\u2209';
    e2uHash['ni']='\u220B';
    e2uHash['prod']='\u220F';
    e2uHash['sum']='\u2211';
    e2uHash['minus']='\u2212';
    e2uHash['lowast']='\u2217';
    e2uHash['radic']='\u221A';
    e2uHash['prop']='\u221D';
    e2uHash['infin']='\u221E';
    e2uHash['ang']='\u2220';
    e2uHash['and']='\u2227';
    e2uHash['or']='\u2228';
    e2uHash['cap']='\u2229';
    e2uHash['cup']='\u222A';
    e2uHash['int']='\u222B';
    e2uHash['there4']='\u2234';
    e2uHash['sim']='\u223C';
    e2uHash['cong']='\u2245';
    e2uHash['asymp']='\u2248';
    e2uHash['ne']='\u2260';
    e2uHash['equiv']='\u2261';
    e2uHash['le']='\u2264';
    e2uHash['ge']='\u2265';
    e2uHash['sub']='\u2282';
    e2uHash['sup']='\u2283';
    e2uHash['nsub']='\u2284';
    e2uHash['sube']='\u2286';
    e2uHash['supe']='\u2287';
    e2uHash['oplus']='\u2295';
    e2uHash['otimes']='\u2297';
    e2uHash['perp']='\u22A5';
    e2uHash['sdot']='\u22C5';
    e2uHash['lceil']='\u2308';
    e2uHash['rceil']='\u2309';
    e2uHash['lfloor']='\u230A';
    e2uHash['rfloor']='\u230B';
    e2uHash['lang']='\u2329';
    e2uHash['rang']='\u232A';
    e2uHash['loz']='\u25CA';
    e2uHash['spades']='\u2660';
    e2uHash['clubs']='\u2663';
    e2uHash['hearts']='\u2665';
    e2uHash['diams']='\u2666';
    })();

    SimileAjax.HTML.deEntify=function(s){
    e2uHash=SimileAjax.HTML._e2uHash;

    var re=/&(\w+?);/;
    while(re.test(s)){
    var m=s.match(re);
    s=s.replace(re,e2uHash[m[1]]);
    }
    return s;
    };

    /* jquery-1.1.3.1.js */
    //conflict with zk3.0.1

    //eval(function(p,a,c,k,e,r){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)r[e(c)]=k[c]||e(c);k=[function(e){return r[e]}];e=function(){return'\\w+'};c=1};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p}('7(1g 18.6=="I"){18.I=18.I;u 6=q(a,c){7(18==9||!9.3X)v 14 6(a,c);v 9.3X(a,c)};7(1g $!="I")6.1I$=$;u $=6;6.11=6.8r={3X:q(a,c){a=a||P;7(6.16(a))v 14 6(P)[6.11.1G?"1G":"1W"](a);7(1g a=="1s"){u m=/^[^<]*(<(.|\\s)+>)[^>]*$/.1V(a);7(m)a=6.31([m[1]]);B v 14 6(c).1L(a)}v 9.4E(a.15==2b&&a||(a.3C||a.C&&a!=18&&!a.1q&&a[0]!=I&&a[0].1q)&&6.2L(a)||[a])},3C:"1.1.3.1",7W:q(){v 9.C},C:0,1M:q(a){v a==I?6.2L(9):9[a]},1Z:q(a){u b=6(a);b.5q=9;v b},4E:q(a){9.C=0;[].R.O(9,a);v 9},F:q(a,b){v 6.F(9,a,b)},2p:q(a){u b=-1;9.F(q(i){7(9==a)b=i});v b},1b:q(f,d,e){u c=f;7(f.15==33)7(d==I)v 9.C&&6[e||"1b"](9[0],f)||I;B{c={};c[f]=d}v 9.F(q(a){E(u b V c)6.1b(e?9.T:9,b,6.4H(9,c[b],e,a,b))})},1f:q(b,a){v 9.1b(b,a,"2z")},2A:q(e){7(1g e=="1s")v 9.2Y().3e(P.66(e));u t="";6.F(e||9,q(){6.F(9.2S,q(){7(9.1q!=8)t+=9.1q!=1?9.5R:6.11.2A([9])})});v t},8b:q(){u a,1S=19;v 9.F(q(){7(!a)a=6.31(1S,9.2O);u b=a[0].3s(K);9.L.2K(b,9);1v(b.1d)b=b.1d;b.4g(9)})},3e:q(){v 9.2F(19,K,1,q(a){9.4g(a)})},5w:q(){v 9.2F(19,K,-1,q(a){9.2K(a,9.1d)})},5t:q(){v 9.2F(19,N,1,q(a){9.L.2K(a,9)})},5s:q(){v 9.2F(19,N,-1,q(a){9.L.2K(a,9.1X)})},2U:q(){v 9.5q||6([])},1L:q(t){u b=6.3k(9,q(a){v 6.1L(t,a)});v 9.1Z(/[^+>] [^+>]/.17(t)||t.J("..")>-1?6.5g(b):b)},7x:q(e){u d=9.1A(9.1L("*"));d.F(q(){9.1I$1a={};E(u a V 9.$1a)9.1I$1a[a]=6.1c({},9.$1a[a])}).3U();u r=9.1Z(6.3k(9,q(a){v a.3s(e!=I?e:K)}));d.F(q(){u b=9.1I$1a;E(u a V b)E(u c V b[a])6.S.1A(9,a,b[a][c],b[a][c].W);9.1I$1a=H});v r},1i:q(t){v 9.1Z(6.16(t)&&6.2s(9,q(b,a){v t.O(b,[a])})||6.2x(t,9))},4Y:q(t){v 9.1Z(t.15==33&&6.2x(t,9,K)||6.2s(9,q(a){v(t.15==2b||t.3C)?6.2w(a,t)<0:a!=t}))},1A:q(t){v 9.1Z(6.1T(9.1M(),t.15==33?6(t).1M():t.C!=I&&(!t.Q||t.Q=="6Z")?t:[t]))},37:q(a){v a?6.2x(a,9).C>0:N},6R:q(a){v a==I?(9.C?9[0].2v:H):9.1b("2v",a)},3F:q(a){v a==I?(9.C?9[0].27:H):9.2Y().3e(a)},2F:q(f,d,g,e){u c=9.C>1,a;v 9.F(q(){7(!a){a=6.31(f,9.2O);7(g<0)a.6E()}u b=9;7(d&&6.Q(9,"1r")&&6.Q(a[0],"2V"))b=9.3R("1z")[0]||9.4g(P.5h("1z"));6.F(a,q(){e.O(b,[c?9.3s(K):9])})})}};6.1c=6.11.1c=q(){u c=19[0],a=1;7(19.C==1){c=9;a=0}u b;1v((b=19[a++])!=H)E(u i V b)c[i]=b[i];v c};6.1c({6n:q(){7(6.1I$)$=6.1I$;v 6},16:q(a){v!!a&&1g a!="1s"&&!a.Q&&a.15!=2b&&/q/i.17(a+"")},40:q(a){v a.4z&&a.2O&&!a.2O.4y},Q:q(b,a){v b.Q&&b.Q.1D()==a.1D()},F:q(a,b,c){7(a.C==I)E(u i V a)b.O(a[i],c||[i,a[i]]);B E(u i=0,4x=a.C;i<4x;i++)7(b.O(a[i],c||[i,a[i]])===N)1F;v a},4H:q(c,b,d,e,a){7(6.16(b))b=b.3D(c,[e]);u f=/z-?2p|5Y-?8p|1e|5U|8i-?1u/i;v b&&b.15==3y&&d=="2z"&&!f.17(a)?b+"4o":b},12:{1A:q(b,c){6.F(c.2R(/\\s+/),q(i,a){7(!6.12.3w(b.12,a))b.12+=(b.12?" ":"")+a})},1E:q(b,c){b.12=c!=I?6.2s(b.12.2R(/\\s+/),q(a){v!6.12.3w(c,a)}).5M(" "):""},3w:q(t,c){v 6.2w(c,(t.12||t).3v().2R(/\\s+/))>-1}},4m:q(e,o,f){E(u i V o){e.T["2N"+i]=e.T[i];e.T[i]=o[i]}f.O(e,[]);E(u i V o)e.T[i]=e.T["2N"+i]},1f:q(e,p){7(p=="1u"||p=="29"){u b={},3r,3p,d=["83","81","80","7Y"];6.F(d,q(){b["7V"+9]=0;b["7T"+9+"7S"]=0});6.4m(e,b,q(){7(6(e).37(\':4f\')){3r=e.7Q;3p=e.7O}B{e=6(e.3s(K)).1L(":4b").5v("2B").2U().1f({48:"1y",3i:"7L",U:"2h",7K:"0",7I:"0"}).5o(e.L)[0];u a=6.1f(e.L,"3i")||"3n";7(a=="3n")e.L.T.3i="7G";3r=e.7E;3p=e.7D;7(a=="3n")e.L.T.3i="3n";e.L.3q(e)}});v p=="1u"?3r:3p}v 6.2z(e,p)},2z:q(e,a,d){u g;7(a=="1e"&&6.M.1h){g=6.1b(e.T,"1e");v g==""?"1":g}7(a.3t(/3x/i))a=6.1U;7(!d&&e.T[a])g=e.T[a];B 7(P.3f&&P.3f.3Y){7(a.3t(/3x/i))a="3x";a=a.1o(/([A-Z])/g,"-$1").2H();u b=P.3f.3Y(e,H);7(b)g=b.57(a);B 7(a=="U")g="1P";B 6.4m(e,{U:"2h"},q(){u c=P.3f.3Y(9,"");g=c&&c.57(a)||""})}B 7(e.3S){u f=a.1o(/\\-(\\w)/g,q(m,c){v c.1D()});g=e.3S[a]||e.3S[f]}v g},31:q(a,c){u r=[];c=c||P;6.F(a,q(i,b){7(!b)v;7(b.15==3y)b=b.3v();7(1g b=="1s"){u s=6.2C(b).2H(),1x=c.5h("1x"),1N=[];u a=!s.J("<1H")&&[1,"<2y>","</2y>"]||!s.J("<7g")&&[1,"<52>","</52>"]||(!s.J("<7c")||!s.J("<1z")||!s.J("<7a")||!s.J("<78"))&&[1,"<1r>","</1r>"]||!s.J("<2V")&&[2,"<1r><1z>","</1z></1r>"]||(!s.J("<75")||!s.J("<74"))&&[3,"<1r><1z><2V>","</2V></1z></1r>"]||!s.J("<73")&&[2,"<1r><4W>","</4W></1r>"]||[0,"",""];1x.27=a[1]+b+a[2];1v(a[0]--)1x=1x.1d;7(6.M.1h){7(!s.J("<1r")&&s.J("<1z")<0)1N=1x.1d&&1x.1d.2S;B 7(a[1]=="<1r>"&&s.J("<1z")<0)1N=1x.2S;E(u n=1N.C-1;n>=0;--n)7(6.Q(1N[n],"1z")&&!1N[n].2S.C)1N[n].L.3q(1N[n])}b=6.2L(1x.2S)}7(0===b.C&&(!6.Q(b,"34")&&!6.Q(b,"2y")))v;7(b[0]==I||6.Q(b,"34")||b.71)r.R(b);B r=6.1T(r,b)});v r},1b:q(c,d,a){u e=6.40(c)?{}:6.3H;7(e[d]){7(a!=I)c[e[d]]=a;v c[e[d]]}B 7(a==I&&6.M.1h&&6.Q(c,"34")&&(d=="70"||d=="6Y"))v c.6W(d).5R;B 7(c.4z){7(a!=I)c.6U(d,a);7(6.M.1h&&/4M|2u/.17(d)&&!6.40(c))v c.35(d,2);v c.35(d)}B{7(d=="1e"&&6.M.1h){7(a!=I){c.5U=1;c.1i=(c.1i||"").1o(/4L\\([^)]*\\)/,"")+(39(a).3v()=="6M"?"":"4L(1e="+a*4X+")")}v c.1i?(39(c.1i.3t(/1e=([^)]*)/)[1])/4X).3v():""}d=d.1o(/-([a-z])/6K,q(z,b){v b.1D()});7(a!=I)c[d]=a;v c[d]}},2C:q(t){v t.1o(/^\\s+|\\s+$/g,"")},2L:q(a){u r=[];7(1g a!="6I")E(u i=0,26=a.C;i<26;i++)r.R(a[i]);B r=a.51(0);v r},2w:q(b,a){E(u i=0,26=a.C;i<26;i++)7(a[i]==b)v i;v-1},1T:q(a,b){E(u i=0;b[i];i++)a.R(b[i]);v a},5g:q(a){u r=[],3P=6.1k++;E(u i=0,4G=a.C;i<4G;i++)7(3P!=a[i].1k){a[i].1k=3P;r.R(a[i])}v r},1k:0,2s:q(c,b,d){7(1g b=="1s")b=14 45("a","i","v "+b);u a=[];E(u i=0,30=c.C;i<30;i++)7(!d&&b(c[i],i)||d&&!b(c[i],i))a.R(c[i]);v a},3k:q(c,b){7(1g b=="1s")b=14 45("a","v "+b);u d=[];E(u i=0,30=c.C;i<30;i++){u a=b(c[i],i);7(a!==H&&a!=I){7(a.15!=2b)a=[a];d=d.6v(a)}}v d}});14 q(){u b=6u.6t.2H();6.M={4D:(b.3t(/.+(?:6s|6q|6o|6m)[\\/: ]([\\d.]+)/)||[])[1],20:/5l/.17(b),2a:/2a/.17(b),1h:/1h/.17(b)&&!/2a/.17(b),3j:/3j/.17(b)&&!/(6h|5l)/.17(b)};6.6g=!6.M.1h||P.6f=="6c";6.1U=6.M.1h?"1U":"5x",6.3H={"E":"68","67":"12","3x":6.1U,5x:6.1U,1U:6.1U,27:"27",12:"12",2v:"2v",2r:"2r",2B:"2B",65:"63",2T:"2T",62:"5Z"}};6.F({4v:"a.L",4p:"6.4p(a)",8o:"6.22(a,2,\'1X\')",8n:"6.22(a,2,\'4t\')",8k:"6.4q(a.L.1d,a)",8h:"6.4q(a.1d)"},q(i,n){6.11[i]=q(a){u b=6.3k(9,n);7(a&&1g a=="1s")b=6.2x(a,b);v 9.1Z(b)}});6.F({5o:"3e",8g:"5w",2K:"5t",8f:"5s"},q(i,n){6.11[i]=q(){u a=19;v 9.F(q(){E(u j=0,26=a.C;j<26;j++)6(a[j])[n](9)})}});6.F({5v:q(a){6.1b(9,a,"");9.8d(a)},8c:q(c){6.12.1A(9,c)},88:q(c){6.12.1E(9,c)},87:q(c){6.12[6.12.3w(9,c)?"1E":"1A"](9,c)},1E:q(a){7(!a||6.1i(a,[9]).r.C)9.L.3q(9)},2Y:q(){1v(9.1d)9.3q(9.1d)}},q(i,n){6.11[i]=q(){v 9.F(n,19)}});6.F(["5Q","5P","5O","5N"],q(i,n){6.11[n]=q(a,b){v 9.1i(":"+n+"("+a+")",b)}});6.F(["1u","29"],q(i,n){6.11[n]=q(h){v h==I?(9.C?6.1f(9[0],n):H):9.1f(n,h.15==33?h:h+"4o")}});6.1c({4n:{"":"m[2]==\'*\'||6.Q(a,m[2])","#":"a.35(\'2m\')==m[2]",":":{5P:"i<m[3]-0",5O:"i>m[3]-0",22:"m[3]-0==i",5Q:"m[3]-0==i",2Q:"i==0",2P:"i==r.C-1",5L:"i%2==0",5K:"i%2","2Q-3u":"a.L.3R(\'*\')[0]==a","2P-3u":"6.22(a.L.5J,1,\'4t\')==a","86-3u":"!6.22(a.L.5J,2,\'4t\')",4v:"a.1d",2Y:"!a.1d",5N:"(a.5H||a.85||\'\').J(m[3])>=0",4f:\'"1y"!=a.G&&6.1f(a,"U")!="1P"&&6.1f(a,"48")!="1y"\',1y:\'"1y"==a.G||6.1f(a,"U")=="1P"||6.1f(a,"48")=="1y"\',84:"!a.2r",2r:"a.2r",2B:"a.2B",2T:"a.2T||6.1b(a,\'2T\')",2A:"\'2A\'==a.G",4b:"\'4b\'==a.G",5F:"\'5F\'==a.G",4l:"\'4l\'==a.G",5E:"\'5E\'==a.G",4k:"\'4k\'==a.G",5D:"\'5D\'==a.G",5C:"\'5C\'==a.G",1J:\'"1J"==a.G||6.Q(a,"1J")\',5B:"/5B|2y|82|1J/i.17(a.Q)"},"[":"6.1L(m[2],a).C"},5A:[/^\\[ *(@)([\\w-]+) *([!*$^~=]*) *(\'?"?)(.*?)\\4 *\\]/,/^(\\[)\\s*(.*?(\\[.*?\\])?[^[]*?)\\s*\\]/,/^(:)([\\w-]+)\\("?\'?(.*?(\\(.*?\\))?[^(]*?)"?\'?\\)/,14 3o("^([:.#]*)("+(6.2J=6.M.20&&6.M.4D<"3.0.0"?"\\\\w":"(?:[\\\\w\\7Z-\\7X*1I-]|\\\\\\\\.)")+"+)")],2x:q(a,c,b){u d,1K=[];1v(a&&a!=d){d=a;u f=6.1i(a,c,b);a=f.t.1o(/^\\s*,\\s*/,"");1K=b?c=f.r:6.1T(1K,f.r)}v 1K},1L:q(t,l){7(1g t!="1s")v[t];7(l&&!l.1q)l=H;l=l||P;7(!t.J("//")){l=l.4h;t=t.2G(2,t.C)}B 7(!t.J("/")&&!l.2O){l=l.4h;t=t.2G(1,t.C);7(t.J("/")>=1)t=t.2G(t.J("/"),t.C)}u b=[l],2j=[],2P;1v(t&&2P!=t){u r=[];2P=t;t=6.2C(t).1o(/^\\/\\//,"");u k=N;u g=14 3o("^[/>]\\\\s*("+6.2J+"+)");u m=g.1V(t);7(m){u o=m[1].1D();E(u i=0;b[i];i++)E(u c=b[i].1d;c;c=c.1X)7(c.1q==1&&(o=="*"||c.Q.1D()==o.1D()))r.R(c);b=r;t=t.1o(g,"");7(t.J(" ")==0)7R;k=K}B{g=/^((\\/?\\.\\.)|([>\\/+~]))\\s*([a-z]*)/i;7((m=g.1V(t))!=H){r=[];u o=m[4],1k=6.1k++;m=m[1];E(u j=0,2e=b.C;j<2e;j++)7(m.J("..")<0){u n=m=="~"||m=="+"?b[j].1X:b[j].1d;E(;n;n=n.1X)7(n.1q==1){7(m=="~"&&n.1k==1k)1F;7(!o||n.Q.1D()==o.1D()){7(m=="~")n.1k=1k;r.R(n)}7(m=="+")1F}}B r.R(b[j].L);b=r;t=6.2C(t.1o(g,""));k=K}}7(t&&!k){7(!t.J(",")){7(l==b[0])b.4e();2j=6.1T(2j,b);r=b=[l];t=" "+t.2G(1,t.C)}B{u h=14 3o("^("+6.2J+"+)(#)("+6.2J+"+)");u m=h.1V(t);7(m){m=[0,m[2],m[3],m[1]]}B{h=14 3o("^([#.]?)("+6.2J+"*)");m=h.1V(t)}m[2]=m[2].1o(/\\\\/g,"");u f=b[b.C-1];7(m[1]=="#"&&f&&f.4d){u p=f.4d(m[2]);7((6.M.1h||6.M.2a)&&p&&1g p.2m=="1s"&&p.2m!=m[2])p=6(\'[@2m="\'+m[2]+\'"]\',f)[0];b=r=p&&(!m[3]||6.Q(p,m[3]))?[p]:[]}B{E(u i=0;b[i];i++){u a=m[1]!=""||m[0]==""?"*":m[2];7(a=="*"&&b[i].Q.2H()=="7P")a="2E";r=6.1T(r,b[i].3R(a))}7(m[1]==".")r=6.4c(r,m[2]);7(m[1]=="#"){u e=[];E(u i=0;r[i];i++)7(r[i].35("2m")==m[2]){e=[r[i]];1F}r=e}b=r}t=t.1o(h,"")}}7(t){u d=6.1i(t,r);b=r=d.r;t=6.2C(d.t)}}7(t)b=[];7(b&&l==b[0])b.4e();2j=6.1T(2j,b);v 2j},4c:q(r,m,a){m=" "+m+" ";u b=[];E(u i=0;r[i];i++){u c=(" "+r[i].12+" ").J(m)>=0;7(!a&&c||a&&!c)b.R(r[i])}v b},1i:q(t,r,h){u d;1v(t&&t!=d){d=t;u p=6.5A,m;E(u i=0;p[i];i++){m=p[i].1V(t);7(m){t=t.7N(m[0].C);m[2]=m[2].1o(/\\\\/g,"");1F}}7(!m)1F;7(m[1]==":"&&m[2]=="4Y")r=6.1i(m[3],r,K).r;B 7(m[1]==".")r=6.4c(r,m[2],h);B 7(m[1]=="@"){u g=[],G=m[3];E(u i=0,2e=r.C;i<2e;i++){u a=r[i],z=a[6.3H[m[2]]||m[2]];7(z==H||/4M|2u/.17(m[2]))z=6.1b(a,m[2])||\'\';7((G==""&&!!z||G=="="&&z==m[5]||G=="!="&&z!=m[5]||G=="^="&&z&&!z.J(m[5])||G=="$="&&z.2G(z.C-m[5].C)==m[5]||(G=="*="||G=="~=")&&z.J(m[5])>=0)^h)g.R(a)}r=g}B 7(m[1]==":"&&m[2]=="22-3u"){u e=6.1k++,g=[],17=/(\\d*)n\\+?(\\d*)/.1V(m[3]=="5L"&&"2n"||m[3]=="5K"&&"2n+1"||!/\\D/.17(m[3])&&"n+"+m[3]||m[3]),2Q=(17[1]||1)-0,d=17[2]-0;E(u i=0,2e=r.C;i<2e;i++){u j=r[i],L=j.L;7(e!=L.1k){u c=1;E(u n=L.1d;n;n=n.1X)7(n.1q==1)n.4a=c++;L.1k=e}u b=N;7(2Q==1){7(d==0||j.4a==d)b=K}B 7((j.4a+d)%2Q==0)b=K;7(b^h)g.R(j)}r=g}B{u f=6.4n[m[1]];7(1g f!="1s")f=6.4n[m[1]][m[2]];49("f = q(a,i){v "+f+"}");r=6.2s(r,f,h)}}v{r:r,t:t}},4p:q(c){u b=[];u a=c.L;1v(a&&a!=P){b.R(a);a=a.L}v b},22:q(a,e,c,b){e=e||1;u d=0;E(;a;a=a[c])7(a.1q==1&&++d==e)1F;v a},4q:q(n,a){u r=[];E(;n;n=n.1X){7(n.1q==1&&(!a||n!=a))r.R(n)}v r}});6.S={1A:q(d,e,c,b){7(6.M.1h&&d.3m!=I)d=18;7(!c.1Q)c.1Q=9.1Q++;7(b!=I){u f=c;c=q(){v f.O(9,19)};c.W=b;c.1Q=f.1Q}7(!d.$1a)d.$1a={};7(!d.$1p)d.$1p=q(){u a;7(1g 6=="I"||6.S.47)v a;a=6.S.1p.O(d,19);v a};u g=d.$1a[e];7(!g){g=d.$1a[e]={};7(d.46)d.46(e,d.$1p,N);B d.7M("5r"+e,d.$1p)}g[c.1Q]=c;7(!9.Y[e])9.Y[e]=[];7(6.2w(d,9.Y[e])==-1)9.Y[e].R(d)},1Q:1,Y:{},1E:q(b,c,a){u d=b.$1a,1Y,2p;7(d){7(c&&c.G){a=c.44;c=c.G}7(!c){E(c V d)9.1E(b,c)}B 7(d[c]){7(a)3l d[c][a.1Q];B E(a V b.$1a[c])3l d[c][a];E(1Y V d[c])1F;7(!1Y){7(b.43)b.43(c,b.$1p,N);B b.7J("5r"+c,b.$1p);1Y=H;3l d[c];1v(9.Y[c]&&((2p=6.2w(b,9.Y[c]))>=0))3l 9.Y[c][2p]}}E(1Y V d)1F;7(!1Y)b.$1p=b.$1a=H}},1t:q(c,b,d){b=6.2L(b||[]);7(!d)6.F(9.Y[c]||[],q(){6.S.1t(c,b,9)});B{u a,1Y,11=6.16(d[c]||H);b.5p(9.42({G:c,1O:d}));7(6.16(d.$1p)&&(a=d.$1p.O(d,b))!==N)9.47=K;7(11&&a!==N&&!6.Q(d,\'a\'))d[c]();9.47=N}},1p:q(b){u a;b=6.S.42(b||18.S||{});u c=9.$1a&&9.$1a[b.G],1S=[].51.3D(19,1);1S.5p(b);E(u j V c){1S[0].44=c[j];1S[0].W=c[j].W;7(c[j].O(9,1S)===N){b.2d();b.2D();a=N}}7(6.M.1h)b.1O=b.2d=b.2D=b.44=b.W=H;v a},42:q(c){u a=c;c=6.1c({},a);c.2d=q(){7(a.2d)v a.2d();a.7H=N};c.2D=q(){7(a.2D)v a.2D();a.7F=K};7(!c.1O&&c.5n)c.1O=c.5n;7(6.M.20&&c.1O.1q==3)c.1O=a.1O.L;7(!c.41&&c.4j)c.41=c.4j==c.1O?c.7C:c.4j;7(c.5k==H&&c.5j!=H){u e=P.4h,b=P.4y;c.5k=c.5j+(e&&e.5i||b.5i);c.7z=c.7y+(e&&e.5f||b.5f)}7(!c.3h&&(c.5e||c.5d))c.3h=c.5e||c.5d;7(!c.5c&&c.5b)c.5c=c.5b;7(!c.3h&&c.1J)c.3h=(c.1J&1?1:(c.1J&2?3:(c.1J&4?2:0)));v c}};6.11.1c({3g:q(c,a,b){v c=="3z"?9.3Z(c,a,b):9.F(q(){6.S.1A(9,c,b||a,b&&a)})},3Z:q(d,b,c){v 9.F(q(){6.S.1A(9,d,q(a){6(9).3U(a);v(c||b).O(9,19)},c&&b)})},3U:q(a,b){v 9.F(q(){6.S.1E(9,a,b)})},1t:q(a,b){v 9.F(q(){6.S.1t(a,b,9)})},1R:q(){u a=19;v 9.5a(q(e){9.4u=0==9.4u?1:0;e.2d();v a[9.4u].O(9,[e])||N})},7w:q(f,g){q 3W(e){u p=e.41;1v(p&&p!=9)2g{p=p.L}25(e){p=9};7(p==9)v N;v(e.G=="3V"?f:g).O(9,[e])}v 9.3V(3W).59(3W)},1G:q(f){7(6.3d)f.O(P,[6]);B 6.2q.R(q(){v f.O(9,[6])});v 9}});6.1c({3d:N,2q:[],1G:q(){7(!6.3d){6.3d=K;7(6.2q){6.F(6.2q,q(){9.O(P)});6.2q=H}7(6.M.3j||6.M.2a)P.43("58",6.1G,N);7(!18.7v.C)6(18).1W(q(){6("#3T").1E()})}}});14 q(){6.F(("7u,7t,1W,7s,7r,3z,5a,7q,"+"7p,7o,7n,3V,59,7m,2y,"+"4k,7l,7k,7j,2c").2R(","),q(i,o){6.11[o]=q(f){v f?9.3g(o,f):9.1t(o)}});7(6.M.3j||6.M.2a)P.46("58",6.1G,N);B 7(6.M.1h){P.7i("<7h"+"7f 2m=3T 7e=K "+"2u=//:><\\/3b>");u a=P.4d("3T");7(a)a.7d=q(){7(9.3a!="1n")v;6.1G()};a=H}B 7(6.M.20)6.3N=3m(q(){7(P.3a=="79"||P.3a=="1n"){3M(6.3N);6.3N=H;6.1G()}},10);6.S.1A(18,"1W",6.1G)};7(6.M.1h)6(18).3Z("3z",q(){u a=6.S.Y;E(u b V a){u c=a[b],i=c.C;7(i&&b!=\'3z\')77 c[i-1]&&6.S.1E(c[i-1],b);1v(--i)}});6.11.1c({76:q(c,b,a){9.1W(c,b,a,1)},1W:q(g,d,c,e){7(6.16(g))v 9.3g("1W",g);c=c||q(){};u f="3K";7(d)7(6.16(d)){c=d;d=H}B{d=6.2E(d);f="50"}u h=9;6.2Z({1C:g,G:f,W:d,2t:e,1n:q(a,b){7(b=="28"||!e&&b=="4V")h.1b("27",a.3c).3J().F(c,[a.3c,b,a]);B c.O(h,[a.3c,b,a])}});v 9},72:q(){v 6.2E(9)},3J:q(){v 9.1L("3b").F(q(){7(9.2u)6.4U(9.2u);B 6.3I(9.2A||9.5H||9.27||"")}).2U()}});6.F("4T,4I,4S,4R,4Q,4P".2R(","),q(i,o){6.11[o]=q(f){v 9.3g(o,f)}});6.1c({1M:q(e,c,a,d,b){7(6.16(c)){a=c;c=H}v 6.2Z({G:"3K",1C:e,W:c,28:a,3G:d,2t:b})},6X:q(d,b,a,c){v 6.1M(d,b,a,c,1)},4U:q(b,a){v 6.1M(b,H,a,"3b")},6V:q(c,b,a){v 6.1M(c,b,a,"4N")},6T:q(d,b,a,c){7(6.16(b)){a=b;b={}}v 6.2Z({G:"50",1C:d,W:b,28:a,3G:c})},6S:q(a){6.36.21=a},6Q:q(a){6.1c(6.36,a)},36:{Y:K,G:"3K",21:0,4O:"6P/x-6O-34-6N",4K:K,38:K,W:H},32:{},2Z:q(s){s=6.1c({},6.36,s);7(s.W){7(s.4K&&1g s.W!="1s")s.W=6.2E(s.W);7(s.G.2H()=="1M"){s.1C+=((s.1C.J("?")>-1)?"&":"?")+s.W;s.W=H}}7(s.Y&&!6.3L++)6.S.1t("4T");u f=N;u h=18.4Z?14 4Z("6L.6J"):14 4J();h.7b(s.G,s.1C,s.38);7(s.W)h.3Q("6H-6G",s.4O);7(s.2t)h.3Q("6F-3O-6D",6.32[s.1C]||"6C, 6B 6A 6z 4r:4r:4r 6y");h.3Q("X-6x-6w","4J");7(s.56)s.56(h);7(s.Y)6.S.1t("4P",[h,s]);u g=q(d){7(h&&(h.3a==4||d=="21")){f=K;7(i){3M(i);i=H}u c;2g{c=6.54(h)&&d!="21"?s.2t&&6.4F(h,s.1C)?"4V":"28":"2c";7(c!="2c"){u b;2g{b=h.3E("53-3O")}25(e){}7(s.2t&&b)6.32[s.1C]=b;u a=6.55(h,s.3G);7(s.28)s.28(a,c);7(s.Y)6.S.1t("4Q",[h,s])}B 6.2X(s,h,c)}25(e){c="2c";6.2X(s,h,c,e)}7(s.Y)6.S.1t("4S",[h,s]);7(s.Y&&!--6.3L)6.S.1t("4I");7(s.1n)s.1n(h,c);7(s.38)h=H}};u i=3m(g,13);7(s.21>0)4C(q(){7(h){h.6r();7(!f)g("21")}},s.21);2g{h.6p(s.W)}25(e){6.2X(s,h,H,e)}7(!s.38)g();v h},2X:q(s,a,b,e){7(s.2c)s.2c(a,b,e);7(s.Y)6.S.1t("4R",[a,s,e])},3L:0,54:q(r){2g{v!r.23&&7A.7B=="4l:"||(r.23>=5u&&r.23<6l)||r.23==5m||6.M.20&&r.23==I}25(e){}v N},4F:q(a,c){2g{u b=a.3E("53-3O");v a.23==5m||b==6.32[c]||6.M.20&&a.23==I}25(e){}v N},55:q(r,b){u c=r.3E("6k-G");u a=!b&&c&&c.J("4B")>=0;a=b=="4B"||a?r.6j:r.3c;7(b=="3b")6.3I(a);7(b=="4N")a=49("("+a+")");7(b=="3F")6("<1x>").3F(a).3J();v a},2E:q(a){u s=[];7(a.15==2b||a.3C)6.F(a,q(){s.R(2l(9.6i)+"="+2l(9.2v))});B E(u j V a)7(a[j]&&a[j].15==2b)6.F(a[j],q(){s.R(2l(j)+"="+2l(9))});B s.R(2l(j)+"="+2l(a[j]));v s.5M("&")},3I:q(a){7(18.4A)18.4A(a);B 7(6.M.20)18.4C(a,0);B 49.3D(18,a)}});6.11.1c({1m:q(b,a){v b?9.1w({1u:"1m",29:"1m",1e:"1m"},b,a):9.1i(":1y").F(q(){9.T.U=9.2i?9.2i:"";7(6.1f(9,"U")=="1P")9.T.U="2h"}).2U()},1j:q(b,a){v b?9.1w({1u:"1j",29:"1j",1e:"1j"},b,a):9.1i(":4f").F(q(){9.2i=9.2i||6.1f(9,"U");7(9.2i=="1P")9.2i="2h";9.T.U="1P"}).2U()},5G:6.11.1R,1R:q(a,b){v 6.16(a)&&6.16(b)?9.5G(a,b):a?9.1w({1u:"1R",29:"1R",1e:"1R"},a,b):9.F(q(){6(9)[6(9).37(":1y")?"1m":"1j"]()})},6e:q(b,a){v 9.1w({1u:"1m"},b,a)},6d:q(b,a){v 9.1w({1u:"1j"},b,a)},6b:q(b,a){v 9.1w({1u:"1R"},b,a)},6a:q(b,a){v 9.1w({1e:"1m"},b,a)},69:q(b,a){v 9.1w({1e:"1j"},b,a)},7U:q(c,a,b){v 9.1w({1e:a},c,b)},1w:q(d,h,f,g){v 9.1l(q(){u c=6(9).37(":1y"),1H=6.5z(h,f,g),5y=9;E(u p V d){7(d[p]=="1j"&&c||d[p]=="1m"&&!c)v 6.16(1H.1n)&&1H.1n.O(9);7(p=="1u"||p=="29"){1H.U=6.1f(9,"U");1H.2f=9.T.2f}}7(1H.2f!=H)9.T.2f="1y";9.2k=6.1c({},d);6.F(d,q(a,b){u e=14 6.2M(5y,1H,a);7(b.15==3y)e.2W(e.1K(),b);B e[b=="1R"?c?"1m":"1j":b](d)})})},1l:q(a,b){7(!b){b=a;a="2M"}v 9.F(q(){7(!9.1l)9.1l={};7(!9.1l[a])9.1l[a]=[];9.1l[a].R(b);7(9.1l[a].C==1)b.O(9)})}});6.1c({5z:q(b,a,c){u d=b&&b.15==64?b:{1n:c||!c&&a||6.16(b)&&b,1B:b,2I:c&&a||a&&a.15!=45&&a||(6.2I.4i?"4i":"4w")};d.1B=(d.1B&&d.1B.15==3y?d.1B:{61:60,89:5u}[d.1B])||8a;d.2N=d.1n;d.1n=q(){6.5I(9,"2M");7(6.16(d.2N))d.2N.O(9)};v d},2I:{4w:q(p,n,b,a){v b+a*p},4i:q(p,n,b,a){v((-5W.5X(p*5W.8e)/2)+0.5)*a+b}},1l:{},5I:q(b,a){a=a||"2M";7(b.1l&&b.1l[a]){b.1l[a].4e();u f=b.1l[a][0];7(f)f.O(b)}},3B:[],2M:q(f,e,g){u z=9;u y=f.T;z.a=q(){7(e.3A)e.3A.O(f,[z.2o]);7(g=="1e")6.1b(y,"1e",z.2o);B{y[g]=8m(z.2o)+"4o";y.U="2h"}};z.5V=q(){v 39(6.1f(f,g))};z.1K=q(){u r=39(6.2z(f,g));v r&&r>-8l?r:z.5V()};z.2W=q(c,b){z.4s=(14 5T()).5S();z.2o=c;z.a();6.3B.R(q(){v z.3A(c,b)});7(6.3B.C==1){u d=3m(q(){u a=6.3B;E(u i=0;i<a.C;i++)7(!a[i]())a.8j(i--,1);7(!a.C)3M(d)},13)}};z.1m=q(){7(!f.24)f.24={};f.24[g]=6.1b(f.T,g);e.1m=K;z.2W(0,9.1K());7(g!="1e")y[g]="8q";6(f).1m()};z.1j=q(){7(!f.24)f.24={};f.24[g]=6.1b(f.T,g);e.1j=K;z.2W(9.1K(),0)};z.3A=q(a,c){u t=(14 5T()).5S();7(t>e.1B+z.4s){z.2o=c;z.a();7(f.2k)f.2k[g]=K;u b=K;E(u i V f.2k)7(f.2k[i]!==K)b=N;7(b){7(e.U!=H){y.2f=e.2f;y.U=e.U;7(6.1f(f,"U")=="1P")y.U="2h"}7(e.1j)y.U="1P";7(e.1j||e.1m)E(u p V f.2k)6.1b(y,p,f.24[p])}7(b&&6.16(e.1n))e.1n.O(f);v N}B{u n=t-9.4s;u p=n/e.1B;z.2o=6.2I[e.2I](p,n,a,(c-a),e.1B);z.a()}v K}}})}',62,524,'||||||jQuery|if||this|||||||||||||||||function||||var|return||||||else|length||for|each|type|null|undefined|indexOf|true|parentNode|browser|false|apply|document|nodeName|push|event|style|display|in|data||global|||fn|className||new|constructor|isFunction|test|window|arguments|events|attr|extend|firstChild|opacity|css|typeof|msie|filter|hide|mergeNum|queue|show|complete|replace|handle|nodeType|table|string|trigger|height|while|animate|div|hidden|tbody|add|duration|url|toUpperCase|remove|break|ready|opt|_|button|cur|find|get|tb|target|none|guid|toggle|args|merge|styleFloat|exec|load|nextSibling|ret|pushStack|safari|timeout|nth|status|orig|catch|al|innerHTML|success|width|opera|Array|error|preventDefault|rl|overflow|try|block|oldblock|done|curAnim|encodeURIComponent|id||now|index|readyList|disabled|grep|ifModified|src|value|inArray|multiFilter|select|curCSS|text|checked|trim|stopPropagation|param|domManip|substr|toLowerCase|easing|chars|insertBefore|makeArray|fx|old|ownerDocument|last|first|split|childNodes|selected|end|tr|custom|handleError|empty|ajax|el|clean|lastModified|String|form|getAttribute|ajaxSettings|is|async|parseFloat|readyState|script|responseText|isReady|append|defaultView|bind|which|position|mozilla|map|delete|setInterval|static|RegExp|oWidth|removeChild|oHeight|cloneNode|match|child|toString|has|float|Number|unload|step|timers|jquery|call|getResponseHeader|html|dataType|props|globalEval|evalScripts|GET|active|clearInterval|safariTimer|Modified|num|setRequestHeader|getElementsByTagName|currentStyle|__ie_init|unbind|mouseover|handleHover|init|getComputedStyle|one|isXMLDoc|relatedTarget|fix|removeEventListener|handler|Function|addEventListener|triggered|visibility|eval|nodeIndex|radio|classFilter|getElementById|shift|visible|appendChild|documentElement|swing|fromElement|submit|file|swap|expr|px|parents|sibling|00|startTime|previousSibling|lastToggle|parent|linear|ol|body|tagName|execScript|xml|setTimeout|version|setArray|httpNotModified|fl|prop|ajaxStop|XMLHttpRequest|processData|alpha|href|json|contentType|ajaxSend|ajaxSuccess|ajaxError|ajaxComplete|ajaxStart|getScript|notmodified|colgroup|100|not|ActiveXObject|POST|slice|fieldset|Last|httpSuccess|httpData|beforeSend|getPropertyValue|DOMContentLoaded|mouseout|click|ctrlKey|metaKey|keyCode|charCode|scrollTop|unique|createElement|scrollLeft|clientX|pageX|webkit|304|srcElement|appendTo|unshift|prevObject|on|after|before|200|removeAttr|prepend|cssFloat|self|speed|parse|input|reset|image|password|checkbox|_toggle|textContent|dequeue|lastChild|odd|even|join|contains|gt|lt|eq|nodeValue|getTime|Date|zoom|max|Math|cos|font|maxLength|600|slow|maxlength|readOnly|Object|readonly|createTextNode|class|htmlFor|fadeOut|fadeIn|slideToggle|CSS1Compat|slideUp|slideDown|compatMode|boxModel|compatible|name|responseXML|content|300|ie|noConflict|ra|send|it|abort|rv|userAgent|navigator|concat|With|Requested|GMT|1970|Jan|01|Thu|Since|reverse|If|Type|Content|array|XMLHTTP|ig|Microsoft|NaN|urlencoded|www|application|ajaxSetup|val|ajaxTimeout|post|setAttribute|getJSON|getAttributeNode|getIfModified|method|FORM|action|options|serialize|col|th|td|loadIfModified|do|colg|loaded|tfoot|open|thead|onreadystatechange|defer|ipt|leg|scr|write|keyup|keypress|keydown|change|mousemove|mouseup|mousedown|dblclick|scroll|resize|focus|blur|frames|hover|clone|clientY|pageY|location|protocol|toElement|clientWidth|clientHeight|cancelBubble|relative|returnValue|left|detachEvent|right|absolute|attachEvent|substring|offsetWidth|object|offsetHeight|continue|Width|border|fadeTo|padding|size|uFFFF|Left|u0128|Right|Bottom|textarea|Top|enabled|innerText|only|toggleClass|removeClass|fast|400|wrap|addClass|removeAttribute|PI|insertAfter|prependTo|children|line|splice|siblings|10000|parseInt|prev|next|weight|1px|prototype'.split('|'),0,{}))

    /* json.js */





    SimileAjax.JSON=new Object();

    (function(){
    var m={
    '\b':'\\b',
    '\t':'\\t',
    '\n':'\\n',
    '\f':'\\f',
    '\r':'\\r',
    '"':'\\"',
    '\\':'\\\\'
    };
    var s={
    array:function(x){
    var a=['['],b,f,i,l=x.length,v;
    for(i=0;i<l;i+=1){
    v=x[i];
    f=s[typeof v];
    if(f){
    v=f(v);
    if(typeof v=='string'){
    if(b){
    a[a.length]=',';
    }
    a[a.length]=v;
    b=true;
    }
    }
    }
    a[a.length]=']';
    return a.join('');
    },
    'boolean':function(x){
    return String(x);
    },
    'null':function(x){
    return"null";
    },
    number:function(x){
    return isFinite(x)?String(x):'null';
    },
    object:function(x){
    if(x){
    if(x instanceof Array){
    return s.array(x);
    }
    var a=['{'],b,f,i,v;
    for(i in x){
    v=x[i];
    f=s[typeof v];
    if(f){
    v=f(v);
    if(typeof v=='string'){
    if(b){
    a[a.length]=',';
    }
    a.push(s.string(i),':',v);
    b=true;
    }
    }
    }
    a[a.length]='}';
    return a.join('');
    }
    return'null';
    },
    string:function(x){
    if(/["\\\x00-\x1f]/.test(x)){
    x=x.replace(/([\x00-\x1f\\"])/g,function(a,b){
    var c=m[b];
    if(c){
    return c;
    }
    c=b.charCodeAt();
    return'\\u00'+
    Math.floor(c/16).toString(16)+
    (c%16).toString(16);
    });
    }
    return'"'+x+'"';
    }
    };

    SimileAjax.JSON.toJSONString=function(o){
    if(o instanceof Object){
    return s.object(o);
    }else if(o instanceof Array){
    return s.array(o);
    }else{
    return o.toString();
    }
    };

    SimileAjax.JSON.parseJSON=function(){
    try{
    return!(/[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]/.test(
    this.replace(/"(\\.|[^"\\])*"/g,'')))&&
    eval('('+this+')');
    }catch(e){
    return false;
    }
    };
    })();


    /* string.js */



    String.prototype.trim=function(){
    return this.replace(/^\s+|\s+$/g,'');
    };

    String.prototype.startsWith=function(prefix){
    return this.length>=prefix.length&&this.substr(0,prefix.length)==prefix;
    };

    String.prototype.endsWith=function(suffix){
    return this.length>=suffix.length&&this.substr(this.length-suffix.length)==suffix;
    };

    String.substitute=function(s,objects){
    var result="";
    var start=0;
    while(start<s.length-1){
    var percent=s.indexOf("%",start);
    if(percent<0||percent==s.length-1){
    break;
    }else if(percent>start&&s.charAt(percent-1)=="\\"){
    result+=s.substring(start,percent-1)+"%";
    start=percent+1;
    }else{
    var n=parseInt(s.charAt(percent+1));
    if(isNaN(n)||n>=objects.length){
    result+=s.substring(start,percent+2);
    }else{
    result+=s.substring(start,percent)+objects[n].toString();
    }
    start=percent+2;
    }
    }

    if(start<s.length){
    result+=s.substring(start);
    }
    return result;
    };


    /* units.js */





    SimileAjax.NativeDateUnit=new Object();



    SimileAjax.NativeDateUnit.makeDefaultValue=function(){

    return new Date();

    };



    SimileAjax.NativeDateUnit.cloneValue=function(v){

    return new Date(v.getTime());

    };



    SimileAjax.NativeDateUnit.getParser=function(format){

    if(typeof format=="string"){

    format=format.toLowerCase();

    }

    return(format=="iso8601"||format=="iso 8601")?

    SimileAjax.DateTime.parseIso8601DateTime:

    SimileAjax.DateTime.parseGregorianDateTime;

    };



    SimileAjax.NativeDateUnit.parseFromObject=function(o){

    return SimileAjax.DateTime.parseGregorianDateTime(o);

    };



    SimileAjax.NativeDateUnit.toNumber=function(v){

    return v.getTime();

    };



    SimileAjax.NativeDateUnit.fromNumber=function(n){

    return new Date(n);

    };



    SimileAjax.NativeDateUnit.compare=function(v1,v2){

    var n1,n2;

    if(typeof v1=="object"){

    n1=v1.getTime();

    }else{

    n1=Number(v1);

    }

    if(typeof v2=="object"){

    n2=v2.getTime();

    }else{

    n2=Number(v2);

    }



    return n1-n2;

    };



    SimileAjax.NativeDateUnit.earlier=function(v1,v2){

    return SimileAjax.NativeDateUnit.compare(v1,v2)<0?v1:v2;

    };



    SimileAjax.NativeDateUnit.later=function(v1,v2){

    return SimileAjax.NativeDateUnit.compare(v1,v2)>0?v1:v2;

    };



    SimileAjax.NativeDateUnit.change=function(v,n){

    return new Date(v.getTime()+n);

    };





    /* window-manager.js */




    SimileAjax.WindowManager={
    _initialized:false,
    _listeners:[],

    _draggedElement:null,
    _draggedElementCallback:null,
    _dropTargetHighlightElement:null,
    _lastCoords:null,
    _ghostCoords:null,
    _draggingMode:"",
    _dragging:false,

    _layers:[]
    };

    SimileAjax.WindowManager.initialize=function(){
    if(SimileAjax.WindowManager._initialized){
    return;
    }

    SimileAjax.DOM.registerEvent(document.body,"mousedown",SimileAjax.WindowManager._onBodyMouseDown);
    SimileAjax.DOM.registerEvent(document.body,"mousemove",SimileAjax.WindowManager._onBodyMouseMove);
    SimileAjax.DOM.registerEvent(document.body,"mouseup",SimileAjax.WindowManager._onBodyMouseUp);
    SimileAjax.DOM.registerEvent(document,"keydown",SimileAjax.WindowManager._onBodyKeyDown);
    SimileAjax.DOM.registerEvent(document,"keyup",SimileAjax.WindowManager._onBodyKeyUp);

    SimileAjax.WindowManager._layers.push({index:0});

    SimileAjax.WindowManager._historyListener={
    onBeforeUndoSeveral:function(){},
    onAfterUndoSeveral:function(){},
    onBeforeUndo:function(){},
    onAfterUndo:function(){},

    onBeforeRedoSeveral:function(){},
    onAfterRedoSeveral:function(){},
    onBeforeRedo:function(){},
    onAfterRedo:function(){}
    };
    SimileAjax.History.addListener(SimileAjax.WindowManager._historyListener);

    SimileAjax.WindowManager._initialized=true;
    };

    SimileAjax.WindowManager.getBaseLayer=function(){
    SimileAjax.WindowManager.initialize();
    return SimileAjax.WindowManager._layers[0];
    };

    SimileAjax.WindowManager.getHighestLayer=function(){
    SimileAjax.WindowManager.initialize();
    return SimileAjax.WindowManager._layers[SimileAjax.WindowManager._layers.length-1];
    };

    SimileAjax.WindowManager.registerEventWithObject=function(elmt,eventName,obj,handlerName,layer){
    SimileAjax.WindowManager.registerEvent(
    elmt,
    eventName,
    function(elmt2,evt,target){
    return obj[handlerName].call(obj,elmt2,evt,target);
    },
    layer
    );
    };

    SimileAjax.WindowManager.registerEvent=function(elmt,eventName,handler,layer){
    if(layer==null){
    layer=SimileAjax.WindowManager.getHighestLayer();
    }

    var handler2=function(elmt,evt,target){
    if(SimileAjax.WindowManager._canProcessEventAtLayer(layer)){
    SimileAjax.WindowManager._popToLayer(layer.index);
    try{
    handler(elmt,evt,target);
    }catch(e){
    SimileAjax.Debug.exception(e);
    }
    }
    SimileAjax.DOM.cancelEvent(evt);
    return false;
    }

    SimileAjax.DOM.registerEvent(elmt,eventName,handler2);
    };

    SimileAjax.WindowManager.pushLayer=function(f,ephemeral,elmt){
    var layer={onPop:f,index:SimileAjax.WindowManager._layers.length,ephemeral:(ephemeral),elmt:elmt};
    SimileAjax.WindowManager._layers.push(layer);

    return layer;
    };

    SimileAjax.WindowManager.popLayer=function(layer){
    for(var i=1;i<SimileAjax.WindowManager._layers.length;i++){
    if(SimileAjax.WindowManager._layers[i]==layer){
    SimileAjax.WindowManager._popToLayer(i-1);
    break;
    }
    }
    };

    SimileAjax.WindowManager.popAllLayers=function(){
    SimileAjax.WindowManager._popToLayer(0);
    };

    SimileAjax.WindowManager.registerForDragging=function(elmt,callback,layer){
    SimileAjax.WindowManager.registerEvent(
    elmt,
    "mousedown",
    function(elmt,evt,target){
    SimileAjax.WindowManager._handleMouseDown(elmt,evt,callback);
    },
    layer
    );
    };

    SimileAjax.WindowManager._popToLayer=function(level){
    while(level+1<SimileAjax.WindowManager._layers.length){
    try{
    var layer=SimileAjax.WindowManager._layers.pop();
    if(layer.onPop!=null){
    layer.onPop();
    }
    }catch(e){
    }
    }
    };

    SimileAjax.WindowManager._canProcessEventAtLayer=function(layer){
    if(layer.index==(SimileAjax.WindowManager._layers.length-1)){
    return true;
    }
    for(var i=layer.index+1;i<SimileAjax.WindowManager._layers.length;i++){
    if(!SimileAjax.WindowManager._layers[i].ephemeral){
    return false;
    }
    }
    return true;
    };

    SimileAjax.WindowManager.cancelPopups=function(evt){
    var evtCoords=(evt)?SimileAjax.DOM.getEventPageCoordinates(evt):{x:-1,y:-1};

    var i=SimileAjax.WindowManager._layers.length-1;
    while(i>0&&SimileAjax.WindowManager._layers[i].ephemeral){
    var layer=SimileAjax.WindowManager._layers[i];
    if(layer.elmt!=null){
    var elmt=layer.elmt;
    var elmtCoords=SimileAjax.DOM.getPageCoordinates(elmt);
    if(evtCoords.x>=elmtCoords.left&&evtCoords.x<(elmtCoords.left+elmt.offsetWidth)&&
    evtCoords.y>=elmtCoords.top&&evtCoords.y<(elmtCoords.top+elmt.offsetHeight)){
    break;
    }
    }
    i--;
    }
    SimileAjax.WindowManager._popToLayer(i);
    };

    SimileAjax.WindowManager._onBodyMouseDown=function(elmt,evt,target){
    if(!("eventPhase"in evt)||evt.eventPhase==evt.BUBBLING_PHASE){
    SimileAjax.WindowManager.cancelPopups(evt);
    }
    };

    SimileAjax.WindowManager._handleMouseDown=function(elmt,evt,callback){
    SimileAjax.WindowManager._draggedElement=elmt;
    SimileAjax.WindowManager._draggedElementCallback=callback;
    SimileAjax.WindowManager._lastCoords={x:evt.clientX,y:evt.clientY};

    SimileAjax.DOM.cancelEvent(evt);
    return false;
    };

    SimileAjax.WindowManager._onBodyKeyDown=function(elmt,evt,target){
    if(SimileAjax.WindowManager._dragging){
    if(evt.keyCode==27){
    SimileAjax.WindowManager._cancelDragging();
    }else if((evt.keyCode==17||evt.keyCode==16)&&SimileAjax.WindowManager._draggingMode!="copy"){
    SimileAjax.WindowManager._draggingMode="copy";

    var img=SimileAjax.Graphics.createTranslucentImage(SimileAjax.urlPrefix+"images/copy.png");
    img.style.position="absolute";
    img.style.left=(SimileAjax.WindowManager._ghostCoords.left-16)+"px";
    img.style.top=(SimileAjax.WindowManager._ghostCoords.top)+"px";
    document.body.appendChild(img);

    SimileAjax.WindowManager._draggingModeIndicatorElmt=img;
    }
    }
    };

    SimileAjax.WindowManager._onBodyKeyUp=function(elmt,evt,target){
    if(SimileAjax.WindowManager._dragging){
    if(evt.keyCode==17||evt.keyCode==16){
    SimileAjax.WindowManager._draggingMode="";
    if(SimileAjax.WindowManager._draggingModeIndicatorElmt!=null){
    document.body.removeChild(SimileAjax.WindowManager._draggingModeIndicatorElmt);
    SimileAjax.WindowManager._draggingModeIndicatorElmt=null;
    }
    }
    }
    };

    SimileAjax.WindowManager._onBodyMouseMove=function(elmt,evt,target){
    if(SimileAjax.WindowManager._draggedElement!=null){
    var callback=SimileAjax.WindowManager._draggedElementCallback;

    var lastCoords=SimileAjax.WindowManager._lastCoords;
    var diffX=evt.clientX-lastCoords.x;
    var diffY=evt.clientY-lastCoords.y;

    if(!SimileAjax.WindowManager._dragging){
    if(Math.abs(diffX)>5||Math.abs(diffY)>5){
    try{
    if("onDragStart"in callback){
    callback.onDragStart();
    }

    if("ghost"in callback&&callback.ghost){
    var draggedElmt=SimileAjax.WindowManager._draggedElement;

    SimileAjax.WindowManager._ghostCoords=SimileAjax.DOM.getPageCoordinates(draggedElmt);
    SimileAjax.WindowManager._ghostCoords.left+=diffX;
    SimileAjax.WindowManager._ghostCoords.top+=diffY;

    var ghostElmt=draggedElmt.cloneNode(true);
    ghostElmt.style.position="absolute";
    ghostElmt.style.left=SimileAjax.WindowManager._ghostCoords.left+"px";
    ghostElmt.style.top=SimileAjax.WindowManager._ghostCoords.top+"px";
    ghostElmt.style.zIndex=1000;
    SimileAjax.Graphics.setOpacity(ghostElmt,50);

    document.body.appendChild(ghostElmt);
    callback._ghostElmt=ghostElmt;
    }

    SimileAjax.WindowManager._dragging=true;
    SimileAjax.WindowManager._lastCoords={x:evt.clientX,y:evt.clientY};

    document.body.focus();
    }catch(e){
    SimileAjax.Debug.exception("WindowManager: Error handling mouse down",e);
    SimileAjax.WindowManager._cancelDragging();
    }
    }
    }else{
    try{
    SimileAjax.WindowManager._lastCoords={x:evt.clientX,y:evt.clientY};

    if("onDragBy"in callback){
    callback.onDragBy(diffX,diffY);
    }

    if("_ghostElmt"in callback){
    var ghostElmt=callback._ghostElmt;

    SimileAjax.WindowManager._ghostCoords.left+=diffX;
    SimileAjax.WindowManager._ghostCoords.top+=diffY;

    ghostElmt.style.left=SimileAjax.WindowManager._ghostCoords.left+"px";
    ghostElmt.style.top=SimileAjax.WindowManager._ghostCoords.top+"px";
    if(SimileAjax.WindowManager._draggingModeIndicatorElmt!=null){
    var indicatorElmt=SimileAjax.WindowManager._draggingModeIndicatorElmt;

    indicatorElmt.style.left=(SimileAjax.WindowManager._ghostCoords.left-16)+"px";
    indicatorElmt.style.top=SimileAjax.WindowManager._ghostCoords.top+"px";
    }

    if("droppable"in callback&&callback.droppable){
    var coords=SimileAjax.DOM.getEventPageCoordinates(evt);
    var target=SimileAjax.DOM.hittest(
    coords.x,coords.y,
    [SimileAjax.WindowManager._ghostElmt,
    SimileAjax.WindowManager._dropTargetHighlightElement
    ]
    );
    target=SimileAjax.WindowManager._findDropTarget(target);

    if(target!=SimileAjax.WindowManager._potentialDropTarget){
    if(SimileAjax.WindowManager._dropTargetHighlightElement!=null){
    document.body.removeChild(SimileAjax.WindowManager._dropTargetHighlightElement);

    SimileAjax.WindowManager._dropTargetHighlightElement=null;
    SimileAjax.WindowManager._potentialDropTarget=null;
    }

    var droppable=false;
    if(target!=null){
    if((!("canDropOn"in callback)||callback.canDropOn(target))&&
    (!("canDrop"in target)||target.canDrop(SimileAjax.WindowManager._draggedElement))){

    droppable=true;
    }
    }

    if(droppable){
    var border=4;
    var targetCoords=SimileAjax.DOM.getPageCoordinates(target);
    var highlight=document.createElement("div");
    highlight.style.border=border+"px solid yellow";
    highlight.style.backgroundColor="yellow";
    highlight.style.position="absolute";
    highlight.style.left=targetCoords.left+"px";
    highlight.style.top=targetCoords.top+"px";
    highlight.style.width=(target.offsetWidth-border*2)+"px";
    highlight.style.height=(target.offsetHeight-border*2)+"px";
    SimileAjax.Graphics.setOpacity(highlight,30);
    document.body.appendChild(highlight);

    SimileAjax.WindowManager._potentialDropTarget=target;
    SimileAjax.WindowManager._dropTargetHighlightElement=highlight;
    }
    }
    }
    }
    }catch(e){
    SimileAjax.Debug.exception("WindowManager: Error handling mouse move",e);
    SimileAjax.WindowManager._cancelDragging();
    }
    }

    SimileAjax.DOM.cancelEvent(evt);
    return false;
    }
    };

    SimileAjax.WindowManager._onBodyMouseUp=function(elmt,evt,target){
    if(SimileAjax.WindowManager._draggedElement!=null){
    try{
    if(SimileAjax.WindowManager._dragging){
    var callback=SimileAjax.WindowManager._draggedElementCallback;
    if("onDragEnd"in callback){
    callback.onDragEnd();
    }
    if("droppable"in callback&&callback.droppable){
    var dropped=false;

    var target=SimileAjax.WindowManager._potentialDropTarget;
    if(target!=null){
    if((!("canDropOn"in callback)||callback.canDropOn(target))&&
    (!("canDrop"in target)||target.canDrop(SimileAjax.WindowManager._draggedElement))){

    if("onDropOn"in callback){
    callback.onDropOn(target);
    }
    target.ondrop(SimileAjax.WindowManager._draggedElement,SimileAjax.WindowManager._draggingMode);

    dropped=true;
    }
    }

    if(!dropped){

    }
    }
    }
    }finally{
    SimileAjax.WindowManager._cancelDragging();
    }

    SimileAjax.DOM.cancelEvent(evt);
    return false;
    }
    };

    SimileAjax.WindowManager._cancelDragging=function(){
    var callback=SimileAjax.WindowManager._draggedElementCallback;
    if("_ghostElmt"in callback){
    var ghostElmt=callback._ghostElmt;
    document.body.removeChild(ghostElmt);

    delete callback._ghostElmt;
    }
    if(SimileAjax.WindowManager._dropTargetHighlightElement!=null){
    document.body.removeChild(SimileAjax.WindowManager._dropTargetHighlightElement);
    SimileAjax.WindowManager._dropTargetHighlightElement=null;
    }
    if(SimileAjax.WindowManager._draggingModeIndicatorElmt!=null){
    document.body.removeChild(SimileAjax.WindowManager._draggingModeIndicatorElmt);
    SimileAjax.WindowManager._draggingModeIndicatorElmt=null;
    }

    SimileAjax.WindowManager._draggedElement=null;
    SimileAjax.WindowManager._draggedElementCallback=null;
    SimileAjax.WindowManager._potentialDropTarget=null;
    SimileAjax.WindowManager._dropTargetHighlightElement=null;
    SimileAjax.WindowManager._lastCoords=null;
    SimileAjax.WindowManager._ghostCoords=null;
    SimileAjax.WindowManager._draggingMode="";
    SimileAjax.WindowManager._dragging=false;
    };

    SimileAjax.WindowManager._findDropTarget=function(elmt){
    while(elmt!=null){
    if("ondrop"in elmt&&(typeof elmt.ondrop)=="function"){
    break;
    }
    elmt=elmt.parentNode;
    }
    return elmt;
    };


    /* xmlhttp.js */



    SimileAjax.XmlHttp=new Object();


    SimileAjax.XmlHttp._onReadyStateChange=function(xmlhttp,fError,fDone){
    switch(xmlhttp.readyState){





    case 4:
    try{
    if(xmlhttp.status==0
    ||xmlhttp.status==200
    ){
    if(fDone){
    fDone(xmlhttp);
    }
    }else{
    if(fError){
    fError(
    xmlhttp.statusText,
    xmlhttp.status,
    xmlhttp
    );
    }
    }
    }catch(e){
    SimileAjax.Debug.exception("XmlHttp: Error handling onReadyStateChange",e);
    }
    break;
    }
    };


    SimileAjax.XmlHttp._createRequest=function(){
    if(SimileAjax.Platform.browser.isIE){
    var programIDs=[
    "Msxml2.XMLHTTP",
    "Microsoft.XMLHTTP",
    "Msxml2.XMLHTTP.4.0"
    ];
    for(var i=0;i<programIDs.length;i++){
    try{
    var programID=programIDs[i];
    var f=function(){
    return new ActiveXObject(programID);
    };
    var o=f();






    SimileAjax.XmlHttp._createRequest=f;

    return o;
    }catch(e){

    }
    }

    }

    try{
    var f=function(){
    return new XMLHttpRequest();
    };
    var o=f();






    SimileAjax.XmlHttp._createRequest=f;

    return o;
    }catch(e){
    throw new Error("Failed to create an XMLHttpRequest object");
    }
    };


    SimileAjax.XmlHttp.get=function(url,fError,fDone){
    var xmlhttp=SimileAjax.XmlHttp._createRequest();

    xmlhttp.open("GET",url,true);
    xmlhttp.onreadystatechange=function(){
    SimileAjax.XmlHttp._onReadyStateChange(xmlhttp,fError,fDone);
    };
    xmlhttp.send(null);
    };


    SimileAjax.XmlHttp.post=function(url,body,fError,fDone){
    var xmlhttp=SimileAjax.XmlHttp._createRequest();

    xmlhttp.open("POST",url,true);
    xmlhttp.onreadystatechange=function(){
    SimileAjax.XmlHttp._onReadyStateChange(xmlhttp,fError,fDone);
    };
    xmlhttp.send(body);
    };

    SimileAjax.XmlHttp._forceXML=function(xmlhttp){
    try{
    xmlhttp.overrideMimeType("text/xml");
    }catch(e){
    xmlhttp.setrequestheader("Content-Type","text/xml");
    }
    };
//end of simile-ajax-bundle.js



    var getHead = function(doc) {
        return doc.getElementsByTagName("head")[0];
    };

    SimileAjax.findScript = function(doc, substring) {
        var heads = doc.documentElement.getElementsByTagName("head");
        for (var h = 0; h < heads.length; h++) {
            var node = heads[h].firstChild;
            while (node != null) {
                if (node.nodeType == 1 && node.tagName.toLowerCase() == "script") {
                    var url = node.src;
                    var i = url.indexOf(substring);
                    if (i >= 0) {
                        return url;
                    }
                }
                node = node.nextSibling;
            }
        }
        return null;
    };
    SimileAjax.includeJavascriptFile = function(doc, url, onerror, charset) {
        onerror = onerror || "";
        if (doc.body == null) {
            try {
                var q = "'" + onerror.replace( /'/g, '&apos' ) + "'"; // ";
                doc.write("<script src='" + url + "' onerror="+ q +
                          (charset ? " charset='"+ charset +"'" : "") +
                          " type='text/javascript'>"+ onerror + "</script>");
                return;
            } catch (e) {
                // fall through
            }
        }

        var script = doc.createElement("script");
        if (onerror) {
            try { script.innerHTML = onerror; } catch(e) {}
            script.setAttribute("onerror", onerror);
        }
        if (charset) {
            script.setAttribute("charset", charset);
        }
        script.type = "text/javascript";
        script.language = "JavaScript";
        script.src = url;
        return getHead(doc).appendChild(script);
    };
    SimileAjax.includeJavascriptFiles = function(doc, urlPrefix, filenames) {
        for (var i = 0; i < filenames.length; i++) {
            SimileAjax.includeJavascriptFile(doc, urlPrefix + filenames[i]);
        }
        SimileAjax.loadingScriptsCount += filenames.length;
        //SimileAjax.includeJavascriptFile(doc, SimileAjax.urlPrefix + "scripts/signal.js?" + filenames.length);
    };
    SimileAjax.includeCssFile = function(doc, url) {
        if (doc.body == null) {
            try {
                doc.write("<link rel='stylesheet' href='" + url + "' type='text/css'/>");
                return;
            } catch (e) {
                // fall through
            }
        }

        var link = doc.createElement("link");
        link.setAttribute("rel", "stylesheet");
        link.setAttribute("type", "text/css");
        link.setAttribute("href", url);
        getHead(doc).appendChild(link);
    };
    SimileAjax.includeCssFiles = function(doc, urlPrefix, filenames) {
        for (var i = 0; i < filenames.length; i++) {
            SimileAjax.includeCssFile(doc, urlPrefix + filenames[i]);
        }
    };

    /**
     * Append into urls each string in suffixes after prefixing it with urlPrefix.
     * @param {Array} urls
     * @param {String} urlPrefix
     * @param {Array} suffixes
     */
    SimileAjax.prefixURLs = function(urls, urlPrefix, suffixes) {
        for (var i = 0; i < suffixes.length; i++) {
            urls.push(urlPrefix + suffixes[i]);
        }
    };

    /**
     * Parse out the query parameters from a URL
     * @param {String} url    the url to parse, or location.href if undefined
     * @param {Object} to     optional object to extend with the parameters
     * @param {Object} types  optional object mapping keys to value types
     *        (String, Number, Boolean or Array, String by default)
     * @return a key/value Object whose keys are the query parameter names
     * @type Object
     */
    SimileAjax.parseURLParameters = function(url, to, types) {
        to = to || {};
        types = types || {};

        if (typeof url == "undefined") {
            url = location.href;
        }
        var q = url.indexOf("?");
        if (q < 0) {
            return to;
        }
        url = (url+"#").slice(q+1, url.indexOf("#")); // toss the URL fragment

        var params = url.split("&"), param, parsed = {};
        var decode = window.decodeURIComponent || unescape;
        for (var i = 0; param = params[i]; i++) {
            var eq = param.indexOf("=");
            var name = decode(param.slice(0,eq));
            var old = parsed[name];
            if (typeof old == "undefined") {
                old = [];
            } else if (!(old instanceof Array)) {
                old = [old];
            }
            parsed[name] = old.concat(decode(param.slice(eq+1)));
        }
        for (var i in parsed) {
            if (!parsed.hasOwnProperty(i)) continue;
            var type = types[i] || String;
            var data = parsed[i];
            if (!(data instanceof Array)) {
                data = [data];
            }
            if (type === Boolean && data[0] == "false") {
                to[i] = false; // because Boolean("false") === true
            } else {
                to[i] = type.apply(this, data);
            }
        }
        return to;
    };

    (function() {
        var javascriptFiles = [
            "jquery-1.1.3.1.js",
            "platform.js",
            "debug.js",
            "xmlhttp.js",
            "json.js",
            "dom.js",
            "graphics.js",
            "date-time.js",
            "string.js",
            "html.js",
            "data-structure.js",
            "units.js",

            "ajax.js",
            "history.js",
            "window-manager.js"
        ];
        var cssFiles = [
        ];

//        if (typeof SimileAjax_urlPrefix == "string") {
//            SimileAjax.urlPrefix = SimileAjax_urlPrefix;
//        } else {
//            var url = SimileAjax.findScript(document, "simile-ajax-api.js");
//            if (url == null) {
//                SimileAjax.error = new Error("Failed to derive URL prefix for Simile Ajax API code files");
//                return;
//            }
//
//            SimileAjax.urlPrefix = url.substr(0, url.indexOf("simile-ajax-api.js"));
//        }
//
//        SimileAjax.parseURLParameters(url, SimileAjax.params, {bundle:Boolean});
//        if (SimileAjax.params.bundle) {
//            SimileAjax.includeJavascriptFiles(document, SimileAjax.urlPrefix, [ "simile-ajax-bundle.js" ]);
//        } else {
//            SimileAjax.includeJavascriptFiles(document, SimileAjax.urlPrefix + "scripts/", javascriptFiles);
//        }
        SimileAjax.includeCssFiles(document, SimileAjax.urlPrefix + "styles/", cssFiles);

        SimileAjax.loaded = true;
    })();
}

// end of simile-ajax-api.js

// zkTimeplot-api.js

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
    var bundle = true;
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
        //})();
        });

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
            // includeJavascriptFiles(Timeplot.jsUrlPrefix, ["timeplot-bundle"]);
            includeCssFiles(Timeplot.cssUrlPrefix,["bundle.css"]);
        } else {
            // includeJavascriptFiles(Timeplot.jsUrlPrefix + "scripts.", javascriptFiles);
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

//end of zkTimeplot-api.js

//zkTimpelot-api.js

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
    var bundle = true;
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
        });
        //})();

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
        //includeJavascriptFiles(Timeplot.jsUrlPrefix, ["timeplot-bundle"]);
        includeCssFiles(Timeplot.cssUrlPrefix,["bundle.css"]);

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

// end of zkTimeplot-api.js

// timeplot-bundle.js
/* timeplot.js */

Timeline.Debug=SimileAjax.Debug;
var log=SimileAjax.Debug.log;


Object.extend=function(destination,source){
for(var property in source){
destination[property]=source[property];
}
return destination;
}




Timeplot.create=function(elmt,plotInfos){
return new Timeplot._Impl(elmt,plotInfos);
};


Timeplot.createPlotInfo=function(params){
return{
id:("id"in params)?params.id:"p"+Math.round(Math.random()*1000000),
dataSource:("dataSource"in params)?params.dataSource:null,
eventSource:("eventSource"in params)?params.eventSource:null,
timeGeometry:("timeGeometry"in params)?params.timeGeometry:new Timeplot.DefaultTimeGeometry(),
valueGeometry:("valueGeometry"in params)?params.valueGeometry:new Timeplot.DefaultValueGeometry(),
timeZone:("timeZone"in params)?params.timeZone:0,
fillColor:("fillColor"in params)?((params.fillColor=="string")?new Timeplot.Color(params.fillColor):params.fillColor):null,
lineColor:("lineColor"in params)?((params.lineColor=="string")?new Timeplot.Color(params.lineColor):params.lineColor):new Timeplot.Color("#606060"),
lineWidth:("lineWidth"in params)?params.lineWidth:1.0,
dotRadius:("dotRadius"in params)?params.dotRadius:2.0,
dotColor:("dotColor"in params)?params.dotColor:null,
eventLineWidth:("eventLineWidth"in params)?params.eventLineWidth:1.0,
showValues:("showValues"in params)?params.showValues:false,
roundValues:("roundValues"in params)?params.roundValues:true,
valuesOpacity:("valuesOpacity"in params)?params.valuesOpacity:75,
bubbleWidth:("bubbleWidth"in params)?params.bubbleWidth:300,
bubbleHeight:("bubbleHeight"in params)?params.bubbleHeight:200
};
};




Timeplot._Impl=function(elmt,plotInfos){
this._id="t"+Math.round(Math.random()*1000000);
this._containerDiv=elmt;
this._plotInfos=plotInfos;
this._painters={
background:[],
foreground:[]
};
this._painter=null;
this._active=false;
this._initialize();
};

Timeplot._Impl.prototype={

dispose:function(){
for(var i=0;i<this._plots.length;i++){
this._plots[i].dispose();
}
this._plots=null;
this._plotsInfos=null;
this._containerDiv.innerHTML="";
},


getElement:function(){
return this._containerDiv;
},


getDocument:function(){
return this._containerDiv.ownerDocument;
},


add:function(div){
this._containerDiv.appendChild(div);
},


remove:function(div){
this._containerDiv.removeChild(div);
},


addPainter:function(layerName,painter){
var layer=this._painters[layerName];
if(layer){
for(var i=0;i<layer.length;i++){
if(layer[i].context._id==painter.context._id){
return;
}
}
layer.push(painter);
}
},


removePainter:function(layerName,painter){
var layer=this._painters[layerName];
if(layer){
for(var i=0;i<layer.length;i++){
if(layer[i].context._id==painter.context._id){
layer.splice(i,1);
break;
}
}
}
},


getWidth:function(){
return this._containerDiv.clientWidth;
},


getHeight:function(){
return this._containerDiv.clientHeight;
},


getCanvas:function(){
return this._canvas;
},


loadText:function(url,separator,eventSource,filter){
if(this._active){
var tp=this;

var fError=function(statusText,status,xmlhttp){
alert("Failed to load data xml from "+url+"\n"+statusText);
tp.hideLoadingMessage();
};

var fDone=function(xmlhttp){
try{
eventSource.loadText(xmlhttp.responseText,separator,url,filter);
}catch(e){
SimileAjax.Debug.exception(e);
}finally{
tp.hideLoadingMessage();
}
};

this.showLoadingMessage();
window.setTimeout(function(){SimileAjax.XmlHttp.get(url,fError,fDone);},0);
}
},


loadXML:function(url,eventSource){
if(this._active){
var tl=this;

var fError=function(statusText,status,xmlhttp){
alert("Failed to load data xml from "+url+"\n"+statusText);
tl.hideLoadingMessage();
};

var fDone=function(xmlhttp){
try{
var xml=xmlhttp.responseXML;
if(!xml.documentElement&&xmlhttp.responseStream){
xml.load(xmlhttp.responseStream);
}
eventSource.loadXML(xml,url);
}finally{
tl.hideLoadingMessage();
}
};

this.showLoadingMessage();
window.setTimeout(function(){SimileAjax.XmlHttp.get(url,fError,fDone);},0);
}
},


putText:function(id,text,clazz,styles){
var div=this.putDiv(id,"timeplot-div "+clazz,styles);
div.innerHTML=text;
return div;
},


putDiv:function(id,clazz,styles){
var tid=this._id+"-"+id;
var div=document.getElementById(tid);
if(!div){
var container=this._containerDiv.firstChild;
div=document.createElement("div");
div.setAttribute("id",tid);
container.appendChild(div);
}
div.setAttribute("class","timeplot-div "+clazz);
this.placeDiv(div,styles);
return div;
},


placeDiv:function(div,styles){
if(styles){
for(style in styles){
if(style=="left"){
styles[style]+=this._paddingX;
styles[style]+="px";
}else if(style=="right"){
styles[style]+=this._paddingX;
styles[style]+="px";
}else if(style=="top"){
styles[style]+=this._paddingY;
styles[style]+="px";
}else if(style=="bottom"){
styles[style]+=this._paddingY;
styles[style]+="px";
}else if(style=="width"){
if(styles[style]<0)styles[style]=0;
styles[style]+="px";
}else if(style=="height"){
if(styles[style]<0)styles[style]=0;
styles[style]+="px";
}
div.style[style]=styles[style];
}
}
},


locate:function(div){
return{
x:div.offsetLeft-this._paddingX,
y:div.offsetTop-this._paddingY
}
},


update:function(){
if(this._active){
for(var i=0;i<this._plots.length;i++){
var plot=this._plots[i];
var dataSource=plot.getDataSource();
if(dataSource){
var range=dataSource.getRange();
if(range){
plot._valueGeometry.setRange(range);
plot._timeGeometry.setRange(range);
}
}
}
this.paint();
}
},


repaint:function(){
if(this._active){
this._prepareCanvas();
for(var i=0;i<this._plots.length;i++){
var plot=this._plots[i];
if(plot._timeGeometry)plot._timeGeometry.reset();
if(plot._valueGeometry)plot._valueGeometry.reset();
}
this.paint();
}
},


paint:function(){
if(this._active&&this._painter==null){
var timeplot=this;
this._painter=window.setTimeout(function(){
timeplot._clearCanvas();

var run=function(action,context){
try{
if(context.setTimeplot)context.setTimeplot(timeplot);
action.apply(context,[]);
}catch(e){
SimileAjax.Debug.exception(e);
}
}

var background=timeplot._painters.background;
for(var i=0;i<background.length;i++){
run(background[i].action,background[i].context);
}
var foreground=timeplot._painters.foreground;
for(var i=0;i<foreground.length;i++){
run(foreground[i].action,foreground[i].context);
}

timeplot._painter=null;
},20);
}
},

_clearCanvas:function(){
var canvas=this.getCanvas();
var ctx=canvas.getContext('2d');
ctx.clearRect(0,0,canvas.width,canvas.height);
},

_prepareCanvas:function(){
var canvas=this.getCanvas();

var s=SimileAjax.DOM.getSize(this._containerDiv);

canvas.width=s.w;
canvas.height=s.h;

this._paddingX=(this.getWidth()-canvas.width)/2;
this._paddingY=(this.getHeight()-canvas.height)/2;

var ctx=canvas.getContext('2d');
ctx.translate(0,canvas.height);
ctx.scale(1,-1);
ctx.globalCompositeOperation='source-over';
},

_isBrowserSupported:function(canvas){
var browser=SimileAjax.Platform.browser;
if(canvas.getContext&&window.getComputedStyle){
return true;
}else{
return false;
}
},

_initialize:function(){



SimileAjax.WindowManager.initialize();

var containerDiv=this._containerDiv;
var doc=containerDiv.ownerDocument;


containerDiv.className="timeplot-container "+containerDiv.className;


while(containerDiv.firstChild){
containerDiv.removeChild(containerDiv.firstChild);
}

var canvas=doc.createElement("canvas");

if(this._isBrowserSupported(canvas)){

var labels=doc.createElement("div");
containerDiv.appendChild(labels);

this._canvas=canvas;
canvas.className="timeplot-canvas";
this._prepareCanvas();
containerDiv.appendChild(canvas);


var elmtCopyright=SimileAjax.Graphics.createTranslucentImage(Timeplot.urlPrefix+"images/copyright.png");
elmtCopyright.className="timeplot-copyright";
elmtCopyright.title="Timeplot (c) SIMILE - http://simile.mit.edu/timeplot/";
SimileAjax.DOM.registerEvent(elmtCopyright,"click",function(){window.location="http://simile.mit.edu/timeplot/";});
containerDiv.appendChild(elmtCopyright);

var timeplot=this;
var painter={
onAddMany:function(){timeplot.update();},
onClear:function(){timeplot.update();}
}


this._plots=[];
if(this._plotInfos){
for(var i=0;i<this._plotInfos.length;i++){
var plot=new Timeplot.Plot(this,this._plotInfos[i]);
var dataSource=plot.getDataSource();
if(dataSource){
dataSource.addListener(painter);
}
this.addPainter("background",{
context:plot.getTimeGeometry(),
action:plot.getTimeGeometry().paint
});
this.addPainter("background",{
context:plot.getValueGeometry(),
action:plot.getValueGeometry().paint
});
this.addPainter("foreground",{
context:plot,
action:plot.paint
});
this._plots.push(plot);
plot.initialize();
}
}


var message=SimileAjax.Graphics.createMessageBubble(doc);
message.containerDiv.className="timeplot-message-container";
containerDiv.appendChild(message.containerDiv);

message.contentDiv.className="timeplot-message";
message.contentDiv.innerHTML="<img src='http://static.simile.mit.edu/timeline/api/images/progress-running.gif' /> Loading...";

this.showLoadingMessage=function(){message.containerDiv.style.display="block";};
this.hideLoadingMessage=function(){message.containerDiv.style.display="none";};

this._active=true;

}else{

this._message=SimileAjax.Graphics.createMessageBubble(doc);
this._message.containerDiv.className="timeplot-message-container";
this._message.containerDiv.style.top="15%";
this._message.containerDiv.style.left="20%";
this._message.containerDiv.style.right="20%";
this._message.containerDiv.style.minWidth="20em";
this._message.contentDiv.className="timeplot-message";
this._message.contentDiv.innerHTML="We're terribly sorry, but your browser is not currently supported by <a href='http://simile.mit.edu/timeplot/'>Timeplot</a>.<br><br> We are working on supporting it in the near future but, for now, see the <a href='http://simile.mit.edu/wiki/Timeplot_Limitations'>list of currently supported browsers</a>.";
this._message.containerDiv.style.display="block";

containerDiv.appendChild(this._message.containerDiv);

}
}
};


/* plot.js */




Timeplot.Plot=function(timeplot,plotInfo){
this._timeplot=timeplot;
this._canvas=timeplot.getCanvas();
this._plotInfo=plotInfo;
this._id=plotInfo.id;
this._timeGeometry=plotInfo.timeGeometry;
this._valueGeometry=plotInfo.valueGeometry;
this._showValues=plotInfo.showValues;
this._theme=new Timeline.getDefaultTheme();
this._dataSource=plotInfo.dataSource;
this._eventSource=plotInfo.eventSource;
this._bubble=null;
};

Timeplot.Plot.prototype={


initialize:function(){
if(this._showValues&&this._dataSource&&this._dataSource.getValue){
this._timeFlag=this._timeplot.putDiv("timeflag","timeplot-timeflag");
this._valueFlag=this._timeplot.putDiv(this._id+"valueflag","timeplot-valueflag");
this._valueFlagLineLeft=this._timeplot.putDiv(this._id+"valueflagLineLeft","timeplot-valueflag-line");
this._valueFlagLineRight=this._timeplot.putDiv(this._id+"valueflagLineRight","timeplot-valueflag-line");
if(!this._valueFlagLineLeft.firstChild){
this._valueFlagLineLeft.appendChild(SimileAjax.Graphics.createTranslucentImage(Timeplot.urlPrefix+"images/line_left.png"));
this._valueFlagLineRight.appendChild(SimileAjax.Graphics.createTranslucentImage(Timeplot.urlPrefix+"images/line_right.png"));
}
this._valueFlagPole=this._timeplot.putDiv(this._id+"valuepole","timeplot-valueflag-pole");

var opacity=this._plotInfo.valuesOpacity;

SimileAjax.Graphics.setOpacity(this._timeFlag,opacity);
SimileAjax.Graphics.setOpacity(this._valueFlag,opacity);
SimileAjax.Graphics.setOpacity(this._valueFlagLineLeft,opacity);
SimileAjax.Graphics.setOpacity(this._valueFlagLineRight,opacity);
SimileAjax.Graphics.setOpacity(this._valueFlagPole,opacity);

var plot=this;

var mouseOverHandler=function(elmt,evt,target){
plot._valueFlag.style.display="block";
mouseMoveHandler(elmt,evt,target);
}

var day=24*60*60*1000;
var month=30*day;

var mouseMoveHandler=function(elmt,evt,target){
if(typeof SimileAjax!="undefined"){
var c=plot._canvas;
var x=Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt,plot._canvas).x);
if(x>c.width)x=c.width;
if(isNaN(x)||x<0)x=0;
var t=plot._timeGeometry.fromScreen(x);
if(t==0){
plot._valueFlag.style.display="none";
return;
}

var v=plot._dataSource.getValue(t);
if(plot._plotInfo.roundValues)v=Math.round(v);
plot._valueFlag.innerHTML=new String(v);
var d=new Date(t);
var p=plot._timeGeometry.getPeriod();
if(p<day){
plot._timeFlag.innerHTML=d.toLocaleTimeString();
}else if(p>month){
plot._timeFlag.innerHTML=d.toLocaleDateString();
}else{
plot._timeFlag.innerHTML=d.toLocaleString();
}

var tw=plot._timeFlag.clientWidth;
var th=plot._timeFlag.clientHeight;
var tdw=Math.round(tw/2);
var vw=plot._valueFlag.clientWidth;
var vh=plot._valueFlag.clientHeight;
var y=plot._valueGeometry.toScreen(v);

if(x+tdw>c.width){
var tx=c.width-tdw;
}else if(x-tdw<0){
var tx=tdw;
}else{
var tx=x;
}

if(plot._timeGeometry._timeValuePosition=="top"){
plot._timeplot.placeDiv(plot._valueFlagPole,{
left:x,
top:th-5,
height:c.height-y-th+6,
display:"block"
});
plot._timeplot.placeDiv(plot._timeFlag,{
left:tx-tdw,
top:-6,
display:"block"
});
}else{
plot._timeplot.placeDiv(plot._valueFlagPole,{
left:x,
bottom:th-5,
height:y-th+6,
display:"block"
});
plot._timeplot.placeDiv(plot._timeFlag,{
left:tx-tdw,
bottom:-6,
display:"block"
});
}

if(x+vw+14>c.width&&y+vh+4>c.height){
plot._valueFlagLineLeft.style.display="none";
plot._timeplot.placeDiv(plot._valueFlagLineRight,{
left:x-14,
bottom:y-14,
display:"block"
});
plot._timeplot.placeDiv(plot._valueFlag,{
left:x-vw-13,
bottom:y-vh-13,
display:"block"
});
}else if(x+vw+14>c.width&&y+vh+4<c.height){
plot._valueFlagLineRight.style.display="none";
plot._timeplot.placeDiv(plot._valueFlagLineLeft,{
left:x-14,
bottom:y,
display:"block"
});
plot._timeplot.placeDiv(plot._valueFlag,{
left:x-vw-13,
bottom:y+13,
display:"block"
});
}else if(x+vw+14<c.width&&y+vh+4>c.height){
plot._valueFlagLineRight.style.display="none";
plot._timeplot.placeDiv(plot._valueFlagLineLeft,{
left:x,
bottom:y-13,
display:"block"
});
plot._timeplot.placeDiv(plot._valueFlag,{
left:x+13,
bottom:y-13,
display:"block"
});
}else{
plot._valueFlagLineLeft.style.display="none";
plot._timeplot.placeDiv(plot._valueFlagLineRight,{
left:x,
bottom:y,
display:"block"
});
plot._timeplot.placeDiv(plot._valueFlag,{
left:x+13,
bottom:y+13,
display:"block"
});
}
}
}

var timeplotElement=this._timeplot.getElement();
SimileAjax.DOM.registerEvent(timeplotElement,"mouseover",mouseOverHandler);
SimileAjax.DOM.registerEvent(timeplotElement,"mousemove",mouseMoveHandler);
}
},


dispose:function(){
if(this._dataSource){
this._dataSource.removeListener(this._paintingListener);
this._paintingListener=null;
this._dataSource.dispose();
this._dataSource=null;
}
},


getDataSource:function(){
return(this._dataSource)?this._dataSource:this._eventSource;
},


getTimeGeometry:function(){
return this._timeGeometry;
},


getValueGeometry:function(){
return this._valueGeometry;
},


paint:function(){
var ctx=this._canvas.getContext('2d');

ctx.lineWidth=this._plotInfo.lineWidth;
ctx.lineJoin='miter';

if(this._dataSource){
if(this._plotInfo.fillColor){

 if (this._plotInfo.fillGradient) {

var gradient=ctx.createLinearGradient(0,this._canvas.height,0,0);
gradient.addColorStop(0,this._plotInfo.fillColor.toString());
gradient.addColorStop(0.5,this._plotInfo.fillColor.toString());
gradient.addColorStop(1,'rgba(255,255,255,0)');

ctx.fillStyle=gradient;
 } else {
ctx.fillStyle = this._plotInfo.fillColor.toString();
}


ctx.beginPath();
ctx.moveTo(0,0);
this._plot(function(x,y){
ctx.lineTo(x,y);
});
ctx.lineTo(this._canvas.width,0);
ctx.fill();
}

if(this._plotInfo.lineColor){
ctx.strokeStyle=this._plotInfo.lineColor.toString();
ctx.beginPath();
this._plot(function(x,y){
ctx.lineTo(x,y);
});
ctx.stroke();
}

if(this._plotInfo.dotColor){
ctx.fillStyle=this._plotInfo.dotColor.toString();
var r=this._plotInfo.dotRadius;
this._plot(function(x,y){
ctx.beginPath();
ctx.arc(x,y,r,0,2*Math.PI,true);
ctx.fill();
});
}
}

if(this._eventSource){
var gradient=ctx.createLinearGradient(0,0,0,this._canvas.height);
gradient.addColorStop(1,'rgba(255,255,255,0)');

ctx.strokeStyle=gradient;
ctx.fillStyle=gradient;
ctx.lineWidth=this._plotInfo.eventLineWidth;
ctx.lineJoin='miter';

var i=this._eventSource.getAllEventIterator();
while(i.hasNext()){
var event=i.next();
var color=event.getColor();
color=(color)?new Timeplot.Color(color):this._plotInfo.lineColor;
var eventStart=event.getStart().getTime();
var eventEnd=event.getEnd().getTime();
if(eventStart==eventEnd){
var c=color.toString();
gradient.addColorStop(0,c);
var start=this._timeGeometry.toScreen(eventStart);
start=Math.floor(start)+0.5;
var end=start;
ctx.beginPath();
ctx.moveTo(start,0);
ctx.lineTo(start,this._canvas.height);
ctx.stroke();
var x=start-4;
var w=7;
}else{
var c=color.toString(0.5);
gradient.addColorStop(0,c);
var start=this._timeGeometry.toScreen(eventStart);
start=Math.floor(start)+0.5;
var end=this._timeGeometry.toScreen(eventEnd);
end=Math.floor(end)+0.5;
ctx.fillRect(start,0,end-start,this._canvas.height);
var x=start;
var w=end-start-1;
}

var div=this._timeplot.putDiv(event.getID(),"timeplot-event-box",{
left:Math.round(x),
width:Math.round(w),
top:0,
height:this._canvas.height-1
});

var plot=this;
var clickHandler=function(event){
return function(elmt,evt,target){
var doc=plot._timeplot.getDocument();
plot._closeBubble();
var coords=SimileAjax.DOM.getEventPageCoordinates(evt);
var elmtCoords=SimileAjax.DOM.getPageCoordinates(elmt);
plot._bubble=SimileAjax.Graphics.createBubbleForPoint(coords.x,elmtCoords.top+plot._canvas.height,plot._plotInfo.bubbleWidth,plot._plotInfo.bubbleHeight,"bottom");
event.fillInfoBubble(plot._bubble.content,plot._theme,plot._timeGeometry.getLabeler());
}
};
var mouseOverHandler=function(elmt,evt,target){
elmt.oldClass=elmt.className;
elmt.className=elmt.className+" timeplot-event-box-highlight";
};
var mouseOutHandler=function(elmt,evt,target){
elmt.className=elmt.oldClass;
elmt.oldClass=null;
}

if(!div.instrumented){
SimileAjax.DOM.registerEvent(div,"click",clickHandler(event));
SimileAjax.DOM.registerEvent(div,"mouseover",mouseOverHandler);
SimileAjax.DOM.registerEvent(div,"mouseout",mouseOutHandler);
div.instrumented=true;
}
}
}
},

_plot:function(f){
var data=this._dataSource.getData();
if(data){
var times=data.times;
var values=data.values;
var T=times.length;
for(var t=0;t<T;t++){
var x=this._timeGeometry.toScreen(times[t]);
var y=this._valueGeometry.toScreen(values[t]);
f(x,y);
}
}
},

_closeBubble:function(){
if(this._bubble!=null){
this._bubble.close();
this._bubble=null;
}
}

}

/* sources.js */




Timeplot.DefaultEventSource=function(eventIndex){
Timeline.DefaultEventSource.apply(this,arguments);
};

Object.extend(Timeplot.DefaultEventSource.prototype,Timeline.DefaultEventSource.prototype);


Timeplot.DefaultEventSource.prototype.loadText=function(text,separator,url,filter){

if(text==null){
return;
}

this._events.maxValues=new Array();
var base=this._getBaseURL(url);

var dateTimeFormat='iso8601';
var parseDateTimeFunction=this._events.getUnit().getParser(dateTimeFormat);

var data=this._parseText(text,separator);

var added=false;

if(filter){
data=filter(data);
}

if(data){
for(var i=0;i<data.length;i++){
var row=data[i];
if(row.length>1){
var evt=new Timeplot.DefaultEventSource.NumericEvent(
parseDateTimeFunction(row[0]),
row.slice(1)
);
this._events.add(evt);
added=true;
}
}
}

if(added){
this._fire("onAddMany",[]);
}
}


Timeplot.DefaultEventSource.prototype._parseText=function(text,separator){
text=text.replace(/\r\n?/g,"\n");
var pos=0;
var len=text.length;
var table=[];
while(pos<len){
var line=[];
if(text.charAt(pos)!='#'){
while(pos<len){
if(text.charAt(pos)=='"'){
var nextquote=text.indexOf('"',pos+1);
while(nextquote<len&&nextquote>-1){
if(text.charAt(nextquote+1)!='"'){
break;
}
nextquote=text.indexOf('"',nextquote+2);
}
if(nextquote<0){

}else if(text.charAt(nextquote+1)==separator){
var quoted=text.substr(pos+1,nextquote-pos-1);
quoted=quoted.replace(/""/g,'"');
line[line.length]=quoted;
pos=nextquote+2;
continue;
}else if(text.charAt(nextquote+1)=="\n"||
len==nextquote+1){
var quoted=text.substr(pos+1,nextquote-pos-1);
quoted=quoted.replace(/""/g,'"');
line[line.length]=quoted;
pos=nextquote+2;
break;
}else{

}
}
var nextseparator=text.indexOf(separator,pos);
var nextnline=text.indexOf("\n",pos);
if(nextnline<0)nextnline=len;
if(nextseparator>-1&&nextseparator<nextnline){
line[line.length]=text.substr(pos,nextseparator-pos);
pos=nextseparator+1;
}else{
line[line.length]=text.substr(pos,nextnline-pos);
pos=nextnline+1;
break;
}
}
}else{
var nextnline=text.indexOf("\n",pos);
pos=(nextnline>-1)?nextnline+1:cur;
}
if(line.length>0){
table[table.length]=line;
}
}
if(table.length<0)return;
return table;
}


Timeplot.DefaultEventSource.prototype.getRange=function(){
var earliestDate=this.getEarliestDate();
var latestDate=this.getLatestDate();
return{
earliestDate:(earliestDate)?earliestDate:null,
latestDate:(latestDate)?latestDate:null,
min:0,
max:0
};
}




Timeplot.DefaultEventSource.NumericEvent=function(time,values){
this._id="e"+Math.round(Math.random()*1000000);
this._time=time;
this._values=values;
};

Timeplot.DefaultEventSource.NumericEvent.prototype={
getID:function(){return this._id;},
getTime:function(){return this._time;},
getValues:function(){return this._values;},


getStart:function(){return this._time;},
getEnd:function(){return this._time;}
};




Timeplot.DataSource=function(eventSource){
this._eventSource=eventSource;
var source=this;
this._processingListener={
onAddMany:function(){source._process();},
onClear:function(){source._clear();}
}
this.addListener(this._processingListener);
this._listeners=[];
this._data=null;
this._range=null;
};

Timeplot.DataSource.prototype={

_clear:function(){
this._data=null;
this._range=null;
},

_process:function(){
this._data={
times:new Array(),
values:new Array()
};
this._range={
earliestDate:null,
latestDate:null,
min:0,
max:0
};
},


getRange:function(){
return this._range;
},


getData:function(){
return this._data;
},


getValue:function(t){
if(this._data){
for(var i=0;i<this._data.times.length;i++){
var l=this._data.times[i];
if(l>t){
return this._data.values[i];
}
}
}
return 0;
},


addListener:function(listener){
this._eventSource.addListener(listener);
},


removeListener:function(listener){
this._eventSource.removeListener(listener);
},


replaceListener:function(oldListener,newListener){
this.removeListener(oldListener);
this.addListener(newListener);
}

}




Timeplot.ColumnSource=function(eventSource,column){
Timeplot.DataSource.apply(this,arguments);
this._column=column-1;
};

Object.extend(Timeplot.ColumnSource.prototype,Timeplot.DataSource.prototype);

Timeplot.ColumnSource.prototype.dispose=function(){
this.removeListener(this._processingListener);
this._clear();
}

Timeplot.ColumnSource.prototype._process=function(){
var count=this._eventSource.getCount();
var times=new Array(count);
var values=new Array(count);
var min=Number.MAX_VALUE;
var max=Number.MIN_VALUE;
var i=0;

var iterator=this._eventSource.getAllEventIterator();
while(iterator.hasNext()){
var event=iterator.next();
var time=event.getTime();
times[i]=time;
var value=this._getValue(event);
if(!isNaN(value)){
if(value<min){
min=value;
}
if(value>max){
max=value;
}
values[i]=value;
}
i++;
}

this._data={
times:times,
values:values
};

this._range={
earliestDate:this._eventSource.getEarliestDate(),
latestDate:this._eventSource.getLatestDate(),
min:min,
max:max
};
}

Timeplot.ColumnSource.prototype._getValue=function(event){
return parseFloat(event.getValues()[this._column]);
}




Timeplot.ColumnDiffSource=function(eventSource,column1,column2){
Timeplot.ColumnSource.apply(this,arguments);
this._column2=column2-1;
};

Object.extend(Timeplot.ColumnDiffSource.prototype,Timeplot.ColumnSource.prototype);

Timeplot.ColumnDiffSource.prototype._getValue=function(event){
var a=parseFloat(event.getValues()[this._column]);
var b=parseFloat(event.getValues()[this._column2]);
return a-b;
}


/**
 * Geometries
 *
 * @fileOverview Geometries
 * @name Geometries
 */

/**
 * This is the constructor for the default value geometry.
 * A value geometry is what regulates mapping of the plot values to the screen y coordinate.
 * If two plots share the same value geometry, they will be drawn using the same scale.
 * If "min" and "max" parameters are not set, the geometry will stretch itself automatically
 * so that the entire plot will be drawn without overflowing. The stretching happens also
 * when a geometry is shared between multiple plots, the one with the biggest range will
 * win over the others.
 *
 * @constructor
 */
Timeplot.DefaultValueGeometry = function(params) {
    if (!params) params = {};
    this._id = ("id" in params) ? params.id : "g" + Math.round(Math.random() * 1000000);
    this._axisColor = ("axisColor" in params) ? ((typeof params.axisColor == "string") ? new Timeplot.Color(params.axisColor) : params.axisColor) : new Timeplot.Color("#606060");
    this._gridColor = ("gridColor" in params) ? ((typeof params.gridColor == "string") ? new Timeplot.Color(params.gridColor) : params.gridColor) : null;
    this._gridLineWidth = ("gridLineWidth" in params) ? params.gridLineWidth : 0.5;
    this._axisLabelsPlacement = ("axisLabelsPlacement" in params) ? params.axisLabelsPlacement : "right";
    //this._gridSpacing = ("gridSpacing" in params) ? params.gridStep : 50;
    this._gridSpacing = ("gridSpacing" in params) ? params.gridSpacing : 50;
    this._gridType = ("gridType" in params) ? params.gridType : "short";
    this._gridShortSize = ("gridShortSize" in params) ? params.gridShortSize : 10;
    this._minValue = ("min" in params) ? params.min : null;
    this._maxValue = ("max" in params) ? params.max : null;
    this._linMap = {
        direct: function(v) {
            return v;
        },
        inverse: function(y) {
            return y;
        }
    };
    this._map = this._linMap;
    this._labels = [];
    this._grid = [];
};

Timeplot.DefaultValueGeometry.prototype = {

    /**
     * Since geometries can be reused across timeplots, we need to call this function
     * before we can paint using this geometry.
     */
    setTimeplot: function(timeplot) {
        this._timeplot = timeplot;
        this._canvas = timeplot.getCanvas();
        this.reset();
    },

    /**
     * Called by all the plot layers this geometry is associated with
     * to update the value range. Unless min/max values are specified
     * in the parameters, the biggest value range will be used.
     */
    setRange: function(range) {
        if ((this._minValue == null) || ((this._minValue != null) && (range.min < this._minValue))) {
            this._minValue = range.min;
        }
        if ((this._maxValue == null) || ((this._maxValue != null) && (range.max * 1.05 > this._maxValue))) {
            this._maxValue = range.max * 1.05; // get a little more head room to avoid hitting the ceiling
        }

        this._updateMappedValues();

        if (!(this._minValue == 0 && this._maxValue == 0)) {
            this._grid = this._calculateGrid();
        }
    },

    /**
     * Called after changing ranges or canvas size to reset the grid values
     */
    reset: function() {
        this._clearLabels();
        this._updateMappedValues();
        this._grid = this._calculateGrid();
    },

    /**
     * Map the given value to a y screen coordinate.
     */
    toScreen: function(value) {
        if (this._canvas && this._maxValue) {
            var v = value - this._minValue;
            return this._canvas.height * (this._map.direct(v)) / this._mappedRange;
        } else {
            return -50;
        }
    },

    /**
     * Map the given y screen coordinate to a value
     */
    fromScreen: function(y) {
        if (this._canvas) {
            return this._map.inverse(this._mappedRange * y / this._canvas.height) + this._minValue;
        } else {
            return 0;
        }
    },

    /**
     * Each geometry is also a painter and paints the value grid and grid labels.
     */
    paint: function() {
        if (this._timeplot) {
            var ctx = this._canvas.getContext('2d');

            ctx.lineJoin = 'miter';

            // paint grid
            if (this._gridColor) {
                var gridGradient = ctx.createLinearGradient(0,0,0,this._canvas.height);
                gridGradient.addColorStop(0, this._gridColor.toHexString());
                gridGradient.addColorStop(0.3, this._gridColor.toHexString());
                gridGradient.addColorStop(1, "rgba(255,255,255,0.5)");

                ctx.lineWidth = this._gridLineWidth;
                ctx.strokeStyle = gridGradient;

                for (var i = 0; i < this._grid.length; i++) {
                    var tick = this._grid[i];
                    var y = Math.floor(tick.y) + 0.5;
                    if (typeof tick.label != "undefined") {
                        if (this._axisLabelsPlacement == "left") {
                            var div = this._timeplot.putText(this._id + "-" + i, tick.label,"timeplot-grid-label",{
                                left: 4,
                                bottom: y + 2,
                                color: this._gridColor.toHexString(),
                                visibility: "hidden"
                            });
                        } else if (this._axisLabelsPlacement == "right") {
                            var div = this._timeplot.putText(this._id + "-" + i, tick.label, "timeplot-grid-label",{
                                right: 4,
                                bottom: y + 2,
                                color: this._gridColor.toHexString(),
                                visibility: "hidden"
                            });
                        }
                        if (y + div.clientHeight < this._canvas.height + 10) {
                            div.style.visibility = "visible"; // avoid the labels that would overflow
                        }
                    }

                    // draw grid
                    ctx.beginPath();
                    if (this._gridType == "long" || tick.label == 0) {
                        ctx.moveTo(0, y);
                        ctx.lineTo(this._canvas.width, y);
                    } else if (this._gridType == "short") {
                        if (this._axisLabelsPlacement == "left") {
                            ctx.moveTo(0, y);
                            ctx.lineTo(this._gridShortSize, y);
                        } else if (this._axisLabelsPlacement == "right") {
                            ctx.moveTo(this._canvas.width, y);
                            ctx.lineTo(this._canvas.width - this._gridShortSize, y);
                        }
                    }
                    ctx.stroke();
                }
            }

            // paint axis
            var axisGradient = ctx.createLinearGradient(0,0,0,this._canvas.height);
            axisGradient.addColorStop(0, this._axisColor.toString());
            axisGradient.addColorStop(0.5, this._axisColor.toString());
            axisGradient.addColorStop(1, "rgba(255,255,255,0.5)");

            ctx.lineWidth = 1;
            ctx.strokeStyle = axisGradient;

            // left axis
            ctx.beginPath();
            ctx.moveTo(0,this._canvas.height);
            ctx.lineTo(0,0);
            ctx.stroke();

            // right axis
            ctx.beginPath();
            ctx.moveTo(this._canvas.width,0);
            ctx.lineTo(this._canvas.width,this._canvas.height);
            ctx.stroke();
        }
    },

    /**
     * Removes all the labels that were added by this geometry
     */
    _clearLabels: function() {
        for (var i = 0; i < this._labels.length; i++) {
            var l = this._labels[i];
            var parent = l.parentNode;
            if (parent) parent.removeChild(l);
        }
    },

    /*
     * This function calculates the grid spacing that it will be used
     * by this geometry to draw the grid in order to reduce clutter.
     */
    _calculateGrid: function() {
        var grid = [];

        if (!this._canvas || this._valueRange == 0) return grid;

        var power = 0;
        if (this._valueRange > 1) {
            while (Math.pow(10,power) < this._valueRange) {
                power++;
            }
            power--;
        } else {
            while (Math.pow(10,power) > this._valueRange) {
                power--;
            }
        }

        var unit = Math.pow(10,power);
        if (unit === 0) {
            return grid;
        }
        var inc = unit;
        while (true) {
            var dy = this.toScreen(this._minValue + inc);

            while (dy < this._gridSpacing) {
                inc += unit;
                dy = this.toScreen(this._minValue + inc);
            }

            if (dy > 2 * this._gridSpacing) { // grids are too spaced out
                unit /= 10;
                inc = unit;
            } else {
                break;
            }
        }

        var v = 0;
        var y = this.toScreen(v);
        if (this._minValue >= 0) {
            while (y < this._canvas.height) {
                if (y > 0) {
                    grid.push({ y: y, label: v });
                }
                v += inc;
                y = this.toScreen(v);
            }
        } else if (this._maxValue <= 0) {
            while (y > 0) {
                if (y < this._canvas.height) {
                    grid.push({ y: y, label: v });
                }
                v -= inc;
                y = this.toScreen(v);
            }
        } else {
            while (y < this._canvas.height) {
                if (y > 0) {
                    grid.push({ y: y, label: v });
                }
                v += inc;
                y = this.toScreen(v);
            }
            v = -inc;
            y = this.toScreen(v);
            while (y > 0) {
                if (y < this._canvas.height) {
                    grid.push({ y: y, label: v });
                }
                v -= inc;
                y = this.toScreen(v);
            }
        }

        return grid;
    },

    /*
     * Update the values that are used by the paint function so that
     * we don't have to calculate them at every repaint.
     */
    _updateMappedValues: function() {
        this._valueRange = Math.abs(this._maxValue - this._minValue);
        this._mappedRange = this._map.direct(this._valueRange);
    }

};

// --------------------------------------------------

/**
 * This is the constructor for a Logarithmic value geometry, which
 * is useful when plots have values in different magnitudes but
 * exhibit similar trends and such trends want to be shown on the same
 * plot (here a cartesian geometry would make the small magnitudes
 * disappear).
 *
 * NOTE: this class extends Timeplot.DefaultValueGeometry and inherits
 * all of the methods of that class. So refer to that class.
 *
 * @constructor
 */
Timeplot.LogarithmicValueGeometry = function(params) {
    Timeplot.DefaultValueGeometry.apply(this, arguments);
    this._logMap = {
        direct: function(v) {
            return Math.log(v + 1) / Math.log(10);
        },
        inverse: function(y) {
            return Math.exp(Math.log(10) * y) - 1;
        }
    };
    this._mode = "log";
    this._map = this._logMap;
    this._calculateGrid = this._logarithmicCalculateGrid;
};

Timeplot.LogarithmicValueGeometry.prototype._linearCalculateGrid = Timeplot.DefaultValueGeometry.prototype._calculateGrid;

Object.extend(Timeplot.LogarithmicValueGeometry.prototype,Timeplot.DefaultValueGeometry.prototype);

/*
 * This function calculates the grid spacing that it will be used
 * by this geometry to draw the grid in order to reduce clutter.
 */
Timeplot.LogarithmicValueGeometry.prototype._logarithmicCalculateGrid = function() {
    var grid = [];

    if (!this._canvas || this._valueRange == 0) return grid;

    var v = 1;
    var y = this.toScreen(v);
    while (y < this._canvas.height || isNaN(y)) {
        if (y > 0) {
            grid.push({ y: y, label: v });
        }
        v *= 10;
        y = this.toScreen(v);
    }

    return grid;
};

/**
 * Turn the logarithmic scaling off.
 */
Timeplot.LogarithmicValueGeometry.prototype.actLinear = function() {
    this._mode = "lin";
    this._map = this._linMap;
    this._calculateGrid = this._linearCalculateGrid;
    this.reset();
};

/**
 * Turn the logarithmic scaling on.
 */
Timeplot.LogarithmicValueGeometry.prototype.actLogarithmic = function() {
    this._mode = "log";
    this._map = this._logMap;
    this._calculateGrid = this._logarithmicCalculateGrid;
    this.reset();
};

/**
 * Toggle logarithmic scaling seeting it to on if off and viceversa.
 */
Timeplot.LogarithmicValueGeometry.prototype.toggle = function() {
    if (this._mode == "log") {
        this.actLinear();
    } else {
        this.actLogarithmic();
    }
};

// -----------------------------------------------------

/**
 * This is the constructor for the default time geometry.
 *
 * @constructor
 */
Timeplot.DefaultTimeGeometry = function(params) {
    if (!params) params = {};
    this._id = ("id" in params) ? params.id : "g" + Math.round(Math.random() * 1000000);
    this._locale = ("locale" in params) ? params.locale : "en";
    this._timeZone = ("timeZone" in params) ? params.timeZone : SimileAjax.DateTime.getTimezone();
    this._labeller = ("labeller" in params) ? params.labeller : null;
    this._axisColor = ("axisColor" in params) ? ((params.axisColor == "string") ? new Timeplot.Color(params.axisColor) : params.axisColor) : new Timeplot.Color("#606060");
    this._gridColor = ("gridColor" in params) ? ((params.gridColor == "string") ? new Timeplot.Color(params.gridColor) : params.gridColor) : null;
    this._gridLineWidth = ("gridLineWidth" in params) ? params.gridLineWidth : 0.5;
    this._axisLabelsPlacement = ("axisLabelsPlacement" in params) ? params.axisLabelsPlacement : "bottom";
    this._gridStep = ("gridStep" in params) ? params.gridStep : 100;
    this._gridStepRange = ("gridStepRange" in params) ? params.gridStepRange : 20;
    this._min = ("min" in params) ? params.min : null;
    this._max = ("max" in params) ? params.max : null;
    this._timeValuePosition =("timeValuePosition" in params) ? params.timeValuePosition : "bottom";
    this._unit = ("unit" in params) ? params.unit : Timeline.NativeDateUnit;
    this._linMap = {
        direct: function(t) {
            return t;
        },
        inverse: function(x) {
            return x;
        }
    };
    this._map = this._linMap;
    this._labeler = this._unit.createLabeller(this._locale, this._timeZone);
    var dateParser = this._unit.getParser("iso8601");
    if (this._min && !this._min.getTime) {
        this._min = dateParser(this._min);
    }
    if (this._max && !this._max.getTime) {
        this._max = dateParser(this._max);
    }
    this._grid = [];
};

Timeplot.DefaultTimeGeometry.prototype = {

    /**
     * Since geometries can be reused across timeplots, we need to call this function
     * before we can paint using this geometry.
     */
    setTimeplot: function(timeplot) {
        this._timeplot = timeplot;
        this._canvas = timeplot.getCanvas();
        this.reset();
    },

    /**
     * Called by all the plot layers this geometry is associated with
     * to update the time range. Unless min/max values are specified
     * in the parameters, the biggest range will be used.
     */
    setRange: function(range) {
        if (this._min) {
            this._earliestDate = this._min;
        } else if (range.earliestDate && ((this._earliestDate == null) || ((this._earliestDate != null) && (range.earliestDate.getTime() < this._earliestDate.getTime())))) {
            this._earliestDate = range.earliestDate;
        }

        if (this._max) {
            this._latestDate = this._max;
        } else if (range.latestDate && ((this._latestDate == null) || ((this._latestDate != null) && (range.latestDate.getTime() > this._latestDate.getTime())))) {
            this._latestDate = range.latestDate;
        }

        if (!this._earliestDate && !this._latestDate) {
            this._grid = [];
        } else {
            this.reset();
        }
    },

    /**
     * Called after changing ranges or canvas size to reset the grid values
     */
    reset: function() {
        this._updateMappedValues();
        if (this._canvas) this._grid = this._calculateGrid();
    },

    /**
     * Map the given date to a x screen coordinate.
     */
    toScreen: function(time) {
        if (this._canvas && this._latestDate) {
            var t = time - this._earliestDate.getTime();

            return this._canvas.width * this._map.direct(t) / this._mappedPeriod;
        } else {
            return -50;
        }
    },

    /**
     * Map the given x screen coordinate to a date.
     */
    fromScreen: function(x) {
        if (this._canvas) {
            return this._map.inverse(this._mappedPeriod * x / this._canvas.width) + this._earliestDate.getTime();
        } else {
            return 0;
        }
    },

    /**
     * Get a period (in milliseconds) this time geometry spans.
     */
    getPeriod: function() {
        return this._period;
    },

    /**
     * Return the labeler that has been associated with this time geometry
     */
    getLabeler: function() {
        return this._labeler;
    },

    /**
     * Return the time unit associated with this time geometry
     */
    getUnit: function() {
        return this._unit;
    },

   /**
    * Each geometry is also a painter and paints the value grid and grid labels.
    */
    paint: function() {
        if (this._canvas) {
            var unit = this._unit;
            var ctx = this._canvas.getContext('2d');

            var gradient = ctx.createLinearGradient(0,0,0,this._canvas.height);

            ctx.strokeStyle = gradient;
            ctx.lineWidth = this._gridLineWidth;
            ctx.lineJoin = 'miter';

            // paint grid
            if (this._gridColor) {
                gradient.addColorStop(0, this._gridColor.toString());
                gradient.addColorStop(1, "rgba(255,255,255,0.9)");

                for (var i = 0; i < this._grid.length; i++) {
                    var tick = this._grid[i];
                    var x = Math.floor(tick.x) + 0.5;
                    if (this._axisLabelsPlacement == "top") {
                        var div = this._timeplot.putText(this._id + "-" + i, tick.label,"timeplot-grid-label",{
                            left: x + 4,
                            top: 2,
                            visibility: "hidden"
                        });
                    } else if (this._axisLabelsPlacement == "bottom") {
                        var div = this._timeplot.putText(this._id + "-" + i, tick.label, "timeplot-grid-label",{
                            left: x + 4,
                            bottom: 2,
                            visibility: "hidden"
                        });
                    }
                    if (x + div.clientWidth < this._canvas.width + 10) {
                        div.style.visibility = "visible"; // avoid the labels that would overflow
                    }

                    // draw separator
                    ctx.beginPath();
                    ctx.moveTo(x,0);
                    ctx.lineTo(x,this._canvas.height);
                    ctx.stroke();
                }
            }

            // paint axis
            gradient.addColorStop(0, this._axisColor.toString());
            gradient.addColorStop(1, "rgba(255,255,255,0.5)");

            ctx.lineWidth = 1;
            gradient.addColorStop(0, this._axisColor.toString());

            ctx.beginPath();
            ctx.moveTo(0,0);
            ctx.lineTo(this._canvas.width,0);
            ctx.stroke();
        }
    },

    /*
     * This function calculates the grid spacing that it will be used
     * by this geometry to draw the grid in order to reduce clutter.
     */
    _calculateGrid: function() {
        var grid = [];

        var time = SimileAjax.DateTime;
        var u = this._unit;
        var p = this._period;

        if (p == 0) return grid;

        // find the time units nearest to the time period
        if (p > time.gregorianUnitLengths[time.MILLENNIUM]) {
            unit = time.MILLENNIUM;
        } else {
            for (var unit = time.MILLENNIUM; unit > 0; unit--) {
                if (time.gregorianUnitLengths[unit-1] <= p && p < time.gregorianUnitLengths[unit]) {
                    unit--;
                    break;
                }
            }
        }

        var t = u.cloneValue(this._earliestDate);

        do {
            time.roundDownToInterval(t, unit, this._timeZone, 1, 0);
            var x = this.toScreen(u.toNumber(t));
            switch (unit) {
                case time.SECOND:
                  var l = t.toLocaleTimeString();
                  break;
                case time.MINUTE:
                  var m = t.getMinutes();
                  var l = t.getHours() + ":" + ((m < 10) ? "0" : "") + m;
                  break;
                case time.HOUR:
                  var l = t.getHours() + ":00";
                  break;
                case time.DAY:
                case time.WEEK:
                case time.MONTH:
                  var l = t.toLocaleDateString();
                  break;
                case time.YEAR:
                case time.DECADE:
                case time.CENTURY:
                case time.MILLENNIUM:
                  var l = t.getUTCFullYear();
                  break;
            }
            if (x > 0) {
                grid.push({ x: x, label: l });
            }
            time.incrementByInterval(t, unit);
        } while (t.getTime() < this._latestDate.getTime());

        return grid;
    },

    /*
     * Update the values that are used by the paint function so that
     * we don't have to calculate them at every repaint.
     */
    _updateMappedValues: function() {
        if (this._latestDate && this._earliestDate) {
            this._period = this._latestDate.getTime() - this._earliestDate.getTime();
            this._mappedPeriod = this._map.direct(this._period);
        } else {
            this._period = 0;
            this._mappedPeriod = 0;
        }
    }

};

// --------------------------------------------------------------
// geometry.js
/**
 * This is the constructor for the magnifying time geometry.
 * Users can interact with this geometry and 'magnify' certain areas of the
 * plot to see the plot enlarged and resolve details that would otherwise
 * get lost or cluttered with a linear time geometry.
 *
 * @constructor
 */
Timeplot.MagnifyingTimeGeometry = function(params) {
    Timeplot.DefaultTimeGeometry.apply(this, arguments);

    var g = this;
    this._MagnifyingMap = {
        direct: function(t) {
            if (t < g._leftTimeMargin) {
                var x = t * g._leftRate;
            } else if ( g._leftTimeMargin < t && t < g._rightTimeMargin ) {
                var x = t * g._expandedRate + g._expandedTimeTranslation;
            } else {
                var x = t * g._rightRate + g._rightTimeTranslation;
            }
            return x;
        },
        inverse: function(x) {
            if (x < g._leftScreenMargin) {
                var t = x / g._leftRate;
            } else if ( g._leftScreenMargin < x && x < g._rightScreenMargin ) {
                var t = x / g._expandedRate + g._expandedScreenTranslation;
            } else {
                var t = x / g._rightRate + g._rightScreenTranslation;
            }
            return t;
        }
    };

    this._mode = "lin";
    this._map = this._linMap;
};

Object.extend(Timeplot.MagnifyingTimeGeometry.prototype,Timeplot.DefaultTimeGeometry.prototype);

/**
 * Initialize this geometry associating it with the given timeplot and
 * register the geometry event handlers to the timeplot so that it can
 * interact with the user.
 */
Timeplot.MagnifyingTimeGeometry.prototype.initialize = function(timeplot) {
    Timeplot.DefaultTimeGeometry.prototype.initialize.apply(this, arguments);

    if (!this._lens) {
        this._lens = this._timeplot.putDiv("lens","timeplot-lens");
    }

    var period = 1000 * 60 * 60 * 24 * 30; // a month in the magnifying lens

    var geometry = this;

    var magnifyWith = function(lens) {
        var aperture = lens.clientWidth;
        var loc = geometry._timeplot.locate(lens);
        geometry.setMagnifyingParams(loc.x + aperture / 2, aperture, period);
        geometry.actMagnifying();
        geometry._timeplot.paint();
    };

    var canvasMouseDown = function(elmt, evt, target) {
        geometry._canvas.startCoords = SimileAjax.DOM.getEventRelativeCoordinates(evt,elmt);
        geometry._canvas.pressed = true;
    };

    var canvasMouseUp = function(elmt, evt, target) {
        geometry._canvas.pressed = false;
        var coords = SimileAjax.DOM.getEventRelativeCoordinates(evt,elmt);
        if (Timeplot.Math.isClose(coords,geometry._canvas.startCoords,5)) {
            geometry._lens.style.display = "none";
            geometry.actLinear();
            geometry._timeplot.paint();
        } else {
            geometry._lens.style.cursor = "move";
            magnifyWith(geometry._lens);
        }
    };

    var canvasMouseMove = function(elmt, evt, target) {
        if (geometry._canvas.pressed) {
            var coords = SimileAjax.DOM.getEventRelativeCoordinates(evt,elmt);
            if (coords.x < 0) coords.x = 0;
            if (coords.x > geometry._canvas.width) coords.x = geometry._canvas.width;
            geometry._timeplot.placeDiv(geometry._lens, {
                left: geometry._canvas.startCoords.x,
                width: coords.x - geometry._canvas.startCoords.x,
                bottom: 0,
                height: geometry._canvas.height,
                display: "block"
            });
        }
    };

    var lensMouseDown = function(elmt, evt, target) {
        geometry._lens.startCoords = SimileAjax.DOM.getEventRelativeCoordinates(evt,elmt);;
        geometry._lens.pressed = true;
    };

    var lensMouseUp = function(elmt, evt, target) {
        geometry._lens.pressed = false;
    };

    var lensMouseMove = function(elmt, evt, target) {
        if (geometry._lens.pressed) {
            var coords = SimileAjax.DOM.getEventRelativeCoordinates(evt,elmt);
            var lens = geometry._lens;
            var left = lens.offsetLeft + coords.x - lens.startCoords.x;
            if (left < geometry._timeplot._paddingX) left = geometry._timeplot._paddingX;
            if (left + lens.clientWidth > geometry._canvas.width - geometry._timeplot._paddingX) left = geometry._canvas.width - lens.clientWidth + geometry._timeplot._paddingX;
            lens.style.left = left;
            magnifyWith(lens);
        }
    };

    if (!this._canvas.instrumented) {
        SimileAjax.DOM.registerEvent(this._canvas, "mousedown", canvasMouseDown);
        SimileAjax.DOM.registerEvent(this._canvas, "mousemove", canvasMouseMove);
        SimileAjax.DOM.registerEvent(this._canvas, "mouseup"  , canvasMouseUp);
        SimileAjax.DOM.registerEvent(this._canvas, "mouseup"  , lensMouseUp);
        this._canvas.instrumented = true;
    }

    if (!this._lens.instrumented) {
        SimileAjax.DOM.registerEvent(this._lens, "mousedown", lensMouseDown);
        SimileAjax.DOM.registerEvent(this._lens, "mousemove", lensMouseMove);
        SimileAjax.DOM.registerEvent(this._lens, "mouseup"  , lensMouseUp);
        SimileAjax.DOM.registerEvent(this._lens, "mouseup"  , canvasMouseUp);
        this._lens.instrumented = true;
    }
};

/**
 * Set the Magnifying parameters. c is the location in pixels where the Magnifying
 * center should be located in the timeplot, a is the aperture in pixel of
 * the Magnifying and b is the time period in milliseconds that the Magnifying
 * should span.
 */
Timeplot.MagnifyingTimeGeometry.prototype.setMagnifyingParams = function(c,a,b) {
    a = a / 2;
    b = b / 2;

    var w = this._canvas.width;
    var d = this._period;

    if (c < 0) c = 0;
    if (c > w) c = w;

    if (c - a < 0) a = c;
    if (c + a > w) a = w - c;

    var ct = this.fromScreen(c) - this._earliestDate.getTime();
    if (ct - b < 0) b = ct;
    if (ct + b > d) b = d - ct;

    this._centerX = c;
    this._centerTime = ct;
    this._aperture = a;
    this._aperturePeriod = b;

    this._leftScreenMargin = this._centerX - this._aperture;
    this._rightScreenMargin = this._centerX + this._aperture;
    this._leftTimeMargin = this._centerTime - this._aperturePeriod;
    this._rightTimeMargin = this._centerTime + this._aperturePeriod;

    this._leftRate = (c - a) / (ct - b);
    this._expandedRate = a / b;
    this._rightRate = (w - c - a) / (d - ct - b);

    this._expandedTimeTranslation = this._centerX - this._centerTime * this._expandedRate;
    this._expandedScreenTranslation = this._centerTime - this._centerX / this._expandedRate;
    this._rightTimeTranslation = (c + a) - (ct + b) * this._rightRate;
    this._rightScreenTranslation = (ct + b) - (c + a) / this._rightRate;

    this._updateMappedValues();
};

/*
 * Turn magnification off.
 */
Timeplot.MagnifyingTimeGeometry.prototype.actLinear = function() {
    this._mode = "lin";
    this._map = this._linMap;
    this.reset();
};

/*
 * Turn magnification on.
 */
Timeplot.MagnifyingTimeGeometry.prototype.actMagnifying = function() {
    this._mode = "Magnifying";
    this._map = this._MagnifyingMap;
    this.reset();
};

/*
 * Toggle magnification.
 */
Timeplot.MagnifyingTimeGeometry.prototype.toggle = function() {
    if (this._mode == "Magnifying") {
        this.actLinear();
    } else {
        this.actMagnifying();
    }
};




/* color.js */






Timeplot.Color=function(color){
this._fromHex(color);
};

Timeplot.Color.prototype={


set:function(r,g,b,a){
this.r=r;
this.g=g;
this.b=b;
this.a=(a)?a:1.0;
return this.check();
},


transparency:function(a){
this.a=a;
return this.check();
},


lighten:function(level){
var color=new Timeplot.Color();
return color.set(
this.r+=parseInt(level,10),
this.g+=parseInt(level,10),
this.b+=parseInt(level,10)
);
},


darken:function(level){
var color=new Timeplot.Color();
return color.set(
this.r-=parseInt(level,10),
this.g-=parseInt(level,10),
this.b-=parseInt(level,10)
);
},


check:function(){
if(this.r>255){
this.r=255;
}else if(this.r<0){
this.r=0;
}
if(this.g>255){
this.g=255;
}else if(this.g<0){
this.g=0;
}
if(this.b>255){
this.b=255;
}else if(this.b<0){
this.b=0;
}
if(this.a>1.0){
this.a=255;
}else if(this.a<0.0){
this.a=0.0;
}
return this;
},


toString:function(alpha){
return'rgba('+this.r+','+this.g+','+this.b+','+((alpha)?alpha:'1.0')+')';
},


toHexString:function(){
return"#"+this._toHex(this.r)+this._toHex(this.g)+this._toHex(this.b);
},


_fromHex:function(color){
if(/^#?([\da-f]{3}|[\da-f]{6})$/i.test(color)){
color=color.replace(/^#/,'').replace(/^([\da-f])([\da-f])([\da-f])$/i,"$1$1$2$2$3$3");
this.r=parseInt(color.substr(0,2),16);
this.g=parseInt(color.substr(2,2),16);
this.b=parseInt(color.substr(4,2),16);
}else if(/^rgb *\( *\d{0,3} *, *\d{0,3} *, *\d{0,3} *\)$/i.test(color)){
color=color.match(/^rgb *\( *(\d{0,3}) *, *(\d{0,3}) *, *(\d{0,3}) *\)$/i);
this.r=parseInt(color[1],10);
this.g=parseInt(color[2],10);
this.b=parseInt(color[3],10);
}
this.a=1.0;
return this.check();
},


_toHex:function(dec){
var hex="0123456789ABCDEF"
if(dec<0)return"00";
if(dec>255)return"FF";
var i=Math.floor(dec/16);
var j=dec%16;
return hex.charAt(i)+hex.charAt(j);
}

};

/* math.js */



Timeplot.Math={


range:function(f){
var F=f.length;
var min=Number.MAX_VALUE;
var max=Number.MIN_VALUE;

for(var t=0;t<F;t++){
var value=f[t];
if(value<min){
min=value;
}
if(value>max){
max=value;
}
}

return{
min:min,
max:max
}
},


movingAverage:function(f,size){
var F=f.length;
var g=new Array(F);
for(var n=0;n<F;n++){
var value=0;
for(var m=n-size;m<n+size;m++){
if(m<0){
var v=f[0];
}else if(m>=F){
var v=g[n-1];
}else{
var v=f[m];
}
value+=v;
}
g[n]=value/(2*size);
}
return g;
},


integral:function(f){
var F=f.length;

var g=new Array(F);
var sum=0;

for(var t=0;t<F;t++){
sum+=f[t];
g[t]=sum;
}

return g;
},


normalize:function(f){
var F=f.length;
var sum=0.0;

for(var t=0;t<F;t++){
sum+=f[t];
}

for(var t=0;t<F;t++){
f[t]/=sum;
}

return f;
},


convolution:function(f,g){
var F=f.length;
var G=g.length;

var c=new Array(F);

for(var m=0;m<F;m++){
var r=0;
var end=(m+G<F)?m+G:F;
for(var n=m;n<end;n++){
var a=f[n-G];
var b=g[n-m];
r+=a*b;
}
c[m]=r;
}

return c;
},







heavyside:function(size){
var f=new Array(size);
var value=1/size;
for(var t=0;t<size;t++){
f[t]=value;
}
return f;
},


gaussian:function(size,threshold){
with(Math){
var radius=size/2;
var variance=radius*radius/log(threshold);
var g=new Array(size);
for(var t=0;t<size;t++){
var l=t-radius;
g[t]=exp(-variance*l*l);
}
}

return this.normalize(g);
},




round:function(x,n){
with(Math){
if(abs(x)>1){
var l=floor(log(x)/log(10));
var d=round(exp((l-n+1)*log(10)));
var y=round(round(x/d)*d);
return y;
}else{
log("FIXME(SM): still to implement for 0 < abs(x) < 1");
return x;
}
}
},


tanh:function(x){
if(x>5){
return 1;
}else if(x<5){
return-1;
}else{
var expx2=Math.exp(2*x);
return(expx2-1)/(expx2+1);
}
},


isClose:function(a,b,value){
return(a&&b&&Math.abs(a.x-b.x)<value&&Math.abs(a.y-b.y)<value);
}

}

/* processor.js */





Timeplot.Operator={


sum:function(data,params){
return Timeplot.Math.integral(data.values);
},


average:function(data,params){
var size=("size"in params)?params.size:30;
var result=Timeplot.Math.movingAverage(data.values,size);
return result;
}
}




Timeplot.Processor=function(dataSource,operator,params){
this._dataSource=dataSource;
this._operator=operator;
this._params=params;

this._data={
times:new Array(),
values:new Array()
};

this._range={
earliestDate:null,
latestDate:null,
min:0,
max:0
};

var processor=this;
this._processingListener={
onAddMany:function(){processor._process();},
onClear:function(){processor._clear();}
}
this.addListener(this._processingListener);
};

Timeplot.Processor.prototype={

_clear:function(){
this.removeListener(this._processingListener);
this._dataSource._clear();
},

_process:function(){




var data=this._dataSource.getData();
var range=this._dataSource.getRange();

var newValues=this._operator(data,this._params);
var newValueRange=Timeplot.Math.range(newValues);

this._data={
times:data.times,
values:newValues
};

this._range={
earliestDate:range.earliestDate,
latestDate:range.latestDate,
min:newValueRange.min,
max:newValueRange.max
};
},

getRange:function(){
return this._range;
},

getData:function(){
return this._data;
},

getValue:Timeplot.DataSource.prototype.getValue,

addListener:function(listener){
this._dataSource.addListener(listener);
},

removeListener:function(listener){
this._dataSource.removeListener(listener);
}
}
// end of timeplot-bundle.js

//zk.loadCSS("js/ext/timeplot/api/styles/timeplot.css");
///////////////////zkTimeplot Class/////////////////////////////////
zkTimeplot={};

//////////////////zkTimeplot.init//////////////////////////////

zkTimeplot.init=function(cmp){
var timeplot=$e(cmp.id+"!timeplot");
var timeplotDiv=$e(cmp.id+"!timeplot");
cmp.timeplot = Timeplot.create(timeplotDiv, cmp.plotinfos);
  ///*
  for(i=0;i<cmp.plotinfos.length;i++){
      var plot=cmp.plotinfos[i];
      plot.timeplot=cmp.timeplot;
      if(plot.dataSourceUri!=null){
          cmp.timeplot.loadText(plot.dataSourceUri,plot.separator, plot.ds);
      }//end if
  if(plot.eventSourceUri!=null){

      cmp.timeplot.loadXML(plot.eventSourceUri,plot.es);
  }//end if
  }//end for
};

//////////////////zkTimeplot.onVisi//////////////////////

zkTimeplot.onVisi=function(cmp){
if(cmp.timeplot==null)
		zkTimeplot.init(cmp);

};
//////////////////zkTimeplot.cleanup///////////////////////
zkTimeplot.cleanup=function(cmp){
cmp.timeplot=null;
cmp.plotinfos=[];
zkTimeplot.valueGeometries={};
zkTimeplot.timeGeometries={};

};
///////////////////zkTimeplot.setAttr/////////////////////////
zkTimeplot.setAttr = function (cmp, name, value) {
	if (cmp) {
		switch (name) {
		case "z.width":
			var timeplot=$e(cmp.id+"!timeplot");
			timeplot.style.width=value;
			cmp.timeplot.repaint();
			return true;
		case "z.height":
			var timeplot=document.getElementById(cmp.id+"!timeplot");
			timeplot.style.height=value;
			cmp.timeplot.repaint();
			return true;
			}//end of switch
	}//end of if

};
///////////////////zkPlotinf Class////////////////////////////
zkPlotinfo={};
/////////////////setParams function///////////////
zkPlotinfo.setParams=function(cmp,pre,name,params,convert){
var v=getZKAttr(cmp,pre+name);
		if(v!=null){
			params[name]=(convert==null)?v:convert(v);
		}
};
////////////////zkPlotinfo.createValueGeometry/////////////////
zkPlotinfo.createValueGeometry=function(cmp){
params={};
zkPlotinfo.setParams(cmp,"valueGeometry.","axisColor",params,null);
zkPlotinfo.setParams(cmp,"valueGeometry.","gridColor",params,null);
zkPlotinfo.setParams(cmp,"valueGeometry.","gridLineWidth",params,parseFloat);
zkPlotinfo.setParams(cmp,"valueGeometry.","axisLabelsPlacement",params,null);
zkPlotinfo.setParams(cmp,"valueGeometry.","gridSpacing",params,parseInt);
zkPlotinfo.setParams(cmp,"valueGeometry.","gridType",params,null);
zkPlotinfo.setParams(cmp,"valueGeometry.","gridShortSize",params,parseInt);
zkPlotinfo.setParams(cmp,"valueGeometry.","min",params,parseInt);
zkPlotinfo.setParams(cmp,"valueGeometry.","max",params,parseInt);
return new Timeplot.DefaultValueGeometry(params);

};
///////////////zkPlotinfo.createTimeGeometry////////////////

zkPlotinfo.createTimeGeometry=function(cmp){
params={};
zkPlotinfo.setParams(cmp,"timeGeometry.","axisColor",params,null);
zkPlotinfo.setParams(cmp,"timeGeometry.","gridColor",params,null);
zkPlotinfo.setParams(cmp,"timeGeometry.","gridLineWidth",params,parseFloat);
zkPlotinfo.setParams(cmp,"timeGeometry.","axisLabelsPlacement",params,null);
zkPlotinfo.setParams(cmp,"timeGeometry.","gridStep",params,parseInt);
zkPlotinfo.setParams(cmp,"timeGeometry.","gridStepRange",params,parseInt);
zkPlotinfo.setParams(cmp,"timeGeometry.","min",params,parseInt);
zkPlotinfo.setParams(cmp,"timeGeometry.","max",params,parseInt);
var type=getZKAttr(cmp,"valueGeometryType");
if("DefaultValueGeometry"==type)
		return new Timeplot.DefaultTimeGeometry(params);
else if("LogarithmicValueGeometry"==type){
		return new Timeplot.LogarithmicValueGeometry(params);
}

};
/////////////////zkPlotinfo.init///////////////////////
zkPlotinfo.init=function(cmp){
//get parent of plotinfo
var timeplot=$e(getZKAttr(cmp,"pid"));
var params={};
params["id"]=cmp.id;
params["showValues"]=getZKAttr(cmp,"showValues")=="true"?true:false;
var tmp=getZKAttr(cmp,"lineColor");//set line Color
if(tmp!=null)
    params["lineColor"]=new Timeplot.Color(tmp);

tmp=getZKAttr(cmp,"fillColor");//set fill color
if(tmp!=null)
    params["fillColor"]=new Timeplot.Color(tmp);

tmp=getZKAttr(cmp,"dotColor");//set dot color
if(tmp!=null)
    params["dotColor"]=new Timeplot.Color(tmp);

params["lineWidth"]=parseFloat(getZKAttr(cmp,"lineWidth"));
params["eventLineWidth"]=parseFloat(getZKAttr(cmp,"eventLineWidth"));
params["dotRadius"]=parseFloat(getZKAttr(cmp,"dotRadius"));
params["valuesOpacity"]=parseInt(getZKAttr(cmp,"valuesOpacity"));
params["roundValues"]=(getZKAttr(cmp,"roundValues")=="true")?true:false;
params["bubbleWidth"]=parseInt(getZKAttr(cmp,"bubbleWidth"));
params["bubbleHeight"]=parseInt(getZKAttr(cmp,"bubbleHeight"));


var es1 = new Timeplot.DefaultEventSource();//for data
var es2 = new Timeplot.DefaultEventSource();//for event

var col=parseInt(getZKAttr(cmp,"dataSourceColumn"));
var ds = new Timeplot.ColumnSource(es1,col);
var operator=eval("("+getZKAttr(cmp,"operator")+")");//sum,average or user-defined function
var optParams=eval("("+getZKAttr(cmp,"operatorParams")+")");
if(operator!=null){
        var operatorDS=new Timeplot.Processor(ds,operator, optParams);
        ds=operatorDS;
}
var dataSourceUri=getZKAttr(cmp,"dataSourceUri");
var eventSourceUri=getZKAttr(cmp,"eventSourceUri");
//if(dataSourceUri!=null){
     params["dataSource"]=ds;
//     }
//if(eventSourceUri!=null)
     params["eventSource"]=es2;

if(zkTimeplot.defaultValueGeometry==null){
            zkTimeplot.defaultValueGeometry=new Timeplot.DefaultValueGeometry();//global var
            zkTimeplot.defaultTimeGeometry=new Timeplot.DefaultTimeGeometry();//global var
            zkTimeplot.valueGeometries={};
            zkTimeplot.timeGeometries={};
}

var valueGeometry=zkTimeplot.defaultValueGeometry;
var timeGeometry=zkTimeplot.defaultTimeGeometry;
if(getZKAttr(cmp,"valueGeometryDefined")=="true"){
            var id=getZKAttr(cmp,"valueGeometry.id");
            if(zkTimeplot.valueGeometries["valueGeometry"+id]==null){
                        valueGeometry=zkPlotinfo.createValueGeometry(cmp);
                        zkTimeplot.valueGeometries["valueGeometry"+id]=valueGeometry;
            }else{
                        valueGeometry=zkTimeplot.valueGeometries["valueGeometry"+id];
            }
}

if(getZKAttr(cmp,"timeGeometryDefined")=="true"){
            var id=getZKAttr(cmp,"timeGeometry.id");
            if(zkTimeplot.timeGeometries["timeGeometry"+id]==null){
                        timeGeometry=zkPlotinfo.createTimeGeometry(cmp);
                        zkTimeplot.timeGeometries["timeGeometry"+id]=timeGeometry;
            }else{
                        timeGeometry=zkTimeplot.timeGeometries["timeGeometry"+id];
            }
}

params["timeGeometry"]=timeGeometry;
params["valueGeometry"]=valueGeometry;


cmp.plotinfo=Timeplot.createPlotInfo(params);
cmp.plotinfo.timeGeometry=timeGeometry;
cmp.plotinfo.valueGeometry=valueGeometry;

cmp.plotinfo.eventSourceUri=eventSourceUri;
cmp.plotinfo.es=es2;
cmp.plotinfo.dataSourceUri=dataSourceUri;
cmp.plotinfo.ds=es1;

cmp.plotinfo.separator=getZKAttr(cmp,"separator");
if(timeplot.plotinfos==null)
        timeplot.plotinfos=new Array();
timeplot.plotinfos[timeplot.plotinfos.length]=cmp.plotinfo;
};
/////////////zkPlotinfo.parseDateTime////////
zkPlotinfo.parseDateTime=function(dateString){
	if(dateString==null) return null;
    try {
        return new Date(Date.parse(dateString));
    } catch (e) {
        return null;
    }
};
////////////zkPlotinfo.newEvent//////////
zkPlotinfo.newEvent=function(eventSource,params){
		var evt=new Timeline.DefaultEventSource.Event(
			zkPlotinfo.parseDateTime(params.start),
			zkPlotinfo.parseDateTime(params.end),
			zkPlotinfo.parseDateTime(params.latestStart),
			zkPlotinfo.parseDateTime(params.earliestEnd),
			!params.duration,
			params.text,
			params.description,
			eventSource._resolveRelativeURL(params.image, ""),
			eventSource._resolveRelativeURL(params.link, ""),
			eventSource._resolveRelativeURL(params.icon, ""),
			params.color,
			params.textColor
		);
	evt._id="dynaEvent"+params.id;
	if("wikiUrl" in params)
		evt.setWikiInfo(params.wikiUrl, params.wikiSection);
	return evt;
};
////////////zkPlotinfo.addPlotEvent///////////

zkPlotinfo.addPlotEvent=function(uuid,event){

		var plot=$e(uuid);
		if(event.length==0) return;

		var evt =zkPlotinfo.newEvent(plot.plotinfo.es,event);
		if(plot.plotinfo.dynaEvents==null){
				 plot.plotinfo.dynaEvents={};
		 }
		 if(plot.plotinfo.dynaEvents[evt._id]!=null) return;//already exists.
		plot.plotinfo.dynaEvents[evt._id]=evt;
		plot.plotinfo.es._events.add(evt);
		plot.plotinfo.timeplot.repaint();
		//plot.plotinfo.es._fire("onAddMany", []);
};
////////////zkPlotinfo.modifyPlotEvent///////////

zkPlotinfo.modifyPlotEvent=function(uuid,event){

		var plot=$e(uuid);
		if(event.length==0) return;
		var evt=plot.plotinfo.dynaEvents["dynaEvent"+event.id];
		if(evt==null) return;
		plot.plotinfo.es._events._events.remove(evt);
		plot.plotinfo.dynaEvents["dynaEvent"+event.id]=null;
		evt =zkPlotinfo.newEvent(plot.plotinfo.es,event);
		//evt._id="dynaEvent"+event.id;
		plot.plotinfo.es._events.add(evt);

		plot.plotinfo.dynaEvents[evt._id]=evt;
		plot.plotinfo.timeplot.repaint();
};

////////////zkPlotinfo.removePlotEvent///////////

zkPlotinfo.removePlotEvent=function(uuid,event){

		var plot=$e(uuid);
		if(event.length==0) return;
		var evt=plot.plotinfo.dynaEvents["dynaEvent"+event.id];
		//alert(evt._id);
		if(evt==null) return;
		plot.plotinfo.es._events._events.remove(evt);
		plot.plotinfo.es._events._index();
		plot.plotinfo.dynaEvents["dynaEvent"+event.id]=null;
		plot.plotinfo.timeplot.repaint();
		var tid = plot.plotinfo.timeplot._id +"-dynaEvent"+event.id;
		//alert(tid);
    	var div = document.getElementById(tid);
    	if(div!=null)
			plot.plotinfo.timeplot._containerDiv.firstChild.removeChild(div);

};
//////////////zkPlotinfo.addPlotData/////////////
zkPlotinfo.addPlotData=function(uuid,data){

		var plot=$e(uuid);
		if(data.length==0) return;
 		if(plot.plotinfo.dynaDatas&&plot.plotinfo.dynaDatas["dynmic"+data["id"]]!=null) return;//already exists.
	   	var dateTimeFormat = 'iso8601';
    	var parseDateTimeFunction = plot.plotinfo.ds._events.getUnit().getParser(dateTimeFormat);
		var evt = new Timeplot.DefaultEventSource.NumericEvent(
		            parseDateTimeFunction(data ["time"]),
		          [parseFloat(data ["value"])]
		        );
		evt._id="dynmic"+data["id"];
		if(plot.plotinfo.dynaDatas==null){
				 plot.plotinfo.dynaDatas={};

				 }
		plot.plotinfo.ds._events.add(evt);
		plot.plotinfo.dynaDatas[evt._id]=evt;
		plot.plotinfo.ds._fire("onAddMany", []);
};
/////////////zkPlotinfo.modifyPlotData//////////////
zkPlotinfo.modifyPlotData=function(uuid,data){

		var plot=$e(uuid);
		if(data.length==0) return;
		var evt= plot.plotinfo.dynaDatas[ "dynmic"+data["id"]];
		if(evt==null) return;
		plot.plotinfo.ds._events._events.remove(evt);
	   	var dateTimeFormat = 'iso8601';
    	var parseDateTimeFunction = plot.plotinfo.ds._events.getUnit().getParser(dateTimeFormat);
		 evt = new Timeplot.DefaultEventSource.NumericEvent(
		            parseDateTimeFunction(data ["time"]),
		          [parseFloat(data ["value"])]
		        );
		evt._id="dynmic"+data["id"];
		plot.plotinfo.ds._events.add(evt);
		plot.plotinfo.dynaDatas[evt._id]=evt;
		plot.plotinfo.ds._fire("onAddMany", []);
};
///////////////zkPlotinfo.removePlotData//////////////////////
zkPlotinfo.removePlotData=function(uuid,data){

		var plot=$e(uuid);
		if(data.length==0) return;
		var evt= plot.plotinfo.dynaDatas[ "dynmic"+data["id"]];
		plot.plotinfo.ds._events._events.remove(evt);
		 plot.plotinfo.dynaDatas[ "dynmic"+data["id"]]=null;
		plot.plotinfo.ds._events._index();
		plot.plotinfo.ds._fire("onAddMany", []);
};

///////////////zkPlotinfo.repaint//////////////////////
zkPlotinfo.repaint=function(uuid){
		var plot=$e(uuid);
		plot.plotinfo.timeplot.repaint();
};
////////////////zkPlotinfo.setAttr///////////////////////////
zkPlotinfo.setAttr=function(cmp,name,value){
if(cmp){
var parent=$e(getZKAttr(cmp,"pid"));
switch(name){
	case "z.dataSourceUri":
		var dataSourceUri=value;
		if(dataSourceUri!=null){
			cmp.plotinfo.dataSourceUri=dataSourceUri;
			cmp.plotinfo.ds.clear();
			cmp.plotinfo.timeplot.loadText(cmp.plotinfo.dataSourceUri,cmp.plotinfo.separator, cmp.plotinfo.ds);
			if(cmp.plotinfo.dynaDatas!=null){
			for(var evtid in cmp.plotinfo.dynaDatas){
					var evt=cmp.plotinfo.dynaDatas[evtid];
					if(evt!=null)
						cmp.plotinfo.ds._events.add(evt);
			}
			cmp.plotinfo.ds._events._index();
			cmp.plotinfo.ds._fire("onAddMany", []);
			}

		}
		break;
	case "z.eventSourceUri":
		var eventSourceUri=value;
		if(eventSourceUri!=null){
			cmp.plotinfo.eventSourceUri=eventSourceUri;
			cmp.plotinfo.es.clear();
			cmp.plotinfo.timeplot.loadXML(cmp.plotinfo.eventSourceUri, cmp.plotinfo.es);

			if(cmp.plotinfo.dynaEvents!=null){
			for(var evtid in cmp.plotinfo.dynaEvents){
					var evt=cmp.plotinfo.dynaEvents[evtid];
					if(evt!=null)
						cmp.plotinfo.es._events.add(evt);

				}
				cmp.plotinfo.es._events._index();
				cmp.plotinfo.es._fire("onAddMany", []);
				//cmp.plotinfo.timeplot.repaint();
			}

		}

		break;

	case "z.repaint":
		cmp.plotinfo.timeplot.repaint();
		break;
	case "z.dotColor":
		//set dot color
		if(value!=null)
			var dotColor=new Timeplot.Color(value);
		cmp.plotinfo["dotColor"]=dotColor;
		cmp.plotinfo.timeplot.repaint();

		break;

	case "z.showValues":
		cmp.plotinfo["showValues"]=(value=="true"?true:false);
		cmp.plotinfo.timeplot.repaint();
		break;
	case "z.lineColor":
			//set line color
			if(value!=null)
				var lineColor=new Timeplot.Color(value);
			cmp.plotinfo["lineColor"]=lineColor;
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.fillColor":
			//set fill color
			if(value!=null)
				var fillColor=new Timeplot.Color(value);
			cmp.plotinfo["fillColor"]=fillColor;
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.dotRadius":
			//set dot radius
			cmp.plotinfo["dotRadius"]=parseFloat(value);
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.lineWidth":
			//set line width
			cmp.plotinfo["lineWidth"]=parseFloat(value);
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.eventLineWidth":
			//set eventLine width
			cmp.plotinfo["eventLineWidth"]=parseFloat(value);
			cmp.plotinfo.timeplot.repaint();
		break;

	case "z.valuesOpacity":
			//set opacity
			cmp.plotinfo["valuesOpacity"]=parseInt(value);
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.bubbleWidth":
			//set bubbleWidth
			cmp.plotinfo["bubbleWidth"]=parseInt(value);
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.bubbleHeight":
			//set bubbleHeight
 			cmp.plotinfo["bubbleHeight"]=parseInt(value);
			cmp.plotinfo.timeplot.repaint();
		break;
	case "z.roundValues":
			//set roundValues
			cmp.plotinfo["roundValues"]=(value=="true"?true:false);
			cmp.plotinfo.timeplot.repaint();
		break;


}

return true;
}
};

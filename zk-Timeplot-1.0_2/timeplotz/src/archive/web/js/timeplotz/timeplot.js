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


/////////////////load  timeplot js library////////////////////////////////
zk.load("ext.timeline.api.zkTimeline-api");
//zk.load("ext.timeline.api.zkTimeline-api-bundle");
zk.load("ext.timeplot.api.simile-ajax-api");
zk.load("ext.timeplot.api.zkTimeplot-api");

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
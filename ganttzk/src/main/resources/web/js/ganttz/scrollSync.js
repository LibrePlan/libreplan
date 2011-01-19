/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function ScrollSync(element){
    var xChanges = [];
    var yChanges = [];
    var notifyScrollX = function(){
        for ( var i = 0; i < xChanges.length; i++) {
            xChanges[i]();
        }
    };
    var notifyScrollY = function(){
        for ( var i = 0; i < yChanges.length; i++) {
            yChanges[i]();
        }
    };
    var notifyListeners = function(){
        notifyScrollX();
        notifyScrollY();
    };
    var toFunction = function(value){
        var result = value;
        if(typeof(value) !== 'function'){
            result = function(){return synched};
        }
        return result;
    };

    this.synchXChangeTo = function(synched){
        var target = toFunction(synched);
        xChanges.push(function(){ target().scrollLeft = element.scrollLeft; });
    };
    this.synchYChangeTo = function(synched){
        var target = toFunction(synched);
        yChanges.push(function(){ target().scrollTop = element.scrollTop; });
    };

    this.notifyXChangeTo = function(listenerReceivingScroll){
        xChanges.push(function(){
            listenerReceivingScroll(element.scrollLeft);
        });
    };

    this.notifyYChangeTo = function(listenerReceivingScroll){
        yChanges.push(function() {
            listenerReceivingScroll(element.scrollTop);
        });
    };

    YAHOO.util.Event.addListener(element,'scroll', notifyListeners);
    return this;
}
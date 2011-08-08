/*
  This file is part of NavalPlan

  Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
                          Desenvolvemento Tecnolóxico de Galicia

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

var months = {"Jan": 0, "Feb": 1, "Mar": 2, "Apr": 3, "May": 4, "Jun": 5, "Jul": 6, "Aug": 7, "Sep": 8, "Oct": 9, "Nov": 10, "Dec": 11};

var DEFAULT_COLOR_STYLE = {"color": "blue", "bgcolor": "white"};

Array.prototype.in_array = function(p_val) {
    for(var i = 0, l = this.length; i < l; i++) {
        if(this[i] == p_val) {
            return true;
        }
    }
    return false;
}

Date.prototype.compareTo = function(other) {
    var this_milli = this.getTime();
    var other_milli = other.getTime();
    return this_milli - other_milli;
}

Date.prototype.lesserThan = function(other) {
    return this.compareTo(other) < 0;
}

Date.prototype.greaterThan = function(other) {
    return this.compareTo(other) > 0;
}

Date.prototype.equals = function(other) {
    return this.compareTo(other) == 0;
}

Date.prototype.getDaysInMonth = function() {
    return 32 - new Date(this.getFullYear(), this.getMonth(), 32).getDate();
}

String.prototype.trim = function(string) {
    return this.replace("^\s+", "").replace("\s+$", "");
}

/**
 * Returns number of month: 'Jan' => 0, 'Feb' => 1, etc
 */
function numberOfMonth(month) {
    return months[month];
}

function dateAtBeginningOfMonthSplitByComma(monthAndYear) {
    var arr = monthAndYear.split(",");
    var monthAndDay = arr[0].split(" ");

    return toDate$3(arr[1], monthAndDay[0], monthAndDay[1]);
}

function dateAtBeginningOfMonthSplitByHyphen(monthAndYear) {
    var arr = monthAndYear.split("-");
    return toDate$3(arr[2], arr[1], arr[0]);
}

function toDate$3(year, Month, day) {
    var month = numberOfMonth(Month);
    return new Date(year, month, day);
}

/**
 * Parses date to Date(). Expects date in format ISO8601 (yyyy-MM-day)
 */
function toDate(date) {
    if (date != undefined) {
        var arr = date.split("-");

        var year = arr[0];
        var month = arr[1] - 1;
        var day = arr[2];

        return new Date(year, month, day);
    }
    return null;
}

/**
 * Returns which days in date.month should be highlighted according to interval
 *
 * If interval is open, all days greater than interval.start should highlighted
 *
 */
function daysToHighlightInInterval(interval, date) {
    var start = toDate(interval.start);
    var end = toDate(interval.end);

    if (sameMonthAndYear(start, date)
            && sameMonthAndYear(start, end)) {
        return daysDelta(start.getDate(), end.getDate());
    }

    if (sameMonthAndYear(start, date)) {
        return daysDelta(start.getDate(), date.getDaysInMonth());
    }

    if (sameMonthAndYear(end, date)) {
        return daysDelta(1, end.getDate());
    }

    if (start.lesserThan(date) && (end == null || end.greaterThan(date)) ) {
        return daysDelta(1, date.getDaysInMonth());
    }

    return new Array();
}

function sameMonthAndYear(d1, d2) {
    return (d1 != null && d2 != null
                && d1.getFullYear() == d2.getFullYear()
                && d1.getMonth() == d2.getMonth());
}

/**
 * Returns an array of days from start to end (both included)
 *
 **/
function daysDelta(start, end) {
    var result = new Array();
    for (var i = start; i <= end; i++) {
        result.push(i);
    }
    return result;
}

/**
 * Highlights elements in days array, turns off those days that are not in days
 *
 **/
function setStyleForDays(nodes, days, colors) {
    nodes.each(function() {
        var day = $(this).attr("_dt");
        if (days.in_array(day)) {
            $(this).attr("style", colorStyleObj(colors));
        } else {
            $(this).removeAttr("style");
        }
    });
}

function colorStyleObj(obj) {
    return colorStyle(obj.color, obj.bgcolor, obj.bold);
}

function colorStyle(color, bgcolor, bold) {
    var cssStyle = "color: " + color + "; background-color: " + bgcolor;
    if (bold != undefined && bold) {
        cssStyle += "; font-weight: bold";
    }
    return cssStyle;
}

/**
 * Highlights those days in a calendar ZUL object that are within interval
 *
 * An interval is an object with two attributes:
 *    interval.start: date (year/month/day)
 *    interval.end: date (year/month/day)
 *
 * colorStyle is an object with two attributes:
 *   color.color: foreground color
 *   color.bgcolor: background color
 *
 * colorStyle is the color used to highlight a day (by default blue over white background)
 *
 */
function highlightDaysInInterval(uuid, intervalJSON, colorStyleJSON) {

    calendarUuid = uuid + "-pp";
    var calendar = document.getElementById(calendarUuid);
    if (calendar == null) {
        return;
    }

    var dateinput = document.getElementById(uuid + "-real");
    if (dateinput === undefined) {
        return;
    }

    var _date = dateinput.value;
    var currentDate = (_date.indexOf(",") != -1) ?
        dateAtBeginningOfMonthSplitByComma(_date) :
        dateAtBeginningOfMonthSplitByHyphen(_date);

    if (currentDate != null) {
        var interval = eval("(" + intervalJSON + ")");
        var nodes = $("#"+calendarUuid+ " td").not(".z-outside");
        var days = daysToHighlightInInterval(interval, currentDate);
        var colorStyle = (colorStyleJSON != undefined) ? eval("(" + colorStyleJSON + ")") : DEFAULT_COLOR_STYLE;

        setStyleForDays(nodes, days, colorStyle);
    }

}
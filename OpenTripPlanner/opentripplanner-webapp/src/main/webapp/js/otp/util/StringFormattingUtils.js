/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

otp.namespace("otp.util");

/**
 * these utils are for formatting a given string / object.  
 * @class 
 */
otp.util.StringFormattingUtils = {

    /** string to dollar - http://javascript.internet.com/forms/currency-format.html */
    currency : function(str)
    {
        var num = str.toString().replace(/\$|\,/g,'');
        if(isNaN(num))
            num = "0";

        var sign = (num == (num = Math.abs(num)));
        num   = Math.floor(num*100+0.50000000001);

        var cents = num%100;
        num   = Math.floor(num/100).toString();
        if(cents<10)
            cents = "0" + cents;
        
        for (var i = 0; i < Math.floor((num.length-(1+i))/3); i++)
            num = num.substring(0,num.length-(4*i+3))+','+ num.substring(num.length-(4*i+3));
        
        return (((sign)?'':'-') + '$' + num + '.' + cents);
    },

    /** */
    timeSpan : function(startTime, endTime, locale, sep)
    {
        var retVal = startTime + sep + endTime;
        try
        {
            if(sep == null)    sep = " - ";
            if(locale == null) locale = otp.config.locale;

            var tf = locale.time.time_format;
            var s = startTime.format(tf);
            var e = endTime.format(tf);
            retVal = s + sep + e;
        }
        catch(e)
        {
        }

        return retVal;
    },

    /** */
    durationHoursMins : function(durr, locale, sep)
    {
        var retVal = durr;
        try
        {
            if(sep == null)    sep = " ";
            if(locale == null) locale = otp.config.locale;

            if(durr >= 60.0 && locale.CLASS_NAME == otp.locale.English.CLASS_NAME)
            {
                var hrs  = Math.floor(durr / 60);
                var mins = durr % 60;

                var m = (mins == 1) ? locale.time.minute_abbrev : locale.time.minutes_abbrev;
                var h = (hrs  == 1) ? locale.time.hour_abbrev   : locale.time.hours_abbrev;
                retVal = hrs + " " + h + sep + mins + " " + m;
            }
            else
            {
                var m = (mins == 1) ? locale.time.minute_abbrev : locale.time.minutes_abbrev;
                retVal = durr + " " + m;
            }
        }
        catch(e)
        {
        }

        return retVal;
    },

    /** (1, transfer, transfers) returns '1 transfer' */
    numSinglePlural : function(numVal, singleStr, pluralStr, sep)
    {
        var retVal = "";
        try
        {
            if(sep == null) sep = " ";
            if(numVal)
            {
                if(numVal == 1)
                    retVal = numVal + sep + singleStr;
                else
                    retVal = numVal + sep + pluralStr;
            }
        }
        catch(e)
        {
        }

        return retVal;
    },


    /** (2, transfer, transfers) returns 'transfers 2' */
    singlePluralNum : function(numVal, singleStr, pluralStr, sep)
    {
        var retVal = "";
        try
        {
            if(sep == null)    sep = " ";
            if(locale == null) locale = otp.config.locale;

            if(numVal)
            {
                if(numVal == 1)
                    retVal = singleStr + sep + numVal;
                else
                    retVal = pluralStr + sep + numVal;
            }
        }
        catch(e)
        {
        }

        return retVal;
    },



    /** */
    percent : function(inStr, symbol)
    {
        var retVal = inStr;
        
        try
        {            
            if(inStr <= 1.0)
            {
                retVal = Math.round(inStr * 100);
            }

            if(symbol == true)
                symbol = "%";
                
            if(symbol != null)
                retVal += symbol;

        }
        catch(e)
        {}
        
        return retVal;
    },

    /** */
    formatDate : function(str)
    {
        var retVal = str;

        // assume it's the YYYYMM format
        if(str.length == 6)
            retVal = str.substr(4) + ' / ' + str.substr(0,4);
            
        return retVal; 
    },

    /** */
    isEmptyValue : function(str, elName)
    {
        if(str === null) return true;
        if(str === '')   return true;

        // check for ZERO value in these elements
        var items = ['price', 'year', 'date'];
        for(var i in items)
        {
            var n = items[i];
            if(elName.contains(n))
            {
                if(str === 0 || str === '0')
                    return true;

                break;
            }
        }

        // not empty value
        return false;
    },

    /**
     * translate a string code (eg: n/s/E/W/sw/ne/SW/Se) into a localized string
     * add pre / post fix to string (eg: <p><br/>North East<br/></p>
     * only return a string, when the translating of the direction give a value
     * otherwise return an empty string
     */
    getDirectionString : function(dir, directions, preStr, postStr, capitolize, defVal)
    {
        var retVal = '';
        var dirStr = this.getDirection(dir, directions, capitolize, defVal);
        if(dirStr && dirStr.length > 1)
        {
            // prefix added to retVal
            if(preStr && preStr.length > 0)
                retVal += preStr;

            retVal = dirStr;

            // postfix added to retVal
            if(postStr && postStr.length > 0)
                retVal += postStr;
        }
        return retVal;
    },

   /**
     * translate a string code (eg: n/s/E/W/sw/ne/SW/Se) into a localized string
    */
    getDirection : function(dir, directions, capitolize, defVal)
    {
        // default to English if directions is not the object we think it to be
        if(directions == null || directions.split != null)
            directions = otp.locale.English.directions;

        if(defVal == null)
            defVal = dir;

        var retVal = defVal;
        try
        {
            switch(dir.toLowerCase())
            {
                case 'n':  retVal = directions.north; break;
                case 's':  retVal = directions.south; break;
                case 'e':  retVal = directions.east;  break;
                case 'w':  retVal = directions.west;  break;
                case 'se': retVal = directions.southEast; break;
                case 'ne': retVal = directions.northEast; break;
                case 'sw': retVal = directions.southWest; break;
                case 'nw': retVal = directions.northWest; break;
                default  : return defVal;
            }

            if(capitolize)
            {
                retVal = this.capitolize(retVal);
            }
        }
        catch(Exp)
        {}
        
        return retVal;
    },

    capitolize : function(inWord)
    {
        var retVal = inWord;
        if(inWord != null && typeof(inWord)=='string' && inWord.length > 1)
        {
            var t = retVal.substring(0,1).toUpperCase();
            var u = retVal.substring(1);
            retVal = t + u;
        }
        return retVal;
    },


    /** returns a length 3 padded route number (assumes the num.length <= 3) */
    padRouteID : function(num)
    {
        var retVal = num;

        // zero padding for length 3 route number
        if(num.length == 1)
            retVal = '00' + num;
        else if(num.length == 2)
            retVal = '0' + num;

        return retVal;
    },

    /** TODO localize */
    serviceDayString : function(v)
    {
        var retVal = "";
        
        if(v.weekday && v.saturday && v.sunday)
           retVal = "All Days";
        else
        {
            if(v.weekday) retVal = "Weekdays";
            if(v.saturday)
            {
                if(retVal.length > 0)
                    retVal += " / "
                    
                 retVal += "Saturday";
            }  
            
            if(v.sunday)
            {
                if(retVal.length > 0)
                    retVal += " / "
                    
                 retVal += "Sunday";
            }  
        }
        
        if(retVal.length > 0)
            retVal = '<b>Service Days: </b>' + retVal;
        else
            retVal = '<b>Service Note: </b> this route <B>lacks</B> a fixed schedule (it could be a future service preview or a shuttle lacking a formal schedule).';
        
        return retVal;
    }, 

    /**
     * utility to convert z[][] to a[] (made up of z[][i] values)
     */
    arraysToStrings: function(arrays, indx, blankVal)
    {
        var retVal = [];
        var i = 0;
        if(indx != null && indx >= 0 && indx <=100)
            i = indx;

        for(var p in arrays)
        {
            var v = arrays[p][i];
            if(v == "" && blankVal)
                v = blankVal;

            retVal[p] = v; 
            //alert(p + ' : ' + retVal[p]);
        }
        
        return retVal;
    },

    /** */
    stripNonPrints : function(inStr)
    {
        // strip non printable 
        return inStr.replace(/[^a-zA-Z0-9&@()*_\-\', ]/g,"");
    },

    /** stub to remove bad words (if open to public) */
    stripCurseWords : function(inStr)
    {
        var retVal = inStr;
        return retVal;
    },

    /** */
    maxLen : function(inStr, maxSize)
    {
        if (!maxSize) maxSize = 100; 
            
        if (!inStr.length > maxSize) 
            inStr = inStr.substr(0, maxSize);
            
        return inStr;
    },


    /** */
    clean : function(inStr, defStr, maxSize)
    {
        var retVal = inStr;
        try
        {
            if(!defStr) defStr  = inStr;

            inStr = this.stripNonPrints(inStr);
            inStr = this.stripCurseWords(inStr);
            inStr = this.maxLen(inStr, maxSize);
            retVal = inStr;
        }
        catch(ex)
        {
            retVal = defStr;
        }
        
        return retVal;
    },


    CLASS_NAME: "otp.util.StringFormattingUtils"
};

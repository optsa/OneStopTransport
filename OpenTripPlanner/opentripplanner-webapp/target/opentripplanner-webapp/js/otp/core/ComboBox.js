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

otp.namespace("otp.core");

/**
 * Simple Ext Text form with Cookie backed memory
 * @class
 */
otp.core.ComboBoxStatic = {

    maxNumValues : 10,
    id           : 'cb.id',
    name         : 'cb.name',
    label        : 'Form',
    display      : 'display',
    cls          : '',
    changeHandler: null,

    anchor       : '95%',
    msgTarget    : 'qtip',
    emptyText    : '...',
    maxHeight    : 150,

    // pointer to other external objects
    locale       : null,
    poi          : null,
    form         : null,

    // internal objects
    m_store      : null,
    m_template   : null,
    m_lastValue  : null,
    m_forceDirty : false,

    geocodeName  : null,
    geocodeCoord : null,
    geocodeRec   : null,
    appendGeocodeName : false,

    /**
     * constructor of sorts
     */
    initialize : function(config)
    {
        otp.configure(this, config);
        
        // we can also have a change handler passed in,
        // which otp.configure doesn't copy over because
        // it's a function
        if (typeof config.changeHandler === "function") {
            this.changeHandler = config.changeHandler;
        }
        
        this.m_store = new Ext.data.SimpleStore({
            fields: [this.display],
            data: Ext.state.Manager.get(this.id, [])
        });

        this.m_template = new Ext.XTemplate(
            '<tpl for=".">',
            '<div class="x-combo-list-item" >',
              '{' + this.display + '}',
            '</div>',
            '</tpl>'
        );
        
        var formOptions = {
                id:            this.id,
                cls:           this.cls,
                hiddenName:    this.name,
                fieldLabel:    this.label,
                displayField:  this.display,
                msgTarget:     this.msgTarget,
                tpl:           this.m_template,
                emptyText:     this.emptyText,
                valueNotFoundText: '',
                store:         this.m_store,
                mode:          'local',
                anchor:        this.anchor,
                triggerAction: 'all',
                allowBlank:    false,
                typeAhead:     false,
                resizable:     true,
                maxHeight :    this.maxHeight,
                lazyRender:    false,
                selectOnFocus: true,
                hideLabel:     true
        };
        if (this.changeHandler) {
            formOptions.listeners = {change: this.changeHandler};
        }

        this.form = new Ext.form.ComboBox(formOptions);
    },

    /** */
    selectCB : function(combo, record, num)
    {
         console.log("Stub ComboBox.selectCB -- not doing anything...")
    },

    /** dirty means that the form is not empty, and it's different than it was before */
    isDirty : function()
    {
        var retVal = false;
        var v = this.form.getRawValue();

        // to be considered dirty, there must first be some content in the form
        if(v != null && v.length > 0)
        {
            // and the existing form content doesn't match what was in the form previously
            retVal = (v != this.m_lastValue);
            if(this.m_forceDirty)
            {
                retVal = true;
                this.m_forceDirty = false;
            }
        }
        return retVal;
    },

    /** */
    setDirty : function()
    {
        this.m_forceDirty = true;
    },


    /** */
    setGeocodeName : function(name, doUpdate)
    {
        this.geocodeName = name;
        if (doUpdate)
        {
            this.setRawValue(name);
            this.setLastValue();
        }
    },

    /** */
    setGeocodeCoord : function(coord, record)
    {
        this.geocodeCoord = coord;
        this.geocodeRec   = record;

        // cleanup ... no 0.0,0.0
        if(coord && coord.indexOf('0.0') == 0 && coord.indexOf('0.0') != coord.lastIndexOf('0.0'))
            this.geocodeCoord = null;
    },

    /** */
    getGeocodeCoord : function()
    {
        return otp.util.ObjUtils.getCoordinate(this.geocodeCoord);
    },

    /** */
    getNamedCoord : function()
    {
        return this.makeGeoParam(this.geocodeName, this.geocodeCoord);
    },

    /** */
    setNamedCoord : function(nc, rec, doUpdate)
    {
        var pg = this.parseGeoParam(nc);
        this.setGeocodeCoord(pg.ll,  rec);
        this.setGeocodeName(pg.name || pg.ll, doUpdate);
    },

    /** */
    setNameLatLon : function(name, lat, lon, rec, doUpdate)
    {
        var ll = (1 * lat).toFixed(6) + ',' + (1 * lon).toFixed(6);
        ll = otp.util.ObjUtils.getCoordinate(ll);
        this.setGeocodeCoord(ll, rec);
        this.setGeocodeName(name || ll, doUpdate);
    },

    /**
     * persist Ext ComboBox's text field content into a Cookie
     */
    persist : function(text) 
    {
        if(this.m_store == null) return;

        // either use passed in text
        if(Ext.isEmpty(text))
        {
            // or use form's raw value to persist
            text = this.getRawValue();
            if(Ext.isEmpty(text))
                return;
        }

        // and check to see if this text value already exists in the cache
        this.m_store.clearFilter(false);

        // note: this find method will not allow prefixes to be added (eg: '2' will fail if there's already a value of '2155')
        var ff = this.m_store.find(this.display, text, 0, false, true);
        if(ff < 0) 
        {
            var data = [[text]];
            var count = this.m_store.getTotalCount();
            var limit = count > this.maxNumValues ? this.maxNumValues - 1 : count;

            for (var i = 0; i < limit; i++)
                data.push([this.m_store.getAt(i).get(this.display)]);

            // the value doesn't yet exist here, so store it
            var p = Ext.state.Manager.getProvider();
            if(p)
            {
                p.expires = otp.util.DateUtils.addDays(365);
                Ext.state.Manager.set(this.id, data); 
            }

            this.m_store.loadData(data);
        }

        // here for debug purposes
        ff = null;
    },

    /** return ExtComboBox */
    getComboBox : function()
    {
        return this.form;
    },
    
    /** return ExtComboBox's current text value */
    getRawValue : function()
    {
        return this.form.getRawValue();
    },
    
    /** set the value of this combo box's text */
    setRawValue : function(val)
    {
        this.form.setRawValue(val);
        this.setLastValue(val);
    },

    setLastValue : function(val)
    {
        if(val == null)
            val = this.getRawValue();

        this.m_lastValue = val;
    },

    /** TODO allow the form to load it's store from another store, in combo with a template */
    load : function(store, template)
    {
    },

    /** */
    clear : function()
    {
        this.form.collapse();
        this.form.reset();
        this.clearGeocode();
    },

    /** */
    clearGeocode : function()
    {
        this.geocodeName  = null;
        this.geocodeCoord = null;
        this.geocodeRec   = null;
    },


    /** */
    blur : function()
    {
        if(this.isDirty())
            this.form.triggerBlur();
    },

    collapse : function()
    {
        this.form.collapse();
    },

    /** */
    reverse : function(combo)
    {
        try
        {
            var tmp = this.getRawValue();
            this.setRawValue(combo.getRawValue());
            combo.setRawValue(tmp);

            this.swapElements(combo, 'geocodeName');
            this.swapElements(combo, 'geocodeCoord');
            this.swapElements(combo, 'geocodeRec');
        }
        catch(e)
        {}
    },


    /** parse a string param -- looking to separate any name :: separation from coordinate data */
    parseGeoParam : function(p)
    {
        var retVal = null;

        if(p)
        {
            retVal = {};

            var ll = p; 

            // process named coordinates, ala NAME::lat,lon
            var s  = p.indexOf("::");
            if(s && s > 0)
            {
                retVal.name = p.substr(0, s);
                ll = p.substr(s+2);
            }

            // process coordinates part of the string
            retVal.ll  = ll; 
            retVal.lat = otp.util.ObjUtils.getLat(ll);
            retVal.lon = otp.util.ObjUtils.getLon(ll);
        }

        return retVal;
    },

    /** builds the 'place' parameter that gets sent down to OTP routing */
    makeGeoParam : function(name, coord)
    {
        var retVal = null;

        if(coord && coord.indexOf(',') > 0)
            retVal = coord;

        // append geocoder string, which OTP will pass back to us in the response
        if(this.appendGeocodeName && name && name.length > 0 && !otp.util.ObjUtils.isCoordinate(name))
            retVal = name + '::' + retVal;

        return retVal;
    },

    CLASS_NAME : "otp.core.ComboBox"
};
otp.core.ComboBox = new otp.Class(otp.core.ComboBoxStatic);

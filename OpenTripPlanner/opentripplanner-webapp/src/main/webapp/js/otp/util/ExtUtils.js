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

//EXT CONSTANTS
try
{
    Ext.BLANK_IMAGE_URL = 'images/ui/s.gif';
    Ext.form.Field.prototype.msgTarget = 'side';
    Ext.QuickTips.init();

    // Set the HTTP accept header for AJAX requests to make sure we get XML
    // (If we don't set this, the accept header is not consistently set across
    // browsers, with Safari/Chrome accepting JSON, which causes our XHR
    // requests to fail. See http://opentripplanner.org/ticket/84)
    Ext.Ajax.defaultHeaders = {
        'Accept': 'application/xml'
    };
    // fixes the Permission denied to access property 'dom' from a non-chrome context bug from Ext 3.0
    // http://extjs.com/forum/showthread.php?p=366510#post366510
    // August 10, 2009
    Ext.lib.Event.resolveTextNode = Ext.isGecko ?
        function(node)
        {
            var retVal = null;
            try
            {
                if(!node){
                    return;
                }
                var s = HTMLElement.prototype.toString.call(node);
                if(s == '[xpconnect wrapped native prototype]' || s == '[object XULElement]'){
                    return;
                }
                retVal = node.nodeType == 3 ? node.parentNode : node;
            }
            catch(e)
            {
                console.log("ExtUtils exception in Ext.lib.Event.resolveTextNode override for isGecko: " + e);
            }
            return retVal;
        } 
        : // ? : else  
        function(node)
        {
            var retVal = null;
            try
            {
                retVal = node && node.nodeType == 3 ? node.parentNode : node;
            }
            catch(e)
            {
                console.log("ExtUtils exception in Ext.lib.Event.resolveTextNode override: " + e);
            }
            return retVal;
        }
    ; // end Ext.libEvent.resolveTextNode


    //
    // From Ext Demo - A reusable error reader class for XML forms
    // Note: not really used, but here if needed (but it is used in the form error handler...)
    //
    Ext.form.XmlErrorReader = function() {
       Ext.form.XmlErrorReader.superclass.constructor.call(this, {
             record : 'field',
             success: '@success'
         }, 
         [ 'id', 'msg']
       );
    };
    Ext.extend(Ext.form.XmlErrorReader, Ext.data.XmlReader);

    //
    //  Stores used in ExtUtils
    //
    otp.util.OPT_ARRAY   = ['opt', 'text'];
    otp.util.OPT_RECORD  = new Ext.data.Record.create(otp.util.OPT_ARRAY);


    /** 
     * tree node plugin 
     * @see http://www.sencha.com/forum/showthread.php?23479-TreeNode-mouseover-event&p=180375
     */
    Ext.tree.NodeMouseoverPlugin = Ext.extend(Object, {
        init: function(tree) {
            if (!tree.rendered) {
                tree.on('render', function() {this.init(tree)}, this);
                return;
            }
            this.tree = tree;
            //tree.body.on('mouseover', this.onTreeMouseover, this, {delegate: 'a.x-tree-node-anchor'});
            //tree.body.on('mouseout',  this.onTreeMouseout,  this, {delegate: 'a.x-tree-node-anchor'});
            tree.body.on('mouseover', this.onTreeMouseover, this, {delegate: 'div.x-tree-node-el'});
            tree.body.on('mouseout',  this.onTreeMouseout,  this, {delegate: 'div.x-tree-node-el'});
        },
    
        XfireEvent : function(e, t, event) {
            var nodeEl = Ext.fly(t).up('div.x-tree-node-el');
            if (nodeEl) {
                var nodeId = nodeEl.getAttributeNS('ext', 'tree-node-id');
                if (nodeId) {
                    var node = this.tree.getNodeById(nodeId);
                    if(node) {
                        node.fireEvent(event, node, e);
                    }
                    
                }
            }
        },
    
        fireEvent : function(e, t, event) {
            if(t && t.attributes) 
            {
                var nodeId = t.getAttribute('ext:tree-node-id');
                if (nodeId) {
                    var node = this.tree.getNodeById(nodeId);
                    if(node) {
                        node.fireEvent(event, node, e);
                    }
                }
            }
        },
    
        onTreeMouseover: function(e, t) {
            this.fireEvent(e, t, 'mouseover');
        },

        onTreeMouseout: function(e, t) {
            this.fireEvent(e, t, 'mouseout');
        }
    });
}
catch(e)
{
    console.log("Ext exception can be ignored -- just means you aren't including Ext.js in your app, which is OK");
}

/**
 * @class 
 */
otp.util.ExtUtils = {

    MAP_ATTRIBUTION    : '<a href="javascript:void(0);" onclick="otp.util.ExtUtils.toggleAboutMap();">Powered by OpenGeo</a>',
    ABOUT_MAP_WINDOW   : null,


    /** first five params are required...config param is optional to override things */
    makeJsonStore : function(url, id, total, root, fields, config)
    {
        // json reader / store
        var c = {
            proxy: new Ext.data.HttpProxy({method: 'GET', url:url}),

            // store config
            autoDestroy     : true,
            autoSave        : false,
            autoLoad        : false,
            restful         : false,  // false = load() request is a POST; true = request becomes a GET

            // reader configs
            idProperty      : id,
            totalProperty   : total,
            root            : root,
            fields          : fields
        };
        otp.extend(c, config);

        var retVal = new Ext.data.JsonStore(c);
        retVal.proxy.on("exception",
            function(p, type, action){console.log("EXCEPTION: ExtUtils.makeJsonStore error type:", type, ", action:", action);}
        );
        return retVal;
    },


    /** */
    setCookie : function(name, value, date, path, domain) 
    {
        try
        {
            // make expire date 1 year into the future...
            if(date == null)
                date = otp.util.DateUtils.addDays(365);

            if(path == null)
                path = '/';

            if(domain == null)
                domain = 'opentripplanner.org';

            Ext.util.Cookies.set(name, value, date, path, domain);
        }
        catch(e)
        {
            console.log("ExtUtils.setCookie() " + e);
        }
    },

    /** this cookie is shared by both OLD text trip planner and newer MAP trip planner as a way to flag customer preference */
    setTripPlannerCookie : function(date) 
    {
        otp.util.ExtUtils.setCookie(otp.util.Constants.TP_COOKIE_NAME, otp.util.Constants.TP_COOKIE_VALUE, date);
    },

    /** assumes a couple of divs named loading & loading-mask exist in html */
    clearSplashScreen : function(time, divName, maskName)
    {
        try
        {
            if(time     == null) time     = 500;
            if(divName  == null) divName  = 'loading'; 
            if(maskName == null) maskName = divName + '-mask';
    
            // clear out the loading message
            setTimeout(function(){
                try{Ext.get(divName).remove();}catch(e){}
                try{Ext.get(maskName).fadeOut({remove:true});}catch(e){}
            }, time);
        }
        catch(e)
        {
            console.log("ExtUtils.clearSplashScreen() " + e);
        }
    },

    /**
     * makes a simple popup dialog, with content from a URL
     */
    makePopup : function(inConfig, title, isShow, width, height, closeable, noCloseButton, x, y)
    {
        var c = {
            title:       title != null ? title  : 'About',
            layout:      'auto',
            closeable:   closeable == true ? true    : false,
            closeAction: closeable == true ? 'close' : 'hide',
            width:       width  != null ? width  : 600,
            height:      height != null ? height : 370,
            plain:       true,
            x:           x,
            y:           y,
            buttonAlign: 'center'
        };
        otp.extend(c,    inConfig);
        otp.configure(c, inConfig);

        return otp.util.ExtUtils.makePopupWithConfig(c, isShow, closeable, noCloseButton);
    },

    /** */
    makePopupWithConfig : function(config, isShow, closeable, noCloseButton)
    {

        // create the window on the first click and reuse on subsequent clicks
        var retVal = new Ext.Window(config);

        // button close
        if(closeable == true && noCloseButton != true)
        {
            var b = new Ext.Button(
            {
                text:  'Close',
                scope: retVal,
                handler: function(a, b)
                {
                    this.close();
                }
            });

            retVal.addButton(b);
        }

        if(isShow === true)
            retVal.show(this);

        // timeout used to close this popup after so many seconds...
        if(config.timeout && config.timeout > 0) {
            setTimeout(function(){
                retVal.close(this);
            }, config.timeout * 1000);
        }

        return retVal;
    },

    /**
     * makes a simple popup dialog, with content from a URL
     */
    makeHtmlPopup: function(url, title, isShow, width, height, closeable, noCloseButton)
    {
        var c = {autoLoad:url, y:100, width:(width != null ? width : 600), layout:'auto', buttonAlign:'center'};
        if(title)  c.title  = title;
        if(height) c.height = height;

        return otp.util.ExtUtils.makePopupWithConfig(c, isShow, closeable, noCloseButton);
    },

    /**
     * within an exception block because if the form is hidden, Ext throws an error
     */
    formSetRawValue : function(form, value, defValue, dirty)
    {
        try
        {
            var v = defValue;
            if(value)
                v = value;
            if(v)
                form.setRawValue(v);
            if(dirty && form.setDirty)
                form.setDirty();
        }
        catch(e)
        {}
    },

    /** 
     * makes a simple popup dialog, with popup items and buttons
     */
    makeFormPopup : function(items, title, isShow, width, height)
    {
        return otp.util.ExtUtils.makePopup({items:items}, title, isShow, width, height);
    },

    /** */
    toggleAboutMap : function()
    {
        if(this.ABOUT_MAP_WINDOW == null)
            this.ABOUT_MAP_WINDOW = otp.util.ExtUtils.makeHtmlPopup('about.html', 'About this map');
            
        this.togglePopup(this.ABOUT_MAP_WINDOW);
    },

    /** */
    togglePopup : function(pop)
    {
        if(pop && pop.show && pop.hide)
        {
            if(pop.isVisible())
                pop.hide();
            else
                pop.show();
        }
    },

    /** */
    makeTextPanel : function(defText)
    {
        if(defText == null)
            defText = '...';
        
        var retVal = new Ext.Panel({
            title:  'Information',
            xtype:  'panel',
            autoScroll: true,
            html:   defText
        });
        
        return retVal;
    },


    /** 
     * make a 'tool' button
     * default is a clear button, with handler that is Panel.m_control.clear()
     */
    makeToolButton : function(handler, id, tip)
    {
        if(id == null)
            id = 'refresh'; // refresh maps to CSS entry for a refresh image (eg: 2 circular arrows)
        if(tip == null)
            tip = '<b>Clear</b><br/>Clears the map of all selections and feature detail windows.';
        if(handler == null)
            handler = function(e, tE, P){ P.m_control.clear();};
            
        return { 
            id   : id,
            qtip : tip,
            handler: handler
        };
    },


    /**
     * utility to set a callback, with param for setting scope of callback
     * 
     * @param {Object} node
     * @param {Object} event
     * @param {Object} callback
     * @param {Object} scopeObj
     */     
    setCallback : function(node, event, callback, scopeObj)
    {
        if(node && event && callback)
        {
            var e = {};
            e[event] = callback;
            if(scopeObj)
                e['scope'] = scopeObj;
            node.on(e);
        }
    },

    setClickCallback: function(node, callback, scopeObj)
    {
        this.setCallback(node, "click", callback, scopeObj);
    },

    setMouseOverCallback: function(node, callback, scopeObj)
    {
        this.setCallback(node, "mouseover", callback, scopeObj);
    },

    setMouseOutCallback: function(node, callback, scopeObj)
    {
        this.setCallback(node, "mouseout", callback, scopeObj);
    },


    /**
     * given a mouseEl (or context menu el), and another panel (say a panel containing a map), return the pixel X,Y 
     */
    getPixelXY: function(mouseEl, panelEl)
    {
        var mouseXY = mouseEl.getXY();
        if(panelEl == null)
            panelEl = Ext.get(otp.util.OpenLayersUtils.MAP_PANEL);
        
        var panelXY = panelEl.getOffsetsTo(document.body);
        var x = mouseXY[0] - panelXY[0];
        var y = mouseXY[1] - panelXY[1];

        return {x: x, y: y};
    },

    //
    // Grid Utils
    //

    /** */
    gridClick : function (grid, rowIndex, props) 
    {
        // prevents repeated popup opening, thus swalling other events (like hyper-links w/in grid)
        if(props == null) 
            props = {description:'', x:'', y:''};

        try
        {
            this.m_lastRowIndex = rowIndex;
            var record = grid.getStore().getAt(rowIndex);

            // get props from the grid store for this row
            for(var p in props) 
            {
                var tmp = record.get(p);
                props[p] = tmp;
            }
        }
        catch(e)
        {
            
        }

        return props;
    },

    /** */
    makeListView : function(store, cols, config)
    {
        if(cols      == null) 
            cols = [{
                header:    'Description',
                width:     .9,
                dataIndex: 'description'
            }];

        var c = {
            layout:      'auto',
            store:       store,
            stripeRows:  true,
            autoShow:    true,
            columns:     cols
        };
        otp.extend(c, config);
        otp.configure(c, config);

        var listView = new Ext.ListView(c);
        return listView;
    },

    /** */
    makeGridView : function(store, cols, config, isMultiSelect)
    {
        if(cols == null) 
            cols = [{
                header:    'Description',
                width:     .9,
                dataIndex: 'description'
            }];

        var c = {
            layout:      'fit',
            height:       150,
            store:       store,
            stripeRows:  true,
            bodyBorder:  false,
            autoShow:    true,
            autoDestroy: true,
            sortable:    true,
            sm:          new Ext.grid.RowSelectionModel({singleSelect : isMultiSelect == true ? false : true}),
            deferRowRender : true,
            forceLayout    : true,
            viewConfig: {
                 forceFit:true,
                  enableRowBody: true
            },
            columns:     cols
        };
        otp.extend(c, config);
        otp.configure(c, config);

        if(c.height == null) c.autoHeight = true;
        if(c.width  == null)  c.autoWidth  = true;

        var retVal = new Ext.grid.GridPanel(c);
        return retVal;
    },


    //////////////////////  TREE UTILS  //////////////////////  TREE UTILS  //////////////////////

    
    /**
     * makes a new tree object
     * 
     * @param {String}  id
     * @param {String}  text
     * @param {String}  nodeCLS
     * @param {String}  iconCLS
     * @param {Boolean} isLeaf
     * @param {Function} clickCallback
     * @param {Object}   scope (for callback)
     */
    makeTreeNode : function(treeNodeConfig, clickCallback, scope, overCallback, outCallback)
    {
        // NOTE: href:# fixes the bug where clicking / dbl-clicking on tree nodes reloads the app url (trip details node)
        var configDefaults = {href:"javascript:void(0);", margins: '0 0 0 0', cmargins: '0 2 0 0', expanded: true, collapsible: true};
        var config = Ext.apply({}, treeNodeConfig, configDefaults);
        var treeNode = new Ext.tree.TreeNode(config);
        this.setClickCallback(treeNode,     clickCallback, scope);
        this.setMouseOverCallback(treeNode, overCallback,  scope);
        this.setMouseOutCallback(treeNode,  outCallback,   scope);
        return treeNode;
    },


    /** */
    clearTreeNode : function(node)
    {
        try 
        {
            node.remove();
        }
        catch(err) 
        {
        }
        try 
        {
            node.destroy();
        } 
        catch(err) 
        {
        }
    },

    /** */
    clearTreeNodes: function(tree)
    {
        try 
        {
            var nodes = tree.root.childNodes;
            for(var i = nodes.length - 1; i >= 0; i--)
            {
                var c1 = nodes[i].childNodes;
                if(c1)
                {
                    for (var j = c1.length - 1; j >= 0; j--) 
                    {
                        var c2 = c1[j].childNodes;
                        if(c2)
                        {
                            for (var k = c2.length - 1; k >= 0; k--) 
                            {
                                this.clearTreeNode(c2[k]);
                            }
                        }
                        this.clearTreeNode(c1[j]);
                    }
                }
                this.clearTreeNode(nodes[i]);
            }
        } 
        catch (err) 
        {
        }
    },

    /** */
    copyTreeNodes: function(nodes, clickCallback, scope)
    {
        var retVal = new Array();
        try 
        {
            var len = nodes.length;
            for(var i = 0; i < len; i++)
            {
                retVal[i] = new Ext.tree.TreeNode(Ext.apply({}, nodes[i].attributes));
                this.setClickCallback(retVal[i], clickCallback, scope);
            }
            
            var chiles = new Array();
            var children = nodes[1].childNodes;
            var len = children.length;
            for(var j = 0; j < len; j++)
            {
                chiles[j] = new Ext.tree.TreeNode(Ext.apply({}, children[j].attributes));
                this.setClickCallback(chiles[j], clickCallback, scope);
            }
            retVal[1].appendChild(chiles);
        }
        catch (err) 
        {
        }
        
        return retVal;
    },

    /**
     * This is for things like the ARRIVE/DEPART pull-down forms 
     */
    makeStaticPullDownStore: function( data )
    {
        return new Ext.data.SimpleStore({
            fields: otp.util.OPT_ARRAY,
            data:   data 
        });
    },



    /////////////// POINT STORE UTILS ///////////////

    // NOTE: Point Store is something used on the map trip planner.  It's reads an XML file in a format that
    // represents point locations (with enough params for a transit stop).
    // Thought I'd put it here in case others have a use...

    // POINT STORE COORDINATE UTILS //

    /**
     * Will pull out X/Y or Lon/Lat from a store we'd define (see below)
     */
    getCoordinateByString : function(key, store, defVal, isLatLon)
    {
        var i   = store.find('description', key);
        var rec = store.getAt(i);
        return otp.util.ExtUtils.getCoordinate(rec, defVal, isLatLon);
    },

    /** */
    getCoordinateByIndex: function(store, index, defVal, isLatLon)
    {
        var rec = store.getAt(index);
        return otp.util.ExtUtils.getCoordinate(rec, defVal, isLatLon);
    },

    /** */
    getCoordinate: function(record, defVal)
    {
        var retVal = defVal;

        try
        {
            var x = record.get('lon');
            var y = record.get('lat');
            
            retVal = x + "," + y;
        }
        catch(err)
        {
            retVal = defVal;
        }

        return retVal;
    },

    /** */
    getName : function(record, defVal)
    {
        var name = null;
        try
        {
            name = record.get('name');
        }
        catch(err)
        {
            name = defVal;
        }

        return name;
    },

    /** */
    getNamedCoordinate : function(record, defVal)
    {
        var coord = this.getCoordinate(record, defVal);
        var name  = this.getName(record);

        var retVal = coord;
        if(name)
            retVal = name + "::" + coord;

        return retVal;
    },

    /**
     * makes a point record (based on type ID)
     */
    makePointRecord : function(typeID) 
    {
        var retVal = null;
        try
        {
            if(typeID == null || typeID == '')
                typeID = '';
            else
                typeID = typeID + '/';

            retVal = new Ext.data.Record.create([
              {name: 'id',          mapping: '@id'},
              {name: 'mode',        mapping: '@mode'},
              {name: 'order',       mapping: '@order'},
              {name: 'routeID',     mapping: '@route'},
              {name: 'lat',         mapping: typeID + 'pos/lat'},
              {name: 'lon',         mapping: typeID + 'pos/lon'},
              {name: 'name',        mapping: typeID + 'name'},
              {name: 'stopId',      mapping: typeID + 'stopId'}, 
              {name: 'areaKey',     mapping: typeID + '@areaKey'},
              {name: 'areaValue',   mapping: typeID + '@areaValue'},
              {name: 'geometry',    mapping: typeID + 'geometry', 
                                    convert: function(n,p) { return otp.util.OpenLayersUtils.geo_json_converter(n,p); }},
              {name: 'agencyId',    convert: function(val, rec) {
                                                 var node = rec;
                                                 if (node.nodeName !== 'leg') {
                                                     node = rec.parentNode;
                                                 }
                                                 return node.nodeName === 'leg'
                                                        ? node.getAttribute('agencyId')
                                                        : null;
                                             }}
            ]);
        }
        catch(e)
        {
            console.log("EXCEPTION: ExtUtils.makePointRecord() " + e);
        }

        return retVal;
    },

    /**
     * makes a point store based on  
     */
    makePointStore : function(nodeID, typeID)
    {
        if(nodeID == null)
            nodeID = 'leg'; // default parent of points are leg nodes

        var rec = this.makePointRecord(typeID);
        return new Ext.data.Store(
        {
            fields   : rec,
            data     : Ext.state.Manager.get('PointStore', []),
            reader   : new Ext.data.XmlReader({ record: nodeID }, rec )
        });
    },    
    
    /** */
    makeLocationStore : function(store)
    { 
        // if a store is passed in, then just empty it and return it
        if(store && store.removeAll)
        {
            store.removeAll();
            return store;
        }

        return this.makePointStore('location', ''); 
    },

    /**
     * loads the first to & from of the trip plan response into a point record
     * return: a populated point record
     */ 
    loadPointRecord : function(nodeID, xmlDoc) 
    {
        var retVal = null;
        try
        {
            var store = this.makePointStore(nodeID, '');
            var nodes = Ext.DomQuery.select('response', xmlDoc);
            store.loadData(nodes);
            retVal = store.getAt(0);
        }
        catch(e)
        {
            retVal = this.makePointRecord(nodeID);  // TODO NICK: the param here was 'typeID'...changed to nodeID so it wouldn't throw exception
            console.log("EXCEPTION: ExtUtils.loadPointRecord " + e);
        }

        return retVal;
    },

    CLASS_NAME: "otp.util.ExtUtils"
};

// TreeMenu.js, Version 1.0, 2004/03
// Copyright (C) 2004 by Hans Bauer, Schillerstr. 30, D-73072 Donzdorf
//                       http://www.h-bauer.de
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation.
//
function Node(id,indent,text,target,url,tooltip,iconOpen,iconClose,isOpen) {    //>Node (Folder or Item)
 this.id        = id;                 this.indent    = indent;                  // Initialize variables
 this.text      = text;               this.target    = target;                  //     ..        ..
 this.url       = url;                this.tooltip   = tooltip;                 //     ..        ..
 this.iconOpen  = iconOpen;           this.iconClose = iconClose;               //     ..        ..
 this.parent    = null;               this.childs    = [];                      //     ..        ..
 this.isOpen    = isOpen;   }                                                   //     ..        ..

function treemenu(name, showLines, showIcons, useCookies) {                     //>treemenu
 this.name      = name;               this.showLines = showLines;               // Initialize variables
 this.showIcons = showIcons;          this.useCookies= useCookies;              //     ..        ..
 this.nodes     = [];                 this.root      = new Node(-1,-1,'root');  //     ..        ..
 this.selected  = -1;                 this.maxIndent = 0;                       //     ..        ..
 this.expire    = 1;                  this.openNodes = '';                      //     ..        ..
 this.classDepth= 2;                                                            // ClassDepth for text-format-> css-file
 this.readCookies();                                                            // Read cookies if available
 if (!navigator.cookieEnabled) this.useCookies = false;                         // Respect the browsers cookie setting
 this.defaults = {                                                              // Default images/icons
   iconRoot  : 'gif/root.gif',        iconItem  : 'gif/item.gif',               //    ..        ..
   iconOpen  : 'gif/open.gif',        iconClose : 'gif/close.gif',              //    ..        ..
   passLine  : 'gif/passline.gif',    empty     : 'gif/empty.gif',              //    ..        ..
   tieLine   : 'gif/tieline.gif',     tiePlus   : 'gif/tieplus.gif',            //    ..        ..
   endLine   : 'gif/endline.gif',     endPlus   : 'gif/endplus.gif',            //    ..        ..
   rectPlus  : 'gif/rectplus.gif',    tieMinus  : 'gif/tieminus.gif',           //    ..        ..
   rectMinus : 'gif/rectminus.gif',   endMinus  : 'gif/endminus.gif',           //    ..        ..
   minIcon   : 'gif/minicon.gif'  } }                                           //
                                                                                // ----------- Build up menu -----------
treemenu.prototype.put = function(open, label, target, url,                     //>Put a node to the treemenu
                                  tooltip, iconOpen, iconClose) {               //     that is initially to be loaded
 if (this.selected==-1) this.selected = this.nodes.length;                      // Set 'selected' if not cookie-defined
 this.add(open, label, target, url, tooltip, iconOpen, iconClose); }            // Add a node to the treemenu

treemenu.prototype.add = function(open, label, target, url,                     //>Add a node to the treemenu
                                  tooltip, iconOpen, iconClose) {               //
 var indent = 0;                                                                // Indent: initialize
 while (label.charAt(indent)==' ') indent++;                                    //   Indent by leading spaces
 if (this.maxIndent<indent) this.maxIndent = indent;                            //   Adjust 'maxIndent'
 var id     = this.nodes.length;                                                // ID of the new node
 var isOpen = (open==0) ? false : true;                                         // IsOpen from given value '0' or '1'
 if (this.openNodes && id<this.openNodes.length)                                // On given 'OpenNodes'
     isOpen = (this.openNodes.charAt(id)=='1') ? true : false;                  // -> Status depending on cookie
 var node   = new Node(id, indent, label.substr(indent),                        // New node: ID corresponds with number
                       target, url, tooltip, iconOpen, iconClose, isOpen);      //   Text without leading spaces
 this.nodes[this.nodes.length] = node;                                          //   Append node to the nodes-array
 for (i=this.nodes.length-1; i>=0; i--)                                         // Parent node:
   if (this.nodes[i].indent < indent) { node.parent = this.nodes[i];   break; } //   Loop back to find parent by indent
 if (!node.parent) node.parent = this.root;                                     //   Root-node is parent if none found
 if (node.parent.indent<node.indent-1)                                          //   Invalid indent
     alert('Indent of "' + node.text + '" must be <' + (node.parent.indent+2)); //   -> alert-message
 node.parent.childs[node.parent.childs.length] = node; }                        //   New node is child of the parent

                                                                                // ---------- Build Html-code ----------
treemenu.prototype.toString = function() {                                      //>ToString used by document.write(...)
 var str = '<div class="TreeMenu">';                                            // Encapsulate class 'TreeMenu'
 var lastIndent = 0;                                                            // Initialize lastIndent
 for (id=0; id<this.nodes.length; id++) {                                       // Loop: Nodes
   var node = this.nodes[id]                                                    //   Current node
   if (lastIndent < node.indent) lastIndent = node.indent;                      //   Update lastIndent to max
   while (lastIndent>node.indent) { str += '</div>';   lastIndent--; }          //   Close previous </div>-Subtrees
   str += this.writeNode(node);                                                 //   Write node
   if (0<node.childs.length) {                                                  //   Parent -> SubTree of childs
     str += '<div id="' + this.name + 'SubTree_' + id                           //   -> Write <div..-block to display
         +  '" style="display:'                                                 //            or to hide the SubTree
         +  ((node.isOpen) ? 'block' : 'none') + '">'; } }                      //            according to isOpen-value
 for (i=lastIndent; i>0; i--) str += '</div>';                                  // Close remaining SubTrees
 str += this.writeCreatedWithTreeMenu();                                        // Write CreatedWithTreeMenu
 str += '</div>';                                                               // Close class 'TreeMenu'
 this.setCookies(this.expire);                                                  // Set Cookies
 this.loadSelected();                                                           // LoadSelected on already filled frames
// alert(str);                                                                  // Discomment to see Html-Code
 return str;  }                                                                 // Return HTML-String

                                                                                // -------------- Write ----------------
treemenu.prototype.writeNode = function(node) {                                 //>WriteNode
 if (node.target=='hide') return '';                                            // Only node with no hidden target
 var str = '<div>'                                                              // Open <div>-block for the node
         + this.writeIndenting(node)  + this.writeTieUpIcon(node) + '&nbsp;'    // Write Indenting, tieUpIcon
         + this.writeNodeSymbol(node) + this.writeNodeText(node) + '</div>';    //       Symbol, Text, close 'TreeNode'
 return str; }                                                                  // Return cumulated Html-String

treemenu.prototype.writeIndenting = function(node) {                            //>WriteIndenting
 if (node.indent < 2) return '';                                                // Only if node-indent >= 2
 var str      = '';                                                             // Initialize str
 var icons    = [];                                                             //            icons[]
 var ancestor = node.parent;                                                    // Start at ancestor = node.parent
 for (i=node.indent-2; i>=0; i--, ancestor=ancestor.parent) {                   // Loop ancestors from right to left
      icons[i] = (this.isLastChild(ancestor) ? 'empty' : 'passLine');  }        //   Last child -> empty, else passLine
 for (i=0; i<=node.indent-2; i++) {                                             // Loop from left to right:
      var icon = this.defaults.empty;                                           //   Default icon = empty
      if (this.showLines && icons[i]!='empty') icon = this.defaults.passLine;   //   or passLine to be shown
      str += '<img name="' + icons[i] + '" src="' + icon + '" alt="" />'; }     //   Html-string for the icon
 return str;  }                                                                 // Return html-string

treemenu.prototype.writeTieUpIcon = function(node)  {                           //>WriteTieUpIcon
 if (node.indent < 1) return '';                                                // Only for indents > 1
 var icon = this.getTieUpIcon(node);                                            // GetTieUpIcon
 var str  = '';                                                                 // Initialize str
 if (0==node.childs.length)                                                     // No childs -> Return only TieUpIcon
      str = '<img id="' + this.name + 'TieUp_' + node.id                        //   Write tieUpIcon with
          + '" src="' + icon + '" alt="" />';                                   //   name & source
 else str = '<a href="javascript: ' + this.name + '.toggle(' + node.id + ')">'  // Parent node:
          +  '<img id="' + this.name + 'TieUp_' + node.id                       //   Write tieUpIcon with
          +  '" src="' + icon + '" alt="" /></a>';                              //   name, source & javascript:toggle
 return str; }                                                                  // Html-code for the TieUpIcon

treemenu.prototype.getTieUpIcon = function(node) {                              //>GetTieUpIcon
 if (0 == node.childs.length) {                                                 // No childs:
   if      (!this.showLines)        return this.defaults.empty;                 //   Don't show Lines   -> empty
   else if (this.isLastChild(node)) return this.defaults.endLine;               //   Else if last child -> endLine
   else                             return this.defaults.tieLine;  }            //   Else if fore child -> tieLine
 else if (node.isOpen) {                                                        // Open parent:
   if      (!this.showLines)        return this.defaults.rectMinus;             //   Don't show Lines   -> rectMinus
   else if (this.isLastChild(node)) return this.defaults.endMinus;              //   Else if last child -> endMinus
   else                             return this.defaults.tieMinus; }            //   Else if fore child -> tieMinus
 else {                                                                         // Closed parent:
   if      (!this.showLines)        return this.defaults.rectPlus;              //   Don't show Lines   -> rectPlus
   else if (this.isLastChild(node)) return this.defaults.endPlus;               //   Else if last child -> endPlus
   else                             return this.defaults.tiePlus;  } }          //   Else if fore child -> tiePlus

treemenu.prototype.writeNodeSymbol = function(node) {                           //>WriteNodeSymbol
 var icon = this.getNodeSymbol(node) ;                                          // GetNodeSymbol
 if (0==node.childs.length) {                                                   // No childs:
   var str = '';                                                                //   Reference to the nodes url
   if (node.url) {    str += '<a href="' + node.url + '"';                      //     if a url is given and load
     if (node.target) str += ' target="' + node.target + '")';                  //     the url into the target frame.
                      str += '">'; }                                            //     Close leading <a..>-tag
   str += '<img id="' + this.name + 'Symbol_' + node.id                         //   Write the Html-code for the
       +  '" src="'   + icon + '" alt="" />';                                   //     image of the node-symbol
   if (node.url) str += '</a>';                                                 //   Close trailing </a>-tag if any
   return str; }                                                                //   Return Html-string for symbol
 return   '<a href="javascript: ' + this.name + '.toggle(' + node.id + ')">'    // Parent:
          + '<img id="' + this.name + 'Symbol_' + node.id                       //   Write Html-string for the image
          + '" src="' + icon + '" alt="" /></a>'; }                             //   and a reference to java -> toggle

treemenu.prototype.getNodeSymbol = function(node) {                             //>GetNodeSymbol
 if (!this.showIcons)  return this.defaults.minIcon;                            // No Symbols-> 'minIcon' (for IE)
 if (0==node.childs.length) {                                                   // No childs:
   if (node.iconOpen)  return node.iconOpen;                                    //   Use nodes  'iconOpen'
   else                return this.defaults.iconItem;  }                        //   or default 'iconItem'
 else if (node.isOpen) {                                                        // Open parent:
   if (node.iconOpen)  return node.iconOpen;                                    //   Use nodes  'iconOpen'
   else                return this.defaults.iconOpen;  }                        //   or default 'iconOpen'
 else {                                                                         // Closed parent:
   if (node.iconClose) return node.iconClose;                                   //   Use nodes  'iconClose'
   else                return this.defaults.iconClose; } }                      //   or default 'iconClose'

treemenu.prototype.writeNodeText = function(node) {                             //>WriteNodeText
 var cls = this.getNodeTextClass(node, this.selected);                          // Get NodeTextClass
 var str = '<a id="' + this.name + 'Node_' + node.id + '" class="' + cls + '"'; // Add '<a id=...' and 'class=...'
 if (node.url) str += ' href="' + node.url + '"';                               // HRef-link to node.url
 else          str += ' href="javascript: '+this.name+'.toggle('+node.id+')"';  //     or to java.toggle
 if (node.url && node.target)  str += ' target="' + node.target   + '"';        // Target ="node.target"
 if (node.tooltip)             str += ' title="'  + node.tooltip  + '"';        // Title  ="node.tooltip"
 str += ' onclick="javascript: ' + this.name + '.pick(' + node.id + ')"';       // OnClick="javascript.pick"
 str += '>' + node.text + ((node.url) ? '</a>' : '</a>') ;                      // Node text, close 'a>'
 return str; }                                                                  // Return HTML-string

treemenu.prototype.getNodeTextClass = function(node, selectID) {                //>GetNodeTextClass for TreeMenu.css
 var cls = (node.id==selectID) ? 'Selected' : 'Node';                           // Class 'Selected', 'Node'
 if (!node.url) cls = 'Item';                                                   //    or 'Item' (without url)
 return cls + '_' + Math.min(node.indent, this.classDepth); }                   // Append '_indent' or '_classDepth'

treemenu.prototype.writeCreatedWithTreeMenu = function() {                      //>WriteCreatedWithTreeMenu
 var path = '',    target ='';                                                  // Path to the freeware 'treemenu'
 var elem = document.getElementById('treepath');                                // Path defined in document
 if (elem) { target = 'main';      path = elem.title;               }           //   ->   Use defined path
 else      { target = '_parent';   path = 'http://www.h-bauer.de';  }           //   Else use 'http://h-bauer.de'
 var str = '<br>Created with <a class="CreatedBy" target="' + target +'"'       // Please don't alter this code to
         + ' href="' + path + '">TreeMenu</a>';                                 // give your visitors the ability to
 return str; }                                                                  // also access this utility. THANKS!

                                                                                // --------------- Load ----------------
treemenu.prototype.loadSelected = function() { this.loadNode(this.selected); }  //>LoadSelected

treemenu.prototype.loadNode = function(id) {                                    //>LoadNode by ID into it's target frame
 if (id<0) return;                                                              // Only nodes with id>=0
 if (this.nodes[id].target=='hide') return;                                     // Only nodes with no hidden target
 for (var i=0; i<parent.frames.length; i++) {                                   // Loop: Frames in frameset
   if (parent.frames[i].name==this.nodes[id].target) {                          //   Target-frame of the selected node
     parent.frames[i].location.href = this.nodes[id].url;                       //   -> Reference to the node to load
     break; } } }                                                               //      Break the loop and return

                                                                                // ----------- Pick / Select -----------
treemenu.prototype.pick = function(id) {                                        //>Pick a node by id
 var node = this.nodes[id];                                                     // Picked node
 if (node.url) {                                                                // Nodes with URL (->no href to toggle)
   if      (node.indent==0       && this.showIcons==false) this.toggle(id);     // -> Toggle top node without icon
   else if (node.childs.length>0 && node.isOpen==false)    this.toggle(id); }   //    Else: open closed parent node
 this.select(id); }                                                             // Select node by ID & unselect previous
// document.location.reload(); }

treemenu.prototype.select = function(id) {                                      //>Select a node by a given ID
 if (!this.nodes[id].url) return;                                               // Only for a node with url:
 if (this.selected >= 0) {                                                      // Deselect selected Html-node:
   node = document.getElementById(this.name + 'Node_' + this.selected);         //   Get selected Html-node by id
   name = this.getNodeTextClass(this.nodes[this.selected],-1);                  //   ClassName for unselected
   if (node && name) node.className = name;                                     //   Unselect previous selected node
   this.selected  = -1;  }                                                      //   Invalidate this.selected
 node = document.getElementById(this.name + 'Node_' + id);                      // Select Html-node:
 name = this.getNodeTextClass(this.nodes[id], id);                              //   ClassName for selected
 if (node && name) node.className = name;                                       //     Selected previous unselected node
 this.selected  = id;                                                           //     Set this.selected value to id
 this.openAncestors(id);                                                        //     Open the nodes ancestors
 this.setCookies(this.expire); }                                                //     Set cookies

treemenu.prototype.openAncestors = function(id) {                               //>OpenAncestors of the node with id
 if (id<0) return;                                                              // Only valid nodes with ID>=0
 var ancestor = this.nodes[id].parent;                                          // Ancestor is parent node;
 while(ancestor.indent>=0) {                                                    // Loop: Ancestors
   if (!ancestor.isOpen) { ancestor.isOpen=true;  this.updateNode(ancestor); }  //   Open and update ancestor
   ancestor = ancestor.parent;   } }                                            //   Parent of ancestor  }

treemenu.prototype.selectPath = function(path) {                                //>SelectPath  (for page registration)
 path = this.pathWithSlash(path);                                               // Ensure path with slash '/'
 if (this.selected>=0) {                                                        // A node ist already selected:
   var url = this.pathWithSlash(this.nodes[this.selected].url);                 //   URL of the selected node
   if (url==path) return;  }                                                    //   URL already selected -> return
 for (id=0; id<this.nodes.length; id++) {                                       // Loop to search node:
   var url = this.pathWithSlash(this.nodes[id].url);                            //   Node path with slash '/'
   if (url && url==path) { this.select(id);    break; } } }                     //   Equal path -> select node by id

treemenu.prototype.pathWithSlash = function(path) {                             //>PathWithSlash
 var parts = path.split("\\");                                                  // Split path at '\' into string-array
 var str   = parts[0];                                                          // Write first part to 'str'
 for (i=1; i<parts.length; i++) str = str + '/' + parts[i];                     // Add next parts divided by '/'
 return str; }                                                                  // Return path with '/' instead of '\'

                                                                                // ---------- Toggle / Update ----------
treemenu.prototype.toggle = function(id) {                                      //>Toggle a node by id
 if (this.nodes[id].childs.length==0) return;                                   // Only for parent nodes
 this.nodes[id].isOpen = !this.nodes[id].isOpen;                                // Toggle node-status (open or close)
 this.updateNode(this.nodes[id]);                                               // Update the node
 this.setCookies(this.expire); }                                                // Set cookies

treemenu.prototype.updateNode = function(node) {                                //>UpdateNode
 subTree = document.getElementById(this.name + 'SubTree_' + node.id);           // Get Html-element: SubTree
 tieUp   = document.getElementById(this.name + 'TieUp_'   + node.id);           //                   TieUpIcon
 symbol  = document.getElementById(this.name + 'Symbol_'  + node.id);           //                   NodeSymbol
 if (subTree) subTree.style.display = (node.isOpen) ? 'block' : 'none';         // Update Html-elem. SubTree
 if (tieUp)   tieUp.src   = this.getTieUpIcon(node);                            //                   TieUpIcon
 if (symbol)  symbol.src  = this.getNodeSymbol(node); }                         //                   NodeSymbol

                                                                                // ----------- IsLastChild -------------
treemenu.prototype.isLastChild = function(node) {                               //>IsLastChild (?)
 var parent = node.parent;                                                      // Parent of the node
 return ((node == parent.childs[parent.childs.length-1]) ? true : false); }     // Check for last child

                                                                                // --------- Level/Lines/Icons ---------
treemenu.prototype.level = function(level) {                                    //>Level to open/close menu
 for (id=0; id<this.nodes.length; id++) {                                       // Loop: nodes
   this.nodes[id].isOpen = (this.nodes[id].indent<level) ? true : false;        //   Open/close node depending on level
   this.updateNode(this.nodes[id]); }                                           //   Update the node
 this.setCookies(this.expire); }                                                // Set cookies

treemenu.prototype.lines = function(bool) {                                     //>Lines to be shown (?)
 if (this.showLines == bool) return;                                            // Nothing changed -> return
 this.showLines = bool;                                                         // Update 'showLines'
 var passLines = document.getElementsByName("passLine");                        // Get PassLines
 if (!passLines) return;                                                        // Existing passLines:
 for (i=0; i<passLines.length; i++) {                                           //  Loop: passLines
   passLines[i].src = (bool) ? this.defaults.passLine : this.defaults.empty; }  //   Update icon-source
 for (id=0; id<this.nodes.length; id++) {                                       // TieUpIcon for each node
   if (this.nodes[id].indent < 1) continue;                                     //   with indent >= 1
   var tieUp = document.getElementById(this.name + 'TieUp_' + id);              //   TieUpIcon of the node
   if (tieUp) tieUp.src = this.getTieUpIcon(this.nodes[id]);  }                 //   Update icon-source
 this.setCookies(this.expire); }                                                // Set cookies

treemenu.prototype.icons = function(bool) {                                     //>Icons to be shown (?)
 if (this.showIcons == bool) return;                                            // Nothing changed -> return
 this.showIcons = bool;                                                         // Set 'showIcons'-value
 for (id=0; id<this.nodes.length; id++) {                                       // Loop: nodes
   var icon  = this.getNodeSymbol(this.nodes[id]);                              //   Get node symbol
   var image = document.getElementById(this.name + 'Symbol_' + id)              //   Get Html-image by id
   if (image)  image.src = icon; }                                              //   Set image source to node symbol
 this.setCookies(this.expire); }                                                // Set cookies

                                                                                // -------------- Cookies --------------
treemenu.prototype.expiration = function(expire) {                              //>Expiration
 this.expire = expire;          this.setCookies(this.expire); }                 // Set/Save expiration period of cookies

treemenu.prototype.cookies = function(bool) {                                   //>Cookies to be used (?)
 if (bool) { this.useCookies = bool;  this.setCookies(this.expire);   }         // Use cookies -> Set cookies
 else      { this.setCookies(-1);     this.useCookies = bool;         } }       // No  cookies -> Clear existing cookies

treemenu.prototype.setCookies = function(expire) {                              //>SetCookies
 this.openNodes = '';                                                           // Initialize 'openNodes'-String
 for (i=0; i<this.nodes.length; i++)                                            // Loop: nodes
   this.openNodes += (this.nodes[i].isOpen) ? '1' : '0';                        //   Fill 'openNodes'-String
 this.setCookie("OpenNodes", this.openNodes, expire);                           // Set cookie 'OpenNodes'
 this.setCookie("ShowLines", this.showLines, expire);                           //            'ShowLines'
 this.setCookie("ShowIcons", this.showIcons, expire);                           //            'ShowIcons'
 this.setCookie("Selected",  this.selected,  expire);                           //            'Selected'
 this.setCookie("Expire"   , this.expire,    expire); }                         //            'Expire'

treemenu.prototype.readCookies = function() {                                   //>ReadCookies (as string!)
 var lines  = this.getCookie("ShowLines");                                      // Get Cookie:  'ShowLines'
 var icons  = this.getCookie("ShowIcons");                                      //              'ShowIcons'
 var select = this.getCookie("Selected");                                       //              'Selected'
 var open   = this.getCookie("OpenNodes");                                      //              'OpenNodes'
 var expire = this.getCookie("Expire");                                         //              'Expire'
 if (lines)   this.showLines = (lines=='true') ? true : false;                  // Set value of 'showLines'
 if (icons)   this.showIcons = (icons=='true') ? true : false;                  //              'showIcons'
 if (select)  this.selected  = select;                                          //              'selected'
 if (open)    this.openNodes = open;                                            //              'openNodes'
 if (expire)  this.expire    = expire;                                          //              'expire'
 if (lines || icons || select ||  open) this.useCookies = true;  }              // Cookies found -> useCookies is true

                                                                                // --------------- Cookie --------------
treemenu.prototype.setCookie = function(name, value, expire) {                  //>SetCookie by name and value
 if (!this.useCookies) return;                                                  // Only if cookies are to be used
 var exp = new Date();                                                          // Actual date
 var end = exp.getTime() + (expire * 24 * 60 * 60 * 1000);                      // In 'expire'-days (-1: -> invalidate)
 exp.setTime(end);                                                              // Expire time of cookes
 document.cookie =  name + '=' + value + '; expires=' + exp.toGMTString(); }    // Set cookie with expiration-date

treemenu.prototype.getCookie = function(name) {                                 //>GetCookie value (as string!)
 var cookies  = document.cookie;                                                // Cookies separated by ';'
 var posName  = cookies.indexOf(name + '=');                                    // Start position of 'name='
 if (posName == -1) return '';                                                  // Cookie not found -> Return ''
 var posValue = posName + name.length + 1;                                      // Start position of cookie-value
 var endValue = cookies.indexOf(';',posValue);                                  // End position of cookie value at ';'
 if (endValue !=-1) return cookies.substring(posValue, endValue);               // ';' -> Return substring as value
 return cookies.substring(posValue); }                                          // Else-> Return rest of line as value

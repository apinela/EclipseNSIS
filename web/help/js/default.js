// default.js, Version 1.0, 2004/03
// Copyright (C) 2004 by Hans Bauer, Schillerstr. 30, D-73072 Donzdorf
//                       http://www.h-bauer.de
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation.
//
function loaded(document) {                                                     //>Loaded a document
 if (0==parent.frames.length) return;                                           // Only if frames (menu) are shown
 var path  = document.location.pathname;                                        // Path of the document
 var frame = parent.location.pathname;                                          // Path of 'index.html'
 var index = frame.indexOf("index.html");                                       // Start-position of 'index.html'
 if (index==-1) index = frame.length;                                           // If frame=='/' (not '/index.html')
 var rel   = path.slice(index);                                                 // Relative path of loaded document
 if (parent.menu.tree) parent.menu.tree.selectPath(rel); }                      // Select appropriate node in menu

<!--

// these are the scripts that are called in the links.

var DHTML = (document.getElementById || document.all || document.layers);
var highlightColor = "rgb(204, 0, 0)";
var lastObj = null;
var lastObjBorder = null;

function highlightDef(id)
{
	if(lastObj) {
	    lastObj.style.border = lastObjBorder;
	    lastObj = null;
	    lastObjBorder = null;
	}
	lastObj = getObj(id);
	if(lastObj) {
        lastObjBorder = lastObj.style.border;
        lastObj.style.border = "2px solid "+highlightColor;
    }
}

function getObj(name)
{
    var temp = new makeObj(name);
    if(temp.obj) {
        return temp;
    }
    else {
        return null;
    }
}

function makeObj(name)
{
    if (document.getElementById)
    {
  	    this.obj = document.getElementById(name);
  	    if(this.obj) {
    	    this.style = this.obj.style;
        }
    }
    else if (document.all)
    {
	    this.obj = document.all[name];
  	    if(this.obj) {
    	    this.style = this.obj.style;
        }
    }
    else if (document.layers)
    {
   	    this.obj = document.layers[name];
   	    this.style = document.layers[name];
    }
}
// -->

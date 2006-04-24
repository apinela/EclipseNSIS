<!--
var numberRegExp = /^[1-9][0-9]*$/
var keywordURLs = new Object();
keywordURLs["!macro"] = "Chapter5.html#5.4.11";
keywordURLs["file"] = "Chapter4.html#4.9.1.5";

function prependSlash(url)
{
    if(url && url.charAt(0) != '/') {
        url = "/" + url;
    }
    return url;
}

function redirectEclipse(url)
{
    document.location = "http://help.eclipse.org/help31/topic" + prependSlash(url);
}

function redirectNSIS(url)
{
    document.location = "http://nsis.sourceforge.net/Docs" + prependSlash(url);
}

function redirectNSISContrib(url)
{
    redirectNSIS(url);
}

function redirectNSISKeyword(keyword)
{
    keyword = keyword.toLowerCase();
    if(keywordURLs[keyword]) {
        redirectNSIS(keywordURLs[keyword]);
    }
}

function redirectNSISSection(section)
{
    var parts;
    var url;
    
    parts = section.split(".");
    if(numberRegExp.test(parts[0])) {
        url = "Chapter"+parts[0]+".html";
    }
    else {
        url = "Appendix"+parts[0]+".html";
    }
    if(parts.length > 1) {
        url = url+"#"+section;
    }
    redirectNSIS(url);
}
//-->

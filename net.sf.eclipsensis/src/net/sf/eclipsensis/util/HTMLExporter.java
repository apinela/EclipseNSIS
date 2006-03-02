/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.texteditor.*;

public class HTMLExporter
{
    private Shell mShell;
    private File mPreviousFile;
    private int[][] mProjections;
    private PrintWriter mWriter = null;
    private StyledText mStyledText;
    private boolean mLineNumbersVisible;
    private StyleRange[] mRanges;
    private int mCurrentProjection;
    private int mCurrentRange;
    private int mCurrentOffset;
    private int mCurrentLine;
    private boolean mProjectionEnabled;
    private ITextEditor mEditor;
    private ISourceViewer mViewer;
    
    public HTMLExporter(ITextEditor editor, ISourceViewer viewer)
    {
        mEditor = editor;
        mViewer = viewer;
        mShell = mEditor.getSite().getShell();
    }
    
    public synchronized void exportHTML()
    {
        FileDialog fd = new FileDialog(mShell,SWT.SAVE);
        fd.setText("Export to HTML");
        fd.setFilterExtensions(new String[] {"*.html;*.htm","*.*"});
        fd.setFilterNames(new String[] {"HTML files (*.html;*.htm)","All files (*.*)"});
        if(mPreviousFile != null) {
            fd.setFileName(mPreviousFile.getAbsolutePath());
        }
        String filename = fd.open();
        if(filename != null) {
            File file = new File(filename);
            if (!file.exists() || Common.openConfirm(mShell,EclipseNSISPlugin.getFormattedString("save.confirm",new Object[]{file.getAbsolutePath()}), EclipseNSISPlugin.getShellImage())) { //$NON-NLS-1$
                mPreviousFile = file;
                writeHTML(file);
                if(file.exists()) {
                    try {
                        Common.openExternalBrowser(file.toURI().toURL().toString());
                    }
                    catch (MalformedURLException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                        Common.openError(mShell, e.getMessage(), EclipseNSISPlugin.getShellImage());
                    }
                }
            }
        }
    }

    private void writeHTML(File file)
    {
        try {
            reset();
            writeHead(file);
            mWriter.print("<body>");
            mWriter.print("<div style=\"");
            FontData fontData = mStyledText.getFont().getFontData()[0];
            mWriter.print("font-family: '");
            mWriter.print(fontData.getName());
            mWriter.print("';");
            mWriter.print("font-size: ");
            mWriter.print(fontData.getHeight());
            mWriter.print("pt;");
            int style = fontData.getStyle();
            String styleText = makeStyle((style&SWT.BOLD) > 0,(style&SWT.ITALIC) > 0,fontData.data.lfUnderline == 1,
                    fontData.data.lfStrikeOut == 1, mStyledText.getForeground(), mStyledText.getBackground());
            if(styleText != null) {
                mWriter.print(styleText);
            }
            mWriter.println("\">");

            int lineNumberWidth = -1;
            int projectionWidth = -1;
            IVerticalRuler ruler = (IVerticalRuler)mEditor.getAdapter(IVerticalRuler.class);
            if(ruler == null) {
                ruler = (IVerticalRuler)mEditor.getAdapter(IVerticalRulerInfo.class);
            }
            int width1 = -1;
            int width2 = -1;
            if(ruler instanceof CompositeRuler) {
                CompositeRuler c = (CompositeRuler)ruler;
                for(Iterator iter = c.getDecoratorIterator();iter.hasNext();) {
                    IVerticalRulerColumn col = (IVerticalRulerColumn)iter.next();
                    if(mLineNumbersVisible && col instanceof LineNumberRulerColumn) {
                        width1 = col.getWidth();
                    }
                    else if(mProjectionEnabled && col instanceof AnnotationRulerColumn && ((AnnotationRulerColumn)col).getModel() instanceof ProjectionAnnotationModel) {
                        width2 = col.getWidth();
                    }
                }
            }
            GC gc = new GC(mStyledText);
            Point p = gc.stringExtent(" ");
            gc.dispose();

            if(mLineNumbersVisible) {
                if(width1 < 0) {
                    lineNumberWidth = p.x*Integer.toString(mViewer.getTextWidget().getLineCount()).length();
                }
                else {
                    lineNumberWidth = (int)Math.ceil((double)width1/(double)p.x);
                }
            }
            if(mProjectionEnabled) {
                if(width2 < 0) {
                    lineNumberWidth = p.x*3;
                }
                else {
                    projectionWidth = Math.max(3,(int)Math.round((double)width2/(double)p.x));
                }
            }
            
            mCurrentLine = 1;
            int total = mStyledText.getCharCount();
            mCurrentOffset = 0;
            mCurrentRange = 0;
            mCurrentProjection = 0;
            mWriter.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
            startLine();
            while(mCurrentRange < mRanges.length) {
                StyleRange range = mRanges[mCurrentRange];
                if(mCurrentOffset < range.start) {
                    writeText(mStyledText.getText(mCurrentOffset,range.start-1),null);
                }
                styleText = makeStyle((range.fontStyle&SWT.BOLD) > 0,(range.fontStyle&SWT.ITALIC) > 0,
                                      range.underline,range.strikeout,range.foreground,range.background);
                writeText(mStyledText.getText(range.start,range.start+range.length-1), styleText);
                mCurrentOffset = range.start+range.length;
                mCurrentRange++;
            }
            if(mCurrentOffset < total) {
                writeText(mStyledText.getText(mCurrentOffset,total-1),null);
            }
            endLine();
            mWriter.print("<tr>");
            if(mLineNumbersVisible) {
                mWriter.print("<td><pre>");
                for(int i=0;i<lineNumberWidth; i++) {
                    mWriter.print(" ");
                }
                mWriter.print("</pre></td>");
            }
            
            if(mProjectionEnabled) {
                mWriter.print("<td><pre>");
                for(int i=0;i<projectionWidth; i++) {
                    mWriter.print(" ");
                }
                mWriter.print("</pre></td>");
            }
            else {
                mWriter.print("<td><pre> </pre></td>");
            }
            
            mWriter.println("<td></td></tr>");
            mWriter.println("</table>");
            mWriter.println("</div>");
            mWriter.println("<div id=\"lineDiv\"></div>");
            mWriter.println("<div style=\"text-align: center; font-family: Arial, Helvetica, sans-serif; font-size: 8pt; font-weight: bold;\"><hr>");
            mWriter.println("Exported as HTML from EclipseNSIS (<a href=\"http://eclipsensis.sf.net\">http://eclipsensis.sf.net</a>)<br>");
            mWriter.println("Copyright &copy; 2004-2006 Sunil Kamath (IcemanK). All rights reserved.");
            mWriter.println("</div>");
            mWriter.println("</body>");
            mWriter.println("</html>");
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
            Common.openError(mShell, e.getMessage(), EclipseNSISPlugin.getShellImage());
            if(file.exists()) {
                file.delete();
            }
        }
        finally {
            IOUtility.closeIO(mWriter);
        }
    }
    
    private void startLine()
    {
        mWriter.print("<tr");
        if(mProjectionEnabled) {
            mWriter.print(" id=\"line");
            mWriter.print(mCurrentLine);
        }
        mWriter.println("\">");
        if(mLineNumbersVisible) {
            mWriter.print("<td class=\"lineNum\"><pre>");
            mWriter.print(mCurrentLine);
            mWriter.print("</pre></td>");
        }

        mWriter.print("<td class=\"ruler\">");
        if(mProjectionEnabled && mCurrentProjection < mProjections.length && 
            mProjections[mCurrentProjection][0] == mCurrentLine) {
            mWriter.print("<a class=\"trigger\" href=\"#\" onClick=\"toggle(this,");
            mWriter.print(mProjections[mCurrentProjection][0]+1);
            mWriter.print(",");
            mWriter.print(mProjections[mCurrentProjection][1]);
            mWriter.print(");\" onMouseOver=\"showLine(this,");
            mWriter.print(mProjections[mCurrentProjection][1]);
            mWriter.print(");\" onMouseOut=\"hideLine();\">[&ndash;]</a>");
            mCurrentProjection++;
        }
        else {
            mWriter.print("<pre> </pre>");
        }
        mWriter.print("</td>");
        
        mWriter.print("<td><pre>");
        mCurrentLine++;
    }

    private void writeText(String text, String style) throws IOException
    {
        LineNumberReader lnr = new LineNumberReader(new StringReader(text));
        String line = lnr.readLine();
        while(line != null) {
            writeSpan(line, style);
            line = lnr.readLine();
            if(line == null) {
                //This is the last line. Check if text ends with CR or LF
                char c = text.charAt(text.length()-1);
                if(c != SWT.CR && c != SWT.LF) {
                    break;
                }
            }
            endLine();
            startLine();
        }
    }

    /**
     * @param text
     * @param style
     */
    private void writeSpan(String text, String style)
    {
        if(style != null) {
            mWriter.print("<span style=\"");
            mWriter.print(style);
            mWriter.print("\">");
        }
        mWriter.print(text);
        if(style != null) {
            mWriter.print("</span>");
        }
    }
    
    private void endLine()
    {
        mWriter.println("</pre></td>");
        mWriter.println("</tr>");
    }
    /**
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeHead(File file) throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        String charset = null;
        IDocumentProvider provider = mEditor.getDocumentProvider();
        if(provider instanceof IStorageDocumentProvider) {
            charset = ((IStorageDocumentProvider)provider).getEncoding(mEditor.getEditorInput());
            if(charset == null) {
                charset = ((IStorageDocumentProvider)provider).getDefaultEncoding();
            }
        }
        if(charset == null) {
            charset = System.getProperty("file.encoding");
        }
        if(charset != null) {
            mWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset)));
        }
        else {
            mWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        }
        
        mWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        mWriter.println("<html>");
        mWriter.println("<head>");
        mWriter.print("<title>");
        mWriter.print(((IPathEditorInput)mEditor.getEditorInput()).getPath().toOSString());
        mWriter.println("</title>");
        mWriter.print("<meta http-equiv=\"Content-Type\" content=\"text/html"); 
        if(charset != null) {
            mWriter.print("; charset=");
            mWriter.print(charset);
        }
        mWriter.println("\">"); 
        mWriter.println("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">");
        mWriter.println("<style type=\"text/css\">");
        mWriter.println("body { background: #FFFFFF}");
        mWriter.println("pre { display: inline }");
        mWriter.println("a { color: #567599; text-decoration: none; }");
        mWriter.println("a:hover { background-color: #F4F4F4; color: #303030; text-decoration: underline}");
        mWriter.println("a.trigger { font: Arial 10px normal; letter-spacing: -5px; color: #567599; text-decoration: none; cursor:pointer; cursor:hand; }");
        mWriter.println("a.trigger:hover { background-color: #F4F4F4; color: #303030; text-decoration: none; }");
        if(mProjectionEnabled) {
            mWriter.println(".hiddenRow { display:none; }");
            mWriter.println("#lineDiv { font-size: 1px; display: none; position: absolute; color: #567599; border-left: solid 1px; border-bottom: solid 1px; width: 1px; height: 1px; }");
        }
        if(mLineNumbersVisible) {
            StringBuffer buf = new StringBuffer("text-align: right; color: #");
            RGB rgb=  null;
            // foreground color
            IPreferenceStore store = EditorsUI.getPreferenceStore();
            String pref = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
            if (store.contains(pref)) {
                if (store.isDefault(pref))
                    rgb= PreferenceConverter.getDefaultColor(store, pref);
                else
                    rgb= PreferenceConverter.getColor(store, pref);
            }
            if (rgb == null) {
                rgb= new RGB(0, 0, 0);
            }
            buf.append(ColorManager.rgbToHex(rgb));
            mWriter.print(".lineNum { ");
            mWriter.print(buf.toString());
            mWriter.println(" }");
        }
        mWriter.print(".ruler { text-align: right; border-right: 2px solid #");
        mWriter.print(ColorManager.rgbToHex(mShell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB()));
        mWriter.println(" }");
        mWriter.println("</style>");
        if(mProjectionEnabled) {
            mWriter.println("<script type=\"text/javascript\" language=\"javascript\">");
            mWriter.println("<!--");
            mWriter.println("if (!String.prototype.endsWith) {");
            mWriter.println("  String.prototype.endsWith = function(suffix) {");
            mWriter.println("    var startPos = this.length - suffix.length;");
            mWriter.println("    if (startPos < 0) {");
            mWriter.println("      return false;");
            mWriter.println("    }");
            mWriter.println("    return (this.lastIndexOf(suffix, startPos) == startPos);");
            mWriter.println("  };");
            mWriter.println("}");
            mWriter.println("function getObject(name)");
            mWriter.println("{");
            mWriter.println("  if(document.all) {");
            mWriter.println("    return document.all[name];");
            mWriter.println("  }");
            mWriter.println("  else {");
            mWriter.println("    return document.getElementById(name);");
            mWriter.println("  }");
            mWriter.println("}");
            mWriter.println("function showLine(link,end)");
            mWriter.println("{");
            mWriter.println("  if(link.innerHTML != \"[+]\") {");
            mWriter.println("    var lineDiv = getObject(\"lineDiv\");");
            mWriter.println("    var sec = getObject(\"line\"+end);");
            mWriter.println("    if(sec && lineDiv) {");
            mWriter.println("      var linkPos = getElementPosition(link);");
            mWriter.println("      var secPos = getElementPosition(sec);");
            mWriter.println("      if(secPos && linkPos) {");
            mWriter.println("        lineDiv.style.left = linkPos.left+linkPos.width/2;");
            mWriter.println("        lineDiv.style.top = linkPos.top+linkPos.height;");
            mWriter.println("        lineDiv.style.width = linkPos.width/2;");
            mWriter.println("        lineDiv.style.height = secPos.top+secPos.height/2-(linkPos.top+linkPos.height);");
            mWriter.println("        lineDiv.style.display = \"block\";");
            mWriter.println("      }");
            mWriter.println("    }");
            mWriter.println("  }");
            mWriter.println("}");
            mWriter.println("function hideLine()");
            mWriter.println("{");
            mWriter.println("  var lineDiv = getObject(\"lineDiv\");");
            mWriter.println("  if(lineDiv) {");
            mWriter.println("    lineDiv.style.display = \"none\";");
            mWriter.println("  }");
            mWriter.println("}");
            mWriter.println("function toggle(link,start,end) ");
            mWriter.println("{");
            mWriter.println("  if(link) {");
            mWriter.println("    var i;");
            mWriter.println("    var sec;");
            mWriter.println("    var expand;");
            mWriter.println("    hideLine();");
            mWriter.println("    if(link.blur) {");
            mWriter.println("      link.blur();");
            mWriter.println("    }");
            mWriter.println("    if(link.innerHTML == \"[+]\") {");
            mWriter.println("      link.innerHTML = \"[&ndash;]\";");
            mWriter.println("      expand = true;");
            mWriter.println("    }");
            mWriter.println("    else {");
            mWriter.println("      link.innerHTML = \"[+]\";");
            mWriter.println("      expand = false;");
            mWriter.println("    }");
            mWriter.println("    for(i=start; i<= end; i++) {");
            mWriter.println("      sec = getObject(\"line\"+i);");
            mWriter.println("      if(sec) {");
            mWriter.println("        if(expand) {");
            mWriter.println("          if(sec.className == \"hiddenRow\") {");
            mWriter.println("            sec.className = \"\";");
            mWriter.println("          }");
            mWriter.println("          else if(sec.className.endsWith(\" hiddenRow\")) {");
            mWriter.println("            sec.className = sec.className.substr(0,sec.className.length-\" hiddenRow\".length);");
            mWriter.println("          }");
            mWriter.println("        }");
            mWriter.println("        else {");
            mWriter.println("          if(sec.className == \"\") {");
            mWriter.println("            sec.className = \"hiddenRow\";");
            mWriter.println("          }");
            mWriter.println("          else {");
            mWriter.println("            sec.className = sec.className + \" hiddenRow\";");
            mWriter.println("          }");
            mWriter.println("        }");
            mWriter.println("      }");
            mWriter.println("    }");
            mWriter.println("  }");
            mWriter.println("}");
            mWriter.println("function getElementPosition(elem){");
            mWriter.println("  var offsetLeft = 0;");
            mWriter.println("  var offsetTop =0;");
            mWriter.println("  var width = elem.offsetWidth;");
            mWriter.println("  var height = elem.offsetHeight;");
            mWriter.println("  while (elem){");
            mWriter.println("    offsetLeft += elem.offsetLeft;");
            mWriter.println("    offsetTop += elem.offsetTop;");
            mWriter.println("    elem = elem.offsetParent;");
            mWriter.println("  }");
            mWriter.println("  if (navigator.userAgent.indexOf('Mac') != -1 && typeof(document.body.leftMargin) !='undefined'){");
            mWriter.println("    offsetLeft += document.body.leftMargin;");
            mWriter.println("    offsetTop += document.body.topMargin;");
            mWriter.println("  }");
            mWriter.println("  return {left:offsetLeft,top:offsetTop, width:width, height: height};");
            mWriter.println("}//-->");
            mWriter.println("</script>");
        }
        mWriter.println("</head>");
    }

    /**
     * 
     */
    private void reset()
    {
        IPreferenceStore store = EditorsUI.getPreferenceStore();
        mLineNumbersVisible =  store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER);
        mStyledText = mViewer.getTextWidget();
        mRanges = mStyledText.getStyleRanges();
        mProjectionEnabled = (mViewer instanceof ProjectionViewer && ((ProjectionViewer)mViewer).isProjectionMode());
        if(mProjectionEnabled) {
            ProjectionAnnotationModel model = ((ProjectionViewer)mViewer).getProjectionAnnotationModel();
            Iterator iter = model.getAnnotationIterator();
            List projections = new ArrayList();
            while(iter.hasNext()) {
                Position pos = model.getPosition((Annotation)iter.next());
                if(pos != null) {
                    projections.add(pos);
                }
            }
            if(projections.size() > 0) {
                Collections.sort(projections, new Comparator() {
                    public int compare(Object o1, Object o2)
                    {
                        Position p1 = (Position)o1;
                        Position p2 = (Position)o2;
                        int n = p1.getOffset()-p2.getOffset();
                        if(n == 0) {
                            n = p1.getLength()-p2.getLength();
                        }
                        return n;
                    }
                });
                for (ListIterator iterator = projections.listIterator(); iterator.hasNext();) {
                    Position pos = (Position)iterator.next();
                    iterator.set(new int[]{mStyledText.getLineAtOffset(pos.getOffset())+1,mStyledText.getLineAtOffset(pos.getOffset()+pos.getLength()-1)+1});
                }
            }
            mProjections = (int[][])projections.toArray(new int[projections.size()][]);
        }
    }
    
    private String makeStyle(boolean bold, boolean italic, boolean underline, 
                             boolean strikeOut, Color fgColor, Color bgColor)
    {
        if(bold || italic || underline || strikeOut || fgColor != null || bgColor != null) {
            StringBuffer buf = new StringBuffer("");
            if(bold) {
                buf.append("font-weight: bold;");
            }
            if(italic) {
                buf.append("font-style: italic;");
            }
            if(underline || strikeOut) {
                buf.append("text-decoration:");
                if(underline) {
                    buf.append(" underline");
                }
                if(strikeOut) {
                    buf.append(" line-through");
                }
                buf.append(";");
            }
            if(fgColor != null) {
                buf.append("color: #").append(ColorManager.rgbToHex(fgColor.getRGB())).append(";");
            }
            if(bgColor != null) {
                buf.append("background-color: #").append(ColorManager.rgbToHex(bgColor.getRGB())).append(";");
            }
            return buf.toString();
        }
        else {
            return null;
        }
    }
}
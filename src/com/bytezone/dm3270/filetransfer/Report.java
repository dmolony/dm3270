package com.bytezone.dm3270.filetransfer;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

public class Report implements Printable
{
  private final String name;
  private final List<String> lines = new ArrayList<> ();

  private int lineHeight;
  private int linesPerPage;
  private int lastPage;

  private final Font plainFont, boldFont, headerFont;

  public Report (String name, String text)
  {
    this.name = name;
    String[] textLines = text.split ("\\n");
    for (String textLine : textLines)
      lines.add (textLine);

    plainFont = new Font ("Menlo", Font.PLAIN, 8);
    boldFont = new Font (plainFont.getFontName (), Font.BOLD, plainFont.getSize ());
    headerFont = new Font ("Dialog", Font.PLAIN, 14);
  }

  @Override
  public int print (Graphics graphics, PageFormat pageFormat, int pageIndex)
      throws PrinterException
  {
    Graphics2D g2 = (Graphics2D) graphics;

    if (lineHeight == 0)
      setMetrics (g2, pageFormat);        // sets totalPages and other fields

    if (pageIndex > lastPage)
      return Printable.NO_SUCH_PAGE;

    int x = 50;
    int y = 40;

    g2.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());

    g2.setFont (headerFont);
    g2.drawString (name, x, y);
    g2.drawLine (x, y + 3, x + g2.getClipBounds ().width - 100, y + 3);

    int firstLine = pageIndex * linesPerPage;
    int lastLine = Math.min (firstLine + linesPerPage, lines.size ());

    g2.setFont (plainFont);
    for (int lineNo = firstLine; lineNo < lastLine; lineNo++)
      g2.drawString (lines.get (lineNo), x, y + (lineNo % linesPerPage + 2) * lineHeight);

    return (Printable.PAGE_EXISTS);
  }

  private void setMetrics (Graphics2D g2, PageFormat pageFormat)
  {
    LineMetrics lm = plainFont.getLineMetrics ("crap", g2.getFontRenderContext ());
    lineHeight = (int) lm.getHeight () + 2;
    // linesPerPage = (int) pageFormat.getImageableHeight () / lineHeight - 9;
    linesPerPage = 66;
    lastPage = (lines.size () - 1) / linesPerPage;      // zero based
  }
}
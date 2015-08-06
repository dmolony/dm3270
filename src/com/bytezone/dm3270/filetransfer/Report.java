package com.bytezone.dm3270.filetransfer;

public class Report//implements Printable
{
  //  private final String name;
  //  private final FileStructure fileStructure;
  //  private final List<String> lines = new ArrayList<> ();
  //
  //  private LineMetrics lineMetrics;
  //  private int lineHeight;
  //  private int linesPerPage;
  //  private int lastPage;
  //
  //  private final Font plainFont, boldFont, headerFont;
  //
  //  public Report (FileStructure fileStructure, String text)
  //  {
  //    this.fileStructure = fileStructure;
  //    this.name = fileStructure.transfer.getFileName ();
  //
  //    for (String textLine : text.split ("\\n"))
  //      lines.add (textLine);
  //
  //    plainFont = new Font ("Menlo", Font.PLAIN, fileStructure.lineSize > 100 ? 7 : 8);
  //    boldFont = new Font (plainFont.getFontName (), Font.BOLD, plainFont.getSize ());
  //    headerFont = new Font ("Dialog", Font.PLAIN, 14);
  //  }
  //
  //  @Override
  //  public int print (Graphics graphics, PageFormat pageFormat, int pageIndex)
  //      throws PrinterException
  //  {
  //    Graphics2D g2 = (Graphics2D) graphics;
  //
  //    if (lineMetrics == null)
  //      setMetrics (g2, pageFormat);        // sets totalPages and other fields
  //
  //    if (pageIndex > lastPage)
  //    {
  //      lineMetrics = null;
  //      return Printable.NO_SUCH_PAGE;
  //    }
  //
  //    int x = 50;
  //    int y = 10;
  //
  //    g2.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());
  //
  //    if (pageFormat.getOrientation () == PageFormat.PORTRAIT)
  //    {
  //      g2.setFont (headerFont);
  //      g2.drawString (name, x, y);
  //      g2.drawLine (x, y + 3, g2.getClipBounds ().width - x, y + 3);
  //      y += 30;
  //    }
  //
  //    int firstLine = pageIndex * linesPerPage;
  //    int lastLine = Math.min (firstLine + linesPerPage, lines.size ());
  //
  //    g2.setFont (plainFont);
  //    for (int lineNo = firstLine; lineNo < lastLine; lineNo++)
  //    {
  //      g2.drawString (lines.get (lineNo), x, y);
  //      y += lineHeight;
  //    }
  //
  //    return (Printable.PAGE_EXISTS);
  //  }
  //
  //  private void setMetrics (Graphics2D g2, PageFormat pageFormat)
  //  {
  //    if (fileStructure.lineSize > 80)
  //      pageFormat.setOrientation (PageFormat.LANDSCAPE);
  //
  //    lineMetrics = plainFont.getLineMetrics ("crap", g2.getFontRenderContext ());
  //
  //    lineHeight = (int) lineMetrics.getHeight () + 1;
  //    // linesPerPage = (int) pageFormat.getImageableHeight () / lineHeight - 9;
  //    linesPerPage = 66;
  //    lastPage = (lines.size () - 1) / linesPerPage;      // zero based
  //  }
}
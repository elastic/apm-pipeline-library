package co.elastic

import hudson.Extension
import hudson.MarkupText
import hudson.MarkupText.SubText
import org.jenkinsci.Symbol
import hudson.console.*

@Extension @Symbol("logReportQueue")
public class LogReportQueueAnnotator extends ConsoleAnnotatorFactory<Object> {
  private static class LogReportQueueConsoleAnnotator extends ConsoleAnnotator {
      private static final long serialVersionUID = 1L

      public ConsoleAnnotator annotate(Object context, MarkupText text) {
          System.out.println("LogReportQueueConsoleAnnotator: ${text.getText()}")
          return this
      }
  }

  @Override
  public ConsoleAnnotator newInstance(Object context) {
      return new LogReportQueueConsoleAnnotator()
  }
}

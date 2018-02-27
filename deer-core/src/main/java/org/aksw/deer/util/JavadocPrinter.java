package org.aksw.deer.util;

import com.github.therapi.runtimejavadoc.*;
import org.aksw.deer.enrichments.DereferencingEnrichmentOperator;

import java.util.stream.Stream;

public class JavadocPrinter {
  // formatters are reusable and thread-safe
  private static final CommentFormatter formatter = new CommentFormatter();

  public static void main(String[] args) {
    printJavadoc(DereferencingEnrichmentOperator.class.getCanonicalName());
  }

  public static void printJavadoc(String fullyQualifiedClassName) {
    ClassJavadoc classDoc = RuntimeJavadoc.getJavadoc(fullyQualifiedClassName).orElse(null);
    if (classDoc == null) {
      System.out.println("no documentation for " + fullyQualifiedClassName);
      return;
    }
//    System.out.println(format(classDoc.getComment()));
//    System.out.println();

    Stream<MethodJavadoc> stream = classDoc.getMethods().stream().filter(s -> s.getName().equals("hello"));
    if (stream.count() == 1) {

    } else {

    }

//    forEach(c -> System.out.println(format(c.getComment())));

  }

  private static String format(Comment c) {
    return formatter.format(c);
  }
}
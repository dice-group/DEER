package org.aksw.deer.enrichment.dereferencing;

public class Triple {

  public String subject;
  public String predicate;

  ;
  public String Object;
  public Triple(String s, String p, String o) {
    subject = s;
    predicate = p;
    Object = o;
  }

  @Override
  public String toString() {
    return "<" + subject + ">" + "\t" + "<" + predicate + ">" + "\t" + "<" + Object + ">" + "\n";
  }


}

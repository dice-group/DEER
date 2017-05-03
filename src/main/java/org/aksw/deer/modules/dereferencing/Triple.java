package org.aksw.deer.modules.dereferencing;

public class Triple
{
    public Triple(String s, String p, String o)
    {
        subject=s;
        predicate=p;
        Object=o;
    }
    @Override public String toString()
    {
        return "<"+subject+">"+"\t"+"<"+predicate+">"+"\t"+"<"+Object+">"+"\n";
    };
    public String subject;
    public String predicate;
    public String Object;


}

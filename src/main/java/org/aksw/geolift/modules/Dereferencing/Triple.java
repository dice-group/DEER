package org.aksw.geolift.modules.Dereferencing;

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
		String triple ="";
		return triple="<"+subject+">"+"\t"+"<"+predicate+">"+"\t"+"<"+Object+">"+"\n";
	};
public String subject;
public String predicate;
public String Object;
 
 
}

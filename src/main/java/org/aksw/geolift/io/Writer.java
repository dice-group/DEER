/**
 * 
 */
package org.aksw.geolift.io;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public class Writer {
	public static void writeModel(Model model, String format, String outputFile) throws IOException
	{
		FileWriter fileWriter = new FileWriter(outputFile);
		model.write(fileWriter, format);
	}
}

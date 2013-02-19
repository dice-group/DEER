/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.geolift.TMP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.TreeSet;
import java.util.regex.Pattern;
//import org.simba.utils.Configuration;
//import org.simba.utils.NamedEntity;
//import org.simba.utils.TurtleReader;

/**
 *
 * @author ngonga
 */
public class Fox implements NerTool {

//    @Override
    public TreeSet<NamedEntity> getNEs(String input) {
        TreeSet<NamedEntity> result = new TreeSet<NamedEntity>();
        boolean error = true;
        while (error) {
            try {
                // Construct data
                String data = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("text", "UTF-8");
                data += "&" + URLEncoder.encode("task", "UTF-8") + "=" + URLEncoder.encode("ner", "UTF-8");
                data += "&" + URLEncoder.encode("output", "UTF-8") + "=" + URLEncoder.encode("turtle", "UTF-8");
                data += "&" + URLEncoder.encode("task", "UTF-8") + "=" + URLEncoder.encode("ner", "UTF-8");
                data += "&" + URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode(input, "UTF-8");

                // Send data
                URL url = new URL("http://139.18.2.164:4444/api");
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String buffer = "", line;
                while ((line = rd.readLine()) != null) {
                    buffer = buffer + line + "\n";
                }

                //read named entities
                result = TurtleReader.read(buffer);
                wr.close();
                rd.close();
                error = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(result);
        return result;
    }

    public static void main(String args[]) {
        Fox fox = new Fox();
        System.out.println(System.currentTimeMillis() / 1000);
        fox.getNEs("A former Wikileaks employee has destroyed unpublished leaks "
                + "to the site of Julian Assange site, and taken the site’s encrypted system to create a spinoff project.");
        System.out.println(System.currentTimeMillis() / 1000);
    }

    /** Gets NEs based on the config c
     * 
     * @param c Input config
     * @return All NEs found
     */
//    @Override
    public TreeSet<NamedEntity> getNEs(Configuration c) {
        return getNEs(c.inputText);
    }

    /** Annotates all sentence in input file and writes them in output file
     * !!DEPRECATED!!
     * @param input Input file
     * @param output Output file
     */
    public static void annotateAllSentences(String input, String output) {
        Fox fox = new Fox();
        TreeSet<NamedEntity> results;
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
            BufferedReader rd = new BufferedReader(new FileReader(input));
            String line = rd.readLine();
            while (line != null) {
                results = fox.getNEs(line);
                System.out.println(results);
                String copy = line + "";
                for (NamedEntity ne : results) {
                    copy = copy.replaceAll(Pattern.quote(ne.label), "[" + ne.label + ", " + ne.type + ", NULL");
                }
                line = rd.readLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

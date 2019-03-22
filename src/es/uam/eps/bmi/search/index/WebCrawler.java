/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sergio
 */
public class WebCrawler {

    IndexBuilder index;
    int maxDocuments;
    String docPath;
    PriorityQueue pagesQueue;

    public WebCrawler(String docPath, int maxDocuments, IndexBuilder index) throws IOException {
        this.maxDocuments = maxDocuments;
        this.docPath = docPath;
        this.index = index;
        this.pagesQueue = new PriorityQueue();

        this.crawl();

    }

    private void set(){
        
    }
    
    private void crawl() throws MalformedURLException, IOException {
        ArrayList links = new ArrayList();
        int count = 0;

        String content = null;
        URLConnection connection = null;
        try {
            connection = new URL("https://www.marca.com/").openConnection();
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(\") &amp;&amp; urlStr.endsWith(\")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            count++;
            System.out.println(urlStr);
            if(!pagesQueue.contains(urlStr)){
                pagesQueue.add(urlStr);
            }
        }
        System.out.println(count);
    }
}

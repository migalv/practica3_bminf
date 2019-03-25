/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.test;

import es.uam.eps.bmi.search.index.WebCrawler;
import es.uam.eps.bmi.search.index.impl.SerializedRAMIndexBuilder;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 * @author sergio
 */
public class TestCrawl {
        public static void main (String a[]) throws IOException, MalformedURLException, MalformedURLException {
            WebCrawler crawler= new WebCrawler("urls.txt", 50, new SerializedRAMIndexBuilder());
            
        
        }

}

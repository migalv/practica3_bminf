/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.test;

import es.uam.eps.bmi.search.index.WebCrawler;
import es.uam.eps.bmi.search.index.impl.DiskIndexBuilder;
import java.io.IOException;

/**
 *
 * @author sergio
 */
public class TestCrawl {
        public static void main (String a[]) throws IOException {
            WebCrawler crawler= new WebCrawler("sad", 50, new DiskIndexBuilder());
            
        
        }

}

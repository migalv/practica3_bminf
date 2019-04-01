/**
 *
 * Fichero TestCrawl.java.
 *
 *
 * @version 1.0
 *
 * Created on 25/03/2019
 */
package es.uam.eps.bmi.search.test;

import es.uam.eps.bmi.search.SearchEngine;
import es.uam.eps.bmi.search.index.WebCrawler;
import es.uam.eps.bmi.search.index.impl.SerializedRAMIndex;
import es.uam.eps.bmi.search.index.impl.SerializedRAMIndexBuilder;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import es.uam.eps.bmi.search.ui.TextResultDocRenderer;
import es.uam.eps.bmi.search.vsm.DocBasedVSMEngine;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Clase TestCrawl encargada de ejecutar el programa de crawling.
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 *
 */
public class TestCrawl {

    /**
     * Programa principal.
     * 
     * @param a
     * @throws IOException
     * @throws MalformedURLException
     */
    public static void main(String a[]) throws IOException, MalformedURLException {
        //Lanzamos el crawler
        WebCrawler crawler = new WebCrawler("urls.txt", 500, new SerializedRAMIndexBuilder());
        
        //Creamos el motor de busqueda con la query y el limite que queramos 
        SearchEngine engine= new DocBasedVSMEngine(new SerializedRAMIndex("index/web"));
        String query = "sergio";
        int cutoff=5;
            
        //Buscamos en el indice nuestra query predefinida y mostramos los cutoff mejores resultados
        SearchRanking ranking = engine.search(query, cutoff);
        System.out.println("  " + engine.getClass().getSimpleName()
                + " + " + engine.getDocMap().getClass().getSimpleName()
                + ": top " + cutoff + " for query '" + query + "'");
        for (SearchRankingDoc result : ranking) {
            System.out.println("\t" + new TextResultDocRenderer(result));
        }
    }

}

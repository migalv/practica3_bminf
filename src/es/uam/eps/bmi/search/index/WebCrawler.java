/**
 *
 * Fichero WebCrawler.java.
 *
 *
 * @version 1.0
 *
 * Created on 15/03/2019
 */
package es.uam.eps.bmi.search.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Clase WebCrawler encargada de rastrear por la web
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 *
 */
public class WebCrawler {

    //Inicializacion de variables
    IndexBuilder index;
    int maxDocuments;
    String docPath;
    Queue<String> pagesPriorityQueue;
    Queue<String> pagesLastQueue;
    ArrayList<String> hostList;
    ArrayList<String> indexList;
    BufferedWriter writeLink = null;

    /**
     * Constructor de WebCrawler
     *
     * @param docPath path donde se encuentran las URLs semilla
     * @param maxDocuments numero maximo de documentos a rastrear
     * @param index indice del rastreador
     *
     * @throws IOException
     * @throws MalformedURLException
     */
    public WebCrawler(String docPath, int maxDocuments, IndexBuilder index) throws IOException, MalformedURLException {
        this.maxDocuments = maxDocuments;
        this.docPath = docPath;
        this.index = index;

        //Inicializamos dos cola de las urls no rastreadas (una con mas prioridad que la otra) 
        this.pagesPriorityQueue = new LinkedBlockingQueue<>();
        this.pagesLastQueue = new LinkedBlockingQueue<>();

        //Inicializamos una lista con hosts
        this.hostList = new ArrayList<>();

        //Inicializamos una lista con las paginas que se puedan indexar
        this.indexList = new ArrayList<>();

        //Rastreamos las URLs
        this.startCrawling();

        //Finalmente indexamos los documentos rastreados
        this.indexar();
    }

    /**
     * Funcion encargada de manejar las URL con llamadas de busqueda a crawl
     *
     * @throws IOException
     * @throws MalformedURLException
     */
    private void startCrawling() throws IOException, MalformedURLException {
        String url;
        boolean queue = false;

        //Leemos las URLs semilla del fichero para empezar a rastrear
        try (BufferedReader reader = new BufferedReader(new FileReader(this.docPath))) {
            while ((url = reader.readLine()) != null) {
                if (!this.indexList.contains(url)) {
                    //Comprobamos que la URL sea correcta
                    url = this.urlConnect(url);

                    //En caso de que sea correcta lo añadimos a la lista de indexacion
                    if (url == null) {
                        break;
                    }

                    this.indexList.add(url);

                    //Lo añadimos a la cola de prioridad
                    this.pagesPriorityQueue.add(url);
                    queue = true;
                }
            }
        }

        //Creamos el fichero donde guardaremos las URLs rastreadas
        writeLink = new BufferedWriter(new FileWriter("graph" + File.separator + "web-Google.txt"));

        //Llamamos a la funcion crawl para que inspeccione la pagina y obtenga
        //los enlaces a los que apunta la url que queremos analizar
        while (queue == true && (this.maxDocuments > this.indexList.size())) {
            //Sacamos la url de la cola y la rastreamos
            if (!this.pagesPriorityQueue.isEmpty()) {
                url = this.pagesPriorityQueue.poll();
            } else {
                url = this.pagesLastQueue.poll();
            }
            if (url != null) {
                this.crawl(url);
            }

            //Comprobamos que aun tenemos paginas por rastrear
            queue = (!this.pagesPriorityQueue.isEmpty()) | (!this.pagesLastQueue.isEmpty());
        }

        //Cerramos el fichero
        writeLink.close();

    }

    /**
     * Funcion encargada de encontrar los links de una URL dada
     *
     * @param url url de donde buscaremos los links
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    private void crawl(String url) throws MalformedURLException, IOException {
        //Inicializacion de variables
        String urlFinal;

        if (this.urlConnect(url) == null) {
            return;
        }

        //En primer lugar, parseamos y obtenemos los links de tipo 'href'
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException ex) {
            return;
        }
        Elements links = doc.select("a[href]");

        //Lectura de todos los enlaces a los que apunta la URL actual
        for (Element link : links) {
            //Con atributo 'href'
            urlFinal = link.attr("abs:href");

            //Comprobamos que la URL sea correcta
            urlFinal = this.urlConnect(urlFinal);

            //En caso de que sea correcta lo añadimos a la lista de indexacion
            if (urlFinal != null) {

                //Añadimos a la lista aquellas nuevas URLS y a la cola las que no se han leido
                if (!this.indexList.contains(urlFinal)) {
                    this.indexList.add(urlFinal);
                    
                    //Si se encuentra en la lista de host lo metemos a la cola menos prioritaria
                    if (this.hostList.contains(new URL(urlFinal).getHost())) {
                        this.pagesLastQueue.add(urlFinal);
                    } else {//En caso contrario lo añadimos a la cola de prioridad
                        this.pagesPriorityQueue.add(urlFinal);
                    }

                    //Escribimos por pantalla el numero de paginas rastreadas
                    this.writeStatus();
                }

                //Escribimos la URL junto con el link al que apunta
                writeLink.write(url + "\t" + urlFinal + "\n");

                //Comprobamos que no se haya llegado al limite
                if (this.indexList.size() == this.maxDocuments) {
                    return;
                }
            }

        }
    }

    /**
     * Funcion encargada de indexar los documentos que se han rastreado con el
     * crawler, pasando solo aquellos que se pueden indexar.
     */
    private void indexar() throws IOException {

        //Escribimos por pantalla un mensaje de aviso
        System.out.println("Fin del rastreo:\n\nPáginas rastreadas\tHosts diferentes\t");
        System.out.println(this.indexList.size() + "\t\t\t\t" + this.hostList.size() + "\t");
        System.out.println("\nSe ha alcanzado el número máximo de documentos a rastrear.");

        //Cuando alcanzamos el numero maximo de documentos escribimos en un fichero
        //todas las URLs extraidas
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("collections" + File.separator + Config.PATHS_FILE))) {
            for (String page : this.indexList) {
                writer.write(page + "\n");
            }
        }

        //Escribimos por pantalla un mensaje de aviso
        System.out.println("Se han obtenido " + this.indexList.size() + " páginas para indexar.\nIndexando...\n\n");

        //Finalmente, indexamos todos los documentos
        this.index.build("collections" + File.separator + Config.PATHS_FILE, "index/web");

        //Escribimos por pantalla un mensaje de aviso
        System.out.println("Fin de la indexación");
    }

    /**
     * Funcion encargada de conectarse a una pagina dada la url, comprobando que
     * sea correcta, tenga un codigo de respuesta valido y se pueda indexar.
     *
     * @param url url a la que queremos conectarnos
     *
     * @return null si ha dado algun error o la url si es correcta
     */
    private String urlConnect(String url) {
        //Inicializacion de variables
        String host, protocol, finalContent;
        Element contentURL;
        URL urlConnection;

        try {

            //Comprobamos que la URL sea correcta
            try {
                urlConnection = new URL(url);
            } catch (MalformedURLException ex) {
                return null;
            }

            //Obtenemos el host y el protocolo de la url
            host = urlConnection.getHost();
            protocol = urlConnection.getProtocol();

            contentURL = Jsoup.parse(urlConnection, 10000);

            finalContent = contentURL.html();

            if (!finalContent.contains("<html") && !finalContent.contains("<!DOCTYPE html")
                    && !finalContent.contains("<!doctype html") && !finalContent.contains("<script")
                    && !finalContent.contains("<href")) {
                return null;
            }

            //Comprobamos que tenga host y protocolo
            if (host == null || protocol == null || url.length() <= 8) {
                return null;
            }

            //Añadimos el host a la lista de hosts
            if (!this.hostList.contains(host)) {
                this.hostList.add(host);
            }

        } catch (IOException ex) {
            return null;
        }

        return url;
    }

    /**
     * Funcion encargada de escribir por pantalla el tamaño de paginas que se
     * han rastreado hasta el momento.
     */
    private void writeStatus() {
        int indexSize = this.indexList.size();

        if (indexSize == 100) {
            System.out.println("Páginas rastreadas\tHosts diferentes\t");
        }

        //Escribimos por pantalla el estado actual del tamaño de nuestra lista de urls
        if (indexSize % 100 == 0) {
            System.out.println(indexSize + "\t\t\t\t" + this.hostList.size() + "\t");
        }

    }

}

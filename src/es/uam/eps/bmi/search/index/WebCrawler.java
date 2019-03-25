/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author sergio
 */
public class WebCrawler {

    IndexBuilder index;
    int maxDocuments;
    String docPath;
    Queue<String> pagesQueue;
    ArrayList<String> list;

    public WebCrawler(String docPath, int maxDocuments, IndexBuilder index) throws IOException, MalformedURLException {
        this.maxDocuments = maxDocuments;
        this.docPath = docPath;
        this.index = index;
        this.pagesQueue = new LinkedBlockingQueue<>();
        this.list = new ArrayList<>();

        this.startCrawling();

    }

    private void startCrawling() throws IOException, MalformedURLException {
        String url;

        //Leemos las URLs semilla para empezar a rastrear
        try (BufferedReader reader = new BufferedReader(new FileReader(this.docPath))) {
            while ((url = reader.readLine()) != null) {
                if (!this.list.contains(url)) {
                    this.pagesQueue.add(url);
                    this.list.add(url);
                }
            }
        }

        //Llamamos a la funcion crawl para que inspeccione la pagina y obtenga
        //los enlaces a los que apunta la url que queremos analizar
        for (int i = 0; i < 10000; i++) {
            if (this.list.size() > 4000) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("pages.txt"))) {
                    for (String page : this.list) {
                        writer.write(page + "\n");
                    }
                }
                return;
            }
            url = this.pagesQueue.poll();
            this.crawl(url);
        }

        System.out.println(this.list.size());

    }

    private void crawl(String url) throws MalformedURLException, IOException {
        //Inicializacion de variables
        ArrayList links = new ArrayList();
        String[] splitter;
        String urlFinal, content = null;
        URL urlConnection;

        try {
            
            //Comprobamos que la URL tenga protocolo y sea correcta
            try {
                urlConnection = new URL(url);
            } catch (MalformedURLException ex) {
                return;
            }

           /* if (url.contains("https://")) {
                System.out.println("SS");
            }*/

            //Si se trata de una pagina https
            if (url.contains("https")) {
                //Nos conectamos a la URL
                HttpsURLConnection connection;
                connection = (HttpsURLConnection) urlConnection.openConnection();
                
                //Solo accedemos a aquellas que dan un codigo de respuesta en la que podamos leer informacion
                //Nos puede salir tambien un error de que no existe la URL
                try {
                    int code = connection.getResponseCode();
                    if (code >= 400 && code < 500) {
                        return;
                    }
                } catch (IOException ex) {
                    return;
                }

                //Obtenemos el contenido de la pagina
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    scanner.useDelimiter("\\Z");
                    if (scanner.hasNext()) {
                        content = scanner.next();
                    } else {//Si no hay contenido a leer
                        return;
                    }
                }

            } else {//Si se trata de una pagina http
                //Nos conectamos a la URL
                HttpURLConnection connection;
                connection = (HttpURLConnection) urlConnection.openConnection();

                //Solo accedemos a aquellas que dan un codigo de respuesta en la que podamos leer informacion
                //Nos puede salir tambien un error de que no existe la URL
                try {
                    int code = connection.getResponseCode();
                    if (code >= 400 && code < 500) {
                        return;
                    }
                } catch (IOException ex) {
                    return;
                }

                //Obtenemos el contenido de la pagina
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    scanner.useDelimiter("\\Z");
                    if (scanner.hasNext()) {
                        content = scanner.next();
                    } else {//Si no hay contenido a leer
                        return;
                    }
                }
            }

        } catch (IOException ex) {
            return;
        }

        /*  Si se ha llegado a este punto, es que se ha obtenido el codigo html de una
            URL , por lo que podemos buscar en ella los enlaces a los que apunta*/
        
        //En primer lugar, parseamos y obtenemos las paginas que esten formadas por http, https o www
        String regex = "\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);

        //Lectura de todos los enlaces a los que apunta la URL actual
        while (m.find()) {
            //Detectamos un enlace posible
            String urlStr = m.group();
            if (urlStr.startsWith("(\") &amp;&amp; urlStr.endsWith(\")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }

            //Realizamos un parseo y obtenemos las urls con enlace directo a la web principal
            if (urlStr.contains(".es/")) {
                splitter = urlStr.split(".es/");
                urlFinal = splitter[0].concat(".es");
            } else if (urlStr.contains(".com/")) {
                splitter = urlStr.split(".com/");
                urlFinal = splitter[0].concat(".com");
            } else if (urlStr.contains(".fr/")) {
                splitter = urlStr.split(".fr/");
                urlFinal = splitter[0].concat(".fr");
            } else if (urlStr.contains(".org/")) {
                splitter = urlStr.split(".org/");
                urlFinal = splitter[0].concat(".org");
            } else if (urlStr.contains(".cat/")) {
                splitter = urlStr.split(".cat/");
                urlFinal = splitter[0].concat(".cat");
            } else if (urlStr.contains(".jp/")) {
                splitter = urlStr.split(".jp/");
                urlFinal = splitter[0].concat(".jp");
            } else if (urlStr.contains(".edu/")) {
                splitter = urlStr.split(".edu/");
                urlFinal = splitter[0].concat(".edu");
            } else if (urlStr.contains(".us/")) {
                splitter = urlStr.split(".us/");
                urlFinal = splitter[0].concat(".us");
            } else if (urlStr.contains(".uk/")) {
                splitter = urlStr.split(".uk/");
                urlFinal = splitter[0].concat(".uk");
            } else if (urlStr.contains(".gov/")) {
                splitter = urlStr.split(".gov/");
                urlFinal = splitter[0].concat(".gov");
            } else if (urlStr.contains(".fr/")) {
                splitter = urlStr.split(".fr/");
                urlFinal = splitter[0].concat(".fr");
            } else if (urlStr.contains(".it/")) {
                splitter = urlStr.split(".it/");
                urlFinal = splitter[0].concat(".it");
            } else if (urlStr.contains(".br/")) {
                splitter = urlStr.split(".br/");
                urlFinal = splitter[0].concat(".br");
            } else if (urlStr.contains(".at/")) {
                splitter = urlStr.split(".at/");
                urlFinal = splitter[0].concat(".at");
            } else if (urlStr.contains(".info/")) {
                splitter = urlStr.split(".info/");
                urlFinal = splitter[0].concat(".info");
            } else if (urlStr.contains(".net/")) {
                splitter = urlStr.split(".net/");
                urlFinal = splitter[0].concat(".net");
            } else if (urlStr.contains(".coop/")) {
                splitter = urlStr.split(".coop/");
                urlFinal = splitter[0].concat(".coop");
            } else {
                urlFinal = urlStr;
            }

            //Añadimos a la lista aquellas nuevas URLS y a la cola las que no se han leido
            if (!this.list.contains(urlFinal)) {
                this.pagesQueue.add(urlFinal);
                this.list.add(urlFinal);
                switch (this.list.size()) {
                    case 100:
                        System.out.println("100");
                        break;
                    case 200:
                        System.out.println("200");
                        break;
                    case 500:
                        System.out.println("500");
                        break;
                    case 700:
                        System.out.println("700");
                        break;
                    case 1000:
                        System.out.println("1000");
                        break;
                    case 1100:
                        System.out.println("1100");
                        break;
                    case 1200:
                        System.out.println("1200");
                        break;
                    case 1500:
                        System.out.println("1500");
                        break;
                    case 2000:
                        System.out.println("2000");
                        break;
                    case 2500:
                        System.out.println("2500");
                        break;
                    case 3000:
                        System.out.println("3000");
                        break;
                    case 4000:
                        System.out.println("4000");
                        break;
                    case 4500:
                        System.out.println("4500");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

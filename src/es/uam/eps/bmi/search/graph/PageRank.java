/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.graph;

import es.uam.eps.bmi.search.index.DocumentFeatureMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;

/**
 *
 * @author sergio
 */
public class PageRank implements DocumentFeatureMap{
    String linkPath;
    double probability;
    int num_iteration;
    List<String> docPaths;
    
    //Mapa que relaciona docID's con su page rank
    Map<Integer, Double> pages; 
    
    //Lista con los links totales
    List<Pair<Integer,Integer>> links;
    
    public PageRank(String linkPath, double probability, int num_iteration) throws IOException {
        //Inicializacion de variables
        this.linkPath= linkPath;
        this.probability= probability;
        this.num_iteration= num_iteration;
        this.pages= new HashMap<>();
        this.docPaths = new ArrayList<>();
        this.links = new ArrayList<>();

        
        //Cargamos los enlaces de cada pagina
        this.loadLinks();
        
        //Calculamos el page rank de cada pagina
        this.pageRankScore();
    }

    @Override
    public double getValue(int docId) {
        return this.pages.get(docId);
    }

    @Override
    public String getDocPath(int docID) throws IOException {
        return this.docPaths.get(docID);
    }

    //Falta implementarla
    @Override
    public double getDocNorm(int docID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int numDocs() {
        return this.pages.size();
    }

    private void loadLinks() throws FileNotFoundException, IOException {
        String buffer;
        String[] linksBuffer;
        int docIDfrom;
        int docIDto;
        
        //Cargamos los paths de los documentos y cargamos los links de manera bidireccional
        try(BufferedReader in= new BufferedReader(new FileReader(this.linkPath))){
            while( (buffer = in.readLine()) != null ){
                linksBuffer = buffer.split("\t");
                
                //Si 'from' no esta en la ruta de documentos lo añadimos
                if(!this.docPaths.contains(linksBuffer[0])){
                    this.docPaths.add(linksBuffer[0]);
                }
                
                //Si 'to' no esta en la ruta de documentos lo añadimos
                if(!this.docPaths.contains(linksBuffer[1])){
                    this.docPaths.add(linksBuffer[1]);
                }
                
                docIDfrom= this.getDocID(linksBuffer[0]);
                docIDto= this.getDocID(linksBuffer[1]);

                if(docIDfrom != -1 && docIDto != -1){
                    this.links.add(new Pair(docIDfrom,docIDto));
                }
            }
            in.close();
        } 
    }
    
    private void pageRankScore(){
        int num_pages=this.docPaths.size();
        int iter=0,iterFrom=0,iterTo=0;
        List<Integer> outlinks = new ArrayList<>(this.docPaths.size());
        Map<Integer, Double> pagesAux = new HashMap<>();
        double finalValue=0;
        
        //Inicializamos scores a 1/N para cada pagina
        //Tambien inicializamos la variable outlinks
        for(int i=0 ; i< num_pages ;i++){
            this.pages.put(i, (double) 1/num_pages);
            pagesAux.put(i, (double) 1/num_pages);
            outlinks.add(0);
        }
        
        //Computamos el numero de outlinks de todos los nodos
        for(int k=0; k<this.links.size();k++){
            Pair<Integer,Integer> pairLink = this.links.get(k) ;
            int docIDfromIndex=pairLink.getKey();
                    
            //Si aun no existe, la creamos
            if(!outlinks.contains(docIDfromIndex)){
                outlinks.set(docIDfromIndex, 1);
            }else{//Si existe, sumamos uno
                outlinks.set(docIDfromIndex, outlinks.get(docIDfromIndex) +1);
            }
            
        }
        
        
        //Realizamos calculos recursivos hasta que los scores convergen
        // O por otro lado, si hemos llegado al numero maximo de iteraciones
        while(iter < this.num_iteration){//Hay que implementar funcion de convergencia
            
            for(int i=0 ; i< num_pages ;i++){
                pagesAux.put(i, (double) this.probability/num_pages);
            }
            
            for(int k=0; k< this.links.size() ; k++){
                iterFrom=this.links.get(k).getKey();
                iterTo=this.links.get(k).getValue();
                
                //Asignamos el nuevo valor
                finalValue= pagesAux.get(iterTo) + ((1- this.probability) * (this.pages.get(iterFrom)/ outlinks.get(iterFrom))) ;
                pagesAux.put(iterTo, finalValue);
            }
            
            for(int i=0 ; i< num_pages ;i++){
                this.pages.put(i, pagesAux.get(i));
            }
            
            iter++;
        }
        
    }
    
    private int getDocID(String docPath){
        int docID=0;
        
        for(String docAux : this.docPaths){
            if(docAux.equals(docPath)){
                return docID;
            }
            docID++;
        }
        
        return -1;
    }
}

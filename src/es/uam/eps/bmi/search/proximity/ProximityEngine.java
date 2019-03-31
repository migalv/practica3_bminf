/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.proximity;

import es.uam.eps.bmi.search.AbstractEngine;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.index.structure.PostingsListIterator;
import es.uam.eps.bmi.search.index.structure.positional.HeapPostingIterator;
import es.uam.eps.bmi.search.index.structure.positional.PositionalPosting;
import es.uam.eps.bmi.search.index.structure.positional.PositionsIterator;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.impl.RankingImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author migal
 */
public class ProximityEngine extends AbstractEngine {

    // Flag para saber si la query es un literal
    boolean isLiteral;
    
    public ProximityEngine(Index idx) {
        super(idx);
    }

    @Override
    public SearchRanking search(String query, int cutoff) throws IOException {
        PriorityQueue<HeapPostingIterator> hpi;
        RankingImpl ranking = new RankingImpl(index, cutoff);
        String[] queryTerms = parse(query);
        // Flag para saber si continuar el el bucle o no
        boolean cont;
        int rangeA = 0;
        int rangeB = 0;
        double score = 0.0;
        
        // Creamos un min-heap para ordenar los docIDs 
        hpi = new PriorityQueue<>(queryTerms.length);
        
        // Recuperamos los iteradores de las postingslist y metemos los primeros al heap
        int i = 0;
        for(String queryTerm : queryTerms){
            
            PostingsListIterator pli = (PostingsListIterator) index.getPostings(queryTerm).iterator();
            // Metemos los primeros postings y sus iteradores en el heap
            if(pli.hasNext())
                hpi.add(new HeapPostingIterator((PositionalPosting) pli.next(), pli, i++));
            else // Si una de las postings list no tiene ningun posting devolvemos un ranking vacio
                return ranking;
        }
        
        cont = true;
        while(cont){
            // Si todos los docIDs coinciden, entonces podemos calcular su score
            if(checkIfAllEqual(hpi)){
                int docID = hpi.peek().getPositionalPosting().getDocID();
                if(index.getDocPath(docID).equals("clueweb09-en0010-79-2218.html"))
                    cont= true;
                List<PositionsIterator> positionsIterators = new ArrayList<>();
                List<Integer> positions = new ArrayList<>();
                
                // Calculamos el score
                score = 0;
                rangeB = Integer.MIN_VALUE;
                List<HeapPostingIterator> parcialList = new ArrayList<>();
                for(i = 0; i < queryTerms.length; i++){
                    // Recuperamos el HPI y lo metemos a una lista para volver a forma el hpi posteriormente
                    if(!hpi.isEmpty()){
                         parcialList.add(hpi.poll());
                    }
                    // Recuperamos los iteradores de las listas de posiciones
                    positionsIterators.add(parcialList.get(i).iterator());
                    // Recuperamos la última aparición de uno de los terminos
                    if(positionsIterators.get(i).hasNext()){
                        positions.add(positionsIterators.get(i).next());
                        rangeB = Integer.max(rangeB, positions.get(i));
                    }else{
                        score = 0;
                        break;
                    }
                }
                // Recuperamos el hpi, recorriendo la lista parcial
                for(HeapPostingIterator aux : parcialList){
                    hpi.add(aux);
                }
                
                while(rangeB < Integer.MAX_VALUE){
                    i = 0;
                    for(int j = 0; j < queryTerms.length; j++){
                        // Avanzar todas las listas hasta posición b
                        positions.set(j, positionsIterators.get(j).nextBefore(rangeB));
                        // Nos queremos quedar en 𝑖 con la posición más baja de las 𝑞 listas
                        if(positions.get(j) < positions.get(i))
                            i = j;
                    }
                    
                    rangeA = positions.get(i);
                    
                    // Si es un literal entonces comprobamos que todos los terminos van seguidos
                    if(isLiteral){
                        // Flag para saber si los terminos van seguidos
                        boolean seguido = true;
                        for(int j = 0; j < queryTerms.length; j++){
                            // Si las posiciones van seguidas y ademas las anteriores también
                            if(j > 0){
                                if(((positions.get(j-1)+1) == positions.get(j)) && seguido)
                                    seguido = true;
                                else // Si las posiciones no van seguidas entonces lo ponemos a false
                                    seguido = false;
                            }
                        }
                        // Si van todas seguidas entonces sumamos 1 al score
                        if(seguido)
                            score++;
                    }else // Si no es literal utilizamos la formula
                        score += 1.0/(rangeB - rangeA - queryTerms.length + 2);
                    
                    rangeB = positionsIterators.get(i).nextAfter(rangeA);
                }
                
                // Si el score es mayor que cero añadimos el documento
                if(score > 0){
                    ranking.add(docID, score);
                }
                
                // Avanzamos en los iteradores de los postings para el siguiente documento
                for(i = 0; i < queryTerms.length; i++){
                    HeapPostingIterator aux = hpi.poll();
                    PostingsListIterator pli = aux.getIterator();
                    if(pli.hasNext()){
                        hpi.add(new HeapPostingIterator((PositionalPosting) pli.next(), pli, aux.getIndex()));
                    }
                    else{
                        cont = false;
                        break;
                    }
                }
            }
            // Si no sacamos el HPI con el docID más pequeño y iteramos en su postingList
            else{
                HeapPostingIterator aux = hpi.poll();
                // Si tiene otro posting, lo añadimos al heap
                if(aux.getIterator().hasNext()){
                    hpi.add(new HeapPostingIterator((PositionalPosting) aux.getIterator().next(), aux.getIterator(), aux.getIndex()));
                }
                else{ // Terminamos el bucle
                    cont = false;
                }
            }
        }
        
        return ranking;
    }  
    
    @Override
    public String[] parse(String query) {
        
        if(query.startsWith("\"") && query.endsWith("\"")){
            isLiteral = true;
            return query.replaceAll("\"", "").toLowerCase().split("\\P{Alpha}+");
        }
        
        return query.toLowerCase().split("\\P{Alpha}+");
    }
    
    
    public boolean checkIfAllEqual(PriorityQueue<HeapPostingIterator> hpi){
        
        for(HeapPostingIterator aux : hpi){
            if(aux.getPositionalPosting().getDocID() != 
                    hpi.peek().getPositionalPosting().getDocID())
                return false;
        }
        return true;
    }
}

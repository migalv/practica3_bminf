/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search;

import es.uam.eps.bmi.search.index.DocumentMap;
import es.uam.eps.bmi.search.index.impl.DocumentMapImpl;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import es.uam.eps.bmi.search.ranking.impl.RankingImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author migal
 */
public class CombinedEngine implements SearchEngine {

    SearchEngine[] engines;
    double[] weights;
    DocumentMap documentMap;
    private static int MAX_CUTOFF = 1000;
    
    /**
     *
     * @param engines
     * @param weights
     */
    public CombinedEngine(SearchEngine[] engines, double[] weights) {
        this.engines = engines;
        this.weights = weights;
    }

    @Override
    public SearchRanking search(String query, int cutoff) throws IOException {
        
        List<SearchRanking> rankings = new ArrayList<>();
        documentMap = new DocumentMapImpl();
        SearchRanking finalRanking;
        Map<String, Double> documentScore = new HashMap<>();
        
        // Realizamos las busquedas con la query en cada motor de busqueda
        for(SearchEngine engine : engines){
            rankings.add(engine.search(query, MAX_CUTOFF));
        }
        
        // Normalizamos los rankings con Rank-Sim
        int i = 0;
        for(SearchRanking ranking : rankings){
            int rankingPos = 0;
            int rankingSize = ranking.size();
            for(SearchRankingDoc doc : ranking){
                String docPath = doc.getPath();
                
                double newScore = (double) (rankingSize - rankingPos)/rankingSize;
                // Si ya hemos encontrado un documento lo combinamos
                if(documentScore.containsKey(docPath)){
                    double previousScore = documentScore.get(docPath);
                    documentScore.put(docPath, previousScore + (newScore*weights[i]));
                }
                else{ // Si no lo añadimos sin más
                    documentScore.put(docPath, newScore*weights[i]);
                }
                rankingPos++;
            }
            i++;
        }
        
        finalRanking = new RankingImpl(documentMap, cutoff);
        
        i = 0;
        for(Entry<String, Double> entry : documentScore.entrySet()){
            ((DocumentMapImpl) documentMap).addDocument(i, entry.getKey());
            ((RankingImpl) finalRanking).add(i++, entry.getValue());
        }
        
        return finalRanking;
    }

    @Override
    public DocumentMap getDocMap() {
        return documentMap;
    }
    
}

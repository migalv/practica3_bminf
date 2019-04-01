/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.DocumentMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author migal
 */
public class DocumentMapImpl implements DocumentMap {

    Map<Integer, String> documentMap;
    
    public DocumentMapImpl(){
        documentMap = new HashMap<>();
    }
    
    @Override
    public String getDocPath(int docID) throws IOException {
        return documentMap.get(docID);
    }

    @Deprecated
    public double getDocNorm(int docID) throws IOException{return 0;}

    @Override
    public int numDocs() {
        return documentMap.size();
    }
    
    public void addDocument(int docID, String path){
        documentMap.put(docID, path);
    }
}

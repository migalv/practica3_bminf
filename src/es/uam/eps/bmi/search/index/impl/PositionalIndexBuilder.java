/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.impl.PositionalDictionary;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author sergio
 */
public class PositionalIndexBuilder extends SerializedRAMIndexBuilder {
    
    @Override
    public void init(String indexPath) throws IOException{
        clear(indexPath);
        nDocs = 0;
        dictionary = new PositionalDictionary();
        docPaths = new ArrayList<>();
    }
    
    @Override
    public void indexText(String text, String path) throws IOException {
        int position = 0;
        
        for (String term : text.toLowerCase().split("\\P{Alpha}+")){
            ((PositionalDictionary)dictionary).add(term, nDocs, position);
            position ++;
        }
            
        docPaths.add(path);
        nDocs++;
    }
    
    @Override
    protected Index getCoreIndex() {
        return new PositionalIndex(dictionary, nDocs);
    }
    
}

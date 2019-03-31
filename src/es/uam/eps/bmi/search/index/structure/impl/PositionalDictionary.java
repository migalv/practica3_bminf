/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.structure.impl;

import es.uam.eps.bmi.search.index.structure.EditablePostingsList;
import es.uam.eps.bmi.search.index.structure.positional.PositionalPostingsList;

/**
 *
 * @author migal
 */
public class PositionalDictionary extends HashDictionary {
    
    public void add(String term, int docID, int position) {
        if (termPostings.containsKey(term)) ((PositionalPostingsList)termPostings.get(term)).addPosition(docID, position);
        else termPostings.put(term, new PositionalPostingsList (docID, position));
    }
}

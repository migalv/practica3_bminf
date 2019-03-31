/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.structure.positional;

import es.uam.eps.bmi.search.index.structure.EditablePostingsList;
import es.uam.eps.bmi.search.index.structure.positional.PositionalPostingListIterator;
import es.uam.eps.bmi.search.index.structure.Posting;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author migal
 */
public class PositionalPostingsList extends EditablePostingsList {
    
    public PositionalPostingsList(){
        super();
    }
    
    @Override
    public void add(int docID, long freq) {
        add(new PositionalPosting(docID, freq, new ArrayList<>()));
    }
    
    public PositionalPostingsList(int docID, int position){
        super();
        addPosition(docID, position);
    }
    
    public void addPosition(int docID, int position){
        if (!postings.isEmpty() && docID == postings.get(postings.size() - 1).getDocID()){
            ((PositionalPosting) postings.get(postings.size() - 1)).positions.add(position);
            postings.get(postings.size() - 1).add1();
        }           
        else{
            add(docID, 1);
            ((PositionalPosting) postings.get(postings.size() - 1)).positions.add(position);
        }
    }
    
    @Override
    public Iterator<Posting> iterator() {
        return new PositionalPostingListIterator(postings);
    }
}

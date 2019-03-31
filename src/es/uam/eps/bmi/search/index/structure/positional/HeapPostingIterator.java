/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.structure.positional;

import es.uam.eps.bmi.search.index.structure.PostingsListIterator;
import java.util.List;

/**
 *
 * @author migal
 */
public class HeapPostingIterator implements Comparable<HeapPostingIterator> {
    
    PositionalPosting positionalPosting;
    PostingsListIterator postingsListIterator;
    int index;
    
    public HeapPostingIterator(int id, long f, List<Integer> pos, PostingsListIterator pli, int index) {
        positionalPosting = new PositionalPosting(id, f, pos);
        postingsListIterator = pli;
        this.index = index;
    }
    
    public HeapPostingIterator(PositionalPosting pp, PostingsListIterator pli, int index) {
        positionalPosting = pp;
        postingsListIterator = pli;
        this.index = index;
    }
    
    public PostingsListIterator getIterator(){
        return postingsListIterator;
    }
    
    public PositionsIterator iterator(){
        return new PositionsIterator(positionalPosting.positions);
    }
    
    public PositionalPosting getPositionalPosting(){
        return positionalPosting;
    }
    
    public int getIndex(){
        return index;
    }
    
    @Override
    public int compareTo(HeapPostingIterator hpi){
        int diff;
        
        diff = positionalPosting.getDocID() - hpi.getPositionalPosting().getDocID();
        
        if(diff == 0)
            return this.index - hpi.index;
        
        return diff;
    }
}

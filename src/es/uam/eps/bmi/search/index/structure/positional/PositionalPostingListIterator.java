/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.structure.positional;

import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsListIterator;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author migal
 */
public class PositionalPostingListIterator implements PostingsListIterator{
    protected List<Posting> postings;
    protected int currentPosting;
    
    public PositionalPostingListIterator(List<Posting> p){
        postings = p;
    }
    
    @Override
    public boolean hasNext() {
        return currentPosting < postings.size();
    }

    @Override
    public Posting next() {
        return postings.get(currentPosting++);
    }

}

/*
 * Copyright (C) 2012 Universitat d'Alacant
 *
 * author: Miquel Esplà Gomis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package es.ua.dlsi.alignment;

import es.ua.dlsi.utils.CmdLineParser;
import es.ua.dlsi.utils.Pair;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class Symetrisation {
    
    /**
     * Symetrisation method which produces, as a result, the intersection of the
     * S2T and T2S alignments.
     * @param instance
     * @param s2t
     * @param t2s
     * @return 
     */
    static public boolean[][] IntersectionSymal(Set<Integer>[] s2t, Set<Integer>[] t2s){
        boolean[][] intersection=new boolean[s2t.length][t2s.length];
        //Initialisation of the alignment matrix to "all the words are unaligned"
        for(boolean[] row: intersection)
            Arrays.fill(row, false);
        //For all the alignments in S2T
        for(int s_word=0;s_word<s2t.length;s_word++){
            if(s2t[s_word]!=null){
                for(Integer t_word: s2t[s_word]){
                    if(t2s[t_word]!=null){
                        //If the alignment appears in both the asymetric alignments, it is added to the symetrised alignment
                        if(t2s[t_word].contains(s_word))
                            intersection[s_word][t_word]=true;
                    }
                }
            }
        }
        return intersection;
    }
    
    /**
     * Symetrisation method which produces, as a result, the union of the
     * @param instance
     * @param s2t
     * @param t2s
     * @return 
     */
    static public boolean[][] UnionSymal(Set<Integer>[] s2t, Set<Integer>[] t2s){
        boolean[][] union=new boolean[s2t.length][t2s.length];
        //Initialisation of the alignment matrix to "all the words are unaligned"
        for(boolean[] row: union)
            Arrays.fill(row, false);
        //All the alignments in S2T are added to the union
        for(int s_word=0;s_word<s2t.length;s_word++){
            if(s2t[s_word]!=null){
                for(Integer t_word: s2t[s_word]){
                    union[s_word][t_word]=true;
                }
            }
        }
        //All the alignments in T2S are added to the union
        for(int t_word=0;t_word<t2s.length;t_word++){
            if(t2s[t_word]!=null){
                for(Integer s_word: t2s[t_word]){
                    union[s_word][t_word]=true;
                }
            }
        }
        return union;
    }
    
    static public boolean[][] GrowDiagFinalAnd(Set<Integer>[] s2t, Set<Integer>[] t2s){
        List<Pair<Integer,Integer>> neighbors=new LinkedList<Pair<Integer, Integer>>(); //neighbors

        //Defining neibourhood
        neighbors.add(new Pair(0,1));
        neighbors.add(new Pair(-1,-0));
        neighbors.add(new Pair(0,-1));
        neighbors.add(new Pair(1,0));

        //Diagonal (diag) neigourhood
        neighbors.add(new Pair(-1,-1));
        neighbors.add(new Pair(-1,1));
        neighbors.add(new Pair(1,-1));
        neighbors.add(new Pair(1,1));
        
        //Intersection of the alignments (starting point)
        boolean[][] currentpoints=IntersectionSymal(s2t, t2s); //symmetric alignment
        //Union of the alignments (space for growing)
        boolean[][] unionalignment=UnionSymal(s2t, t2s); //union alignment
        //Adding currently unaligned words in SL to the list
        Set<Integer> unaligned_s=new LinkedHashSet<Integer>();
        for(int current_row=0;current_row<currentpoints.length;current_row++){
            boolean aligned=false;
            for(int current_col=0;current_col<currentpoints[current_row].length;current_col++){
                if(currentpoints[current_row][current_col]){
                    aligned=true;
                    break;
                }
            }
            if(!aligned)
                unaligned_s.add(current_row);
        }
        //Adding currently unaligned words in TL to the list
        Set<Integer> unaligned_t=new LinkedHashSet<Integer>();
        for(int current_col=0;current_col<currentpoints[0].length;current_col++){
            boolean aligned=false;
            for(int current_row=0;current_row<currentpoints.length;current_row++){
                if(currentpoints[current_row][current_col]){
                    aligned=true;
                    break;
                }
            }
            if(!aligned)
                unaligned_t.add(current_col);
        }

        boolean added;

        //Grow-diag
        do{
            added=false;
            //For all the current alignment
            for(int current_row=0;current_row<currentpoints.length;current_row++){
                for(int current_col=0;current_col<currentpoints[current_row].length;current_col++){
                    //If the word is aligned, the neibourghs are checked
                    if(currentpoints[current_row][current_col]){
                        for(Pair<Integer,Integer> n: neighbors){
                            int p1=current_row+n.getFirst();
                            int p2=current_col+n.getSecond();
                            if(p1>=0 && p1<currentpoints.length && p2>=0 && p2<currentpoints[0].length){
                                //Check the neighbours
                                if((unaligned_s.contains(p1) || unaligned_t.contains(p2)) &&
                                        unionalignment[p1][p2]){
                                    currentpoints[p1][p2]=true;
                                    unaligned_s.remove(p1);
                                    unaligned_t.remove(p2);
                                    added=true;
                                }
                            }
                        }
                    }
                }
            }
        }while (added);
            
        //Final-and
        for(int sw: unaligned_s){
            int t_toremove=-1;
            for(int tw: unaligned_t){
                if(unionalignment[sw][tw]){
                    t_toremove=tw;
                    currentpoints[sw][tw]=true;
                    break;
                }
            }
            unaligned_t.remove(t_toremove);
        }
        
        return currentpoints;
    }
}

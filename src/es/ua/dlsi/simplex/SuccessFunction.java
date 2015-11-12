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

package es.ua.dlsi.simplex;

import es.ua.dlsi.alignment.TrainedAlignment;
import es.ua.dlsi.alignment.ReferenceAlignment;
import es.ua.dlsi.features.Instance;
import es.ua.dlsi.utils.Pair;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class SuccessFunction{

    static double AER(List<List<Pair<Integer,Integer>>> alignments, List<Instance> instances){
        if(alignments.size()!=instances.size()){
            System.err.println("For coputing AER, both lists of alignments must have the same number of paris of sentences.");
            System.exit(-1);
        }
        Set<Pair<Integer,Integer>> common_sure_al=new LinkedHashSet<Pair<Integer,Integer>>();
        Set<Pair<Integer,Integer>> common_possible_al=new LinkedHashSet<Pair<Integer,Integer>>();
        
        int surerefal=0;
        int nalignments=0;
        int ncommon_sure_al=0;
        int ncommon_possible_al=0;
        for(int align=0;align<alignments.size();align++){
            Instance i=instances.get(align);
            List<Pair<Integer,Integer>> alignment=alignments.get(align);
            for(Pair<Integer,Integer> p: alignment){
                nalignments++;
                if(i.isSureAlignment(p.getFirst(), p.getSecond())){
                    common_sure_al.add(p);
                    common_possible_al.add(p);
                }
                else if(i.isPossibleAlignment(p.getFirst(), p.getSecond()))
                    common_possible_al.add(p);
            }
            ncommon_sure_al+=common_sure_al.size();
            ncommon_possible_al+=common_possible_al.size();
            for(Set<ReferenceAlignment> raset: i.getAlignment()){
                if(raset!=null){
                    for(ReferenceAlignment ra: raset){
                        if(ra.isSure())
                            surerefal++;
                    }
                }
            }
        }
        return 1.0-((double)ncommon_sure_al+(double)ncommon_possible_al)/((double)nalignments+(double)surerefal);
    }
    
    static double Precision(List<List<Pair<Integer,Integer>>> alignments, List<Instance> instances){
        if(alignments.size()!=instances.size()){
            System.err.println("For coputing AER, both lists of alignments must have the same number of paris of sentences.");
            System.exit(-1);
        }
        int nalign=0;
        Set<Pair<Integer,Integer>> common_al=new LinkedHashSet<Pair<Integer,Integer>>();
        for(int align=0;align<alignments.size();align++){
            Instance i=instances.get(align);
            List<Pair<Integer,Integer>> alignment=alignments.get(align);
            nalign+=alignment.size();
            for(Pair<Integer,Integer> p: alignment){
                if(i.isSureAlignment(p.getFirst(), p.getSecond())){
                    common_al.add(p);
                }
                else if(i.isPossibleAlignment(p.getFirst(), p.getSecond()))
                    common_al.add(p);
            }
        }
        return ((double)common_al.size()/((double)nalign));
    }
    
    static double Recall(List<List<Pair<Integer,Integer>>> alignments, List<Instance> instances){
        if(alignments.size()!=instances.size()){
            System.err.println("For coputing AER, both lists of alignments must have the same number of paris of sentences.");
            System.exit(-1);
        }
        int nalign_ref=0;
        Set<Pair<Integer,Integer>> common_al=new LinkedHashSet<Pair<Integer,Integer>>();
        for(int align=0;align<alignments.size();align++){
            List<Pair<Integer,Integer>> alignment=alignments.get(align);
            Instance i=instances.get(align);
            nalign_ref+=i.CountAlignments();
            for(Pair<Integer,Integer> p: alignment){
                if(i.isSureAlignment(p.getFirst(), p.getSecond())){
                    common_al.add(p);
                }
                else if(i.isPossibleAlignment(p.getFirst(), p.getSecond()))
                    common_al.add(p);
            }
        }
        return ((double)common_al.size()/((double)nalign_ref));
    }
    
    static double FMeasure(List<List<Pair<Integer,Integer>>> alignments, List<Instance> instances){
        double precision=Precision(alignments, instances);
        double recall=Recall(alignments, instances);
        return 2*precision*recall/(precision+recall);
    }
    
    static double ComputeAER(Map<Instance,double[][]> probs, List<Double> null_probabilities) {
        List<List<Pair<Integer,Integer>>> alignments=new
                LinkedList<List<Pair<Integer, Integer>>>();
        List<Instance> instances=new LinkedList<Instance>();
        Iterator<Double> nprob_iter=null_probabilities.iterator();
        for(Entry<Instance,double[][]> e: probs.entrySet()){
            Instance i=e.getKey();
            instances.add(i);
            double[][] jprobs=e.getValue();
            double null_probability=nprob_iter.next();
            Map<Integer,Integer> al1=TrainedAlignment.GreedyAlignProbPrecomputedS2T(i,jprobs, null_probability);
            Map<Integer,Integer> al2=TrainedAlignment.GreedyAlignProbPrecomputedT2S(i,jprobs, null_probability);
            List<Pair<Integer,Integer>> alignment=new LinkedList<Pair<Integer, Integer>>();
            boolean[][] newalignment=TrainedAlignment.GrowDiagFinalAnd(i,al1,al2);
            for(int row=0;row<newalignment.length;row++){
                for(int col=0;col<newalignment[row].length;col++){
                    if(newalignment[row][col]){
                        Pair<Integer,Integer> pair=new Pair<Integer, Integer>(row, col);
                        alignment.add(pair);
                    }
                }
            }
            alignments.add(alignment);
        }
        return AER(alignments, instances);
    }
    
    static double ComputeFMeasure(Map<Instance,double[][]> probs, List<Double> null_probabilities) {
        List<List<Pair<Integer,Integer>>> alignments=new
                LinkedList<List<Pair<Integer, Integer>>>();
        List<Instance> instances=new LinkedList<Instance>();
            Iterator<Double> nprob_iter=null_probabilities.iterator();
        for(Entry<Instance,double[][]> e: probs.entrySet()){
            Instance i=e.getKey();
            instances.add(i);
            double[][] jprobs=e.getValue();
            double null_probability=nprob_iter.next();
            Map<Integer,Integer> al1=TrainedAlignment.GreedyAlignProbPrecomputedS2T(i,jprobs,null_probability);
            Map<Integer,Integer> al2=TrainedAlignment.GreedyAlignProbPrecomputedT2S(i,jprobs,null_probability);
            List<Pair<Integer,Integer>> alignment=new LinkedList<Pair<Integer, Integer>>();
            boolean[][] newalignment=TrainedAlignment.GrowDiagFinalAnd(i,al1,al2);
            for(int row=0;row<newalignment.length;row++){
                for(int col=0;col<newalignment[row].length;col++){
                    if(newalignment[row][col]){
                        Pair<Integer,Integer> pair=new Pair<Integer, Integer>(row, col);
                        alignment.add(pair);
                    }
                }
            }
            alignments.add(alignment);
        }
        return FMeasure(alignments, instances);
    }
}

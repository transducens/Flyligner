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

package es.ua.dlsi.gradientdescent;

import es.ua.dlsi.alignment.ReferenceAlignment;
import es.ua.dlsi.features.Instance;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class SuccessFunction{
    
    /*static double JointProbabilityDerivative(Instance instance, int word1, int word2,
            int feat_row, int feat_col, double[][] jointprobs, double null_probability){
        double sum=0.0;
        double feat_w1w2=instance.GetFeatures(word1, word2).GetFeature(feat_row+1, feat_col+1);
        for(int w1=0;w1<instance.getSegment1().size();w1++){
            if(instance.getAlignment()[w1]!=null){
                Set<ReferenceAlignment> words2=instance.getAlignment()[w1];
                for(ReferenceAlignment w2: words2){
                    double prob_ij=jointprobs[w1][w2.getCounterpart()];
                    double feat_ij_w1w2=instance.GetFeatures(w1,
                            w2.getCounterpart()).GetFeature(feat_row+1, feat_col+1);
                    sum+=prob_ij*(feat_ij_w1w2);
                }
            }
        }
        sum+=null_probability*(instance.getNunaligned_s2()+instance.getNunaligned_s2());
        return feat_w1w2-sum;
    }*/
    
    static double JointProbabilityDerivative(Instance instance, int word1, int word2,
            int feat_row, int feat_col, double sum){
        double feat_w1w2=instance.GetFeatures(word1, word2).GetFeature(feat_row+1, feat_col+1);
        return feat_w1w2-sum;
    }
    
    static double JointProbabilityDerivativeSusbtractingTerm(Instance instance,
            int feat_row, int feat_col, double[][] jointprobs, double null_probability){
        double sum=0.0;
        for(int w1=0;w1<instance.getSegment1().size();w1++){
            if(instance.getAlignment()[w1]!=null){
                Set<ReferenceAlignment> words2=instance.getAlignment()[w1];
                for(ReferenceAlignment w2: words2){
                    double prob_ij=jointprobs[w1][w2.getCounterpart()];
                    double feat_ij_w1w2=instance.GetFeatures(w1,
                            w2.getCounterpart()).GetFeature(feat_row+1, feat_col+1);
                    sum+=prob_ij*(feat_ij_w1w2);
                }
            }
        }
        sum+=null_probability*(instance.getNunaligned_s2()+instance.getNunaligned_s2());
        return sum;
    }
    
    static double NullAlignmentLogJointProbabilityDerivative(Instance instance,
            int feat_row, int feat_col, double[][] jointprobs, double null_probability){
        double sum=0.0;
        for(int w1=0;w1<instance.getSegment1().size();w1++){
            if(instance.getAlignment()[w1]!=null){
                Set<ReferenceAlignment> words2=instance.getAlignment()[w1];
                for(ReferenceAlignment w2: words2){
                    double prob_ij=jointprobs[w1][w2.getCounterpart()];
                    double feat_ij_w1w2=instance.GetFeatures(w1,
                            w2.getCounterpart()).GetFeature(feat_row+1, feat_col+1);
                    sum+=prob_ij*(feat_ij_w1w2);
                }
            }
        }
        sum+=null_probability*(instance.getNunaligned_s2()+instance.getNunaligned_s1());
        return -sum;
    }
    
    /*static double JointProbabilityDerivativeDelta(Instance instance, int w1, int w2,
            int feat_row, int feat_col, double[][] jointprobs){
        double sum=0.0;
        for(int i=0;i<instance.getSegment1().size();i++){
            int deltajj;
            if(i==w1)
                deltajj=1;
            else
                deltajj=0;
            for(int j=0;j<instance.getSegment2().size();j++){
                int deltakk;
                if(j==w2)
                    deltakk=1;
                else
                    deltakk=0;
                double prob_ij=jointprobs[i][j];
                double feat_ij=instance.GetFeatures(i, j).GetFeature(feat_row+1, feat_col+1);
                sum+=(deltajj*deltakk-prob_ij)*feat_ij;
            }
        }
        return sum;
    }*/
    
    static double Compute(Map<Instance,double[][]> probs, List<Double> null_probabilities) {
        double sum=0.0;
        Iterator<Double> null_prob_iter=null_probabilities.iterator();
        for(Entry<Instance,double[][]> e: probs.entrySet()){
            Instance i=e.getKey();
            double nullprob=Math.log(null_prob_iter.next());
            double[][] jprobs=e.getValue();
            for(int w1=0;w1<i.getSegment1().size();w1++){
                //Improvement only valid for EvaluationFunction = prob*a
                if(i.getAlignment()[w1]!=null){
                    Set<ReferenceAlignment> words2=i.getAlignment()[w1];
                    for(ReferenceAlignment w2: words2){
                        sum+=Math.log(jprobs[w1][w2.getCounterpart()]);
                    }
                }
            }
            sum+=nullprob*(i.getNunaligned_s2()+i.getNunaligned_s1());
        }
        return sum;
    }
    
    static double Derivative(int deriv_reference_row, int deriv_reference_col,
            Map<Instance,double[][]> probs, List<Double> null_probabilities) {
        double sum=0.0;
        Iterator<Double> null_prob_iter=null_probabilities.iterator();
        for(Entry<Instance,double[][]> e: probs.entrySet()){
            Instance i=e.getKey();
            double[][] jprobs=e.getValue();
            double nullprob=null_prob_iter.next();
            double derivative_second_term=JointProbabilityDerivativeSusbtractingTerm(i,
                    deriv_reference_row, deriv_reference_col, jprobs, nullprob);
            for(int w1=0;w1<i.getSegment1().size();w1++){
                //Improvement only valid for EvaluationFunction = prob*a
                if(i.getAlignment()[w1]!=null){
                    Set<ReferenceAlignment> words2=i.getAlignment()[w1];
                    for(ReferenceAlignment w2: words2){
                        //sum+=JointProbabilityDerivative(i, w1, w2.getCounterpart(),
                        //        deriv_reference_row, deriv_reference_col, jprobs, nullprob);
                        sum+=JointProbabilityDerivative(i, w1, w2.getCounterpart(),
                                deriv_reference_row, deriv_reference_col, derivative_second_term);
                    }
                }else{
                    sum+=NullAlignmentLogJointProbabilityDerivative(i, deriv_reference_row,
                            deriv_reference_col, jprobs, nullprob);
                }
            }
        }
        return sum;
    }
}

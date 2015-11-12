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

import es.ua.dlsi.features.FeaturesMatrix;
import es.ua.dlsi.features.Instance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miquel
 */
public class Probabilities {
    static public Map<Instance,double[][]> BuildAllJointProbabilityMatrix(List<Instance> instances,
            double[][] weights, List<Double> null_probabilities){
        Map<Instance,double[][]> probs=new HashMap<Instance, double[][]>();
        for(Instance i: instances){
            probs.put(i,BuildJointProbabilityMatrix(i, weights));
            null_probabilities.add(NullProbability(i, weights));
        }
        return probs;
    }
    
    /*static public Map<Instance,double[][]> BuildAllLogJointProbabilityMatrix(List<Instance> instances,
            double[][] weights, List<Double> null_probabilities){
        Map<Instance,double[][]> probs=new HashMap<Instance, double[][]>();
        int counter=0;
        for(Instance i: instances){
            counter++;
            probs.put(i,BuildLogJointProbabilityMatrix(i, weights));
            null_probabilities.add(LogNullProbability(i, weights));
        }
        return probs;
    }*/
    
    static public double[][] BuildJointProbabilityMatrix(Instance i, double[][] weights){
        double normalisation_denom=NormalisationDenomForJointProbability(i, weights);
        double[][] p=new double[i.getSegment1().size()][i.getSegment2().size()];
        for(int w1=0;w1<i.getSegment1().size();w1++){
            for(int w2=0;w2<i.getSegment2().size();w2++){
                p[w1][w2]=JointProbability(i, w1, w2, weights, normalisation_denom);
            }
        }
        return p;
    }
    
    /*static public double[][] BuildLogJointProbabilityMatrix(Instance i, double[][] weights){
        double normalisation_denom=NormalisationDenomForJointProbability(i, weights);
        double[][] p=new double[i.getSegment1().size()][i.getSegment2().size()];
        for(int w1=0;w1<i.getSegment1().size();w1++){
            for(int w2=0;w2<i.getSegment2().size();w2++){
                p[w1][w2]=Math.log(JointProbability(i, w1, w2, weights, normalisation_denom));
            }
        }
        return p;
    }*/

    static double SubFunction(FeaturesMatrix fm, double[][] weights){
        double sum=0.0;
        for(int row=0;row<weights.length;row++){
            for(int col=0;col<weights.length;col++){
                sum+=weights[row][col]*fm.GetFeature(row+1,col+1);
            }
        }
        return Math.exp(sum);
    }
    
    static public double JointProbability(Instance instance, int w1, int w2, double[][] weights, double denom){
        double num=SubFunction(instance.GetFeatures(w1, w2), weights);
        return num/denom;
    }

    static public double NormalisationDenomForJointProbability(Instance instance, double[][] weights){
        double denom=0.0;
        for(int i=0;i<instance.getSegment1().size();i++){
            for(int j=0;j<instance.getSegment2().size();j++){
                denom+=SubFunction(instance.GetFeatures(i, j), weights);
            }
        }
        //the +1 is caused by the null probability which is exp(SUM(lambda_i*0))
        return denom+instance.getSegment1().size()*instance.getSegment2().size();
    }

    static public double NullProbability(Instance instance, double[][] weights){
        return 1.0/NormalisationDenomForJointProbability(instance, weights);
    }
    
    /*static public double LogNullProbability(Instance instance, double[][] weights){
        return Math.log(NullProbability(instance, weights));
    }*/
}

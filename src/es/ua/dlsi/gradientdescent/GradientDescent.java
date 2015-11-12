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

import es.ua.dlsi.alignment.Probabilities;
import es.ua.dlsi.features.Instance;
import es.ua.dlsi.segmentation.Evidence;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miquel
 */
public class GradientDescent {
    static public double[][] Maximize(List<Instance> instances, double[][] weights,
            double learning_rate, double convergence_condition){
        double current_gain=0;
        double prev_gain;
        double[][] prev_weights=weights.clone();
        List<Double> null_probabilities=new LinkedList<Double>();
        Map<Instance,double[][]> probabilities=Probabilities.
                BuildAllJointProbabilityMatrix(instances, weights, null_probabilities);
        double val=SuccessFunction.Compute(probabilities, null_probabilities);
        int iter=0;
        double errordiff;
        do{
            iter++;
            double[][] new_weights=new double[prev_weights.length][prev_weights.length];
            for(int k=0;k<prev_weights.length;k++)
                System.arraycopy(prev_weights[k], 0, new_weights[k], 0, prev_weights[k].length);
            for(int row=0;row<prev_weights.length;row++){
                for(int col=0;col<prev_weights[row].length;col++){
                    new_weights[row][col]+=learning_rate*SuccessFunction.
                            Derivative(row, col, probabilities, null_probabilities);
                }
            }
            null_probabilities=new LinkedList<Double>();
            probabilities=Probabilities.BuildAllJointProbabilityMatrix(instances, new_weights, null_probabilities);
            double new_val=SuccessFunction.Compute(probabilities, null_probabilities);
            prev_gain=current_gain;
            current_gain=new_val;
           // val=new_val;
            prev_weights=new_weights;
            //errordiff=Math.abs(Math.exp(current_gain)/Math.exp(prev_gain))-1;
            errordiff=Math.abs(prev_gain/current_gain)-1;
            System.err.println(errordiff);
        }while(iter<3 || errordiff>convergence_condition);
        for(int i=0;i<Evidence.max_seg_len;i++){
            for(int j=0;j<Evidence.max_seg_len;j++){
                System.out.print(prev_weights[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
        return prev_weights;
    }
}

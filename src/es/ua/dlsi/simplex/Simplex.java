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

import es.ua.dlsi.alignment.Probabilities;
import es.ua.dlsi.features.Instance;
import es.ua.dlsi.segmentation.Evidence;
import es.ua.dlsi.utils.Pair;
import java.util.*;

public class Simplex
{   
    //NelderMeadMinimizer
    static public double[][] Maximize(List<Instance> instances, double convergence_condition)
    {
        int dimensions=Evidence.max_seg_len*Evidence.max_seg_len+1;
        List<Pair<Double,double[][]>> valuated_vertex=new LinkedList<Pair<Double,double[][]>>();
        
        // Create and initialize the vertex
        Random rand=new Random(2);
        for(int i=0;i<dimensions;i++){
            double[][] tmpweights=new double[Evidence.max_seg_len][Evidence.max_seg_len];
            for(int k=0;k<Evidence.max_seg_len;k++){
                for(int l=0;l<Evidence.max_seg_len;l++)
                    tmpweights[k][l]=rand.nextDouble()*1;//*0.1;
            }
            List<Double> null_probabilities=new LinkedList<Double>();
            Map<Instance,double[][]> probabilities=Probabilities.
                    BuildAllJointProbabilityMatrix(instances, tmpweights, null_probabilities);
            double value=SuccessFunction.ComputeAER(probabilities,null_probabilities);
            valuated_vertex.add(new Pair<Double, double[][]>(value,tmpweights));
        }
        boolean convergence;
        BubbleSortValuatedVertex(valuated_vertex);
        do{
            convergence=Step(valuated_vertex, instances, convergence_condition);
        }while(!convergence);
        return valuated_vertex.get(0).getSecond();
    }

    public static void BubbleSortValuatedVertex(List<Pair<Double,double[][]>> values)
    {
        for(int j = 0; j < values.size(); j++ ) {
            boolean no_exch = true;
            for(int i = values.size()-1; i > j; i-- )
                if( values.get(i).getFirst() < values.get(i-1).getFirst() ) {
                    Pair<Double,double[][]> v = values.get(i);
                    values.set(i, values.get(i-1));
                    values.set(i-1, v);
                    no_exch = false;
                }

            if( no_exch )
                break;
        }
    }
    
    public static double[][] CenterPoint(List<Pair<Double,double[][]>> vertex){
        double[][] mvertex=new double[Evidence.max_seg_len][Evidence.max_seg_len];
        for(int i=0;i<Evidence.max_seg_len;i++){
            for(int j=0;j<Evidence.max_seg_len;j++){
                double point=0.0;
                for(int p=0; p<vertex.size()-1;p++)
                    point+=vertex.get(p).getSecond()[i][j];
                mvertex[i][j]=point/vertex.size();
            }
        }
        return mvertex;
    }
    
    public static List<double[][]> Reduction(List<Pair<Double,double[][]>> vertexlist){
        List<double[][]> newvertexlist=new LinkedList<double[][]>();
        double[][] bestvertex=vertexlist.get(0).getSecond();
        for(int vert=1; vert<vertexlist.size();vert++){
            double[][] vertex=vertexlist.get(vert).getSecond();
            double[][] newvertex=new double[Evidence.max_seg_len][Evidence.max_seg_len];
            for(int i=0;i<Evidence.max_seg_len;i++){
                for(int j=0;j<Evidence.max_seg_len;j++){
                    newvertex[i][j]=bestvertex[i][j]+0.5*(vertex[i][j]-bestvertex[i][j]);
                }
            }
            newvertexlist.add(newvertex);
        }
        return newvertexlist;
    }
    
    public static double[][] Expansion(double[][] mvertex, double[][] worst){
        return Transformation(mvertex, worst, 2);
    }
    
    public static double[][] Contraction(double[][] mvertex, double[][] worst){
        return Transformation(mvertex, worst, -0.5);
    }
    
    public static double[][] Reflection(double[][] mvertex, double[][] worst){
        return Transformation(mvertex, worst, 1);
    }
    
    public static double[][] Transformation(double[][] mvertex, double[][] worst, double factor){
        double[][] newvertex=new double[Evidence.max_seg_len][Evidence.max_seg_len];
        for(int i=0;i<Evidence.max_seg_len;i++){
            for(int j=0;j<Evidence.max_seg_len;j++){
                double step=factor*(mvertex[i][j]-worst[i][j]);
                newvertex[i][j]=mvertex[i][j]+step;
            }
        }
        return newvertex;
    }

    public static boolean Step(List<Pair<Double,double[][]>> vertexlist, List<Instance> instances, double convergencecond){
        double[][] mvertex=CenterPoint(vertexlist);
        
        Pair<Double,double[][]> worstpair=vertexlist.get(vertexlist.size()-1);
        double[][] worstpoint=worstpair.getSecond();
        //double worstvalue=worstpair.getFirst();
        //double[][] bestpoint=vertexlist.get(0).getSecond();
        //double[][] bestpoint=vertexlist.get(0).getSecond();
        double[][] reflectedpoint=Reflection(mvertex,worstpoint);
        List<Double> null_probabilities=new LinkedList<Double>();
        Map<Instance,double[][]> reflectedprobabilities=Probabilities.
                BuildAllJointProbabilityMatrix(instances, reflectedpoint, null_probabilities);
        double reflectedvalue=SuccessFunction.ComputeAER(reflectedprobabilities,null_probabilities);
        
        double bestvalue=vertexlist.get(0).getFirst();
        double second_worse_value=vertexlist.get(vertexlist.size()-2).getFirst();
        
        if(reflectedvalue<bestvalue){
            double[][] expandedpoint=Reflection(mvertex,worstpoint);
            Map<Instance,double[][]> expandedprobabilities=Probabilities.
                    BuildAllJointProbabilityMatrix(instances, reflectedpoint, null_probabilities);
            double expandedvalue=SuccessFunction.ComputeAER(expandedprobabilities,null_probabilities);
            if(reflectedvalue<expandedvalue){
                System.err.println("Choosing expansion");
                worstpair.setFirst(expandedvalue);
                worstpair.setSecond(expandedpoint);
            }
            else{
                System.err.println("Choosing refleciton");
                worstpair.setFirst(reflectedvalue);
                worstpair.setSecond(reflectedpoint);
            }
        }
        else{
            if(reflectedvalue<second_worse_value){
                System.err.println("Choosing refleciton");
                worstpair.setFirst(reflectedvalue);
                worstpair.setSecond(reflectedpoint);
            }
            else{
                double[][] contractedpoint=Contraction(mvertex,worstpoint);
                Map<Instance,double[][]> contractedprobabilities=Probabilities.
                        BuildAllJointProbabilityMatrix(instances, reflectedpoint, null_probabilities);
                double contractedvalue=SuccessFunction.ComputeAER(contractedprobabilities,null_probabilities);
                if(contractedvalue<worstpair.getFirst()){
                    System.err.println("Choosing contraction");
                    worstpair.setFirst(contractedvalue);
                    worstpair.setSecond(contractedpoint);
                }
                else{
                    System.err.println("Choosing reduction");
                     List<double[][]> newsublist=Reduction(vertexlist);
                     for(int vert=1; vert<vertexlist.size();vert++){
                         Map<Instance,double[][]> newprobabilities=Probabilities.
                                BuildAllJointProbabilityMatrix(instances, newsublist.get(vert-1), null_probabilities);
                        double newdvalue=SuccessFunction.ComputeAER(newprobabilities,null_probabilities);
                        vertexlist.get(vert).setFirst(newdvalue);
                        vertexlist.get(vert).setSecond(newsublist.get(vert-1));
                     }
                }
            }
        }
        BubbleSortValuatedVertex(vertexlist);
        double[][] bestpoint=vertexlist.get(0).getSecond();
        worstpoint=vertexlist.get(vertexlist.size()-1).getSecond();
        return EvaluateConvergence(bestpoint, worstpoint, convergencecond);
    }
    
    static public boolean EvaluateConvergence(double[][] best, double[][] worst, double condition){
        double sum=0.0;
        for(int i=0;i<best.length;i++){
            for(int j=0;j<best.length;j++){
                sum+=Math.abs(best[i][j]-worst[i][j]);
            }
        }
        double val=sum/Math.pow(best.length,2);
        System.err.println(val);
        if(val<condition) return true;
        else return false;
    }
}
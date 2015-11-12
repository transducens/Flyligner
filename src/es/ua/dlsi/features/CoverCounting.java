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

package es.ua.dlsi.features;

import es.ua.dlsi.segmentation.Evidence;
import java.util.Arrays;

/**
 *
 * @author miquel
 */
public class CoverCounting {
    
    private int[][] coveragematrix;
    
    public CoverCounting(){
        coveragematrix=new int[Evidence.max_seg_len][Evidence.max_seg_len];
        for(int i=0;i<coveragematrix.length;i++)
            Arrays.fill(coveragematrix[i], 0);
    }
    
    public void AddCover(int l1, int l2){
        if(l1<=Evidence.max_seg_len && l2<=Evidence.max_seg_len)
            coveragematrix[l1-1][l2-1]++;
    }
    
    public int GetTotalCovering(int l1, int l2){
        return coveragematrix[l1-1][l2-1];
    }
    
    public FeaturesMatrix GetFeatures(){
        FeaturesMatrix exit=new FeaturesMatrix();
        for(int i=0;i<this.coveragematrix.length;i++){
            for(int j=0;j<this.coveragematrix[i].length;j++){
                int l1=i+1;
                int l2=j+1;
                if(coveragematrix[i][j]==0)
                    exit.SetFeature(l1, l2, 0.0);
                else{
                    //int max_subsegs=((this.coveragematrix.length-l1)+1)*((this.coveragematrix[i].length-l2)+1);
                    exit.SetFeature(l1, l2, (double)coveragematrix[i][j]);///max_subsegs);
                }
            }
        }
        return exit;
    }
}

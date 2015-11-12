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
public class FeaturesMatrix {
    
    private double[][] featurelist;
    
    public FeaturesMatrix(){
        featurelist=new double[Evidence.max_seg_len][Evidence.max_seg_len];
        for(int i=0;i<featurelist.length;i++)
            Arrays.fill(featurelist[i],0.0);
    }
    
    public void SetFeature(int l1, int l2, double value){
        featurelist[l1-1][l2-1]=value;
    }
    
    public double GetFeature(int l1, int l2){
        return featurelist[l1-1][l2-1];
    }
}

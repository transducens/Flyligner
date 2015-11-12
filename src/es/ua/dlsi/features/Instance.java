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

import es.ua.dlsi.alignment.ReferenceAlignment;
import es.ua.dlsi.segmentation.Evidence;
import es.ua.dlsi.segmentation.Segment;
import es.ua.dlsi.segmentation.SubSegment;
import es.ua.dlsi.segmentation.TranslationUnit;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class Instance {
    private FeaturesMatrix[][] features;
    
    private Set[] alignment;
    
    private int nunaligned_s1;
    
    private int nunaligned_s2;
    
    private Segment segment1;
    
    private Segment segment2;
    
    public FeaturesMatrix GetFeatures(int word1, int word2){
        if(features[word1][word2]==null)
            return new FeaturesMatrix();
        else
            return features[word1][word2];
    }
    
    public Instance(TranslationUnit tu, Set[] alignment){
        this.alignment=alignment;
        this.segment1=tu.getSource();
        this.segment2=tu.getTarget();
        Set<Integer> unaligned_s1=new LinkedHashSet<Integer>();
        Set<Integer> unaligned_s2=new LinkedHashSet<Integer>();
        for(int n=0;n<segment2.size();n++)
            unaligned_s2.add(n);
        for(int a=0;a<alignment.length;a++){
            if(alignment[a]==null)
                unaligned_s1.add(a);
            Set<ReferenceAlignment> s2_align=alignment[a];
            if(s2_align!=null){
                for(ReferenceAlignment ra: s2_align)
                    unaligned_s2.remove(ra.getCounterpart());
            }
        }
        this.nunaligned_s1=unaligned_s1.size();
        this.nunaligned_s2=unaligned_s2.size();
        features=new FeaturesMatrix[this.segment1.size()][this.segment2.size()];
        CoverCounting[][] coverage=new CoverCounting[this.segment1.size()][this.segment2.size()];
        for(int i=0;i<coverage.length;i++)
            Arrays.fill(coverage[i],null);
        for(Evidence e: tu.getEvidences()){
            SubSegment sseg=e.getSegment();
            SubSegment tseg=e.getTranslation();
            for(int sw=sseg.getPosition();sw<sseg.getPosition()+sseg.getLength();sw++){
                for(int tw=tseg.getPosition();tw<tseg.getPosition()+tseg.getLength();tw++){
                    if(coverage[sw][tw]==null)
                        coverage[sw][tw]=new CoverCounting();
                    coverage[sw][tw].AddCover(sseg.getLength(), tseg.getLength());
                }
            }
        }
        for(int sw=0;sw<coverage.length;sw++){
            for(int tw=0;tw<coverage[sw].length;tw++){
                if(features[sw][tw]==null)
                    features[sw][tw]=new FeaturesMatrix();
                if(coverage[sw][tw]==null)
                    features[sw][tw]=new FeaturesMatrix();
                else
                    features[sw][tw]=coverage[sw][tw].GetFeatures();
            }
        }
    }

    public Set[] getAlignment() {
        return alignment;
    }

    public int CountAlignments() {
        int exit=0;
        for(Set<ReferenceAlignment> res: alignment){
            exit+=res.size();
        }
        return exit;
    }
    
    public boolean isPossibleAlignment(int index, int counterpart){
        Set<ReferenceAlignment> ref=alignment[index];
        if(ref==null)
            return false;
        for(ReferenceAlignment ra: ref){
            if(ra.getCounterpart()==counterpart && !ra.isSure())
                return true;
        }
        return false;
    }
    
    public boolean isSureAlignment(int index, int counterpart){
        Set<ReferenceAlignment> ref=alignment[index];
        if(ref==null)
            return false;
        for(ReferenceAlignment ra: ref){
            if(ra.getCounterpart()==counterpart && ra.isSure())
                return true;
        }
        return false;
    }

    public Segment getSegment1() {
        return segment1;
    }

    public Segment getSegment2() {
        return segment2;
    }

    public int getNunaligned_s1() {
        return nunaligned_s1;
    }

    public int getNunaligned_s2() {
        return nunaligned_s2;
    }
}

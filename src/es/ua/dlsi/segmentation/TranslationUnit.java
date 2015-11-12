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

package es.ua.dlsi.segmentation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that represents a translation unit (TU) in a translation memory. It
 * includes the evidences (sub-segments which appear in the {@link #source}
 * segment and their translation appears in the {@link #target} segment) of the
 * segments to compute the corresponding features.
 * @author Miquel Esplà Gomis
 * @version 0.9
 */
public class TranslationUnit implements Comparable<TranslationUnit>, Serializable{

    /** Source segment in the translation unit */
    protected Segment source;

    /** Target segment in the translation unit */
    protected Segment target;

    /** List of evidences found in the translation unit (sub-segments
     * which appear in the {@link #source} segment and their translation appears
     * in the {@link #target} segment)
     */
    protected Set<Evidence> evidences;

    /**
     * Overloaded constructor from two objects Strig
     * @param source Source segment
     * @param target Target segment
     */
    public TranslationUnit(String source, String target){
        this.source=new Segment(source);
        this.target=new Segment(target);
        this.evidences=new LinkedHashSet<Evidence>();
    }

    /**
     * Overloaded constructor from two objects Sentence
     * @param source Source segment
     * @param target Target segment
     */
    public TranslationUnit(Segment source, Segment target){
        this.source=source;
        this.target=target;
        this.evidences=new LinkedHashSet<Evidence>();
    }

    /**
     * Method which returns the source segment
     * @return Returns the source segment
     */
    public Segment getSource() {
        return source;
    }

    /**
     * Method which sets the source segment
     * @param source The new source segment
     */
    public void setSource(Segment source) {
        this.source = source;
    }

    /**
     * Method which returns the target segment
     * @return Returns the target segment
     */
    public Segment getTarget() {
        return target;
    }

    /**
     * Method which sets the target segment
     * @param target The new target segment
     */
    public void setTarget(Segment target) {
        this.target=target;
    }

    /**
     * Method which sets the target segment
     * @return Returns the list with all the evidences indexed by their source-side
     */
    public Set<Evidence> getEvidences() {
        return this.evidences;
    }

    /**
     * Method that compares two objects to tell if they are equal or not
     * @param o Object with which to compare
     * @return Returns <code>true</code> if both objects are equal and <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o){
        boolean exit;

        if(o.getClass()==TranslationUnit.class){
            TranslationUnit tu=(TranslationUnit)o;
            if(this.source.equals(tu.source) && this.target.equals(tu.target))
                exit=true;
            else
                exit=false;
        }
        else
            exit=false;
        return exit;
    }

    /**
     * Hashing method based on the private variables of the method.
     * @return Returns a hash code based on the private variables of the method
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 61 * hash + (this.target != null ? this.target.hashCode() : 0);
        return hash;
    }

    /**
     * Class that represents a translation unit in a translation memory. It includes
     * the evidences of the segments to compute the corresponding features.
     * @param tu TU with which this TU will be compared
     */
    @Override
    public int compareTo(TranslationUnit tu) {
        if(this.source.size()==tu.source.size())
            return 0;
        else{
            if(this.source.size()<tu.source.size())
                return 1;
            else
                return -1;
        }
    }

    /**
     * Method that uses a SegmentDictionary object to detect evidences (sub-segments
     * which appear in the {@link #source} segment and their translation appears
     * in the {@link #target} segment)
     * @param sd Segment dictionary with a set of sub-segments in source language and their translation
     * @param maxlen Maximum length of sub-segments
     * @param debug Flag that indicates if debug messages should be shown or not
     * @return Returns the number of evidences found in the TU
     */
    public int CollectHTMLEvidences(SegmentDictionary sd, int maxlen, boolean debug){
        List<SubSegment> segmenteds=this.source.AllSubSegmentsInSentence(maxlen);
        List<SubSegment> segmentedt=this.target.AllSubSegmentsInSentence(maxlen);
        if(debug)
            System.out.print("\tEvidences: ");
        for(SubSegment ss: segmenteds){
            Set<Segment> trans=sd.GetTargetSegment(new Segment(ss.getSentence()));
            if(trans!=null){
                for(Segment strans: trans){
                    Set<Integer> pos=strans.Appears(this.getTarget());
                    if(pos!=null){
                        for(int p: pos){
                            if(debug)
                                System.out.print("["+ss+","+strans+"], ");
                            evidences.add(new Evidence(ss, new SubSegment(strans.getSentence(), p, strans.size())));
                        }
                    }
                }
            }
        }
        if(debug)
            System.out.println();
        for(SubSegment st: segmentedt){
            Set<Segment> trans=sd.GetSourceSegment(new Segment(st.getSentence()));
            if(trans!=null){
                for(Segment strans: trans){
                    Set<Integer> pos=strans.Appears(this.getSource());
                    if(pos!=null){
                        for(int p: pos){
                            if(debug)
                                System.out.print("["+strans+","+st+"], ");
                            evidences.add(new Evidence(new SubSegment(strans.getSentence(), p, strans.size()),st));
                        }
                    }
                }
            }
        }
        return evidences.size();
    }

    /**
     * Method that uses a SegmentDictionary object to detect evidences (sub-segments
     * which appear in the {@link #source} segment and their translation appears
     * in the {@link #target} segment)
     * @param sd Segment dictionary with a set of sub-segments in source language and their translation
     * @param maxlen Maximum length of sub-segments
     * @param debug Flag that indicates if debug messages should be shown or not
     * @return Returns the number of evidences found in the TU
     */
    public int CollectEvidences(SegmentDictionary sd, int maxlen, boolean debug){
        List<SubSegment> segmenteds=this.source.AllSubSegmentsInSentence(maxlen);
        List<SubSegment> segmentedt=this.target.AllSubSegmentsInSentence(maxlen);
        if(debug)
            System.out.print("\tEvidences: ");
        for(SubSegment ss: segmenteds){
            //System.out.println("Segment: "+ss);
            Set<Segment> trans=sd.GetTargetSegment(new Segment(ss.getSentence()));
            if(trans!=null){
                for(Segment strans: trans){
                    //System.out.println("\tTrans: "+strans);
                    //for(SubSegment st: segmentedt){
                        /*if(strans.equals(new Segment(st.getSentence()))){
                            //System.out.println("\t\tAdded: "+strans);
                            if(debug)
                                System.out.print("["+ss+","+st+"], ");
                            evidences.add(new Evidence(ss, st));
                        }*/
                        Set<Integer> pos=strans.Appears(this.getTarget());
                        if(pos!=null){
                            for(int p: pos){
                                if(debug)
                                    System.out.print("["+ss+","+strans+"], ");
                                evidences.add(new Evidence(ss, new SubSegment(strans.getSentence(), p, strans.size())));
                            }
                        }
                    //}
                }
            }
        }
        if(debug)
            System.out.println();
        for(SubSegment st: segmentedt){
            //System.out.println("Segment: "+st);
            Set<Segment> trans=sd.GetSourceSegment(new Segment(st.getSentence()));
            if(trans!=null){
                for(Segment strans: trans){
                    //System.out.println("\tTrans: "+strans);
                    /*for(SubSegment ss: segmenteds){
                        if(strans.equals(new Segment(ss.getSentence()))){
                            //System.out.println("\t\tAdded: "+strans);
                            evidences.add(new Evidence(ss, st));
                        }
                    }*/
                    Set<Integer> pos=strans.Appears(this.getSource());
                    if(pos!=null){
                        for(int p: pos){
                            if(debug)
                                System.out.print("["+strans+","+st+"], ");
                            evidences.add(new Evidence(new SubSegment(strans.getSentence(), p, strans.size()),st));
                        }
                    }
                }
            }
        }
        return evidences.size();
    }

    public int CombineEvidence(PrintWriter debug){
        Set<Evidence> newev=new LinkedHashSet<Evidence>();
        for(Evidence e1: this.evidences){
            for(Evidence e2: this.evidences){
                if(e1!=e2){
                    SubSegment soverlap=e1.getSegment().OverlappingCombination(e2.getSegment());
                    SubSegment toverlap=e1.getTranslation().OverlappingCombination(e2.getTranslation());
                    if(soverlap!=null && toverlap!=null){
                        if(debug!=null){
                            debug.println(e1.getSegment()+"+"+e2.getSegment()+" | "
                                    +e1.getTranslation()+"+"+e2.getTranslation()+" = "
                                    +soverlap+"|"+toverlap);
                        }
                        newev.add(new Evidence(soverlap, toverlap));
                    }
                }
            }
            if(debug!=null)
                debug.println();
        }
        this.evidences.addAll(newev);
        return newev.size();
    }
    
    /**
     * Method that prints a list of alignments, using the following format:
     * &lt;s_starting_position;s_length,...|t_position,t_length,...&gt;&lt;s_starting_position;s_length,...|t_position,t_length,...&gt...
     */
    public void PrintAlignments(OutputStream os){
        PrintWriter pw=new PrintWriter(os);
        for(Evidence e: this.evidences){
            SubSegment sss=e.getSegment();
            SubSegment tss=e.getSegment();
            pw.print(sss.getPosition());
            pw.print(";");
            pw.print(sss.getLength());
            pw.print("|");
            pw.print(tss.getPosition());
            pw.print(";");
            pw.print(tss.getLength());
        }
        pw.println();
    }
}

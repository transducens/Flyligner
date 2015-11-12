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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;

/**
 * This class represents a dictionary of sub-segments and their possible translations.
 * The sub-segments and their translations are loaded and saved in a efficient
 * way in order to allow the construction of the evidences in the TM objects with
 * the maximum speed possible.
 * @author Miquel Esplà Gomis
 * @version 0.9
 */
public class SegmentDictionary implements Serializable{

    /*static */private int max_len;

    /** Map of equivalent segments indexed by forain lang segments*/
    Map<Segment,Set<Segment>> sdic;

    /** Map of equivalent segments indexed by English segments*/
    Map<Segment,Set<Segment>> tdic;

    /**
     * Class default constructor
     */
    public SegmentDictionary(){
        sdic=new LinkedHashMap<Segment, Set<Segment>>();
        tdic=new LinkedHashMap<Segment, Set<Segment>>();
    }

    /**
     * Method that returns the size of the dictionary of Spanish segments
     * @return Returns the size of the dictionary
     */
    public int sSize(){
        return sdic.size();
    }

    /**
     * Method that returns the size of the dictionary
     * @return Returns the size of the dictionary
     */
    public int tSize(){
        return tdic.size();
    }

    /**
     * Method which returns the translation of a segment
     * @param segment Segment for which the trnalsation must be returned
     * @return Returns the translation of a given segment
     */
    public Set<Segment> GetTargetSegment(Segment segment){
        return sdic.get(segment);
    }

    /**
     * Method which returns the translation of a segment
     * @param segment Segment for which the trnalsation must be returned
     * @return Returns the translation of a given segment
     */
    public Set<Segment> GetSourceSegment(Segment segment){
        return tdic.get(segment);
    }

    /**
     * Method that sets the maximum length of subsegments
     * @param value
     */
    public void SetMaxLen(int value){
        max_len=value;
    }

    /**
     * Method that returns the maximum length of subsegments
     * @return Returns the maximum length of subsegments
     */
    public int GetMaxLen(){
        return max_len;
    }

    /**
     * Method that adds a new pair of sub-segments in the list of pairs of sub-segments
     * @param s Source sub-segment
     * @param t Target sub-segment
     */
    public void AddSegmentPair(Segment s, Segment t){
        if(sdic.containsKey(s))
            sdic.get(s).add(t);
        else{
            Set<Segment> tmpset=new LinkedHashSet<Segment>();
            tmpset.add(t);
            sdic.put(s, tmpset);
        }

        if(tdic.containsKey(t))
            tdic.get(t).add(s);
        else{
            Set<Segment> tmpset=new LinkedHashSet<Segment>();
            tmpset.add(s);
            tdic.put(t, tmpset);
        }
    }

    public void LoadHTMLSegments(String sfilename, String trans_sfilename, boolean debug){
        BufferedReader input_source, input_target;
        String sline, tline;
        try {
            input_source = new BufferedReader(new FileReader(sfilename));
            input_target = new BufferedReader(new FileReader(trans_sfilename));
            sline = input_source.readLine();
            tline = input_target.readLine();
            while(sline!=null && tline!=null){
                if(sline.matches("<p>.*</p>") && tline.matches("<p>.*</p>")){
                    Segment ssegment=new Segment(sline.substring(3, sline.length()-4));
                    Segment tsegment=new Segment(tline.substring(3, tline.length()-4));
                    if(debug){
                        System.out.print("Adding '");
                        System.out.print(ssegment.toString());
                        System.out.print("' '");
                        System.out.print(tsegment.toString());
                        System.out.println("'");
                    }
                    AddSegmentPair(ssegment, tsegment);
                }
                sline = input_source.readLine();
                tline = input_target.readLine();
            }
            input_source.close();
            input_target.close();
            if((sline==null && tline!=null) || (sline!=null && tline==null)){
                System.out.println(sline+"\n"+tline);
                System.out.println();
                System.err.println("Error: different number of lines between the files "+sfilename+" and "+trans_sfilename+".");
                System.exit(-1);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.err.println("Error while reading source translations file.");
        }
    }

    /**
     * Method which loads the segment dictionary from a parallel document
     * @param sfilename Path of the file with the original segments
     * @param trans_sfilename Path of the file with the translation of the segments
     */
    public void LoadSegments(String sfilename, String trans_sfilename, boolean debug){
        BufferedReader input_source, input_target;
        String sline, tline;
        try {
            input_source = new BufferedReader(new FileReader(sfilename));
            input_target = new BufferedReader(new FileReader(trans_sfilename));
            sline = input_source.readLine();
            tline = input_target.readLine();
            while(sline!=null && tline!=null){
                Segment ssegment=new Segment(sline/*.substring(0, sline.length())*/);
                Segment tsegment=new Segment(tline/*.substring(0, tline.length())*/);
                if(debug){
                    System.out.print("Adding '");
                    System.out.print(ssegment.toString());
                    System.out.print("' '");
                    System.out.print(tsegment.toString());
                    System.out.println("'");
                }
                AddSegmentPair(ssegment, tsegment);
                sline = input_source.readLine();
                tline = input_target.readLine();
            }
            input_source.close();
            input_target.close();
            if((sline==null && tline!=null) || (sline!=null && tline==null)){
                System.out.println(sline+"\n"+tline);
                System.out.println();
                System.err.println("Error: different number of lines between the files "+sfilename+" and "+trans_sfilename+".");
                System.exit(-1);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.err.println("Error while reading source translations file.");
        }
    }

    /**
     * Method which loads the segment dictionary from a parallel document
     * @param sfilename Path of the file with the original segments
     * @param trans_sfilename Path of the file with the translation of the segments
     */
    public void LoadDotEndingSegments(String sfilename, String trans_sfilename, boolean debug){
        BufferedReader input_source, input_target;
        String sline, tline;
        try {
            input_source = new BufferedReader(new FileReader(sfilename));
            input_target = new BufferedReader(new FileReader(trans_sfilename));
            sline = input_source.readLine();
            tline = input_target.readLine();
            while(sline!=null && tline!=null){
                if(sline.length()>=2 && tline.length()>=2){
                    Segment ssegment=new Segment(sline.substring(0, sline.length()-2));
                    Segment tsegment=new Segment(tline.substring(0, tline.length()-2));
                    if(debug){
                        System.out.print("Adding '");
                        System.out.print(ssegment.toString());
                        System.out.print("' '");
                        System.out.print(tsegment.toString());
                        System.out.println("'");
                    }
                    AddSegmentPair(ssegment, tsegment);
                }
                sline = input_source.readLine();
                tline = input_target.readLine();
            }
            input_source.close();
            input_target.close();
            if((sline==null && tline!=null) || (sline!=null && tline==null)){
                System.out.println(sline+"\n"+tline);
                System.out.println();
                System.err.println("Error: different number of lines between the files "+sfilename+" and "+trans_sfilename+".");
                System.exit(-1);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.err.println("Error while reading source translations file.");
        }
    }

    /**
     * Method that prints the dictionary in a human-readable format
     */
    public void PrintDictionary(){
        for(Segment s: sdic.keySet()){
            for(Segment t: sdic.get(s)){
                System.out.print(s.toString());
                System.out.print(" - ");
                System.out.println(t.toString());
            }
        }
    }
}

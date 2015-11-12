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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents an index in a segment, it is the position of the sub-segment
 * in the segment and its length.
 * @author Miquel Esplà Gomis
 * @version 0.9
 */
public class SubSegment extends Segment implements Serializable{
    /** Starting position of the sub-segment in the segment */
    private int position;

    /** Length of the sub-segment */
    private int length;
    
    /**
     * Method that determines if a sub-segment contains a word in a given position
     * of a segments.
     * @param wposition Position of the word in the segment
     * @param initialpos Initial position of the sub-segment in the segment
     * @param length Length of the sub-segment
     * @return Returns <code>true</code> if the word is contained by the sub-segment and <code>false</code> otherwise.
     */
    public boolean Cover(int wposition){
        if(position<=wposition && position+length>wposition)
            return true;
        else
            return false;
    }
    
    /**
     * Overloaded constructor
     * @param position New position to be set
     * @param length New length to be set
     */
    public SubSegment(String segment, int position, int length) {
        super(segment);
        this.position = position;
        this.length = length;
    }

    /**
     * Overloaded constructor
     * @param position New position to be set
     * @param length New length to be set
     */
    public SubSegment(List<Word> segment, int position, int length) {
        super(segment);
        this.position = position;
        this.length = length;
    }

    /**
     * Method that returns the length of the sub-segment
     * @return Returns the length of the sub-segment
     */
    public int getLength() {
        return length;
    }

    /**
     * Method that sets a new value for the length of the sub-segment
     * @param length New value to be set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Method that returns the starting position of the sub-segment in the segment
     * @return Returns the starting position of the sub-segment in the segment
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method that sets a new value for the starting position of the sub-segment
     * @param position Returns the starting position of the sub-segment in the segment
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Method that compares two objects to tell if they are equal or not
     * @param o Object with which to compare
     * @return Returns <code>true</code> if both objects are equal and <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o){
        boolean exit;

        if(o.getClass()==SubSegment.class){
            SubSegment s=(SubSegment)o;
            if(length==s.length && position==s.position){
                if(sentence.equals(s.sentence))
                    exit=true;
                else
                    exit=false;
            }
            else
                exit=false;
        }
        else
            exit=false;
        return exit;
    }

    /**
     * Method that computes a hash code from the values of the variables in the class.
     * @return Returns a hash code from the values of the variables in the class.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + length*position;
        return hash;
    }

    /**
     * Method that returns a new SubSegment combining an overlapping sub-segment.
     * If there is no overlapping between sub-segments, the method returns null.
     * @param ss The sub-segment to combine with the current sub-segment.
     * @return Returns the combined sub-segment if exists overlapping between the current
     * sub-segment and ss, and null if there is no overlapping.
     */
    public SubSegment OverlappingCombination(SubSegment ss) {
        SubSegment exit=null;
        if(position<ss.position && ss.position<position+length && position+length<ss.position+ss.length){
            int p=position;
            int l=(ss.position+ss.length)-p;
            List<Word> words=new LinkedList<Word>(sentence);
            words.addAll(ss.sentence.subList((position+length)-ss.position, ss.sentence.size()));
            exit=new SubSegment(words, p, l);
        }
        else if(ss.position<position && position<ss.position+ss.length && ss.position+ss.length<position+length){
            int p=ss.position;
            int l=(position+length)-p;
            List<Word> words=new LinkedList<Word>(ss.sentence);
            words.addAll(sentence.subList((ss.position+ss.length)-position, sentence.size()));
            exit=new SubSegment(words, p, l);
        }
        return exit;
    }
}

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
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

/**
 * This class contains the information about a Word. A Word is a String
 * which is asociated with a numeric code representing the token which may be
 * used for comparing words. The correspondence between words and their codes
 * is contained in a static hash map.
 * @author Miquel Esplà Gomis
 */
public class Word implements Serializable{
    /** Static map containing the correspondence between the tokens and their codes */
    static protected BidiMap word_map=new TreeBidiMap();

    /** The value of the word */
    protected String word;

    /** Word of the code */
    protected int code;
    
    protected String pos_cathegory;

    static public String GetWordFromCode(Integer code){
        return (String)word_map.getKey(code);
    }

    /**
     * Default constructor of a word
     */
    protected Word(){
        
    }

    /**
     * Default constructor of a word, which assigns a given value to the word.
     * @param s Value of the word.
     */
    public Word(String s){
        word=s;
        if(word_map.containsKey(s))
            code=(Integer)word_map.get(s);
        else{
            code=word_map.size();
            word_map.put(s, word_map.size());
        }
    }

    /**
     * Method that refresh the code asigned to the word when it is loaded as an
     * object from a file.
     * @param s New value for the word.
     */
    public void RefreshCode(){
        if(word_map.containsKey(this.word))
            code=(Integer)word_map.get(this.word);
        else{
            code=word_map.size();
            word_map.put(this.word, word_map.size());
        }
    }

    /**
     * Method that modifyis the word (both the code and the value)
     * @param s New value for the word.
     */
    public void Modify(String s){
        word=s;
        if(word_map.containsKey(s))
            code=(Integer)word_map.get(s);
        else{
            code=word_map.size();
            word_map.put(s, word_map.size());
        }
    }

    /**
     * Method that returns the value of the word as a String.
     * @return The value of the word as a String.
     */
    public String getValue(){
        return word;
    }

    /**
     * Method that returns the code of the word as an Integer.
     * @return The code of the word as an Integer.
     */
    public int getCode(){
        return code;
    }

    /**
     * Method that compares the segment with another object.
     * @param o The object with which the segment will be compared
     * @return Returns <code>true</code> if the objects are equal and <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o){
        boolean exit;

        if(o.getClass()!=Word.class)
            exit=false;
        else{
            Word w=(Word)o;
            exit=(w.getCode()==getCode());
        }
        return exit;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.word != null ? this.word.hashCode() : 0);
        hash = 79 * hash + this.code;
        return hash;
    }

    public String getPos_cathegory() {
        return pos_cathegory;
    }

    public void setPos_cathegory(String pos_cathegory) {
        this.pos_cathegory = pos_cathegory;
    }
}

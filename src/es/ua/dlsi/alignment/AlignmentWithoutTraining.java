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

import es.ua.dlsi.segmentation.Evidence;
import es.ua.dlsi.segmentation.SegmentDictionary;
import es.ua.dlsi.segmentation.TranslationUnit;
import es.ua.dlsi.utils.CmdLineParser;
import es.ua.dlsi.utils.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class AlignmentWithoutTraining {
    
    /**
     * Symetrisation method which produces, as a result, the intersection of the
     * S2T and T2S alignments.
     * @param instance
     * @param s2t
     * @param t2s
     * @return 
     */
    static public boolean[][] IntersectionSymal(Set<Integer>[] s2t, Set<Integer>[] t2s){
        boolean[][] intersection=new boolean[s2t.length][t2s.length];
        //Initialisation of the alignment matrix to "all the words are unaligned"
        for(boolean[] row: intersection)
            Arrays.fill(row, false);
        //For all the alignments in S2T
        for(int s_word=0;s_word<s2t.length;s_word++){
            if(s2t[s_word]!=null){
                for(Integer t_word: s2t[s_word]){
                    if(t2s[t_word]!=null){
                        //If the alignment appears in both the asymetric alignments, it is added to the symetrised alignment
                        if(t2s[t_word].contains(s_word))
                            intersection[s_word][t_word]=true;
                    }
                }
            }
        }
        return intersection;
    }
    
    /**
     * Symetrisation method which produces, as a result, the union of the
     * @param instance
     * @param s2t
     * @param t2s
     * @return 
     */
    static public boolean[][] UnionSymal(Set<Integer>[] s2t, Set<Integer>[] t2s){
        boolean[][] union=new boolean[s2t.length][t2s.length];
        //Initialisation of the alignment matrix to "all the words are unaligned"
        for(boolean[] row: union)
            Arrays.fill(row, false);
        //All the alignments in S2T are added to the union
        for(int s_word=0;s_word<s2t.length;s_word++){
            if(s2t[s_word]!=null){
                for(Integer t_word: s2t[s_word]){
                    union[s_word][t_word]=true;
                }
            }
        }
        //All the alignments in T2S are added to the union
        for(int t_word=0;t_word<t2s.length;t_word++){
            if(t2s[t_word]!=null){
                for(Integer s_word: t2s[t_word]){
                    union[s_word][t_word]=true;
                }
            }
        }
        return union;
    }
    
    static public boolean[][] GrowDiagFinalAnd(Set<Integer>[] s2t, Set<Integer>[] t2s){
        List<Pair<Integer,Integer>> neighbors=new LinkedList<Pair<Integer, Integer>>(); //neighbors

        //Defining neibourhood
        neighbors.add(new Pair(0,1));
        neighbors.add(new Pair(-1,-0));
        neighbors.add(new Pair(0,-1));
        neighbors.add(new Pair(1,0));

        //Diagonal (diag) neigourhood
        neighbors.add(new Pair(-1,-1));
        neighbors.add(new Pair(-1,1));
        neighbors.add(new Pair(1,-1));
        neighbors.add(new Pair(1,1));
        
        //Intersection of the alignments (starting point)
        boolean[][] currentpoints=IntersectionSymal(s2t, t2s); //symmetric alignment
        //Union of the alignments (space for growing)
        boolean[][] unionalignment=UnionSymal(s2t, t2s); //union alignment
        //Adding currently unaligned words in SL to the list
        Set<Integer> unaligned_s=new LinkedHashSet<Integer>();
        for(int current_row=0;current_row<currentpoints.length;current_row++){
            boolean aligned=false;
            for(int current_col=0;current_col<currentpoints[current_row].length;current_col++){
                if(currentpoints[current_row][current_col]){
                    aligned=true;
                    break;
                }
            }
            if(!aligned)
                unaligned_s.add(current_row);
        }
        //Adding currently unaligned words in TL to the list
        Set<Integer> unaligned_t=new LinkedHashSet<Integer>();
        for(int current_col=0;current_col<currentpoints[0].length;current_col++){
            boolean aligned=false;
            for(int current_row=0;current_row<currentpoints.length;current_row++){
                if(currentpoints[current_row][current_col]){
                    aligned=true;
                    break;
                }
            }
            if(!aligned)
                unaligned_t.add(current_col);
        }

        boolean added;

        //Grow-diag
        do{
            added=false;
            //For all the current alignment
            for(int current_row=0;current_row<currentpoints.length;current_row++){
                for(int current_col=0;current_col<currentpoints[current_row].length;current_col++){
                    //If the word is aligned, the neibourghs are checked
                    if(currentpoints[current_row][current_col]){
                        for(Pair<Integer,Integer> n: neighbors){
                            int p1=current_row+n.getFirst();
                            int p2=current_col+n.getSecond();
                            if(p1>=0 && p1<currentpoints.length && p2>=0 && p2<currentpoints[0].length){
                                //Check the neighbours
                                if((unaligned_s.contains(p1) || unaligned_t.contains(p2)) &&
                                        unionalignment[p1][p2]){
                                    currentpoints[p1][p2]=true;
                                    unaligned_s.remove(p1);
                                    unaligned_t.remove(p2);
                                    added=true;
                                }
                            }
                        }
                    }
                }
            }
        }while (added);
            
        //Final-and
        for(int sw: unaligned_s){
            int t_toremove=-1;
            for(int tw: unaligned_t){
                if(unionalignment[sw][tw]){
                    t_toremove=tw;
                    currentpoints[sw][tw]=true;
                    break;
                }
            }
            unaligned_t.remove(t_toremove);
        }
        
        return currentpoints;
    }

    /**
     * @param args the command line arguments
     */
    /*public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option othreshold = parser.addStringOption("threshold");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option odebug = parser.addStringOption('d',"debug");
        CmdLineParser.Option otmpath = parser.addStringOption("tm-path");
        CmdLineParser.Option otmsource = parser.addStringOption('s',"source");
        CmdLineParser.Option otmtarget = parser.addStringOption('t',"target");
        CmdLineParser.Option osegsource = parser.addStringOption("seg-source");
        CmdLineParser.Option osegtarget = parser.addStringOption("seg-target");
        CmdLineParser.Option ohtmlsubsegs = parser.addBooleanOption("html");
        CmdLineParser.Option oreverse = parser.addBooleanOption("reverse");
        CmdLineParser.Option omaxseglen = parser.addIntegerOption('m',"max-segment-len");

        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        String output=(String)parser.getOptionValue(ooutput,null);
        String tmpath=(String)parser.getOptionValue(otmpath,null);
        String debugpath=(String)parser.getOptionValue(odebug,null);
        String tmsource = (String)parser.getOptionValue(otmsource,null);
        String tmtarget = (String)parser.getOptionValue(otmtarget,null);
        String segsource = (String)parser.getOptionValue(osegsource,null);
        String segtarget = (String)parser.getOptionValue(osegtarget,null);
        int maxseglen=(Integer)parser.getOptionValue(omaxseglen,-1);
        Boolean htmlsubsegs=(Boolean)parser.getOptionValue(ohtmlsubsegs,false);
        Boolean reverse=(Boolean)parser.getOptionValue(oreverse,false);
        
        BufferedReader sreader=null, treader=null;
        try{
            sreader=new BufferedReader(new FileReader(sourcesegsfile));
        }catch(FileNotFoundException ex){
            System.err.println("Error: It was impossible to find file '"+sourcesegsfile+"'. Please check the path.");
            System.exit(-1);
        }
        try{
            treader=new BufferedReader(new FileReader(targetsegsfile));
        }catch(FileNotFoundException ex){
            System.err.println("Error: It was impossible to find file '"+targetsegsfile+"'. Please check the path.");
            System.exit(-1);
        }

        boolean debug=(debugpath!=null);

        if(tmpath==null){
            if(tmsource==null){
                System.err.println("Error: It is necessary to define the file containing the source language segments of the translation memory (use parameter --tm-source).");
                System.exit(-1);
            }
            if(tmtarget==null){
                System.err.println("Error: It is necessary to define the file containing the target language segments of the translation memory (use parameter --tm-target).");
                System.exit(-1);
            }
            if(segsource==null){
                System.err.println("Error: It is necessary to define the file containing the source language sub-segments translation (use parameter --seg-source).");
                System.exit(-1);
            }
            if(segtarget==null){
                System.err.println("Error: It is necessary to define the file containing the target language sub-segments translation (use parameter --seg-target).");
                System.exit(-1);
            }

            if(segsource!=null && segtarget!=null){
                //Loading sub-segment dictionary
                SegmentDictionary sdic = new SegmentDictionary();
                if(htmlsubsegs) {
                    sdic.LoadHTMLSegments(segsource, segtarget, debug);
                }
                else {
                    sdic.LoadSegments(segsource, segtarget, debug);
                }
                trans_memory.GenerateEvidences(sdic, maxseglen, debug);
            }
        }
        else{
            trans_memory.LoadTMFromObject(tmpath);
        }

        PrintWriter pw;
        try{
            pw=new PrintWriter(output);
        } catch(FileNotFoundException ex){
            System.err.println("Warning: Output file "+output+" could not be found: the results will be printed in the default output.");
            pw=new PrintWriter(System.out);
        } catch(NullPointerException ex){
            System.err.println("Warning: No output file: the results will be printed in the default output.");
            pw=new PrintWriter(System.out);
        }

        int counter=0;
        Segment s;
        Segment t;
        for(TranslationUnit tu: trans_memory.GetTUs()){
            counter++;
            Set<Integer>[] alignment;
            if(reverse){
                alignment=GeometricAligner.AlignT2SBestAddAllTied(tu, maxseglen);
                s=tu.getTarget();
                t=tu.getSource();
            }
            else{
                alignment=GeometricAligner.AlignS2TBestAddAllTied(tu, maxseglen);
                s=tu.getSource();
                t=tu.getTarget();
            }
            pw.println("# Sentence pair ("+counter+") source length "+s.size()+" target length "+t.size()+" alignment");
            pw.println(s.toString());
            Set<Integer> nullaligned=new LinkedHashSet<Integer>();
            Set<Integer>[] wordlist=new Set[t.size()];
            for(int i=0;i<alignment.length;i++){
                if(alignment[i]==null) {
                    nullaligned.add(i+1);
                }
                else{
                    for(int n: alignment[i]){
                        if(wordlist[n]==null) {
                            wordlist[n]=new LinkedHashSet<Integer>();
                        }
                        wordlist[n].add(i+1);
                    }
                }
            }
            pw.print("NULL ({");
            for(int n: nullaligned){
                pw.print(" ");
                pw.print(n);
            }
            pw.print(" })");
            for(int i=0;i<t.size();i++){
                pw.print(" ");
                pw.print(t.getSentence().get(i).getValue());
                pw.print(" ({");
                if(wordlist[i]!=null){
                    for(int n: wordlist[i]){
                        pw.print(" ");
                        pw.print(n);
                    }
                }
                pw.print(" })");
            }
            pw.println();
            pw.flush();
        }
        pw.close();
    }*/
    
    
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option osourcesegsfile = parser.addStringOption('s',"source-segments");
        CmdLineParser.Option otargetsegsfile = parser.addStringOption('t',"target-segments");
        CmdLineParser.Option osourcetransfile = parser.addStringOption("source-translations");
        CmdLineParser.Option otargettransfile = parser.addStringOption("target-translations");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option odebug = parser.addStringOption('d',"debug");
        CmdLineParser.Option osym = parser.addStringOption("symmetrisation");
        CmdLineParser.Option omaxseglen = parser.addIntegerOption('m',"max-segment-len");
        CmdLineParser.Option ohelp = parser.addBooleanOption('h',"help");

        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        String sourcesegsfile=(String)parser.getOptionValue(osourcesegsfile,null);
        String targetsegsfile=(String)parser.getOptionValue(otargetsegsfile,null);
        String sourcetransfile=(String)parser.getOptionValue(osourcetransfile,null);
        String targettransfile=(String)parser.getOptionValue(otargettransfile,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String debugpath=(String)parser.getOptionValue(odebug,null);
        String sym=(String)parser.getOptionValue(osym,"gdfa");
        int maxseglen=(Integer)parser.getOptionValue(omaxseglen,-1);
        boolean debug=(debugpath!=null);
        boolean help=(Boolean)parser.getOptionValue(ohelp,false);
        
        if(help){
            System.err.println("This class must be called:\n");
            System.err.println("java -cp es.ua.dlsi.alignment.AlignmentWithoutTraining"+
                    "-s <source_language_sentences> -t <source_language_sentences>"+
                    "--source-translations <source_language_sub-segment_dictionary>"+
                    "--target-translations <target_language_sub-segment_dictionary>"+
                    "-m <sub-segmetn_maximum_length> [-o <output_path>] [--symmetrisation <symmetrisation_method>]\n" );
            System.err.print("\t*source_language_sentences and target_language_sentences: ");
            System.err.println("the list of sentences to be aligned; these files must contain a sentence per line.");
            System.err.print("\t*source_language_sub-segment_dictionary and target_language_sub-segment_dictionary: ");
            System.err.println("the list of aligned pairs of sub-segments; these files must contain a sub-segment per line.");
            System.err.println("\t*sub-segmetn_maximum_length: maximum length of "+
                    "the sub-segments used as a source of bilingual information");
            System.err.println("\t*output_path: path to the file containing the output. The output will"+
                    " be produced in the same format than GIZA++ symmetrised alignments.");
            System.err.println("\t*symmetrisation_method: the heuristic for symmetrising the alignments."+
                    " One of the following values must be chosen: 'union', 'intersection', or 'gdfa'"+
                    " (grow-diag-final-and, which is the default value");
        }
        else{
            if(maxseglen<0){
                System.err.println("Error: the maximum value of the segments length must be set to a number higher than 0 (use option -m or --max-segment-len)");
                System.exit(-1);
            }

            if(sourcesegsfile==null){
                System.err.println("Error: It is necessary to define a source language segments file (use option '-s').");
                System.exit(-1);
            }
            if(targetsegsfile==null){
                System.err.println("Error: It is necessary to define a target language segments file (use option '-t').");
                System.exit(-1);
            }
            if(sourcetransfile==null){
                System.err.println("Error: It is necessary to define the file containing the source language sub-segments (use option '--source-translations').");
                System.exit(-1);
            }
            if(targettransfile==null){
                System.err.println("Error: It is necessary to define the file containing the target language sub-segments (use option '--target-translations').");
                System.exit(-1);
            }

            PrintWriter pw;
            try{
                pw=new PrintWriter(output);
            }catch (NullPointerException ex){
                System.err.println("Error while trying to open output file '"+output+"'. Output redirected to standard output");
                pw=new PrintWriter(System.out);
            }catch (IOException ex){
                System.err.println("Error while trying to open output file '"+output+"'. Output redirected to standard output");
                pw=new PrintWriter(System.out);
            }

            PrintWriter dbgpw=null;
            if(debug){
                try{
                    dbgpw=new PrintWriter(debugpath);
                }catch (NullPointerException ex){
                    System.err.println("Error while trying to open output file '"+debugpath+"'. Output redirected to standard output");
                    dbgpw=new PrintWriter(System.out);
                }catch (IOException ex){
                    System.err.println("Error while trying to open output file '"+debugpath+"'. Output redirected to standard output");
                    dbgpw=new PrintWriter(System.out);
                }
            }

            SegmentDictionary sd;
            File sf=new File(sourcetransfile);
            File tf=new File(targettransfile);
            System.err.println("Loading dictionary from text files.");
            sd=new SegmentDictionary();
            sd.LoadSegments(sourcetransfile, targettransfile, false);
            System.err.println("Dictionary loaded successfully.");

            BufferedReader sreader=null, treader=null;
            try{
                sreader=new BufferedReader(new FileReader(sourcesegsfile));
            }catch(FileNotFoundException ex){
                System.err.println("Error: It was impossible to find file '"+sourcesegsfile+"'. Please check the path.");
                System.exit(-1);
            }
            try{
                treader=new BufferedReader(new FileReader(targetsegsfile));
            }catch(FileNotFoundException ex){
                System.err.println("Error: It was impossible to find file '"+targetsegsfile+"'. Please check the path.");
                System.exit(-1);
            }

            String sline, tline;
            Evidence.max_seg_len=maxseglen;
            try{
                while((sline=sreader.readLine())!=null && (tline=treader.readLine())!=null){
                    TranslationUnit tu=new TranslationUnit(sline, tline);

                    //Array.newInstance(Class<LinkedHashSet<Integer>>, 1);

                    tu.CollectEvidences(sd, Evidence.max_seg_len, false);
                    Set<Integer>[] alignment_t2s=GeometricAligner.AlignT2SBestAddAllTied(tu, maxseglen);
                    Set<Integer>[] alignment_s2t=GeometricAligner.AlignS2TBestAddAllTied(tu, maxseglen);

                    boolean[][] al=null;
                    if(sym.equals("union")){
                        al=UnionSymal(alignment_s2t,alignment_t2s);
                    }else if(sym.equals("intersection")){
                        al=IntersectionSymal(alignment_s2t,alignment_t2s);
                    }else if(sym.equals("gdfa")){
                        al=GrowDiagFinalAnd(alignment_s2t,alignment_t2s);
                    }
                    else{
                        System.err.println("Wrong alignment method "+sym);
                        System.exit(-1);
                    }
                    for(int row=0;row<al.length;row++){
                        for(int col=0;col<al[row].length;col++){
                            if(al[row][col]){
                                pw.print(row);
                                pw.print("-");
                                pw.print(col);
                                pw.print(" ");
                            }
                        }
                    }
                    pw.println();
                }
                sreader.close();
                if(treader!=null){
                    treader.close();
                }
            }catch(IOException ex){
                System.err.println("Error while trying to read source/target segment files");
                System.exit(-1);
            }
            pw.close();
            if(dbgpw!=null){
                dbgpw.close();
            }
        }
    }
}

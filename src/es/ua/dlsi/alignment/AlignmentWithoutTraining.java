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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class AlignmentWithoutTraining {
     
    
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
                        al=Symetrisation.UnionSymal(alignment_s2t,alignment_t2s);
                    }else if(sym.equals("intersection")){
                        al=Symetrisation.IntersectionSymal(alignment_s2t,alignment_t2s);
                    }else if(sym.equals("gdfa")){
                        al=Symetrisation.GrowDiagFinalAnd(alignment_s2t,alignment_t2s);
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

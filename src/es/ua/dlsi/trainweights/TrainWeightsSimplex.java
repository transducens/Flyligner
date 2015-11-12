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

package es.ua.dlsi.trainweights;

import es.ua.dlsi.alignment.ReferenceAlignment;
import es.ua.dlsi.features.FeaturesMatrix;
import es.ua.dlsi.features.Instance;
import es.ua.dlsi.segmentation.Evidence;
import es.ua.dlsi.segmentation.SegmentDictionary;
import es.ua.dlsi.segmentation.TranslationUnit;
import es.ua.dlsi.simplex.Simplex;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.BufferedReader;
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
public class TrainWeightsSimplex {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option osourcesegsfile = parser.addStringOption('s',"source-segments");
        CmdLineParser.Option otargetsegsfile = parser.addStringOption('t',"target-segments");
        CmdLineParser.Option osourcetransfile = parser.addStringOption("source-translations");
        CmdLineParser.Option otargettransfile = parser.addStringOption("target-translations");
        CmdLineParser.Option oalignmentsfile = parser.addStringOption('a',"alignments");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option odebug = parser.addStringOption('d',"debug");
        CmdLineParser.Option omaxlen = parser.addIntegerOption('m',"maxlen");
        CmdLineParser.Option occondition = parser.addDoubleOption('c',"convergence-condition");
        CmdLineParser.Option oonlysures = parser.addBooleanOption("only-sures");
        

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
        String alignmentsfile=(String)parser.getOptionValue(oalignmentsfile,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String debugpath=(String)parser.getOptionValue(odebug,null);
        Evidence.max_seg_len=(Integer)parser.getOptionValue(omaxlen,5);
        double ccondition=(Double)parser.getOptionValue(occondition,0.0001);
        boolean onlysures=(Boolean)parser.getOptionValue(oonlysures,false);
        boolean debug=(debugpath!=null);

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
        if(alignmentsfile==null){
            System.err.println("Error: It is necessary to define the path to the file containing the gold standard alignments (use option '-a').");
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
        
        SegmentDictionary sd=new SegmentDictionary();
        sd.LoadSegments(sourcetransfile, targettransfile, false);
        
        BufferedReader sreader=null, treader=null, areader=null;
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
        try{
            areader=new BufferedReader(new FileReader(alignmentsfile));
        }catch(FileNotFoundException ex){
            System.err.println("Error: It was impossible to find file '"+alignmentsfile+"'. Please check the path.");
            System.exit(-1);
        }
        
        List<Instance> instances=new LinkedList<Instance>();
        
        String sline, tline;
        try{
            int counter=1;
            String alignment=areader.readLine();
            String[] align_info;
            while((sline=sreader.readLine())!=null && (tline=treader.readLine())!=null){
                TranslationUnit tu=new TranslationUnit(sline, tline);
                if(alignment==null){
                    System.err.println("Error: there are more pairs of segments than alignments");
                    System.exit(-1);
                }
                else{
                    //Array.newInstance(Class<LinkedHashSet<Integer>>, 1);
                    Set[] alignment_array=new Set[tu.getSource().size()];
                    Arrays.fill(alignment_array,null);
                    align_info=alignment.split(" ");
                    int sentence, w1, w2;
                    boolean possible=false;
                    try{
                        switch(align_info.length){
                            case 4: possible=true; break;
                            case 3: possible=false; break;
                            default: System.err.println("Error in format of alignment file: "+alignment); System.exit(-1);
                        }
                        sentence= Integer.parseInt(align_info[0]);
                        w1= Integer.parseInt(align_info[1]);
                        w2= Integer.parseInt(align_info[2]);
                        while(sentence==counter){
                            if(!possible || !onlysures){
                                if(alignment_array[w1-1]==null)
                                    alignment_array[w1-1]=new LinkedHashSet();
                                alignment_array[w1-1].add(new ReferenceAlignment(w2-1, possible));
                            }
                            alignment=areader.readLine();
                            if(alignment==null)
                                sentence++;
                            else{
                                align_info=alignment.split(" ");
                                switch(align_info.length){
                                    case 4: possible=true; break;
                                    case 3: possible=false; break;
                                    default: System.err.println("Error in format of alignment file: "+alignment); System.exit(-1);
                                }
                                sentence= Integer.parseInt(align_info[0]);
                                w1= Integer.parseInt(align_info[1]);
                                w2= Integer.parseInt(align_info[2]);
                            }
                        }
                    }catch(NumberFormatException ex){
                        System.err.println("Error in format of alignment file: "+alignment);
                        System.exit(-1);
                    }
                    tu.CollectHTMLEvidences(sd, Evidence.max_seg_len, false);
                    Instance instance=new Instance(tu, alignment_array);
                    if(dbgpw!=null){
                        dbgpw.print("Adding instance ");
                        dbgpw.println(counter);
                        dbgpw.print("Sentence 1: ");
                        dbgpw.println(instance.getSegment1());
                        dbgpw.print("Sentence 2: ");
                        dbgpw.println(instance.getSegment2());
                        dbgpw.print("Alignment: ");
                        for(int i=0;i<instance.getAlignment().length;i++){
                            if(instance.getAlignment()[i]!=null){
                                for(Object o: instance.getAlignment()[i]){
                                    dbgpw.print("[");
                                    dbgpw.print(instance.getSegment1().getWord(i).getValue());
                                    dbgpw.print("-");
                                    int a=(Integer)o;
                                    dbgpw.print(instance.getSegment2().getWord(a).getValue());
                                    dbgpw.print("] ");
                                }
                            }
                        }
                        dbgpw.println();
                        dbgpw.print("Evidences: ");
                        for(Evidence e: tu.getEvidences()){
                            dbgpw.print("[");
                            dbgpw.print(e.getSegment());
                            dbgpw.print("-");
                            dbgpw.print(e.getTranslation());
                            dbgpw.print("] ");
                        }
                        dbgpw.println();
                        for(int i=0;i<instance.getSegment1().getSentence().size();i++){
                            dbgpw.print("Features ");
                            dbgpw.print(instance.getSegment1().getWord(i).getValue());
                            dbgpw.print(": ");
                            for(int j=0;j<instance.getSegment2().getSentence().size();j++){
                                dbgpw.print("[");
                                dbgpw.print(instance.getSegment2().getWord(j).getValue());
                                dbgpw.print("|");
                                FeaturesMatrix fm=instance.GetFeatures(i, j);
                                for(int k=1;k<=5;k++){
                                    for(int l=1;l<=5;l++){
                                        dbgpw.print(fm.GetFeature(k, l));
                                        dbgpw.print(" - ");
                                    }
                                }
                                dbgpw.print("]");
                            }
                            dbgpw.println();
                        }
                        
                        dbgpw.println();
                    }
                    instances.add(instance);
                }
                counter++;
                //if(counter==3) break;
            }
            sreader.close();
            if(treader!=null){
                treader.close();
            }
            areader.close();
            System.err.println("Starting maximization");
            double[][] new_weigths=Simplex.Maximize(instances, ccondition);
            for(double[] line: new_weigths){
                pw.print(line[0]);
                for(int i=1;i<line.length;i++){
                    pw.print(" ");
                    pw.print(line[i]);
                }
                pw.println();
            }
            pw.close();
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

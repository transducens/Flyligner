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


//-s /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkingsimplex/clean.corpus.sl -t /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkingsimplex/clean.corpus.tl --source-translations /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkingsimplex/ssegs.sl --target-translations /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkingsimplex/ssegs.tl -a /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkingsimplex/refalignment.naacl -m 3
//-s /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkinggradientdescent/clean.corpus.sl -t /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkinggradientdescent/clean.corpus.tl --source-translations /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkinggradientdescent/ssegs.sl --target-translations /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkinggradientdescent/ssegs.tl -a /home/miquel/Projects/TreballAlTren/ExperimentsColing2012/dumbexample/checkinggradientdescent/refalignment.naacl -m 3
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import es.ua.dlsi.features.Instance;
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
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author miquel
 */
public class TrainedAlignment {
    /**
     * Symetrisation method which produces, as a result, the intersection of the
     * S2T and T2S alignments.
     * @param instance
     * @param s2t
     * @param t2s
     * @return 
     */
    static public boolean[][] IntersectionSymal(Instance instance,
            Map<Integer,Integer> s2t, Map<Integer,Integer> t2s){
        boolean[][] intersection=new boolean[instance.getSegment1().size()][instance.getSegment2().size()];
        //Initialisation of the alignment matrix to "all the words are unaligned"
        for(boolean[] row: intersection)
            Arrays.fill(row, false);
        //For all the alignments in S2T
        for(Entry<Integer,Integer> s2t_entry: s2t.entrySet()){
            Integer s_word=s2t_entry.getKey();
            Integer t_word=s2t_entry.getValue();
            //For all the alignments in T2S
            if(t2s.containsKey(t_word)){
                //If the alignment appears in both the asymetric alignments, it is added to the symetrised alignment
                if(t2s.get(t_word)==s_word)
                    intersection[s_word][t_word]=true;
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
    static public boolean[][] UnionSymal(Instance instance,
            Map<Integer,Integer> s2t, Map<Integer,Integer> t2s){
        boolean[][] union=new boolean[instance.getSegment1().size()][instance.getSegment2().size()];
        //Initialisation of the alignment matrix to "all the words are unaligned"
        for(boolean[] row: union)
            Arrays.fill(row, false);
        //All the alignments in S2T are added to the union
        for(Entry<Integer,Integer> s2t_entry: s2t.entrySet()){
            Integer s_word=s2t_entry.getKey();
            Integer t_word=s2t_entry.getValue();
            union[s_word][t_word]=true;
        }
        //All the alignments in T2S are added to the union
        for(Entry<Integer,Integer> t2s_entry: t2s.entrySet()){
            Integer t_word=t2s_entry.getKey();
            Integer s_word=t2s_entry.getValue();
            union[s_word][t_word]=true;
        }
        return union;
    }
    
    static public boolean[][] GrowDiagFinalAnd(Instance instance,
            Map<Integer,Integer> s2t, Map<Integer,Integer> t2s){
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
        boolean[][] currentpoints=IntersectionSymal(instance, s2t, t2s); //symmetric alignment
        //Union of the alignments (space for growing)
        boolean[][] unionalignment=UnionSymal(instance, s2t, t2s); //union alignment
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
    
    static public Map<Integer,Integer> GreedyAlignS2T(Instance i, double[][] weights, PrintWriter debug){
        //Computing the complete matrix of probabilities
        double[][] probabilities=Probabilities.BuildJointProbabilityMatrix(i, weights);
        if(debug!=null){
            debug.println(i.getSegment1().toString());
            debug.println(i.getSegment2().toString());
            for(double[] row: probabilities){
                for(double cell: row){
                    debug.print(cell);
                    debug.print(" ");
                }
                debug.println();
            }
        }
        double null_probability=1/Probabilities.NormalisationDenomForJointProbability(i, weights);
        return GreedyAlignProbPrecomputedS2T(i, probabilities, null_probability);
    }
    
    static public Map<Integer,Integer> GreedyAlignT2S(Instance i, double[][] weights, PrintWriter debug){
        //Computing the complete matrix of probabilities
        double[][] probabilities=Probabilities.BuildJointProbabilityMatrix(i, weights);
        if(debug!=null){
            debug.println(i.getSegment1().toString());
            debug.println(i.getSegment2().toString());
            for(double[] row: probabilities){
                for(double cell: row){
                    debug.print(cell);
                    debug.print(" ");
                }
                debug.println();
            }
        }
        double null_probability=1/Probabilities.NormalisationDenomForJointProbability(i, weights);
        return GreedyAlignProbPrecomputedT2S(i, probabilities, null_probability);
    }
    
    static public Map<Integer,Integer> GreedyAlignProbPrecomputedS2T(Instance i, double[][] probabilities, double null_probability){
        //Map with the best alignments for each source language word
        Map<Integer,Integer> alignment=new HashMap<Integer, Integer>();
        //Runing on the words in the SL sentence
        for(int w1=0; w1<i.getSegment1().size();w1++){
            int best_candidate=-1;
            double best_probability=null_probability;
            //The probabilities table is checked for all the words in the TL sentence
            for(int w2=0; w2<i.getSegment2().size();w2++){
                double new_probability=probabilities[w1][w2];
                if(best_probability<new_probability){
                    best_probability=new_probability;
                    best_candidate=w2;
                }else if(new_probability>null_probability && best_probability==new_probability){
                    if(Math.abs(w1-w2)<Math.abs(w1-best_candidate))
                        best_candidate=w2;
                }
            }
            if(best_candidate>=0)
                alignment.put(w1,best_candidate);
        }
        return alignment;
    }
    
    static public Map<Integer,Integer> GreedyAlignProbPrecomputedT2S(Instance i, double[][] probabilities, double null_probability){
        //Map with the best alignments for each source language word
        Map<Integer,Integer> alignment=new HashMap<Integer, Integer>();
        //Runing on the words in the SL sentence
        for(int w2=0; w2<i.getSegment2().size();w2++){
            int best_candidate=-1;
            double best_probability=null_probability;
            //The probabilities table is checked for all the words in the TL sentence
            for(int w1=0; w1<i.getSegment1().size();w1++){
                double new_probability=probabilities[w1][w2];
                if(best_probability<new_probability){
                    best_probability=new_probability;
                    best_candidate=w1;
                }
            }
            if(best_candidate>=0)
                alignment.put(w2,best_candidate);
        }
        return alignment;
    }
    
    /*static public Map<Integer,Set<Integer>> GreedyAlignProbPrecomputedT2S(Instance i, double[][] probabilities, double null_probability){
        //Map with the best alignments for each source language word
        Map<Integer,Set<Integer>> alignment=new HashMap<Integer, Set<Integer>>();
        //Runing on the words in the SL sentence
        for(int w2=0; w2<i.getSegment2().size();w2++){
            Set<Integer> best_candidates=new LinkedHashSet<Integer>();
            double best_probability=0.0;
            //The probabilities table is checked for all the words in the TL sentence
            for(int w1=0; w1<i.getSegment1().size();w1++){
                double new_probability=probabilities[w1][w2];
                if(best_probability<new_probability){
                    best_probability=new_probability;
                    best_candidates=new LinkedHashSet<Integer>();
                    best_candidates.add(w1);
                }
                else if (best_probability==new_probability){
                    best_candidates.add(w1);
                }
            }
            alignment.put(w2,best_candidates);
        }
        return alignment;
    }*/
    
    /*static public List<Pair<Integer,Integer>> GreedyAlign(Instance i, double[][] weights){
        //Resulting alignment (pairs of words)
        List<Pair<Integer,Integer>> alignment=new LinkedList<Pair<Integer, Integer>>();
        //List of unaligned words in s
        Set<Integer> unalignedwords_s=new LinkedHashSet<Integer>();
        //List of unaligned words in t
        Set<Integer> unalignedwords_t=new LinkedHashSet<Integer>();
        //Map of probabilities: for each probability, the alignments
        Map<Double,Set<Pair<Integer,Integer>>> probs=new HashMap<Double,Set<Pair<Integer,Integer>>>();
        //Loading unaligned words (all of them in the beggining) for the sentences
        for(int w2=0; w2<i.getSegment2().size();w2++)
            unalignedwords_t.add(w2);
        for(int w1=0; w1<i.getSegment1().size();w1++){
            unalignedwords_s.add(w1);
            //Adding the properties and the pairs to the map of properties
            for(int w2=0; w2<i.getSegment2().size();w2++){
                double newprob=Probabilities.JointProbability(i, w1, w2, weights);
                Set<Pair<Integer,Integer>> sub_map=probs.get(newprob);
                if(sub_map==null){
                    sub_map=new LinkedHashSet<Pair<Integer, Integer>>();
                    probs.put(newprob,sub_map);
                }
                sub_map.add(new Pair<Integer, Integer>(w1, w2));
            }
        }
        SortedSet<Double> sortedprobs=new TreeSet<Double>(probs.keySet());
        while(sortedprobs.size()!=0 && unalignedwords_s!=null && unalignedwords_t!=null){
            double highestprob=sortedprobs.last();
            Set<Pair<Integer, Integer>> alignments=probs.get(sortedprobs.last());
            sortedprobs.remove(highestprob);
            for(Pair<Integer, Integer> p: alignments){
                if(unalignedwords_s.contains(p.getFirst()) ||
                        unalignedwords_t.contains(p.getSecond())){
                    alignment.add(p);
                    unalignedwords_s.remove(p.getFirst());
                    unalignedwords_t.remove(p.getSecond());
                    if(unalignedwords_s==null && unalignedwords_t==null)
                        break;
                }
            }
        }
        return alignment;
    }
    
    static public List<Pair<Integer,Integer>> GreedyAlignProbPrecomputed(Instance i, double[][] probabilities){
        List<Pair<Integer,Integer>> alignment=new LinkedList<Pair<Integer, Integer>>();
        Set<Integer> unalignedwords_s=new LinkedHashSet<Integer>();
        Set<Integer> unalignedwords_t=new LinkedHashSet<Integer>();
        Map<Double,Set<Pair<Integer,Integer>>> probs=new HashMap<Double,Set<Pair<Integer,Integer>>>();
        for(int w1=0; w1<i.getSegment1().size();w1++)
            unalignedwords_s.add(w1);
        for(int w2=0; w2<i.getSegment2().size();w2++)
            unalignedwords_t.add(w2);
        for(int j=0;j<probabilities.length;j++){
            for(int k=0;k<probabilities[j].length;k++){
                double newprob=probabilities[j][k];
                Set<Pair<Integer,Integer>> sub_map=probs.get(newprob);
                if(sub_map==null){
                    sub_map=new LinkedHashSet<Pair<Integer, Integer>>();
                    probs.put(newprob,sub_map);
                }
                sub_map.add(new Pair<Integer, Integer>(j, k));
            }
        }
        SortedSet<Double> sortedprobs=new TreeSet<Double>(probs.keySet());
        while(sortedprobs.size()!=0 && !unalignedwords_s.isEmpty() && !unalignedwords_t.isEmpty()){
            double highestprob=sortedprobs.last();
            Set<Pair<Integer, Integer>> alignments=probs.get(sortedprobs.last());
            sortedprobs.remove(highestprob);
            for(Pair<Integer, Integer> p: alignments){
                if(unalignedwords_s.contains(p.getFirst()) ||
                        unalignedwords_t.contains(p.getSecond())){
                    alignment.add(p);
                    unalignedwords_s.remove(p.getFirst());
                    unalignedwords_t.remove(p.getSecond());
                    if(unalignedwords_s.isEmpty() && unalignedwords_t.isEmpty())
                        break;
                }
            }
        }
        return alignment;
    }*/
    
    static private double[][] ReadWeightsFile(String path) throws FileNotFoundException, IOException{
        double[][] weights;
        
        BufferedReader br=new BufferedReader(new FileReader(path));
        String line;
        List<String[]> read_values=new LinkedList<String[]>();
        int len=-1;
        while((line=br.readLine())!=null){
            String[] values=line.split(" ");
            if(len==-1)
                len=values.length;
            else{
                if(len!=values.length){
                    System.err.println("Malformed weights file!");
                    System.exit(-1);
                }
            }
            read_values.add(values);
        }
        br.close();
        weights=new double[read_values.size()][len];
        int counter=0;
        for(String[] v: read_values){
            for(int i=0;i<v.length;i++)
                weights[counter][i]=Double.parseDouble(v[i]);
            counter++;
        }
        return weights;
    }
    
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option osourcesegsfile = parser.addStringOption('s',"source-segments");
        CmdLineParser.Option otargetsegsfile = parser.addStringOption('t',"target-segments");
        CmdLineParser.Option osourcetransfile = parser.addStringOption("source-translations");
        CmdLineParser.Option otargettransfile = parser.addStringOption("target-translations");
        CmdLineParser.Option oweightsfile = parser.addStringOption('w',"weights");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option odebug = parser.addStringOption('d',"debug");
        CmdLineParser.Option osym = parser.addStringOption("symmetrisation");
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
        String weightsfile=(String)parser.getOptionValue(oweightsfile,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String debugpath=(String)parser.getOptionValue(odebug,null);
        String sym=(String)parser.getOptionValue(osym,"gdfa");
        boolean debug=(debugpath!=null);
        boolean help=(Boolean)parser.getOptionValue(ohelp,false);
        
        if(help){
            System.err.println("This class must be called:\n");
            System.err.println("java -cp es.ua.dlsi.alignment.TrainedAlignment"+
                    "-s <source_language_sentences> -t <source_language_sentences>"+
                    "--source-translations <source_language_sub-segment_dictionary>"+
                    "--target-translations <target_language_sub-segment_dictionary>"+
                    "-w <weights> [-o <output_path>] [--symmetrisation <symmetrisation_method>]\n" );
            System.err.print("\t*source_language_sentences and target_language_sentences: ");
            System.err.println("the list of sentences to be aligned; these files must contain a sentence per line.");
            System.err.print("\t*source_language_sub-segment_dictionary and target_language_sub-segment_dictionary: ");
            System.err.println("the list of aligned pairs of sub-segments; these files must contain a sub-segment per line.");
            System.err.println("\t*weights: path to the file containing the weights; the file must contain"+
                    " NxN values, where N is the maximum length of the sub-segments used as a source of"+
                    " information. The file must contain N rows and, in every row, N floating point numbers"+
                    " separated by a simple blank space.");
            System.err.println("\t*output_path: path to the file containing the output. The output will"+
                    " be produced in the same format than GIZA++ symmetrised alignments.");
            System.err.println("\t*symmetrisation_method: the heuristic for symmetrising the alignments."+
                    " One of the following values must be chosen: 'union', 'intersection', or 'gdfa'"+
                    " (grow-diag-final-and, which is the default value");
        }
        else{
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
                System.err.println("Warning: undefined output file. Output redirected to standard output");
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
            //File sd_obj = new File(sf.getParent()+"/"+sf.getName()+"_"+tf.getName()+".obj");
            /*if(sd_obj.canRead()){
                Date ssegs_datetime = new Date(sf.lastModified());
                Date tsegs_datetime = new Date(tf.lastModified());
                Date obj_datetime = new Date(sd_obj.lastModified());
                if(ssegs_datetime.before(obj_datetime) && tsegs_datetime.before(obj_datetime)){
                    System.err.println("Loading dictionary from previous preprociessing.");
                    try{
                        ObjectInputStream ois=new ObjectInputStream(new FileInputStream(sd_obj));
                        sd=(SegmentDictionary)ois.readObject();
                        ois.close();
                    }catch(IOException ex){
                        ex.printStackTrace(System.err);
                        System.exit(-1);
                    }catch(ClassNotFoundException ex){
                        ex.printStackTrace(System.err);
                        System.exit(-1);
                    }
                }else{
                    System.err.println("Loading dictionary from text files.");
                    sd=new SegmentDictionary();
                    sd.LoadSegments(sourcetransfile, targettransfile, false);

                    try{
                        FileOutputStream fos = new FileOutputStream(sd_obj);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(sd);
                        oos.close();
                    }catch(FileNotFoundException ex){
                        ex.printStackTrace(System.err);
                        System.exit(-1);
                    }catch(IOException ex){
                        ex.printStackTrace(System.err);
                        System.exit(-1);
                    }
                }
            }
            else{*/
                System.err.println("Loading dictionary from text files.");
                sd=new SegmentDictionary();
                sd.LoadSegments(sourcetransfile, targettransfile, false);
    /*
                try{
                    FileOutputStream fos = new FileOutputStream(sd_obj);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(sd);
                    oos.close();
                }catch(FileNotFoundException ex){
                    ex.printStackTrace(System.err);
                    System.exit(-1);
                }catch(IOException ex){
                    ex.printStackTrace(System.err);
                    System.exit(-1);
                }
            }*/
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
            double[][] weights=null;
            try{
                weights=ReadWeightsFile(weightsfile);
            }catch(IOException ex){
                System.err.println("Error while trying to open/read weights file "+weightsfile);
                System.exit(-1);
            }
            Evidence.max_seg_len=weights.length;
            try{
                //int counter=1;
                //String[] align_info;
                while((sline=sreader.readLine())!=null && (tline=treader.readLine())!=null){
                    TranslationUnit tu=new TranslationUnit(sline, tline);

                    //Array.newInstance(Class<LinkedHashSet<Integer>>, 1);
                    Set[] alignment_array=new Set[tu.getSource().size()];
                    Arrays.fill(alignment_array,null);

                    tu.CollectEvidences(sd, Evidence.max_seg_len, false);
                    Instance instance=new Instance(tu, alignment_array);
                    Map<Integer,Integer> al1=GreedyAlignS2T(instance,weights, dbgpw);
                    Map<Integer,Integer> al2=GreedyAlignT2S(instance,weights, dbgpw);


                    boolean[][] al=null;
                    if(sym.equals("union")){
                        al=TrainedAlignment.UnionSymal(instance, al1, al2);
                    }else if(sym.equals("intersection")){
                        al=TrainedAlignment.IntersectionSymal(instance, al1, al2);
                    }else if(sym.equals("gdfa")){
                        al=TrainedAlignment.GrowDiagFinalAnd(instance, al1, al2);
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
                    /*if(debug){
                        dbgpw.print("Adding instance ");
                        dbgpw.println(counter);
                        dbgpw.print("Sentence 1: ");
                        dbgpw.println(instance.getSegment1());
                        dbgpw.print("Sentence 2: ");
                        dbgpw.println(instance.getSegment2());

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
                                for(int k=1;k<=Evidence.max_seg_len;k++){
                                    for(int l=1;l<=Evidence.max_seg_len;l++){
                                        dbgpw.print(fm.GetFeature(k, l));
                                        dbgpw.print(" - ");
                                    }
                                }
                                dbgpw.print("]");
                            }
                            dbgpw.println();
                        }

                        dbgpw.println();
                    }*/
                }
                //counter++;
                //if(counter==30) break;
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

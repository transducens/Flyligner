/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.dlsi.alignment;

//import es.ua.dlsi.recommendation.GeometricRecommender;
import static es.ua.dlsi.alignment.TrainedAlignment.GreedyAlignS2T;
import es.ua.dlsi.features.Instance;
import es.ua.dlsi.segmentation.Evidence;
import es.ua.dlsi.segmentation.TranslationUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class GeometricAligner {
    
    
    /**
     * This class computes the alignment forces matrix between the words
     * of both the segments of a translation unit using machine translated
     * sub-segments with length L. In this algorithm, we compute the weigth of
     * each translated sub-segment as 1/Length(sub_segment1)*Lenght(sub_segment2).
     * Then, we fill the table putting, in each square, the summation of the
     * weights of each sub-segment with length between 1 and L.
     * @param tu Translation unit to be aligned
     * @param debug Flag that indicates if the debugging messages should be shown
     * @return Returns a table of alignment forces between words in both the segments in the translation unit
     */
    public static double[][] AlignmentForces(TranslationUnit tu, int maxlen, boolean debug){
        double[][] scores=new double[tu.getSource().size()][tu.getTarget().size()];
        for(int i=0; i<scores.length;i++)
            Arrays.fill(scores[i], 0.0);
        for(Evidence e: tu.getEvidences()){
            if(e.getSegment().getLength()<=maxlen && e.getTranslation().getLength()<=maxlen){
                if(debug){
                    System.out.print(e.getSegment());
                    System.out.print(" - ");
                    System.out.println(e.getTranslation());
                }
                for(int i=0; i<e.getSegment().getLength(); i++){
                    for(int j=0; j<e.getTranslation().getLength(); j++){
                        scores[i+e.getSegment().getPosition()][j+e.getTranslation().getPosition()]+=1.0/((double)e.getSegment().getLength()*e.getTranslation().getLength());
                    }
                }
            }
        }
        if(debug){
            for(int i=0;i<scores.length;i++){
                for(int j=0;j<scores[i].length;j++){
                    System.out.print(scores[i][j]);
                    System.out.print("\t");
                }
                System.out.println();
            }
        }
        return scores;
    }
    
    public static Set<Integer>[] AlignS2TBestAddAllTied(TranslationUnit tu, int maxlen,
            double[][] alignment_forces){
        Set<Integer>[] exit=new Set[tu.getSource().size()];
        Arrays.fill(exit, null);
        for(int i=0;i<tu.getSource().size();i++){
            Set<Integer> bestcandidates=new LinkedHashSet<Integer>();
            double best_score=0.0;
            for(int j=0;j<tu.getTarget().size();j++){
                if(alignment_forces[i][j]>best_score){
                    bestcandidates=new LinkedHashSet<Integer>();
                    bestcandidates.add(j);
                    best_score=alignment_forces[i][j];
                }
                else if(alignment_forces[i][j]==best_score){
                    bestcandidates.add(j);
                }
            }
            if(best_score>0)
                exit[i]=bestcandidates;
        }
        return exit;
    }

    public static Set<Integer>[] AlignS2TGoodEnoougth(TranslationUnit tu, int maxlen,
            double[][] alignment_forces){
        Set<Integer>[] exit=new Set[tu.getSource().size()];
        Arrays.fill(exit, null);
        for(int i=0;i<tu.getSource().size();i++){
            Set<Integer> bestcandidates=new LinkedHashSet<Integer>();
            for(int j=0;j<tu.getTarget().size();j++){
                if(alignment_forces[i][j]>0.5){
                    bestcandidates.add(j);
                }
            }
            if(bestcandidates.size()>0)
                exit[i]=bestcandidates;
        }
        return exit;
    }

    public static Set<Integer>[] AlignS2TBestAddAllTied(TranslationUnit tu, int maxlen){
        double[][] alignment_forces=AlignmentForces(tu, maxlen, false);
        return AlignS2TBestAddAllTied(tu,maxlen,alignment_forces);
    }

    public static Set<Integer>[] AlignS2TBestNoAlignmentForTied(TranslationUnit tu, int maxlen,
            double[][] alignment_forces){
        Set<Integer>[] exit=new Set[tu.getSource().size()];
        Arrays.fill(exit, null);
        for(int i=0;i<tu.getSource().size();i++){
            Set<Integer> bestcandidates=null;
            double best_score=0.0;
            for(int j=0;j<tu.getTarget().size();j++){
                if(alignment_forces[i][j]>best_score){
                    bestcandidates=new LinkedHashSet<Integer>();
                    bestcandidates.add(j);
                    best_score=alignment_forces[i][j];
                }
                else if(alignment_forces[i][j]==best_score){
                    bestcandidates=null;
                }
            }
            if(best_score>0)
                exit[i]=bestcandidates;
        }
        return exit;
    }

    public static Set<Integer>[] AlignS2TBestNoAlignmentForTied(TranslationUnit tu, int maxlen){
        double[][] alignment_forces=AlignmentForces(tu, maxlen, false);
        return AlignS2TBestAddAllTied(tu,maxlen,alignment_forces);
    }

    public static Set<Integer>[] AlignT2SBestAddAllTied(TranslationUnit tu, int maxlen,
            double[][] alignment_forces){
        Set<Integer>[] exit=new Set[tu.getTarget().size()];
        Arrays.fill(exit, null);
        for(int i=0;i<tu.getTarget().size();i++){
            Set<Integer> bestcandidates=new LinkedHashSet<Integer>();
            double best_score=0.0;
            for(int j=0;j<tu.getSource().size();j++){
                if(alignment_forces[j][i]>best_score){
                    bestcandidates=new LinkedHashSet<Integer>();
                    bestcandidates.add(j);
                    best_score=alignment_forces[j][i];
                }
                else if(alignment_forces[j][i]==best_score){
                    bestcandidates.add(j);
                }
            }
            if(best_score>0)
                exit[i]=bestcandidates;
        }
        return exit;
    }

    public static Set<Integer>[] AlignT2SBestAddAllTied(TranslationUnit tu, int maxlen){
        double[][] alignment_forces=AlignmentForces(tu, maxlen, false);
        return AlignT2SBestAddAllTied(tu,maxlen,alignment_forces);
    }

    public static Set<Integer>[] AlignT2SBestNoAlignmentForTied(TranslationUnit tu, int maxlen,
            double[][] alignment_forces){
        Set<Integer>[] exit=new Set[tu.getTarget().size()];
        Arrays.fill(exit, null);
        for(int i=0;i<tu.getTarget().size();i++){
            Set<Integer> bestcandidates=null;
            double best_score=0.0;
            for(int j=0;j<tu.getSource().size();j++){
                if(alignment_forces[i][j]>best_score){
                    bestcandidates=new LinkedHashSet<Integer>();
                    bestcandidates.add(i);
                    best_score=alignment_forces[i][j];
                }
                else if(alignment_forces[i][j]==best_score){
                    bestcandidates=null;
                }
            }
            if(best_score>0)
                exit[i]=bestcandidates;
        }
        return exit;
    }

    public static Set<Integer>[] AlignT2SBestNoAlignmentForTied(TranslationUnit tu, int maxlen){
        double[][] alignment_forces=AlignmentForces(tu, maxlen, false);
        return AlignS2TBestAddAllTied(tu,maxlen,alignment_forces);
    }
}

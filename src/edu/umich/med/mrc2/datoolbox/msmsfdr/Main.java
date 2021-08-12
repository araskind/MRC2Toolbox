package edu.umich.med.mrc2.datoolbox.msmsfdr;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final double inverseFdrBinSize = 10000;

  public static void main(String[] args) {
    int nThreads = Runtime.getRuntime().availableProcessors() - 1;

    double[] targetData = null; // target scores
    double[] decoyData = null; // decoy scores
    double[] positiveData = null; // scores with high confidence to be positive
    double[] negativeData = null; // scores with high confidence to be negative
    double[] targetProbArray = null; // probabilities from target
    double[] targetFdrArray; // FDR calculated using the probability
    String plotFileNamePrefix = ""; // path prefix to write the plots
    String fileName = ""; // file name of the plot

    try {
      ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
      if (targetData.length > 10 && decoyData.length > 10 && positiveData.length > 10) {
        MixtureModelKDESemiParametric mixtureModelKDESemiParametric = 
        		new MixtureModelKDESemiParametric(executorService, nThreads);
        mixtureModelKDESemiParametric.NoBinPoints = 1000;
        mixtureModelKDESemiParametric.SetData(targetData, decoyData, positiveData, negativeData);
        mixtureModelKDESemiParametric.Modeling();
        mixtureModelKDESemiParametric.GeneratePlot(plotFileNamePrefix, fileName);
        assignProb(targetData, targetProbArray, mixtureModelKDESemiParametric.MixtureModelProb);

        double[] fdrArray = estimateFDR(targetProbArray);
        targetFdrArray = new double[targetProbArray.length];
        for (int i = 0; i < targetProbArray.length; ++i) {
          targetFdrArray[i] = fdrArray[(int) (targetProbArray[i] * inverseFdrBinSize)];
        }
      } else {
        System.err.printf("There are not enough data points to fix a mixture model "
        		+ "(decoy = %d, target = %d, id = %d). "
        		+ "Will estimate the probability using a simplified approach.%n", 
        		decoyData.length, targetData.length, positiveData.length);
        System.exit(1);
      }

      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
          throw new InterruptedException("Thread pool did not terminate normally.");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  private static void assignProb(double[] targetScoreArray, double[] targetProbArray, float[][] mixtureModelProb) {
    if (mixtureModelProb == null) {
      for (int i = 0; i < targetScoreArray.length; ++i) {
        targetProbArray[i] = targetScoreArray[i] > 0 ? 1 : 0;
      }
    } else {
      float[] scoreArray = new float[mixtureModelProb.length];
      for (int i = 0; i < mixtureModelProb.length; ++i) {
        scoreArray[i] = mixtureModelProb[i][0];
      }
      Arrays.parallelSort(scoreArray); // make sure that the scores are sorted, but it does not matter much because the scores are supposed to be sorted already
      for (int i = 0; i < targetScoreArray.length; ++i) {
        int idx = Arrays.binarySearch(scoreArray, (float) targetScoreArray[i]);
        if (idx < 0) {
          idx = Math.min(scoreArray.length - 1, -1 * idx - 1);
        }
        targetProbArray[i] = Math.min(1, Math.max(0, mixtureModelProb[mixtureModelProb.length - 1 - idx][2]));
      }
    }
  }

  private static double[] estimateFDR(double[] targetProbArray) {
    int targetCount = targetProbArray.length;
    double pepSum = 0;
    int[] targetCountArray = new int[(int) (1 * inverseFdrBinSize) + 1];
    double[] pepArray = new double[targetCountArray.length];
    for (double probability : targetProbArray) {
      ++targetCountArray[(int) (probability * inverseFdrBinSize)];
      pepArray[(int) (probability * inverseFdrBinSize)] += (1 - probability);
      pepSum += (1 - probability);
    }

    double lastFdr = Math.min(pepSum / (double) targetCount, 1);
    double[] fdrArray = new double[targetCountArray.length];
    fdrArray[0] = lastFdr;
    for (int i = 0; i < targetCountArray.length - 1; ++i) {
      targetCount -= targetCountArray[i];
      pepSum -= pepArray[i];
      double fdr;
      if (targetCount != 0) {
        fdr = Math.min(lastFdr, pepSum / (double) targetCount);
      } else {
        fdr = Math.min(lastFdr, 1);
      }
      lastFdr = fdr;
      fdrArray[i + 1] = fdr;
    }
    return fdrArray;
  }

}

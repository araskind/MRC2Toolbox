/*
Modified from DIA-Umpire's MixtureModelKDESemiParametric.java
 */
package edu.umich.med.mrc2.datoolbox.msmsfdr;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.msmsfdr.jMEF.ExpectationMaximization1D;
import edu.umich.med.mrc2.datoolbox.msmsfdr.jMEF.MixtureModel;
import edu.umich.med.mrc2.datoolbox.msmsfdr.jMEF.PVector;
import umontreal.ssj.probdist.EmpiricalDist;
import umontreal.ssj.probdist.NormalDist;

/**
 * This class provides semi-parametric mixture modeling
 */
public class MixtureModelKDESemiParametric {

  private static final boolean writeModel = false;

  private final ExecutorService executorService;
  private final int nThreads;

  private EmpiricalDist targetEmpiricalDist;
  private EmpiricalDist decoyEmpiricalDist;
  private EmpiricalDist positiveEmpiricalDist;
  private EmpiricalDist negativeEmpiricalDist;
  double bandWidth;
  NormalDist kern = new NormalDist();
  public float[][] MixtureModelProb;
  MixtureModel mmc;
  public int NoBinPoints = 1000;
  float max;
  float min;
  float intv;
  double[] p;
  double[] f1;
  double[] f0;
  double weight_incorrect;

  double weight_correct;
  double pisum = 0d;
  int MAX_ITERATIONS = 50;
  double[] model_kde_x;
  double[] model_kde_y;
  double[] decoy_kde_y;
  double[] correct_kde_y;
  double[] inicorrect_kde_y;

  public MixtureModelKDESemiParametric(ExecutorService executorService, int nThreads) {
    this.executorService = executorService;
    this.nThreads = nThreads;
  }

  public void GeneratePlot(String fileNamePrefix, String runName) throws IOException {
    double[] targetObs = new double[targetEmpiricalDist.getN()];
    double[] decoyObs = new double[decoyEmpiricalDist.getN()];
    double[] positiveObs = new double[positiveEmpiricalDist.getN()];
    double[] negativeObs = new double[negativeEmpiricalDist.getN()];

    for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
      targetObs[i] = targetEmpiricalDist.getObs(i);
    }
    for (int i = 0; i < decoyEmpiricalDist.getN(); ++i) {
      decoyObs[i] = decoyEmpiricalDist.getObs(i);
    }
    for (int i = 0; i < positiveEmpiricalDist.getN(); ++i) {
      positiveObs[i] = positiveEmpiricalDist.getObs(i);
    }
    for (int i = 0; i < negativeEmpiricalDist.getN(); ++i) {
      negativeObs[i] = negativeEmpiricalDist.getObs(i);
    }

    XYSeries model1 = new XYSeries("Fitted negative distribution");
    XYSeries model2 = new XYSeries("Fitted positive distribution");

    if (writeModel) {
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileNamePrefix + "_model.csv"), 1 << 24);
      bufferedWriter.write("score,model,fitted_positive,fitted_negative\n");
      for (int i = 0; i < NoBinPoints; ++i) {
        bufferedWriter.write(model_kde_x[i] + "," + model_kde_y[i] + "," + correct_kde_y[i] + "," + decoy_kde_y[i] + "\n");
      }
      bufferedWriter.close();
      bufferedWriter = new BufferedWriter(new FileWriter(fileNamePrefix + "_target_hist.csv"), 1 << 24);
      for (double v : targetObs) {
        bufferedWriter.write(v + "\n");
      }
      bufferedWriter.close();
      bufferedWriter = new BufferedWriter(new FileWriter(fileNamePrefix + "_decoy_hist.csv"), 1 << 24);
      for (double v : decoyObs) {
        bufferedWriter.write(v + "\n");
      }
      bufferedWriter.close();
    }

    for (int i = 0; i < NoBinPoints; ++i) {
      model1.add(model_kde_x[i], decoy_kde_y[i]);
      model2.add(model_kde_x[i], correct_kde_y[i]);
    }

    MixtureModelProb = new float[NoBinPoints + 1][3];
    float positiveaccu = 0f;
    float negativeaccu = 0f;

    MixtureModelProb[0][0] = (float) model2.getMaxX() + Float.MIN_VALUE;
    MixtureModelProb[0][1] = 1f;
    MixtureModelProb[0][2] = 1f;

    double t = decoyEmpiricalDist.getMean();
    int tIdx = 0;

    for (int i = 1; i < NoBinPoints + 1; ++i) {
      double positiveNumber = correct_kde_y[NoBinPoints - i];
      double negativeNumber = decoy_kde_y[NoBinPoints - i];
      MixtureModelProb[i][0] = (float) model_kde_x[NoBinPoints - i];
      if (MixtureModelProb[i][0] > t) {
        tIdx = i;
      }
      positiveaccu += positiveNumber;
      negativeaccu += negativeNumber;
      if (negativeNumber + positiveNumber == 0) {
        MixtureModelProb[i][2] = 0;
      } else {
        MixtureModelProb[i][2] = 0.999999f * (float) (positiveNumber / (negativeNumber + positiveNumber));
      }
      if (negativeaccu + positiveaccu == 0) {
        MixtureModelProb[i][1] = 0;
      } else {
        MixtureModelProb[i][1] = 0.999999f * (positiveaccu / (negativeaccu + positiveaccu));
      }
    }

    // make local probability monotone in [tIdx, NoBinPoints]
    float lastProb = MixtureModelProb[tIdx][2];
    for (int i = tIdx + 1; i < NoBinPoints + 1; ++i) {
      lastProb = Math.min(lastProb, MixtureModelProb[i][2]);
      MixtureModelProb[i][2] = lastProb;
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(model1);
    dataset.addSeries(model2);

    HistogramDataset histogramDataset = new HistogramDataset();
    histogramDataset.setType(HistogramType.SCALE_AREA_TO_1);
    histogramDataset.addSeries("Target ions", targetObs, 100);
    histogramDataset.addSeries("Decoy ions", decoyObs, 100);
    histogramDataset.addSeries("Positive ions", positiveObs, 100);
    histogramDataset.addSeries("Negative ions", negativeObs, 100);

    JFreeChart chart = ChartFactory.createHistogram(runName, "Score", "Ions", histogramDataset, PlotOrientation.VERTICAL, true, false, false);
    XYPlot plot = chart.getXYPlot();

    NumberAxis domain = (NumberAxis) plot.getDomainAxis();
    domain.setRange(min, max);
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setForegroundAlpha(0.5f);
    chart.setBackgroundPaint(Color.white);

    XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();

    plot.setDataset(1, dataset);
    plot.setRenderer(1, render);
    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    ChartUtils.saveChartAsPNG(new File(fileNamePrefix + "_model.png"), chart, 1000, 600);
  }

  public void Modeling() throws Exception {
    GenerateInitialModel();
    p = new double[targetEmpiricalDist.getN()];
    f1 = new double[targetEmpiricalDist.getN()];
    f0 = new double[targetEmpiricalDist.getN()];
    //double[] plast = new double[TargetEmpiricalDist.getN()];
    double miniIdScore = targetEmpiricalDist.getMean() - 1.5 * targetEmpiricalDist.getStandardDeviation();
    double meanDecoy = decoyEmpiricalDist.getSampleMean();
    if (meanDecoy > miniIdScore) {
      miniIdScore = meanDecoy;
    }

    double targetLowIdCdf = targetEmpiricalDist.cdf(miniIdScore);
    double decoyLowIdCdf = decoyEmpiricalDist.cdf(miniIdScore);
    weight_incorrect = targetLowIdCdf / decoyLowIdCdf;
    weight_correct = 1 - weight_incorrect;

    //initialization
    GenerateIniCorrectDensity();
    GenerateDecoyDensity();
    pisum = 0d;

    int[] idxArray = new int[targetEmpiricalDist.getN()];
    for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
      int idx = Arrays.binarySearch(model_kde_x, targetEmpiricalDist.getObs(i));
      if (idx < 0) {
        idx = Math.max(0, -1 * idx -2);
      }
      idxArray[i] = idx;
    }

    for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
      f1[i] = inicorrect_kde_y[idxArray[i]];
      f0[i] = decoy_kde_y[idxArray[i]];
      //p[i] = f1[i] / TargetKDELookUp(TargetEmpiricalDist.getObs(i));
      if (f1[i] + f0[i] == 0) {
        p[i] = 0;
      } else {
        p[i] = f1[i] / (f1[i] + f0[i]);
      }
      pisum += p[i];
    }

    ///EM
    GenerateCorrectDensity();
    double likelihood = 0d;
    for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
      if (decoy_kde_y[idxArray[i]] + correct_kde_y[idxArray[i]] > 0) {
        likelihood += Math.log(decoy_kde_y[idxArray[i]] + correct_kde_y[idxArray[i]]);
      }
    }

    int iterations = 0;
    double logLikelihoodNew = likelihood;
    double logLikelihoodThreshold = Math.abs(logLikelihoodNew) * 0.00001;
    double logLikelihoodOld;

    do {

      logLikelihoodOld = logLikelihoodNew;

      // Update of iterations and log likelihood value
      Update(idxArray);
      ++iterations;
      logLikelihoodNew=0;
      for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
        if (decoy_kde_y[idxArray[i]] + correct_kde_y[idxArray[i]] > 0) {
          logLikelihoodNew += Math.log(decoy_kde_y[idxArray[i]] + correct_kde_y[idxArray[i]]);
        }
      }
    } while (Math.abs(logLikelihoodNew - logLikelihoodOld) > logLikelihoodThreshold && iterations < MAX_ITERATIONS);
    weight_correct = pisum / targetEmpiricalDist.getN();
    weight_incorrect = 1 - weight_correct;
    Update(idxArray);
    GenerateDecoyDensity();
  }

  private void Update(int[] idxArray) throws Exception {
    pisum=0d;
    for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
      f1[i] = correct_kde_y[idxArray[i]];
      f0[i] = decoy_kde_y[idxArray[i]];
      if (f1[i] + f0[i] == 0) {
        p[i] = 0;
      } else {
        p[i] = f1[i] / (f1[i] + f0[i]);
      }
      pisum += p[i];
    }
    GenerateCorrectDensity();
  }

  public void SetData(double[] targetdata, double[] decoydata, double[] positiveData, double[] negativeData) throws Exception {
    Arrays.parallelSort(targetdata);
    Arrays.parallelSort(decoydata);
    Arrays.parallelSort(positiveData);
    Arrays.parallelSort(negativeData);

    min = (float) Math.min(targetdata[0], decoydata[0]);
    max = (float) Math.max(targetdata[targetdata.length - 1], positiveData[positiveData.length - 1]);

    targetEmpiricalDist = new EmpiricalDist(targetdata);
    decoyEmpiricalDist = new EmpiricalDist(decoydata);
    positiveEmpiricalDist = new EmpiricalDist(positiveData);
    negativeEmpiricalDist = new EmpiricalDist(negativeData);

    //Silverman's ‘rule of thumb’ (Scott Variation uses factor = 1.06)
    bandWidth = 0.99 * Math.min(targetEmpiricalDist.getSampleStandardDeviation(), (targetEmpiricalDist.getInterQuartileRange() / 1.34)) / Math.pow(targetEmpiricalDist.getN(), 0.2);

    intv = (max - min) / NoBinPoints;
    //bandWidth=intv*5;
    model_kde_x = new double[NoBinPoints];
    for (int i = 0; i < NoBinPoints; ++i) {
      model_kde_x[i] = min + i * intv;
    }
    GenerateTargetDensity();
  }

  private void GenerateIniCorrectDensity() throws Exception {
    inicorrect_kde_y = new double[NoBinPoints];
    int multi = Math.min(nThreads * 8, NoBinPoints);
    ArrayList<Future<?>> futuresList = new ArrayList<>(multi);
    for (int i = 0; i < multi; ++i) {
      final int currentThread = i;
      futuresList.add(executorService.submit(() -> {
        int start = (int) ((currentThread * ((long) NoBinPoints)) / multi);
        int end = (int) (((currentThread + 1) * ((long) NoBinPoints)) / multi);
        for (int j = start; j < end; ++j) {
          PVector point = new PVector(2);
          point.array[0] = model_kde_x[j];
          inicorrect_kde_y[j] = mmc.EF.density(point, mmc.param[1]) * weight_correct;
        }
      }));
      for (Future<?> f : futuresList) {
        f.get();
      }
    }
  }

  private void GenerateCorrectDensity() throws Exception {
    correct_kde_y = new double[NoBinPoints];
    int multi = Math.min(nThreads * 8, NoBinPoints);
    ArrayList<Future<?>> futuresList = new ArrayList<>(multi);
    for (int i = 0; i < multi; ++i) {
      final int currentThread = i;
      futuresList.add(executorService.submit(() -> {
        int start = (int) ((currentThread * ((long) NoBinPoints)) / multi);
        int end = (int) (((currentThread + 1) * ((long) NoBinPoints)) / multi);
        for (int j = start; j < end; ++j) {
          correct_kde_y[j] = weight_correct * ProbBasedKDE(targetEmpiricalDist, model_kde_x[j]);
        }
      }));
    }
    for (Future<?> f : futuresList) {
      f.get();
    }
  }

  private void GenerateDecoyDensity() throws Exception {
    decoy_kde_y = new double[NoBinPoints];
    int multi = Math.min(nThreads * 8, NoBinPoints);
    ArrayList<Future<?>> futureList = new ArrayList<>(multi);
    for (int i = 0; i < multi; ++i) {
      final int currentThread = i;
      futureList.add(executorService.submit(() -> {
        int start = (int) ((currentThread * ((long) NoBinPoints)) / multi);
        int end = (int) (((currentThread + 1) * ((long) NoBinPoints)) / multi);
        for (int j = start; j < end; ++j) {
          decoy_kde_y[j] = weight_incorrect * KDE(decoyEmpiricalDist, model_kde_x[j]);
        }
      }));
    }
    for (Future<?> f : futureList) {
      f.get();
    }
  }

  private void GenerateTargetDensity() throws Exception {
    model_kde_y = new double[NoBinPoints];
    int multi = Math.min(nThreads * 8, NoBinPoints);
    ArrayList<Future<?>> futureList = new ArrayList<>(multi);
    for (int i = 0; i < multi; ++i) {
      final int currentThread = i;
      futureList.add(executorService.submit(() -> {
        int start = (int) ((currentThread * ((long) NoBinPoints)) / multi);
        int end = (int) (((currentThread + 1) * ((long) NoBinPoints)) / multi);
        for (int j = start; j < end; ++j) {
          model_kde_y[j] = KDE(targetEmpiricalDist, model_kde_x[j]);
        }
      }));
    }
    for (Future<?> f : futureList) {
      f.get();
    }
  }

  private void GenerateInitialModel() {
    PVector[] points = new PVector[targetEmpiricalDist.getN()];
    PVector[] centroids = new PVector[2];

    for (int i = 0; i < targetEmpiricalDist.getN(); ++i) {
      points[i] = new PVector(1);
      points[i].array[0] = targetEmpiricalDist.getObs(i);
    }

    centroids[0] = new PVector(1);
    centroids[0].array[0] = decoyEmpiricalDist.getMedian();
    centroids[1] = new PVector(1);
    centroids[1].array[0] = positiveEmpiricalDist.getMedian();
    Vector<PVector>[] clusters = KMeans.run(points, 2, centroids);
    MixtureModel mm = ExpectationMaximization1D.initialize(clusters);
    mmc = ExpectationMaximization1D.run(points, mm);
  }

  private double ProbBasedKDE(EmpiricalDist dist, double x) {
    // Computes and returns the kernel density estimate at $y$, where the
    // kernel is the density kern.density(x), and the bandwidth is $h$.
    double z;
    double a = kern.getXinf();       // lower limit of density
    double b = kern.getXsup();       // upper limit of density
    double sum = 0;
    int n = dist.getN();
    for (int i = 0; i < n; ++i) {
      z = (x - dist.getObs(i)) / bandWidth;
      if ((z >= a) && (z <= b)) {
        sum += kern.density(z) * p[i];
      }
    }
    sum /= (bandWidth * pisum);
    return sum;
  }

  private double KDE(EmpiricalDist dist, double y) {
    // Computes and returns the kernel density estimate at $y$, where the
    // kernel is the density kern.density(x), and the bandwidth is $h$.
    double z;
    double a = kern.getXinf();       // lower limit of density
    double b = kern.getXsup();       // upper limit of density
    double sum = 0;
    int n = dist.getN();
    for (int i = 0; i < n; ++i) {
      z = (y - dist.getObs(i)) / bandWidth;
      if ((z >= a) && (z <= b)) {
        sum += kern.density(z);
      }
    }
    sum /= (bandWidth * n);
    return sum;
  }
}

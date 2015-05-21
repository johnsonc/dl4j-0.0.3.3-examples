package org.deeplearning4j.rbm;


import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.conf.override.ClassifierOverride;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;


/**
 * Created by agibsonccc on 9/12/14.
 *
 * ? Output layer not a instance of output layer returning ?
 *
 */
public class MLPBackpropIrisExample {

    private static Logger log = LoggerFactory.getLogger(MLPBackpropIrisExample.class);

    public static void main(String[] args) throws IOException {
        // Customizing params
        Nd4j.MAX_SLICES_TO_PRINT = -1;
        Nd4j.MAX_ELEMENTS_PER_SLICE = -1;

        log.info("Load data....");
        DataSetIterator iter = new IrisDataSetIterator(150, 150);
        DataSet next = iter.next();
        Nd4j.writeTxt(next.getFeatureMatrix(), "iris.txt", "\t");
        next.normalizeZeroMeanZeroUnitVariance();

        log.info("Split data....");
        SplitTestAndTrain testAndTrain = next.splitTestAndTrain(110);
        DataSet train = testAndTrain.getTrain();
        DataSet test = testAndTrain.getTest();

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .layer(new RBM()).nIn(4).nOut(3)
                .visibleUnit(RBM.VisibleUnit.GAUSSIAN).hiddenUnit(RBM.HiddenUnit.RECTIFIED)
                .iterations(100).weightInit(WeightInit.DISTRIBUTION).dist(new UniformDistribution(0, 1))
                .activationFunction("tanh").k(1)
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-1f).momentum(0.9).regularization(true).l2(2e-4)
                .optimizationAlgo(OptimizationAlgorithm.LBFGS).constrainGradientToUnitNorm(true)
                .list(2).hiddenLayerSizes(new int[]{3})
                .backward(true)
                .override(new ClassifierOverride(1))
                .build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(1)));

        log.info("Train model....");
        model.fit(train);

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation();
        INDArray output = model.output(test.getFeatureMatrix());
        eval.eval(test.getLabels(),output);
        log.info(eval.stats());
    }
}

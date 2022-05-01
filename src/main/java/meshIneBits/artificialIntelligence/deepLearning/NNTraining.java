/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package meshIneBits.artificialIntelligence.deepLearning;

import meshIneBits.artificialIntelligence.AI_Tool;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

/**
 * Provides tools to train and save a neural network.
 */
public class NNTraining {
    /**
     * The number of iterations to train the neural network.
     */
    private static final int N_EPOCHS = 25000;
    /**
     * The number of labels for the outputs.
     * Here it is the position and the rotation.
     */
    private static final int CLASSES_COUNT = 2; // ! Do not change if the DataSet format has not changed
    /**
     * The number of parameters for the inputs.
     */
    private static final int FEATURES_COUNT = 20; // ! Do not change if the DataSet format has not changed
    /**
     * The number of neurons in a hidden layer.
     */
    public static final int HIDDEN_NEURONS_COUNT = 30; // This number could also be specified for each layer.

    private DataNormalization normalizer;
    private MultiLayerNetwork model;

    private DataSet trainingDataSet;
    private DataSet testDataSet;

    private boolean stop_training = false;
    private boolean is_training = false;

    public static void main(String[] args) {
        try {
            NNTraining nnTraining = new NNTraining();
            nnTraining.train(true);
            nnTraining.evaluateModel();
            try {
                nnTraining.save();
            } catch (IOException eSave) {
                System.out.println("Neural Network training params could not be saved !");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Trains the neural network with the dataSet
     */
    public NNTraining() {
        try {
            initDatasetsAndNormalizer();
            initNeuralNetwork();
        } catch (@NotNull IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Initialize the dataSet and normalize it.
     */
    private void initDatasetsAndNormalizer() throws IOException, InterruptedException {
        // 1) datasets
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(AI_Tool.DATASET_FILE_PATH)));
        DataSetIterator iter = new RecordReaderDataSetIterator(rr, getNumberOfExamples()/100, 0, 1, true); //debugOnly
        // 0 and 1 because our labels are columns 0 and 1
        DataSet fullDataSet = iter.next();
        fullDataSet.shuffle();


        // 3) normalizer
        /*normalizer = new NormalizerStandardize();
        normalizer.fitLabel(true);
        normalizer.fit(fullDataSet);
        normalizer.transform(fullDataSet); //todo @etienne
        iter.setPreProcessor(normalizer);
*/
        // 2) split data in two
        System.out.println("dataSet Size : " + fullDataSet.numExamples());
        SplitTestAndTrain testAndTrain = fullDataSet.splitTestAndTrain(fullDataSet.numExamples() - 1); // 100% for training and 0% for testing //todo @etienne
        trainingDataSet = testAndTrain.getTrain();
        testDataSet = testAndTrain.getTest();//todo see mini batches


    }

    /**
     * Initialize the neural network configuration.
     */
    private void initNeuralNetwork() {
        /*
         TODO: 2021-01-17
          - inquire about neural networks configuration and find the one that suit the best our application
          - test different nn configurations (activations, weightInits, updaters, l2 regularization, neurons count, layers count, backpropagation...)
          - propose to the user to configure nn hyper-parameters in meshineBits UI, and display the training score at the end
         */

        //The Neural Network configuration
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(12345)//todo remove after the tests
                .activation(Activation.RELU)
                //.weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.008))
                //.updater(new Nesterovs(0.01, 0.9)) //momentum should be <1
                .l2(1e-5)//change pas grand chose de changer la val ?
                //.weightDecay(1e-5)//change pas grand chose
                .weightInit(WeightInit.RELU) //bien pour regression LECUN_NORMAL
                .list()
//https://deeplearning4j.konduit.ai/deeplearning4j/how-to-guides/tuning-and-training/troubleshooting-training
                //Input Layer
                .layer(0, new DenseLayer.Builder().nIn(FEATURES_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                //Hidden Layers
                .layer(1, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(4, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(5, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(6, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(7, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(8, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(8, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                //Output Layer
                .layer(9, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)//MSE bien pour regression
                        .activation(Activation.IDENTITY) //do not change
                        .nIn(HIDDEN_NEURONS_COUNT)
                        .nOut(CLASSES_COUNT)
                        .build())

                .backpropType(BackpropType.Standard)
                .build();


        model = new MultiLayerNetwork(configuration);
        model.init();

    }

    /**
     * Launch a monitoring process either on the web UI or in the console.
     */
    private void startMonitoring() {
        UIServer uiServer = UIServer.getInstance();
        //Configures where the network information (gradients, score vs. time etc.) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        uiServer.attach(statsStorage);

        //Choose between console logs and UI logs.
        //model.setListeners(new ScoreIterationListener(100)); //logs console
        model.setListeners(new StatsListener(statsStorage)); //logs UI

        String VISUALIZE_TRAINING_LINK_STRING = "http://localhost:9000/train/overview";
        System.out.println("\nTo visualize the training, go to " + VISUALIZE_TRAINING_LINK_STRING + " in your browser");
        try {
            Desktop.getDesktop().browse(URI.create(VISUALIZE_TRAINING_LINK_STRING));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //todo @Andre can we suppress it ?
    public void evaluateModel() {
        INDArray features = testDataSet.getFeatures();
        INDArray labels = testDataSet.getLabels();
        INDArray prediction = model.output(features, false);
      //  normalizer.revert(testDataSet);
      //  normalizer.revertLabels(prediction);
        //todo @Andre print score sinon
        System.out.println("predictions : \n" + prediction + "\n\n labels : \n" + labels); //debugOnly
        System.out.println(model.score());
    }

    /**
     * Trains the neural network with the dataSet.
     *
     * @param enableMonitoring Launch the browser visualisation if set to true
     */
    public void train(boolean enableMonitoring) {
        if (enableMonitoring)
            startMonitoring();

        // training
        is_training = true;
        for (int i = 0; i < N_EPOCHS; i++) {
            model.fit(trainingDataSet);
            if (stop_training) {
                stop_training = false;
                break;
            }
        }
        is_training = false;
    }

    public void stop_training() { //todo @Etienne
        if (is_training)
            stop_training = true;
    }

    /**
     * Save the neural network configuration and normalizer.
     */
    public void save() throws IOException {
        //1) save model
        File locationToSave = new File(AI_Tool.MODEL_PATH); // where to save the model
        // write the model
        ModelSerializer.writeModel(model, locationToSave, false);

        // 2) save Normalizer
        NormalizerSerializer saver = NormalizerSerializer.getDefault();
        File normalsFile = new File(AI_Tool.NORMALIZER_PATH);
        saver.write(normalizer, normalsFile);

        System.out.println("The neural network parameters and configuration have been saved.");
    }

    /**
     * @return the number of lines of the dataSet file.
     */
    private int getNumberOfExamples() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(AI_Tool.DATASET_FILE_PATH));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }
}
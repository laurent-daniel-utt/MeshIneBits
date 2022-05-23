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

package meshIneBits.artificialIntelligence.borderPaver;

import meshIneBits.artificialIntelligence.AI_Tool;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;


@SuppressWarnings({"DuplicatedCode", "FieldCanBeLocal"})
public class Test_SumModel {
    //Random number generator seed, for reproducibility
    public static final int seed = 12345;
    //Number of epochs (full passes of the data)
    public static final int nEpochs = 10000;
    private static final int batchSize = 120;

    private static DataSet testDataSet;
    private static DataSet trainDataSet;
    private static MultiLayerNetwork net;
    private static DataNormalization normalizer;

    public static void main(String[] args) throws IOException, InterruptedException {

        //Create the network
        int numInput = 20;
        int numOutputs = 2;
        int nHidden = 30; //4.8 WITH 50.   5.1 with 60 OU 40. 4.7 with 90
        net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.RELU) //XAVIER or RELU with RELU/LEAKYRELU as activation function
                .activation(Activation.LEAKYRELU) //RELU ou LEAKYRELU
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.01, 0.95))
                .gradientNormalization(GradientNormalization.RenormalizeL2PerParamType)
                .list()

                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden).build())
                .layer(1, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden).build())
                .layer(2, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden).build())
                .layer(3, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden).build())
                .layer(4, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden).build())
                .layer(5, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden).build())
                .layer(6, new DenseLayer.Builder().nIn(nHidden).nOut(nHidden).build())
                .layer(7, new OutputLayer.Builder()
                        .activation(Activation.IDENTITY) //IDENTITY
                        .lossFunction(LossFunctions.LossFunction.MSE) //MSE
                        .nIn(nHidden).nOut(numOutputs).build())
                .backpropType(BackpropType.Standard)
                .setInputType(InputType.feedForward(10))
                .build()
        );
        net.init();
        net.setListeners(new ScoreIterationListener(100));

        startMonitoring();

        //Generate the training data
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(AI_Tool.DATASET_FILE_PATH)));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr, batchSize, 0, 1, true);
        //DataSet fullDataSet = iterator.next();
        //fullDataSet.shuffle();


       /* normalizer = new NormalizerStandardize();
        normalizer.fitLabel(true);
        normalizer.fit(fullDataSet);
        normalizer.transform(fullDataSet);
        iterator.setPreProcessor(normalizer);

        SplitTestAndTrain testAndTrain = fullDataSet.splitTestAndTrain(fullDataSet.numExamples()/10-1); //80% for training and 20% for testing
        testDataSet = testAndTrain.getTest();
        trainDataSet = testAndTrain.getTrain();
*/
        //Train the network on the full data set, and evaluate in periodically
        net.fit(iterator, nEpochs);

        DataSet fullDataSet = iterator.next();
        SplitTestAndTrain testAndTrain = fullDataSet.splitTestAndTrain(fullDataSet.numExamples() - 1); // 100% for training and 0% for testing //todo @etienne
        testDataSet = testAndTrain.getTest();

        INDArray features = testDataSet.getFeatures();
        INDArray out = net.output(features, false);
        INDArray labels = testDataSet.getLabels();
        // normalizer.revert(testDataSet);
        // normalizer.revertLabels(out);

        System.out.println("predictions : \n" + out + "\n labels : \n" + labels); //debugOnly
        System.out.println(net.score());
        save();
    }

    /**
     * Save the neural network configuration and normalizer.
     */
    public static void save() throws IOException {
        //1) save model
        File locationToSave = new File(AI_Tool.MODEL_PATH); // where to save the model
        // write the model
        ModelSerializer.writeModel(net, locationToSave, false);

        // 2) save Normalizer
        // NormalizerSerializer saver = NormalizerSerializer.getDefault();
        //File normalsFile = new File(AI_Tool.NORMALIZER_PATH);
        //saver.write(normalizer, normalsFile);

        System.out.println("The neural network parameters and configuration have been saved.");
    }

    /**
     * @return the number of lines of the dataSet file.
     */
    @SuppressWarnings("unused")
    private static int getNumberOfExamples() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(AI_Tool.DATASET_FILE_PATH));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }

    /**
     * Launch a monitoring process either on the web UI or in the console.
     */
    private static void startMonitoring() {
        UIServer uiServer = UIServer.getInstance();
        //Configures where the network information (gradients, score vs. time etc.) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        uiServer.attach(statsStorage);

        //Choose between console logs and UI logs.
        //model.setListeners(new ScoreIterationListener(100)); //logs console
        net.setListeners(new StatsListener(statsStorage)); //logs UI

        String VISUALIZE_TRAINING_LINK_STRING = "http://localhost:9000/train/overview";
        System.out.println("\nTo visualize the training, go to " + VISUALIZE_TRAINING_LINK_STRING + " in your browser");
        try {
            Desktop.getDesktop().browse(URI.create(VISUALIZE_TRAINING_LINK_STRING));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
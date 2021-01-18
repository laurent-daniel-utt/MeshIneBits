package meshIneBits.artificialIntelligence.deeplearning;

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
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NNTraining {
    /**
     * The number of neurons in an hidden layer
     */
    public static final int HIDDEN_NEURONS_COUNT = 20;
    /**
     * The number of labels for the outputs. Here it is the position and the rotation.
     */
    private static final int CLASSES_COUNT = 2;
    /**
     * The number of parameters for the inputs.
     */
    private static final int FEATURES_COUNT = 60;
    /**
     * The name and location of the csv file which contains the dataSet.
     */
    private static final String DATASET_PATH = "dataSet.csv";
    /**
     * The name and location where the trained model will be saved.
     */
    private static final String MODEL_PATH = "src/main/java/meshIneBits/artificialIntelligence/deeplearning/trained_model.zip";
    /**
     * The name and path where the data normalizer will be saved.
     */
    private static final String NORMALIZER_PATH = "src/main/java/meshIneBits/artificialIntelligence/deeplearning/normalizer_saved.bin";
    /**
     * The number of iterations to train the neural network.
     */
    private static final int N_EPOCHS = 10000;
    private DataNormalization normalizer;
    private MultiLayerNetwork model;

    private DataSet trainingDataSet;
    private DataSet testDataSet;


    /**
     * Trains the neural network with the dataSet
     */
    public NNTraining() {
        try {
            initDatasetsAndNormalizer();
            initNeuralNetwork();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void initDatasetsAndNormalizer() throws IOException, InterruptedException {
        // 1) datasets
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(DATASET_PATH)));
        DataSetIterator iter =new RecordReaderDataSetIterator(rr, this.getNumberOfExamples(), 0, 1, true); //debugonly
        // 0 and 1 because our labels are columns 0 and 1
        DataSet fullDataSet = iter.next();

        // 2) split data in two
        SplitTestAndTrain testAndTrain = fullDataSet.splitTestAndTrain(0.65); // 65% for training and 35% for testing
        this.trainingDataSet = testAndTrain.getTrain();
        this.testDataSet = testAndTrain.getTest();

        // 3) normalizer
        normalizer = new NormalizerStandardize();
        normalizer.fit(fullDataSet);
    }


    public void initNeuralNetwork() {
        /*
         TODO: 2021-01-17
          - inquire about neural networks configuration and find the one that suit the best our application
          - test different nn configurations (activations, weightInits, updaters, l2 regularization, neurons count, layers count, backpropagation...)
          - propose to the user to configure nn hyper-parameters in meshineBits UI, and display the training score at the end
         */

        //The Neural Network configuration
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.0001))
                .l2(1e-5)
                .list()

                //Input Layer
                .layer(0, new DenseLayer.Builder().nIn(FEATURES_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(4, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(6, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(7, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())
                .layer(8, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(Activation.RELU)
                        .build())

                //Output Layer
                .layer(9, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.RELU)
                        .nIn(HIDDEN_NEURONS_COUNT)
                        .nOut(CLASSES_COUNT)
                        .build())

                .backpropType(BackpropType.Standard)
                .build();


        this.model = new MultiLayerNetwork(configuration);
        this.model.init();

    }

    public void startMonitoring() {
        UIServer uiServer = UIServer.getInstance();
        //Configures where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        uiServer.attach(statsStorage);

        //Choice between console logs and UI logs.
        //model.setListeners(new ScoreIterationListener(100)); //logs console
        model.setListeners(new StatsListener(statsStorage)); //logs UI
        System.out.println("\nTo visualize the training, go to http://localhost:9000/train/overview in your browser");

    }


    public void evaluateModel() {
        INDArray features = testDataSet.getFeatures();
        INDArray labels = testDataSet.getLabels();
        INDArray prediction = model.output(features, false);
        normalizer.revert(testDataSet);
        normalizer.revertLabels(prediction);
        System.out.println("predictions : \n" + prediction + "\n\n labels : \n" + labels);
    }


    public void train(boolean enableMonitoring) {

        if (enableMonitoring) {
            startMonitoring();
        }

        // training
        this.normalizer.transform(trainingDataSet);
        for (int i = 0; i < N_EPOCHS; i++) {
            model.fit(trainingDataSet);
        }
    }


    public void save() throws IOException {
        //todo @Andre changer la destination du fichier zip
        // aussi changer les emplacements des fichiers csv stp
        // (à mettre dans un package resources dans le package deeplearning)
        //1) save model
        File locationToSave = new File(MODEL_PATH); // where to save the model
        // write the model
        ModelSerializer.writeModel(model, locationToSave, false);

        // 2) save Normalizer
        NormalizerSerializer saver = NormalizerSerializer.getDefault();
        File normalsFile = new File(NORMALIZER_PATH);
        saver.write(normalizer,normalsFile);

        System.out.println("The neural network parameters and configuration have been saved.");
    }

    public int getNumberOfExamples() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(DATASET_PATH));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }
}
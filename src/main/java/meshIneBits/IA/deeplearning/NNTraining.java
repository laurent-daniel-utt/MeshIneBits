package meshIneBits.IA.deeplearning;

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

import java.io.File;
import java.io.IOException;

public class NNTraining {
    public static final int BATCH_SIZE = 50;    //todo @ALL, on ne devrait plus l'indiquer, ca doit se faire en fonction du nombre de lignes du fichier csv
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
     * The number of iterations to train the neural network.
     */
    private static final int N_EPOCHS = 10000;
    private static final Activation ACTIVATION_FUNCTION = Activation.IDENTITY; //todo @Etienne virer
    private DataNormalization normalizer;
    private MultiLayerNetwork model;

    private DataSet trainingDataSet;
    private DataSet testDataSet;



    public NNTraining() {

        try {
            initDatasetsAndNormalizer();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        initNeuralNetwork();

    }


    private void initDatasetsAndNormalizer() throws IOException, InterruptedException {
        // 1) datasets
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(DATASET_PATH)));

        DataSetIterator iter = new RecordReaderDataSetIterator(rr, BATCH_SIZE, 0, 1, true);
        // 0 and 1 because our labels are columns 0 and 1
        DataSet fullDataSet = iter.next();

        SplitTestAndTrain testAndTrain = fullDataSet.splitTestAndTrain(0.65); // 65% for training and 35% for testing
        this.trainingDataSet = testAndTrain.getTrain();
        this.testDataSet = testAndTrain.getTest();

        // 2) normalizer
        normalizer = new NormalizerStandardize();
        normalizer.fit(fullDataSet);
    }


    public void initNeuralNetwork() {
        //todo @all tester les différentes configs et se renseigner pour trouver la meilleure
        //The Neural Network configuration
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .l2(1e-5)
                .list()

                //Input Layer
                .layer(0, new DenseLayer.Builder().nIn(FEATURES_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(4, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(5, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(6, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(7, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())
                .layer(8, new DenseLayer.Builder().nIn(HIDDEN_NEURONS_COUNT).nOut(HIDDEN_NEURONS_COUNT)
                        .activation(ACTIVATION_FUNCTION)
                        .build())

                //Output Layer
                .layer(9, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
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
        System.out.println("\n To visualize the training, go to http://localhost:9000/train/overview in your browser");

    }


    public void evaluateModel() {
        // todo a verifier
        INDArray features = testDataSet.getFeatures();
        INDArray labels = testDataSet.getLabels();
        INDArray prediction = model.output(features, false);
        normalizer.revert(testDataSet);
        normalizer.revertLabels(prediction);
        System.out.println("predictions : \n" + prediction + "\n\n labels : \n" + labels);

    }


    public void train(boolean enableMonitoring) throws IOException, InterruptedException {

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
        // 1) save model

        // where to save the model
        File locationToSave = new File("trained_model.zip");
        // boolean save Updater (set to true if you want to make further training on the model)
        boolean saveUpdater = false;
        // save the model
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);

        // 2) save Normalizer
        NormalizerSerializer saver = NormalizerSerializer.getDefault();
        File normalsFile = new File("normalizer_saved.bin");
        saver.write(normalizer,normalsFile);

        System.out.println("Everything is saved");
    }
}
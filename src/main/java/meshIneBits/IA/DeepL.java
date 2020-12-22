package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;
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
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class DeepL {
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
    private static final String PATH_NAME_TRAIN = "dataSet.csv";
    private static final String PATH_NAME_PREDICT = "dataToPredict.csv"; //todo @Etienne virer
    /**
     * The number of iterations to train the neural network.
     */
    private static final int N_EPOCHS = 10000;
    private static final Activation ACTIVATION_FUNCTION = Activation.IDENTITY; //todo @Etienne virer
    private static DataNormalization normalizer;
    private static MultiLayerNetwork model;

    //todo @Etienne main temporaire, enlever après
    public static void main(String[] args) {
        try {
            trainWithCsvDataSet();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a .csv file and returns the DataSetIterator
     *
     * @param filename       the file to read
     * @param labelIndexFrom the first index of the labels
     * @param labelIndexTo   the last index of the labels
     * @return the DataSetIterator to iterate over all data from the DataSet
     */
    private static DataSet readCSVDataset(String filename, int labelIndexFrom, int labelIndexTo) throws IOException, InterruptedException {
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(filename)));

        DataSetIterator iter = new RecordReaderDataSetIterator(rr, BATCH_SIZE, labelIndexFrom, labelIndexTo, true);
        return iter.next();
    }

    public static void trainWithCsvDataSet() throws IOException, InterruptedException {
        DataSet allData = readCSVDataset(PATH_NAME_TRAIN, 0, 1); //the first two columns of the .csv are the labels
        allData.shuffle(123);

        normalizer = new NormalizerStandardize();
        normalizer.fit(allData);
        normalizer.transform(allData);

        //todo @Andre à l'avenir, on est pas obligés de garder les 35% de test si?
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65); // 65% for training and 35% for testing
        DataSet trainingData = testAndTrain.getTrain();
        DataSet testingData = testAndTrain.getTest();

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


        model = new MultiLayerNetwork(configuration);
        model.init();


        UIServer uiServer = UIServer.getInstance();
        //Configures where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        uiServer.attach(statsStorage);

        //Choice between console logs and UI logs.
        //model.setListeners(new ScoreIterationListener(100)); //logs console
        model.setListeners(new StatsListener(statsStorage)); //logs UI
        System.out.println("\n To visualize the training, go to http://localhost:9000/train/overview in your browser");

        //the training
        for (int i = 0; i < N_EPOCHS; i++) {
            model.fit(trainingData);
        }

        INDArray features = testingData.getFeatures();
        INDArray labels = testingData.getLabels();
        INDArray prediction = model.output(features, false);
        normalizer.revert(testingData);
        normalizer.revertLabels(prediction);
        System.out.println("predictions : \n" + prediction + "\n\n labels : \n" + labels);
        // File locationToSave = new File("RegressionTestMeshineBits.zip");
        // boolean saveUpdater = true;
        // ModelSerializer.writeModel(model, locationToSave, saveUpdater);

        //System.out.print("Saved");
        //todo @Etienne sauvegarder le modèle pour pas avoir à réapprendre à chaque fois
    }


    /**
     * Once the neural network is trained, predicts the placement of a Bit2D from the sectionPoints.
     *
     * @param sectionPoints    the points on which the bit should be placed.
     * @param startPoint       the first point on which the bound of the bit will be placed.
     * @param angleLocalSystem the angle of the local coordinate system.
     * @return the bit, with predicted placement and orientation.
     */
    public static Bit2D getBitPlacement(Vector<Vector2> sectionPoints, Vector2 startPoint, double angleLocalSystem) throws IOException, InterruptedException {
        Vector<Vector2> transformedPoints = DataPreparation.getSectionInLocalCoordinateSystem(sectionPoints);
        Vector<Vector2> pointsForDl = DataPreparation.getInputPointsForDL(transformedPoints);
        System.out.println("SECTION POINTS SIZE " + pointsForDl.size());
        StringBuilder csvLine = new StringBuilder();
        csvLine.append("0,0,"); //todo @Etienne faire mieux
        boolean firstPoint = true;
        for (Vector2 point : pointsForDl) { // add points
            if (firstPoint) {
                csvLine.append(point.x);
                firstPoint = false;
            } else {
                csvLine.append(",").append(point.x);
            }
            csvLine.append(",").append(point.y);
        }
        try {
            FileWriter fw = new FileWriter(PATH_NAME_PREDICT);
            fw.write(csvLine + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//TEST N°1, avec un csv une ligne
        DataSet oneData = readCSVDataset(PATH_NAME_PREDICT, 0, 1);
        normalizer = new NormalizerStandardize();
        normalizer.fit(oneData);
        normalizer.transform(oneData);

        INDArray features = oneData.getFeatures();
        INDArray prediction = model.output(features, false);
        normalizer.revertLabels(prediction);
        System.out.println("prediction pos: " + prediction.getDouble(0));
        System.out.println("prediction angle: " + prediction.getDouble(1));

        double bitPos = prediction.getDouble(0);
        double bitAngle = prediction.getDouble(1);

        Bit2D bit = getBitFromNeuralNetworkOutput(bitPos, bitAngle, startPoint, angleLocalSystem);
        System.out.println("FINAL POSITION : " + bit.getOrigin().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().getEquivalentAngle2());
        DebugTools.A = bit.getOrigin();

//TEST N°2, avec un dataset en double[]
        double[][] featuresTab = new double[1][pointsForDl.size() * 2];
        int j = 0;
        for (Vector2 point : pointsForDl) {
            featuresTab[0][j] = point.x;
            featuresTab[0][j + 1] = point.y;
            j += 2;
        }
        DataSet oneData2 = new DataSet();
        INDArray features2 = Nd4j.create(featuresTab);
       /* oneData2.addFeatureVector(features2);
        normalizer = new NormalizerStandardize();
        normalizer.fit(oneData2);
        normalizer.transform(oneData2);
        features2 = oneData2.getFeatures();*/
        prediction = model.output(features2, false);
        normalizer.revertLabels(prediction);
        System.out.println("prediction pos: " + prediction.getDouble(0));
        System.out.println("prediction angle: " + prediction.getDouble(1));

        bitPos = prediction.getDouble(0);
        bitAngle = prediction.getDouble(1);

        bit = getBitFromNeuralNetworkOutput(bitPos, bitAngle, startPoint, angleLocalSystem);
        System.out.println("FINAL POSITION : " + bit.getOrigin().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().getEquivalentAngle2());
        DebugTools.A = bit.getOrigin();
        return bit;

    }


    /**
     * @param edgeAbscissa     position of a bit, that comes from the neural network's output
     * @param bitLocalAngle    angle of a bit, that comes from the neural network's output
     * @param posLocalSystem   position of the local coordinate system's origin used to prepare data for the neural network
     * @param angleLocalSystem angle of the local coordinate system used to prepare data for the neural network
     * @return the bit's center position in global coordinate system
     */
    public static Bit2D getBitFromNeuralNetworkOutput(double edgeAbscissa, double bitLocalAngle, Vector2 posLocalSystem, double angleLocalSystem) {

        // convert angles in Vector2
        Vector2 bitAngleLocalV2 = Vector2.getEquivalentVector(bitLocalAngle);
        Vector2 angleLocalSystemV2 = Vector2.getEquivalentVector(angleLocalSystem);

        // bit's colinear and orthogonal unit vectors computation
        Vector2 colinear = bitAngleLocalV2.normal();
        Vector2 orthogonal = colinear.rotate(new Vector2(0, -1)); // 90deg anticlockwise rotation

        // bit's center's position in local coordinate system
        Vector2 positionLocal = orthogonal.mul(edgeAbscissa)
                .add(colinear.mul(CraftConfig.bitLength / 2))
                .sub(orthogonal.mul(CraftConfig.bitWidth / 2));

        // bits center's position in global coordinate system
        Vector2 positionGlobal = positionLocal.rotate(angleLocalSystemV2).add(posLocalSystem);

        Vector2 orientationGlobal = bitAngleLocalV2.rotate(angleLocalSystemV2);

        return new Bit2D(positionGlobal, orientationGlobal);
    }
}
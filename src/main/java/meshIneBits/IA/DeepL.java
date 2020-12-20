package meshIneBits.IA;

import meshIneBits.Bit2D;
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
    public static final int BATCH_SIZE = 30;    //le nombre d'exemples
    public static final int HIDDEN_NEURONS_COUNT = 20; //le nombre de neurones dans une couche cachée
    private static final int CLASSES_COUNT = 2;  //le nombre de classes possibles en sortie
    private static final int FEATURES_COUNT = 60; //le nombre de paramètres en entrée
    private static final String PATH_NAME_TRAIN = "dataSet.csv";
    private static final String PATH_NAME_PREDICT = "dataToPredict.csv";
    private static final int N_EPOCHS = 10000;
    private static final Activation ACTIVATION_FUNCTION = Activation.IDENTITY;
    private static DataNormalization normalizer;
    private static MultiLayerNetwork model;

    public static void main(String[] args) {
        try {
            trainWithCsvDataSet();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static DataSet readCSVDataset(String filename, int labelIndexFrom, int labelIndexTo) throws IOException, InterruptedException {
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(filename)));

        DataSetIterator iter = new RecordReaderDataSetIterator(rr, BATCH_SIZE, labelIndexFrom, labelIndexTo, true);
        return iter.next();
    }

    public static void trainWithCsvDataSet() throws IOException, InterruptedException {
        DataSet allData = readCSVDataset(PATH_NAME_TRAIN, 0, 1);
        allData.shuffle(123);

        normalizer = new NormalizerStandardize();
        normalizer.fit(allData);
        normalizer.transform(allData);

        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);
        DataSet trainingData = testAndTrain.getTrain();
        DataSet testingData = testAndTrain.getTest();


       /* MultiLayerConfiguration netConf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS)

                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(FEATURES_COUNT)
                        .nOut(nHidden)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(nHidden)
                        .nOut(nHidden)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden)
                        .nOut(CLASSES_COUNT)
                        .build())
                .build();
*/

        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .activation(Activation.TANH)//todo c'est quoi tanh, tester d'autres
                .weightInit(WeightInit.XAVIER)//todo c'est quoi Xavier, tester d'autres
                .updater(new Adam(0.001))//todo c'est quoi Adam, tester d'autres
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


        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();
        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);
        model.setListeners(new StatsListener(statsStorage)); //Les logs UI

        //todo @André http://localhost:9000/train/overview : c'est l'adresse pour visualiser l'UI, tu peux enlever le todo, et laisser juste un commentaire quand t'as vu ^^
        // model.setListeners(new ScoreIterationListener(100)); //Les logs console

        for (int i = 0; i < N_EPOCHS; i++) {
            model.fit(trainingData);

            //      INDArray features = testingData.getFeatures();
            //      INDArray labels = testingData.getLabels();//todo do something with it
            //      INDArray predicted = model.output(features, false);
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
        //todo sauvegarder le modèle pour pas avoir à réapprendre à chaque fois
    }


    public static Bit2D getBitPlacement(Vector<Vector2> sectionPoints, Vector2 startPoint, double angleLocalSystem) throws IOException, InterruptedException {
        Vector<Vector2> transformedPoints = DataPreparation.getSectionInLocalCoordinateSystem(sectionPoints);
        Vector<Vector2> pointsForDl = DataPreparation.getInputPointsForDL(transformedPoints);
        System.out.println("SECTION POINTS SIZE " + pointsForDl.size());
        String csvLine = "";
        csvLine += "0,0,"; //todo @Etienne faire mieux
        boolean firstPoint = true;
        for (Vector2 point : pointsForDl) { // add points
            if (firstPoint) {
                csvLine += point.x;
                firstPoint = false;
            } else {
                csvLine += "," + point.x;
            }
            csvLine += "," + point.y;
        }
        try {
            FileWriter fw = new FileWriter(PATH_NAME_PREDICT);
            fw.write(csvLine + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        Bit2D bit = Exploitation.getBitFromNeuralNetworkOutput(bitPos, bitAngle, startPoint, angleLocalSystem);
        System.out.println("FINAL POSITION : " + bit.getOrigin().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().getEquivalentAngle2());
        DataPreparation.A = bit.getOrigin();

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

        bit = Exploitation.getBitFromNeuralNetworkOutput(bitPos, bitAngle, startPoint, angleLocalSystem);
        System.out.println("FINAL POSITION : " + bit.getOrigin().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().toString());
        System.out.println("FINAL ANGLE    : " + bit.getOrientation().getEquivalentAngle2());
        DataPreparation.A = bit.getOrigin();
        return bit;

    }
}
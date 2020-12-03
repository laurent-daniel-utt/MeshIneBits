package meshIneBits.IA;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class DeepL {
    private static final int CLASSES_COUNT = 3;  //le nombre de classes possibles en sortie
    private static final int FEATURES_COUNT = 4; //le nombre de paramètres en entrée
    public static final int BATCH_SIZE = 150;    //le nombre d'exemples

    public DeepL() {
        launch();
    }

    public void launch() {

        DataSet allData;
        try {
            RecordReader recordReader = new CSVRecordReader(0, ',');
            recordReader.initialize(new FileSplit(new ClassPathResource("iris.txt").getFile()));

            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, BATCH_SIZE, FEATURES_COUNT, CLASSES_COUNT);
            allData = iterator.next();
            allData.shuffle(42);

            DataNormalization normalizer = new NormalizerStandardize();
            normalizer.fit(allData);
            normalizer.transform(allData);

            SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);
            DataSet trainingData = testAndTrain.getTrain();
            DataSet testData = testAndTrain.getTest();


            MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                    .seed(123)
                    .updater(new AdaGrad())
                    .activation(Activation.TANH)
                    .weightInit(WeightInit.XAVIER)
                    .list()
                    .layer(0, new DenseLayer.Builder()
                            .nIn(FEATURES_COUNT)
                            .nOut(3)
                            .build())
                    .layer(1, new DenseLayer.Builder()
                            .nIn(3)
                            .nOut(3)
                            .build())
                    .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .activation(Activation.SOFTMAX)
                            .nIn(3)
                            .nOut(CLASSES_COUNT)
                            .build())
                    .build();

            MultiLayerNetwork model = new MultiLayerNetwork(configuration);
            model.init();
            model.fit(trainingData);

            INDArray output = model.output(testData.getFeatures());

            Evaluation eval = new Evaluation(CLASSES_COUNT);
            eval.eval(testData.getLabels(), output);
            System.out.println(eval.stats());

        }
        catch (Exception e) {

        }

    }

}
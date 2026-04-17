package meshIneBits.qa;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * QA utility to validate exported batch XML integrity against expected software rules.
 *
 * <p>Validations:
 * 1) layer-height is non-decreasing in each batch;
 * 2) each bit contains exactly one direct sub-bit with a valid direct id child;
 * 3) in each cutting block, any chute/drop cut-paths must be the last cut-paths entry;
 * 4) prints summary per batch.
 */
public final class BatchXmlIntegrityValidator {

  private static final List<String> DEFAULT_BATCHES = Arrays.asList(
      "C:\\Dev\\MeshIneBits\\Batch 0.xml",
      "C:\\Dev\\MeshIneBits\\Batch 1.xml",
      "C:\\Dev\\MeshIneBits\\Batch 2.xml",
      "C:\\Dev\\MeshIneBits\\Batch 3.xml"
  );

  private BatchXmlIntegrityValidator() {
  }

  public static void main(String[] args) throws Exception {
    List<String> paths = args == null || args.length == 0
        ? DEFAULT_BATCHES
        : Arrays.asList(args);

    int globalBits = 0;
    int globalLayers = 0;
    int globalBatches = 0;
    List<String> allErrors = new ArrayList<>();

    for (String path : paths) {
      BatchResult result = validateOneBatch(path);
      globalBatches++;
      globalBits += result.verifiedBits;
      globalLayers += result.detectedLayers;
      if (!result.errors.isEmpty()) {
        allErrors.addAll(result.errors);
      }

      String status = result.errors.isEmpty() ? "OK" : "FAILED";
      System.out.println(
          "Batch " + result.batchLabel + " : " + result.verifiedBits + " bits verifies, "
              + result.detectedLayers + " couches detectees. Integrite : " + status);
    }

    if (!allErrors.isEmpty()) {
      System.out.println();
      System.out.println("---- DETAILS DES ECHECS D'INTEGRITE ----");
      for (String error : allErrors) {
        System.out.println(error);
      }
      throw new IllegalStateException(
          "Validation XML echouee sur " + allErrors.size() + " point(s).");
    }

    System.out.println();
    System.out.println("Validation terminee: " + globalBatches + " batch(es), "
        + globalLayers + " couche(s), " + globalBits + " bit(s) verifies.");
  }

  private static BatchResult validateOneBatch(String xmlPath) throws Exception {
    File file = new File(xmlPath);
    BatchResult result = new BatchResult(file.getName());

    if (!file.exists()) {
      result.errors.add("[" + result.batchLabel + "] Fichier introuvable: " + xmlPath);
      return result;
    }

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(file);
    document.getDocumentElement().normalize();

    Element mesh = document.getDocumentElement();
    if (mesh == null || !"mesh".equals(mesh.getTagName())) {
      result.errors.add("[" + result.batchLabel + "] Racine XML invalide (attendu: <mesh>).");
      return result;
    }

    NodeList layerNodes = mesh.getElementsByTagName("layer");
    result.detectedLayers = layerNodes.getLength();

    validateLayerHeights(result, layerNodes);
    validateBitsAndCuttingIntegrity(result, layerNodes);
    return result;
  }

  private static void validateLayerHeights(BatchResult result, NodeList layerNodes) {
    double prev = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < layerNodes.getLength(); i++) {
      Element layer = asElement(layerNodes.item(i));
      if (layer == null) {
        result.errors.add("[" + result.batchLabel + "] Layer #" + i + " invalide.");
        continue;
      }
      Element h = getDirectChild(layer, "layer-height");
      if (h == null) {
        result.errors.add("[" + result.batchLabel + "] layer-height manquant au layer #" + i + ".");
        continue;
      }
      double current;
      try {
        current = Double.parseDouble(h.getTextContent().trim());
      } catch (NumberFormatException nfe) {
        result.errors.add("[" + result.batchLabel + "] layer-height non numerique au layer #" + i
            + " : '" + h.getTextContent() + "'");
        continue;
      }
      if (current < prev) {
        result.errors.add("[" + result.batchLabel + "] layer-height non croissant au layer #" + i
            + " (" + current + " < " + prev + ").");
      }
      prev = current;
    }
  }

  private static void validateBitsAndCuttingIntegrity(BatchResult result, NodeList layerNodes) {
    for (int li = 0; li < layerNodes.getLength(); li++) {
      Element layer = asElement(layerNodes.item(li));
      if (layer == null) {
        continue;
      }
      List<Element> bits = getDirectChildren(layer, "bit");
      for (Element bit : bits) {
        result.verifiedBits++;
        String bitIdLabel = getBitIdLabel(bit);

        // Rule #2: exactly one direct <sub-bit> and valid direct <id>
        List<Element> subBits = getDirectChildren(bit, "sub-bit");
        if (subBits.size() != 1) {
          result.errors.add("[" + result.batchLabel + "][bit " + bitIdLabel
              + "] nombre de <sub-bit> invalide: attendu=1, trouve=" + subBits.size() + ".");
        } else {
          Element subId = getDirectChild(subBits.get(0), "id");
          if (subId == null) {
            result.errors.add("[" + result.batchLabel + "][bit " + bitIdLabel
                + "] <sub-bit><id> manquant.");
          } else {
            String text = subId.getTextContent() == null ? "" : subId.getTextContent().trim();
            try {
              int id = Integer.parseInt(text);
              if (id <= 0) {
                result.errors.add("[" + result.batchLabel + "][bit " + bitIdLabel
                    + "] <sub-bit><id> invalide (<=0): " + id + ".");
              }
            } catch (NumberFormatException nfe) {
              result.errors.add("[" + result.batchLabel + "][bit " + bitIdLabel
                  + "] <sub-bit><id> non numerique: '" + text + "'.");
            }
          }
        }

        // Rule #3: any cut-path tagged chute/drop must be last cut-paths in cutting
        Element cutting = getDirectChild(bit, "cutting");
        if (cutting != null) {
          List<Element> cutPaths = getDirectChildren(cutting, "cut-paths");
          validateCutPathOrdering(result, bitIdLabel, cutPaths);
        }
      }
    }
  }

  private static void validateCutPathOrdering(BatchResult result, String bitIdLabel,
      List<Element> cutPaths) {
    if (cutPaths.isEmpty()) {
      return;
    }
    int lastIndex = cutPaths.size() - 1;
    for (int i = 0; i < cutPaths.size(); i++) {
      Element cp = cutPaths.get(i);
      boolean hasDrop = getDirectChild(cp, "drop") != null;
      boolean hasChuteTag = false;
      for (Element fallType : getDirectChildren(cp, "fall-type")) {
        String value = fallType.getTextContent() == null ? "" : fallType.getTextContent().trim();
        if ("chute".equalsIgnoreCase(value)) {
          hasChuteTag = true;
          break;
        }
      }
      if ((hasDrop || hasChuteTag) && i != lastIndex) {
        result.errors.add("[" + result.batchLabel + "][bit " + bitIdLabel
            + "] regle chute/drop violee: cut-path index " + i
            + " marque chute/drop mais non dernier (dernier index=" + lastIndex + ").");
      }
    }
  }

  private static String getBitIdLabel(Element bit) {
    Element id = getDirectChild(bit, "id-bit");
    return id == null ? "?" : id.getTextContent().trim();
  }

  private static Element asElement(Node node) {
    return node instanceof Element ? (Element) node : null;
  }

  private static Element getDirectChild(Element parent, String tagName) {
    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
        return (Element) child;
      }
    }
    return null;
  }

  private static List<Element> getDirectChildren(Element parent, String tagName) {
    List<Element> out = new ArrayList<>();
    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
        out.add((Element) child);
      }
    }
    return out;
  }

  private static final class BatchResult {
    private final String batchLabel;
    private int verifiedBits;
    private int detectedLayers;
    private final List<String> errors = new ArrayList<>();

    private BatchResult(String batchLabel) {
      this.batchLabel = batchLabel;
    }
  }
}

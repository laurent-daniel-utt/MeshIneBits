package patternTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.UnitSquarePattern;

public class UnitSquarePatternIntegrityTest extends PatternIntegrityTest {

	static {
		logger = meshIneBits.util.Logger.createSimpleInstanceFor(UnitSquarePatternIntegrityTest.class);
	}

	@Override
	@BeforeEach
	protected void setUp() {
		pattern = new UnitSquarePattern();
		UnitSquarePattern p = (UnitSquarePattern) pattern;
		logger.info("applyQuickRegroup=true");
		p.setApplyQuickRegroup(true);
		logger.info("limitAction=10000");
		p.setLimitAction(10000);
		CraftConfig.templateChoice = pattern;
	}

	@Override
	protected void checkSlicedPart() {
		// Nothing to check
	}

	@Override
	protected void checkGeneratedPart() {
		for (Layer layer : part.getLayers()) {
			// Assure each layer is empty
			assertEquals(0, layer.getSelectedPattern().getBitsKeys().size(),
					"Layer " + layer.getLayerNumber() + " should be empty");
		}
	}
}
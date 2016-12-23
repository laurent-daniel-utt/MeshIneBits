package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import meshIneBits.MeshIneBitsMain;
import meshIneBits.Slicer.Config.CraftConfig;
import meshIneBits.Slicer.Config.CraftConfigLoader;

public class Ribbon extends JTabbedPane implements Observer {
	private static final long serialVersionUID = -1759701286071368808L;
	ViewObservable sv;

	public Ribbon() {
		setFont(new Font(this.getFont().toString(), Font.PLAIN, 15));
		addTab("File", new JPanel());
		addTab("Slicer", new JScrollPane(new SlicerTab()));
		addTab("Template", new JScrollPane(new TemplateTab()));
		addTab("Review", new JScrollPane(new ReviewTab()));
		//addTab("Export", new JScrollPane(new ExportTab()));
		//addTab("Help", new JScrollPane(new HelpTab()));
		//addTab("Advanced", new JScrollPane(new AdvancedTab()));

		Ribbon.this.setSelectedIndex(1);

		JButton fileMenuBtn = new FileMenuButton();
		this.setTabComponentAt(0, fileMenuBtn);
		this.setEnabledAt(0, false);		

		//		addChangeListener(new ChangeListener() {
		//			@Override
		//			public void stateChanged(ChangeEvent e) {
		//				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
		//				int index = sourceTabbedPane.getSelectedIndex();
		//				if(sourceTabbedPane.getTitleAt(index) == "Export"){
		//					Ribbon.this.setSelectedIndex(1);
		//					final JFileChooser fc = new JFileChooser();
		//					fc.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
		//					int returnVal = fc.showSaveDialog(null);
		//
		//					if (returnVal == JFileChooser.APPROVE_OPTION) {
		//						//XmlTool xt = new XmlTool(part, fc.getSelectedFile());
		//						//xt.writeXmlCode();
		//					}
		//				}
		//				else if(sourceTabbedPane.getTitleAt(index) == "Help"){
		//					Ribbon.this.setSelectedIndex(1);
		//					JOptionPane.showMessageDialog(null, "For any help call your mother. \nMeshineBits has been made in 2016 by Thibault Cassard & Nicolas Gouju.", "Help", JOptionPane.PLAIN_MESSAGE);
		//				}
		//			}
		//		});
	}

	@Override
	public void update(Observable sv, Object arg) {
		this.sv = (ViewObservable) sv;	
		revalidate();
		repaint();
	}

	private class FileMenuButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5613899244422633632L;

		public FileMenuButton() {
			ImageIcon icon = new ImageIcon(
					new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "bars.png")).getImage().getScaledInstance(24, 24, Image.SCALE_REPLICATE));
			this.setIcon(icon);

			this.setContentAreaFilled (false);
			//			fileMenuBtn.setOpaque (false);
			this.setBorder (null);	
			//			fileMenuBtn.setFocusPainted (false);

			JPopupMenu filePopup = new JPopupMenu();
			filePopup.add(new JMenuItem("Open"));
			filePopup.add(new JMenuItem("Save"));
			filePopup.add(new JMenuItem("Export"));
			filePopup.add(new JMenuItem("Help"));

			this.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					filePopup.show(null, FileMenuButton.this.getLocationOnScreen().x -5, FileMenuButton.this.getLocationOnScreen().y + 25);
				}
			});
		}
	}

	//	private class AdvancedTab extends RibbonTab {
	//		public AdvancedTab() {
	//			super();
	//			OptionsContainer optionsCont = new OptionsContainer("Advanced Options");
	//			optionsCont.add(new LabeledSpinner("Min % machin :  ", 0, 0, 360, 22.5));
	//			optionsCont.add(new LabeledSpinner("suckerDiameter :  ", 0, 0, 360, 22.5));
	//			optionsCont.add(new LabeledSpinner("Layer to selected truc :  ", 0, 0, 360, 22.5));
	//
	//			add(optionsCont);
	//			add(new TabContainerSeparator());
	//		}
	//	}

	private class TemplateTab extends RibbonTab {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2963705108403089250L;

		public TemplateTab() {
			super();

			OptionsContainer bitsCont = new OptionsContainer("Bits options");
			LabeledSpinner bitThicknessSpinner = new LabeledSpinner("Bit thickness (mm) :  ", CraftConfig.bitThickness, 0, 999, 1);
			LabeledSpinner bitWidthSpinner = new LabeledSpinner("Bit width (mm) :  ", CraftConfig.bitWidth, 0, 999, 1);
			LabeledSpinner bitLengthSpinner = new LabeledSpinner("Bit length (mm) :  ", CraftConfig.bitLength, 0, 999, 1);
			bitsCont.add(bitThicknessSpinner);
			bitsCont.add(bitWidthSpinner);
			bitsCont.add(bitLengthSpinner);

			GalleryContainer patternGallery = new GalleryContainer("Pattern");
			JToggleButton pattern1Btn = new JToggleButton();
			JToggleButton pattern2Btn = new JToggleButton();
			if (CraftConfig.patternNumber == 1) {
				pattern1Btn.setSelected(true);
			} else {
				pattern2Btn.setSelected(true);
			}
			patternGallery.addButton(pattern1Btn, "p1.png");
			patternGallery.addButton(pattern2Btn, "p2.png");

			OptionsContainer patternCont = new OptionsContainer("Template options");
			LabeledSpinner rotationSpinner = new LabeledSpinner("Rotation (°) :  ", CraftConfig.rotation, 0, 360, 22.5);
			LabeledSpinner xOffsetSpinner = new LabeledSpinner("X Offset (mm) :  ", CraftConfig.xOffset, -999, 999, 1);
			LabeledSpinner bitsOffsetSpinner = new LabeledSpinner("Offset btwn bits (mm) :  ", CraftConfig.bitsOffset, 0, 999, 1);
			LabeledSpinner yOffsetSpinner = new LabeledSpinner("Y Offset (mm) :  ", CraftConfig.yOffset, -999, 999, 1);
			LabeledSpinner layersOffsetSpinner = new LabeledSpinner("Offset btwn layers (mm) :  ", CraftConfig.layersOffset, 0, 999, 1);
			patternCont.add(rotationSpinner);
			patternCont.add(xOffsetSpinner);
			patternCont.add(bitsOffsetSpinner);
			patternCont.add(yOffsetSpinner);
			patternCont.add(layersOffsetSpinner);

			OptionsContainer computeCont = new OptionsContainer("Compute");
			JButton computeBtn = new ButtonIcon("Generate layers", "cog.png");
			LabeledSpinner minPercentageOfSlicesSpinner = new LabeledSpinner("Min % of slices in a bit3D :  ", CraftConfig.minPercentageOfSlices, 0, 100, 1);
			LabeledSpinner defaultSliceToSelectSpinner = new LabeledSpinner("Default slice to select (%) :  ", CraftConfig.defaultSliceToSelect, 0, 100, 1);
			computeCont.add(minPercentageOfSlicesSpinner);
			computeCont.add(defaultSliceToSelectSpinner);
			computeCont.add(computeBtn);

			add(bitsCont);
			add(new TabContainerSeparator());
			add(patternGallery);
			add(new TabContainerSeparator());
			add(patternCont);
			add(new TabContainerSeparator());
			add(computeCont);

			addConfigSpinnerChangeListener(bitThicknessSpinner, "bitThickness");
			addConfigSpinnerChangeListener(bitWidthSpinner, "bitWidth");
			addConfigSpinnerChangeListener(bitLengthSpinner, "bitLength");
			addConfigSpinnerChangeListener(rotationSpinner, "rotation");
			addConfigSpinnerChangeListener(xOffsetSpinner, "xOffset");
			addConfigSpinnerChangeListener(bitsOffsetSpinner, "bitsOffset");
			addConfigSpinnerChangeListener(yOffsetSpinner, "yOffset");
			addConfigSpinnerChangeListener(layersOffsetSpinner, "layersOffset");
			addConfigSpinnerChangeListener(minPercentageOfSlicesSpinner, "minPercentageOfSlices");
			addConfigSpinnerChangeListener(defaultSliceToSelectSpinner, "defaultSliceToSelect");

			pattern1Btn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (pattern1Btn.isSelected()) {
						pattern2Btn.setSelected(false);
						CraftConfig.patternNumber = 1;
					} else {
						pattern2Btn.setSelected(true);
						CraftConfig.patternNumber = 2;
					}
				}
			});

			pattern2Btn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (pattern2Btn.isSelected()) {
						pattern1Btn.setSelected(false);
						CraftConfig.patternNumber = 2;
					} else {
						pattern1Btn.setSelected(true);
						CraftConfig.patternNumber = 1;
					}
				}
			});


		}
	}

	//	private class ExportTab extends RibbonTab {
	//		public ExportTab() {
	//			super();
	//		}
	//	}
	//
	//	private class HelpTab extends RibbonTab {
	//		public HelpTab() {
	//			super();
	//		}
	//	}

	private class ButtonIcon extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4439705350058229259L;

		public ButtonIcon(String label, String iconName) {
			super(label);
			try {
				ImageIcon icon = new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName)).getImage().getScaledInstance(22, 22, Image.SCALE_DEFAULT));
				this.setIcon(icon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.setHorizontalAlignment(LEFT);
			this.setMargin(new Insets(0, 0, 0, 2));
		}
	}

	private class GalleryContainer extends OptionsContainer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5081506030712556983L;

		public GalleryContainer(String title) {
			super(title);
			this.setLayout(new GridLayout(1, 2, 3, 3));
		}

		public void addButton(JToggleButton btn, String iconName) {
			Icon icon = new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName));
			btn.setIcon(icon);
			btn.setPreferredSize(new Dimension(60, 60));
			this.add(btn);
		}
	}

	private class LabeledSpinner extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6726754934854914029L;
		private JSpinner spinner;

		public LabeledSpinner(String label, double defaultValue, double minValue, double maxValue, double step) {
			this.setLayout(new BorderLayout());
			this.setBackground(Color.WHITE);
			this.add(new JLabel(label), BorderLayout.WEST);
			spinner = new JSpinner(new SpinnerNumberModel(defaultValue, minValue, maxValue, step));
			this.add(spinner, BorderLayout.EAST);
			this.setBorder(new EmptyBorder(4, 0, 0, 0));
		}

		public void addChangeListener(ChangeListener listener) {
			spinner.addChangeListener(listener);
		}

		public Double getValue() {
			return (Double) spinner.getValue();
		}
	}

	private class OptionsContainer extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 136154266552080732L;

		public OptionsContainer(String title) {
			this.setLayout(new GridLayout(3, 0, 3, 3));
			this.setBackground(Color.WHITE);
			TitledBorder centerBorder = BorderFactory.createTitledBorder(title);
			centerBorder.setTitleJustification(TitledBorder.CENTER);
			centerBorder.setTitleFont(new Font(this.getFont().toString(), Font.BOLD, 12));
			centerBorder.setTitleColor(Color.gray);
			centerBorder.setBorder(BorderFactory.createEmptyBorder());
			this.setBorder(centerBorder);
			this.setMinimumSize(new Dimension(500, 500));
		}

		@Override
		public int getBaseline(int width, int height) {
			return 0;
		}

		@Override
		public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
			return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
		}
	}

	private class ReviewTab extends RibbonTab {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6062849183461607573L;

		public ReviewTab() {
			super();
			OptionsContainer layerCont = new OptionsContainer("Review layers");
			layerCont.add(new JButton("Show next issue"));
			layerCont.add(new JButton("Show prev issue"));

			add(layerCont);
			add(new TabContainerSeparator());

			OptionsContainer modifCont = new OptionsContainer("Pattern modifications");
			JButton removeBitBtn = new JButton("Remove bit");
			JButton replaceBitBtn1 = new JButton("Replace bit 1");
			JButton replaceBitBtn2 = new JButton("Replace bit 2");
			modifCont.add(removeBitBtn);
			modifCont.add(replaceBitBtn1);
			modifCont.add(replaceBitBtn2);

			add(modifCont);
			add(new TabContainerSeparator());

			removeBitBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

				}
			});
		}
	}

	private class RibbonTab extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5540398663631111329L;

		public RibbonTab() {
			FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 10, 0);
			layout.setAlignOnBaseline(true);
			this.setLayout(layout);
			this.setBackground(Color.WHITE);
			this.setFocusable(false);
		}
	}

	private class SlicerTab extends RibbonTab {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2435250564072409684L;
		private File file = null;

		public SlicerTab() {
			super();

			OptionsContainer fileCont = new OptionsContainer("File");
			JButton newBtn = new ButtonIcon("Open", "file-o.png");
			JButton saveBtn = new ButtonIcon("Save", "save.png");
			JButton closeBtn = new ButtonIcon("Close", "times.png");

			fileCont.add(newBtn);
			fileCont.add(saveBtn);
			fileCont.add(closeBtn);

			OptionsContainer slicerCont = new OptionsContainer("Slicer options");
			LabeledSpinner sliceHeightSpinner = new LabeledSpinner("Slice height (mm) :  ", CraftConfig.sliceHeight, 0, 999, 0.1);
			LabeledSpinner firstSliceHeightPercentSpinner = new LabeledSpinner("First slice height (%) :  ", CraftConfig.firstSliceHeightPercent, 0, 999, 10);
			slicerCont.add(sliceHeightSpinner);
			slicerCont.add(firstSliceHeightPercentSpinner);

			OptionsContainer modelOrientationCont = new OptionsContainer("model Orientation");
			JButton editOrientationBtn = new ButtonIcon("  Edit orientation  ", "rotate-left.png");
			modelOrientationCont.add(editOrientationBtn);

			OptionsContainer computeCont = new OptionsContainer("Compute");
			JLabel fileSelectedLabel = new JLabel("No file selected");
			JButton computeBtn = new ButtonIcon("Slice model", "align-center.png");
			computeCont.add(fileSelectedLabel);
			computeCont.add(computeBtn);

			add(fileCont);
			add(new TabContainerSeparator());
			add(slicerCont);
			add(new TabContainerSeparator());
			add(modelOrientationCont);
			add(new TabContainerSeparator());			
			add(computeCont);

			addConfigSpinnerChangeListener(sliceHeightSpinner, "sliceHeight");
			addConfigSpinnerChangeListener(firstSliceHeightPercentSpinner, "firstSliceHeightPercent");




			newBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JFileChooser fc = new JFileChooser();
					fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
					fc.setSelectedFile(new File(CraftConfig.lastSlicedFile));
					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						fileSelectedLabel.setText(fc.getSelectedFile().getName());
						setFile(fc.getSelectedFile());
					}
				}
			});

			saveBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CraftConfigLoader.saveConfig(null);
				}
			});

			closeBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					sv.setPart(null);
				}
			});

			computeBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (file != null) {
						try {
							MeshIneBitsMain.sliceModel(file.toString());
							System.out.println("oui");
						} catch (Exception e1) {
							e1.printStackTrace();
							StringBuilder sb = new StringBuilder();
							sb.append(e1.toString());
							sb.append("\n");
							for (StackTraceElement el : e1.getStackTrace()) {
								sb.append(el.toString());
								sb.append("\n");
							}
							JOptionPane.showMessageDialog(null, sb, "Exception", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

		}	

		private void setFile(File file) {
			this.file = file;
		}
	}

	private class TabContainerSeparator extends JSeparator {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7739612020735334296L;

		public TabContainerSeparator() {
			this.setOrientation(SwingConstants.VERTICAL);
			Dimension d = this.getPreferredSize();
			d.height = 105;
			this.setPreferredSize(d);
		}
	}

	private void addConfigSpinnerChangeListener(LabeledSpinner spinner, String configFieldName) {
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				try {
					Field f = CraftConfig.class.getField(configFieldName);
					f.setDouble(null, spinner.getValue());
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}
}
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.MeshIneBitsMain;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Logger;
import meshIneBits.util.XmlTool;

public class Ribbon extends JTabbedPane implements Observer {
	private static final long serialVersionUID = -1759701286071368808L;
	private ViewObservable viewObservable;
	private File file = null;
	JLabel fileSelectedLabel;
	JButton computeSlicesBtn;

	public Ribbon() {
		viewObservable = ViewObservable.getInstance();
		
		setFont(new Font(this.getFont().toString(), Font.PLAIN, 15));
		addTab("File", new JPanel());
		addTab("Slicer", new JScrollPane(new SlicerTab()));
		addTab("Template", new JScrollPane(new TemplateTab()));
		addTab("Review", new JScrollPane(new ReviewTab()));

		Ribbon.this.setSelectedIndex(1);
		setEnabledAt(3, false);

		FileMenuButton fileMenuBtn = new FileMenuButton();
		this.setTabComponentAt(0, fileMenuBtn);
		this.setEnabledAt(0, false);		
	}

	@Override
	public void update(Observable o, Object arg) {
		if(viewObservable.getCurrentPart() != null && viewObservable.getCurrentPart().isGenerated()){
			this.setEnabledAt(3, true);
			//Ribbon.this.setSelectedIndex(3);
		}
		else if(viewObservable.getCurrentPart() == null || !viewObservable.getCurrentPart().isGenerated()){
			this.setEnabledAt(3, false);
			Ribbon.this.setSelectedIndex(1);
		}
		
		if(viewObservable.getCurrentPart() != null && viewObservable.getCurrentPart().isSliced())
			computeSlicesBtn.setEnabled(true);
		
		revalidate();
		repaint();
	}

	private class FileMenuButton extends JToggleButton {
		private static final long serialVersionUID = 5613899244422633632L;

		public FileMenuButton() {
			this.setFocusable(false);
			ImageIcon icon = new ImageIcon(
					new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "bars.png")).getImage().getScaledInstance(24, 24, Image.SCALE_REPLICATE));
			this.setIcon(icon);

			this.setContentAreaFilled (false);
			//			fileMenuBtn.setOpaque (false);
			this.setBorder (null);	
			//			fileMenuBtn.setFocusPainted (false);

			FileMenuPopUp filePopup = new FileMenuPopUp();	

			this.addActionListener(new ActionListener() {
				
				@Override 
				public void actionPerformed(ActionEvent ev) {
	                    JToggleButton b = FileMenuButton.this;
	                    if (b.isSelected()) {
	                    	filePopup.show(null, FileMenuButton.this.getLocationOnScreen().x -5, FileMenuButton.this.getLocationOnScreen().y + 25);
	                    } else {
	                        filePopup.setVisible(false);
	                    }
	                }
			});
			
			
		}
	}
	
	private class FileMenuPopUp extends JPopupMenu{
		private static final long serialVersionUID = 3631645660924751860L;
		
		JMenuItem openMenu;
		JMenuItem closeMenu;
		JMenuItem exportMenu;
		JMenuItem helpMenu;

		public FileMenuPopUp(){
			openMenu = new FileMenuItem("Open", "file-o.png");
			closeMenu = new FileMenuItem("Close part", "times.png");
			exportMenu = new FileMenuItem("Export", "file-code-o.png");
			helpMenu = new FileMenuItem("Help", "info-circle.png");
			
			add(openMenu);
			add(closeMenu);
			add(exportMenu);
			add(helpMenu);
			
			openMenu.setRolloverEnabled(true);

			openMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					final JFileChooser fc = new JFileChooser();
					fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
					fc.setSelectedFile(new File(CraftConfig.lastSlicedFile));
					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						fileSelectedLabel.setText(file.getName());					
					}
				}
			});
			
			
			
			closeMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					Ribbon.this.viewObservable.setPart(null);
					Logger.updateStatus("Ready");
				}
			});
			
			exportMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					Ribbon.this.setSelectedIndex(1);
										final JFileChooser fc = new JFileChooser();
										fc.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
										int returnVal = fc.showSaveDialog(null);
					
										GeneratedPart part = ViewObservable.getInstance().getCurrentPart();
										if (returnVal == JFileChooser.APPROVE_OPTION && part != null && part.isGenerated()) {
											XmlTool xt = new XmlTool(part, fc.getSelectedFile().getPath());
											xt.writeXmlCode();
										}
										else{
											Logger.error("The XML file cannot be generated");
										}
				}
			});
			
			helpMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					JOptionPane.showMessageDialog(null, "For any help call your mom. If she's busy, ask Thibault Cassard & Nicolas Gouju.\n"
							+ "They made MeshIneBits in 2016 with Laurent Daniel as a supervisor.\n"
							+ "You'll have to call them dad though.", "Help", JOptionPane.PLAIN_MESSAGE);
				}
			});
		}
		
	}
	
	private class FileMenuItem extends JMenuItem{

		private static final long serialVersionUID = 3576752233844578812L;

		public FileMenuItem(String label, String iconName){
			super(label);
			setRolloverEnabled(true);
			
			try {
				ImageIcon icon = new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName)).getImage().getScaledInstance(22, 22, Image.SCALE_DEFAULT));
				this.setIcon(icon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.setHorizontalAlignment(LEFT);
			this.setMargin(new Insets(0, 0, 0, 2));

			addMouseListener(new MouseListener() {
			    @Override
			    public void mouseEntered(MouseEvent e) {
			        setArmed(true);
			    };

			    @Override
			    public void mouseExited(MouseEvent e) {
			        setArmed(false);
			    }

				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				};
			});
		}
	}

	private class TemplateTab extends RibbonTab {
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

			computeBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Ribbon.this.viewObservable.getCurrentPart().buildBits2D();
				}
			});
		}
	}

	private class ButtonIcon extends JButton {
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
	
	private class ribbonCheckBox extends JCheckBox implements Observer{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 9143671052675167109L;

		public ribbonCheckBox(String label){
			super(label);
			viewObservable.addObserver(this);
			this.setFocusable(false);
		}

		@Override
		public void update(Observable o, Object arg) {
			
		}
		
	}

	private class ReviewTab extends RibbonTab {

		private static final long serialVersionUID = -6062849183461607573L;
		
		private void replaceSelectedBit(double percentageLength, double percentageWidth){
			ViewObservable vo = ViewObservable.getInstance();
			GeneratedPart part = vo.getCurrentPart();
			Layer layer = part.getLayers().get(vo.getCurrentLayerNumber());
			if(vo.getSelectedBitKey() == null){
				Logger.warning("There is no bit selected");
				return;
			}	
			Bit3D bit = layer.getBit3D(vo.getSelectedBitKey());
			layer.replaceBit(bit, percentageLength, percentageWidth);
		}

		public ReviewTab() {
			super();
			
			JCheckBox slicesCheckBox = new ribbonCheckBox("Show slices"){
				private static final long serialVersionUID = 7090657482323001875L;

				@Override
				public void update(Observable o, Object arg) {
					setSelected(viewObservable.showSlices());
				}
			};
			
			JCheckBox liftPointsCheckBox = new ribbonCheckBox("Show liftPoints"){
				private static final long serialVersionUID = 7090657482323001875L;

				@Override
				public void update(Observable o, Object arg) {
					setSelected(viewObservable.showLiftPoints());
				}
			};
			JCheckBox previousLayerCheckBox = new JCheckBox("Show Previous layer");
			JCheckBox cutPathsCheckBox = new JCheckBox("Show cut paths");
			
			OptionsContainer displayCont = new OptionsContainer("Display options");
			displayCont.add(slicesCheckBox);
			displayCont.add(liftPointsCheckBox);
			displayCont.add(previousLayerCheckBox);
			displayCont.add(cutPathsCheckBox);

			add(displayCont);
			add(new TabContainerSeparator());
			
			OptionsContainer sliceSelectionCont = new OptionsContainer("Slice selection");
			add(sliceSelectionCont);
			add(new TabContainerSeparator());

			OptionsContainer modifCont = new OptionsContainer("Replace bit");
			JButton replaceBitBtn1 = new JButton("Replace bit 1");
			JButton replaceBitBtn2 = new JButton("Replace bit 2");
			modifCont.add(replaceBitBtn1);
			modifCont.add(replaceBitBtn2);

			add(modifCont);
			add(new TabContainerSeparator());
			
			slicesCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					viewObservable.toggleShowSlice(slicesCheckBox.isSelected());
				}
			});
			
			liftPointsCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					viewObservable.toggleShowLiftPoints(liftPointsCheckBox.isSelected());
				}
			});
			
			replaceBitBtn1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					replaceSelectedBit(100, 50);
				}
			});
			
			replaceBitBtn2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					replaceSelectedBit(50, 100);
				}
			});
		}
	}

	private class RibbonTab extends JPanel {

		private static final long serialVersionUID = 5540398663631111329L;

		public RibbonTab() {
			FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 10, 0);
			layout.setAlignOnBaseline(true);
			this.setLayout(layout);
			this.setBackground(Color.WHITE);
			this.setFocusable(false);
		}
	}

	private class SlicerTab extends RibbonTab{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2435250564072409684L;
		

		public SlicerTab() {
			super();

			OptionsContainer slicerCont = new OptionsContainer("Slicer options");
			LabeledSpinner sliceHeightSpinner = new LabeledSpinner("Slice height (mm) :  ", CraftConfig.sliceHeight, 0, 999, 0.1);
			LabeledSpinner firstSliceHeightPercentSpinner = new LabeledSpinner("First slice height (%) :  ", CraftConfig.firstSliceHeightPercent, 0, 999, 10);
			slicerCont.add(sliceHeightSpinner);
			slicerCont.add(firstSliceHeightPercentSpinner);

			OptionsContainer modelOrientationCont = new OptionsContainer("model Orientation");
			JButton editOrientationBtn = new ButtonIcon("  Edit orientation  ", "rotate-left.png");
			modelOrientationCont.add(editOrientationBtn);

			OptionsContainer computeCont = new OptionsContainer("Compute");
			computeSlicesBtn = new ButtonIcon("Slice model", "align-center.png");
			fileSelectedLabel = new JLabel("No file selected");
			computeCont.add(fileSelectedLabel);
			computeCont.add(computeSlicesBtn);

			add(slicerCont);
			add(new TabContainerSeparator());
			add(modelOrientationCont);
			add(new TabContainerSeparator());			
			add(computeCont);

			addConfigSpinnerChangeListener(sliceHeightSpinner, "sliceHeight");
			addConfigSpinnerChangeListener(firstSliceHeightPercentSpinner, "firstSliceHeightPercent");

			computeSlicesBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					computeSlicesBtn.setEnabled(false);
					if (Ribbon.this.viewObservable.getCurrentPart() != null)
						Ribbon.this.viewObservable.setPart(null);

					if (file != null) {
						try {
							MeshIneBitsMain.sliceModel(file.toString());
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
	}

	private class TabContainerSeparator extends JSeparator {
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
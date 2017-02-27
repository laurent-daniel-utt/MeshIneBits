package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.MeshIneBitsMain;
import meshIneBits.Config.CraftConfig;
import meshIneBits.Config.CraftConfigLoader;
import meshIneBits.util.Logger;
import meshIneBits.util.XmlTool;

public class Ribbon extends JTabbedPane implements Observer {
	private static final long serialVersionUID = -1759701286071368808L;
	private ViewObservable viewObservable;
	private File file = null;
	private JLabel fileSelectedLabel;
	private JButton computeSlicesBtn;
	private JButton computeTemplateBtn;
	private JLabel selectedSlice;
	private JPopupMenu filePopup;

	public Ribbon() {
		viewObservable = ViewObservable.getInstance();

		// Add the tab
		addTab("File", new JPanel());
		addTab("Slicer", new JScrollPane(new SlicerTab()));
		addTab("Template", new JScrollPane(new TemplateTab()));
		addTab("Review", new JScrollPane(new ReviewTab()));

		// Add the menu button
		FileMenuButton fileMenuBtn = new FileMenuButton();
		this.setTabComponentAt(0, fileMenuBtn);

		Ribbon.this.setSelectedIndex(indexOfTab("Slicer"));

		// Disabling the tabs that are useless before slicing
		setEnabledAt(indexOfTab("Review"), false);
		setEnabledAt(indexOfTab("File"), false);

		// Visual options
		setFont(new Font(this.getFont().toString(), Font.PLAIN, 15));
	}

	private void addConfigSpinnerChangeListener(LabeledSpinner spinner, String configFieldName) {
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				try {
					Field f = CraftConfig.class.getField(configFieldName);
					f.setDouble(null, spinner.getValue());
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void update(Observable o, Object arg) {
		// If no STL loaded, disable slice and generate layers button
		if (viewObservable.getCurrentPart() == null) {
			setEnabledAt(indexOfTab("Review"), false);
			computeSlicesBtn.setEnabled(false);
			computeTemplateBtn.setEnabled(false);
		}

		// If a STL is loaded & sliced but layers not generated, enable the generate layers button
		if ((viewObservable.getCurrentPart() != null) && viewObservable.getCurrentPart().isGenerated()) {
			setEnabledAt(indexOfTab("Review"), true);
			selectedSlice.setText(" " + String.valueOf(viewObservable.getCurrentPart().getLayers().get(viewObservable.getCurrentLayerNumber()).getSliceToSelect()));
			computeTemplateBtn.setEnabled(true);
		}

		// If a STL is loaded & sliced & layers generated, enable both button (to allow redo computation)
		if ((viewObservable.getCurrentPart() != null) && viewObservable.getCurrentPart().isSliced()) {
			computeSlicesBtn.setEnabled(true);
			computeTemplateBtn.setEnabled(true);
		}

		revalidate();
	}

	private class AboutDialogWindow extends JDialog {
		private static final long serialVersionUID = -3389839563563221684L;

		public AboutDialogWindow(JFrame parent, String title, boolean modal) {
			super(parent, title, modal);

			// Visual options
			Image windowIcon = new ImageIcon(this.getClass().getClassLoader().getResource("resources/icon.png")).getImage();
			this.setIconImage(windowIcon);
			this.setSize(270, 145);
			this.setLocationRelativeTo(null);
			this.setResizable(false);

			// Setting up the dialog
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));

			JLabel bg = new JLabel("");
			ImageIcon icon = new ImageIcon(
					new ImageIcon(this.getClass().getClassLoader().getResource("resources/MeshIneBits.png")).getImage().getScaledInstance(248, 42, Image.SCALE_SMOOTH));
			bg.setIcon(icon);
			bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
			bg.setForeground(new Color(0, 0, 0, 8));
			bg.setAlignmentX(Component.CENTER_ALIGNMENT);

			JLabel copyrightLabel = new JLabel("Copyrightę 2016 Thibault Cassard & Nicolas Gouju.");
			copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

			JButton helpFileBtn = new JButton("Open help file (PDF format)");
			helpFileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

			jp.add(new JLabel(" "));
			jp.add(bg);
			jp.add(copyrightLabel);
			jp.add(new JLabel(" "));
			jp.add(helpFileBtn);
			AboutDialogWindow.this.getContentPane().add(jp, BorderLayout.CENTER);

			// Actions listener
			helpFileBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AboutDialogWindow.this.dispose();
					Desktop dt = Desktop.getDesktop();
					try {
						dt.open(new File(this.getClass().getClassLoader().getResource("resources/help.pdf").getPath()));
					} catch (IOException e1) {
						Logger.error("Failed to load help file");
					}
				}
			});

			this.setVisible(true);
		}
	}

	private class ButtonIcon extends JButton {
		private static final long serialVersionUID = 4439705350058229259L;

		public ButtonIcon(String label, String iconName) {
			this(label, iconName, false);
		}

		public ButtonIcon(String label, String iconName, boolean onlyIcon) {
			this(label, iconName, onlyIcon, 22, 22);
		}

		public ButtonIcon(String label, String iconName, boolean onlyIcon, int width, int height) {
			super((label.isEmpty() ? "" : " ") + label);
			this.setHorizontalAlignment(LEFT);
			this.setMargin(new Insets(0, 0, 0, 2));

			try {
				ImageIcon icon = new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName)).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
				this.setIcon(icon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (onlyIcon) {
				setContentAreaFilled(false);
				setBorder(new EmptyBorder(3, 3, 3, 3));

				// Actions listener
				addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mouseEntered(java.awt.event.MouseEvent evt) {
						setContentAreaFilled(true);
					}

					@Override
					public void mouseExited(java.awt.event.MouseEvent evt) {
						setContentAreaFilled(false);
					}
				});
			}
		}
	}

	private class FileMenuButton extends JToggleButton {
		private static final long serialVersionUID = 5613899244422633632L;

		public FileMenuButton() {
			// Visual options
			this.setFocusable(false);
			this.setBorder(null);
			this.setContentAreaFilled(false);

			// Setting up
			ImageIcon icon = new ImageIcon(
					new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "bars.png")).getImage().getScaledInstance(24, 24, Image.SCALE_REPLICATE));
			this.setIcon(icon);

			ImageIcon selectedIcon = new ImageIcon(
					new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "blue-bars.png")).getImage().getScaledInstance(24, 24, Image.SCALE_REPLICATE));

			filePopup = new FileMenuPopUp();

			// Actions listener
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					JToggleButton b = FileMenuButton.this;
					if (b.isSelected()) {
						filePopup.show(null, FileMenuButton.this.getLocationOnScreen().x - 5, FileMenuButton.this.getLocationOnScreen().y + 25);
						setIcon(selectedIcon);
					} else {
						filePopup.setVisible(false);
						setIcon(icon);
					}
				}
			});

			filePopup.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					FileMenuButton.this.setSelected(false);
					setIcon(icon);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					FileMenuButton.this.setSelected(false);
					setIcon(icon);
				}

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					FileMenuButton.this.setSelected(true);
					setIcon(icon);
				}

			});

		}
	}

	private class FileMenuItem extends JMenuItem {

		private static final long serialVersionUID = 3576752233844578812L;

		public FileMenuItem(String label, String iconName) {
			super(label);

			// Visual options
			setRolloverEnabled(true);
			this.setHorizontalAlignment(LEFT);
			this.setMargin(new Insets(0, 0, 0, 2));

			// Setting up
			try {
				ImageIcon icon = new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName)).getImage().getScaledInstance(22, 22, Image.SCALE_DEFAULT));
				this.setIcon(icon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Actions listener
			addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
				};

				@Override
				public void mouseEntered(MouseEvent e) {
					setArmed(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setArmed(false);
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				};
			});
		}
	}

	private class FileMenuPopUp extends JPopupMenu {
		private static final long serialVersionUID = 3631645660924751860L;

		private JMenuItem openMenu;
		private JMenuItem closeMenu;
		private JMenuItem exportMenu;
		private JMenuItem aboutMenu;

		public FileMenuPopUp() {
			// Setting up
			openMenu = new FileMenuItem("Open", "file-o.png");
			closeMenu = new FileMenuItem("Close part", "times.png");
			exportMenu = new FileMenuItem("Export", "file-code-o.png");
			aboutMenu = new FileMenuItem("About", "info-circle.png");

			add(openMenu);
			add(closeMenu);
			add(exportMenu);
			addSeparator();
			add(aboutMenu);

			openMenu.setRolloverEnabled(true);

			// Actions listener
			openMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);//Close the popUpMenu
					final JFileChooser fc = new JFileChooser();
					fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
					fc.setCurrentDirectory(new File(CraftConfig.lastSlicedFile).getParentFile());
					System.out.println(new File(CraftConfig.lastSlicedFile));
					//fc.setSelectedFile(new File(CraftConfig.lastSlicedFile));
					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						fileSelectedLabel.setText(file.getName());
						Logger.updateStatus("Ready to slice " + file.getName());
						computeSlicesBtn.setEnabled(true);
					}
				}
			});

			closeMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);//Close the popUpMenu
					Ribbon.this.setSelectedIndex(indexOfTab("Slicer"));
					Ribbon.this.viewObservable.setPart(null);
				}
			});

			exportMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);//Close the popUpMenu
					final JFileChooser fc = new JFileChooser();
					fc.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
					int returnVal = fc.showSaveDialog(null);

					GeneratedPart part = ViewObservable.getInstance().getCurrentPart();
					if ((returnVal == JFileChooser.APPROVE_OPTION) && (part != null) && part.isGenerated()) {
						XmlTool xt = new XmlTool(part, Paths.get(fc.getSelectedFile().getPath()));
						xt.writeXmlCode();
					} else {
						Logger.error("The XML file has not been generated");
					}
				}
			});

			aboutMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					new AboutDialogWindow(null, "About MeshIneBits", true);
				}
			});
		}
	}

	private class GalleryContainer extends OptionsContainer {

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
		private static final long serialVersionUID = 6726754934854914029L;
		
		private JSpinner spinner;

		public LabeledSpinner(String label, double defaultValue, double minValue, double maxValue, double step) {
			// Visual options
			this.setLayout(new BorderLayout());
			this.setBackground(Color.WHITE);
			this.setBorder(new EmptyBorder(4, 0, 0, 0));
			
			// Setting up
			this.add(new JLabel(label), BorderLayout.WEST);
			spinner = new JSpinner(new SpinnerNumberModel(defaultValue, minValue, maxValue, step));
			this.add(spinner, BorderLayout.EAST);
			
		}

		public void addChangeListener(ChangeListener listener) {
			spinner.addChangeListener(listener);
		}

		public Double getValue() {
			return (Double) spinner.getValue();
		}
	}

	private class OptionsContainer extends JPanel {
		
		private static final long serialVersionUID = 136154266552080732L;

		public OptionsContainer(String title) {
			// Visual options
			this.setMinimumSize(new Dimension(500, 500));
			this.setLayout(new GridLayout(3, 0, 10, 0));
			this.setBackground(Color.WHITE);
			
			TitledBorder centerBorder = BorderFactory.createTitledBorder(title);
			centerBorder.setTitleJustification(TitledBorder.CENTER);
			centerBorder.setTitleFont(new Font(this.getFont().toString(), Font.BOLD, 12));
			centerBorder.setTitleColor(Color.gray);
			centerBorder.setBorder(BorderFactory.createEmptyBorder());
			this.setBorder(centerBorder);
			
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

		private static final long serialVersionUID = -6062849183461607573L;

		public ReviewTab() {
			super();

			// Setting up
			JCheckBox slicesCheckBox = new RibbonCheckBox("Show slices") {
				private static final long serialVersionUID = 7090657482323001875L;

				@Override
				public void update(Observable o, Object arg) {
					setSelected(viewObservable.showSlices());
				}
			};

			JCheckBox liftPointsCheckBox = new RibbonCheckBox("Show liftPoints") {
				private static final long serialVersionUID = 7090657482323001875L;

				@Override
				public void update(Observable o, Object arg) {
					setSelected(viewObservable.showLiftPoints());
				}
			};
			JCheckBox previousLayerCheckBox = new RibbonCheckBox("Show Previous layer") {
				private static final long serialVersionUID = 7090657482323001875L;

				@Override
				public void update(Observable o, Object arg) {
					setSelected(viewObservable.showPreviousLayer());
				}
			};
			JCheckBox cutPathsCheckBox = new RibbonCheckBox("Show cut paths") {
				private static final long serialVersionUID = 7090657482323001875L;

				@Override
				public void update(Observable o, Object arg) {
					setSelected(viewObservable.showCutPaths());
				}
			};

			OptionsContainer displayCont = new OptionsContainer("Display options");
			displayCont.add(slicesCheckBox);
			displayCont.add(liftPointsCheckBox);
			displayCont.add(previousLayerCheckBox);
			displayCont.add(cutPathsCheckBox);

			add(displayCont);
			add(new TabContainerSeparator());

			OptionsContainer sliceSelectionCont = new OptionsContainer("Selected slice");
			sliceSelectionCont.setLayout(new BoxLayout(sliceSelectionCont, BoxLayout.PAGE_AXIS));
			
			ButtonIcon upArrow = new ButtonIcon("", "angle-up.png");
			upArrow.setAlignmentX(CENTER_ALIGNMENT);
			upArrow.setHorizontalAlignment(SwingConstants.CENTER);
			
			selectedSlice = new JLabel();
			selectedSlice.setFont(new Font("Helvetica", Font.PLAIN, 20));
			selectedSlice.setHorizontalAlignment(SwingConstants.CENTER);
			selectedSlice.setPreferredSize(new Dimension(90, 25));
			selectedSlice.setAlignmentX(CENTER_ALIGNMENT);
			
			ButtonIcon downArrow = new ButtonIcon("", "angle-down.png");
			downArrow.setHorizontalAlignment(SwingConstants.CENTER);
			downArrow.setAlignmentX(CENTER_ALIGNMENT);

			sliceSelectionCont.add(upArrow);
			sliceSelectionCont.add(selectedSlice);
			sliceSelectionCont.add(downArrow);

			OptionsContainer modifCont = new OptionsContainer("Replace bit");
			JButton replaceBitBtn1 = new ButtonIcon("", "cut-length.png", true, 80, 25);
			JButton replaceBitBtn2 = new ButtonIcon("", "cut-width.png", true, 80, 25);
			JButton replaceByFullBitBtn = new ButtonIcon("", "full-bit.png", true, 80, 25);
			modifCont.add(replaceBitBtn1);
			modifCont.add(replaceBitBtn2);
			modifCont.add(replaceByFullBitBtn);

			add(sliceSelectionCont);
			add(new TabContainerSeparator());
			add(modifCont);

			// Actions listener
			slicesCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewObservable.toggleShowSlice(slicesCheckBox.isSelected());
				}
			});

			liftPointsCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewObservable.toggleShowLiftPoints(liftPointsCheckBox.isSelected());
				}
			});

			previousLayerCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewObservable.toggleShowPreviousLayer(previousLayerCheckBox.isSelected());
				}
			});

			cutPathsCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewObservable.toggleShowCutPaths(cutPathsCheckBox.isSelected());
				}
			});

			upArrow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Layer currentLayer = viewObservable.getCurrentPart().getLayers().get(viewObservable.getCurrentLayerNumber());
					currentLayer.setSliceToSelect(currentLayer.getSliceToSelect() + 1);
				}
			});

			downArrow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Layer currentLayer = viewObservable.getCurrentPart().getLayers().get(viewObservable.getCurrentLayerNumber());
					currentLayer.setSliceToSelect(currentLayer.getSliceToSelect() - 1);
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

			replaceByFullBitBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					replaceSelectedBit(100, 100);
				}
			});
		}

		private void replaceSelectedBit(double percentageLength, double percentageWidth) {
			ViewObservable vo = ViewObservable.getInstance();
			GeneratedPart part = vo.getCurrentPart();
			Layer layer = part.getLayers().get(vo.getCurrentLayerNumber());
			
			if (vo.getSelectedBitKey() == null) {
				Logger.warning("There is no bit selected");
				return;
			}
			
			Bit3D bit = layer.getBit3D(vo.getSelectedBitKey());
			
			layer.replaceBit(bit, percentageLength, percentageWidth);
		}
	}

	private class RibbonCheckBox extends JCheckBox implements Observer {

		private static final long serialVersionUID = 9143671052675167109L;

		public RibbonCheckBox(String label) {
			super(label);
			// Visual options
			this.setBackground(Color.WHITE);
			this.setFocusable(false);
			
			// Setting up
			viewObservable.addObserver(this);
		}

		@Override
		public void update(Observable o, Object arg) {

		}

	}

	private class RibbonTab extends JPanel {

		private static final long serialVersionUID = 5540398663631111329L;

		public RibbonTab() {
			// Visual options
			FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 10, 0);
			layout.setAlignOnBaseline(true);
			this.setLayout(layout);
			this.setBackground(Color.WHITE);
			this.setFocusable(false);
		}
	}

	private class SlicerTab extends RibbonTab {

		private static final long serialVersionUID = -2435250564072409684L;

		public SlicerTab() {
			super();

			// Setting up
			OptionsContainer slicerCont = new OptionsContainer("Slicer options");
			LabeledSpinner sliceHeightSpinner = new LabeledSpinner("Slice height (mm) :  ", CraftConfig.sliceHeight, 0, 999, 0.1);
			LabeledSpinner firstSliceHeightPercentSpinner = new LabeledSpinner("First slice height (%) :  ", CraftConfig.firstSliceHeightPercent, 0, 999, 10);
			slicerCont.add(sliceHeightSpinner);
			slicerCont.add(firstSliceHeightPercentSpinner);

			OptionsContainer computeCont = new OptionsContainer("Compute");
			computeSlicesBtn = new ButtonIcon("Slice model", "gears.png");
			computeSlicesBtn.setHorizontalAlignment(SwingConstants.CENTER);
			computeSlicesBtn.setEnabled(false);
			fileSelectedLabel = new JLabel("No file selected");
			computeCont.add(fileSelectedLabel);
			computeCont.add(computeSlicesBtn);

			add(slicerCont);
			add(new TabContainerSeparator());
			add(computeCont);

			
			// Actions listener
			addConfigSpinnerChangeListener(sliceHeightSpinner, "sliceHeight");
			
			addConfigSpinnerChangeListener(firstSliceHeightPercentSpinner, "firstSliceHeightPercent");

			computeSlicesBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if (Ribbon.this.viewObservable.getCurrentPart() != null) {
						Ribbon.this.viewObservable.setPart(null);
					}

					if (file != null) {
						computeSlicesBtn.setEnabled(false);
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
							computeSlicesBtn.setEnabled(true);
						}
					}
				}
			});

		}
	}

	private class TabContainerSeparator extends JSeparator {
		private static final long serialVersionUID = 7739612020735334296L;

		public TabContainerSeparator() {
			// Visual options
			this.setOrientation(SwingConstants.VERTICAL);
			Dimension d = this.getPreferredSize();
			d.height = 105;
			this.setPreferredSize(d);
		}
	}

	private class TemplateTab extends RibbonTab {
		
		private static final long serialVersionUID = -2963705108403089250L;

		public TemplateTab() {
			super();

			// Setting up
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
			if (CraftConfig.patternNumber == 3) {
				pattern1Btn.setSelected(true);
			} else {
				pattern2Btn.setSelected(true);
			}
			patternGallery.addButton(pattern1Btn, "p1.png");
			patternGallery.addButton(pattern2Btn, "p2.png");

			OptionsContainer patternCont = new OptionsContainer("Template options");
			LabeledSpinner rotationSpinner = new LabeledSpinner("Rotation (░) :  ", CraftConfig.rotation, 0, 360, 22.5);
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
			computeTemplateBtn = new ButtonIcon("Generate layers", "cog.png");
			computeTemplateBtn.setEnabled(false);
			computeTemplateBtn.setHorizontalAlignment(SwingConstants.CENTER);
			LabeledSpinner minPercentageOfSlicesSpinner = new LabeledSpinner("Min % of slices in a bit3D :  ", CraftConfig.minPercentageOfSlices, 0, 100, 1);
			LabeledSpinner defaultSliceToSelectSpinner = new LabeledSpinner("Default slice to select (%) :  ", CraftConfig.defaultSliceToSelect, 0, 100, 1);
			computeCont.add(minPercentageOfSlicesSpinner);
			computeCont.add(defaultSliceToSelectSpinner);
			computeCont.add(computeTemplateBtn);

			add(bitsCont);
			add(new TabContainerSeparator());
			add(patternGallery);
			add(new TabContainerSeparator());
			add(patternCont);
			add(new TabContainerSeparator());
			add(computeCont);

			// Actions listener
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
						CraftConfig.patternNumber = 3;
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

			computeTemplateBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					computeTemplateBtn.setEnabled(false);
					Ribbon.this.viewObservable.getCurrentPart().buildBits2D();
					CraftConfigLoader.saveConfig(null);
				}
			});
		}
	}
}
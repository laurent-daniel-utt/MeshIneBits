/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits.gui.utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import meshIneBits.util.Logger;

public class AboutDialogWindow extends JDialog {
    private static final long serialVersionUID = -3389839563563221684L;

    public AboutDialogWindow(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);

        // Visual options
        Image windowIcon = IconLoader.get("icon.png").getImage();
        this.setIconImage(windowIcon);
        this.setSize(500, 375);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        // Setting up the dialog
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));

        JLabel bg = new JLabel("");
        ImageIcon icon = IconLoader.get("MeshIneBits.png", 248, 42);
        bg.setIcon(icon);
        bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
        bg.setForeground(new Color(0, 0, 0, 8));
        bg.setAlignmentX(Component.CENTER_ALIGNMENT);



        JLabel copyrightLabel0 = new JLabel("Copyright (C) 2016-2022 DANIEL Laurent.");
        copyrightLabel0.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel1 = new JLabel("Copyright (C) 2016 CASSARD Thibault & GOUJU Nicolas.");
        copyrightLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel2 = new JLabel("Copyright (C) 2017-2018 TRAN Quoc Nhat Han.");
        copyrightLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel3 = new JLabel("Copyright (C) 2018 VALLON Benjamin.");
        copyrightLabel3.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel4 = new JLabel("Copyright (C) 2018 LORIMER Campbell.");
        copyrightLabel4.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel5 = new JLabel("Copyright (C) 2018 D'AUTUME Christian.");
        copyrightLabel5.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel6 = new JLabel("Copyright (C) 2019 DURINGER Nathan (Tests).");
        copyrightLabel6.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel7 = new JLabel("Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO Andr\u00e9.");
        copyrightLabel7.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel8 = new JLabel("Copyright (C) 2020-2021 DO Quang Bao.");
        copyrightLabel8.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel9 = new JLabel("Copyright (C) 2021 VANNIYASINGAM Mithulan.");
        copyrightLabel9.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel10 = new JLabel("Copyright (C) 2015 Cédric Siourakhan");
        copyrightLabel10.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel11 = new JLabel("Copyright (C) 2016 Gabriel Magny");
        copyrightLabel11.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel12 = new JLabel("Copyright (C) 2021  Quang Long");
        copyrightLabel12.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel13 = new JLabel("Copyright (C) 2022 Mhamad Atlab");
        copyrightLabel13.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel14 = new JLabel("Copyright (C) 2022 Majed Hlaihel");
        copyrightLabel14.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel copyrightLabel15 = new JLabel("Copyright (C) 2026 Aymeric Sirejol");
        copyrightLabel15.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton helpFileBtn = new JButton("Open help file (PDF format)");
    helpFileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    jp.add(new JLabel(" "));
    jp.add(bg);
    jp.add(copyrightLabel0);
    jp.add(copyrightLabel10);
    jp.add(copyrightLabel11);
    jp.add(copyrightLabel1);
    jp.add(copyrightLabel2);
    jp.add(copyrightLabel3);
    jp.add(copyrightLabel4);
    jp.add(copyrightLabel5);
    jp.add(copyrightLabel6);
    jp.add(copyrightLabel7);
    jp.add(copyrightLabel8);
    jp.add(copyrightLabel9);
    jp.add(copyrightLabel12);
    jp.add(copyrightLabel13);
    jp.add(copyrightLabel14);
    jp.add(copyrightLabel15);
    jp.add(new JLabel(" "));
    jp.add(helpFileBtn);
    this.getContentPane()
        .add(jp, BorderLayout.CENTER);

    // Actions listener
    helpFileBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        AboutDialogWindow.this.dispose();
        Desktop dt = Desktop.getDesktop();
        try {
          dt.open(new File(
              Objects.requireNonNull(this.getClass()
                      .getClassLoader()
                      .getResource("resources/help.pdf"))
                  .getPath()));
        } catch (IOException e1) {
          Logger.error("Failed to load help file");
        }
      }
    });

    this.setVisible(true);
  }
}

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

package meshIneBits.gui.view3d.view;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.util.CustomLogger;
import processing.awt.PSurfaceAWT;
import processing.core.PApplet;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class UIParameterWindow extends PApplet {

  public static class WindowBuilder {

    private String title;
    private int width;
    private int height;
    private UIPWListener listener;

    public WindowBuilder setTitle(String title) {
      this.title = title;
      return this;
    }

    public WindowBuilder setSize(int width, int height) {
      this.width = width;
      this.height = height;
      return this;
    }

    public WindowBuilder setListener(UIPWListener listener) {
      this.listener = listener;
      return this;
    }

    public <T extends UIParameterWindow> T build(Class<T> c)
        throws NoSuchMethodException,
        InvocationTargetException,
        InstantiationException,
        IllegalAccessException {
      Constructor<T> constructor = c.getConstructor();
      T obj = constructor.newInstance();
      obj.setTitle(title).setSizeWindow(width, height).setUIControllerListener(listener);
      return obj;
    }

  }


  private UIPWListener listener;
  final CustomLogger logger = new CustomLogger(this.getClass());

  private ControlP5 control;
  private String title;
  private boolean exited = false;

  public abstract void onOpen();

  public abstract void onClose();

  protected abstract void generateButton();

  protected abstract void updateButton();

  @SuppressWarnings("unused")
  public abstract void controlEvent(ControlEvent theEvent);

  public void setup() {
    surface.setTitle(title);
    surface.setResizable(true);
    frameRate(Visualization3DConfig.UIPW_FRAMERATE);
    control = new ControlP5(this);
    generateButton();
    control.enableShortcuts();
  }

  public void draw() {
    background(
        Visualization3DConfig.UIPW_BACKGROUND.getRed(),
        Visualization3DConfig.UIPW_BACKGROUND.getGreen(),
        Visualization3DConfig.UIPW_BACKGROUND.getBlue());
    //noStroke();
    updateButton();
  }

  public UIPWListener getListener() {
    return listener;
  }

  public ControlP5 getControl() {
    return control;
  }

  public UIParameterWindow setTitle(String title) {
    this.title = title;
    return this;
  }

  public UIParameterWindow setUIControllerListener(UIPWListener listener) {
    this.listener = listener;
    return this;
  }

  @Override
  public void exitActual() {
    PSurfaceAWT.SmoothCanvas surf = (PSurfaceAWT.SmoothCanvas) this.getSurface().getNative();
    Frame frame = surf.getFrame();
    frame.dispose();
  }

  public void closeWindow() {
    onClose();
    listener = null;
    exit();
  }

  @Override
  public void exit() {
    if (!exited) {
      super.exit();
      exited = true;
    }
  }

  public UIParameterWindow setSizeWindow(int width, int height) {
    super.setSize(width, height);
    return this;
  }
}

package meshIneBits.gui.view3d.Processor;

import java.util.ArrayList;
import java.util.List;

public class DisplayState {

  public enum State {PAVED_VIEW, MODEL_VIEW, ANIMATION_VIEW}

  interface OnStateChangedListener {

    void onShapeDisplayStateChanged(State state);
  }

  private State state = State.MODEL_VIEW;
  private final List<OnStateChangedListener> listeners = new ArrayList<>();

  public void setState(State state) {
    this.state = state;
    notifyListener();
  }

  private void notifyListener() {
    listeners.forEach(listener -> listener.onShapeDisplayStateChanged(state));
  }

  public State getState() {
    return state;
  }

  @SuppressWarnings("unused")
  public void addListener(OnStateChangedListener listener) {
    listeners.add(listener);
  }


}

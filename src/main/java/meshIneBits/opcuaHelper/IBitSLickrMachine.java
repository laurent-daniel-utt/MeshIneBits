package meshIneBits.opcuaHelper;

public interface IBitSLickrMachine {
  ICustomResponse startMachine();
  ICustomResponse stopMachine();
  ICustomResponse pauseMachine();
}

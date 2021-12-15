package utils;

public class TestException {

  public void tryToDoSomeThing(String string) throws Exception {
    try {
      if (string == null) {
        throw new NullPointerException();

      }
      System.out.println("tryToDoSomeThing called");
    } catch (NullPointerException e) {
      System.out.println("NULL POINTER EXEPTION");
      throw new Exception();
    }
  }

  public void caller(String string) {
    try {
      tryToDoSomeThing(string);
    } catch (Exception e) {
      System.out.println("EXCEPTION");
    }
  }

  public static void main(String[] args) {
    TestException t = new TestException();
    t.caller(null);
  }
}

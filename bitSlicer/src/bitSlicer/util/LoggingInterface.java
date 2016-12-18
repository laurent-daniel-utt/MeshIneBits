package bitSlicer.util;

public interface LoggingInterface {
	void error(String error);

	void message(String message);

	void setProgress(int value, int max);

	void updateStatus(String status);

	void warning(String warning);
}
package lib;

public class Utils {

	public static String GetErrPositon()
	{
		String errPos = "in class:" + Thread.currentThread().getStackTrace()[2].getClassName();
		errPos       += ",in function:" + Thread.currentThread().getStackTrace()[2].getMethodName();
		return errPos;
	}
}

package scannerwrapper;
import java.util.Scanner;

public class ScannerWrapped {
	public static Scanner sc = new Scanner(System.in);
	public static void close() {
		sc.close();
	}

}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

public class Fusion {

	public static byte[] padArr(byte[] arr, int toSize) {

		if (arr.length >= toSize) return arr;

		byte[] newArr = new byte[toSize];

		for (int i = 0; i < arr.length; i++) {
			newArr[i] = arr[i];
		}

		for (int i = arr.length; i < toSize; i++) {
			newArr[i] = 0;
		}

		return newArr;
	}

	public static byte[] addSizeToArr(byte[] arr) {
		byte[] newArr = new byte[4 + arr.length];
		int oldSize = arr.length;

		for (int i = 0; i < arr.length; i++) {
			newArr[i + 4] = arr[i];
		}

		int mask = 0x0ff;

		for (int i = 3; i >= 0; i--) {
			newArr[i] = (byte) (mask & oldSize);
			oldSize = oldSize >> 8;
		}

		return newArr;
	}

	public static byte[] removePadding(byte[] arr) {
		int fileSize = getSizeFromPaddedArr(arr);
		byte[] newArr = new byte[fileSize];

		for (int i = 0; i < fileSize; i++) {
			newArr[i] = arr[i + 4];
		}

		return newArr;
	}

	public static int getSizeFromPaddedArr(byte[] arr) {
		byte[] sizearr = new byte[4];

		for (int i = 0; i < 4; i++) {
			sizearr[i] = arr[i];
		}

		return ByteBuffer.wrap(sizearr).getInt();
	}

	public static byte[] xorArrs(byte[] arr1, byte[] arr2) {
		// Assume equal size
		if (arr1.length != arr2.length) {
			System.out.println("LENGTH NOT EQUAL!");
			System.exit(1);
		}
		byte[] xorArr = new byte[arr1.length];

		for (int i = 0; i < arr1.length; i++) {
			xorArr[i] = (byte) (arr1[i] ^ arr2[i]);
		}

		return xorArr;
	}

	public static void main(String... args) {
		try {
			byte[] f1 = Files.readAllBytes(Paths.get("../res.txt"));
			byte[] f2 = Files.readAllBytes(Paths.get("../res2.txt"));

			System.out.println(f1.length);
			System.out.println(f2.length);
			f1 = addSizeToArr(f1);
			f2 = addSizeToArr(f2);

			byte[] padF1 = padArr(f1, f2.length);
			byte[] padF2 = padArr(f2, f1.length);

			byte[] fused = xorArrs(padF1, padF2);


			byte[] out = removePadding(xorArrs(fused, padF2));

			Files.write(Paths.get("../out.txt"), out);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

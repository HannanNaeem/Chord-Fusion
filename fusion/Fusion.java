import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

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
			// get all files in dir
		
			final File folder = new File ("../testFiles/");
			ArrayList<byte[]> ttl = new ArrayList<byte[]>();
			ArrayList<byte[]> songs1List = new ArrayList<byte[]>();
			ArrayList<byte[]> songs2List = new ArrayList<byte[]>();
			int fusedCap = Integer.parseInt(args[0]);

			System.out.println(fusedCap);

			for (final File f : folder.listFiles()){
				ttl.add(Files.readAllBytes(Paths.get("../testFiles/" + f.getName())));
			} 

			int currCap = 50;
			Random r = new Random();
			
			for(; currCap <= fusedCap; currCap += 50){

				int counter = 0;
				for (final byte[] f : ttl){
					// System.out.println(f.getName());		
					int result = r.nextInt(100);
					if ((result % 2 == 0 && songs1List.size() < currCap) || (songs2List.size() >= currCap)) 
						songs1List.add(f);
					else
						songs2List.add(f);
	
					counter++;
				}
				


				// fuse and store and get size


			}
			// System.out.println("list 1 size: " + songs1List.size());
			// System.out.println("list 2 size: " +songs2List.size());
			
		// 	// sorting on size
			Collections.sort(songs1List, new Comparator<byte[]>(){
				@Override
				public int compare(byte[] lhs, byte[] rhs){
					return lhs.length < rhs.length ? -1 : lhs.length == rhs.length ? 0 : 1;
				}
			});

			Collections.sort(songs2List, new Comparator<byte[]>(){
				@Override
				public int compare(byte[] lhs, byte[] rhs){
					return lhs.length < rhs.length ? -1 : lhs.length == rhs.length ? 0 : 1;
				}
			});

			long fusedSize = 0;
			long repSize = 0;

			for (int i = 0; i < songs1List.size(); i++ ){
				
				byte[] f1 = addSizeToArr(songs1List.get(i));
				byte[] f2 = addSizeToArr(songs2List.get(i));
	
				byte[] padF1 = padArr(f1, f2.length);
				byte[] padF2 = padArr(f2, f1.length);
	
				byte[] fused = xorArrs(padF1, padF2);

				fusedSize += fused.length;
				repSize += songs1List.get(i).length + songs2List.get(i).length;
	
				// System.out.println("Fused size: " + fused.length);
	
				// byte[] out = removePadding(xorArrs(fused, padF1));
	
				// Files.write(Paths.get("../fusedFiles/out" + i + ".mp3"), fused);
				// Files.write(Paths.get("../replicas/outA" + i + ".mp3"), songs1List.get(i));
				// Files.write(Paths.get("../replicas/outB" + i + ".mp3"), songs2List.get(i));
				// System.out.println("Written: " +"../fusedFiles/out" + i + ".mp3" );
			}
			System.out.println("Fused size: " + fusedSize/ Math.pow(2,20));
			System.out.println("Replicas size: " + repSize/ Math.pow(2,20));			

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

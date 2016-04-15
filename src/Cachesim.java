import java.util.*;
import java.io.*; 
import java.math.*;

public class cachesim{
	
	private static class Block {
		boolean validBit;
		boolean dirtyBit;
		String tag; 
		int address; 
		List<String> myValue;
		
		//Creating the cache object that holds necessary info
		public Block (boolean va, boolean di, String ta, int tS, List<String> d){
			validBit = va;
			di = dirtyBit;
			tag = ta;
			address = tS; 
			myValue = d; 
		}
	}
	
	private static int cacheSize;
	private static int numWays; 
	private static int blockSize;
	private static int indexBits;
	private static int offsetBits; 
	
	private static List<String> myMem;
	private static HashMap<Integer, List<Block>> myCache; 
	
	//Constructor for the cache
	public cachesim (int size, int associativity, int blocks){
		cacheSize = kbToByte(size); 
		numWays = associativity;
		blockSize = blocks;
	}
	
	public static int kbToByte(int kb) {
		return (int) (kb * 1024);
	}
	
	public static HashMap<Integer, List<Block>> makeCache(){
		Map<Integer, List<Block>> cacheNew = new HashMap<Integer, List<Block>>();
//		System.out.println(blockSize);
		int numBlocks = cacheSize / blockSize; 
		int numSets = numBlocks / numWays; 
		int set = 0; 
		
		//Initializing Cache
		for(int i = 0; i < numSets; i++){
			List<Block> newArray = new ArrayList<Block>(); 
			List<String> dataToPut = new ArrayList<String>(); 
			for (int j = 0; j < numWays; j++){
				Block cacheToPut = new Block(false, false, "", 0, dataToPut); 
				newArray.add(cacheToPut);
			}
			cacheNew.put(set, newArray);
			set++; 
		}
		return cacheNew; 
	}
	
	public static List<String> makeMemory(){
		List<String> nm = new ArrayList<String>();
		for (long i = 0; i < Math.pow(2, 24) - 1; i++){
			nm.add("");
		}
		return nm; 
	}
	
	public static double logBase2(int value) {
		return (int) (Math.log(value) / Math.log(2));
	}
	
	public static String hexToBinaryString(String hex) {
		return Integer.toBinaryString(Integer.parseInt(hex.substring(2), 16));
	}
	
	//Calculate the index of a given memory address
	public static int calcIndex (String address){
		//If index is size 0;
		indexBits = (int) logBase2(myCache.size());
		if(indexBits == 0){
			return indexBits;
		}
		
		String binaryString = parseAddress(address); 
		
		// TODO: refactor 
		return Integer.parseInt(binaryString.substring(binaryString.length() - offsetBits - indexBits, binaryString.length() - offsetBits) , 2); 
	}
	
	public static String padString(String toPad, int desiredLength) {
		while (toPad.length() < desiredLength) {
			toPad = "0" + toPad;
		}
		return toPad; 
	}
	
	public static String parseAddress(String address) {
		String binaryString = hexToBinaryString(address); 
		binaryString = padString(binaryString, 24); 
		return binaryString; 
	}
	
	//Calculate the tag of a given memory address
	public static String calcTag (String address){
		String binaryString = parseAddress(address); 
		return binaryString.substring(0, binaryString.length() - offsetBits - indexBits); 
	}
	
	//Calculate the offset of a given memory address
	public static int calcOffset (String address){
		offsetBits = (int) logBase2(blockSize); 
		String binaryString = parseAddress(address);
		return Integer.parseInt(binaryString.substring(binaryString.length() - offsetBits, binaryString.length()), 2); 
	}

	public static String instructionProcess (String instruction, int counter){
		String[] instructionArray = instruction.split(" "); // split by spaces
		String type = instructionArray[0]; // 'store' or 'load' 
		String address = instructionArray[1]; // the hex address thing
		System.out.println(address); 
		
		// offset, index, and tag of the instruction that was read 
		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);
		
		boolean hit = false; 
		int hitIndex = 0; 
		
		String out = ""; 
		
		switch (type) {
		case "store": out = store(hit, hitIndex, instructionArray, counter);
		case "load": out = load(hit, hitIndex, instructionArray);
		}
		
		return out;
	}
	
	
	public static String store(boolean hit, int hitIndex, String[] instructionArray, int counter) {
		// Check through entire cache for a tag that matches current tag
		
		String address = instructionArray[1]; // the hex address thing
		int numBytes = Integer.parseInt(instructionArray[2]);
		String writeValue = instructionArray[3];
		
		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);
		
		 for (int i = 0; i < myCache.get(index).size(); i++){
			 Block sample = myCache.get(index).get(i); 
			 if(sample.tag.equals(tag)){ //Match 
				 hit = true; 
				 hitIndex = i;
				 break; 
			 }
		 }
		 
		 if (hit){
			 for (int i = 0; i < numBytes; i++){
				 myCache.get(index).get(hitIndex).myValue.set(offset + i, writeValue.substring(index, i+2)); 
			 }
			 myCache.get(index).get(hitIndex).dirtyBit = true; 
			 return "split" + " " + address + " hit"; 
		 }
		 
		 else{
			 String s0 = "";
			 String s1 = "";
			 
			 for(int i = 0; i < offsetBits; i++){
				 s0 += "0";
				 s1 += "1"; 
			 }
			 
			 String binaryString = parseAddress(address); 
			 
			 String indexString = binaryString.substring(binaryString.length() - offsetBits - indexBits, binaryString.length() - offsetBits);
			 
			 String binaryAdd1 = tag + indexString + s0; 
			 String binaryAdd2 = tag + indexString + s1; 
			 
			 int lower = Integer.parseInt(binaryAdd1, 2);
			 int upper = Integer.parseInt(binaryAdd2, 2); 
			 
			 for (int i = 0; i< numBytes; i++){
				 int currentBinaryIndex = i * 2;
				 int nextBinaryIndex = (2 * i) + 2; 
				 myMem.set(lower + i, writeValue.substring(currentBinaryIndex, nextBinaryIndex));
			 }
			 
			 // TODO: check it
			 Block cacheNew = new Block(true, false, tag, counter, myMem.subList(lower, upper + 1)); 
			 boolean full = true; 
			 
			 for (int i = 0; i < myCache.get(index).size(); i++){
				 Block sample = myCache.get(index).get(i);
				 
				 if(!sample.validBit){
					 myCache.get(index).set(i, cacheNew);
					 full = false;
					 break; 
				 }
			 }
			 
			 if (full){
				 int indexMin = 0;
				 int absMin = Integer.MAX_VALUE; 
				 
				 for(int i = 0; i < myCache.get(index).size(); i++){
					 if (myCache.get(index).get(i).address < absMin){
						 absMin = myCache.get(index).get(i).address;
						 indexMin = i; 
					 }
				 }
				 if (myCache.get(index).get(indexMin).dirtyBit){
					 for (int i = lower; i <= upper; i++){
						 myMem.set(i, myCache.get(index).get(indexMin).myValue.get(i-lower)); 
					 }
				 }
				 myCache.get(index).set(indexMin, cacheNew); 
			 }
			 return "store" + " " + address + " miss"; 
		 }
	}
	
	
	public static String load(boolean hit, int hitIndex, String[] instructionArray) {
		for (int i = 0; i < myCache.get(index).size(); i++){
			Block sample = myCache.get(index).get(i); 
			
			if(sample.tag.equals(tag)){
				hit = true;
				hitIndex = i;
				break;
			}
		}
		if (hit) {
			String value = "";
			for (int i = 0; i < Integer.parseInt(instructionArray[2]); i++){
				if (myCache.get(index).get(hitIndex).myValue.get(offset + i).equals("")){
					value += "00";
				}
				else{
					value += myCache.get(index).get(hitIndex).myValue.get(offset + i);  
				}
			}
			return instructionArray[0] + " " + instructionArray[1] + " hit " + value; 
		}
		else{
			String s0 = "";
			String s1 = "";
			for (int i = 0; i < offsetBits; i++){
				s0 += "0";
				s1 += "1";
			}
			String binaryAdd = Integer.toBinaryString(Integer.parseInt(address.substring(2), 16)); 
			while (binaryAdd.length() < 24){
				binaryAdd = "0" + binaryAdd; 
			}
			String sIndex = binaryAdd.substring(binaryAdd.length() - offsetBits - indexBits, binaryAdd.length() - offsetBits);
			String bAdd0 = tag + sIndex + s0;
			String bAdd1 = tag + sIndex + s1;
			int lower = Integer.parseInt(bAdd0, 2);
			int upper = Integer.parseInt(bAdd1, 2); 
			Block sample = new Block(true, false, tag, counter, myMem.subList(lower,  upper + 1));
			boolean full = true;
			
			for(int i = 0; i< myCache.get(index).size(); i++){
				Block test = myCache.get(index).get(i);
				if (!test.validBit){
					myCache.get(index).set(index,  sample); 
					full = false;
					break; 
				}
			}
			if (full){
				int minI = 0;
				int absMin = Integer.MAX_VALUE;
				for (int i = 0; i < myCache.get(index).size(); i++){
					if (myCache.get(index).get(i).address < absMin){
						absMin = myCache.get(index).get(i).address;
						minI = i;
					}
				}
				if (myCache.get(index).get(minI).dirtyBit){
					for(int i = lower; i <= upper; i++){
						myMem.set(i, myCache.get(index).get(minI).myValue.get(i = lower));
					}
				}
				myCache.get(index).set(minI, sample);
			}
			String value ="";
			for (int i = 0; i<Integer.parseInt(instructionArray[2]); i++){
				if (sample.myValue.get(offset + i).equals("")){
					value += "00";
				}
				else{
					value += sample.myValue.get(offset+i);
				}
			}
			return instructionArray[0] + " " + instructionArray[1] + " miss " +value; 
		}
	}
	
	public static void main(String [] args) throws IOException{
		String fileName = args[0]; 
		int size = Integer.parseInt(args[1]);
		int asso = Integer.parseInt(args[2]);
		int block = Integer.parseInt(args[3]); 
//		System.out.println(block);
		BufferedReader br = new BufferedReader (new FileReader(fileName));
		cachesim cs = new cachesim(size, asso, block); 
		myCache = cs.makeCache(); 
		myMem = cs.makeMemory();
		int counter = 1; 
		String line = br.readLine();
		while (line != null){
			System.out.println(cs.instructionProcess(line, counter));
			counter += 1;
			line = br.readLine();
		}
	}
}
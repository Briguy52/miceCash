import java.util.*;
import java.io.*; 
import java.math.*;

public class cachesim{
	
	private static int cacheSize;
	private static int numWays; 
	private static int blockSize;
	private static int indexBits;
	private static int offsetBits; 

	private static List<String> myMem;
	private static HashMap<Integer, List<Block>> myCache;

	private static class Block {
		boolean validBit;
		String tag; 
		int address; 
		List<String> myValue;

		//Creating the cache object that holds necessary info
		public Block (boolean va, String ta, int tS, List<String> d){
			validBit = va; // was this just initialized or not? 
			tag = ta;
			address = tS; 
			myValue = d; 
		}
	}
 

	//Constructor for the cache
	public cachesim (int size, int associativity, int blocks){
		cacheSize = kbToByte(size); 
		numWays = associativity;
		blockSize = blocks;
	}

	public static int kbToByte(int kb) {
		return (int) (kb * 1024);
	}

	// TODO: refactor
	public static HashMap<Integer, List<Block>> makeCache(){
		Map<Integer, List<Block>> cacheNew = new HashMap<Integer, List<Block>>();
		int numBlocks = cacheSize / blockSize; 
		int numSets = numBlocks / numWays; 
		int set = 0; 

		//Initializing Cache
		for(int i = 0; i < numSets; i++){
			List<Block> newArray = new ArrayList<Block>(); 
			List<String> dataToPut = new ArrayList<String>(); 
			for (int j = 0; j < numWays; j++){
				Block cacheToPut = new Block(false, "", 0, dataToPut); 
				newArray.add(cacheToPut);
			}
			cacheNew.put(set, newArray);
			set++; 
		}
		return (HashMap<Integer, List<Block>>) cacheNew; 
	}

	// TODO: refactor
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

	public static int calcIndex (String address){
		//If index is size 0;
		indexBits = (int) logBase2(myCache.size());
		if(indexBits == 0){
			return indexBits;
		}
		String binaryString = parseAddress(address); 
		return Integer.parseInt(binaryString.substring(getIndexStart(binaryString), getIndexEnd(binaryString)) , 2); 
	}
	
	public static int getIndexStart(String binaryString) {
		return binaryString.length() - offsetBits - indexBits;
	}
	
	public static int getIndexEnd(String binaryString) {
		return binaryString.length() - offsetBits; 
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

	public static String calcTag (String address){
		String binaryString = parseAddress(address); 
		return binaryString.substring(0, getIndexStart(binaryString)); 
	}

	public static int calcOffset (String address){
		offsetBits = (int) logBase2(blockSize); 
		String binaryString = parseAddress(address);
		return Integer.parseInt(binaryString.substring(getIndexEnd(binaryString), binaryString.length()), 2); 
	}

	public static String instructionProcess (Instruction instruction, int counter){
		
		String type = instruction.getInstrType();

		if (type.equals("store")) {
			return store(instruction, counter);
		}
		else {
			return load(instruction, counter);
		}
	}
	
	public static String store(Instruction instruction, int counter) {
		
		String address = instruction.getAddress();
		int numBytes = instruction.getNumBytes();
		String writeValue = instruction.getWriteValue();

		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);
		
		boolean hit = false; 
		int hitIndex = 0; 
		while (hitIndex < myCache.get(index).size()) {
			Block checkBlock = myCache.get(index).get(hitIndex);
			if (tag.equals(checkBlock.tag)) {
				hit = true;
				break; 
			}
			hitIndex++; 
		}

		if (hit){
			// Loop through and Store writeValue at the desired location 
			for (int i = 0; i < numBytes; i++){
				// TODO: refactor 
				myCache.get(index).get(hitIndex).myValue.set(offset + i, writeValue.substring(index, i+2)); 
			}
			return "store " + address + " hit"; 
		}

		else{ // Miss 
			String binaryString = parseAddress(address); 
			String indexString = binaryString.substring(getIndexStart(binaryString), getIndexEnd(binaryString));
			
			int lower = Integer.parseInt(tag + indexString + offsetPad0(), 2); 
			int upper = Integer.parseInt(tag + indexString + offsetPad1(), 2); 

			for (int i = 0; i< numBytes; i++){
				int currentBinaryIndex = i * 2;
				int nextBinaryIndex = (2 * i) + 2; 
				myMem.set(lower + i, writeValue.substring(currentBinaryIndex, nextBinaryIndex));
			}

			Block newBlock = new Block(true, tag, counter, myMem.subList(lower, upper + 1)); 
			
			boolean isFull = true;
			int count = 0;
			while (count < myCache.get(index).size()) {
				Block checkBlock = myCache.get(index).get(count);
				if (!checkBlock.validBit) {
					isFull = false;
					myCache.get(index).set(count, checkBlock);
					break;
				}
				count ++; 
			}

			if (isFull) { // Write through 
				int indexMin = 0;
				int currMin = Integer.MAX_VALUE; 
				
				// Find min for LRU 
				for(int i = 0; i < myCache.get(index).size(); i++){
					if (myCache.get(index).get(i).address < currMin){
						currMin = myCache.get(index).get(i).address;
						indexMin = i; 
					}
				}
				
				myCache.get(index).set(indexMin, newBlock); 
			}
			return "store " + address + " miss"; 
		}
	}


	public static String load(Instruction instruction, int counter) {

//		String address = instructionArray[1]; // the hex address thing
//		int numBytes = Integer.parseInt(instructionArray[2]);
		
		String address = instruction.getAddress();
		int numBytes = instruction.getNumBytes();

		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);
		
		boolean hit = false; 
		int hitIndex = 0; 

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
			for (int i = 0; i < numBytes; i++){
				if (myCache.get(index).get(hitIndex).myValue.get(offset + i).equals("")){
					value += "00";
				}
				else {
					value += myCache.get(index).get(hitIndex).myValue.get(offset + i);  
				}
			}
			return "load " + address + " hit " + value; 
		}
		else{
			String s0 = "";
			String s1 = "";
			for (int i = 0; i < offsetBits; i++){
				s0 += "0";
				s1 += "1";
			}
			String binaryString = parseAddress(address);

			String sIndex = binaryString.substring(binaryString.length() - offsetBits - indexBits, binaryString.length() - offsetBits);
			String bAdd0 = tag + sIndex + s0;
			String bAdd1 = tag + sIndex + s1;
			int lower = Integer.parseInt(bAdd0, 2);
			int upper = Integer.parseInt(bAdd1, 2); 
			Block sample = new Block(true, tag, counter, myMem.subList(lower,  upper + 1));
			boolean full = true;

			for (int i = 0; i< myCache.get(index).size(); i++){
				Block test = myCache.get(index).get(i);
				if (!test.validBit){
					myCache.get(index).set(index,  sample); 
					full = false;
					break; 
				}
			}
			if (full) {
				int minI = 0;
				int absMin = Integer.MAX_VALUE;
				for (int i = 0; i < myCache.get(index).size(); i++){
					if (myCache.get(index).get(i).address < absMin){
						absMin = myCache.get(index).get(i).address;
						minI = i;
					}
				}
//				if (myCache.get(index).get(minI).dirtyBit){
//					for(int i = lower; i <= upper; i++){
//						myMem.set(i, myCache.get(index).get(minI).myValue.get(i = lower));
//					}
//				}
				myCache.get(index).set(minI, sample);
			}
			String value ="";
			for (int i = 0; i<numBytes; i++){
				if (sample.myValue.get(offset + i).equals("")){
					value += "00";
				}
				else{
					value += sample.myValue.get(offset+i);
				}
			}
			return "load " + address + " miss " + value; 
		}
	}
	
	public static String offsetPad0() {
		String out = "";
		while (out.length() < offsetBits) {
			out += "0"; 
		}
		return out; 
	}
	
	public static String offsetPad1() {
		String out = "";
		while (out.length() < offsetBits) {
			out += "1"; 
		}
		return out; 
	}
	
	public List<Instruction> buildInstructions(String fileName) {
		List<Instruction> out = new ArrayList<Instruction>(); 
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while(scanner.hasNextLine()) {
				String[] instructionArray = scanner.nextLine().split(" "); 
				String type = instructionArray[0]; // store or load
				String address = instructionArray[1]; // the hex address thing
				int numBytes = Integer.parseInt(instructionArray[2]);
				if (instructionArray.length == 4) {
					String writeValue = instructionArray[3];
					out.add(new Instruction(type, numBytes, address, writeValue));
				}
				else {
					out.add(new Instruction(type, numBytes, address));
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
		return out; 
	}


	public static void main(String [] args) throws IOException{
		String[] test = {"tracefile_simple", "1024", "4", "32"};

		//		String fileName = args[0]; 
		//		int size = Integer.parseInt(args[1]);
		//		int asso = Integer.parseInt(args[2]);
		//		int block = Integer.parseInt(args[3]); 
		//		System.out.println(block);

		String fileName = test[0]; 
		int size = Integer.parseInt(test[1]);
		int asso = Integer.parseInt(test[2]);
		int block = Integer.parseInt(test[3]); 

		cachesim cs = new cachesim(size, asso, block); 
		myCache = cs.makeCache(); 
		myMem = cs.makeMemory();
		List<Instruction> myInstructions = cs.buildInstructions(fileName); 
		for (int i=0; i<myInstructions.size(); i++) {
			System.out.println(cs.instructionProcess(myInstructions.get(i), i+1));
		}

	}
}
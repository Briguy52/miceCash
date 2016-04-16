import java.util.*;
import java.io.*; 
import java.math.*;

public class cachesim{

	private static int cacheSize;
	private static int numWays; 
	private static int blockSize;
	private static int indexBits;
	private static int offsetBits; 

	private static Map<Integer, List<Block>> myCache;
	private static List<String> myMem;

	public cachesim (int size, int associativity, int numBlocks){
		cacheSize = kbToByte(size); 
		numWays = associativity;
		blockSize = numBlocks;
		myCache = initCache(); 
		myMem = initMem();
	}

	public static int kbToByte(int kb) {
		return kb * 1024;
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
		return Integer.parseInt(binaryString.substring(getIndexStart(binaryString), getIndexEnd(binaryString)), 2); 
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

	public static String processInstructions (Instruction instruction, int counter){
		String type = instruction.type;
		if (type.equals("store")) { return store(instruction, counter); }
		else { return load(instruction, counter); }
	}

	public static String store(Instruction instruction, int counter) {

		String address = instruction.address;
		int numBytes = instruction.numBytes;
		String writeValue = instruction.writeVal;

		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);

		int hitIndex = getHitIndex(index, tag); 

		if (didHit(index,tag)) { // Store hit
			// Loop through and Store writeValue at the desired location 
			int count = 0;
			while (count < numBytes) {
				String toWrite = writeValue.substring(index, count + 2); 
				myCache.get(count).get(hitIndex).myValue.set(offset + count, toWrite); 
				count++; 
			}
			return "store " + address + " hit"; 
		}
		else{ // Store miss 
			int lower = makeLower(address, tag); 
			int upper = makeUpper(address, tag);
			int count = 0;
			while (count < numBytes) { 
				int currBinIndex = count * 2; 
				int nextBinIndex = (2 * count) + 2; 
				myMem.set(lower + count, writeValue.substring(currBinIndex, nextBinIndex));
				count++; 
			}
			if (checkFull(index)) { // Write through 
				myCache.get(index).set(getMinIndex(index), new Block(true, tag, counter, myMem.subList(lower, upper + 1))); 
			}
			return "store " + address + " miss"; 
		}
	}

	public static int getMinIndex(int index) {
		int count = 0; 
		int minIndex = 0;
		int minValue = Integer.MAX_VALUE; 
		while (count < myCache.get(index).size()) {
			int current = myCache.get(index).get(count).address; 
			if (current < minValue) { minValue = current; minIndex = count; }
			count++; 
		}
		return minIndex; 
	}

	public static int getHitIndex(int index, String tag) {
		int hitIndex = 0; 
		while (hitIndex < myCache.get(index).size()) {
			Block checkBlock = myCache.get(index).get(hitIndex);
			if (tag.equals(checkBlock.tag)) {
				break; 
			}
			hitIndex++; 
		}
		return hitIndex; 
	}

	public static boolean didHit(int index, String tag) {
		int hitIndex = 0; 
		boolean hit = false; 
		while (hitIndex < myCache.get(index).size()) {
			Block checkBlock = myCache.get(index).get(hitIndex);
			if (tag.equals(checkBlock.tag)) {
				hit = true;
				break; 
			}
			hitIndex++; 
		}
		return hit;
	}

	public static boolean checkFull(int index) {
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
		return isFull; 
	}

	public static String load(Instruction instruction, int counter) {

		String address = instruction.address;
		int numBytes = instruction.numBytes;

		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);

		int hitIndex = getHitIndex(index, tag); 

		if (didHit(index, tag)) {
			String value = "";
			int count = 0;
			while (count < numBytes) {
				String curr = myCache.get(index).get(hitIndex).myValue.get(offset + count);  
				if (!curr.equals("")) { value += curr; }
				else { value += "00"; }
				count++;
			}
			return "load " + address + " hit " + value; 
		}
		else { // Load miss 
			int lower = makeLower(address, tag); 
			int upper = makeUpper(address, tag);
			Block newBlock = new Block(true, tag, counter, myMem.subList(lower,  upper + 1));
			if (checkFull(index)) { myCache.get(index).set(getMinIndex(index), newBlock); }
			String value = "";
			int count = 0; 
			while (count < numBytes) {
				String curr = newBlock.myValue.get(offset + count);
				if (!curr.equals("")) { value += curr; }
				else { value += "00"; }
				count++;
			}
			return "load " + address + " miss " + value; 
		}
	}

	public static String makeIndexString(String address, String tag) {
		String binaryString = parseAddress(address); 
		return binaryString.substring(getIndexStart(binaryString), getIndexEnd(binaryString));
	}

	public static int makeUpper(String address, String tag) {
		return Integer.parseInt(tag + makeIndexString(address, tag) + offsetPad1(), 2); 
	}

	public static int makeLower(String address, String tag) {
		return Integer.parseInt(tag + makeIndexString(address, tag) + offsetPad0(), 2); 
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

	public static List<Instruction> buildInstructions(String fileName) {
		List<Instruction> out = new ArrayList<Instruction>(); 
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while(scanner.hasNextLine()) {
				String[] instructionArray = scanner.nextLine().split(" "); 
				String type = instructionArray[0]; // store or load
				String address = instructionArray[1]; // the hex address thing
				int numBytes = Integer.parseInt(instructionArray[2]);
				if (instructionArray.length == 4) { // Store instruction
					String writeValue = instructionArray[3];
					out.add(new Instruction(type, numBytes, address, writeValue));
				}
				else { // Load instruction
					out.add(new Instruction(type, numBytes, address));
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
		return out; 
	}

	public static Map<Integer, List<Block>> initCache(){
		Map<Integer, List<Block>> cacheNew = new HashMap<Integer, List<Block>>();
		int numBlocks = cacheSize / blockSize; 
		int numSets = numBlocks / numWays; 
		int setNumber = 0; 
		int count = 0;
		while(count < numSets) {
			List<Block> blocks = new ArrayList<Block>();
			List<String> blockVal = new ArrayList<String>(); 
			int index = 0;
			while (index < numWays) { blocks.add(new Block(false, "", 0, blockVal)); index++; }
			cacheNew.put(setNumber, blocks);
			count++; 
		}
		return cacheNew; 
	}

	public static List<String> initMem(){
		List<String> mem = new ArrayList<String>(); 
		int count = 0;
		while (count < Math.pow(2,24)-1) { mem.add(""); count++; }
		return mem; 
	}

	public static void main(String [] args) throws IOException{
		//		String[] test = {"tracefile_simple", "1024", "4", "32"};

		String fileName = args[0]; 
		int size = Integer.parseInt(args[1]);
		int asso = Integer.parseInt(args[2]);
		int block = Integer.parseInt(args[3]); 
		//
		//		String fileName = test[0]; 
		//		int size = Integer.parseInt(test[1]);
		//		int asso = Integer.parseInt(test[2]);
		//		int block = Integer.parseInt(test[3]); 

		cachesim cs = new cachesim(size, asso, block); 
		List<Instruction> myInstructions = buildInstructions(fileName); 
		for (int i=0; i<myInstructions.size(); i++) {
			System.out.println(processInstructions(myInstructions.get(i), i+1));
		}

	}
}
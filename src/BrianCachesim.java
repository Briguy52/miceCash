import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BrianCachesim {

	private List<Instruction> myInstructions; 
	private String fileName; 
	private int cacheSize;
	private int numWays; // associativity
	private int blockSize; 
	private int numBlocks;
	private int numSets;  
	private int offsetBits; 
	private int indexBits; 

	private Map<Integer, List<oldBlock>> myCache;
	private Map<Integer, String> myMemory; 

	public BrianCachesim() {}; 

	public BrianCachesim(String fileName, int cacheSize, int numWays, int blockSize) {
		this.fileName = fileName; 
		this.cacheSize = cacheSize;
		this.numWays = numWays;
		this.blockSize = blockSize; 
		this.init(); 
	}

	private void init() {
		this.myCache = new HashMap<Integer, List<oldBlock>>();
		this.myMemory = new HashMap<Integer, String>();

		this.numBlocks = this.cacheSize / this.blockSize;
		this.numSets = this.numBlocks / this.numWays; 

		this.offsetBits = this.logBase2(this.blockSize); 
		this.indexBits = this.logBase2(this.numSets);

		this.myInstructions = this.buildInstructions();
		this.loopThroughInstructions(this.myInstructions);
	}

	// Change of base from nat. log to 2 
	private int logBase2(int value) {
		return (int) (Math.log(value) / Math.log(2));
	}

	private void loopThroughInstructions(List<Instruction> instructions) {
		instructions.stream().forEach(instr -> this.execute(instr));
	}

	private void execute(Instruction instr) {

		// TODO: refactor
		int tag = this.parseTag(instr.getAddress()); // tag is to the 'left' of index + offset
		int blockOffset = this.parseBlkOff(instr.getAddress());
		int index = this.parseIndex(instr.getAddress());

		//		int numBytes = instr.getNumBytes();
		String writeVal = this.padWithZeroes(instr.getWriteValue(), instr.getNumBytes() * 8);
		String hexAddress = this.padWithZeroes(Integer.toString(instr.getAddress()), 6);
		List<oldBlock> cacheSet = this.myCache.get(index);
		switch (instr.getInstrType()) {
		case "store": this.store(cacheSet, index, tag, instr.getNumBytes(), instr.getAddress(), blockOffset, writeVal);
		break; 
		case "load": this.load(cacheSet, index, tag, instr.getNumBytes(), instr.getAddress(), blockOffset); 
		break;
		}

	}

	private void load(List<oldBlock> cacheSet, int index, int tag, int numBytes, int address, int blkOff) {
		if (!cacheSet.contains(new oldBlock(tag, this.blockSize)) || cacheSet == null) { 
			System.out.println("Awww miss");
			// Begin loading in a new Block
			oldBlock block = new oldBlock(tag, this.blockSize); 
			int start = address - blkOff;
			// Let's build a Block (byte by byte...)
			for (int i = start; i < (this.blockSize + start); i++) {
				String memVal = this.myMemory.get(i); 
				if (memVal == null) { // not found
					memVal = "00000000"; // 8 bits of 0
				}
				block.writeByte(memVal, blkOff + i - address);
				if (this.needsPadding(i, address, numBytes)) {
					String toPad = this.makeHex(memVal);
					String afterPad = this.padWithZeroes(toPad, 2);
					System.out.println(afterPad); // for testing
				}
			}
			// Now we need to make/find a set to put this in 
			if (cacheSet == null) {
				// No set, make a new one
				List<oldBlock> set = new ArrayList<oldBlock>();
				set.add(block);
				this.myCache.put(index, set);
			}
			else {
				if (!this.setFull(cacheSet, this.numWays)) {
					// LRU eviction policy
					cacheSet.remove(0); 
				}
				cacheSet.add(block); 
			}
		}
		else {
			System.out.print("Woooo hit!");
			oldBlock block = cacheSet.remove(cacheSet.indexOf(new oldBlock(tag, this.blockSize)));
			cacheSet.add(block); 
			String readVal = block.read(blkOff, numBytes);
			String toPad = this.makeHex(readVal);
			String afterPad = this.padWithZeroes(toPad, 2);
			System.out.println(afterPad); 
		}
	}

	private boolean setFull(List<oldBlock> set, int numWays) {
		return set.size() >= numWays; 
	}

	private String makeHex(String in) {
		return Integer.toHexString(Integer.parseInt(in, 2)); // radix 2, then to hex
	}

	private boolean needsPadding(int currByte, int address, int numBytes) {
		return currByte >= address && (address + numBytes) > currByte; 
	}

	private void store(List<oldBlock> cacheSet, int index, int tag, int numBytes, int address, int blkOff, String writeVal) {
		if (!cacheSet.contains(new oldBlock(tag, this.blockSize)) || cacheSet == null) { 
			System.out.println("Awww miss");
		}
		else { 
			System.out.println("Wooo hit!");
			oldBlock block = cacheSet.remove(cacheSet.indexOf(new oldBlock(tag, this.blockSize))); 
			// rewrite data & "refresh" cache entry by re-adding to end
			block.write(writeVal, blkOff, numBytes);
			cacheSet.add(block);
		}
		// Write through (cycle through each) 
		for (int i = 0; i < numBytes; i++) {
			this.myMemory.put(address + i, this.bytesToWrite(writeVal, i));							
		}
	}
	
	private String bytesToWrite(String full, int current) {
		return full.substring(current * 8, (current + 1) * 8); 
	}

	private String padWithZeroes(String val, int expectedLength) {
		String out = val; 
		for (int i=0; i< (expectedLength - val.length()); i++) {
			out = "0" + out; // prepend a 0 to pad
		} 
		return out; 
	}

	public static int kbToByte(int kb) {
		return (int) (kb * 1024);
	}

	private int parseIndex(int address) {
		int indexBits = address >> this.offsetBits;
		return indexBits % this.numSets;
	}

	private int parseBlkOff(int address) {
		return address % this.offsetBits; 
	}

	private int parseTag(int address) {
		return address >> (this.indexBits + this.offsetBits);
	}

	// TODO: complete
	public List<Instruction> buildInstructions() {
		List<Instruction> out = new ArrayList<Instruction>(); 
		try {
			BufferedReader myReader = new BufferedReader (new FileReader(this.fileName));
			int counter = 1; 
			String line = myReader.readLine();
			while (line != null){
				String[] lineSplit = line.split(" ");
				String type = lineSplit[0]; // 'load' or 'store' 
				int address = Integer.toBinaryString(Integer.parseInt(lineSplit[1], 16)); 
				int numBytes = 
				System.out.println(address);
				boolean hit = false; 
				int offset = calcOffset(address);
				int index = calcIndex(address); 
				String tag = calcTag(address);
				counter += 1;
				line = myReader.readLine();
				System.out.println(cs.instructionProcess(line, counter));
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//		try {
//			Scanner scanner = new Scanner(new File(this.fileName));
//			while(scanner.hasNextLine()) {
//				Scanner nextLine = new Scanner(scanner.nextLine());
//				String instrType = nextLine.next(); 
//				int address = Integer.decode(nextLine.next());
//				int numBytes = Integer.parseInt(nextLine.next());
//				// TODO: do rest of this scanner stuff
//				Instruction instr;
//				if (nextLine.hasNext()) {
//					instr = new Instruction(address, 
//											numBytes,
//											instrType,
//											Integer.toBinaryString(Integer.parseInt(nextLine.next(), 16)));
//				}
//				else {
//					instr = new Instruction(address, numBytes, instrType); 
//				}
//				out.add(instr);
//			}
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found");
		}
		return out; 
	}

	public static void main(String[] args) {
		// args comes in the form: cachesim tracefile 1024 4 32
//		BrianCachesim c = new BrianCachesim(args[0],
//				BrianCachesim.kbToByte(Integer.parseInt(args[1])),
//				Integer.parseInt(args[2]),
//				Integer.parseInt(args[3])
//				); 
		String[] test = {"tracefile_simple", "1024", "4", "32"};
		BrianCachesim c = new BrianCachesim(test[0],
				BrianCachesim.kbToByte(Integer.parseInt(test[1])),
				Integer.parseInt(test[2]),
				Integer.parseInt(test[3])
				); 
	}
}

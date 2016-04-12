import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Cachesim {

	private List<Instruction> myInstructions; 
	private String fileName; 
	private int cacheSize;
	private int numWays; // associativity
	private int blockSize; 
	private int numBlocks;
	private int numSets;  
	private int offsetBits; 
	private int indexBits; 
	
	private Map<Integer, List<Block>> myCache;
	private Map<Integer, String> myMemory; 
	
	public Cachesim() {}; 
	
	public Cachesim(String fileName, int cacheSize, int numWays, int blockSize) {
		this.fileName = fileName; 
		this.cacheSize = cacheSize;
		this.numWays = numWays;
		this.blockSize = blockSize; 
		this.init(); 
	}
	
	private void init() {
		this.myCache = new HashMap<Integer, List<Block>>();
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
		int tag = (instr.getAddress() >> (this.indexBits + this.offsetBits));
		int offset = instr.getAddress() % this.offsetBits;
		int index = (instr.getAddress() >> this.offsetBits) % this.numSets;
		
//		int numBytes = instr.getNumBytes();
		String writeVal = this.padWithZeroes(instr.getWriteValue(), instr.getNumBytes() * 8);
		String hexAddress = this.padWithZeroes(Integer.toString(instr.getAddress()), 6);
		switch (instr.getInstrType()) {
			case "store": this.store(index, tag, instr.getNumBytes(), instr.getAddress());
						  break; 
			case "load": this.load(); 
						 break;
		}
		
	}
	
	private void load() {
		
	}
	
	private void store(int index, int tag, int numBytes, int address) {
		List<Block> cacheSet = this.myCache.get(index);
		if (!cacheSet.contains(new Block(tag, this.blockSize)) || cacheSet == null) { 
			System.out.println("Awww miss");
		}
		else { 
			System.out.println("Wooo hit!");
			Block block = cacheSet.remove(cacheSet.indexOf(new Block(tag, this.blockSize))); 
			// rewrite data & "refresh" cache entry by re-adding to end
			block.writeToEnd(offset, numBytes, writeValue);
			cacheSet.add(block);
		}
		// write through to main memory regardless
			// create new bytes in mem and write to them if not present or overwrite old bytes
			for (int i = 0; i < numBytes; i++) {
				this.myMemory.put(address + i, writeValue.substring(i*8, (i+1)*8));							
			}
	}
	
	private String padWithZeroes(String val, int expectedLength) {
		String out = val; 
		for (int i=0; i< (expectedLength - val.length()); i++) {
			out = "0" + out; // prepend a 0 to pad
		} 
		return out; 
	}
	
	public static int kbToByte(int kb) {
		return (int) (kb * (Math.pow(2, 10)));
	}

	// TODO: complete
	public List<Instruction> buildInstructions() {
		List<Instruction> out = new ArrayList<Instruction>(); 
		try {
			Scanner scanner = new Scanner(new File(this.fileName));
			while(scanner.hasNextLine()) {
				Scanner nextLine = new Scanner(scanner.nextLine());
				int address = Integer.decode(nextLine.next());
				int numBytes = Integer.parseInt(nextLine.next());
				// TODO: do rest of this scanner stuff
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
		return out; 
	}

	public static void main(String[] args) {
		// args comes in the form: cachesim tracefile 1024 4 32
		Cachesim c = new Cachesim(args[0],
								  Cachesim.kbToByte(Integer.parseInt(args[1])),
								  Integer.parseInt(args[2]),
								  Integer.parseInt(args[3])
								  ); 
		
	}
}

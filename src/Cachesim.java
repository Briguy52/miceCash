import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
	
	public Cachesim() {}; 
	
	public Cachesim(String fileName, int cacheSize, int numWays, int blockSize) {
		this.fileName = fileName; 
		this.cacheSize = cacheSize;
		this.numWays = numWays;
		this.blockSize = blockSize; 
		this.init(); 
	}
	
	private void init() {
		this.numBlocks = this.cacheSize / this.blockSize;
		this.numSets = this.numBlocks / this.numWays; 
		
		this.offsetBits = this.logBase2(this.blockSize);
		this.indexBits = this.logBase2(this.numSets);
		
		Block block = new Block(this.blockSize); 
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
		int address = inst.address;
		int offset = address % offsetBitSize;
		int index = (address >> offsetBitSize) % numSets;
		int tag = (address >> (offsetBitSize + indexBitSize));
		int numBytes = inst.numBytes;
		String writeValueIncomplete = inst.writeValue; // possibly missing leading zeros
		String writeValue = writeValueIncomplete;

	}
	
	public static int kbToByte(int kb) {
		return (int) (kb * (Math.pow(2, 10)));
	}

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

import java.util.*;
import java.io.*; 
import java.math.*;

public class CacheTest{
	
	private static class Cache{
		boolean valid;
		boolean dirty;
		String tag; 
		int timeStamp; 
		List<String> data;
		
		//Creating the cache object that holds necessary info
		public Cache (boolean va, boolean di, String ta, int tS, List<String> d){
			valid = va;
			di = dirty;
			tag = ta;
			timeStamp = tS; //Which line of instruction accessed this mem. addr. 
			data = d; 
		}
	}
	
	private static int cacheSize;
	private static int assoc; 
	private static int blockSize;
	private static int bitsIndex;
	private static int bitsOffset; 
	private static List<String> mainMemory;
	private static HashMap<Integer, List<Cache>> cache; 
	
	//Constructor for the cache
	public CacheTest (int size, int associativity, int blocks){
		cacheSize = size * 1024; 
		assoc = associativity;
		blockSize = blocks;
	}
	
	public static HashMap<Integer, List<Cache>> makeCache(){
		HashMap<Integer, List<Cache>> cacheNew = new HashMap<Integer, List<Cache>>();
//		System.out.println(blockSize);
		int numBlocks = cacheSize / blockSize; 
		int numSets = numBlocks / assoc; 
		int set = 0; 
		
		//Initializing Cache
		for(int i = 0; i < numSets; i++){
			List<Cache> newArray = new ArrayList<Cache>(); 
			List<String> dataToPut = new ArrayList<String>(); 
			for (int j = 0; j < assoc; j++){
				Cache cacheToPut = new Cache(false, false, "", 0, dataToPut); 
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
	
	//Calculate the index of a given memory address
	public static int calcIndex (String memAddress){
		double cap = Math.log(cache.size()) / Math.log(2); //Taking log base 2 of cache size
		bitsIndex = (int) cap; 
		
		String binaryAdd = Integer.toBinaryString(Integer.parseInt(memAddress.substring(2), 16)); //Converting from hex to int decimal to string binary
		//If index is size 0;
		if(bitsIndex == 0){
			return 0;
		}
		while (binaryAdd.length() < 24){
			binaryAdd = "0" + binaryAdd; 
		}
		//Returns int decimal of binary string
		return Integer.parseInt(binaryAdd.substring(binaryAdd.length() - bitsOffset - bitsIndex, binaryAdd.length() - bitsOffset) , 2); 
	}
	
	//Calculate the tag of a given memory address
	public static String calcTag (String memAddress){
		String binaryAdd = Integer.toBinaryString(Integer.parseInt(memAddress.substring(2), 16));
		while(binaryAdd.length() < 24){
			binaryAdd = "0" + binaryAdd; 
		}
		
		return binaryAdd.substring(0, binaryAdd.length() - bitsOffset - bitsIndex); 
	}
	
	//Calculate the offset of a given memory address
	public static int calcOffset (String memAddress){
		double cap = Math.log(blockSize) / Math.log(2);
		bitsOffset = (int) cap; 
//		System.out.println(memAddress);
		String binaryAdd = Integer.toBinaryString(Integer.parseInt(memAddress.substring(2), 16));
		while(binaryAdd.length() < 24){
			binaryAdd = "0" + binaryAdd; 
		}
		return Integer.parseInt(binaryAdd.substring(binaryAdd.length() - bitsOffset, binaryAdd.length()), 2); 
	}
	
	public static String instructionProcess (String instruction, int pcCounter){
		String[] parse = instruction.split(" ");
		String address = parse[1]; 
		System.out.println(address);
		boolean hit = false; 
		int offset = calcOffset(address);
		int index = calcIndex(address); 
		String tag = calcTag(address);
		int hitIndex = 0; 
		
		if(parse.length == 4){ //store instruction found
			 for (int i = 0; i < cache.get(index).size(); i++){
				 Cache sample = cache.get(index).get(i); 
				 if(sample.tag.equals(tag)){ //Match 
					 hit = true; 
					 hitIndex = i;
					 break; 
				 }
			 }
			 if (hit){
				 for (int i = 0; i < Integer.parseInt(parse[2]); i++){
					 cache.get(index).get(hitIndex).data.set(offset + i, parse[3].substring(index, i+2)); 
				 }
				 cache.get(index).get(hitIndex).dirty = true; 
				 return "split" + " " + parse[1] + " hit"; 
			 }
			 else{
				 String s0 = "";
				 String s1 = "";
				 
				 for(int i = 0; i < bitsOffset; i++){
					 s0 += "0";
					 s1 += "1"; 
				 }
				 
				 String binaryAdd = Integer.toBinaryString(Integer.parseInt(address.substring(2), 16));
				 while (binaryAdd.length() < 24){
					 binaryAdd = "0" + binaryAdd; 
				 }
				 String indexString = binaryAdd.substring(binaryAdd.length() -bitsOffset - bitsIndex, binaryAdd.length() - bitsOffset);
				 String binaryAdd1 = tag + indexString + s0; 
				 String binaryAdd2 = tag + indexString + s1; 
				 int lower = Integer.parseInt(binaryAdd1, 2);
				 int upper = Integer.parseInt(binaryAdd2, 2); 
				 
				 for (int i = 0; i< Integer.parseInt(parse[2]); i++){
					 mainMemory.set(lower + i, parse[3].substring(2*i, 2*i+2));
				 }
				 
				 //This may be wrong. arg 5
				 Cache cacheNew = new Cache(true, false, tag, pcCounter, mainMemory.subList(lower, upper + 1)); 
				 boolean full = true; 
				 
				 for (int i = 0; i < cache.get(index).size(); i++){
					 Cache sample = cache.get(index).get(i);
					 
					 if(!sample.valid){
						 cache.get(index).set(i, cacheNew);
						 full = false;
						 break; 
					 }
				 }
				 
				 if (full){
					 int indexMin = 0;
					 int absMin = Integer.MAX_VALUE; 
					 
					 for(int i = 0; i < cache.get(index).size(); i++){
						 if (cache.get(index).get(i).timeStamp < absMin){
							 absMin = cache.get(index).get(i).timeStamp;
							 indexMin = i; 
						 }
					 }
					 if (cache.get(index).get(indexMin).dirty){
						 for (int i = lower; i <= upper; i++){
							 mainMemory.set(i, cache.get(index).get(indexMin).data.get(i-lower)); 
						 }
					 }
					 cache.get(index).set(indexMin, cacheNew); 
				 }
				 return "store" + " " + parse[1] + " miss"; 
			 }
		}
		else{
			for (int i = 0; i < cache.get(index).size(); i++){
				Cache sample = cache.get(index).get(i); 
				
				if(sample.tag.equals(tag)){
					hit = true;
					hitIndex = i;
					break;
				}
			}
			if (hit) {
				String value = "";
				for (int i = 0; i < Integer.parseInt(parse[2]); i++){
					if (cache.get(index).get(hitIndex).data.get(offset + i).equals("")){
						value += "00";
					}
					else{
						value += cache.get(index).get(hitIndex).data.get(offset + i);  
					}
				}
				return parse[0] + " " + parse[1] + " hit " + value; 
			}
			else{
				String s0 = "";
				String s1 = "";
				for (int i = 0; i < bitsOffset; i++){
					s0 += "0";
					s1 += "1";
				}
				String binaryAdd = Integer.toBinaryString(Integer.parseInt(address.substring(2), 16)); 
				while (binaryAdd.length() < 24){
					binaryAdd = "0" + binaryAdd; 
				}
				String sIndex = binaryAdd.substring(binaryAdd.length() - bitsOffset - bitsIndex, binaryAdd.length() - bitsOffset);
				String bAdd0 = tag + sIndex + s0;
				String bAdd1 = tag + sIndex + s1;
				int lower = Integer.parseInt(bAdd0, 2);
				int upper = Integer.parseInt(bAdd1, 2); 
				Cache sample = new Cache(true, false, tag, pcCounter, mainMemory.subList(lower,  upper + 1));
				boolean full = true;
				
				for(int i = 0; i< cache.get(index).size(); i++){
					Cache test = cache.get(index).get(i);
					if (!test.valid){
						cache.get(index).set(index,  sample); 
						full = false;
						break; 
					}
				}
				if (full){
					int minI = 0;
					int absMin = Integer.MAX_VALUE;
					for (int i = 0; i < cache.get(index).size(); i++){
						if (cache.get(index).get(i).timeStamp < absMin){
							absMin = cache.get(index).get(i).timeStamp;
							minI = i;
						}
					}
					if (cache.get(index).get(minI).dirty){
						for(int i = lower; i <= upper; i++){
							mainMemory.set(i, cache.get(index).get(minI).data.get(i = lower));
						}
					}
					cache.get(index).set(minI, sample);
				}
				String value ="";
				for (int i = 0; i<Integer.parseInt(parse[2]); i++){
					if (sample.data.get(offset + i).equals("")){
						value += "00";
					}
					else{
						value += sample.data.get(offset+i);
					}
				}
				return parse[0] + " " + parse[1] + " miss " +value; 
			}
		}
	}
	
	public static void main(String [] args) throws IOException{
		
		String[] test = {"tracefile_simple", "1024", "4", "32"};
		
		String fileName = test[0]; 
		int size = Integer.parseInt(test[1]);
		int asso = Integer.parseInt(test[2]);
		int block = Integer.parseInt(test[3]); 
		
		BufferedReader br = new BufferedReader (new FileReader(fileName));
//		int size = Integer.parseInt(args[1]);
//		int asso = Integer.parseInt(args[2]);
//		int block = Integer.parseInt(args[3]); 
//		System.out.println(block);
		CacheTest cs = new CacheTest(size, asso, block); 
		cache = cs.makeCache(); 
		mainMemory = cs.makeMemory();
		int counter = 1; 
		String line = br.readLine();
		while (line != null){
			System.out.println(cs.instructionProcess(line, counter));
			counter += 1;
			line = br.readLine();
		}
	}
}
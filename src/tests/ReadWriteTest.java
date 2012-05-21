package tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.*;

import core.*;

public class ReadWriteTest {
	public static void main(String[] args) throws IOException, ImportException {
		String test = "test4.nex";
		NexusReader reader = new NexusReader(test);
		List<Tree> trees1 = reader.readTrees();

		reader.setFile(test);
		List<Tree> trees2 = reader.readTrees();
		
		//Test if reading in the same trees twice creates two equal tree objects.
		for(int i = 0; i < trees1.size(); i++) {
			System.out.print("Tree read in twice equal to itself: ");
			System.out.println(RootedTreeUtils.equal((RootedTree) trees1.get(i), (RootedTree) trees2.get(i)));
		}
		
		String out = "out.nex";
		NexusWriter writer = new NexusWriter(out);
		writer.writeTrees(trees1);
		
		reader.setFile(out);
		List<Tree> trees3 = reader.readTrees();
		
		//Test if a tree read, written, read again matches itself unchanged.
		for(int i = 0; i < trees1.size(); i++) {
			System.out.print("Tree read, written, read again equal to itself: ");
			System.out.println(RootedTreeUtils.equal((RootedTree) trees1.get(i), (RootedTree) trees3.get(i)));
		}
		
		
	}

}

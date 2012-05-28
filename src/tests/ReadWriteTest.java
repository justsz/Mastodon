package tests;

import java.io.IOException;
import java.util.List;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.*;

import core.*;

public class ReadWriteTest {
	public static void main(String[] args) throws IOException, ImportException {
		//String test = "newickTest.txt";
		String test = "test4.nex";
		TreeReader reader = new TreeReader(test);
		List<SimpleRootedTree> trees1 = reader.readSimpleRootedTrees();

		reader.reset();
		List<SimpleRootedTree> trees2 = reader.readSimpleRootedTrees();
		
		//Test if reading in the same trees twice creates two equal tree objects.
		for(int i = 0; i < trees1.size(); i++) {
			System.out.print("Tree" + i + " read in twice equal to itself: ");
			System.out.println(RootedTreeUtils.equal((RootedTree) trees1.get(i), (RootedTree) trees2.get(i)));
		}
		
		String out = "out.nex";
		NexusWriter writer = new NexusWriter(out);
		writer.writeTrees(trees1);
		
		reader.setFile(out);
		List<SimpleRootedTree> trees3 = reader.readSimpleRootedTrees();
		
		//Test if a tree read, written, read again matches itself.
		for(int i = 0; i < trees1.size(); i++) {
			System.out.print("Tree read, written, read again equal to itself: ");
			System.out.println(RootedTreeUtils.equal(trees1.get(i), trees3.get(i)));
		}
		
		
	}

}

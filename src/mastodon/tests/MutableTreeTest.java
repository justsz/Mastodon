package mastodon.tests;

import java.io.IOException;
import java.util.List;

import mastodon.core.NexusWriter;
import mastodon.core.TreeReader;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.SimpleRootedTree;

public class MutableTreeTest {
	public static void main(String[] args) throws IOException, ImportException{
		String test = "test4.nex";
		TreeReader reader = new TreeReader(test);

		List<MutableRootedTree> mutableTrees = reader.readMutableRootedTrees();
		
		reader.reset();
		List<SimpleRootedTree> simpleTrees = reader.readSimpleRootedTrees();

		System.out.println("Mutable rooted trees equal to simple rooted trees: ");
		for(int i = 0; i < mutableTrees.size(); i++) {
			if (!RootedTreeUtils.equal(mutableTrees.get(i), (RootedTree) simpleTrees.get(i)))
				System.out.println(false);
			else 
				System.out.println(true);
		}
		
		NexusWriter writer = new NexusWriter("out.nex");
		writer.writeTrees(mutableTrees);
		
	}
}

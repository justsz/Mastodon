package mastodon.tests;

import java.io.IOException;
import java.util.List;

import mastodon.core.NexusWriter;
import mastodon.core.TreeReader;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.CompactRootedTree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.SimpleRootedTree;

public class CompactTreeTest {
	public static void main(String[] args) throws IOException, ImportException{
		String test = "test4.nex";
		TreeReader reader = new TreeReader(test);

		List<CompactRootedTree> compactTrees = reader.readCompactRootedTrees();

		reader.reset();
		List<SimpleRootedTree> simpleTrees = reader.readSimpleRootedTrees();

		System.out.print("Compact rooted trees equal to simple rooted trees: ");
		for(int i = 0; i < compactTrees.size(); i++) {
			if (!RootedTreeUtils.equal(compactTrees.get(i), (RootedTree) simpleTrees.get(i)))
				System.out.println(false);
			else 
				System.out.println(true);
		}
		
		NexusWriter writer = new NexusWriter("out.nex");
		writer.writeTrees(compactTrees);
		
	}
}

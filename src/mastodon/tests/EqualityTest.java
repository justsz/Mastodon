/**
 * 
 */
package mastodon.tests;

import java.io.IOException;
import java.util.List;

import mastodon.core.TreeReader;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.SimpleRootedTree;


/**
 * @author justs
 *
 */
public class EqualityTest {

	public static void main(String[] args) throws IOException, ImportException {
		String test = "tiny.trprobs";
		TreeReader reader = new TreeReader(test);
		List<SimpleRootedTree> trees = reader.readSimpleRootedTrees();
		
		//should be false
		System.out.println(RootedTreeUtils.equal(trees.get(0), trees.get(1)));

	}

}

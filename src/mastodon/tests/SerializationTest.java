/**
 * 
 */
package mastodon.tests;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jebl.evolution.io.ImportException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import mastodon.core.BitTreeSystem;
import mastodon.core.RunResult;
import mastodon.core.TreeReader;

/**
 * @author justs
 *
 */
public class SerializationTest {
	public static void main(String args[]) throws IOException, ImportException {
		String test = 
				"simple.trees";
		//				"snowflake-48d500.trees";
		//				"carnivores1k.trprobs";
		//				"carnivores1kUnWeighted.trprobs";
		//				"H3N2_1441_tips.500.trees";
		//				"H3N2_1968-2011.338_tips.500.trees";

		TreeReader reader = new TreeReader(test);
		BitTreeSystem bts = new BitTreeSystem();
		List<RootedTree> trees;

		double start = System.currentTimeMillis();
		do {
			trees = reader.read100RootedTrees();
			bts.addTrees(trees);
		} while (trees.size() == 100);
		System.out.println(bts.getClades().size());
		trees = null;	//signals to the GC that this can be disposed of
		bts.findMapTree();
		System.out.println("tree adding time: " + (System.currentTimeMillis() - start));


		List<ArrayList<Taxon>> a = new ArrayList<ArrayList<Taxon>>();
		a.add(new ArrayList<Taxon>());
		List<BitSet> b = new ArrayList<BitSet>();
		b.add(new BitSet());
		List<double[]> c = new ArrayList<double[]>();
		c.add(bts.pruneFast(new BitSet()));
		bts.unPrune();
		List<SimpleRootedTree> d = new ArrayList<SimpleRootedTree>();
		d.add(bts.reconstructMapTree(null, null));
		Map<Taxon, Double> e = new HashMap<Taxon, Double>();
		for (Taxon taxon : bts.getAllTaxa()) {
			e.put(taxon, 0.0);
		}

		RunResult emptyResult = new RunResult(bts, a, b, c, d, e, "Manual", 0, 0);

		try
		{
			FileOutputStream fileOut =
					new FileOutputStream("employee.ser");
			ObjectOutputStream out =
					new ObjectOutputStream(fileOut);
			out.writeObject(emptyResult);
			out.close();
			fileOut.close();
		}catch(IOException i)
		{
			i.printStackTrace();
		}

		
		RunResult readResult = null;
		try
		{
			FileInputStream fileIn =
					new FileInputStream("employee.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			readResult = (RunResult) in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException i)
		{
			i.printStackTrace();
			return;
		}catch(ClassNotFoundException caa)
		{
			//System.out.println(.RunResult class not found.);
			caa.printStackTrace();
			return;
		}
		
		System.out.println("hey!");
	}
	
	
}

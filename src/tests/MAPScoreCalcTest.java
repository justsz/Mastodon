package tests;

import java.io.IOException;
import java.util.List;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import core.MAPScoreCalculator;
import core.TreeReader;

/**
 * @author justs
 *
 */
public class MAPScoreCalcTest {

	public static void main(String[] args) throws IOException, ImportException {
		String test = "carnivores.trprobs";
		//String test = "number19.txt";
		TreeReader reader = new TreeReader(test);
		List<MutableRootedTree> trees = reader.readMutableRootedTrees();
		
		MAPScoreCalculator calc = new MAPScoreCalculator();
		float score = calc.getMAPScore(trees.get(0), trees);
		System.out.println("score: " + score);
	}

}

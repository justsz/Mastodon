/**
 * 
 */
package core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;

/**
 * @author justs
 *	some stuff (c) BEAST
 */
public class CladeSystem {


	public CladeSystem() {
	}

	/**
	 */
	public CladeSystem(Tree targetTree) {
		this.targetTree = targetTree;
		add(targetTree, true);
	}

	/**
	 * adds all the clades in the tree
	 */
	public void add(Tree tree, boolean includeTips) {
		if (taxonList == null) {
			taxonList = new ArrayList<Taxon>(tree.getTaxa());	//might need cloning
		}

		// Recurse over the tree and add all the clades (or increment their
		// frequency if already present). The root clade is added too (for
		// annotation purposes).
		addClades(tree, tree.getRoot(), includeTips);
	}
	//
	//	        public Clade getClade(NodeRef node) {
	//	            return null;
	//	        }

	private BitSet addClades(Tree tree, Node node, boolean includeTips) {

		BitSet bits = new BitSet();

		if (tree.isExternal(node)) {

			int index = taxonList.getTaxonIndex(tree.getTaxon(node).getId());
			bits.set(index);

			if (includeTips) {
				addClade(bits);
			}

		} else {

			for (int i = 0; i < tree.getChildCount(node); i++) {

				NodeRef node1 = tree.getChild(node, i);

				bits.or(addClades(tree, node1, includeTips));
			}

			addClade(bits);
		}

		return bits;
	}

	private void addClade(BitSet bits) {
		Clade clade = cladeMap.get(bits);
		if (clade == null) {
			clade = new Clade(bits);
			cladeMap.put(bits, clade);
		}
		clade.setCount(clade.getCount() + 1);
	}

	public Map getCladeMap() {
		return cladeMap;
	}

	private double getCladeCredibility(BitSet bits) {
		Clade clade = cladeMap.get(bits);
		if (clade == null) {
			return 0.0;
		}
		return clade.getCredibility();
	}



	//
	// Private stuff
	//
	List<Taxon> taxonList = null;
	Map<BitSet, Clade> cladeMap = new HashMap<BitSet, Clade>();

	Tree targetTree;

}

package core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.RootedTree;

/**
 * This class contains methods for the manipulation of Trees using BitSets.
 * @author justs
 * @author Creating clade summary code adapted from CladeSystem in BEAST's code.
 */
/**
 * @author justs
 *
 */
public class CopyOfBitTreeSystem {
	//	private List<? extends RootedTree> trees;
	private Set<Taxon> taxa;	//Set of all unique taxa
	private Map<BitSet, Integer> clades;	//Map of unique clades and their frequencies of appearence in the set of trees
	private boolean newTree = false;	//used to mark the beginning of a new tree during the search for unique clades
	private List<BitSet> bitTree;	//temporary storage for BitSet representation of individual trees
	private List<BitTree> bitTrees;
	private int treeCount;
	private boolean weighted;


	/**
	 * Object will hold all information about the set of trees needed for their analysis.
	 */
	public CopyOfBitTreeSystem() {
		treeCount = 0;
		weighted = true;
		this.taxa = new LinkedHashSet<Taxon>();
		this.clades = new HashMap<BitSet, Integer>();
		this.bitTrees = new ArrayList<BitTree>();

		//construct full set of taxa
		//		for(RootedTree tree : trees) {
		//			taxa.addAll(tree.getTaxa());
		//		}
	}
	
	/**
	 * Converts the input RootedTrees to BitTrees.
	 * Creates a central Map of all unique clades and their frequencies of appearance in the set of trees. Returns a List of Lists of BitSets that are linked to the central 
	 * Map of clades and represent the tree set.
	 * @param trees - trees to convert
	 */

	public void addTrees(List<? extends RootedTree> trees) {
		//need to do this for every tree? Can it be assumed that all trees have the same taxa list?
		for(RootedTree tree : trees) {
			taxa.addAll(tree.getTaxa());
		}

		for(RootedTree tree : trees) {
			//double start = System.currentTimeMillis();
			addClades(tree, tree.getRootNode());
			//System.out.print (System.currentTimeMillis() - start);
			//System.out.println("\t" + clades.size());
			float weight = -1;	//signifies unweighted tree
			if(tree.getAttribute("weight") != null) {	//checking this might be better to put in a pre-processing stage
				if(!weighted) {
					System.out.println("Weight annotation error. Not all trees are weighted. Exiting.");
					System.exit(2);
				}
				weighted = true;
				weight = (Float) tree.getAttribute("weight");
			} else {
				if(weighted && treeCount != 0) {
					System.out.println("Weight annotation error at: " + tree.getAttributeMap() + " Exiting.");
					System.exit(2);
				}
				weighted = false;
			}
			bitTrees.add(new BitTree(bitTree, weight));
			newTree = false;
			
		}

		treeCount += trees.size();
	}
	
	/**
	 * Returns the list of BitTrees in this system.
	 * @return list of stored BitTrees
	 */
	public List<BitTree> getBitTrees() {
		//System.out.println("treeCount=" + treeCount + "\tbitTrees=" + bitTrees.size() + "\ttaxaCount=" + taxa.size());
		return bitTrees;
	}


	/**
	 * Recursively traverses a tree to extract all unique clades.
	 * @param tree - tree to analyse
	 * @param node - call function from outside it with tree's root node
	 * @return the clade of the entire tree
	 */
	private BitSet addClades(RootedTree tree, Node node) {
		BitSet bits = new BitSet();
		if (tree.isExternal(node)) {
			int index = getIndex(taxa, tree.getTaxon(node));
			if(index < 0) {
				System.out.println("Taxon not found during BitTree construction. Exiting.");
				System.exit(1);	//Maybe create proper exception.
			}
			bits.set(index);

			//uncomment to include single tip clades
			//addClade(bits);
		} else {
			for(Node node1 : tree.getChildren(node)) {
				bits.or(addClades(tree, node1));
			}
			addClade(bits);
		}
		return bits;
	}


	/**
	 * Add given clade to the central list of unique clades
	 * @param bits - BitSet clade to add
	 */
	private void addClade(BitSet bits) {
		//reset clade list for each new tree
		if(!newTree) {
			bitTree = new ArrayList<BitSet>();
			newTree = true;
		}

//		double start = System.currentTimeMillis();
		Integer bset = clades.get(bits);
		if (bset == null) {
//		if (!clades.containsKey(bits)) {
			clades.put(bits, 1);
		} else {
			clades.put(bits, clades.get(bits) + 1);
		}

		
//		for(BitSet s : clades.keySet()) {
//			if(s.equals(bits)) {	//removed a cast to BitSet on bits. Check it didn't break anything.
//				bitTree.add(s);
//				break;
//			}
//		}
		
		//this makes sure all trees' clades are referenced to the central list of unique clades
		for (Map.Entry<BitSet, Integer> entry : clades.entrySet()) {
			if(entry.getKey().equals(bits)) {
				bitTree.add(entry.getKey());
				break;
			}
        }
		
		
//		bitTree.add(bits);
//		double timer = System.currentTimeMillis() - start;
//		if (timer > 3)
//		System.out.println(timer);
	}

	/**
	 * If the trees are weighted, returns the max weight tree. Otherwise returns the index of the tree that has the maximum probability clades. 
	 * @return index of the MAP tree
	 */
	public int getMapTreeIndex() {	//this could all be done at creation time
		int index = 0;
		if(weighted) {
			float maxWeight = 0.0f;
			for(int i = 0; i < treeCount; i++) {
				float weight = bitTrees.get(i).getWeight();
				if(weight > maxWeight) {
					maxWeight = weight;
					index = i;
				}
			}
		} else {

			long[] scores = new long[treeCount];
			for(int i = 0; i < scores.length; i++) {
				long score = 0;
				BitTree tree = bitTrees.get(i);
				for(BitSet bs1 : tree.getBits()) {
					for(Map.Entry<BitSet, Integer> bs2 : clades.entrySet()) {
						if (bs1.equals(bs2.getKey())) {
							score += bs2.getValue();
						}
					}
				}
				scores[i] = score;
			}

			long maxScore = 0;

			for(int i = 0; i < scores.length; i++) {
				if(scores[i] > maxScore) {
					maxScore = scores[i];
					index = i;
				}
			}
		}
		return index;
	}

	/**
	 * Finds the index of taxon in list of taxa. Needed to know which bit to set in a BitSet representing a clade.
	 * @param taxa - list of all unique taxa in set of trees
	 * @param taxon - seeked taxon
	 * @return index of taxon in taxa
	 */
	private int getIndex(Set<Taxon> taxa, Taxon taxon) {
		Object[] taxaA = taxa.toArray();
		for(int i = 0; i < taxaA.length; i++) {
			if(taxon.equals((Taxon) taxaA[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns taxon at index from all taxa. 
	 * @param taxa - list of all unique taxa in set of trees
	 * @param index index of sought taxa
	 * @return taxon at index in set of all unique taxa
	 */
	private Taxon getTaxon(Set<Taxon> taxa, int index) {
		Object[] taxaA = taxa.toArray();
		return (Taxon) taxaA[index];
	}

	public List<Taxon> getTaxa(BitSet bits) {
		List<Taxon> taxaList = new ArrayList<Taxon>();
		for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1)) {
			taxaList.add(getTaxon(taxa, i));
		}
		return taxaList;
	}

	/**
	 * Returns Map of all unique clades in set of trees.
	 * @return Map of all unique clades in set of trees.
	 */
	public Map<BitSet, Integer> getClades() {
		return clades;
	}

	/**
	 * Returns the number of unique taxa in this system.
	 * @return number of unique taxa in system
	 */
	public int getTaxaCount() {
		return taxa.size();
	}

	/**
	 * Creates a list of nodes corresponding to a full list of nodes and BitSet of "active" nodes. 
	 * @param externalNodes - full list of nodes
	 * @param bs - "active nodes"
	 * @return list of "active nodes"
	 */
	private List<Node> getNodes(Node[] externalNodes, BitSet bs) {
		List<Node> nodes = new ArrayList<Node>();
		//iterate through set bits
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
			Node node = externalNodes[bs.nextSetBit(i)];
			if (node != null)
				nodes.add(externalNodes[bs.nextSetBit(i)]);
		}
		return nodes;
	}


	/**
	 * Reconstructs a RootedTree from a BitTree. Must be called from the same BitTreeSystem object that created the BitTree representation in the first place.
	 * @param bitTree - BitTree to reconstruct
	 * @param highlights - leaves that should be colored
	 * @return reconstructed tree object
	 */
	public SimpleRootedTree reconstructTree(BitTree bitTree, BitSet highlights) {
		//Pay attention to order and size of all the various Lists.

		//sort bitSets in ascending cardinality(number of bits set)
		Comparator<BitSet> c = new Comparator<BitSet>() {
			public int compare(BitSet b1, BitSet b2) {
				return ((Integer)b1.cardinality()).compareTo(b2.cardinality());
			}
		};
		List<BitSet> bitSets = bitTree.getBits();
		Collections.sort(bitSets, c);

		Object[] taxaA = taxa.toArray();	//could be stored as an instance variable since it's so useful
		SimpleRootedTree tree = new SimpleRootedTree();
		//BitSet of all taxa in this tree, not necessarily in all trees
		BitSet allTaxa = bitSets.get(bitSets.size()-1);
		//int numberOfTaxaInTree = allTaxa.cardinality();
		Node[] externalNodes = new Node[taxaA.length];


		// !!! Problems arise in writer if there is a tree with a different set of taxa in it.

		//build the tree from bottom up
		//first create all tips
		for (int i = allTaxa.nextSetBit(0); i >= 0; i = allTaxa.nextSetBit(i+1)) {
			externalNodes[i] = tree.createExternalNode((Taxon) taxaA[i]);
		}

		int numberOfInternalNodes = bitSets.size();	//assumption which I'm pretty sure is always true
		Node[] internalNodes = new Node[numberOfInternalNodes];


		//iterate through list of clades. First add ones of size 2. Then for bigger ones, search the list of already created clades backwards to find sub-clades and add as nodes
		for(int i = 0; i < numberOfInternalNodes; i++) {
			List<Node> nodes = new ArrayList<Node>();
			BitSet copy = (BitSet) bitSets.get(i).clone();
			for(int e = i-1; e >= 0; e--) {					
				if(copy.intersects(bitSets.get(e))) {
					nodes.add(internalNodes[e]);
					//use XOR to make sure only the largest sub clade is added, as the sub-sub clades has already been added to sub clade. Sub!
					copy.xor(bitSets.get(e));
				}
			}
			nodes.addAll(getNodes(externalNodes, copy));
			internalNodes[i] = tree.createInternalNode(nodes);
		}
		tree.setAttribute("W", bitTree.getWeight());	//should the attribute be named W or weight?

		if (highlights != null) {
			for(Node node : getNodes(externalNodes, highlights)) {
				Color color = Color.red;
				node.setAttribute("!color", color);
				//begin code for highlighting the path from the tip to root
				Node parent = tree.getParent(node);
				while(parent != null) {
					parent.setAttribute("!color", color);
					parent = tree.getParent(parent);
				}
			}
		}

		return tree;
	}


	/**
	 * Prune the taxa flagged in the input BitSet in set of all trees 
	 * @param a - taxa to prune
	 * @return List of filters used in pruning. Pass to unPrune to undo pruning.
	 */
	public List<BitSet> prune(BitSet a){
		//Might have to deal with clades that become equal to existing ones(add up frequency)

		List<BitSet> filters = new ArrayList<BitSet>();
//		for(BitSet key : clades.keySet()) {
//			BitSet filter = (BitSet) a.clone();
//			filter.and(key);
//			filters.add(filter);
//			key.xor(filter);	
//		}
		
		
		
		for (Map.Entry<BitSet, Integer> entry : clades.entrySet()) {
			BitSet key = entry.getKey();
			BitSet filter = (BitSet) a.clone();
			filter.and(key);
			filters.add(filter);
			key.xor(filter);
        }


		//
		//Beginning of code that would clean up "clades" of duplicate entries. 
		//Might be easier to just adjust the probability calculator do take into account duplicates correctly.
		//
		//		Object[] keys = clades.keySet().toArray();
		//		System.out.println();
		//		for(int i = 0; i < keys.length; i++) {
		//			for(int e = i+1; e < keys.length - 0; e++) {
		//				//System.out.println(keys[i].equals(keys[e]));
		//				//System.out.println(keys[i] + " vs " + keys[e]);
		//				if(keys[i].equals(keys[e])) {
		//					clades.put((BitSet) keys[i], clades.get(keys[i]) + clades.get(keys[e]));
		//				}
		//				
		//			}
		//		}
		return filters;
	}

	/**
	 * UnPrune trees to previous state.
	 * @param a - list of filters used in original pruning of trees
	 */
	public void unPrune(List<BitSet> filters) {
//		Object[] keys = clades.keySet().toArray();
		Iterator<BitSet> keys = clades.keySet().iterator();
//		for(int i = 0; i < keys.length; i++) {
//			((BitSet) keys[i]).xor(filters.get(i));
//		}
		for(BitSet bs : filters) {
			keys.next().xor(bs);
		}
		
	}
}
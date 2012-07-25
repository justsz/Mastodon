package mastodon.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTreeUtils;
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
public class BitTreeSystem {
	private LinkedHashSet<Taxon> taxa;	//Set of all unique taxa. LinkedHashSet because order needs to be maintained
	private Map<BitSet, Clade> clades;	//Map of unique clades and their frequencies of appearence in the set of trees
	private boolean newTree = false;	//used to mark the beginning of a new tree during the search for unique clades
	private List<BitSet> bitTree;	//temporary storage for BitSet representation of individual trees
	private List<BitTree> bitTrees;
	private int treeCount;
	private boolean weighted;
	private int treeNumber;
	private boolean firstTree;
	private BitTree mapTree;


	/**
	 * Object will hold all information about the set of trees needed for their analysis.
	 */
	public BitTreeSystem() {
		treeCount = 0;	 
		treeNumber = -1;	//later incremented to 0 an so on. Start at -1 to match other indexing
		weighted = true;
		this.taxa = new LinkedHashSet<Taxon>();
		this.clades = new HashMap<BitSet, Clade>();
		this.bitTrees = new ArrayList<BitTree>();
		firstTree = true;


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
		if (firstTree) {
			taxa.addAll(trees.get(0).getTaxa());
			firstTree = false;
		}

		for(RootedTree tree : trees) {
			if(!taxa.equals(tree.getTaxa())) {
				System.out.println("Trees don't all contain the same taxa. Exiting.");
				System.exit(2);
			}
		}



		for(RootedTree tree : trees) {
			addClades(tree, tree.getRootNode());
			double weight = -1;	//signifies unweighted tree
			if(tree.getAttribute("weight") != null) {	//checking this might be better to put in a pre-processing stage
				if(!weighted) {
					System.out.println("Weight annotation error. Not all trees are weighted. Exiting.");
					System.exit(2);
				}
				weighted = true;
				weight = ((Float) tree.getAttribute("weight")).doubleValue();
			} else {
				if(weighted && treeCount != 0) {
					System.out.println("Weight annotation error at: " + tree.getAttributeMap() + " Exiting.");
					System.exit(2);
				}
				weighted = false;
			}
			BitTree tr = new BitTree(bitTree, weight);

			/////Magical but likely useless ordering step/////
			//tr.order();


			bitTrees.add(tr);
			//			bitTrees.add(new BitTree(bitTree, weight));			
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
			treeNumber++;
		}

		Clade clade = clades.get(bits);
		if (clade == null) {
			//this cloning is still up for debate. Might change depending on how other features will be implemented
			Clade newClade = new Clade((BitSet) bits.clone());
			clades.put(bits, newClade);
			newClade.addTree(treeNumber);
			//System.out.println(bits);
		} else {
			clade.incrementCount();
			clade.addTree(treeNumber);
		}


		bitTree.add(clades.get(bits).getCladeBits());
	}

	/**
	 * If the trees are weighted, returns the max weight tree. Otherwise returns the index of the tree that has the maximum probability clades. 
	 * @return index of the MAP tree
	 */
	public void findMapTree() {	//this could all be done at creation time
		int index = 0;
		if(weighted) {
			double maxWeight = 0;
			for(int i = 0; i < treeCount; i++) {
				double weight = bitTrees.get(i).getWeight();
				if(weight > maxWeight) {
					maxWeight = weight;
					index = i;
				}
			}
		} 
		else {

			long[] scores = new long[treeCount];
			for(int i = 0; i < scores.length; i++) {
				long score = 0;
				BitTree tree = bitTrees.get(i);
				for(BitSet bs : tree.getBits()) {
					Clade clade = clades.get(bs);
					if (clade != null) {
						score += clade.getFrequency();
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
		
		System.out.println("Map tree: " + (index+1));
		mapTree = bitTrees.get(index);		
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
	 * @param index index of sought taxa
	 * @return taxon at index in set of all unique taxa
	 */
	public Taxon getTaxon(int index) {
		Object[] taxaA = taxa.toArray();
		return (Taxon) taxaA[index];
	}

	public List<Taxon> getTaxa(BitSet bits) {
		List<Taxon> taxaList = new ArrayList<Taxon>();
		for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1)) {
			taxaList.add(getTaxon(i));
		}
		return taxaList;
	}

	/**
	 * Returns Map of all unique clades in set of trees.
	 * @return Map of all unique clades in set of trees.
	 */
	public Map<BitSet, Clade> getClades() {
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
	 * Returns the set of taxa present in all trees.
	 * @return set of taxa present in all trees
	 */
	public Set<Taxon> getAllTaxa() {
		return taxa;
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
	public SimpleRootedTree reconstructTree(BitTree bitTree, BitSet highlights, Map<Taxon, Double> pruningFreq) {
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
			if (pruningFreq != null) {
				externalNodes[i].setAttribute("pruningFreq", pruningFreq.get((Taxon) taxaA[i]));
			}
		}

		int numberOfInternalNodes = bitSets.size();	//assumption which I'm pretty sure is always true
		Node[] internalNodes = new Node[numberOfInternalNodes];


		//iterate through list of clades. First add ones of size 2. 
		//Then for bigger ones, search the list of already created clades backwards to find sub-clades and add as nodes
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
			internalNodes[i].setAttribute("cladeProb", (double) clades.get(bitSets.get(i)).getFrequency()/treeCount);
		}
		tree.setAttribute("W", bitTree.getWeight());	//should the attribute be named W or weight?

		if (highlights != null) {
			for(Node node : getNodes(externalNodes, highlights)) {
				node.setAttribute("pruned", true);				
			}
			
			for(Node node : getNodes(externalNodes, highlights)) {
				Node parent = tree.getParent(node);
				boolean colorParent = true;
				while(parent != null && colorParent) {
					for(Node child : tree.getChildren(parent)) {
						if (child.getAttribute("pruned") == null) {
							colorParent = false;
							break;
						}
					}
					if(colorParent) {
						parent.setAttribute("pruned", true);	
						parent = tree.getParent(parent);	
					}
				}
			}
		}

		return tree;
	}
	
	public SimpleRootedTree reconstructMapTree(BitSet highlights, Map<Taxon, Double> pruningFreq) {
		return reconstructTree(mapTree, highlights, pruningFreq);
	}


	/**
	 * Prune the taxa flagged in the input BitSet in set of all trees 
	 * @param pruner - taxa to prune
	 * @return List of filters used in pruning. Pass to unPrune to undo pruning.
	 */
	public Map<BitSet, BitSet> prune(BitSet pruner){
		Map<BitSet, BitSet> filters = new HashMap<BitSet, BitSet>();

		for (Map.Entry<BitSet, Clade> entry : clades.entrySet()) {
			BitSet cladeBits = entry.getValue().getCladeBits();
			if(cladeBits.intersects(pruner)) {
				BitSet filter = (BitSet) pruner.clone();
				filter.and(cladeBits);
				filters.put(entry.getKey(), filter);
				cladeBits.xor(filter);
			}
		}
		return filters;
	}

	/**
	 * UnPrune trees to previous state.
	 * @param filters - list of filters used in original pruning of trees
	 */
	public void unPrune(Map<BitSet, BitSet> filters) {
		for(Map.Entry<BitSet, BitSet> filter : filters.entrySet()) {
			clades.get(filter.getKey()).getCladeBits().xor(filter.getValue());
		}
	}

	public double[] pruneFast(BitSet pruner) {
		//weighted = false;
		double[] result = new double[2];
		//List<HashSet<Integer>> subTrees = new ArrayList<HashSet<Integer>>();
		//List<Clade3> mapClades = new ArrayList<Clade3>(mapTree.getBits().size());

		Map<BitSet, BitSet> filters = new HashMap<BitSet, BitSet>();
		Map<BitSet, BitSet> prunedClades = new HashMap<BitSet, BitSet>(clades.size());
		//		List<BitSet> possibleLimiters = new ArrayList<BitSet>(); 

		//Clade3 cl = new Clade3(new BitSet());	//just a placeholder
		//System.out.println(cl);
		//		double start = System.currentTimeMillis();
		for (Map.Entry<BitSet, Clade> entry : clades.entrySet()) {
			BitSet cladeBits = entry.getValue().getCladeBits();
			if(cladeBits.intersects(pruner)) {
				BitSet filter = (BitSet) pruner.clone();
				filter.and(cladeBits);
				filters.put(entry.getKey(), filter);
				cladeBits.xor(filter);
			}
			if (cladeBits.cardinality() > 1) {	//not very needed, might be some performance benefit			
				BitSet cl = prunedClades.get(cladeBits);
				BitSet matchingTrees = (BitSet) entry.getValue().getCladeToTrees().clone();
				if(cl == null) {
					prunedClades.put(cladeBits, matchingTrees);
					//					if (matchingTrees.cardinality() == 1 && mapTree.getBits().contains(cladeBits)) {	//mid-processing
					//						System.out.println("possible");
					//						possibleLimiters.add(cladeBits);
					//					}
				} else {
					cl.or(matchingTrees);
				}
			}
		}


		//mid-processing step, might not be very useful
		//		for (BitSet bs : possibleLimiters) {
		//			if (prunedClades.get(bs).size() == 1) {
		//				unPrune(filters);
		//				return 1;
		//			}
		//		}

		//		System.out.println(System.currentTimeMillis() - start);


		BitSet runningIntersection = new BitSet();
		for(BitSet bs : mapTree.getBits()) {  
			if (bs.cardinality() > 1) {
				BitSet clade = prunedClades.get(bs); //bad name
				if (runningIntersection.cardinality() == 0) {
					runningIntersection.or(clade);
				} else {
					runningIntersection.and(clade);
				}
			}
			if (runningIntersection.cardinality() == 1) {	//a limiting clade has reduced it to only the map tree
				unPrune(filters);
				if(weighted) {
					result[0] = bitTrees.get(runningIntersection.nextSetBit(0)).getWeight();
				} else {
					result[0] = 1.0/bitTrees.size();
				}
				result[1] = 1;
				forTest = runningIntersection;
				return result;
			}
		}


		//calculate map score based on whether the trees are weighted or not. Might simplify this later if I specialize to unweighted trees

		
		//this block checks whether the trees deduced to be equal to the map tree don't have extra clades
		int cladeCount = 0;
		boolean first = true;
		for (int i = runningIntersection.nextSetBit(0); i >= 0; i = runningIntersection.nextSetBit(i+1)) {
			int c = 0;
			Set<BitSet> ble = new HashSet<BitSet>();
			for(BitSet b : bitTrees.get(i).getBits()) {

					if(b.cardinality() > 1) {
						ble.add(b);						
					}
			}
			
			c = ble.size();
			if (first) {
				cladeCount = c;
				first = false;
			}
			if(cladeCount != c) {
				//System.out.println("Are these trees really equal? [Comparing tree " + runningIntersection.nextSetBit(0) + " against tree " + i + "]");
				if (bitTrees.get(runningIntersection.nextSetBit(0)).equals(bitTrees.get(i))) {
				//	System.out.println("True.");
				} else {
					runningIntersection.clear(i);
					//System.out.println("False. Tree removed.");
				}
			}
		}
		
		

		int subTreeCount = runningIntersection.cardinality();
		if(weighted) {
			for (int i = runningIntersection.nextSetBit(0); i >= 0; i = runningIntersection.nextSetBit(i+1)) {
				result[0] += bitTrees.get(i).getWeight();
			}
		} else {
			result[0] = (double) subTreeCount/bitTrees.size();
		}		

		result[1] = subTreeCount;


		unPrune(filters);
		forTest = runningIntersection;
		return result;
	}

	private BitSet forTest;	
	public BitSet getTest() {
		return forTest;
	}
}
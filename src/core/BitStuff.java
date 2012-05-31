/**
 * 
 */
package core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTree;

/**
 * @author justs
 *	Creating clade summary code adapted from CladeSystem in BEAST's code.
 */
public class BitStuff {
	private List<? extends RootedTree> trees;
	private Set<Taxon> taxa;
	private Map<BitSet, Integer> clades;
	private boolean newTree = false;
	private List<BitSet> bitTree;


	public BitStuff(List<? extends RootedTree> trees) {
		this.trees = trees;
		this.taxa = new LinkedHashSet<Taxon>();
		this.clades = new HashMap<BitSet, Integer>();
//		this.bitTree = new ArrayList<BitSet>();

		//construct full set of taxa
		for(RootedTree tree : trees) {
			taxa.addAll(tree.getTaxa());
		}

		//		//check
		//		for(Object taxon : taxa.toArray()) {
		//			System.out.print(((Taxon)taxon).getName());
		//		}
		//		System.out.println(); //newline


	}

	public List<ArrayList<BitSet>> makeBits() {
		List<ArrayList<BitSet>> bitTrees = new ArrayList<ArrayList<BitSet>>(trees.size());
		for(RootedTree tree : trees) {
			//newTree = true;
			addClades(tree, tree.getRootNode());
			bitTrees.add((ArrayList<BitSet>) bitTree);	//spit out bitTree here
			newTree = false;
		}
		return bitTrees;
	}

	BitSet addClades(RootedTree tree, Node node) {
		BitSet bits = new BitSet();
		//Node root = tree.getRootNode();

		//		for(int i = 0; i < tree.getChildren(node).size(); i++) {
		//			
		//		}

		if (tree.isExternal(node)) {

			int index = getIndex(taxa, tree.getTaxon(node));
			bits.set(index);


			//this is for including single tip clades
			//if (includeTips) {
			//addClade(bits);
			//}

		} else {

			for(Node node1 : tree.getChildren(node)) {
				bits.or(addClades(tree, node1));
			}
			addClade(bits);
		}

		return bits;	

	}

	void addClade(BitSet bits) {
		if(!newTree) {
			bitTree = new ArrayList<BitSet>();
			newTree = true;
		}
		Integer bset = clades.get(bits);
		if (bset == null) {
			clades.put(bits, 1);
		} else {
			clades.put(bits, clades.get(bits) + 1);
		}
		
		for(BitSet s : clades.keySet()) {
			if(s.equals((BitSet)(bits))) {
				bitTree.add(s);
			}
		}
	}

	int getIndex(Set<Taxon> taxa, Taxon taxon) {
		int index = 0;
		for(Object t : taxa.toArray()) {
			if(taxon.equals(t)) {
				break;
			}
			index++;	//safe enough because all taxa should contain every taxon due to initial step
		}
		return index;
	}

	Taxon getTaxon(Set<Taxon> taxa, int index) {
		Object[] taxaA = taxa.toArray();
		return (Taxon) taxaA[index];
	}

	public Map<BitSet, Integer> getClades() {
		return clades;
	}

	List<Node> getNodes(Node[] externalNodes, BitSet bs) {
		List<Node> nodes = new ArrayList<Node>();
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
			nodes.add(externalNodes[bs.nextSetBit(i)]);
		}
		return nodes;
	}


	public MutableRootedTree reconstructTree(List<BitSet> bitSets) {
		Comparator<BitSet> c = new Comparator<BitSet>() {
			public int compare(BitSet b1, BitSet b2) {
				return ((Integer)b1.cardinality()).compareTo(b2.cardinality());
			}
		};
		Collections.sort(bitSets, c);
		Object[] taxaA = taxa.toArray();	//could be stored as an instance variable since it's so useful


		MutableRootedTree tree = new MutableRootedTree();
		BitSet allTaxa = bitSets.get(bitSets.size()-1);
		int numberOfTaxaInTree = allTaxa.cardinality();
		Node[] externalNodes = new Node[taxaA.length];

		//if (taxaA.length == numberOfTaxaInTree) {
			//for(int i = 0; i < taxaA.length; i ++) {
		
		//Problems arise in writer if there is a tree with a different sent of taxa in it.
			for (int i = allTaxa.nextSetBit(0); i >= 0; i = allTaxa.nextSetBit(i+1)) {
				externalNodes[i] = tree.createExternalNode((Taxon) taxaA[i]);
			}
//		} else {
//			//not yet implemented how to handle a tree which has less taxa than the total number
//			for(int i = 0; i < externalNodes.length; i++) {
//				for(int e = 0; e < externalNodes.length; e++) {
//					//extrenalNodes[i] = tree.createExternalNode((Taxon)bitSets.get(bitSets.size()-1));
//				}
//			}
//		}

		int numberOfInternalNodes = bitSets.size();	//assumption
		Node[] internalNodes = new Node[numberOfInternalNodes];

		for(int i = 0; i < numberOfInternalNodes; i++) {	//exclude bitset representing the whole tree from this loop
			//if(bitSets.get(i).cardinality() == 2) {
			//	internalNodes[i] = tree.createInternalNode(getNodes(externalNodes, bitSets.get(i)));
			//} else {
			List<Node> nodes = new ArrayList<Node>();
			BitSet copy = (BitSet) bitSets.get(i).clone();
			for(int e = i-1; e >= 0; e--) {					
				if(copy.intersects(bitSets.get(e))) {
					//System.out.println("i = " + i + "; e = " + e);
					nodes.add(internalNodes[e]);
					copy.xor(bitSets.get(e));
					//						if(copy.cardinality() == 0) {
					//							break;
					//						}
				}
			}
			nodes.addAll(getNodes(externalNodes, copy));
			internalNodes[i] = tree.createInternalNode(nodes);
			//}
		}

		//		List<Node> finalNodes = new ArrayList<Node>();
		//		BitSet copy = (BitSet) bitSets.get(bitSets.size()-1).clone();
		//		for(int i = numberOfTaxaInTree-3; i > 0; i--) {
		//			System.out.println(i);
		//			finalNodes.add(internalNodes[i]);
		//			copy.xor(bitSets.get(i));
		//			if(copy.cardinality() == 0) {
		//				break;
		//			}
		//		}

		//		tree.createInternalNode(finalNodes);
		return tree;
	}

	//	List<BitSet> sort(List<BitSet> bitsets) {
	//		Collections.sort
	//	}
	
	public void pruneEVERYTHING(){
		///Need to deal with clades that become equal to existing ones(add up frequency)
		BitSet a = new BitSet();
		a.set(0);
		
		for(BitSet key : clades.keySet()) {
			if(a.intersects(key)) {
				//BitSet rep = 
				key.xor(a);
			}
		}
		
		System.out.println("COMPLETE");
	}


}

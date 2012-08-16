/* Copyright (C) 2012 Justs Zarins
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package mastodon.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ProgressMonitorInputStream;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.CompactRootedTree;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.ReRootedTree;
import jebl.evolution.trees.RootedFromUnrooted;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;

/**
 * This class uses NexusImporter and NewickImporter to read in Tree objects from NEXUS or Newick files. A number of different Tree implementations can be imported.
 * @author justs
 *
 */
public class TreeReader {
	private Reader reader;
	private TreeImporter imp;	 
	private String fileName;	//Currently used filename.	


	/**
	 * Plain constructor. Call setFile afterwards. 
	 * @throws IOException 
	 */
	public TreeReader() {
	}

	/**
	 * Constructor that creates a NexusImporter or NewickImporter object bound to the provided filename. 
	 * @param filename - name of input file
	 * @throws IOException 
	 */
	public TreeReader(String filename) throws IOException {
		setFile(filename);
	}

	/**
	 * Change target file of input. If the file contains #NEXUS then the file is treated as Nexus. Otherwise it is assumed to be Newick.
	 * @param filename
	 * @throws IOException 
	 */
	public void setFile(String filename) throws IOException {
		fileName = filename;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

		//(c) FigTree
		String line = bufferedReader.readLine();
		while (line != null && line.length() == 0) {
			line = bufferedReader.readLine();
		}
		bufferedReader.close();

		boolean isNexus = (line != null && line.toUpperCase().contains("#NEXUS"));

		reader = new FileReader(fileName);

		if (isNexus) {
			imp = new NexusImporter(reader);
		} else {
			imp = new NewickImporter(reader, true);
		}	  
	}

	/**
	 * Returns the current set filename.
	 * @return current set filename
	 */
	public String getFile() {
		return fileName;
	}


	/**
	 * Close reader and re-open to beginning of the current input file.
	 * @throws IOException
	 */
	public void reset() throws IOException {
		reader.close();
		setFile(fileName);
	}


	/**
	 * Reads the file and converts to a list of SimpleRootedTree objects. These are immutable.
	 * @return list of SimpleRootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<SimpleRootedTree> readSimpleRootedTrees() throws IOException, ImportException {
		List<SimpleRootedTree> trees = new ArrayList<SimpleRootedTree>();
		Tree tree;
		while (imp.hasTree()) {
			tree = imp.importNextTree();
			trees.add((SimpleRootedTree) tree);
		}

		if (trees.size() == 0) {
			throw new ImportException("This file contained no trees.");
		}

		reader.close();
		return trees;
	}


	/**
	 * Reads the file and converts to a list of MutableRootedTree objects.
	 * @return list of MutableRootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<MutableRootedTree> readMutableRootedTrees() throws IOException, ImportException {
		List<MutableRootedTree> trees = new ArrayList<MutableRootedTree>();
		Tree tree;

		while (imp.hasTree()) {
			tree = imp.importNextTree();
			System.out.println("Reading mutable rooted trees only works with JEBL 2, modified by Andrew to convert a simple rooted tree to a mutable one");
			//			trees.add(new MutableRootedTree((RootedTree) tree));
		}

		if (trees.size() == 0) {
			throw new ImportException("This file contained no trees.");
		}

		reader.close();
		return trees;
	}


	/**
	 * Reads the next 100 trees in the file file and converts to a list of RootedTree objects. Input has to be rooted trees.
	 * @return list of RootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<RootedTree> read100RootedTrees() throws IOException, ImportException {
		List<RootedTree> trees = new ArrayList<RootedTree>();
		Tree tree;

		if(!imp.hasTree()) {	//in case number of trees is an integer multiple of 100
			reader.close();
		} else {
			int counter = 100;
			while (imp.hasTree() && counter > 0) {
				tree = imp.importNextTree();
				//trees.add(new MutableRootedTree((RootedTree) tree));
				trees.add((RootedTree) tree);
				counter--;
			}
		}
		return trees;
	}

	/**
	 * Reads the next 100 trees in the file file and converts to a list of RootedTree objects. Input is rooted on the specified outgroupString taxon.
	 * @param outgroupString - outgroup to root trees on
	 * @return list of RootedTree objects rooted on the specified outgroup
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<RootedTree> read100ReRootedTrees(String outgroupString) throws IOException, ImportException {
		List<RootedTree> trees = new ArrayList<RootedTree>();
		Tree tree;

		if(!imp.hasTree()) {	//in case number of trees is an integer multiple of 100
			reader.close();
		} else {
			int counter = 100;
			while (imp.hasTree() && counter > 0) {
				tree = imp.importNextTree();

				Node outgroup = tree.getNode(Taxon.getTaxon(outgroupString));
				trees.add(new RootedFromUnrooted(tree, tree.getAdjacencies(outgroup).get(0), outgroup, 1));
				counter--;
			}
		}
		return trees;
	}


	/**
	 * Read one more tree.
	 * @return the read tree
	 * @throws IOException
	 * @throws ImportException
	 */
	public RootedTree readNextTree() throws IOException, ImportException {

		RootedTree tree = null;

		if(imp.hasTree()) {
			tree = (RootedTree) imp.importNextTree();
		}

		return tree;
	}

	/**
	 * Reads in only the tree at the specified position in the file.
	 * Check that indexing matches up correctly and the expected tree is returned.
	 * @param index - position of desired tree in file
	 * @return the tree at index
	 * @throws IOException
	 * @throws ImportException
	 */
	public SimpleRootedTree getTree(int index) throws IOException, ImportException {
		reset();
		SimpleRootedTree tree = (SimpleRootedTree) imp.importNextTree();
		while (index > 0) {
			//this can give null pointer exception if you give index too large. 
			//tree = (SimpleRootedTree) imp.importNextTree();
			imp.hasTree();
			index--;
		}
		//tree = (SimpleRootedTree) imp.importNextTree();
		return (SimpleRootedTree) imp.importNextTree();
	}


	/**
	 * Reads the file and converts to a list of CompactRootedTree objects.
	 * @return list of CompactRootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<CompactRootedTree> readCompactRootedTrees() throws IOException, ImportException {
		List<CompactRootedTree> trees = new ArrayList<CompactRootedTree>();
		Tree tree;

		while (imp.hasTree()) {
			tree = imp.importNextTree();
			trees.add(new CompactRootedTree((RootedTree) tree));
		}

		if (trees.size() == 0) {
			throw new ImportException("This file contained no trees.");
		}

		reader.close();
		return trees;
	}
}

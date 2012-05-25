package core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.trees.CompactRootedTree;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;

/**
 * This class uses NexusImporter to read in Tree objects from NEXUS (eg .nex) files. A number of different Tree implementations can be imported.
 * @author justs
 *
 */
public class NexusReader {
	BufferedReader reader;
	NexusImporter imp;
	/**
	 * Currently used filename.
	 */
	String fileName;

	/**
	 * Constructor that creates a NexusImporter object bound to the provided filename. 
	 * @param filename - name of input file
	 * @throws FileNotFoundException
	 */
	public NexusReader(String filename) throws FileNotFoundException {
		setFile(filename);
	}

	/**
	 * Change target file of input.
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public void setFile(String filename) throws FileNotFoundException {
		fileName = filename;
		reader = new BufferedReader(new FileReader(fileName));
		imp = new NexusImporter(reader);
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
	 * Reads the NEXUS file and converts to a list of SimpleRootedTree objects. These are immutable.
	 * @return list of SimpleRootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<SimpleRootedTree> readSimpleRootedTrees() throws IOException, ImportException {
		List<SimpleRootedTree> trees = new ArrayList<SimpleRootedTree>();
		Tree tree;
		while((tree = imp.importNextTree()) != null) {
			trees.add((SimpleRootedTree) tree);
		}
		reader.close();
		return trees;
	}


	/**
	 * Reads the NEXUS file and converts to a list of MutableRootedTree objects.
	 * @return list of MutableRootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<MutableRootedTree> readMutableRootedTrees() throws IOException, ImportException {
		List<MutableRootedTree> trees = new ArrayList<MutableRootedTree>();
		Tree tree;
		while((tree = imp.importNextTree()) != null) {
			trees.add(new MutableRootedTree((RootedTree) tree));
		}
		reader.close();
		return trees;
	}


	/**
	 * Reads the NEXUS file and converts to a list of CompactRootedTree objects.
	 * @return list of CompactRootedTree objects
	 * @throws IOException
	 * @throws ImportException
	 */
	public List<CompactRootedTree> readCompactRootedTrees() throws IOException, ImportException {
		List<CompactRootedTree> trees = new ArrayList<CompactRootedTree>();
		Tree tree;
		while((tree = imp.importNextTree()) != null) {
			trees.add(new CompactRootedTree((RootedTree) tree));
		}
		reader.close();
		return trees;
	}
}

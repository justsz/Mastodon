package mastodon.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.trees.Tree;

/**
 * This class uses JEBL's NexusExporter to write tree objects to file. 
 * @author justs
 */
public class NexusWriter {
	private BufferedWriter writer;
	private NexusExporter exp;

	/**
	 * Constructor that creates a NexusExporter object bound to the filename provided.
	 * @param filename - name of output file
	 * @throws IOException
	 */
	public NexusWriter(String filename) throws IOException {
		setFile(filename);
	}

	/**
	 * Changes target file for output.
	 * @param filename - name of output file
	 * @throws IOException
	 */
	public void setFile(String filename) throws IOException {
		writer = new BufferedWriter(new FileWriter(filename));
		exp = new NexusExporter(writer);
	}

	/**
	 * Uses NexusWriter to write a given list of Trees to file. After writing the writer is closed.
	 * @param trees - list of Trees to be written to file
	 * @throws IOException
	 */
	public void writeTrees(List<? extends Tree> trees) throws IOException {
		exp.exportTrees(trees);
		writer.close();
	}
}

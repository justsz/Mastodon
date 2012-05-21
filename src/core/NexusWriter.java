package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.trees.Tree;

public class NexusWriter {
	BufferedWriter writer;
	NexusExporter exp;

	public NexusWriter(String filename) throws IOException {
		setFile(filename);
	}

	public void setFile(String filename) throws IOException {
		writer = new BufferedWriter(new FileWriter(filename));
		exp = new NexusExporter(writer);
	}

	public void writeTrees(List<Tree> trees) throws IOException {
		exp.exportTrees(trees);
		writer.close();
	}
}

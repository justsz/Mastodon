package core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.trees.Tree;

public class NexusReader {
	BufferedReader reader;
	NexusImporter imp;

	public NexusReader(String filename) throws FileNotFoundException {
		setFile(filename);
	}

	public void setFile(String filename) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(filename));
		imp = new NexusImporter(reader);
	}

	public List<Tree> readTrees() throws IOException, ImportException {
		List<Tree> trees = imp.importTrees();
		reader.close();
		return trees;
	}

}

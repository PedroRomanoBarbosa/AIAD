package parser;

import java.io.File;

public class test {

	public static void main(String[] args) {
		File file = new File("./data/data.xml");
		Parser parser = new Parser();
		parser.execute(file);

	}

}

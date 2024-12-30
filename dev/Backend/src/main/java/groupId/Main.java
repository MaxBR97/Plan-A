package groupId;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import Model.Model;
import Model.ModelSet;
import parser.*;


@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		//SpringApplication.run(Main.class, args);

		Model parser;
            try {
                parser = new Model("./dev/Backend/src/main/resources/ExampleZimplProgram.zpl");
            
	ModelSet emdot = parser.getSet("Zmanim");
	System.out.println(emdot.getType());
	parser.appendToSet(emdot, "42abc");  // Will preserve formatting
				Thread.sleep(1500);
	
	// Remove from a set
	//parser.modifySet("Emdot", "42", false);
	Thread.sleep(1000);
	// Get set elements
	//java.util.Set<String> elements = parser.getSetElements("Emdot");

	//System.out.println(elements);
} catch (Exception ex) {
}


		}

}

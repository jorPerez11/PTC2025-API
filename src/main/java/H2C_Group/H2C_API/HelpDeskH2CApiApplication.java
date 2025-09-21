package H2C_Group.H2C_API;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelpDeskH2CApiApplication {

	public static void main(String[] args) {
		{
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue())

			);
			SpringApplication.run(HelpDeskH2CApiApplication.class, args);
		}
	}

}
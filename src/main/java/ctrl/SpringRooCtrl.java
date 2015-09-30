package ctrl;

import lombok.Getter;
import model.SpringRoo;
import model.mainLayer.TableObjects;

@Getter
public class SpringRooCtrl {
	private final String name = "SpringRoo";
	private final String ext = "roo";
		
	public String createProject(TableObjects objects){
		SpringRoo roo = new SpringRoo(objects);
        roo.criarProjeto()
        	.configDatabase()
			.configDomain()
			.configFrontEnd()
			.configsExtras()
			.quit();
		return roo.build();
    }
}

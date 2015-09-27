package ctrl;

import model.SpringRoo;
import model.mainLayer.TableObjects;


public class SpringRooCtrl {
	private final String name = "SpringRoo";
	private SpringRoo roo;
		
	public String createProject(TableObjects objects){
		SpringRoo roo = new SpringRoo(objects);
        roo.criarProjeto()
        	.configDatabase()
			.configDomain();
        /*roo.quit();
        roo.scriptProject;*/
		return roo.build();
    }
}

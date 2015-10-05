package model;

import java.util.ArrayList;
import java.util.List;

import model.domainLayer.Attribute;
import model.domainLayer.ClassEnum;
import model.domainLayer.Constraints;
import model.domainLayer.Entity;
import model.domainLayer.interfaces.FullNamePath;
import model.infrastructureLayer.DataBase;
import model.infrastructureLayer.Infrastructure;
import model.interfaceLayer.Interface;
import model.interfaceLayer.InterfaceApplication;
import model.mainLayer.Project;
import model.mainLayer.TableObjects;

public class SpringRoo {
	private TableObjects tableObjects;
	private StringBuilder scriptProject;
	
	public SpringRoo criarProjeto() {
		Project project = tableObjects.getProject();
		scriptProject.append("// CRIANDO O NOVO PROJETO\n");
        scriptProject.append("project --topLevelPackage " +
        	this.configpath(project) + "." + project.getName() + 
    	"\n\n");
        
        return this;
	}
	
	public String build(){
		return this.scriptProject.toString();
	}
	
	private String configpath(Project project){
		Infrastructure infra = project.getInfrastructure();
		
		return infra.getBasePackage();
	}
	
	public SpringRoo configDatabase() {
		Project project = tableObjects.getProject();
		Infrastructure infra = project.getInfrastructure();
		DataBase db = infra.getDataBase();
		
		scriptProject.append("// Configurações do banco de dados\n");
		scriptProject.append("jpa setup --provider " + 
				infra.getDbFramework().getName().toUpperCase() + 
				" --database " + db.getName().toUpperCase() + "\n");
		if(db.getUser() != null){
			scriptProject.append("database properties set --key database.username --value " +
					db.getUser() + "\n");
		}
		if(db.getPass() != null){
			scriptProject.append("database properties set --key database.password --value " +
					db.getPass() + "\n");
		}
		if(db.getHost() != null){
			scriptProject.append("database properties set --key database.url --value " +
					db.getHost() + "\n");
		}
		
		this.scriptProject.append("\n");
		return this;
	}
	
	public SpringRoo configDomain() {
		this.configEnums();
		List<Entity> sortEntityList = this.configDomains();
		this.configFields(sortEntityList);
		return this;
	}
	
	private void configEnums() {
		List<ClassEnum> enums = tableObjects.getEnums();
		scriptProject.append("// Configurações do domínio\n");
		scriptProject.append("// Configurações dos Enums\n");
		for(ClassEnum e : enums){
			scriptProject.append("enum type --class " + e.fullNamePath() + "\n");
			for(String valueEnum: e.getValues()){
				scriptProject.append("enum constant --name " + valueEnum + "\n");
			}
			this.scriptProject.append("\n");
		}
		this.scriptProject.append("\n");
	}
	
	private List<Entity> configDomains() {
		scriptProject.append("// Configurações das entitidades \n");
		List<Entity> sortList = this.sortEntities();
		for(Entity ent: sortList){
			this.scriptProject.append(
					"entity jpa --class " +
					ent.fullNamePath()
			);
			if(ent.isAbstrato()){
				this.scriptProject.append(" --abstract");
			}
			else {
				this.scriptProject.append(" --testAutomatically");
			}
			if(!ent.getClassExtends().isEmpty()){
				this.scriptProject.append(" --inheritanceType JOINED --extends " +
						ent.getClassExtends().iterator().next().fullNamePath().toString()
				);
			}
			this.scriptProject.append("\n");
		}
		return sortList;
	}

	private List<Entity> sortEntities() {
		List<Entity> list = new ArrayList<Entity>();
		for(Entity ent: this.tableObjects.getEntities()){
			for(Entity superEntity: ent.getClassExtends()){
				this.navigateUpParent(list, superEntity);
			}
			list.add(ent);
		}
		
		return list;
	}

	private void navigateUpParent(List<Entity> list, Entity parent) {
		if(!list.contains(parent)){
			for(Entity superEntity: parent.getClassExtends()){
				this.navigateUpParent(list, superEntity);			}
			list.add(parent);
		}
	}

	private void configFields(List<Entity> sortEntityList) {
		this.scriptProject.append("\n\n");
		scriptProject.append("// Configurações dos campos\n");
		for(Entity single: sortEntityList){
			if(!single.getAttributes().isEmpty()){
				for(Attribute attr: single.getAttributes()){
					this.scriptProject.append("field ");
					
					String[] traducao = new TiposBasicos().temTraducao(attr.getDatetype());
		            if (traducao == null) {
		            	FullNamePath typeEntidade = (FullNamePath) attr.getDatetype();
		            	String typeField;
		            	if(typeEntidade instanceof ClassEnum){
		            		typeField = "enum";
		            	}
		            	else {
		            		if(attr.getCollectionType() != null){
			            		typeField = attr.getCollectionType().getValor().toLowerCase();
			            	} else {
			            		typeField = "reference";
			            	}
		            	}
		            	
		            	this.scriptProject.append(" "+ typeField + " --fieldName " + attr.getName());
		            	this.scriptProject.append(" --type " + typeEntidade.fullNamePath());
		            } else {
						this.scriptProject.append(new TiposBasicos().temTraducao(attr.getDatetype())[0]);
		            	this.scriptProject.append(" --fieldName " + attr.getName());
		                if (!traducao[1].isEmpty())
		                	this.scriptProject.append(" --type " + new TiposBasicos().temTraducao(attr.getDatetype())[1]);
		            }
		           this.configConstraints(attr, traducao != null ? traducao[0]:null);
		           this.scriptProject.append(" --class " + single.fullNamePath() + "\n");
				}
				this.scriptProject.append("\n");
			}
		}
	}
	
	private void configConstraints(Attribute attr, String type) {
		Constraints cons = attr.getConstraints();
		String min = "min";
		String max = "max";
		if(cons.isPk()){
			this.scriptProject.append(" --unique " + cons.isUnique());
		} else{
			if(cons.isUnique()){
				this.scriptProject.append(" --unique " + cons.isUnique());
			}
		}
		if(cons.isNullable()){
			this.scriptProject.append(" --notnull " + !cons.isNullable());
		}
		if(type != null){
			if(type == "String"){
				if(cons.getMax() != null){
					max = "sizeMax";
					this.scriptProject.append(" --" + max + " " + cons.getMax());
				}
				if(cons.getMin() != null){
					min = "sizeMin";
					this.scriptProject.append(" --" + min + " " + cons.getMin());
				}
			}
			else {
				if(type == "int" || type == "Integer"){
					if(cons.getMax() != null){
						max = "sizeMax";
						this.scriptProject.append(" --" + max + " " + cons.getMax());
					}
					if(cons.getMin() != null){
						min = "sizeMin";
						this.scriptProject.append(" --" + min + " " + cons.getMin());
					}
				}
			}
		}
	}

	public SpringRoo configFrontEnd() {
		Project project = this.tableObjects.getProject();
		Interface iface = project.getIface();
		for(InterfaceApplication ifaceApp: iface.getInterfaceApplication()){
			if(ifaceApp.getType().getValor() == "HtmlView"){
				this.interfaceHTML();
				break;
			}
			else{
				this.interfaceRestJson();
			}
		}
		return this;
	}
	
	private SpringRoo interfaceHTML(){
		this.scriptProject.append("// Configurações para interface WEB\n");
		this.scriptProject.append("web mvc setup\n");
		this.scriptProject.append("web mvc all --package ~.web\n\n");
		return this;
	}
	
	private SpringRoo interfaceRestJson(){
		this.scriptProject.append("// Configurações para interface Rest\n");
		this.scriptProject.append("json all --deepSerialize\n\n");
		return this;
	}
	
	public SpringRoo configsExtras(){
		this.scriptProject.append("// Configurações Extras\n\n");
		this.scriptProject.append("web mvc language --code de\n");
		this.scriptProject.append("web mvc language --code es\n");
		//this.scriptProject.append("logging setup --level INFO\n");
		return this;
	}
	
	public SpringRoo quit(){
		this.scriptProject.append("quit\n");
		return this;
	}
	
	public SpringRoo(TableObjects objects) {
		this.tableObjects = objects;
		this.scriptProject = new StringBuilder();
	}	
}

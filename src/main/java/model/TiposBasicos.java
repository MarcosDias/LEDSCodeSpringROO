package model;

import java.util.HashMap;

import lombok.Getter;
import model.domainLayer.Classifier;
import model.domainLayer.PrimaryDateType;

@Getter
public class TiposBasicos {
    private HashMap<String, String[]> traducao = new HashMap<String, String[]>();

    /**
     * Busca se existe um tipo, dentro das possibilidades mapeadas no enum
     *
     * @param tipo
     * @return
     */
    public String[] temTraducao(Classifier tipo) {
        if (tipo instanceof PrimaryDateType) {
            return traducao.get(
        		((PrimaryDateType) tipo).getType().getValue()
    		);
        }
        return null;
    }

    public TiposBasicos(){
        String[] intMap = {"number", "java.lang.Integer"};
        traducao.put("int", intMap);
        traducao.put("Integer", intMap);
        String[] longMap = {"number", "java.lang.Long"};
        traducao.put("long", longMap);
        traducao.put("Long", longMap);
        String[] floatMap = {"number", "java.lang.Float"};
        traducao.put("float", floatMap);
        traducao.put("Float", floatMap);
        String[] doubleMap = {"number", "java.lang.Double"};
        traducao.put("double", doubleMap);
        traducao.put("Double", doubleMap);
        String[] booleanMap = {"boolean", ""};
        traducao.put("boolean", booleanMap);
        String[] stringMap = {"string", ""};
        traducao.put("String", stringMap);
        String[] datetimeMap = {"date", "java.util.Calendar"};
        traducao.put("Datetime", datetimeMap);
    }
}

package Utils;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {
	   private  Map<String, Object> scenarioContext = new HashMap<>();
//	   public void scenarioContext(){
//	       scenarioContext = new HashMap<>();
//	   }
	   public void put(String key, Object value) {
	       scenarioContext.put(key.toString(), value);
	   }
	   public Object get(String key){
	       return scenarioContext.get(key.toString());
	   }
	}
	

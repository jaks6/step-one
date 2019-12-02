package ee.mass.epm;

import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;

public class EngineStats {

    public List<ProcessDefinition> processDefinitions;
    public List<ProcessInstance> runningProcesses;


    public static EngineStats create(Engine e){
        EngineStats stats = new EngineStats();
        stats.processDefinitions = e.repositoryService.createProcessDefinitionQuery().list();
        stats.runningProcesses = e.runtimeService.createProcessInstanceQuery().list();
        return stats;
    }
}

import ee.mass.epm.Engine;
import ee.mass.epm.SampleProcess;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MultipleEnginesTest {


    @Test
    public void myFirstTest() {

        Engine e1 = new Engine();

        Engine e2 = new Engine();

        ArrayList<Engine> engines = new ArrayList<>();
        engines.add(e1);
        engines.add(e2);

        for (Engine e : engines){
            e.deploy("fog.bpmn", SampleProcess.BPMN_STRING);
            List<ProcessDefinition> list = e.getRepositoryService().createProcessDefinitionQuery().list();
            System.out.println(list);
            e.startProcessInstance("fog-process");

            finishOneSimulatedUserTask(e);


            for (int i = 0; i < 5; i++) {
                e.update();
                System.out.println(e.getTaskService().createTaskQuery().list());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exc) {
                exc.printStackTrace();
            }

            System.out.println(e.getTaskService().createTaskQuery().list());
            finishOneSimulatedUserTask(e);

        }






    }

    private static void finishOneSimulatedUserTask(Engine engine) {
        List<Task> tasks = engine.getTaskService().createTaskQuery().taskCategory("simulated").list();
        engine.getTaskService().complete(tasks.get(0).getId());
    }
}

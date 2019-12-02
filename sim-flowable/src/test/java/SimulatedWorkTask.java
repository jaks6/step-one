import ee.mass.epm.Engine;
import ee.mass.epm.SampleProcess;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimulatedWorkTask {

    @Test
    public void main(){
        Engine engine = new Engine();

        engine.deploy("fog.bpmn", SampleProcess.BPMN_STRING);
        engine.startProcessInstance("fog-process");

        assertEquals(1, engine.getTaskService().createTaskQuery().list().size());
        finishOneSimulatedUserTask(engine);


        for (int i = 0; i < 5; i++) {
            engine.update();

//            assertEquals(0, engine.getTaskService().createTaskQuery().list().size());
            System.out.println(engine.getTaskService().createTaskQuery().list());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        System.out.println(engine.getTaskService().createTaskQuery().list());
        assertEquals(1, engine.getTaskService().createTaskQuery().list().size());

        finishOneSimulatedUserTask(engine);


        assertEquals(0, engine.getTaskService().createTaskQuery().list().size());
//        engine.doTasks();

    }

    private static void finishOneSimulatedUserTask(Engine engine) {
        List<Task> tasks = engine.getTaskService().createTaskQuery().taskCategory("simulated").list();
        engine.getTaskService().complete(tasks.get(0).getId());
    }
}

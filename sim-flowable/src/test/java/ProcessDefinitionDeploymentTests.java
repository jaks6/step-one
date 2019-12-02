import ee.mass.epm.Engine;
import ee.mass.epm.SampleProcess;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ProcessDefinitionDeploymentTests {

    Engine e1;

    @Before
    public void setUp(){


        String id = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        e1 = new Engine();

    }
    @Test
    public void inputStreamDeployment() throws FileNotFoundException {
        File process1 = new File("src/test/resources/Fog_Pong.bpmn20.xml");

        e1.deploy(process1.getName(), new FileInputStream(process1));

        int deploymentsSize = e1.getRepositoryService().createDeploymentQuery().list().size();
        int defsSize = e1.getRepositoryService().createProcessDefinitionQuery().list().size();

        assertEquals(1, deploymentsSize);
        assertEquals(1, defsSize);
        ProcessDefinition processDefinition = e1.getRepositoryService().createProcessDefinitionQuery().list().get(0);
        assertEquals("fog-pong", processDefinition.getKey());
        assertEquals("Fog Pong", processDefinition.getName());


    }

    @Test
    public void stringDeployment() throws InterruptedException {

        e1.deploy("fog.bpmn20.xml", SampleProcess.BPMN_STRING);


        int deploymentsSize = e1.getRepositoryService().createDeploymentQuery().list().size();
        int defsSize = e1.getRepositoryService().createProcessDefinitionQuery().list().size();

        assertEquals(1, deploymentsSize);
        assertEquals(1, defsSize);
        ProcessDefinition processDefinition = e1.getRepositoryService().createProcessDefinitionQuery().list().get(0);
        assertEquals("fog-process", processDefinition.getKey());
        assertEquals("Fog Process", processDefinition.getName());



    }


}

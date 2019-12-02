package ee.mass.epm;

public class SampleProcess {

    public static final String BPMN_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
            "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "             xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n" +
            "             xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\"\n" +
            "             xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\"\n" +
            "             xmlns:flowable=\"http://flowable.org/bpmn\"\n" +
            "             xmlns:epmsim=\"http://ut.ee/~jaks/epm\"\n" +
            "\n" +
            "             typeLanguage=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "             expressionLanguage=\"http://www.w3.org/1999/XPath\"\n" +
            "             targetNamespace=\"http://www.flowable.org/processdef\">\n" +
            "\n" +
            "    <process id=\"fog-process\" name=\"Fog Process\" isExecutable=\"true\" >\n" +
            "\n" +
            "        <startEvent id=\"startEvent\"/>\n" +
            "        <sequenceFlow sourceRef=\"startEvent\" targetRef=\"lightTask\" />\n" +
            "\n" +
            "        <userTask id=\"lightTask\" name=\"Light Task\" flowable:category=\"simulated\" flowable:triggerable=\"true\" >\n" +
            "            <extensionElements>\n" +
            "                <flowable:field name=\"job_size\" stringValue=\"5\" />\n" +
            "            </extensionElements>\n" +
            "        </userTask>\n" +
            "        <sequenceFlow sourceRef=\"lightTask\" targetRef=\"javaService\"  />\n" +
            "\n" +
            "        <serviceTask id=\"javaService\"\n" +
            "                     name=\"My Java Service Task\"\n" +
            "                     flowable:class=\"ee.mass.epm.sim.task.SimulatedTask\"\n" +
            "                     flowable:triggerable=\"true\"\n" +
            "                     >\n" +
            "            <extensionElements>\n" +
            "                <flowable:field name=\"jobSize\" stringValue=\"5\" />" +
            "                <epmsim:jobsize value=\"5\"/>\n" +
            "            </extensionElements>\n" +
            "\n" +
            "        </serviceTask>\n" +
            "        <sequenceFlow sourceRef=\"javaService\" targetRef=\"heavyTask\" />\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "        <userTask id=\"heavyTask\" name=\"Heavy Task\" flowable:category=\"simulated\"/>\n" +
            "        <sequenceFlow sourceRef=\"heavyTask\" targetRef=\"rejectEnd\"/>\n" +
            "\n" +
            "\n" +
            "\n" +
            "        <endEvent id=\"rejectEnd\"/>\n" +
            "\n" +
            "    </process>\n" +
            "\n" +
            "</definitions>";


}

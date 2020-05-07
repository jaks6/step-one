package ee.mass.epm.samples.bottomup;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateFogReportsTask implements JavaDelegate {

    Tuple<Integer, HashMap<String, Integer>> report;

    @Override
    public void execute(DelegateExecution execution) {
        LoadReport lastFogReport;
        lastFogReport = execution.getVariable("loadReport", LoadReport.class);


        List<LoadReport> reportList;

        if ( !execution.hasVariable("reportList") ){
            reportList = new ArrayList<>();
        } else {
            reportList = execution.getVariable("reportList", List.class);
        }

        reportList.add(lastFogReport);
        execution.setVariable("reportList", reportList);


    }
}

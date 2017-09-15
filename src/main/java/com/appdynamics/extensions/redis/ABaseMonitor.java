package com.appdynamics.extensions.redis;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This is a base class that each monitor can extend from. It removes all the boiler plate code from all the extension.
 */
public abstract class ABaseMonitor extends AManagedMonitor{

    private static final Logger logger = LoggerFactory.getLogger(ABaseMonitor.class);
    protected String monitorName;
    protected MonitorConfiguration configuration;

    public ABaseMonitor(){
        this.monitorName = getMonitorName();
        logger.info("Using {} Version [{}]",monitorName, getImplementationVersion());
    }

    protected void initialize(Map<String, String> args) {
        if(configuration == null){
            MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
            MonitorConfiguration conf = new MonitorConfiguration(getDefaultMetricPrefix(), createTaskRunner(), metricWriteHelper);
            conf.setConfigYml(args.get("config-file"));
            initializeMoreStuff(conf);
            this.configuration = conf;
        }
    }

    protected void initializeMoreStuff(MonitorConfiguration conf) {
        ;
    }

    @Override
    public TaskOutput execute(Map<String, String> args, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.debug("The raw arguments are {}" + args);
        initialize(args);
        configuration.executeTask();
        return new TaskOutput(String.format("A run of %s completed.",monitorName));
    }

    abstract String getDefaultMetricPrefix();

    abstract String getMonitorName();

    abstract void doRun(ATaskExecutor taskCounter);

    abstract int getTaskCount();

    protected Runnable createTaskRunner() {
        return new Runnable() {
            @Override
            public void run() {
                ATaskExecutor obj = new ATaskExecutor(ABaseMonitor.this);
                doRun(obj);
            }
        };
    }

    protected void onComplete(){
        configuration.getMetricWriter().onTaskComplete();
    }

    protected static String getImplementationVersion() {
        return ABaseMonitor.class.getPackage().getImplementationTitle();
    }

}

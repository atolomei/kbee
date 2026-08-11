

package com.novamens.content.web.admin.markup;



import java.io.*;
import java.lang.management.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.codahale.metrics.jvm.ThreadDump;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.Model;

import com.novamens.wicket.util.BCElement;


import org.apache.wicket.request.resource.ByteArrayResource;

public class JvmDumpPanel extends AbstractSystemInfoPanel {
    			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JvmDumpPanel.class.getName());
	
    public JvmDumpPanel() {
        this("info-panel");
    }

    public JvmDumpPanel(String id) {
        super(id);

        
        

        final ByteArrayResource resource = new ByteArrayResource("text/html") {
            @Override
            protected String getFilename() {
                return "stack" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")) + ".txt";
            }

            @Override
            protected byte[] getData(Attributes attributes) {
                try {
                    final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                    ThreadDump td = new ThreadDump(threadMXBean);
                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    td.dump(byteArrayOutputStream);
                    InputStream targetStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    return IOUtils.toByteArray(targetStream);
                } catch (IOException e) {
                    logger.error(e);
                    return null;
                }

            }

            @Override
            protected void setResponseHeaders(ResourceResponse resourceResponse, Attributes attributes) {
                super.setResponseHeaders(resourceResponse, attributes);
            }
        };

        @SuppressWarnings("rawtypes")
		ResourceLink link = new ResourceLink("download", resource);
        add(link);


        Label tot = new Label("total", String.valueOf(getTotalThreads()));
        Label dump = new Label("dump", getDump());

        dump.setEscapeModelStrings(false);
        add(dump);
        add(tot);

    }


    private int total_threads = -1;
    private String dump_threads = null;

    
    public void onInitialize() {
    	super.onInitialize();
    }

    public String getDump() {
        if (dump_threads == null)
            generateThreadDump();
        return dump_threads;

    }

    public int getTotalThreads() {
        if (total_threads < 0)
            generateThreadDump();
        return total_threads;
    }


    private void generateThreadDump() {
        final StringBuilder dump = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<Long, Float> threadCPUUsage;
        try {
            threadCPUUsage = getThreadCpuUsage();
        }catch (Exception e) {threadCPUUsage = new HashMap<>();}

        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);
        total_threads = threadInfos.length;

        List<ThreadInfo> threadSorted = Arrays.asList(threadInfos);
        final Map<Long, Float> finalThreadCPUUsage = threadCPUUsage;
        final Comparator<ThreadInfo> cpuComparator = Comparator.comparingDouble(t -> finalThreadCPUUsage.containsKey(t.getThreadId()) ? finalThreadCPUUsage.get(t.getThreadId()) : 0d);
        threadSorted.sort(cpuComparator.reversed());

        for (ThreadInfo threadInfo : threadSorted) {
            dump.append("<b>Thread:</b> ");
            dump.append(threadInfo.getThreadName());
            dump.append(" (");
            dump.append(threadInfo.getThreadId());
            dump.append("). ");
            final Thread.State state = threadInfo.getThreadState();
            dump.append(" <b>java.lang.Thread.State:</b> ");
            dump.append(state);
            if (threadInfo.getLockName() != null) {
                dump.append(" <b>Lock Name:</b> ");
                dump.append(threadInfo.getLockName());
            }
            if (threadInfo.getLockOwnerId() > 0) {
                dump.append(" <b>Lock Owner:</b> ");
                dump.append(threadInfo.getLockOwnerName());
                dump.append(" (");
                dump.append(threadInfo.getLockOwnerId());
                dump.append(")");
            }
            if (threadCPUUsage.containsKey(threadInfo.getThreadId())) {
                dump.append(" <b>Cpu%:</b> ");
                dump.append(threadCPUUsage.get(threadInfo.getThreadId()));

            }

            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                dump.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;at ");
                dump.append(stackTraceElement);
            }
            dump.append("<br/>");
            dump.append("<br/>");
        }
        dump_threads = dump.toString();

    }

    private Map<Long, Float> getThreadCpuUsage() {
        int sampleTime = 1000;
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        Map<Long, Long> threadInitialCPU = new HashMap<Long, Long>();
        Map<Long, Float> threadCPUUsage = new HashMap<Long, Float>();
        long initialUptime = runtimeMxBean.getUptime();

        ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(false, false);
        for (ThreadInfo info : threadInfos) {
            threadInitialCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
        }

        try {
            Thread.sleep(sampleTime);
        } catch (InterruptedException e) {
        }
        long upTime = runtimeMxBean.getUptime();

        Map<Long, Long> threadCurrentCPU = new HashMap<Long, Long>();
        threadInfos = threadMxBean.dumpAllThreads(false, false);
        for (ThreadInfo info : threadInfos) {
            threadCurrentCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
        }

        // CPU over all processes
        //int nrCPUs = osMxBean.getAvailableProcessors();
        // total CPU: CPU % can be more than 100% (devided over multiple cpus)
        long nrCPUs = 1;
        // elapsedTime is in ms.
        long elapsedTime = (upTime - initialUptime);
        for (ThreadInfo info : threadInfos) {
            // elapsedCpu is in ns
            Long initialCPU = threadInitialCPU.get(info.getThreadId());
            if (initialCPU != null) {
                long elapsedCpu = threadCurrentCPU.get(info.getThreadId()) - initialCPU;
                float cpuUsage = elapsedCpu / (elapsedTime * 1000000F * nrCPUs);
                threadCPUUsage.put(info.getThreadId(), cpuUsage);
            }
        }

        return threadCPUUsage;
    }

    protected BCElement getPageBCElement() {
        return new BCElement(new Model<String>("JVM Thread"));
    }

}

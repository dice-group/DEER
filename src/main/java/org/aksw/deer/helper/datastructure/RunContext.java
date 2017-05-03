package org.aksw.deer.helper.datastructure;

/**
 * @author Kevin Dreßler
 */
public class RunContext {

    private String subDir;
    private long jobId;

    public RunContext(long jobId, String subDir) {
        this.subDir = subDir;
        this.jobId = jobId;
    }

    public String getSubDir() {
        return subDir;
    }

    public long getJobId() {
        return jobId;
    }
}
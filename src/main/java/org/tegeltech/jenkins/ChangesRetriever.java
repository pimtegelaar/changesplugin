package org.tegeltech.jenkins;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.tegeltech.jenkinscommits.JenkinsApiClient;
import com.tegeltech.jenkinscommits.domain.Changes;
import com.tegeltech.jenkinscommits.domain.CommitsResponse;
import com.tegeltech.jenkinscommits.domain.JenkinsJob;
import com.tegeltech.jenkinscommits.http.HttpClient;
import com.tegeltech.jenkinscommits.mapper.CommitsResponseMapper;
import com.tegeltech.jenkinscommits.mapper.ResponseMapper;
import okhttp3.OkHttpClient;

import java.util.Arrays;
import java.util.List;

public class ChangesRetriever {

    private final JenkinsApiClient jenkinsApiClient;

    public ChangesRetriever() {
        this (new JenkinsApiClient(new HttpClient(new OkHttpClient.Builder().build())));
    }

    public ChangesRetriever(JenkinsApiClient jenkinsApiClient) {
        this.jenkinsApiClient = jenkinsApiClient;
    }

    public Changes retrieve(String jobName, int buildNumber, String mainsrcdirs, String testsrcdirs) {
        JenkinsJob jenkinsJob = new JenkinsJob(jobName);

        String commitsResponse = jenkinsApiClient.fetchCommits(jenkinsJob, buildNumber);

        ResponseMapper responseParser = new ResponseMapper(new XmlMapper());
        CommitsResponse changes = responseParser.parseCommits(commitsResponse);

        List<String> sourceDirs = Arrays.asList(mainsrcdirs.split(","));
        List<String> testSourceDirs = Arrays.asList(testsrcdirs.split(","));
        CommitsResponseMapper commitsResponseMapper = new CommitsResponseMapper(sourceDirs, testSourceDirs);
        return commitsResponseMapper.parse(changes);
    }
}

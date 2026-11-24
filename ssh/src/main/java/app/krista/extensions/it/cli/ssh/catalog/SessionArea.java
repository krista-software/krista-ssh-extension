/*
 * Ssh Extension Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.it.cli.ssh.catalog;

import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.it.cli.ssh.service.SSHService;
import app.krista.extensions.util.EventHandler;
import app.krista.model.base.File;
import app.krista.model.base.FreeForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Domain(id = "catEntryDomain_ce0c6501-f903-44d1-8309-1441dfac045d",
        name = "CLI",
        ecosystemId = "catEntryEcosystem_f7baeeb8-5cbe-4c4c-b0f3-5f2891b36d9d",
        ecosystemName = "IT",
        ecosystemVersion = "0a77e411-4277-4f52-a903-f98770ea398d")
public class SessionArea {

    private static final Logger log = LoggerFactory.getLogger(SessionArea.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SSHService sshService;
    private final EventHandler eventHandler;

    @Inject
    public SessionArea(SSHService sshService, EventHandler eventHandler) {
        this.sshService = sshService;
        this.eventHandler = eventHandler;
    }

    /**
     * Shuts down the executor service gracefully.
     * Waits up to 60 seconds for pending tasks to complete before forcing shutdown.
     */
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_9f1a48ec-5f08-4d95-b49f-b9ab16e14627",
            name = "SSH Command Asynchronous Result As A File",
            description = "This request will accept jobid produced by asynchoronous request and as result file output will be produced.",
            area = "Session",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.File(name = "Output", multipleFileUpload = false, required = false, attributes = {}, options = {})
    public File sSHCommandAsynchronousResultAsAFile(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData,
            @Field(name = "JobId", type = "Text", required = true, attributes = {}, options = {}) String jobId) {
        if (eventName.equals(jobId)) {
            String data = (String) eventData.get("Data");
            try {
                return sshService.createFile(data);
            } catch (IOException cause) {
                throw new IllegalArgumentException(cause.getMessage());
            }
        }
        throw new IllegalArgumentException("Job ID '" + jobId + "' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request.");
    }

    @CatalogRequest(
            id = "localDomainRequest_18f2d94b-0ece-45b9-8331-84e484bdcc37",
            name = "SSH Command Asynchronous Result",
            description = "This request will accept jobid produced by asynchoronous request and as result string output will be produced.",
            area = "Session",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field(name = "Output", type = "Text", required = false, attributes = {}, options = {})
    public String sSHCommandAsynchronousResult(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData,
            @Field(name = "JobId", type = "Text") String jobId) {
        if (eventName.equals(jobId)) {
            return (String) eventData.get("Data");
        }
        throw new IllegalArgumentException("Job ID '" + jobId + "' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request.");
    }

    @CatalogRequest(
            id = "localDomainRequest_ff003f10-c963-444f-b689-de9fd00a317d",
            name = "Run SSH Command Asynchronous",
            description = "This request is used to run any ssh command on the specified host; it accepts parameters such as the username, password used to create sessions, and the command to run. An optional parameter port is also accepted if a port other than the default is needed. As a result, one jobid will be produced which can be useful to fetch the result.",
            area = "Session",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "JobId", required = false)
    public String runSSHCommandAsynchronous(
            @Field.Text(name = "Host Name") String hostName,
            @Field(name = "Port", type = "Number", required = false) Double port,
            @Field.Text(name = "Username") String username,
            @Field.Text(name = "Password") String password,
            @Field.Text(name = "Command") String command,
            @Field(name = "Timeout In Seconds", type = "Number", required = false) Double timeoutInSeconds) {

        final String jobId = UUID.randomUUID().toString();
        executorService.submit(() -> {
            try {
                String output = sshService.executeSSHCommand(username, password, hostName, port, command, timeoutInSeconds);
                FreeForm freeForm = new FreeForm();
                freeForm.put("Data", "Text", output);
                eventHandler.handleEvent(jobId, freeForm);
            } catch (Exception cause) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("SSH command execution failed on host '" + hostName + "'. " + cause.getMessage(), cause.getCause());
            }
        });
        return jobId;
    }

    @CatalogRequest(
            id = "localDomainRequest_fb7fafa3-2929-4466-adbc-cc003312bfb7",
            name = "Run SSH Command",
            description = "This request is used to run any ssh command on the specified host; it accepts parameters such as the username, password used to create sessions, and the command to run. An optional parameter port is also accepted if a port other than the default is needed. As a result, string output will be produced.",
            area = "Session",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Output", required = false)
    public String runSSHCommand(
            @Field.Text(name = "Host Name") String hostName,
            @Field(name = "Port", type = "Number", required = false) Double port,
            @Field.Text(name = "Username") String username,
            @Field.Text(name = "Password") String password,
            @Field.Text(name = "Command") String command,
            @Field(name = "Timeout In Seconds", type = "Number", required = false) Double timeoutInSeconds) {

        try {
            return sshService.executeSSHCommand(username, password, hostName, port, command, timeoutInSeconds);
        } catch (Exception cause) {
            log.error("SSH Command Output - {}", cause.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
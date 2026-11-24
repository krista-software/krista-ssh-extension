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

package app.krista.extensions.it.cli.ssh.service;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.it.cli.ssh.client.KristaMediaClient;
import app.krista.model.base.File;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class SSHService {

    public static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    public static final String SSH_COMMAND_EXECUTION_FAILED = "SSH command execution failed. ";
    private static final Logger log = LoggerFactory.getLogger(SSHService.class);

    private final KristaMediaClient kristaMediaClient;


    @Inject
    public SSHService(KristaMediaClient kristaMediaClient) {
        this.kristaMediaClient = kristaMediaClient;
    }

    public String executeSSHCommand(String username, String password, String host, Double port, String command, Double timeout) {
        Session session = null;
        ChannelExec channel = null;

        try {
            if (username != null && password != null && host != null) {
                session = new JSch().getSession(username, host, getPort(port));
                session.setPassword(password);
                session.setConfig(STRICT_HOST_KEY_CHECKING, "no");
                session.connect();

                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
                channel.setErrStream(errorOutputStream);
                channel.connect();
                if (timeout != null && timeout > 0) {
                    Thread.sleep(timeout.intValue() * 1000L);
                } else {
                    while (!channel.isEOF()) {
                        if (!session.isConnected() || channel.isClosed()) {
                            break;
                        }
                    }
                }

                System.out.println("SSH Command Output - {}" + responseStream);
                System.out.println("SSH Command Error Output - {}" + errorOutputStream);
                closeSessionAndChannel(session, channel);
                return !responseStream.toString().isBlank() ? responseStream.toString() : errorOutputStream.toString();
            }
            return null;
        } catch (Exception cause) {
            closeSessionAndChannel(session, channel);
            cause.printStackTrace();
            Thread.currentThread().interrupt();
            throw new IllegalStateException(SSH_COMMAND_EXECUTION_FAILED + "Unable to connect to host '" + host + "' or execute command. " + cause.getMessage(), cause);
        } finally {
            closeSessionAndChannel(session, channel);
        }
    }

    private void closeSessionAndChannel(Session session, ChannelExec channel) {
        if (session != null) {
            session.disconnect();
        }
        if (channel != null) {
            channel.disconnect();
        }
    }

    public int getPort(Double port) {
        if (port == null) {
            return 22;
        }
        return port.intValue();
    }

    public String getFilePath(File file) {
        try {
            java.io.File downloadedFile = kristaMediaClient.toJavaFile(file);
            return downloadedFile.getAbsolutePath();
        } catch (IOException cause) {
            throw new IllegalArgumentException("Unable to retrieve file. The file may not exist or is inaccessible. " + cause.getMessage(), cause);
        }
    }

    public File createFile(String content) throws IOException {
        String fileExtension = "log";
        java.io.File file = getFileFromString(content, fileExtension);
        return  kristaMediaClient.toKristaFile(file);

    }

    public java.io.File getFileFromString(String content, String extension) throws FileNotFoundException {
        String fileName = KristaMediaClient.ZIP_DIR + "ssh-logs-" +
                new SimpleDateFormat("dd-MM-yyyy HH.mm.ss aa").format(new Date()) + "." + extension;
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            printWriter.write(content + '\n');
        }
        return new java.io.File(fileName);
    }
}

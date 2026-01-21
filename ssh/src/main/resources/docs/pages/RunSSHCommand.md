# Run SSH Command

## Overview

Executes an SSH command on a remote server and returns the command output as text. This is a synchronous operation that waits for the command to complete before returning the result.

## Request Details

- **Area**: Session
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ❌ No - Commands may have side effects and should not be automatically retried

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Host Name | Text | Yes | IP address or hostname of the SSH server | `192.168.1.100` |
| Port | Number | No | SSH port number (default: 22) | `22` |
| Username | Text | Yes | SSH username for authentication | `admin` |
| Password | Text | Yes | SSH password for authentication | `********` |
| Command | Text | Yes | Shell command to execute | `ls -la /home` |
| Timeout In Seconds | Number | No | Maximum execution time in seconds | `30` |

### Parameter Details

#### Host Name
The IP address or fully qualified domain name of the target SSH server.
- Must be reachable from the Krista platform
- Can be IPv4 address (e.g., `192.168.1.100`) or hostname (e.g., `server.example.com`)

#### Port
The SSH port number on the target server.
- Default value: `22`
- Only specify if using a non-standard SSH port

#### Username
The SSH username for authentication.
- Must have permission to execute the specified command
- Case-sensitive on most systems

#### Password
The SSH password for authentication.
- Transmitted securely over SSH protocol
- Case-sensitive

#### Command
The shell command to execute on the remote server.
- Executed in the user's default shell
- Can include pipes, redirects, and command chaining
- Examples: `ls -la`, `cat /etc/hosts`, `df -h && free -m`

#### Timeout In Seconds
Maximum time to wait for command completion.
- If not specified, waits until command completes or connection closes
- Useful for long-running commands to prevent indefinite waiting

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Output | Text | The command output (stdout or stderr) |

### Output Details

- Returns stdout if the command produces standard output
- Returns stderr if the command produces only error output
- Returns empty string if the command produces no output
- Returns `null` if an error occurs during execution

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Host Name is null | SSH command execution failed | Provide a valid hostname or IP address |
| Username is null | SSH command execution failed | Provide a valid username |
| Password is null | SSH command execution failed | Provide a valid password |

## Error Handling

### System Errors (SYSTEM_ERROR)

**SSH Command Execution Failed**

**Cause**: Connection failure, authentication error, or command execution error

**Error Message**: `SSH command execution failed. Unable to connect to host '[hostname]' or execute command. [details]`

**Common Scenarios**:
- Authentication failed (wrong username/password)
- Connection refused (SSH service not running)
- Host unreachable (network issues)
- Connection timeout

**Resolution**:
1. Verify hostname/IP is correct and reachable
2. Verify SSH service is running on the target server
3. Verify username and password are correct
4. Check network connectivity and firewall rules

## Usage Examples

### Example 1: List Directory Contents

**Input**:
```
Host Name: "192.168.1.100"
Port: 22
Username: "admin"
Password: "********"
Command: "ls -la /home"
Timeout In Seconds: 30
```

**Output**:
```
total 12
drwxr-xr-x  3 root  root  4096 Jan 15 10:00 .
drwxr-xr-x 23 root  root  4096 Jan 10 08:00 ..
drwxr-xr-x  5 admin admin 4096 Jan 15 10:00 admin
```

### Example 2: Check Disk Space

**Input**:
```
Host Name: "server.example.com"
Username: "monitor"
Password: "********"
Command: "df -h"
```

**Output**:
```
Filesystem      Size  Used Avail Use% Mounted on
/dev/sda1        50G   25G   23G  53% /
/dev/sdb1       100G   60G   35G  63% /data
```

### Example 3: Execute Multiple Commands

**Input**:
```
Host Name: "192.168.1.100"
Username: "admin"
Password: "********"
Command: "uptime && free -m && df -h /"
```

**Output**:
```
 10:30:00 up 45 days,  3:22,  2 users,  load average: 0.15, 0.10, 0.05
              total        used        free      shared  buff/cache   available
Mem:           7982        2156        3824         256        2001        5312
Swap:          2047           0        2047
Filesystem      Size  Used Avail Use% Mounted on
/dev/sda1        50G   25G   23G  53% /
```

## Business Rules

1. **Single Command Execution** - Each request executes one command (can include chained commands with `&&`, `;`, or `|`)
2. **Synchronous Operation** - Request blocks until command completes or timeout expires
3. **No Session Persistence** - Each request creates a new SSH session
4. **Output Capture** - Both stdout and stderr are captured
5. **Exit Code Not Returned** - Only command output is returned, not the exit code

## Limitations

1. **No SSH Key Authentication** - Only password authentication is supported
2. **No Interactive Commands** - Commands requiring user input are not supported
3. **Output Size** - Large outputs may be limited by JVM heap memory
4. **No Session Reuse** - Each request creates a new connection (overhead for multiple commands)
5. **Host Key Verification Disabled** - StrictHostKeyChecking is set to "no"

## Best Practices

### 1. Use Appropriate Timeouts
Set timeout values based on expected command duration to prevent indefinite waiting.

### 2. Chain Related Commands
Use `&&` or `;` to execute multiple related commands in a single request.

### 3. Handle Empty Output
Check for empty or null output in your Krista application logic.

### 4. Use Absolute Paths
Specify absolute paths for files and directories to avoid path resolution issues.

### 5. Escape Special Characters
Properly escape special characters in commands if needed.

## Common Use Cases

### 1. Server Health Check
```
Scenario: Check server health metrics
Command: "uptime && free -m && df -h"
Result: Returns system uptime, memory usage, and disk space
```

### 2. Log File Retrieval
```
Scenario: Get last 100 lines of a log file
Command: "tail -100 /var/log/application.log"
Result: Returns recent log entries
```

### 3. Service Status Check
```
Scenario: Check if a service is running
Command: "systemctl status nginx"
Result: Returns service status information
```

## Related Catalog Requests

- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md) - For long-running commands
- [SSH Command Asynchronous Result](pages/SSHCommandAsynchronousResult.md) - Get async command results
- [SSH Command Asynchronous Result As A File](pages/SSHCommandAsynchronousResultAsAFile.md) - Get async results as file

## Technical Implementation

### Service Class
- **Class**: `SSHService`
- **Package**: `app.krista.extensions.it.cli.ssh.service`
- **Method**: `executeSSHCommand()`

### SSH Library
- **Library**: JSch 0.1.55
- **Channel Type**: exec

## Troubleshooting

### Command Returns Null

**Cause**: An exception occurred during execution

**Solution**: Check the Krista logs for detailed error messages. Verify connection parameters.

### Command Times Out

**Cause**: Command takes longer than specified timeout

**Solution**: Increase the timeout value or use asynchronous execution for long-running commands.

### Partial Output Returned

**Cause**: Command was interrupted or connection was lost

**Solution**: Check network stability. Consider using asynchronous execution with file output.

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md)


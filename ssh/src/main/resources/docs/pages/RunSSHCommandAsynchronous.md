# Run SSH Command Asynchronous

## Overview

Executes an SSH command on a remote server asynchronously and immediately returns a Job ID. The command runs in the background, and results can be retrieved later using the Job ID with the "SSH Command Asynchronous Result" or "SSH Command Asynchronous Result As A File" requests.

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
| Command | Text | Yes | Shell command to execute | `find / -name "*.log"` |
| Timeout In Seconds | Number | No | Maximum execution time in seconds | `300` |

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
- Ideal for long-running commands that may take minutes or hours
- Can include pipes, redirects, and command chaining
- Examples: `find / -name "*.log"`, `tar -czf backup.tar.gz /data`

#### Timeout In Seconds
Maximum time to wait for command completion.
- Recommended for long-running commands
- If not specified, waits until command completes

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Job Id | Text | Unique identifier (UUID) for retrieving results later |

### Output Details

- Returns a UUID string immediately after submitting the command
- Job ID is used to retrieve results with other catalog requests
- Job ID format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Host Name is null | SSH command execution failed | Provide a valid hostname or IP address |
| Username is null | SSH command execution failed | Provide a valid username |
| Password is null | SSH command execution failed | Provide a valid password |

## Error Handling

### System Errors (SYSTEM_ERROR)

**SSH Command Execution Failed**

**Cause**: Connection failure or command execution error during async execution

**Error Message**: `SSH command execution failed on host '[hostname]'. [details]`

**Common Scenarios**:
- Authentication failed (wrong username/password)
- Connection refused (SSH service not running)
- Host unreachable (network issues)
- Command execution error

**Resolution**:
1. Verify hostname/IP is correct and reachable
2. Verify SSH service is running on the target server
3. Verify username and password are correct
4. Check network connectivity and firewall rules

> **📝 Note**: Errors during async execution are captured and returned when retrieving results.

## Usage Examples

### Example 1: Long-Running File Search

**Input**:
```
Host Name: "192.168.1.100"
Username: "admin"
Password: "********"
Command: "find / -name '*.log' -mtime -7"
Timeout In Seconds: 600
```

**Output**:
```
Job Id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**Result Retrieval**:
Use the Job ID with "SSH Command Asynchronous Result" to get the file list.

### Example 2: Database Backup

**Input**:
```
Host Name: "db-server.example.com"
Username: "backup_user"
Password: "********"
Command: "pg_dump -U postgres mydb > /backup/mydb_$(date +%Y%m%d).sql"
Timeout In Seconds: 1800
```

**Output**:
```
Job Id: "b2c3d4e5-f6a7-8901-bcde-f23456789012"
```

### Example 3: System Update

**Input**:
```
Host Name: "192.168.1.50"
Username: "root"
Password: "********"
Command: "apt-get update && apt-get upgrade -y"
Timeout In Seconds: 3600
```

**Output**:
```
Job Id: "c3d4e5f6-a7b8-9012-cdef-345678901234"
```

## Business Rules

1. **Immediate Return** - Request returns immediately with Job ID without waiting for command completion
2. **Single-Threaded Execution** - Only one async command executes at a time (queued execution)
3. **Event-Driven Results** - Results are delivered via Krista's event system when command completes
4. **Job ID Persistence** - Job IDs remain valid until results are retrieved
5. **No Duplicate Execution** - Each request generates a unique Job ID

## Limitations

1. **Single Concurrent Command** - Only one async command can execute at a time due to single-threaded executor
2. **No SSH Key Authentication** - Only password authentication is supported
3. **No Interactive Commands** - Commands requiring user input are not supported
4. **Job ID Expiration** - Job IDs may expire if results are not retrieved
5. **No Progress Tracking** - Cannot check command progress; only completion status

## Best Practices

### 1. Use for Long-Running Commands
Reserve async execution for commands expected to take more than a few seconds.

### 2. Store Job IDs
Save the returned Job ID for later result retrieval.

### 3. Set Appropriate Timeouts
Configure timeout values based on expected command duration.

### 4. Handle Execution Errors
Check for errors when retrieving results, as execution errors are captured asynchronously.

### 5. Use File Output for Large Results
For commands with large output, use "SSH Command Asynchronous Result As A File" to avoid memory issues.

## Async Execution Flow

```
┌──────────────┐    ┌─────────────┐    ┌────────────────┐
│  User        │───►│ SessionArea │───►│ ExecutorService│───┐
│  Request     │    │             │    │ (Background)   │   │
└──────────────┘    └──────┬──────┘    └────────────────┘   │
                           │                                │
                           ▼                                ▼
                   ┌──────────────┐              ┌────────────────┐
                   │  Return      │              │  SSHService    │
                   │  Job ID      │              │  (Async Exec)  │
                   │  (UUID)      │              └───────┬────────┘
                   └──────────────┘                      │
                                                         ▼
                                                ┌────────────────┐
                                                │  EventHandler  │
                                                │  (Trigger)     │
                                                └────────────────┘
```

**Steps:**
1. User submits async request with connection parameters and command
2. UUID Job ID is generated immediately
3. Task is submitted to background ExecutorService
4. Job ID is returned to user (non-blocking)
5. Background thread executes SSH command
6. On completion, EventHandler triggers event with Job ID
7. User retrieves result using Job ID

## Common Use Cases

### 1. Scheduled Backups
```
Scenario: Run database backup during off-hours
Command: "pg_dump -U postgres production > /backup/prod_backup.sql"
Result: Returns Job ID; backup runs in background
```

### 2. Log Analysis
```
Scenario: Search large log files for patterns
Command: "grep -r 'ERROR' /var/log/application/ > /tmp/errors.txt"
Result: Returns Job ID; search runs in background
```

### 3. System Maintenance
```
Scenario: Run system updates
Command: "yum update -y && yum clean all"
Result: Returns Job ID; updates run in background
```

## Related Catalog Requests

- [Run SSH Command](pages/RunSSHCommand.md) - For quick, synchronous commands
- [SSH Command Asynchronous Result](pages/SSHCommandAsynchronousResult.md) - Get async command results as text
- [SSH Command Asynchronous Result As A File](pages/SSHCommandAsynchronousResultAsAFile.md) - Get async results as file

## Technical Implementation

### Area Class
- **Class**: `SessionArea`
- **Package**: `app.krista.extensions.it.cli.ssh.catalog`
- **Method**: `runSSHCommandAsynchronous()`

### Executor Service
- **Type**: Single-threaded ExecutorService
- **Behavior**: Commands are queued and executed sequentially

### Event Handler
- **Class**: `EventHandler`
- **Purpose**: Triggers event when async command completes

## Troubleshooting

### Job ID Not Returned

**Cause**: Request failed before Job ID could be generated

**Solution**: Check connection parameters and verify SSH server is accessible.

### Results Not Available

**Cause**: Command is still executing or failed

**Solution**: Wait for command to complete. Check timeout settings.

### Command Queued for Long Time

**Cause**: Previous async command is still running

**Solution**: Wait for previous command to complete. Only one async command runs at a time.

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [SSH Command Asynchronous Result](pages/SSHCommandAsynchronousResult.md)
- [SSH Command Asynchronous Result As A File](pages/SSHCommandAsynchronousResultAsAFile.md)


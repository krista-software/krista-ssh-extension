# SSH Command Asynchronous Result

## Overview

Retrieves the output of a previously executed asynchronous SSH command as text. This request waits for the async command to complete and returns the command output using the Job ID returned by "Run SSH Command Asynchronous".

## Request Details

- **Area**: Session
- **Type**: WAIT_FOR_EVENT
- **Retry Support**: ❌ No - Event-based request that waits for command completion

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Job Id | Text | Yes | The Job ID returned by "Run SSH Command Asynchronous" | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |

### Parameter Details

#### Job Id
The unique identifier returned by the "Run SSH Command Asynchronous" request.
- Must be the exact UUID returned by the async request
- Case-sensitive
- Format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

> **📝 Note**: The Job ID is only valid for the specific async command that generated it.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Output | Text | The command output (stdout or stderr) from the async command |

### Output Details

- Returns the complete output from the async command
- Includes both stdout and stderr content
- Returns empty string if command produced no output
- Waits for command completion before returning

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Job ID not found | Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request. | Use the exact Job ID from the async request |
| Job ID is null | Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request. | Provide a valid Job ID |

## Error Handling

### Input Errors (INPUT_ERROR)

**Invalid Job ID**

**Cause**: The provided Job ID does not match any pending async command

**Error Message**: `Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request.`

**Common Scenarios**:
- Typo in Job ID
- Job ID from a different async request
- Job ID has already been used to retrieve results
- Job ID has expired

**Resolution**:
1. Verify the Job ID matches exactly what was returned by the async request
2. Ensure you're using the correct Job ID for the intended command
3. Check if results were already retrieved (Job IDs are single-use)

## Usage Examples

### Example 1: Retrieve File Search Results

**Async Request** (Run SSH Command Asynchronous):
```
Host Name: "192.168.1.100"
Username: "admin"
Password: "********"
Command: "find /var/log -name '*.log' -mtime -1"
```

**Async Response**:
```
Job Id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**Result Request** (SSH Command Asynchronous Result):
```
Job Id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**Output**:
```
/var/log/syslog
/var/log/auth.log
/var/log/kern.log
/var/log/application/app.log
```

### Example 2: Retrieve Backup Status

**Async Request**:
```
Command: "pg_dump -U postgres mydb > /backup/mydb.sql && echo 'Backup completed successfully'"
```

**Async Response**:
```
Job Id: "b2c3d4e5-f6a7-8901-bcde-f23456789012"
```

**Result Request**:
```
Job Id: "b2c3d4e5-f6a7-8901-bcde-f23456789012"
```

**Output**:
```
Backup completed successfully
```

### Example 3: Retrieve System Update Results

**Async Request**:
```
Command: "apt-get update && apt-get upgrade -y"
```

**Async Response**:
```
Job Id: "c3d4e5f6-a7b8-9012-cdef-345678901234"
```

**Result Request**:
```
Job Id: "c3d4e5f6-a7b8-9012-cdef-345678901234"
```

**Output**:
```
Hit:1 http://archive.ubuntu.com/ubuntu focal InRelease
Get:2 http://archive.ubuntu.com/ubuntu focal-updates InRelease [114 kB]
...
0 upgraded, 0 newly installed, 0 to remove and 0 not upgraded.
```

## Business Rules

1. **Event-Based Retrieval** - Request waits for the async command to complete before returning
2. **Single Use** - Each Job ID can only be used once to retrieve results
3. **Text Output** - Returns output as plain text string
4. **Complete Output** - Returns the entire command output, not partial results
5. **No Timeout** - Waits indefinitely for command completion

## Limitations

1. **Single Use Job ID** - Job ID cannot be reused after results are retrieved
2. **No Partial Results** - Cannot retrieve partial output while command is running
3. **Memory Constraints** - Large outputs may be limited by JVM heap memory
4. **No Progress Indication** - Cannot check command progress
5. **Text Only** - For file output, use "SSH Command Asynchronous Result As A File"

## Best Practices

### 1. Store Job IDs Securely
Save Job IDs immediately after async request returns.

### 2. Use Correct Job ID
Double-check Job ID before submitting result request.

### 3. Handle Large Outputs
For commands with large output, consider using file-based result retrieval.

### 4. Plan for Waiting
This request blocks until the async command completes; plan your workflow accordingly.

### 5. Error Handling
Implement error handling for invalid Job ID scenarios.

## Event Flow

```
┌──────────────┐                    ┌─────────────┐                    ┌──────────────┐
│  Async       │                    │ Event       │                    │  Result      │
│  Command     │                    │ Handler     │                    │  Request     │
└──────┬───────┘                    └──────┬──────┘                    └──────┬───────┘
       │                                   │                                  │
       │── Execute Command ───────────────►│                                  │
       │                                   │                                  │
       │                                   │◄── Wait for Event (Job ID) ──────│
       │                                   │                                  │
       │── Command Complete ──────────────►│                                  │
       │   (Trigger Event)                 │                                  │
       │                                   │── Return Output ────────────────►│
       │                                   │                                  │
```

## Common Use Cases

### 1. Long-Running Script Results
```
Scenario: Retrieve results of a data processing script
Job Id: "uuid-from-async-request"
Result: Complete script output
```

### 2. Backup Verification
```
Scenario: Verify backup completed successfully
Job Id: "uuid-from-backup-command"
Result: Backup status and any error messages
```

### 3. Log Analysis Results
```
Scenario: Get results of log search
Job Id: "uuid-from-grep-command"
Result: Matching log entries
```

## Related Catalog Requests

- [Run SSH Command](pages/RunSSHCommand.md) - For quick, synchronous commands
- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md) - Submit async commands
- [SSH Command Asynchronous Result As A File](pages/SSHCommandAsynchronousResultAsAFile.md) - Get results as file

## Technical Implementation

### Area Class
- **Class**: `SessionArea`
- **Package**: `app.krista.extensions.it.cli.ssh.catalog`
- **Method**: `sSHCommandAsynchronousResult()`

### Event Handling
- **Event Name**: Job ID (UUID)
- **Event Data**: FreeForm containing "Data" key with command output

## Troubleshooting

### Job ID Not Found

**Cause**: Invalid or expired Job ID

**Solution**: 
1. Verify Job ID is correct
2. Check if results were already retrieved
3. Ensure async command was submitted successfully

### Empty Output Returned

**Cause**: Command produced no output

**Solution**: 
1. Verify command syntax
2. Check if command writes to stdout
3. Consider redirecting output in the command

### Request Hangs

**Cause**: Async command is still running

**Solution**: 
1. Wait for command to complete
2. Check if command has appropriate timeout
3. Verify command is not stuck

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md)
- [SSH Command Asynchronous Result As A File](pages/SSHCommandAsynchronousResultAsAFile.md)


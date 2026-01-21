# SSH Command Asynchronous Result As A File

## Overview

Retrieves the output of a previously executed asynchronous SSH command as a downloadable log file. This request waits for the async command to complete and returns the command output as a `.log` file using the Job ID returned by "Run SSH Command Asynchronous".

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
| Output | File | A `.log` file containing the command output |

### Output Details

- Returns a Krista File object
- File format: `.log`
- File name format: `ssh-logs-DD-MM-YYYY HH.mm.ss AM/PM.log`
- Contains the complete command output (stdout and stderr)
- File is stored in Krista's media server

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Job ID not found | Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request. | Use the exact Job ID from the async request |
| Job ID is null | Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request. | Provide a valid Job ID |
| File creation error | [IOException message] | Check file system permissions and disk space |

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

### System Errors (SYSTEM_ERROR)

**File Creation Error**

**Cause**: Unable to create the output file

**Error Message**: `[IOException details]`

**Common Scenarios**:
- Disk space exhausted
- File system permissions issue
- Temporary directory not accessible

**Resolution**:
1. Check available disk space on the Krista server
2. Verify file system permissions
3. Contact system administrator

**Unsupported File Format**

**Cause**: File extension cannot be determined

**Error Message**: `Unable to determine file type. The file '[filename]' must have a valid extension (e.g., .log, .txt).`

**Resolution**: This error should not occur for this request as the extension is always `.log`.

## Usage Examples

### Example 1: Retrieve Large Log Search Results

**Async Request** (Run SSH Command Asynchronous):
```
Host Name: "192.168.1.100"
Username: "admin"
Password: "********"
Command: "grep -r 'ERROR' /var/log/application/"
Timeout In Seconds: 600
```

**Async Response**:
```
Job Id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**Result Request** (SSH Command Asynchronous Result As A File):
```
Job Id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**Output**:
```
File: ssh-logs-27-11-2025 10.30.45 AM.log
Content: [All matching ERROR lines from log files]
```

### Example 2: Retrieve System Inventory

**Async Request**:
```
Command: "lshw -short && lsblk && df -h && free -m"
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
File: ssh-logs-27-11-2025 02.15.30 PM.log
Content: [Complete hardware and storage inventory]
```

### Example 3: Retrieve Backup Log

**Async Request**:
```
Command: "tar -czvf /backup/data.tar.gz /data 2>&1"
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
File: ssh-logs-27-11-2025 11.45.00 PM.log
Content: [List of all files added to the archive]
```

## Business Rules

1. **Event-Based Retrieval** - Request waits for the async command to complete before returning
2. **Single Use** - Each Job ID can only be used once to retrieve results
3. **File Output** - Returns output as a downloadable `.log` file
4. **Complete Output** - File contains the entire command output
5. **Timestamped Filename** - File name includes creation timestamp
6. **Krista Storage** - File is stored in Krista's media server

## Limitations

1. **Single Use Job ID** - Job ID cannot be reused after results are retrieved
2. **No Partial Results** - Cannot retrieve partial output while command is running
3. **Fixed File Format** - Output is always a `.log` file
4. **Temporary Storage** - Files are stored in `/tmp/` directory on the server
5. **No Progress Indication** - Cannot check command progress

## Best Practices

### 1. Use for Large Outputs
Prefer file output for commands that generate large amounts of output.

### 2. Store Job IDs Securely
Save Job IDs immediately after async request returns.

### 3. Download Files Promptly
Download result files promptly as temporary files may be cleaned up.

### 4. Archive Important Results
Save important result files to permanent storage.

### 5. Use Descriptive Commands
Include timestamps or identifiers in commands for easier tracking.

## File Creation Flow

```
┌──────────────┐    ┌─────────────┐    ┌────────────────┐    ┌──────────────┐
│  Async       │    │ Event       │    │  SSHService    │    │ Krista Media │
│  Command     │    │ Handler     │    │                │    │ Server       │
└──────┬───────┘    └──────┬──────┘    └───────┬────────┘    └──────┬───────┘
       │                   │                   │                    │
       │── Complete ──────►│                   │                    │
       │                   │── Get Output ────►│                    │
       │                   │                   │── Create File ────►│
       │                   │                   │   (ssh-logs-*.log) │
       │                   │                   │◄── File Handle ────│
       │                   │◄── Krista File ───│                    │
       │                   │                   │                    │
```

## Common Use Cases

### 1. Audit Log Collection
```
Scenario: Collect security audit logs for review
Command: "cat /var/log/auth.log"
Result: Downloadable log file for offline analysis
```

### 2. System Diagnostics
```
Scenario: Collect system diagnostic information
Command: "dmesg && journalctl -xe"
Result: Comprehensive diagnostic log file
```

### 3. Application Log Export
```
Scenario: Export application logs for troubleshooting
Command: "cat /var/log/application/*.log"
Result: Combined application log file
```

## Related Catalog Requests

- [Run SSH Command](pages/RunSSHCommand.md) - For quick, synchronous commands
- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md) - Submit async commands
- [SSH Command Asynchronous Result](pages/SSHCommandAsynchronousResult.md) - Get results as text

## Technical Implementation

### Area Class
- **Class**: `SessionArea`
- **Package**: `app.krista.extensions.it.cli.ssh.catalog`
- **Method**: `sSHCommandAsynchronousResultAsAFile()`

### Service Class
- **Class**: `SSHService`
- **Method**: `createFile()`
- **File Extension**: `.log`

### Media Client
- **Class**: `KristaMediaClient`
- **Method**: `toKristaFile()`
- **Storage**: Krista File Repository

## Troubleshooting

### Job ID Not Found

**Cause**: Invalid or expired Job ID

**Solution**: 
1. Verify Job ID is correct
2. Check if results were already retrieved
3. Ensure async command was submitted successfully

### File Download Fails

**Cause**: File creation or storage error

**Solution**: 
1. Check Krista server disk space
2. Verify file repository is accessible
3. Check server logs for errors

### Empty File Returned

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
- [SSH Command Asynchronous Result](pages/SSHCommandAsynchronousResult.md)


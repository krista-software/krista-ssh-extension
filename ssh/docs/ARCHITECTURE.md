# SSH Extension Architecture Document

## 1. Overview

The Krista SSH Extension enables secure remote command execution on SSH-enabled servers. It provides both synchronous and asynchronous command execution capabilities with support for text and file-based output.

### Key Technologies
- **Java 11** - Runtime environment
- **JSch 0.1.55** - SSH protocol implementation
- **HK2** - Dependency injection framework
- **Krista Extension Framework** - Platform integration

### Extension Metadata
| Property | Value |
|----------|-------|
| Extension Version | 2.0.2 |
| Domain | CLI |
| Ecosystem | IT |

---

## 2. Architecture Layers

The SSH Extension follows a 5-layer architecture pattern:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           KRISTA PLATFORM                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  LAYER 1: EXTENSION ENTRY POINT                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  SSHExtension.java                                                    │  │
│  │  - @Extension(version = "2.0.2")                                      │  │
│  │  - @StaticResource(path = "docs", file = "docs")                      │  │
│  │  - Custom tabs configuration                                          │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  LAYER 2: CATALOG REQUEST LAYER                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  SessionArea.java                                                     │  │
│  │  @Domain(id="CLI", ecosystem="IT")                                    │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐   │  │
│  │  │ Run SSH Command │  │ Run SSH Async   │  │ Get Async Result    │   │  │
│  │  │ (CHANGE_SYSTEM) │  │ (CHANGE_SYSTEM) │  │ (WAIT_FOR_EVENT)    │   │  │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  LAYER 3: SERVICE LAYER                                                     │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  SSHService.java                                                      │  │
│  │  - executeSSHCommand()     - SSH connection & command execution       │  │
│  │  - createFile()            - Output file generation                   │  │
│  │  - getFilePath()           - File path resolution                     │  │
│  │  - closeSessionAndChannel() - Resource cleanup                        │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  LAYER 4: CLIENT LAYER                                                      │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  KristaMediaClient.java                                               │  │
│  │  - toKristaFile()          - Upload to Krista media server            │  │
│  │  - toJavaFile()            - Download from Krista media server        │  │
│  │  - compressFile()          - ZIP compression for unsupported formats  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  LAYER 5: EXTERNAL INTEGRATION LAYER                                        │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐  │
│  │  JSch SSH Library   │  │  Krista File        │  │  Remote SSH         │  │
│  │  - Session mgmt     │  │  Repository         │  │  Servers            │  │
│  │  - Channel exec     │  │  - FileHandle       │  │  - Linux/Unix       │  │
│  │  - Authentication   │  │  - FileRepository   │  │  - Windows (SSH)    │  │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              SSH Extension                                    │
│                                                                              │
│  ┌────────────────────┐                                                      │
│  │   SSHExtension     │◄─────── Extension Entry Point                        │
│  │   (v2.0.2)         │         - Version management                         │
│  └─────────┬──────────┘         - Static resources                           │
│            │                                                                 │
│            ▼                                                                 │
│  ┌────────────────────┐         ┌────────────────────┐                       │
│  │    SessionArea     │────────►│    EventHandler    │                       │
│  │                    │         │  (Async Events)    │                       │
│  │  - runSSHCommand() │         └────────────────────┘                       │
│  │  - runSSHAsync()   │                                                      │
│  │  - getAsyncResult()│                                                      │
│  │  - getAsyncFile()  │                                                      │
│  └─────────┬──────────┘                                                      │
│            │                                                                 │
│            ▼                                                                 │
│  ┌────────────────────┐         ┌────────────────────┐                       │
│  │    SSHService      │────────►│  KristaMediaClient │                       │
│  │                    │         │                    │                       │
│  │  - executeSSH()    │         │  - toKristaFile()  │                       │
│  │  - createFile()    │         │  - toJavaFile()    │                       │
│  └─────────┬──────────┘         └─────────┬──────────┘                       │
│            │                              │                                  │
│            ▼                              ▼                                  │
│  ┌────────────────────┐         ┌────────────────────┐                       │
│  │   JSch Library     │         │   FileRepository   │                       │
│  │   (SSH Protocol)   │         │   (Krista Storage) │                       │
│  └─────────┬──────────┘         └────────────────────┘                       │
│            │                                                                 │
└────────────┼─────────────────────────────────────────────────────────────────┘
             │
             ▼
    ┌────────────────────┐
    │   Remote SSH       │
    │   Servers          │
    │   (Port 22)        │
    └────────────────────┘
```

---

## 4. Data Flow

### 4.1 Synchronous Command Execution Flow

```
┌──────────┐    ┌─────────────┐    ┌────────────┐    ┌──────────┐    ┌────────────┐
│  User    │───►│ SessionArea │───►│ SSHService │───►│  JSch    │───►│ Remote SSH │
│  Request │    │             │    │            │    │  Session │    │  Server    │
└──────────┘    └─────────────┘    └────────────┘    └──────────┘    └────────────┘
                                                                            │
┌──────────┐    ┌─────────────┐    ┌────────────┐    ┌──────────┐           │
│  Output  │◄───│ SessionArea │◄───│ SSHService │◄───│  JSch    │◄──────────┘
│  (Text)  │    │             │    │            │    │  Channel │
└──────────┘    └─────────────┘    └────────────┘    └──────────┘
```

**Steps:**
1. User submits request with host, credentials, and command
2. SessionArea validates input and calls SSHService
3. SSHService creates JSch session with credentials
4. JSch connects to remote server (port 22 or custom)
5. Command is executed via exec channel
6. Output (stdout/stderr) is captured
7. Session and channel are closed
8. Output returned to user

### 4.2 Asynchronous Command Execution Flow

```
┌──────────┐    ┌─────────────┐    ┌────────────────┐
│  User    │───►│ SessionArea │───►│ ExecutorService│───┐
│  Request │    │             │    │ (Background)   │   │
└──────────┘    └──────┬──────┘    └────────────────┘   │
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
1. User submits async request
2. UUID Job ID is generated immediately
3. Task submitted to ExecutorService
4. Job ID returned to user (non-blocking)
5. Background thread executes SSH command
6. On completion, EventHandler triggers event with Job ID
7. User retrieves result using Job ID

---

## 5. Sequence Diagram

### 5.1 Run SSH Command (Synchronous)

```
User          SessionArea       SSHService         JSch           Remote Server
  │                │                │                │                  │
  │─── Request ───►│                │                │                  │
  │  (host,user,   │                │                │                  │
  │   pass,cmd)    │                │                │                  │
  │                │── execute() ──►│                │                  │
  │                │                │── getSession()►│                  │
  │                │                │                │── connect() ────►│
  │                │                │                │◄── connected ────│
  │                │                │── openChannel()│                  │
  │                │                │── setCommand() │                  │
  │                │                │── connect() ──►│                  │
  │                │                │                │── execute ──────►│
  │                │                │                │◄── output ───────│
  │                │                │◄── output ─────│                  │
  │                │                │── disconnect()►│                  │
  │                │◄── output ─────│                │                  │
  │◄── output ─────│                │                │                  │
  │                │                │                │                  │
```

---

## 6. Performance Limitations

### 6.1 Connection Limitations

| Limitation | Value | Impact |
|------------|-------|--------|
| Concurrent Async Commands | 1 (Single-threaded executor) | Only one async command executes at a time |
| Session Reuse | None | New connection per command (overhead) |
| Connection Timeout | JVM default | May hang on unreachable hosts |
| Max Output Size | JVM heap memory | Large outputs may cause OOM |

### 6.2 Throughput Limitations

| Metric | Typical Value | Notes |
|--------|---------------|-------|
| Connection establishment | 1-5 seconds | Network latency dependent |
| Authentication | < 1 second | After connection |
| Command execution | Variable | Depends on command complexity |
| Output transfer | Network dependent | Large outputs take longer |

### 6.3 Resource Consumption

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Resource Usage Timeline                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Memory ▲                                                                   │
│         │     ┌─────────────────────────────────────┐                       │
│         │     │  Output Buffer (ByteArrayOutputStream)                      │
│         │  ┌──┴─────────────────────────────────────┴──┐                    │
│         │  │  Session + Channel Objects                │                    │
│         └──┴───────────────────────────────────────────┴──────────► Time    │
│            │                                           │                    │
│         Connect                                    Disconnect               │
│                                                                             │
│  Threads ▲                                                                  │
│          │  ┌──────────────────────────────────────────┐                    │
│          │  │  Main Thread (Sync) / Executor (Async)   │                    │
│          └──┴──────────────────────────────────────────┴──────────► Time    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.4 Recommendations

| Scenario | Recommendation |
|----------|----------------|
| Long-running commands | Use async execution with appropriate timeout |
| Large output | Use file output (getAsyncResultAsFile) |
| Multiple commands | Chain with `&&` or `;` in single request |
| Frequent connections | Consider connection pooling (future enhancement) |

---

## 7. Error Scenarios

### 7.1 Error Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Error Handling Flow                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐     ┌──────────────────────────────────────────────────────┐
│ User Request │────►│                    SessionArea                       │
└──────────────┘     │  ┌─────────────────────────────────────────────────┐ │
                     │  │ Input Validation                                │ │
                     │  │ - Null checks (host, user, pass)                │ │
                     │  │ - Job ID validation                             │ │
                     │  └──────────────────────┬──────────────────────────┘ │
                     │                         │                            │
                     │                         ▼                            │
                     │  ┌─────────────────────────────────────────────────┐ │
                     │  │ SSHService Execution                            │ │
                     │  │ ┌─────────────────────────────────────────────┐ │ │
                     │  │ │ Connection Errors                           │ │ │
                     │  │ │ - Auth fail                                 │ │ │
                     │  │ │ - Connection refused                        │ │ │
                     │  │ │ - Host unreachable                          │ │ │
                     │  │ │ - Socket timeout                            │ │ │
                     │  │ └─────────────────────────────────────────────┘ │ │
                     │  │ ┌─────────────────────────────────────────────┐ │ │
                     │  │ │ Execution Errors                            │ │ │
                     │  │ │ - Command not found                         │ │ │
                     │  │ │ - Permission denied                         │ │ │
                     │  │ │ - Timeout exceeded                          │ │ │
                     │  │ └─────────────────────────────────────────────┘ │ │
                     │  └──────────────────────┬──────────────────────────┘ │
                     │                         │                            │
                     │                         ▼                            │
                     │  ┌─────────────────────────────────────────────────┐ │
                     │  │ File Handling Errors (KristaMediaClient)        │ │
                     │  │ - File not found                                │ │
                     │  │ - Unsupported file format                       │ │
                     │  │ - I/O errors                                    │ │
                     │  └─────────────────────────────────────────────────┘ │
                     └──────────────────────────────────────────────────────┘
```

### 7.2 Error Categories

| Category | Error Message | Cause | Solution |
|----------|---------------|-------|----------|
| **Connection** | SSH command execution failed. Unable to connect to host '[host]' or execute command. | Auth fail, connection refused, network issue | Verify credentials, check SSH service, verify network |
| **Job ID** | Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request. | Invalid or expired Job ID | Use exact Job ID from async request |
| **File** | Unable to retrieve file. The file may not exist or is inaccessible. | File not found | Verify file exists |
| **Format** | Unable to determine file type. The file '[filename]' must have a valid extension. | Missing file extension | Add valid extension |

### 7.3 Exception Hierarchy

```
Exception
├── IllegalArgumentException
│   ├── Invalid Job ID
│   └── Unsupported file format
├── IllegalStateException
│   └── SSH command execution failed
└── IOException
    ├── File not found
    └── I/O error during file operations
```

---

## 8. Security Considerations

### 8.1 Authentication Flow

```
┌──────────────┐                    ┌──────────────┐                    ┌──────────────┐
│   Krista     │                    │  SSH         │                    │   Remote     │
│   Extension  │                    │  (JSch)      │                    │   Server     │
└──────┬───────┘                    └──────┬───────┘                    └──────┬───────┘
       │                                   │                                   │
       │── Username + Password ───────────►│                                   │
       │                                   │── SSH Handshake ─────────────────►│
       │                                   │◄── Server Key ────────────────────│
       │                                   │   (StrictHostKeyChecking=no)      │
       │                                   │── Password Auth ─────────────────►│
       │                                   │◄── Auth Success/Fail ─────────────│
       │◄── Session Established ───────────│                                   │
       │                                   │                                   │
```

### 8.2 Security Configuration

| Setting | Current Value | Notes |
|---------|---------------|-------|
| StrictHostKeyChecking | no | Accepts any host key (convenience over security) |
| Authentication | Password only | SSH keys not supported |
| Encryption | SSH protocol default | End-to-end encrypted |
| Credential Storage | Not stored | Per-request credentials |

### 8.3 Security Recommendations

1. **Network Security**: Use within trusted networks only
2. **Credential Management**: Use secure credential storage in Krista
3. **Host Verification**: Consider enabling host key checking for production
4. **Audit Logging**: Enable logging for command execution tracking

---

## 9. Dependencies

### 9.1 External Dependencies

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Dependency Graph                                    │
└─────────────────────────────────────────────────────────────────────────────┘

                        ┌─────────────────────┐
                        │   SSH Extension     │
                        │      (2.0.2)        │
                        └──────────┬──────────┘
                                   │
           ┌───────────────────────┼───────────────────────┐
           │                       │                       │
           ▼                       ▼                       ▼
┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│   JSch              │ │   Krista APIs       │ │   Jersey Media      │
│   (0.1.55)          │ │   (1.0.94)          │ │   Multipart (2.35)  │
│                     │ │                     │ │                     │
│   SSH Protocol      │ │   Platform APIs     │ │   File Handling     │
└─────────────────────┘ └─────────────────────┘ └─────────────────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │   HK2 API           │
                        │   (3.0.3)           │
                        │                     │
                        │   Dependency        │
                        │   Injection         │
                        └─────────────────────┘
```

### 9.2 Internal Dependencies

| Component | Depends On | Purpose |
|-----------|------------|---------|
| SessionArea | SSHService, EventHandler | Command execution, async events |
| SSHService | KristaMediaClient | File operations |
| KristaMediaClient | FileRepository | Krista storage integration |

---

## 10. Version History

| Version | Changes |
|---------|---------|
| 2.0.2 | Current stable release |
| 2.0.1 | KE-1571: New Media Server support |

---

*Document generated: 2025-11-27*
*Extension: SSH Extension v2.0.2*


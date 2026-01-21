# Authentication

## Overview

The SSH Extension uses password-based authentication to establish secure connections with remote SSH servers. This document explains the authentication mechanism, security considerations, and best practices.

## Authentication Mode

### Password Authentication

The SSH Extension supports **password-based authentication** only. Each catalog request requires the following credentials:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Username | Text | Yes | SSH username for the target server |
| Password | Text | Yes | SSH password for the target server |

> **📝 Note**: SSH key-based authentication is not currently supported.

## SSH Protocol Details

### Protocol Version
- **SSH Protocol**: SSH-2 (via JSch library)
- **Default Port**: 22

### Host Key Verification

The extension is configured with `StrictHostKeyChecking=no` for convenience. This means:

- ✅ Connections to new hosts are accepted automatically
- ✅ No manual host key verification required
- ⚠️ Vulnerable to man-in-the-middle attacks in untrusted networks

> **⚠️ Warning**: For production environments with strict security requirements, ensure network connectivity between Krista and target servers is secured at the network level.

## Authentication Flow

```
┌──────────────┐                    ┌──────────────┐                    ┌──────────────┐
│   Krista     │                    │  SSH         │                    │   Remote     │
│   Extension  │                    │  (JSch)      │                    │   Server     │
└──────┬───────┘                    └──────┬───────┘                    └──────┬───────┘
       │                                   │                                   │
       │── Username + Password ───────────►│                                   │
       │                                   │── SSH Handshake ─────────────────►│
       │                                   │◄── Server Key ────────────────────│
       │                                   │   (Auto-accepted)                 │
       │                                   │── Password Auth ─────────────────►│
       │                                   │◄── Auth Success/Fail ─────────────│
       │◄── Session Established ───────────│                                   │
       │                                   │                                   │
```

**Steps:**
1. User provides username and password in the catalog request
2. Extension creates SSH session with provided credentials
3. SSH handshake is performed with the remote server
4. Server's host key is automatically accepted
5. Password authentication is attempted
6. On success, session is established and command can be executed
7. On failure, an error is returned

## Session Management

### Session Lifecycle

Each catalog request creates a new SSH session:

1. **Session Creation** - New session created with provided credentials
2. **Connection** - Session connects to the remote server
3. **Command Execution** - Command is executed via exec channel
4. **Session Closure** - Session is disconnected after command completion

> **📝 Note**: Sessions are not reused between requests. Each request establishes a new connection.

### Connection Timeout

- **Default Timeout**: JVM default socket timeout
- **Custom Timeout**: Use the "Timeout In Seconds" parameter to set execution timeout

## Security Best Practices

### 1. Use Dedicated Service Accounts
Create dedicated SSH accounts for Krista with minimal required permissions.

```
# Example: Create a limited user on Linux
sudo useradd -m -s /bin/bash krista_user
sudo passwd krista_user
```

### 2. Limit Command Permissions
Restrict what commands the SSH user can execute using sudoers or rbash.

### 3. Use Strong Passwords
Ensure passwords meet your organization's security requirements:
- Minimum 12 characters
- Mix of uppercase, lowercase, numbers, and symbols
- No dictionary words

### 4. Network Security
- Use VPN or private networks between Krista and target servers
- Configure firewall rules to limit SSH access
- Consider using SSH jump hosts for additional security

### 5. Enable Audit Logging
Enable SSH logging on target servers:
```
# /etc/ssh/sshd_config
LogLevel VERBOSE
```

## Troubleshooting Authentication

### Authentication Failed

**Error Message**: `SSH command execution failed. Unable to connect to host '[hostname]' or execute command. Auth fail`

**Causes**:
- Incorrect username or password
- Account is locked or disabled
- Password has expired

**Solutions**:
1. Verify username and password are correct
2. Test credentials using a standard SSH client
3. Check if the account is locked: `passwd -S username`
4. Verify password hasn't expired: `chage -l username`

### Connection Refused

**Error Message**: `SSH command execution failed. Unable to connect to host '[hostname]' or execute command. Connection refused`

**Causes**:
- SSH service not running on target server
- Firewall blocking SSH port
- Incorrect port number

**Solutions**:
1. Verify SSH service is running: `systemctl status sshd`
2. Check firewall rules: `iptables -L -n | grep 22`
3. Verify the correct port is specified

### Host Unreachable

**Error Message**: `SSH command execution failed. Unable to connect to host '[hostname]' or execute command. UnknownHostException`

**Causes**:
- Hostname cannot be resolved
- Network connectivity issues
- Target server is down

**Solutions**:
1. Verify hostname/IP is correct
2. Test network connectivity: `ping hostname`
3. Verify DNS resolution: `nslookup hostname`

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Run SSH Command](pages/RunSSHCommand.md)
- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md)


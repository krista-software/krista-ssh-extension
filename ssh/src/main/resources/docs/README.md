# Krista SSH Extension

## Overview

The Krista SSH Extension enables secure remote command execution on SSH-enabled servers directly from Krista. It provides both synchronous and asynchronous command execution capabilities, allowing you to run shell commands on remote Linux/Unix servers and retrieve the output in text or file format.

## Key Features

- 🔐 **Secure SSH Connections** - Password-based authentication over encrypted SSH protocol
- ⚡ **Synchronous Execution** - Run commands and wait for immediate results
- 🔄 **Asynchronous Execution** - Submit long-running commands and retrieve results later
- 📄 **Flexible Output** - Get command output as text or downloadable log files
- ⏱️ **Timeout Control** - Configure execution timeouts for long-running commands
- 🔌 **Custom Port Support** - Connect to SSH servers on non-standard ports

## Quick Start Guide

1. **Add the Extension** - Navigate to Catalog → IT → CLI → Extensions → SSH and add to your workspace
2. **Configure Connection** - Provide hostname, username, and password for your SSH server
3. **Execute Commands** - Use the "Run SSH Command" request to execute shell commands
4. **Retrieve Results** - View command output directly or download as a log file

## Documentation Structure

### Getting Started
- [Extension Configuration](pages/ExtensionConfiguration.md) - How to add and configure the SSH extension
- [Authentication](pages/Authentication.md) - SSH authentication and security details

### Command Execution
- [Run SSH Command](pages/RunSSHCommand.md) - Execute commands synchronously
- [Run SSH Command Asynchronous](pages/RunSSHCommandAsynchronous.md) - Execute commands asynchronously

### Result Retrieval
- [SSH Command Asynchronous Result](pages/SSHCommandAsynchronousResult.md) - Get async command output as text
- [SSH Command Asynchronous Result As A File](pages/SSHCommandAsynchronousResultAsAFile.md) - Get async command output as a file

### Reference
- [Release Notes](pages/ReleaseNotes.md) - Version history and changes

## Support & Resources

| Property | Value |
|----------|-------|
| **Extension Version** | 2.0.2 |
| **Domain** | CLI |
| **Ecosystem** | IT |
| **SSH Protocol** | SSH-2 (via JSch) |
| **Default Port** | 22 |

## Technical Requirements

- Target SSH server must be accessible from the Krista platform
- SSH service must be running on the target server
- Valid username and password credentials required
- Network connectivity between Krista and target server

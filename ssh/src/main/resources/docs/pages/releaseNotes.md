# Release Notes

## Version History

### SSH Extension 2.0.2 (Current)

**Release Date**: November 2025

**Changes**:
- Improved error messages for better user experience
- Enhanced documentation with comprehensive guides
- Updated error handling for SSH command execution

---

### SSH Extension 2.0.1

**Release Date**: 2024

**Developer**: Avadh Dobariya

**Compatibility**:
| Component | Version |
|-----------|---------|
| Krista Service APIs Java | 1.0.115 |
| Global Catalog Version | GC.2025.11.5 |
| Appliance Release Version | 3.3.1 |

**Resolved Issues**:
- **KE-1571**: Added support for New Media Server

---

## Upgrade Notes

### Upgrading to 2.0.2

No breaking changes. Error messages have been improved for clarity:

| Previous Message | New Message |
|------------------|-------------|
| `Invalid job id - [jobId]` | `Job ID '[jobId]' not found. Please verify the Job ID returned by the 'Run SSH Command Asynchronous' request.` |
| `Exception while executing ssh command - [details]` | `SSH command execution failed. Unable to connect to host '[hostname]' or execute command. [details]` |
| `File not found - [details]` | `Unable to retrieve file. The file may not exist or is inaccessible. [details]` |
| `Unsupported file format` | `Unable to determine file type. The file '[filename]' must have a valid extension (e.g., .log, .txt).` |

---

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
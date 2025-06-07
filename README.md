# MRC-Dectector
A static analysis tool based on Soot for detecting multi-window resource conflicts.

## Getting Started
### Environment
 - Operating System: Windows 10/11
- JDK Version: Java 8 or higher
- Build Tool: Maven
 - Soot: Version 4.3.0 or higher
 - Android SDK: Required for correct resolution of Android framework classes.

   Ensure you use the android.jar file that matches the target API level of the APK under analysis.

   For example, if the APK targets Android 10 (API 29), use:
   ANDROID_SDK/platforms/android-29/android.jar 
 ### Installation
 #### 1. Clone the Repository
Clone the MRC-Detector project from GitHub:
```bash
git clone https://github.com/Huimilia/MRC.git
cd MRC-Dectector
 ```
 #### 2. Configure Maven Dependencies
Make sure Maven is installed and access
```bash
mvn clean install
```
This command will compile the project and resolve all required libraries
#### 3. Set Up Android SDK
To support analysis of Android APKs, ensure the Android SDK is properly installed on your system.
Identify the target API level of the APK (e.g., API 29 for Android 10).
Locate the corresponding android.jar file, typically located in:
```bash
<ANDROID_SDK_ROOT>/platforms/android-29/android.jar
```
### Usage
After building the project, use the following command to analyze an APK:
```bash
java -jar target/MRC-Detector.jar path/to/app.apk path/to/android.jar
```
### Project Structure
The following table describes the core Java files and their responsibilities in the MRC-Detector project:
| File                           | Purpose                                                                  |
|--------------------------------|--------------------------------------------------------------------------|
| ExclusiveResourceAnalyzer.java | Handles analysis logic for exclusively resources except NFC              |
| ManifestUtils.java             | Parses the AndroidManifest.xml to extract permissions and features.      |
| NFCResourceAnalyzer.java       | Handles analysis logic for NFC resources                                 |
| SharedResourceAnalyzer.java    | Handles analysis logic for shared resources                              |
| SootUtils.java                 | Configures and runs Soot to build call graphs and perform analysis.      |
| ResourceAnalyzer.java          | Abstract base class for all specific resource analyzers.                 |
| mrc.java                       | Main entry point of the tool. Initializes analysis and coordinates flow. |
#### Resource-Specific Analyzers
| File                           | Resource Analyzed  | Purpose                                                               |
|--------------------------------|--------------------|-----------------------------------------------------------------------|
| AudioAnalyzer.java             | Audio(exclusive)   | Defines the target paths related to exclusive audio resources.        |
| AudioAnalyzer._shared.java     | Audio(shared)      | Defines the target paths related to shared audio resources.           |
| CameraAnalyzer.java            | Camera             | Defines the target paths related to camera resource access.           |
| FingerprintAnalyzer.java       | Fingerprint        | Defines the target paths related to fingerprint hardware access.      |
| TouchscreenAnalyzer.java       | Touchscreen        | Defines the target paths related to keyboard or secure input access.  |
| MicrophoneAnalyzer.java        | Microphone         | Defines the target paths related to microphone usage.                 |
| NFCAnalyzer.java               | NFC                | Defines the target paths related to NFC resource operations.          |



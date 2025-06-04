# Don’t Mess with Bro’s Cheese! An Empirical Study of Resource Conflict in Android Multi-window
## Table of Contents
- [Abstract](#abstract)
  - [MRC](#mrc)
    - [Types of MRCs](#types-of-mrcs)
      - [MRC-Type I: Unresumed Access](#mrc-type-i-unresumed-access)
      - [MRC-Type II: Repeated Preemption](#mrc-type-ii-repeated-preemption)
      - [MRC-Type III: Shared Hijacking](#mrc-type-iii-shared-hijacking)
- [Motivating Examples](#motivating-examples)
- [MRC-Detector](#mrc-dectector)
  - [Preprocessing](#preprocessing)
  - [Call Graph Analysis](#call-graph-analysis)
  - [Control Flow Analysis](#control-flow-analysis)
- [Getting Started](#getting-started)
  - [Environment](#environment)
  - [Installation](#installation)
    - [1. Clone the Repository](#1-clone-the-repository)
    - [2. Configure Maven Dependencies](#2-configure-maven-dependencies)
    - [3. Set Up Android SDK](#3-set-up-android-sdk)
  - [Usage](#usage)
  - [Project Structure](#project-structure)
    - [Resource-Specific Analyzers](#resource-specific-analyzers)

 
# Don’t Mess with Bro’s Cheese! An Empirical Study of Resource Conflict in Android Multi-window
## Abstract
The multi-window mode in Android has greatly improved productivity and usability by allowing multiple apps to run concurrently. However, alongside the advantages, such mode also introduces unforeseen risks in both functionality and security. In this work, we present the first systematic study to identify a previously unexplored class of issues, termed Multi-window Resource Conflicts (MRCs). Such conflicts occur when multiple app windows access the same system resource concurrently, potentially leading to crashes, functionality failures or unintended behaviors. To enhance the robustness and security of Android multi-window execution, we conduct a systematic and in-depth empirical study on the MRCs. We begin with a comprehensive root cause analysis, categorizing MRCs into three fundamental types based on their triggering patterns and affected resource states. To enable large-scale detection, we develop MRC-Detector, a static analysis framework that automatically identifies MRC issues in Android apps. Our manual verification confirms its high accuracy and effectiveness. We apply the MRC-Detector to the detection of over 150k real-world apps from F-droid and Google Play, uncovering the prevalence of MRC risks. Additionally, the distribution of MRC issues is analyzed in depth across multiple dimensions, including MRC type, APK size, app source and security classification. We further investigated the recognizion and confirmation from developers and received 14 positive responses from vendors and project maintainers. Finally, comprehensive mitigation strategies are discussed. The materials of the study are available at: https://github.com/Huimilia/MRC.
## MRC
In Android multi-window mode, multiple applications may simultaneously access system resources. If the triggering event does not originate from the user or developer, the access process is referred to as unintended access triggering. Such unintended triggering may cause unexpected anomalies, including crashes, functional failures, or undefined behavior. Based on this concept, we define Multi-window Resource Conflicts (MRCs) as resource access conflicts caused by non-user-driven triggering events during multi-window execution.
### Types of MRCs    
Based on the access patterns and triggering mechanisms, we identify three distinct types of MRCs:
![image](https://github.com/Huimilia/MRC/blob/main/images/Execution%20Sequences.png)
- #### MRC-Type I: Unresumed Access  
This type occurs when a non-focused window attempts to obtain focus triggered by a non-user-driven event, which can potentially lead to unstable or undefined behavior. For instance, in multi-window mode, the system focus initially held by window w1 is preempted by w2. After w2 exits, w1 regains focus. However, since the focus reacquisition is triggered by a non-user-driven event, the resource access logic in w1 may fail to resume properly, resulting in an unresumed access conflict.
- #### MRC-Type II: Repeated Preemption  
This type occurs when a window repeatedly and unintentionally acquires focus through non-user-driven events, continuously preempting other active windows. This behavior disrupts resource access or UI interaction of other apps, degrading both usage experience and system stability.
- #### MRC-Type III: Shared Hijacking  
This type arises when a non-focused or background window accesses a shared resource without the user’s awareness or consent. For example, a non-user-driven event triggers a background window to hijack a shared resource (e.g., clipboard or sensor), leading to unintended operations, potential privacy leakage, or functionality failure.
### Motivating Examples
We use two real-world examples to intuitively illustrate the triggering process and execution effects of MRC issues.
Fig. 1 illustrates a typical MRC that occurs when the app Duxiaoman and the app TikTok alternately access the Camera resource in multi-window mode. A user first launches the Duxiaoman in full-screen mode to utilize its QR code scanning, where the camera is successfully accessed (Fig. 1(a)). Subsequently, the user opens TikTok in floating-window mode and activates its photo-taking functionality. At this stage, the TikTok runs under the multi-window mode, while the camera preview interface of Duxiaoman is suspended (Fig. 1(b)). However, after the user closes the floating window of TikTok, the camera preview of Duxiaoman fails to resume, and the interface becomes unresponsive. When the user attempts to click the flashlight button, Duxiaoman crashes and automatically restarts, displaying a bottom-aligned message indicating a runtime exception (Fig. 1(c)).
![image](https://github.com/Huimilia/MRC/blob/main/images/motivation_fig1.png)
Fig. 2 illustrates an MRC that occurs during touchscreen recording involving the app Shein and Alipay in multi-window mode. To record the product selection process in Shein, a user first activates the system’s touchscreen recording and successfully records Shein’s interface in full-screen mode (Fig. 2(a)). Subsequently, the user logs in Alipay in free-form mode to pay for products bought in Shein. Both Shein and Alipay are correctly displayed in the screen recording (Fig. 2(b)). Then, the user clicks the password input field of Alipay to complete the online payment, which triggers its secure keyboard. To protect sensitive information, Alipay deliberately obscures its interface by rendering a black screen to block the screen recording. Ideally, this black screen should only cover the area occupied by Alipay, while the remaining visible portion of Shein should remain unaffected. However, in practice, the entire screen turns black—overlapping Shein’s interface and occupying its screen space.
![image](https://github.com/Huimilia/MRC/blob/main/images/motivation_fig2.png)
## MRC-Dectector
To enable automated detection of MRC issues, we design and implement a static analysis tool named MRC-Detector, built upon the Soot framework. Given an Android APK as input, MRC-Detector automatically analyzes the code structure and call graph to identify potential MRC issues. Fig. 5 illustrates the overall workflow of MRC-Detector, which is composed of three main stages:
![image](https://github.com/Huimilia/MRC/blob/main/images/MRC-Dectector.png)
### Preprocessing
MRC-Detector first decompiles the target APK and converts the bytecode into its intermediate representation by Soot. After that, the permission declarations and <uses-feature> tags from the AndroidManifest file are extracted by a common-used app parser APKParser, where the hardware resources utilized by the app can be identified. The identified resources are then fed into our developed resource analyzer for two parts of configuration extraction based on the resource’s classification:
1. Multi-window focus-awareness callbacks (e.g., onWindowFocusChanged for focus monitoring)
2. Corresponding resource management APIs (e.g., android.media.AudioRecord.startRecording() for audio recording)
### Call Graph Analysis
The previously configured multi-window focus-aware callbacks and resource management APIs can be regarded as the source and sink of resource access, respectively. We then examine whether there exists a feasible path from the source to the sink. If no such path is found, it indicates that the corresponding resource is not properly managed under multi-window execution, thereby enabling us to determine the presence of a potential MRC.
To reduce redundant path matching, MRC-Detector adopts a bottom-up taint tracking approach in practice. First, it builds a complete Call Graph (CG) of the target app using Soot. Then starting from each sink API, a reverse traversal is performed on the CG to collect all caller methods and corresponding Activities. MRC-Detector then checks whether the traversal path can be matched to a source callback that contains the correct resource access logic. If no such match is found, the case is identified as a potential MRC issue.
Different types of MRCs are handled in a differentiated manner based on their specific triggering patterns and analysis requirements:
- For detection of MRC-Type I (Unresumed Access) and MRC-Type II (Repeated Preemption), the sink APIs are set as resource start APIs.
- For MRC-Type III (Shared Hijacking), the sink APIs are set as resource release APIs.
In particular, MRC-Type II requires additional checking to determine whether the sink APIs may be repeatedly invoked, which is implemented either inter-procedurally or intra-procedurally:
1. Inter-procedural repetition: MRC-Detector utilizes the LoopFinder tool to identify strongly connected components in the CG that include the target API as a node, thereby detecting recursive or indirect cyclic invocations.
2. Intra-procedural repetition: MRC-Detector records the ancestor methods (i.e., the callers in the invocation path) of the target API and further confirms them through Control Flow Graph (CFG) analysis in the next stage.
### Control Flow Analysis
 After obtaining feasible call paths, MRC-Detector continues to perform code logic analysis of the source callback to further verify its trigger behavior.
- For MRC-Type I & III, MRC-Detector constructs the CFG for the source callback and verifies whether the target sink API resides in the correct conditional branch:
   - hasFocus branch for MRC-Type I
   - !hasFocus branch for MRC-Type III  
   If the sink API violates the expected conditional branch, it is identified as an MRC issue.
 - For MRC-Type II, MRC-Detector builds the CFG of the ancestor method to determine whether the sink API is located inside a loop, which indicates a repeated access. If so, the case is flagged.
## Getting Started
### Environment
 - Operating System: Windows 10/11
- JDK Version: Java 8 or higher
- Build Tool: Maven
 - Soot: Version 4.3.0 or higher
 - Android SDK: Required for correct resolution of Android framework classes.
  - Ensure you use the android.jar file that matches the target API level of the APK under analysis.
   - For example, if the APK targets Android 10 (API 29), use:
    - ANDROID_SDK/platforms/android-29/android.jar 
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



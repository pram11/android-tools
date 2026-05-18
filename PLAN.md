# Project Execution Plan

This document tracks the incremental phases of the Android Armyknife Tools application.

## Mandatory Git Synchronization Rule
After every single step or sub-task completion, the responsible Agent must update the status checklist in this file, execute a git commit containing the updated `PLAN.md` along with the source files, and push the changes to the remote repository before moving to the next task.

---

## Phase 1: Planning and Initialization
*   [x] Step 1.1: Define core feature scope and hardware sensor constraints.
*   [x] Step 1.2: Establish the hierarchical menu structure and adaptive navigation model.
*   [x] Step 1.3: Generate repository structure and initialize the base Android project.

## Phase 2: Architecture Setup
*   [x] Step 2.1: Implement the multi-module build configuration or structured package layout.
*   [x] Step 2.2: Define the global data contract (`ToolCategory`, `ToolItem`) and the central Tool Registry.
*   [x] Step 2.3: Implement the Material 3 Adaptive Navigation base layout (Bottom Navigation / Navigation Rail).
*   [x] Step 2.4: Build the dynamic Dashboard (Home) Grid Screen that automatically populates from the Tool Registry.
*   [x] Step 2.5: Implement the Search and Favorites global screens filtering the Registry.

## Phase 3: Core Feature Modules Implementation

### Category 1: Sensor Tools
*   [x] Step 3.1.1: Compass (Geomagnetic Field Sensor integration).
*   [x] Step 3.1.2: Bubble Level (Accelerometer integration).
*   [x] Step 3.1.3: Sound Meter (Microphone audio amplitude tracking).
*   [x] Step 3.1.4: Lux Meter (Light Sensor integration).
*   [x] Step 3.1.5: Metal Detector (Magnetic Field intensity processing).
*   [ ] Step 3.1.6: Speedometer (Location GPS provider integration).

### Category 2: Hardware Control
*   [ ] Step 3.2.1: QR/Barcode Scanner & Generator (CameraX & local rendering).
*   [ ] Step 3.2.2: Magnifier (CameraX zoom control integration).
*   [ ] Step 3.2.3: Flashlight & SOS (CameraManager torch control).
*   [ ] Step 3.2.4: Mirror (Front camera rendering).

### Category 3: Media & Files
*   [ ] Step 3.3.1: APK Extractor (PackageManager integration).
*   [ ] Step 3.3.2: Image Converter & Compressor (Bitmap factory processing).
*   [ ] Step 3.3.3: PDF Utility (PdfDocument rendering engine).
*   [ ] Step 3.3.4: Voice Recorder (MediaRecorder local storage management).

### Category 4: Data & Utilities
*   [ ] Step 3.4.1: Unit Converter (Pure function translation layer).
*   [ ] Step 3.4.2: Financial & Lifestyle Calculators (BMI, Age, Discount logic).
*   [ ] Step 3.4.3: Text Crypto Tool (Local Base64, Hex, AES implementations).
*   [ ] Step 3.4.4: Morse Code Converter (Text parsing mapped to flash/vibration outputs).
*   [ ] Step 3.4.5: Random Generator (Kotlin Random utilities).

## Phase 4: Quality Assurance and Polishing
*   [ ] Step 4.1: Dynamic runtime permission checking and graceful degradation flows.
*   [ ] Step 4.2: UI/UX validation across folding devices, tablets, and standard smartphones.
*   [ ] Step 4.3: Memory leak profiling and optimization for long-running sensor listeners.

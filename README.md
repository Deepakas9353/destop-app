# Elite Print Utility - Setup and Packaging Guide

## Features
- **Printers**: Auto-detects all connected system printers.
- **Silent Printing**: Directly sends jobs to the printer without the system dialog.
- **PDF Support**: Advanced PDF rendering using Apache PDFBox.
- **Customization**: Supports 1-50 copies, layout selection, and duplex options.

## Technology Stack
- **Java 17+**
- **JavaFX 17**
- **Apache PDFBox** (for silent PDF printing)
- **Maven** (Project management)

## How to Build and Run
1. Ensure you have **Java 17** or higher and **Maven** installed.
2. Open a terminal in the project root.
3. To run the app:
   ```bash
   mvn javafx:run
   ```

## Creating the EXE Installer (jpackage)
To generate a Windows EXE installer, follow these steps:

### 1. Build the Shadow JAR
First, package the application into a JAR:
```bash
mvn clean package
```

### 2. Run jpackage
Use the following command to generate the installer (replace paths if necessary):
```bash
jpackage --name "ElitePrint" \
         --input target/ \
         --main-jar desktop-printing-app-1.0-SNAPSHOT.jar \
         --main-class com.printapp.Launcher \
         --type exe \
         --win-shortcut \
         --win-menu \
         --vendor "EliteAppCorp"
```
*Note: You must have WiX Toolset installed on your Windows machine for jpackage to generate an EXE/MSI.*

## Project Structure
- `src/main/java/com/printapp/App.java`: Main JavaFX UI.
- `src/main/java/com/printapp/service/PrinterService.java`: Logic for printer detection and PDF handling.
- `src/main/java/com/printapp/model/PrintConfig.java`: Data model for print settings.
- `pom.xml`: Dependency and build configuration.

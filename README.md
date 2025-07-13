# Ceph Analysis Tool 🦷

A JavaFX-based desktop tool designed for manual cephalometric analysis. It enables users to annotate over 50 anatomical landmarks on cephalometric radiographs, perform line and angle measurements, and store data in JSON format for further evaluation.

-------

## 🛠 Features

- Annotate 50+ cephalometric points manually  
- Draw free-hand lines and measure angles  
- Store measurement data in JSON format  
- Easy image upload and intuitive JavaFX interface  
- Enhances diagnostic accuracy and clinical workflow efficiency

--------

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher  
- JavaFX SDK  
- VS Code / IntelliJ IDEA  
- JSON-simple (or any JSON library)

### Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/Khushmeet13/ceph-analysis-tool.git
   ```

2. Open the project in your preferred IDE (VS Code or IntelliJ)

3. Make sure JavaFX libraries are properly added to the project

4. Build and run `App.java` to launch the application

-------

## 📂 Folder Structure

```
cepha-tool/
├── src/        # Java source files
├── lib/        # External libraries (JavaFX, JSON)
├── bin/        # Compiled class files
└── images/     # Cephalometric image samples
```

------

## 📄 Sample JSON Output

```json
{
  "points": [
    { "name": "Nasion", "x": 120, "y": 200 },
    { "name": "Sella", "x": 110, "y": 210 }
  ],
  "angles": [
    { "name": "SNA", "value": 82.5 }
  ]
}
```

-----

## 📸 Screenshots 
Here are some UI previews of the Cephalometric Analysis Tool in action.

images/annotations.png


------

## 👨‍💻 Technologies Used

- Java 11+  
- JavaFX (UI components)  
- JSON (data storage and retrieval)  
- VS Code (or IntelliJ for development)


-------
Made with using Java, JavaFX, and JSON to simplify orthodontic assessments.


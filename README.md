# FlashFetch

FlashFetch is a cross-platform, command-line utility written in Java that simplifies downloading operating system ISO images. It is designed as a companion tool to **FlashBurn**, providing a streamlined workflow from OS discovery to bootable USB creation.

The project emphasizes clarity, modularity, and safety while demonstrating real-world networking, threading, and system-integration concepts.

---

## Features

- Multi-OS support  
  - Windows  
  - Linux distributions (categorized by use case)  
  - macOS  
  - BSD systems  
  - Other operating systems  

- Concurrent downloads with progress tracking  
- Resume support for interrupted downloads (HTTP Range requests)  
- Download manager with pause, resume, and cancel capabilities  
- Persistent, configurable settings  
- Linux-only integration with FlashBurn for USB flashing  

---

## FlashBurn Integration

FlashFetch integrates with **FlashBurn**, a C++ USB flashing utility. When invoked, FlashFetch will automatically download, compile, and execute FlashBurn if it is not already present.

FlashBurn repository:  
https://github.com/neilkumaran/flashburn

---

## Requirements

- Java 11 or newer  
- Internet connection  
- Linux (required only for FlashBurn integration)  
- `g++` installed (for compiling FlashBurn)  

---

## Building and Running

Compile:
```bash
javac *.java
java Main

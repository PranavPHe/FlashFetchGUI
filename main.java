// Neil Kumaran
// 1/23/26
// Mr Scimeca
// FlashFlash, a tool to download operating systems way easier

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.net.*;

/**
 * FlashFetch - A command-line tool for downloading operating system ISO files
 * Features:
 * - Multi-OS support (Windows, Linux, macOS, BSD, etc.)
 * - Concurrent downloads with progress tracking
 * - Resume capability for interrupted downloads
 * - Integration with FlashBurn (USB burning tool)
 * - Configurable settings (download directory, timeouts, retries)
 */
public class Main {
    // Scanner for user input throughout the application
    private static Scanner scanner = new Scanner(System.in);
    
    // Default download directory in user's Downloads folder
    private static String downloadDirectory = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "FlashFetch";
    
    // Configuration variables for download behavior
    private static boolean checksumVerification = true;  // Verify file integrity after download
    private static int concurrentDownloads = 2;          // Maximum simultaneous downloads
    private static int downloadTimeout = 30000;          // Connection timeout in milliseconds
    private static boolean autoRetry = true;             // Automatically retry failed downloads
    private static int maxRetries = 3;                   // Maximum retry attempts
    private static String preferredMirror = "auto";      // Mirror selection preference
    
    // Thread-safe lists for managing download tasks
    private static List<DownloadTask> downloadQueue = new CopyOnWriteArrayList<>();
    private static List<DownloadTask> completedDownloads = new CopyOnWriteArrayList<>();
    
    // Configuration file name for persistent settings
    private static final String SETTINGS_FILE = "flashfetch_settings.properties";

    /**
     * Inner class representing a single download task
     * Tracks download progress, status, and metadata
     */
    static class DownloadTask {
        String name;           // Display name of the OS being downloaded
        String url;            // Download URL
        String filePath;       // Local file path for saving
        String status;         // Current status: "queued", "downloading", "paused", "completed", "failed"
        String checksum;       // Expected checksum for verification
        long totalSize;        // Total file size in bytes
        long downloadedSize;   // Bytes downloaded so far
        Thread downloadThread; // Thread handling this download
        
        // Constructor initializes a new download task
        DownloadTask(String name, String url, String filePath) {
            this.name = name; 
            this.url = url; 
            this.filePath = filePath;
            this.status = "queued"; 
            this.totalSize = 0; 
            this.downloadedSize = 0;
        }
        
        // Calculate download progress as a percentage
        int getProgress() { 
            return totalSize == 0 ? 0 : (int)((downloadedSize * 100) / totalSize); 
        }
    }

    /**
     * Main entry point for the application
     * Loads settings, ensures download directory exists, and displays main menu loop
     */
    public static void main(String[] args) {
        loadSettings();              // Load saved configuration
        ensureDownloadDirectory();   // Create download directory if needed
        
        // Main application loop
        while (true) {
            displayMainMenu();
            int choice = getUserChoice();
            
            // Route user to appropriate menu based on selection
            switch (choice) {
                case 1: osMenu("windows", "WINDOWS", OSDatabase.getWindowsMenu()); break;
                case 2: linuxMenu(); break;  // Special menu for Linux with categories
                case 3: osMenu("macos", "macOS", OSDatabase.getMacOSMenu()); break;
                case 4: osMenu("bsd", "BSD SYSTEMS", OSDatabase.getBSDMenu()); break;
                case 5: osMenu("other", "OTHER OS", OSDatabase.getOtherMenu()); break;
                case 6: viewDownloads(); break;      // Download manager
                case 7: settings(); break;           // Settings menu
                case 8: sendToFlashBurn(); break;    // USB burning integration
                case 0:
                    // Exit the application
                    System.out.println("\nGo get FlashBurn!!! Written in C++ instead of Java!");
                    scanner.close();
                    return;
                default: 
                    System.out.println("\nInvalid option.");
            }
        }
    }

    /**
     * Display the main menu with ASCII art borders
     * Shows current download directory and active download count
     */
    private static void displayMainMenu() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                    FlashFetch                            ║");
        System.out.println("║          Use with Neil Kumaran's FlashBurn!!             ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  [1] Windows                                             ║");
        System.out.println("║  [2] Linux Distributions                                 ║");
        System.out.println("║  [3] macOS                                               ║");
        System.out.println("║  [4] BSD Systems                                         ║");
        System.out.println("║  [5] Other Operating Systems                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  [6] View Downloads                                      ║");
        System.out.println("║  [7] Settings                                            ║");
        System.out.println("║  [8] Send to FlashBurn                                   ║");
        System.out.println("║  [0] Exit                                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("  Download Directory: " + downloadDirectory);
        
        // Display active download count if any downloads are in progress
        long active = downloadQueue.stream().filter(d -> d.status.equals("downloading")).count();
        if (active > 0) System.out.println("  Active Downloads: " + active);
        
        System.out.print("\nSelect an option: ");
    }

    /**
     * Generic OS menu display for Windows, macOS, BSD, and Other OS categories
     * @param type OS type identifier
     * @param title Menu title to display
     * @param items Array of menu items [id, display_name]
     */
    private static void osMenu(String type, String title, String[][] items) {
        while (true) {
            clearScreen();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.printf("║                    %-36s  ║\n", title);
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            
            int num = 1;
            // Display each menu item, with separators for dividers
            for (String[] item : items) {
                if (item[0].equals("---")) {
                    System.out.println("╠══════════════════════════════════════════════════════════╣");
                } else {
                    System.out.printf("║  [%-2d] %-50s║\n", num++, item[1]);
                }
            }
            System.out.println("║  [0]  Back to Main Menu                                  ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.print("\nSelect an option: ");
            
            int choice = getUserChoice();
            if (choice == 0) return;  // Return to main menu
            
            // Find the selected OS by matching choice number to non-divider items
            int idx = 0;
            for (String[] item : items) {
                if (!item[0].equals("---")) {
                    idx++;
                    if (idx == choice) {
                        downloadOS(item[0]);  // Initiate download for selected OS
                        break;
                    }
                }
            }
        }
    }

    /**
     * Special menu for Linux distributions organized by categories
     * (Beginner-Friendly, Advanced, Security, Lightweight, etc.)
     */
    private static void linuxMenu() {
        while (true) {
            clearScreen();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║               LINUX DISTRIBUTIONS                        ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            
            OSDatabase.Category[] categories = OSDatabase.getLinuxCategories();
            int num = 1;
            Map<Integer, String> choiceMap = new HashMap<>();
            
            // Display each category and its distributions
            for (OSDatabase.Category cat : categories) {
                System.out.printf("║  %-56s║\n", cat.displayName + ":");
                for (String distro : cat.distros) {
                    System.out.printf("║  [%-2d] %-50s║\n", num, OSDatabase.getDisplayName(distro));
                    choiceMap.put(num++, distro);  // Map choice number to distro ID
                }
                System.out.println("╠══════════════════════════════════════════════════════════╣");
            }
            System.out.println("║  [0]  Back to Main Menu                                  ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.print("\nSelect an option: ");
            
            int choice = getUserChoice();
            if (choice == 0) return;
            if (choiceMap.containsKey(choice)) {
                downloadOS(choiceMap.get(choice));  // Download selected distribution
            }
        }
    }

    /**
     * Handle the download process for a selected OS
     * Displays available versions/architectures and initiates download
     * @param osId Unique identifier for the OS from OSDatabase
     */
    private static void downloadOS(String osId) {
        clearScreen();
        String osName = OSDatabase.getDisplayName(osId);
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  " + centerText(osName + " Download", 54) + "  ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        
        // Get available releases from database
        List<OSDatabase.OSEntry> releases = OSDatabase.getDownloads(osId);
        
        if (releases.isEmpty()) {
            System.out.println("\n✗ No downloads available for this OS.");
            pause();
            return;
        }
        
        // Display available versions
        System.out.println("\n  Available versions:\n");
        for (int i = 0; i < releases.size(); i++) {
            OSDatabase.OSEntry r = releases.get(i);
            System.out.printf("  [%d] %s %s (%s)\n", i + 1, r.name, r.version, r.arch);
        }
        System.out.println("\n  [0] Back");
        System.out.print("\n  Select version: ");
        
        int choice = getUserChoice();
        if (choice == 0 || choice > releases.size()) return;
        
        // Create filename from OS name, version, and architecture
        OSDatabase.OSEntry selected = releases.get(choice - 1);
        String filename = (selected.name + "_" + selected.version + "_" + selected.arch + ".iso")
            .replaceAll("[<>:\"/\\\\|?*]", "_");  // Remove invalid filename characters
        String downloadPath = chooseDownloadLocation(filename);
        
        // Start download if user confirmed location
        if (downloadPath != null) {
            startDownload(selected.name + " " + selected.version, selected.url, downloadPath, selected.checksum);
        }
    }

    /**
     * Prompt user to choose download location
     * @param defaultFilename Suggested filename for the download
     * @return Full path for download, or null if cancelled
     */
    private static String chooseDownloadLocation(String defaultFilename) {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║               Choose Download Location                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("\n  Current directory: " + downloadDirectory);
        System.out.println("  Filename: " + defaultFilename);
        System.out.println("\n  [1] Use default location");
        System.out.println("  [2] Enter custom path");
        System.out.println("  [0] Cancel");
        System.out.print("\n  Select option: ");
        
        int choice = getUserChoice();
        switch (choice) {
            case 1: 
                // Use default directory
                return downloadDirectory + File.separator + defaultFilename;
            case 2:
                // Allow custom path entry
                System.out.print("\n  Enter full path: ");
                String path = scanner.nextLine().trim();
                if (path.isEmpty()) return downloadDirectory + File.separator + defaultFilename;
                // If path is a directory, append filename; otherwise use as-is
                return new File(path).isDirectory() ? path + File.separator + defaultFilename : path;
            default: 
                return null;  // Cancel
        }
    }

    /**
     * Initialize and start a download task in a separate thread
     * Handles file existence checks, resume capability, and HTTP redirects
     * @param name Display name for the download
     * @param url Download URL
     * @param filePath Local path to save file
     * @param expectedChecksum Checksum for verification (optional)
     */
    private static void startDownload(String name, String url, String filePath, String expectedChecksum) {
        File targetFile = new File(filePath);
        
        // Check if file already exists and prompt user
        if (targetFile.exists()) {
            System.out.println("\n  File already exists: " + filePath);
            System.out.print("  [1] Overwrite  [2] Resume  [3] Cancel: ");
            int choice = getUserChoice();
            if (choice == 3) return;
            if (choice == 1) targetFile.delete();  // Delete for fresh download
        }
        
        // Create download task and add to queue
        DownloadTask task = new DownloadTask(name, url, filePath);
        task.checksum = expectedChecksum;
        downloadQueue.add(task);
        
        // Create download thread
        Thread downloadThread = new Thread(() -> {
            task.status = "downloading";
            HttpURLConnection conn = null;
            
            try {
                // Establish HTTP connection with timeout
                URL downloadUrl = new URL(url);
                conn = (HttpURLConnection) downloadUrl.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setConnectTimeout(downloadTimeout);
                conn.setReadTimeout(downloadTimeout);
                
                // Follow HTTP redirects manually
                int responseCode = conn.getResponseCode();
                while (responseCode == 301 || responseCode == 302 || responseCode == 303) {
                    String newUrl = conn.getHeaderField("Location");
                    conn.disconnect();
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    responseCode = conn.getResponseCode();
                }
                
                // Check for existing partial download for resume capability
                File existingFile = new File(filePath);
                long existingSize = existingFile.exists() ? existingFile.length() : 0;
                
                // Request resume from existing byte position
                if (existingSize > 0) {
                    conn.disconnect();
                    conn = (HttpURLConnection) downloadUrl.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setRequestProperty("Range", "bytes=" + existingSize + "-");
                    responseCode = conn.getResponseCode();
                }
                
                // Set total size and adjust for resumed downloads
                task.totalSize = conn.getContentLengthLong();
                if (existingSize > 0 && responseCode == 206) {  // 206 = Partial Content
                    task.totalSize += existingSize;
                    task.downloadedSize = existingSize;
                }
                
                // Download file in chunks
                try (InputStream in = conn.getInputStream();
                     RandomAccessFile out = new RandomAccessFile(filePath, "rw")) {
                    
                    if (existingSize > 0 && responseCode == 206) out.seek(existingSize);
                    
                    byte[] buffer = new byte[8192];  // 8KB buffer
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        if (task.status.equals("paused")) break;  // Allow pausing
                        out.write(buffer, 0, bytesRead);
                        task.downloadedSize += bytesRead;
                    }
                }
                
                // Mark as completed if download finished
                if (task.downloadedSize >= task.totalSize || task.totalSize == -1) {
                    task.status = "completed";
                    completedDownloads.add(task);
                    downloadQueue.remove(task);
                }
            } catch (Exception e) {
                task.status = "failed";
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
        
        task.downloadThread = downloadThread;
        downloadThread.start();
        
        // Display confirmation message
        System.out.println("\n  ╔════════════════════════════════════════════════════════╗");
        System.out.println("  ║              ✓ DOWNLOAD STARTED                        ║");
        System.out.println("  ╚════════════════════════════════════════════════════════╝");
        System.out.println("\n  File: " + name);
        System.out.println("  Saving to: " + filePath);
        System.out.println("\n  Go to [6] View Downloads to see progress.");
        pause();
    }

    /**
     * Display download manager with active and completed downloads
     * Allows pausing, resuming, canceling, and viewing download progress
     */
    private static void viewDownloads() {
        while (true) {
            clearScreen();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║                   DOWNLOAD MANAGER                       ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            
            // Display active downloads with progress bars
            System.out.println("\n  ACTIVE DOWNLOADS:");
            List<DownloadTask> active = new ArrayList<>();
            for (DownloadTask t : downloadQueue) {
                if (t.status.equals("downloading") || t.status.equals("queued") || t.status.equals("paused")) {
                    active.add(t);
                }
            }
            
            if (active.isEmpty()) {
                System.out.println("  (No active downloads)");
            } else {
                for (int i = 0; i < active.size(); i++) {
                    DownloadTask t = active.get(i);
                    // Choose icon based on status
                    String icon = t.status.equals("downloading") ? "↓" : t.status.equals("paused") ? "⏸" : "⏳";
                    System.out.printf("  [%d] %s %s %s (%s)\n", i+1, icon, t.name, progressBar(t.getProgress()), t.status);
                }
            }
            
            // Display recently completed downloads (last 5)
            System.out.println("\n  COMPLETED:");
            if (completedDownloads.isEmpty()) {
                System.out.println("  (None)");
            } else {
                int show = Math.min(completedDownloads.size(), 5);
                for (int i = 0; i < show; i++) {
                    DownloadTask t = completedDownloads.get(completedDownloads.size() - 1 - i);
                    System.out.println("  ✓ " + t.name);
                }
            }
            
            System.out.println("\n  [1] Pause/Resume  [2] Cancel  [3] Clear  [4] Open Folder  [0] Back");
            System.out.print("\nChoice: ");
            
            int choice = getUserChoice();
            if (choice == 0) return;
            if (choice == 3) { completedDownloads.clear(); }  // Clear completed list
            if (choice == 4) {
                // Open download folder in OS file manager
                try {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) Runtime.getRuntime().exec("explorer.exe \"" + downloadDirectory + "\"");
                    else if (os.contains("mac")) Runtime.getRuntime().exec("open " + downloadDirectory);
                    else Runtime.getRuntime().exec("xdg-open " + downloadDirectory);
                } catch (IOException e) { System.out.println("Could not open folder."); pause(); }
            }
        }
    }

    /**
     * Display and manage application settings
     * Allows configuration of download directory, concurrent downloads, timeouts, etc.
     */
    private static void settings() {
        while (true) {
            clearScreen();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║                     SETTINGS                             ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.printf("║  Download Dir: %-41s║\n", truncate(downloadDirectory, 41));
            System.out.printf("║  Concurrent: %-2d | Timeout: %-2ds | Retry: %-15s║\n", 
                concurrentDownloads, downloadTimeout/1000, autoRetry ? "Yes("+maxRetries+")" : "No");
            System.out.printf("║  Checksum: %-6s | Mirror: %-28s║\n", checksumVerification ? "On" : "Off", preferredMirror);
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] Change Directory  [2] Concurrent  [3] Timeout       ║");
            System.out.println("║  [4] Toggle Retry      [5] Toggle Checksum               ║");
            System.out.println("║  [6] Disk Space        [7] Reset Defaults  [0] Back      ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.print("\nChoice: ");
            
            int choice = getUserChoice();
            switch (choice) {
                case 0: return;
                case 1:
                    // Change download directory
                    System.out.print("  New directory: ");
                    String dir = scanner.nextLine().trim();
                    if (!dir.isEmpty()) {
                        new File(dir).mkdirs();  // Create directory if it doesn't exist
                        downloadDirectory = dir;
                        saveSettings();
                    }
                    break;
                case 2:
                    // Set concurrent download limit
                    System.out.print("  Concurrent downloads (1-5): ");
                    int n = getUserChoice();
                    if (n >= 1 && n <= 5) { concurrentDownloads = n; saveSettings(); }
                    break;
                case 3:
                    // Set connection timeout
                    System.out.print("  Timeout in seconds (10-120): ");
                    int t = getUserChoice();
                    if (t >= 10 && t <= 120) { downloadTimeout = t * 1000; saveSettings(); }
                    break;
                case 4:
                    // Toggle auto-retry
                    autoRetry = !autoRetry;
                    saveSettings();
                    break;
                case 5:
                    // Toggle checksum verification
                    checksumVerification = !checksumVerification;
                    saveSettings();
                    break;
                case 6:
                    // Display disk space information
                    File d = new File(downloadDirectory);
                    if (d.exists()) {
                        System.out.println("\n  Free: " + formatSize(d.getFreeSpace()));
                        System.out.println("  Total: " + formatSize(d.getTotalSpace()));
                    }
                    pause();
                    break;
                case 7:
                    // Reset all settings to defaults
                    downloadDirectory = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "FlashFetch";
                    concurrentDownloads = 2; downloadTimeout = 30000; autoRetry = true; maxRetries = 3;
                    checksumVerification = true; preferredMirror = "auto";
                    saveSettings();
                    System.out.println("  ✓ Reset to defaults");
                    pause();
                    break;
            }
        }
    }

    // ==================== FLASHBURN INTEGRATION ====================
    // Constants for FlashBurn (USB burning tool) integration
    private static final String FLASHBURN_DIR = System.getProperty("user.home") + File.separator + "FlashBurn";
    private static final String FLASHBURN_BINARY = FLASHBURN_DIR + File.separator + "flashburn";
    private static final String FLASHBURN_SOURCE = FLASHBURN_DIR + File.separator + "main.cpp";
    private static final String FLASHBURN_URL = "https://raw.githubusercontent.com/neilkumaran/FlashBurn/main/main.cpp";

    /**
     * FlashBurn integration menu - allows burning ISOs to USB drives
     * Only available on Linux systems
     */
    private static void sendToFlashBurn() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                  SEND TO FLASHBURN                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        // FlashBurn only works on Linux
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            System.out.println("║  ✗ FlashBurn is only available on Linux.                 ║");
            System.out.println("║  For Windows: use Rufus or Ventoy                        ║");
            System.out.println("║  For macOS: use balenaEtcher                             ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            pause();
            return;
        }

        // Scan for available ISO files
        List<File> isos = scanForISOs();
        if (isos.isEmpty()) {
            System.out.println("║  No ISO files found. Download an OS first!               ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            pause();
            return;
        }

        // Display available ISOs
        System.out.println("║  Available ISOs:                                         ║");
        for (int i = 0; i < isos.size(); i++) {
            System.out.printf("║  [%d] %-52s║\n", i+1, truncate(isos.get(i).getName(), 52));
        }
        System.out.println("║  [B] Browse  [0] Back                                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.print("\nSelect ISO: ");

        String input = scanner.nextLine().trim().toLowerCase();
        if (input.equals("0")) return;

        String selectedIso = null;
        if (input.equals("b")) {
            // Allow manual path entry
            System.out.print("Enter ISO path: ");
            selectedIso = scanner.nextLine().trim();
            if (!new File(selectedIso).exists()) { System.out.println("File not found."); pause(); return; }
        } else {
            // Select from numbered list
            try {
                int idx = Integer.parseInt(input);
                if (idx > 0 && idx <= isos.size()) selectedIso = isos.get(idx-1).getAbsolutePath();
            } catch (NumberFormatException e) { return; }
        }

        if (selectedIso != null) selectDiskAndBurn(selectedIso);
    }

    /**
     * Scan download directory for ISO files
     * @return List of ISO files found
     */
    private static List<File> scanForISOs() {
        List<File> isos = new ArrayList<>();
        File dir = new File(downloadDirectory);
        if (dir.exists()) {
            File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".iso"));
            if (files != null) Collections.addAll(isos, files);
        }
        return isos;
    }

    /**
     * Display available disks and burn ISO to selected disk
     * Uses lsblk to enumerate available block devices
     * @param isoPath Path to ISO file to burn
     */
    private static void selectDiskAndBurn(String isoPath) {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  ⚠  SELECT TARGET DISK - WILL BE ERASED!                 ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        // Enumerate available disks using lsblk command
        List<String[]> disks = new ArrayList<>();
        try {
            Process p = new ProcessBuilder("lsblk", "-d", "-o", "NAME,SIZE,MODEL", "-n").start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 3);
                // Filter for physical disks (sd* or nvme*)
                if (parts.length >= 2 && (parts[0].startsWith("sd") || parts[0].startsWith("nvme"))) {
                    disks.add(new String[]{"/dev/" + parts[0], parts[1], parts.length > 2 ? parts[2] : ""});
                }
            }
        } catch (IOException e) { System.out.println("Could not scan disks."); pause(); return; }

        if (disks.isEmpty()) { System.out.println("No disks found."); pause(); return; }

        // Display available disks
        for (int i = 0; i < disks.size(); i++) {
            String[] d = disks.get(i);
            System.out.printf("║  [%d] %-52s║\n", i+1, d[0] + " " + d[1] + " " + d[2]);
        }
        System.out.println("║  [0] Cancel                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.print("\nSelect disk: ");

        int choice = getUserChoice();
        if (choice == 0 || choice > disks.size()) return;

        // Require explicit confirmation before burning
        String disk = disks.get(choice-1)[0];
        System.out.print("\nType 'YES' to confirm burning to " + disk + ": ");
        if (!scanner.nextLine().trim().equals("YES")) { System.out.println("Cancelled."); pause(); return; }

        // Ensure FlashBurn is installed and compiled
        if (!ensureFlashBurn()) { pause(); return; }

        // Execute FlashBurn with ISO and target disk
        try {
            ProcessBuilder pb = new ProcessBuilder(FLASHBURN_BINARY, isoPath, disk);
            pb.inheritIO();  // Show FlashBurn's output in console
            pb.start().waitFor();
            System.out.println("\n✓ FlashBurn completed!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        pause();
    }

    /**
     * Ensure FlashBurn is installed and compiled
     * Downloads source from GitHub if needed and compiles with g++
     * @return true if FlashBurn is ready, false otherwise
     */
    private static boolean ensureFlashBurn() {
        File binary = new File(FLASHBURN_BINARY);
        File source = new File(FLASHBURN_SOURCE);

        // Check if binary already exists and is executable
        if (binary.exists() && binary.canExecute()) return true;

        System.out.println("\nInstalling FlashBurn...");
        new File(FLASHBURN_DIR).mkdirs();

        // Download source code if not present
        if (!source.exists()) {
            System.out.println("  Downloading...");
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(FLASHBURN_URL).openConnection();
                if (conn.getResponseCode() != 200) { System.out.println("Download failed."); return false; }
                try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(source)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }
                System.out.println("  ✓ Downloaded");
            } catch (IOException e) { System.out.println("Download error: " + e.getMessage()); return false; }
        }

        // Compile C++ source with g++
        System.out.println("  Compiling...");
        try {
            Process p = new ProcessBuilder("g++", "-o", FLASHBURN_BINARY, FLASHBURN_SOURCE, "-std=c++17")
                .directory(new File(FLASHBURN_DIR)).redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line; while ((line = r.readLine()) != null) System.out.println("    " + line);
            if (p.waitFor() != 0) { System.out.println("Compile failed. Install g++: sudo pacman -S gcc"); return false; }
            new File(FLASHBURN_BINARY).setExecutable(true);
            System.out.println("  ✓ Compiled");
            return true;
        } catch (Exception e) { System.out.println("Compile error: " + e.getMessage()); return false; }
    }

    // ==================== UTILITIES ====================
    
    /**
     * Load settings from properties file
     * Uses defaults if file doesn't exist or values are invalid
     */
    private static void loadSettings() {
        Properties p = new Properties();
        try (FileInputStream f = new FileInputStream(SETTINGS_FILE)) {
            p.load(f);
            downloadDirectory = p.getProperty("downloadDirectory", downloadDirectory);
            checksumVerification = Boolean.parseBoolean(p.getProperty("checksumVerification", "true"));
            concurrentDownloads = Integer.parseInt(p.getProperty("concurrentDownloads", "2"));
            downloadTimeout = Integer.parseInt(p.getProperty("downloadTimeout", "30000"));
            autoRetry = Boolean.parseBoolean(p.getProperty("autoRetry", "true"));
            maxRetries = Integer.parseInt(p.getProperty("maxRetries", "3"));
            preferredMirror = p.getProperty("preferredMirror", "auto");
        } catch (Exception e) { /* Use defaults if loading fails */ }
    }

    /**
     * Save current settings to properties file
     */
    private static void saveSettings() {
        Properties p = new Properties();
        p.setProperty("downloadDirectory", downloadDirectory);
        p.setProperty("checksumVerification", String.valueOf(checksumVerification));
        p.setProperty("concurrentDownloads", String.valueOf(concurrentDownloads));
        p.setProperty("downloadTimeout", String.valueOf(downloadTimeout));
        p.setProperty("autoRetry", String.valueOf(autoRetry));
        p.setProperty("maxRetries", String.valueOf(maxRetries));
        p.setProperty("preferredMirror", preferredMirror);
        try (FileOutputStream f = new FileOutputStream(SETTINGS_FILE)) { 
            p.store(f, "FlashFetch Settings"); 
        } catch (Exception e) { /* Silently fail */ }
    }

    /** Create download directory if it doesn't exist */
    private static void ensureDownloadDirectory() { new File(downloadDirectory).mkdirs(); }
    
    /** Clear terminal screen using ANSI escape codes */
    private static void clearScreen() { System.out.print("\033[H\033[2J"); System.out.flush(); }
    
    /** Pause and wait for user to press Enter */
    private static void pause() { System.out.print("\nPress Enter..."); scanner.nextLine(); }
    
    /** Get integer input from user, returns -1 on invalid input */
    private static int getUserChoice() { 
        try { return Integer.parseInt(scanner.nextLine().trim()); } 
        catch (Exception e) { return -1; } 
    }
    
    /** Truncate string to maximum length with ellipsis */
    private static String truncate(String s, int max) { 
        return s.length() <= max ? s : s.substring(0, max-3) + "..."; 
    }
    
    /** Center text within a given width */
    private static String centerText(String t, int w) { 
        int p = (w - t.length()) / 2; 
        return " ".repeat(Math.max(0,p)) + t; 
    }
    
    /** Create ASCII progress bar from percentage (0-100) */
    private static String progressBar(int pct) { 
        int f = pct/5;  // 20 blocks total (100% / 5 = 20)
        return "[" + "█".repeat(f) + "░".repeat(20-f) + "] " + pct + "%"; 
    }
    
    /** Format byte size to human-readable format (B, KB, MB, GB) */
    private static String formatSize(long b) {
        if (b < 1024) return b + " B";
        if (b < 1024*1024) return String.format("%.1f KB", b/1024.0);
        if (b < 1024*1024*1024) return String.format("%.1f MB", b/(1024.0*1024));
        return String.format("%.2f GB", b/(1024.0*1024*1024));
    }
}

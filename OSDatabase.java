// Neil Kumaran
// 1/23/26
// Mr Scimeca
// FlashFetch's OS Database which it pulls from

import java.util.*;

/**
 * OSDatabase - Comprehensive database of operating system download links
 * 
 * This class maintains a centralized repository of OS download information including:
 * - Display names for all operating systems
 * - Download URLs for various versions and architectures
 * - Menu structures for organizing OS categories
 * 
 * Supports multiple OS families:
 * - Windows (consumer, LTSC, Server editions)
 * - Linux (Debian, Arch, RPM, Security, Lightweight distributions)
 * - BSD variants (FreeBSD, OpenBSD, NetBSD, etc.)
 * - macOS versions
 * - Alternative/Hobby OSes (Haiku, ReactOS, TempleOS, etc.)
 * - Recovery and utility tools
 */
public class OSDatabase {
    
    /**
     * OSEntry - Represents a single downloadable OS version
     * Contains all metadata needed to download and identify an OS release
     */
    public static class OSEntry {
        public String name;      // Operating system name
        public String version;   // Version number or codename
        public String url;       // Direct download URL
        public String arch;      // Architecture (x64, x86, amd64, ARM64, etc.)
        public String checksum;  // Optional checksum for verification (MD5/SHA256)
        
        // Constructor without checksum
        public OSEntry(String name, String version, String url, String arch) {
            this.name = name;
            this.version = version;
            this.url = url;
            this.arch = arch;
            this.checksum = null;
        }
        
        // Constructor with checksum for integrity verification
        public OSEntry(String name, String version, String url, String arch, String checksum) {
            this.name = name;
            this.version = version;
            this.url = url;
            this.arch = arch;
            this.checksum = checksum;
        }
    }
    
    /**
     * Category - Groups related Linux distributions together
     * Used for organizing the Linux menu into logical sections
     */
    public static class Category {
        public String id;           // Category identifier
        public String displayName;  // Human-readable category name
        public String[] distros;    // Array of distro IDs in this category
        
        public Category(String id, String displayName, String... distros) {
            this.id = id;
            this.displayName = displayName;
            this.distros = distros;
        }
    }

    // Maps OS IDs to human-readable display names
    private static final Map<String, String> DISPLAY_NAMES = new HashMap<>();
    
    // Maps OS IDs to lists of available downloads (versions/architectures)
    private static final Map<String, List<OSEntry>> OS_DOWNLOADS = new HashMap<>();
    
    // Static initializer - runs once when class is loaded
    // Populates all database maps with OS information
    static {
        initDisplayNames();      // Load friendly names for all OSes
        initWindows();           // Load Windows download links
        initLinuxDebian();       // Load Debian-based distributions
        initLinuxArch();         // Load Arch-based distributions
        initLinuxRPM();          // Load RPM-based distributions
        initLinuxSecurity();     // Load security/pentesting distributions
        initLinuxOther();        // Load other Linux distributions
        initBSD();               // Load BSD variants
        initMacOS();             // Load macOS versions
        initOther();             // Load alternative OSes and utilities
    }
    
    /**
     * Initialize display names for all operating systems
     * Maps internal IDs to user-friendly names shown in menus
     */
    private static void initDisplayNames() {
        // Windows consumer and enterprise editions
        DISPLAY_NAMES.put("windows11", "Windows 11");
        DISPLAY_NAMES.put("windows11ltsc", "Windows 11 LTSC");
        DISPLAY_NAMES.put("windows10", "Windows 10");
        DISPLAY_NAMES.put("windows10ltsc", "Windows 10 LTSC");
        DISPLAY_NAMES.put("windows81", "Windows 8.1");
        DISPLAY_NAMES.put("windows7", "Windows 7 SP1");
        DISPLAY_NAMES.put("windowsvista", "Windows Vista");
        DISPLAY_NAMES.put("windowsxp", "Windows XP");
        DISPLAY_NAMES.put("server2022", "Windows Server 2022");
        DISPLAY_NAMES.put("server2019", "Windows Server 2019");
        DISPLAY_NAMES.put("server2016", "Windows Server 2016");
        DISPLAY_NAMES.put("server2012r2", "Windows Server 2012 R2");
        DISPLAY_NAMES.put("tiny11", "Tiny11");
        DISPLAY_NAMES.put("tiny10", "Tiny10");
        DISPLAY_NAMES.put("windowspe", "Windows PE");
        
        // Linux - Debian-based distributions
        DISPLAY_NAMES.put("ubuntu-desktop", "Ubuntu Desktop");
        DISPLAY_NAMES.put("ubuntu-server", "Ubuntu Server");
        DISPLAY_NAMES.put("kubuntu", "Kubuntu");
        DISPLAY_NAMES.put("xubuntu", "Xubuntu");
        DISPLAY_NAMES.put("lubuntu", "Lubuntu");
        DISPLAY_NAMES.put("popos", "Pop!_OS");
        DISPLAY_NAMES.put("debian", "Debian");
        DISPLAY_NAMES.put("linuxmint", "Linux Mint");
        DISPLAY_NAMES.put("elementary", "elementary OS");
        DISPLAY_NAMES.put("zorin", "Zorin OS");
        DISPLAY_NAMES.put("mxlinux", "MX Linux");
        DISPLAY_NAMES.put("antix", "antiX");
        
        // Linux - Arch-based distributions
        DISPLAY_NAMES.put("archlinux", "Arch Linux");
        DISPLAY_NAMES.put("manjaro", "Manjaro");
        DISPLAY_NAMES.put("endeavouros", "EndeavourOS");
        DISPLAY_NAMES.put("garuda", "Garuda Linux");
        DISPLAY_NAMES.put("arcolinux", "ArcoLinux");
        
        // Linux - RPM-based distributions (Red Hat family)
        DISPLAY_NAMES.put("fedora-workstation", "Fedora Workstation");
        DISPLAY_NAMES.put("fedora-server", "Fedora Server");
        DISPLAY_NAMES.put("rocky", "Rocky Linux");
        DISPLAY_NAMES.put("almalinux", "AlmaLinux");
        DISPLAY_NAMES.put("centos-stream", "CentOS Stream");
        DISPLAY_NAMES.put("opensuse-tumbleweed", "openSUSE Tumbleweed");
        DISPLAY_NAMES.put("opensuse-leap", "openSUSE Leap");
        
        // Linux - Security and penetration testing distributions
        DISPLAY_NAMES.put("kali", "Kali Linux");
        DISPLAY_NAMES.put("parrot", "Parrot Security");
        DISPLAY_NAMES.put("blackarch", "BlackArch");
        DISPLAY_NAMES.put("tails", "Tails");
        DISPLAY_NAMES.put("whonix", "Whonix");
        
        // Linux - Lightweight and specialty distributions
        DISPLAY_NAMES.put("puppylinux", "Puppy Linux");
        DISPLAY_NAMES.put("tinycore", "Tiny Core Linux");
        DISPLAY_NAMES.put("alpine", "Alpine Linux");
        DISPLAY_NAMES.put("void", "Void Linux");
        DISPLAY_NAMES.put("slackware", "Slackware");
        DISPLAY_NAMES.put("gentoo", "Gentoo");
        DISPLAY_NAMES.put("nixos", "NixOS");
        DISPLAY_NAMES.put("clearlinux", "Clear Linux");
        DISPLAY_NAMES.put("steamos", "SteamOS");
        
        // BSD operating systems
        DISPLAY_NAMES.put("freebsd", "FreeBSD");
        DISPLAY_NAMES.put("openbsd", "OpenBSD");
        DISPLAY_NAMES.put("netbsd", "NetBSD");
        DISPLAY_NAMES.put("ghostbsd", "GhostBSD");
        DISPLAY_NAMES.put("dragonflybsd", "DragonFly BSD");
        
        // macOS versions
        DISPLAY_NAMES.put("macos-sequoia", "macOS Sequoia");
        DISPLAY_NAMES.put("macos-sonoma", "macOS Sonoma");
        DISPLAY_NAMES.put("macos-ventura", "macOS Ventura");
        DISPLAY_NAMES.put("macos-monterey", "macOS Monterey");
        
        // Alternative and hobby operating systems
        DISPLAY_NAMES.put("chromeos-flex", "ChromeOS Flex");
        DISPLAY_NAMES.put("chromiumos", "Chromium OS");
        DISPLAY_NAMES.put("android-x86", "Android x86");
        DISPLAY_NAMES.put("blissos", "Bliss OS");
        DISPLAY_NAMES.put("primeos", "PrimeOS");
        DISPLAY_NAMES.put("solaris", "Oracle Solaris");
        DISPLAY_NAMES.put("openindiana", "OpenIndiana");
        DISPLAY_NAMES.put("minix", "Minix");
        DISPLAY_NAMES.put("plan9", "Plan 9 / 9front");
        DISPLAY_NAMES.put("haiku", "Haiku");
        DISPLAY_NAMES.put("reactos", "ReactOS");
        DISPLAY_NAMES.put("freedos", "FreeDOS");
        DISPLAY_NAMES.put("kolibrios", "KolibriOS");
        DISPLAY_NAMES.put("menuetos", "MenuetOS");
        DISPLAY_NAMES.put("templeos", "TempleOS / Shrine");
        DISPLAY_NAMES.put("serenityos", "SerenityOS");
        
        // Recovery and utility tools
        DISPLAY_NAMES.put("hirens", "Hiren's BootCD PE");
        DISPLAY_NAMES.put("systemrescue", "SystemRescue");
        DISPLAY_NAMES.put("gparted", "GParted Live");
        DISPLAY_NAMES.put("clonezilla", "Clonezilla");
        DISPLAY_NAMES.put("dban", "DBAN (Secure Erase)");
        DISPLAY_NAMES.put("memtest86", "Memtest86+");
        DISPLAY_NAMES.put("medicat", "MediCat USB");
        DISPLAY_NAMES.put("ventoy", "Ventoy");
        DISPLAY_NAMES.put("rescuezilla", "Rescuezilla");
        DISPLAY_NAMES.put("supergrub", "Super Grub2 Disk");
        DISPLAY_NAMES.put("bootrepair", "Boot Repair Disk");
    }
    
    /**
     * Initialize Windows operating system downloads
     * Includes consumer editions, LTSC versions, Server editions, and lightweight variants
     */
    private static void initWindows() {
        // Windows 11 - Latest consumer OS with multiple architecture support
        OS_DOWNLOADS.put("windows11", Arrays.asList(
            new OSEntry("Windows 11", "24H2 (Latest)", "https://software.download.prss.microsoft.com/dbazure/Win11_24H2_English_x64.iso", "x64"),
            new OSEntry("Windows 11", "24H2 (Latest)", "https://software.download.prss.microsoft.com/dbazure/Win11_24H2_English_Arm64.iso", "ARM64"),
            new OSEntry("Windows 11", "23H2", "https://software.download.prss.microsoft.com/dbazure/Win11_23H2_English_x64.iso", "x64")
        ));
        
        // Windows 11 LTSC - Long-Term Servicing Channel for enterprise stability
        OS_DOWNLOADS.put("windows11ltsc", Arrays.asList(
            new OSEntry("Windows 11 Enterprise LTSC", "2024 (Evaluation)", "https://software-static.download.prss.microsoft.com/dbazure/Win11_Enterprise_LTSC_2024_x64.iso", "x64")
        ));
        
        // Windows 10 - Stable enterprise-grade OS with extended support
        OS_DOWNLOADS.put("windows10", Arrays.asList(
            new OSEntry("Windows 10", "22H2 (Latest)", "https://software.download.prss.microsoft.com/dbazure/Win10_22H2_English_x64.iso", "x64"),
            new OSEntry("Windows 10", "22H2 (Latest)", "https://software.download.prss.microsoft.com/dbazure/Win10_22H2_English_x32.iso", "x86")
        ));
        
        // Windows 10 LTSC - Enterprise long-term support versions
        OS_DOWNLOADS.put("windows10ltsc", Arrays.asList(
            new OSEntry("Windows 10 Enterprise LTSC", "2021 (Evaluation)", "https://software-static.download.prss.microsoft.com/sg/Win10_Enterprise_LTSC_2021_x64.iso", "x64"),
            new OSEntry("Windows 10 Enterprise LTSC", "2021 (Evaluation)", "https://software-static.download.prss.microsoft.com/sg/Win10_Enterprise_LTSC_2021_x32.iso", "x86"),
            new OSEntry("Windows 10 Enterprise LTSC", "2019", "https://software-static.download.prss.microsoft.com/pr/Win10_Enterprise_LTSC_2019_x64.iso", "x64")
        ));
        
        // Windows 8.1 - Legacy support for older systems
        OS_DOWNLOADS.put("windows81", Arrays.asList(
            new OSEntry("Windows 8.1", "Update 3", "https://software.download.prss.microsoft.com/pr/Win8.1_English_x64.iso", "x64"),
            new OSEntry("Windows 8.1", "Update 3", "https://software.download.prss.microsoft.com/pr/Win8.1_English_x32.iso", "x86")
        ));
        
        // Windows 7 - Classic OS with various editions from Archive.org
        OS_DOWNLOADS.put("windows7", Arrays.asList(
            new OSEntry("Windows 7 SP1", "Ultimate", "https://archive.org/download/Windows7UltimateX64/Windows%207%20Ultimate%20x64.iso", "x64"),
            new OSEntry("Windows 7 SP1", "Ultimate", "https://archive.org/download/windows-7-ultimate-x86_202201/Windows%207%20Ultimate%20x86.iso", "x86"),
            new OSEntry("Windows 7 SP1", "Professional", "https://archive.org/download/win-7-pro-english-x-64/Win7_Pro_English_x64.iso", "x64"),
            new OSEntry("Windows 7 SP1", "Home Premium", "https://archive.org/download/windows-7-home-premium-x64_202104/Windows%207%20Home%20Premium%20x64.iso", "x64")
        ));
        
        // Windows Vista - Historical OS versions from Archive.org
        OS_DOWNLOADS.put("windowsvista", Arrays.asList(
            new OSEntry("Windows Vista SP2", "Ultimate", "https://archive.org/download/windows-vista-ultimate-sp-2-x-64/Windows%20Vista%20Ultimate%20SP2%20x64.iso", "x64"),
            new OSEntry("Windows Vista SP2", "Ultimate", "https://archive.org/download/windows-vista-ultimate-sp-2-x-86/Windows%20Vista%20Ultimate%20SP2%20x86.iso", "x86"),
            new OSEntry("Windows Vista SP2", "Business", "https://archive.org/download/windows-vista-business-sp-2/Windows%20Vista%20Business%20SP2.iso", "x86")
        ));
        
        // Windows XP - Legacy OS for retro computing from Archive.org
        OS_DOWNLOADS.put("windowsxp", Arrays.asList(
            new OSEntry("Windows XP SP3", "Professional", "https://archive.org/download/WinXPProSP3x86/en_windows_xp_professional_with_service_pack_3_x86.iso", "x86"),
            new OSEntry("Windows XP SP2", "Professional x64", "https://archive.org/download/windowsxpprofessionalx64/WindowsXPProfessionalx64.iso", "x64"),
            new OSEntry("Windows XP SP3", "Home Edition", "https://archive.org/download/windows-xp-home-sp-3/Windows%20XP%20Home%20SP3.iso", "x86")
        ));
        
        // Windows Server 2022 - Latest server OS with evaluation versions
        OS_DOWNLOADS.put("server2022", Arrays.asList(
            new OSEntry("Windows Server 2022", "Standard (Evaluation)", "https://software-static.download.prss.microsoft.com/sg/Windows_Server_2022_x64.iso", "x64"),
            new OSEntry("Windows Server 2022", "Datacenter (Evaluation)", "https://software-static.download.prss.microsoft.com/sg/Windows_Server_2022_Datacenter_x64.iso", "x64")
        ));
        
        // Windows Server 2019 - Enterprise server platform
        OS_DOWNLOADS.put("server2019", Arrays.asList(
            new OSEntry("Windows Server 2019", "Standard (Evaluation)", "https://software-static.download.prss.microsoft.com/pr/Windows_Server_2019_x64.iso", "x64"),
            new OSEntry("Windows Server 2019", "Essentials (Evaluation)", "https://software-static.download.prss.microsoft.com/pr/Windows_Server_2019_Essentials_x64.iso", "x64")
        ));
        
        // Windows Server 2016 - Legacy server version
        OS_DOWNLOADS.put("server2016", Arrays.asList(
            new OSEntry("Windows Server 2016", "Standard (Evaluation)", "https://software-static.download.prss.microsoft.com/pr/Windows_Server_2016_x64.iso", "x64")
        ));
        
        // Windows Server 2012 R2 - Older but still supported server OS
        OS_DOWNLOADS.put("server2012r2", Arrays.asList(
            new OSEntry("Windows Server 2012 R2", "Standard (Evaluation)", "https://software-static.download.prss.microsoft.com/pr/Windows_Server_2012_R2_x64.iso", "x64")
        ));
        
        // Tiny11 - Lightweight Windows 11 with reduced bloat
        OS_DOWNLOADS.put("tiny11", Arrays.asList(
            new OSEntry("Tiny11", "2311", "https://archive.org/download/tiny-11-NTDEV/tiny11%202311%20x64.iso", "x64"),
            new OSEntry("Tiny11", "23H2", "https://archive.org/download/tiny-11-NTDEV/tiny11%2023H2%20x64.iso", "x64")
        ));
        
        // Tiny10 - Lightweight Windows 10 for older hardware
        OS_DOWNLOADS.put("tiny10", Arrays.asList(
            new OSEntry("Tiny10", "22H2", "https://archive.org/download/tiny-10_202307/tiny10%2022H2%20x64.iso", "x64"),
            new OSEntry("Tiny10", "21H2", "https://archive.org/download/tiny10-21h2/tiny10%20x64%2021H2.iso", "x64"),
            new OSEntry("Tiny10", "21H2", "https://archive.org/download/tiny10-21h2/tiny10%20x86%2021H2.iso", "x86")
        ));
        
        // Windows PE - Pre-installation environment for recovery and deployment
        OS_DOWNLOADS.put("windowspe", Arrays.asList(
            new OSEntry("Windows PE", "11 (Latest)", "https://archive.org/download/winpe11/WinPE11_x64.iso", "x64"),
            new OSEntry("Windows PE", "10", "https://archive.org/download/winpe10/WinPE10_x64.iso", "x64"),
            new OSEntry("Hiren's BootCD PE", "Latest", "https://www.hirensbootcd.org/files/HBCD_PE_x64.iso", "x64")
        ));
    }
    
    /**
     * Initialize Debian-based Linux distributions
     * Includes Ubuntu family, Debian, Linux Mint, and other Debian derivatives
     */
    private static void initLinuxDebian() {
        // Ubuntu Desktop - Most popular Linux desktop distribution
        OS_DOWNLOADS.put("ubuntu-desktop", Arrays.asList(
            new OSEntry("Ubuntu", "24.04.1 LTS (Noble Numbat)", "https://releases.ubuntu.com/24.04.1/ubuntu-24.04.1-desktop-amd64.iso", "amd64"),
            new OSEntry("Ubuntu", "24.10 (Oracular Oriole)", "https://releases.ubuntu.com/24.10/ubuntu-24.10-desktop-amd64.iso", "amd64"),
            new OSEntry("Ubuntu", "22.04.4 LTS (Jammy Jellyfish)", "https://releases.ubuntu.com/22.04.4/ubuntu-22.04.4-desktop-amd64.iso", "amd64"),
            new OSEntry("Ubuntu", "20.04.6 LTS (Focal Fossa)", "https://releases.ubuntu.com/20.04.6/ubuntu-20.04.6-desktop-amd64.iso", "amd64")
        ));
        
        // Ubuntu Server - Enterprise-grade server platform
        OS_DOWNLOADS.put("ubuntu-server", Arrays.asList(
            new OSEntry("Ubuntu Server", "24.04.1 LTS", "https://releases.ubuntu.com/24.04.1/ubuntu-24.04.1-live-server-amd64.iso", "amd64"),
            new OSEntry("Ubuntu Server", "24.10", "https://releases.ubuntu.com/24.10/ubuntu-24.10-live-server-amd64.iso", "amd64"),
            new OSEntry("Ubuntu Server", "22.04.4 LTS", "https://releases.ubuntu.com/22.04.4/ubuntu-22.04.4-live-server-amd64.iso", "amd64"),
            new OSEntry("Ubuntu Server", "20.04.6 LTS", "https://releases.ubuntu.com/20.04.6/ubuntu-20.04.6-live-server-amd64.iso", "amd64")
        ));
        
        // Kubuntu - Ubuntu with KDE Plasma desktop
        OS_DOWNLOADS.put("kubuntu", Arrays.asList(
            new OSEntry("Kubuntu", "24.04.1 LTS", "https://cdimage.ubuntu.com/kubuntu/releases/24.04.1/release/kubuntu-24.04.1-desktop-amd64.iso", "amd64"),
            new OSEntry("Kubuntu", "24.10", "https://cdimage.ubuntu.com/kubuntu/releases/24.10/release/kubuntu-24.10-desktop-amd64.iso", "amd64"),
            new OSEntry("Kubuntu", "22.04.4 LTS", "https://cdimage.ubuntu.com/kubuntu/releases/22.04.4/release/kubuntu-22.04.4-desktop-amd64.iso", "amd64")
        ));
        
        // Xubuntu - Ubuntu with Xfce desktop (lightweight)
        OS_DOWNLOADS.put("xubuntu", Arrays.asList(
            new OSEntry("Xubuntu", "24.04.1 LTS", "https://cdimage.ubuntu.com/xubuntu/releases/24.04.1/release/xubuntu-24.04.1-desktop-amd64.iso", "amd64"),
            new OSEntry("Xubuntu", "24.10", "https://cdimage.ubuntu.com/xubuntu/releases/24.10/release/xubuntu-24.10-desktop-amd64.iso", "amd64"),
            new OSEntry("Xubuntu", "22.04.4 LTS", "https://cdimage.ubuntu.com/xubuntu/releases/22.04.4/release/xubuntu-22.04.4-desktop-amd64.iso", "amd64")
        ));
        
        // Lubuntu - Ubuntu with LXQt desktop (very lightweight)
        OS_DOWNLOADS.put("lubuntu", Arrays.asList(
            new OSEntry("Lubuntu", "24.04.1 LTS", "https://cdimage.ubuntu.com/lubuntu/releases/24.04.1/release/lubuntu-24.04.1-desktop-amd64.iso", "amd64"),
            new OSEntry("Lubuntu", "24.10", "https://cdimage.ubuntu.com/lubuntu/releases/24.10/release/lubuntu-24.10-desktop-amd64.iso", "amd64"),
            new OSEntry("Lubuntu", "22.04.4 LTS", "https://cdimage.ubuntu.com/lubuntu/releases/22.04.4/release/lubuntu-22.04.4-desktop-amd64.iso", "amd64")
        ));
        
        // Pop!_OS - Ubuntu derivative by System76, optimized for developers
        OS_DOWNLOADS.put("popos", Arrays.asList(
            new OSEntry("Pop!_OS", "22.04 LTS", "https://iso.pop-os.org/22.04/amd64/intel/43/pop-os_22.04_amd64_intel_43.iso", "amd64 (Intel/AMD)"),
            new OSEntry("Pop!_OS", "22.04 LTS", "https://iso.pop-os.org/22.04/amd64/nvidia/43/pop-os_22.04_amd64_nvidia_43.iso", "amd64 (NVIDIA)")
        ));
        
        // Debian - The universal operating system, foundation for many distros
        OS_DOWNLOADS.put("debian", Arrays.asList(
            new OSEntry("Debian", "12.8 (Bookworm)", "https://cdimage.debian.org/debian-cd/current/amd64/iso-cd/debian-12.8.0-amd64-netinst.iso", "amd64"),
            new OSEntry("Debian", "12.8 (Bookworm) DVD", "https://cdimage.debian.org/debian-cd/current/amd64/iso-dvd/debian-12.8.0-amd64-DVD-1.iso", "amd64"),
            new OSEntry("Debian", "11.11 (Bullseye)", "https://cdimage.debian.org/debian-cd/11.11.0/amd64/iso-cd/debian-11.11.0-amd64-netinst.iso", "amd64")
        ));
        
        // Linux Mint - User-friendly Ubuntu derivative with multiple desktop options
        OS_DOWNLOADS.put("linuxmint", Arrays.asList(
            new OSEntry("Linux Mint", "22 Cinnamon", "https://mirrors.kernel.org/linuxmint/stable/22/linuxmint-22-cinnamon-64bit.iso", "64bit"),
            new OSEntry("Linux Mint", "22 MATE", "https://mirrors.kernel.org/linuxmint/stable/22/linuxmint-22-mate-64bit.iso", "64bit"),
            new OSEntry("Linux Mint", "22 Xfce", "https://mirrors.kernel.org/linuxmint/stable/22/linuxmint-22-xfce-64bit.iso", "64bit"),
            new OSEntry("Linux Mint", "21.3 Cinnamon", "https://mirrors.kernel.org/linuxmint/stable/21.3/linuxmint-21.3-cinnamon-64bit.iso", "64bit")
        ));
        
        // elementary OS - Beautiful, privacy-respecting OS for creative users
        OS_DOWNLOADS.put("elementary", Arrays.asList(
            new OSEntry("elementary OS", "8.0 Circe", "https://ams3.dl.elementary.io/download/elementary-os-8.0-stable.20241211.iso", "amd64"),
            new OSEntry("elementary OS", "7.1 Horus", "https://ams3.dl.elementary.io/download/elementary-os-7.1-stable.20231022.iso", "amd64")
        ));
        
        // Zorin OS - Windows-like interface for easy switching from Windows
        OS_DOWNLOADS.put("zorin", Arrays.asList(
            new OSEntry("Zorin OS", "17.2 Core", "https://mirrors.edge.kernel.org/zorinos/17/Zorin-OS-17.2-Core-64-bit.iso", "64bit"),
            new OSEntry("Zorin OS", "17.2 Lite", "https://mirrors.edge.kernel.org/zorinos/17/Zorin-OS-17.2-Lite-64-bit.iso", "64bit"),
            new OSEntry("Zorin OS", "17.2 Education", "https://mirrors.edge.kernel.org/zorinos/17/Zorin-OS-17.2-Education-64-bit.iso", "64bit")
        ));
        
        // MX Linux - Midweight distro combining Debian stability with Xfce/KDE
        OS_DOWNLOADS.put("mxlinux", Arrays.asList(
            new OSEntry("MX Linux", "23.4 Xfce", "https://sourceforge.net/projects/mx-linux/files/Final/Xfce/MX-23.4_x64.iso", "x64"),
            new OSEntry("MX Linux", "23.4 KDE", "https://sourceforge.net/projects/mx-linux/files/Final/KDE/MX-23.4_KDE_x64.iso", "x64"),
            new OSEntry("MX Linux", "23.4 Fluxbox", "https://sourceforge.net/projects/mx-linux/files/Final/Fluxbox/MX-23.4_fluxbox_x64.iso", "x64")
        ));
        
        // antiX - Ultra-lightweight Debian-based distro for older hardware
        OS_DOWNLOADS.put("antix", Arrays.asList(
            new OSEntry("antiX", "23.1 Full", "https://sourceforge.net/projects/antix-linux/files/Final/antiX-23.1/antiX-23.1_x64-full.iso", "x64"),
            new OSEntry("antiX", "23.1 Base", "https://sourceforge.net/projects/antix-linux/files/Final/antiX-23.1/antiX-23.1_x64-base.iso", "x64"),
            new OSEntry("antiX", "23.1 Core", "https://sourceforge.net/projects/antix-linux/files/Final/antiX-23.1/antiX-23.1_x64-core.iso", "x64")
        ));
    }
    
    /**
     * Initialize Arch-based Linux distributions
     * Rolling release distros known for cutting-edge software and customization
     */
    private static void initLinuxArch() {
        // Arch Linux - Minimalist, rolling release for advanced users
        OS_DOWNLOADS.put("archlinux", Arrays.asList(
            new OSEntry("Arch Linux", "Latest", "https://geo.mirror.pkgbuild.com/iso/latest/archlinux-x86_64.iso", "x86_64"),
            new OSEntry("Arch Linux", "Latest (Bootstrap)", "https://geo.mirror.pkgbuild.com/iso/latest/archlinux-bootstrap-x86_64.tar.zst", "x86_64")
        ));
        
        // Manjaro - User-friendly Arch derivative with GUI installer
        OS_DOWNLOADS.put("manjaro", Arrays.asList(
            new OSEntry("Manjaro", "KDE Plasma", "https://download.manjaro.org/kde/24.2.1/manjaro-kde-24.2.1-241219-linux612.iso", "x64"),
            new OSEntry("Manjaro", "GNOME", "https://download.manjaro.org/gnome/24.2.1/manjaro-gnome-24.2.1-241219-linux612.iso", "x64"),
            new OSEntry("Manjaro", "Xfce", "https://download.manjaro.org/xfce/24.2.1/manjaro-xfce-24.2.1-241219-linux612.iso", "x64")
        ));
        
        // EndeavourOS - Terminal-centric Arch installer with helpful community
        OS_DOWNLOADS.put("endeavouros", Arrays.asList(
            new OSEntry("EndeavourOS", "Endeavour", "https://mirror.alpix.eu/endeavouros/iso/EndeavourOS_Endeavour-2024.09.22.iso", "x86_64")
        ));
        
        // Garuda Linux - Performance-focused with gaming optimizations
        OS_DOWNLOADS.put("garuda", Arrays.asList(
            new OSEntry("Garuda Linux", "dr460nized", "https://iso.builds.garudalinux.org/iso/latest/garuda/dr460nized/latest.iso", "x86_64"),
            new OSEntry("Garuda Linux", "GNOME", "https://iso.builds.garudalinux.org/iso/latest/garuda/gnome/latest.iso", "x86_64"),
            new OSEntry("Garuda Linux", "Xfce", "https://iso.builds.garudalinux.org/iso/latest/garuda/xfce/latest.iso", "x86_64")
        ));
        
        // ArcoLinux - Educational Arch distro teaching Linux customization
        OS_DOWNLOADS.put("arcolinux", Arrays.asList(
            new OSEntry("ArcoLinux", "Latest", "https://sourceforge.net/projects/arcolinux/files/ArcoLinux/arcolinux-v24.12.01-x86_64.iso", "x86_64"),
            new OSEntry("ArcoLinuxB", "Plasma", "https://sourceforge.net/projects/arcolinux/files/ArcoLinuxB/arcolinuxb-plasma-v24.12.01-x86_64.iso", "x86_64"),
            new OSEntry("ArcoLinuxB", "Xfce", "https://sourceforge.net/projects/arcolinux/files/ArcoLinuxB/arcolinuxb-xfce-v24.12.01-x86_64.iso", "x86_64")
        ));
    }
    
    /**
     * Initialize RPM-based Linux distributions
     * Red Hat family including Fedora, RHEL clones, and SUSE variants
     */
    private static void initLinuxRPM() {
        // Fedora Workstation - Cutting-edge Red Hat sponsored desktop
        OS_DOWNLOADS.put("fedora-workstation", Arrays.asList(
            new OSEntry("Fedora Workstation", "41", "https://download.fedoraproject.org/pub/fedora/linux/releases/41/Workstation/x86_64/iso/Fedora-Workstation-Live-x86_64-41-1.4.iso", "x86_64"),
            new OSEntry("Fedora Workstation", "40", "https://download.fedoraproject.org/pub/fedora/linux/releases/40/Workstation/x86_64/iso/Fedora-Workstation-Live-x86_64-40-1.14.iso", "x86_64")
        ));
        
        // Fedora Server - Enterprise server platform with latest features
        OS_DOWNLOADS.put("fedora-server", Arrays.asList(
            new OSEntry("Fedora Server", "41", "https://download.fedoraproject.org/pub/fedora/linux/releases/41/Server/x86_64/iso/Fedora-Server-dvd-x86_64-41-1.4.iso", "x86_64"),
            new OSEntry("Fedora Server", "40", "https://download.fedoraproject.org/pub/fedora/linux/releases/40/Server/x86_64/iso/Fedora-Server-dvd-x86_64-40-1.14.iso", "x86_64")
        ));
        
        // Rocky Linux - Community RHEL rebuild for enterprise use
        OS_DOWNLOADS.put("rocky", Arrays.asList(
            new OSEntry("Rocky Linux", "9.5", "https://download.rockylinux.org/pub/rocky/9/isos/x86_64/Rocky-9.5-x86_64-dvd.iso", "x86_64"),
            new OSEntry("Rocky Linux", "9.5 Minimal", "https://download.rockylinux.org/pub/rocky/9/isos/x86_64/Rocky-9.5-x86_64-minimal.iso", "x86_64"),
            new OSEntry("Rocky Linux", "8.10", "https://download.rockylinux.org/pub/rocky/8/isos/x86_64/Rocky-8.10-x86_64-dvd1.iso", "x86_64")
        ));
        
        // AlmaLinux - Another RHEL rebuild with commercial support
        OS_DOWNLOADS.put("almalinux", Arrays.asList(
            new OSEntry("AlmaLinux", "9.5", "https://repo.almalinux.org/almalinux/9.5/isos/x86_64/AlmaLinux-9.5-x86_64-dvd.iso", "x86_64"),
            new OSEntry("AlmaLinux", "9.5 Minimal", "https://repo.almalinux.org/almalinux/9.5/isos/x86_64/AlmaLinux-9.5-x86_64-minimal.iso", "x86_64"),
            new OSEntry("AlmaLinux", "8.10", "https://repo.almalinux.org/almalinux/8.10/isos/x86_64/AlmaLinux-8.10-x86_64-dvd.iso", "x86_64")
        ));
        
        // CentOS Stream - Rolling release preview of upcoming RHEL
        OS_DOWNLOADS.put("centos-stream", Arrays.asList(
            new OSEntry("CentOS Stream", "9", "https://mirrors.centos.org/mirrorlist?path=/9-stream/BaseOS/x86_64/iso/CentOS-Stream-9-latest-x86_64-dvd1.iso", "x86_64"),
            new OSEntry("CentOS Stream", "9 Boot", "https://mirrors.centos.org/mirrorlist?path=/9-stream/BaseOS/x86_64/iso/CentOS-Stream-9-latest-x86_64-boot.iso", "x86_64")
        ));
        
        // openSUSE Tumbleweed - Rolling release with stability focus
        OS_DOWNLOADS.put("opensuse-tumbleweed", Arrays.asList(
            new OSEntry("openSUSE Tumbleweed", "Latest", "https://download.opensuse.org/tumbleweed/iso/openSUSE-Tumbleweed-DVD-x86_64-Current.iso", "x86_64"),
            new OSEntry("openSUSE Tumbleweed", "NET", "https://download.opensuse.org/tumbleweed/iso/openSUSE-Tumbleweed-NET-x86_64-Current.iso", "x86_64")
        ));
        
        // openSUSE Leap - Stable release based on SUSE Linux Enterprise
        OS_DOWNLOADS.put("opensuse-leap", Arrays.asList(
            new OSEntry("openSUSE Leap", "15.6", "https://download.opensuse.org/distribution/leap/15.6/iso/openSUSE-Leap-15.6-DVD-x86_64-Media.iso", "x86_64"),
            new OSEntry("openSUSE Leap", "15.6 NET", "https://download.opensuse.org/distribution/leap/15.6/iso/openSUSE-Leap-15.6-NET-x86_64-Media.iso", "x86_64")
        ));
    }
    
    /**
     * Initialize security and penetration testing distributions
     * Specialized distros with pre-installed security tools
     */
    private static void initLinuxSecurity() {
        // Kali Linux - Premier penetration testing platform
        OS_DOWNLOADS.put("kali", Arrays.asList(
            new OSEntry("Kali Linux", "2024.4", "https://cdimage.kali.org/kali-2024.4/kali-linux-2024.4-installer-amd64.iso", "amd64"),
            new OSEntry("Kali Linux", "2024.4 Live", "https://cdimage.kali.org/kali-2024.4/kali-linux-2024.4-live-amd64.iso", "amd64"),
            new OSEntry("Kali Linux", "2024.4 NetInstaller", "https://cdimage.kali.org/kali-2024.4/kali-linux-2024.4-installer-netinst-amd64.iso", "amd64")
        ));
        
        // Parrot Security - Security testing with focus on cloud pentesting
        OS_DOWNLOADS.put("parrot", Arrays.asList(
            new OSEntry("Parrot Security", "6.2", "https://deb.parrot.sh/parrot/iso/6.2/Parrot-security-6.2_amd64.iso", "amd64"),
            new OSEntry("Parrot Home", "6.2", "https://deb.parrot.sh/parrot/iso/6.2/Parrot-home-6.2_amd64.iso", "amd64"),
            new OSEntry("Parrot HTB", "6.2", "https://deb.parrot.sh/parrot/iso/6.2/Parrot-htb-6.2_amd64.iso", "amd64")
        ));
        
        // BlackArch - Arch-based with 2800+ penetration testing tools
        OS_DOWNLOADS.put("blackarch", Arrays.asList(
            new OSEntry("BlackArch", "Latest Full", "https://ftp.halifax.rwth-aachen.de/blackarch/iso/blackarch-linux-full-2024.09.01-x86_64.iso", "x86_64"),
            new OSEntry("BlackArch", "Latest Slim", "https://ftp.halifax.rwth-aachen.de/blackarch/iso/blackarch-linux-slim-2024.09.01-x86_64.iso", "x86_64"),
            new OSEntry("BlackArch", "Latest Netinst", "https://ftp.halifax.rwth-aachen.de/blackarch/iso/blackarch-linux-netinst-2024.09.01-x86_64.iso", "x86_64")
        ));
        
        // Tails - Privacy-focused live OS routing through Tor
        OS_DOWNLOADS.put("tails", Arrays.asList(
            new OSEntry("Tails", "6.11", "https://download.tails.net/tails/stable/tails-amd64-6.11/tails-amd64-6.11.iso", "amd64")
        ));
        
        // Whonix - Anonymous OS using Tor isolation in VMs
        OS_DOWNLOADS.put("whonix", Arrays.asList(
            new OSEntry("Whonix Gateway", "17", "https://download.whonix.org/ova/17.2.3.1/Whonix-Gateway-Xfce-17.2.3.1.ova", "x86_64 (OVA)"),
            new OSEntry("Whonix Workstation", "17", "https://download.whonix.org/ova/17.2.3.1/Whonix-Workstation-Xfce-17.2.3.1.ova", "x86_64 (OVA)")
        ));
    }
    
    /**
     * Initialize other Linux distributions
     * Lightweight, source-based, and specialty distributions
     */
    private static void initLinuxOther() {
        // Puppy Linux - Extremely lightweight, runs entirely in RAM
        OS_DOWNLOADS.put("puppylinux", Arrays.asList(
            new OSEntry("Puppy Linux", "FossaPup64 9.5", "https://distro.ibiblio.org/puppylinux/puppy-fossa/fossapup64-9.5.iso", "64bit"),
            new OSEntry("Puppy Linux", "BionicPup64 8.0", "https://distro.ibiblio.org/puppylinux/puppy-bionic/bionicpup64/bionicpup64-8.0-uefi.iso", "64bit")
        ));
        
        // Tiny Core Linux - Minimal Linux (12MB base) for extreme customization
        OS_DOWNLOADS.put("tinycore", Arrays.asList(
            new OSEntry("Tiny Core Linux", "15.0 Core", "http://tinycorelinux.net/15.x/x86/release/Core-current.iso", "x86"),
            new OSEntry("Tiny Core Linux", "15.0 TinyCore", "http://tinycorelinux.net/15.x/x86/release/TinyCore-current.iso", "x86"),
            new OSEntry("Tiny Core Linux", "15.0 CorePlus", "http://tinycorelinux.net/15.x/x86/release/CorePlus-current.iso", "x86"),
            new OSEntry("Tiny Core Linux", "15.0 64bit", "http://tinycorelinux.net/15.x/x86_64/release/TinyCorePure64-current.iso", "x86_64")
        ));
        
        // Alpine Linux - Security-focused, minimal distro for containers
        OS_DOWNLOADS.put("alpine", Arrays.asList(
            new OSEntry("Alpine Linux", "3.21 Standard", "https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/x86_64/alpine-standard-3.21.0-x86_64.iso", "x86_64"),
            new OSEntry("Alpine Linux", "3.21 Extended", "https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/x86_64/alpine-extended-3.21.0-x86_64.iso", "x86_64"),
            new OSEntry("Alpine Linux", "3.21 Virtual", "https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/x86_64/alpine-virt-3.21.0-x86_64.iso", "x86_64")
        ));
        
        // Void Linux - Independent rolling release with runit init
        OS_DOWNLOADS.put("void", Arrays.asList(
            new OSEntry("Void Linux", "Latest glibc", "https://repo-default.voidlinux.org/live/current/void-live-x86_64-20240314-base.iso", "x86_64"),
            new OSEntry("Void Linux", "Latest musl", "https://repo-default.voidlinux.org/live/current/void-live-x86_64-musl-20240314-base.iso", "x86_64 musl"),
            new OSEntry("Void Linux", "Latest Xfce", "https://repo-default.voidlinux.org/live/current/void-live-x86_64-20240314-xfce.iso", "x86_64")
        ));
        
        // Slackware - Oldest surviving Linux distro, UNIX-like simplicity
        OS_DOWNLOADS.put("slackware", Arrays.asList(
            new OSEntry("Slackware", "15.0 DVD", "https://mirrors.slackware.com/slackware/slackware-iso/slackware64-15.0-iso/slackware64-15.0-install-dvd.iso", "x86_64"),
            new OSEntry("Slackware", "15.0 DVD1", "https://mirrors.slackware.com/slackware/slackware-iso/slackware64-15.0-iso/slackware64-15.0-install-dvd1.iso", "x86_64")
        ));
        
        // Gentoo - Source-based with extreme customization and optimization
        OS_DOWNLOADS.put("gentoo", Arrays.asList(
            new OSEntry("Gentoo", "Latest Minimal", "https://bouncer.gentoo.org/fetch/root/all/releases/amd64/autobuilds/current-install-amd64-minimal/install-amd64-minimal-20241229T170402Z.iso", "amd64"),
            new OSEntry("Gentoo", "Latest LiveGUI", "https://bouncer.gentoo.org/fetch/root/all/releases/amd64/autobuilds/current-livegui-amd64/livegui-amd64-20241229T170402Z.iso", "amd64")
        ));
        
        // NixOS - Declarative, reproducible system configuration
        OS_DOWNLOADS.put("nixos", Arrays.asList(
            new OSEntry("NixOS", "24.11 GNOME", "https://channels.nixos.org/nixos-24.11/latest-nixos-gnome-x86_64-linux.iso", "x86_64"),
            new OSEntry("NixOS", "24.11 Plasma", "https://channels.nixos.org/nixos-24.11/latest-nixos-plasma-x86_64-linux.iso", "x86_64"),
            new OSEntry("NixOS", "24.11 Minimal", "https://channels.nixos.org/nixos-24.11/latest-nixos-minimal-x86_64-linux.iso", "x86_64")
        ));
        
        // Clear Linux - Intel-optimized for maximum performance
        OS_DOWNLOADS.put("clearlinux", Arrays.asList(
            new OSEntry("Clear Linux", "Latest Desktop", "https://cdn.download.clearlinux.org/releases/current/clear/clear-live-desktop.iso", "x86_64"),
            new OSEntry("Clear Linux", "Latest Server", "https://cdn.download.clearlinux.org/releases/current/clear/clear-live-server.iso", "x86_64")
        ));
        
        // SteamOS - Gaming-focused OS for Steam Deck and PCs
        OS_DOWNLOADS.put("steamos", Arrays.asList(
            new OSEntry("SteamOS", "3.0 (Holo)", "https://steamdeck-images.steampowered.com/recovery/steamdeck-recovery-4.img.bz2", "x86_64 (Recovery)"),
            new OSEntry("HoloISO", "Latest", "https://github.com/HoloISO/releases/releases/download/beta3/holoiso-beta3.iso", "x86_64")
        ));
    }
    
    /**
     * Initialize BSD operating systems
     * Unix-like systems derived from Berkeley Software Distribution
     */
    private static void initBSD() {
        // FreeBSD - Popular BSD with broad hardware support
        OS_DOWNLOADS.put("freebsd", Arrays.asList(
            new OSEntry("FreeBSD", "14.2", "https://download.freebsd.org/releases/amd64/amd64/ISO-IMAGES/14.2/FreeBSD-14.2-RELEASE-amd64-dvd1.iso", "amd64"),
            new OSEntry("FreeBSD", "14.2 Disc1", "https://download.freebsd.org/releases/amd64/amd64/ISO-IMAGES/14.2/FreeBSD-14.2-RELEASE-amd64-disc1.iso", "amd64"),
            new OSEntry("FreeBSD", "13.4", "https://download.freebsd.org/releases/amd64/amd64/ISO-IMAGES/13.4/FreeBSD-13.4-RELEASE-amd64-dvd1.iso", "amd64")
        ));
        
        // OpenBSD - Security-focused BSD with proactive security features
        OS_DOWNLOADS.put("openbsd", Arrays.asList(
            new OSEntry("OpenBSD", "7.6", "https://cdn.openbsd.org/pub/OpenBSD/7.6/amd64/install76.iso", "amd64"),
            new OSEntry("OpenBSD", "7.6 CD", "https://cdn.openbsd.org/pub/OpenBSD/7.6/amd64/cd76.iso", "amd64")
        ));
        
        // NetBSD - Portable BSD running on 50+ platforms
        OS_DOWNLOADS.put("netbsd", Arrays.asList(
            new OSEntry("NetBSD", "10.0", "https://cdn.netbsd.org/pub/NetBSD/NetBSD-10.0/images/NetBSD-10.0-amd64.iso", "amd64"),
            new OSEntry("NetBSD", "9.4", "https://cdn.netbsd.org/pub/NetBSD/NetBSD-9.4/images/NetBSD-9.4-amd64.iso", "amd64")
        ));
        
        // GhostBSD - User-friendly FreeBSD with desktop environment
        OS_DOWNLOADS.put("ghostbsd", Arrays.asList(
            new OSEntry("GhostBSD", "24.10.1 MATE", "https://download.ghostbsd.org/releases/amd64/24.10.1/GhostBSD-24.10.1-MATE.iso", "amd64"),
            new OSEntry("GhostBSD", "24.10.1 Xfce", "https://download.ghostbsd.org/releases/amd64/24.10.1/GhostBSD-24.10.1-Xfce.iso", "amd64")
        ));
        
        // DragonFly BSD - Fork of FreeBSD with advanced filesystem (HAMMER)
        OS_DOWNLOADS.put("dragonflybsd", Arrays.asList(
            new OSEntry("DragonFly BSD", "6.4.0", "https://mirror-master.dragonflybsd.org/iso-images/dfly-x86_64-6.4.0_REL.iso", "x86_64")
        ));
    }
    
    /**
     * Initialize macOS versions
     * Note: These are InstallAssistant packages, not bootable ISOs
     * Require Mac hardware or VM to create bootable media
     */
    private static void initMacOS() {
        // macOS Sequoia - Latest macOS version (15.x)
        OS_DOWNLOADS.put("macos-sequoia", Arrays.asList(
            new OSEntry("macOS Sequoia", "15.0", "https://swcdn.apple.com/content/downloads/sequoia/InstallAssistant.pkg", "Universal (PKG)")
        ));
        
        // macOS Sonoma - macOS 14.x
        OS_DOWNLOADS.put("macos-sonoma", Arrays.asList(
            new OSEntry("macOS Sonoma", "14.0", "https://swcdn.apple.com/content/downloads/sonoma/InstallAssistant.pkg", "Universal (PKG)")
        ));
        
        // macOS Ventura - macOS 13.x
        OS_DOWNLOADS.put("macos-ventura", Arrays.asList(
            new OSEntry("macOS Ventura", "13.0", "https://swcdn.apple.com/content/downloads/ventura/InstallAssistant.pkg", "Universal (PKG)")
        ));
        
        // macOS Monterey - macOS 12.x
        OS_DOWNLOADS.put("macos-monterey", Arrays.asList(
            new OSEntry("macOS Monterey", "12.0", "https://swcdn.apple.com/content/downloads/monterey/InstallAssistant.pkg", "Universal (PKG)")
        ));
    }
    
    /**
     * Initialize alternative operating systems and utility tools
     * Includes ChromeOS, Android x86, hobby OSes, and recovery tools
     */
    private static void initOther() {
        // Chrome OS and Android-based systems
        OS_DOWNLOADS.put("chromeos-flex", Arrays.asList(
            new OSEntry("ChromeOS Flex", "Latest", "https://dl.google.com/chromeos-flex/images/latest/chromeos_flex_image.bin.zip", "x86_64")
        ));
        OS_DOWNLOADS.put("chromiumos", Arrays.asList(
            new OSEntry("Chromium OS", "Latest", "https://chromium.arnoldthebat.co.uk/dates/latest-stable.img.7z", "x86_64")
        ));
        OS_DOWNLOADS.put("android-x86", Arrays.asList(
            new OSEntry("Android x86", "9.0-r2", "https://sourceforge.net/projects/android-x86/files/Release%209.0/android-x86_64-9.0-r2.iso", "x86_64"),
            new OSEntry("Android x86", "8.1-r6", "https://sourceforge.net/projects/android-x86/files/Release%208.1/android-x86_64-8.1-r6.iso", "x86_64")
        ));
        OS_DOWNLOADS.put("blissos", Arrays.asList(
            new OSEntry("Bliss OS", "16.9.4", "https://sourceforge.net/projects/blissos-x86/files/Official/BlissOS-16.9.4-x86_64-foss-gapps.iso", "x86_64"),
            new OSEntry("Bliss OS", "15.8.6", "https://sourceforge.net/projects/blissos-x86/files/Official/BlissOS-15.8.6-x86_64-foss-gapps.iso", "x86_64")
        ));
        OS_DOWNLOADS.put("primeos", Arrays.asList(
            new OSEntry("PrimeOS", "2.6.0 Standard", "https://sourceforge.net/projects/primeos/files/64-bit/PrimeOS-2.6.0-Standard-64-bit.iso", "x86_64"),
            new OSEntry("PrimeOS", "2.6.0 Classic", "https://sourceforge.net/projects/primeos/files/64-bit/PrimeOS-2.6.0-Classic-64-bit.iso", "x86_64")
        ));
        
        // Unix and Unix-like systems
        OS_DOWNLOADS.put("solaris", Arrays.asList(
            new OSEntry("Oracle Solaris", "11.4", "https://www.oracle.com/solaris/solaris11/downloads/solaris-downloads.html", "x86_64 (Manual)"),
            new OSEntry("OpenSolaris", "b134", "https://archive.org/download/opensolaris-b134/osol-1002-56-x86.iso", "x86")
        ));
        OS_DOWNLOADS.put("openindiana", Arrays.asList(
            new OSEntry("OpenIndiana", "2024.04 Hipster", "https://dlc.openindiana.org/isos/hipster/20240426/OI-hipster-gui-20240426.iso", "x86_64"),
            new OSEntry("OpenIndiana", "2024.04 Text", "https://dlc.openindiana.org/isos/hipster/20240426/OI-hipster-text-20240426.iso", "x86_64")
        ));
        OS_DOWNLOADS.put("minix", Arrays.asList(
            new OSEntry("Minix", "3.4.0", "https://download.minix3.org/iso/minix_R3.4.0-d5e4fc0.iso.bz2", "x86")
        ));
        OS_DOWNLOADS.put("plan9", Arrays.asList(
            new OSEntry("Plan 9", "4th Edition", "https://9p.io/plan9/download/plan9.iso.bz2", "x86"),
            new OSEntry("9front", "Latest", "http://9front.org/iso/9front-10522.amd64.iso.gz", "amd64")
        ));
        
        // Alternative and hobby operating systems
        OS_DOWNLOADS.put("haiku", Arrays.asList(
            new OSEntry("Haiku", "R1/beta5", "https://cdn.haiku-os.org/haiku-release/r1beta5/haiku-r1beta5-x86_64-anyboot.iso", "x86_64")
        ));
        OS_DOWNLOADS.put("reactos", Arrays.asList(
            new OSEntry("ReactOS", "0.4.14", "https://sourceforge.net/projects/reactos/files/ReactOS/0.4.14/ReactOS-0.4.14-iso.zip", "x86"),
            new OSEntry("ReactOS", "0.4.14 Live", "https://sourceforge.net/projects/reactos/files/ReactOS/0.4.14/ReactOS-0.4.14-Live.zip", "x86")
        ));
        OS_DOWNLOADS.put("templeos", Arrays.asList(
            new OSEntry("TempleOS", "5.03 (Final)", "https://archive.org/download/TempleOS_ISO_Archive/TempleOS.ISO", "x86_64"),
            new OSEntry("TempleOS", "5.03 Lite", "https://archive.org/download/TempleOS_ISO_Archive/TempleOSLite.ISO", "x86_64"),
            new OSEntry("Shrine", "0.9 (Fork)", "https://github.com/minexew/Shrine/releases/download/v0.9/shrine-v0.9.iso", "x86_64")
        ));
        OS_DOWNLOADS.put("menuetos", Arrays.asList(
            new OSEntry("MenuetOS", "64bit", "https://menuetos.net/download.php?id=M64-V1.52.60.zip", "x64"),
            new OSEntry("MenuetOS", "32bit", "https://menuetos.net/download.php?id=M32-1.39.26.zip", "x86")
        ));
        OS_DOWNLOADS.put("kolibrios", Arrays.asList(
            new OSEntry("KolibriOS", "Latest", "https://builds.kolibrios.org/eng/latest-iso.7z", "x86")
        ));
        OS_DOWNLOADS.put("serenityos", Arrays.asList(
            new OSEntry("SerenityOS", "Latest", "https://github.com/SerenityOS/serenity/releases/latest", "x86_64 (Build Required)")
        ));
        OS_DOWNLOADS.put("freedos", Arrays.asList(
            new OSEntry("FreeDOS", "1.3", "https://www.ibiblio.org/pub/micro/pc-stuff/freedos/files/distributions/1.3/official/FD13-FullUSB.zip", "x86"),
            new OSEntry("FreeDOS", "1.3 LiveCD", "https://www.ibiblio.org/pub/micro/pc-stuff/freedos/files/distributions/1.3/official/FD13-LiveCD.zip", "x86")
        ));
        
        // Recovery and utility tools for system repair and diagnostics
        OS_DOWNLOADS.put("hirens", Arrays.asList(
            new OSEntry("Hiren's BootCD PE", "1.0.2", "https://www.hirensbootcd.org/files/HBCD_PE_x64.iso", "x64")
        ));
        OS_DOWNLOADS.put("systemrescue", Arrays.asList(
            new OSEntry("SystemRescue", "11.03", "https://sourceforge.net/projects/systemrescuecd/files/sysresccd-x86/11.03/systemrescue-11.03-amd64.iso", "amd64"),
            new OSEntry("SystemRescue", "10.02", "https://sourceforge.net/projects/systemrescuecd/files/sysresccd-x86/10.02/systemrescue-10.02-amd64.iso", "amd64")
        ));
        OS_DOWNLOADS.put("gparted", Arrays.asList(
            new OSEntry("GParted Live", "1.6.0-3", "https://sourceforge.net/projects/gparted/files/gparted-live-stable/1.6.0-3/gparted-live-1.6.0-3-amd64.iso", "amd64"),
            new OSEntry("GParted Live", "1.6.0-3 i686", "https://sourceforge.net/projects/gparted/files/gparted-live-stable/1.6.0-3/gparted-live-1.6.0-3-i686.iso", "i686")
        ));
        OS_DOWNLOADS.put("clonezilla", Arrays.asList(
            new OSEntry("Clonezilla", "3.1.3-16 Stable", "https://sourceforge.net/projects/clonezilla/files/clonezilla_live_stable/3.1.3-16/clonezilla-live-3.1.3-16-amd64.iso", "amd64"),
            new OSEntry("Clonezilla", "3.1.3-16 i686", "https://sourceforge.net/projects/clonezilla/files/clonezilla_live_stable/3.1.3-16/clonezilla-live-3.1.3-16-i686.iso", "i686")
        ));
        OS_DOWNLOADS.put("dban", Arrays.asList(
            new OSEntry("DBAN", "2.3.0", "https://sourceforge.net/projects/dban/files/dban/dban-2.3.0/dban-2.3.0_i586.iso", "i586")
        ));
        OS_DOWNLOADS.put("memtest86", Arrays.asList(
            new OSEntry("Memtest86+", "7.00", "https://memtest.org/download/v7.00/mt86plus_7.00_64.iso.zip", "x86_64"),
            new OSEntry("Memtest86+", "6.20", "https://memtest.org/download/v6.20/mt86plus_6.20_64.iso.zip", "x86_64"),
            new OSEntry("Memtest86+ Legacy", "5.31b", "https://memtest.org/download/5.31b/memtest86+-5.31b.iso.zip", "x86")
        ));
        OS_DOWNLOADS.put("medicat", Arrays.asList(
            new OSEntry("MediCat USB", "Latest", "https://medicatusb.com/", "x86_64 (Manual)")
        ));
        OS_DOWNLOADS.put("ventoy", Arrays.asList(
            new OSEntry("Ventoy", "1.0.99", "https://github.com/ventoy/Ventoy/releases/download/v1.0.99/ventoy-1.0.99-linux.tar.gz", "Multi"),
            new OSEntry("Ventoy", "1.0.99 Windows", "https://github.com/ventoy/Ventoy/releases/download/v1.0.99/ventoy-1.0.99-windows.zip", "Multi")
        ));
        OS_DOWNLOADS.put("rescuezilla", Arrays.asList(
            new OSEntry("Rescuezilla", "2.5", "https://github.com/rescuezilla/rescuezilla/releases/download/2.5/rescuezilla-2.5-64bit.jammy.iso", "x86_64"),
            new OSEntry("Rescuezilla", "2.5 i386", "https://github.com/rescuezilla/rescuezilla/releases/download/2.5/rescuezilla-2.5-32bit.jammy.iso", "i386")
        ));
        OS_DOWNLOADS.put("supergrub", Arrays.asList(
            new OSEntry("Super Grub2 Disk", "2.06s4", "https://sourceforge.net/projects/supergrub2/files/2.06s4/super_grub2_disk_2.06s4/super_grub2_disk_hybrid_2.06s4.iso", "Multi")
        ));
        OS_DOWNLOADS.put("bootrepair", Arrays.asList(
            new OSEntry("Boot Repair Disk", "Latest", "https://sourceforge.net/projects/boot-repair-cd/files/boot-repair-disk-64bit.iso", "x86_64")
        ));
    }
    
    // ==================== PUBLIC API METHODS ====================
    
    /**
     * Get the display name for an OS ID
     * @param id Internal OS identifier
     * @return Human-readable display name, or the ID itself if not found
     */
    public static String getDisplayName(String id) {
        return DISPLAY_NAMES.getOrDefault(id, id);
    }
    
    /**
     * Get all available downloads for a specific OS
     * @param id Internal OS identifier
     * @return List of OSEntry objects with download information, or empty list if not found
     */
    public static List<OSEntry> getDownloads(String id) {
        return OS_DOWNLOADS.getOrDefault(id, new ArrayList<>());
    }
    
    /**
     * Get all available OS IDs in the database
     * @return Set of all OS identifiers
     */
    public static Set<String> getAllOSIds() {
        return OS_DOWNLOADS.keySet();
    }
    
    /**
     * Check if an OS exists in the database
     * @param id Internal OS identifier
     * @return true if OS has download entries, false otherwise
     */
    public static boolean hasOS(String id) {
        return OS_DOWNLOADS.containsKey(id);
    }
    
    // ==================== MENU STRUCTURE METHODS ====================
    
    /**
     * Get Windows menu structure
     * Returns array of [id, display_name] pairs for menu display
     * Includes separators marked with "---"
     */
    public static String[][] getWindowsMenu() {
        return new String[][] {
            {"windows11", "Windows 11 (Latest)"},
            {"windows11ltsc", "Windows 11 LTSC"},
            {"windows10", "Windows 10 (22H2)"},
            {"windows10ltsc", "Windows 10 LTSC"},
            {"windows81", "Windows 8.1"},
            {"windows7", "Windows 7 SP1"},
            {"windowsvista", "Windows Vista"},
            {"windowsxp", "Windows XP"},
            {"---", ""},  // Separator
            {"server2022", "Windows Server 2022"},
            {"server2019", "Windows Server 2019"},
            {"server2016", "Windows Server 2016"},
            {"server2012r2", "Windows Server 2012 R2"},
            {"---", ""},  // Separator
            {"tiny11", "Tiny11 (Lightweight W11)"},
            {"tiny10", "Tiny10 (Lightweight W10)"},
            {"windowspe", "Windows PE"}
        };
    }
    
    /**
     * Get Linux categories for organized menu display
     * Groups Linux distributions by package manager and purpose
     */
    public static Category[] getLinuxCategories() {
        return new Category[] {
            // Debian-based: APT package manager, beginner-friendly
            new Category("debian", "DEBIAN-BASED", 
                "ubuntu-desktop", "ubuntu-server", "kubuntu", "xubuntu", "lubuntu", 
                "popos", "debian", "linuxmint", "elementary", "zorin", "mxlinux", "antix"),
            
            // Arch-based: Pacman package manager, rolling release
            new Category("arch", "ARCH-BASED",
                "archlinux", "manjaro", "endeavouros", "garuda", "arcolinux"),
            
            // RPM-based: DNF/YUM package manager, enterprise focus
            new Category("rpm", "RPM-BASED",
                "fedora-workstation", "fedora-server", "rocky", "almalinux", 
                "centos-stream", "opensuse-tumbleweed", "opensuse-leap"),
            
            // Security: Penetration testing and privacy tools
            new Category("security", "SECURITY / PENTESTING",
                "kali", "parrot", "blackarch", "tails", "whonix"),
            
            // Other: Lightweight, source-based, and specialty distributions
            new Category("other", "LIGHTWEIGHT / SPECIALTY",
                "puppylinux", "tinycore", "alpine", "void", "slackware", 
                "gentoo", "nixos", "clearlinux", "steamos")
        };
    }
    
    /**
     * Get BSD menu structure
     * Returns array of [id, display_name] pairs for BSD variants
     */
    public static String[][] getBSDMenu() {
        return new String[][] {
            {"freebsd", "FreeBSD"},
            {"openbsd", "OpenBSD"},
            {"netbsd", "NetBSD"},
            {"ghostbsd", "GhostBSD"},
            {"dragonflybsd", "DragonFly BSD"}
        };
    }
    
    /**
     * Get macOS menu structure
     * Returns array of [id, display_name] pairs for macOS versions
     */
    public static String[][] getMacOSMenu() {
        return new String[][] {
            {"macos-sequoia", "macOS 15 Sequoia"},
            {"macos-sonoma", "macOS 14 Sonoma"},
            {"macos-ventura", "macOS 13 Ventura"},
            {"macos-monterey", "macOS 12 Monterey"}
        };
    }
    
    /**
     * Get "Other OS" menu structure
     * Includes ChromeOS, Android, Unix variants, hobby OSes, and recovery tools
     */
    public static String[][] getOtherMenu() {
        return new String[][] {
            // Chrome and Android systems
            {"chromeos-flex", "ChromeOS Flex"},
            {"chromiumos", "Chromium OS"},
            {"android-x86", "Android x86"},
            {"blissos", "Bliss OS (Android)"},
            {"primeos", "PrimeOS (Android)"},
            {"---", ""},  // Separator
            
            // Unix and Unix-like systems
            {"solaris", "Oracle Solaris"},
            {"openindiana", "OpenIndiana (illumos)"},
            {"minix", "Minix"},
            {"plan9", "Plan 9 / 9front"},
            {"---", ""},  // Separator
            
            // Alternative and hobby operating systems
            {"reactos", "ReactOS"},
            {"haiku", "Haiku"},
            {"templeos", "TempleOS / Shrine"},
            {"menuetos", "MenuetOS"},
            {"kolibrios", "KolibriOS"},
            {"serenityos", "SerenityOS"},
            {"freedos", "FreeDOS"},
            {"---", ""},  // Separator
            
            // Recovery and utility tools
            {"hirens", "Hiren's BootCD PE"},
            {"systemrescue", "SystemRescue"},
            {"gparted", "GParted Live"},
            {"clonezilla", "Clonezilla"},
            {"rescuezilla", "Rescuezilla"},
            {"dban", "DBAN (Secure Erase)"},
            {"memtest86", "Memtest86+"},
            {"ventoy", "Ventoy"},
            {"supergrub", "Super Grub2 Disk"},
            {"bootrepair", "Boot Repair Disk"},
            {"medicat", "MediCat USB"}
        };
    }
}
